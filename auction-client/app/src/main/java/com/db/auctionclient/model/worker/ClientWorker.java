package com.db.auctionclient.model.worker;

import com.db.auctionclient.model.entities.AuctionPhase;
import com.db.auctionclient.model.entities.AuctionTask;
import com.db.auctionclient.model.AuctionRepository;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import auctionplatform.protocol.AuctionConfiguration;
import auctionplatform.protocol.AuctionReady;
import auctionplatform.protocol.NetworkHelper;
import auctionplatform.protocol.Register;
import auctionplatform.protocol.RequestAuctionStart;
import auctionplatform.protocol.RequestConnectionData;
import auctionplatform.protocol.ResponseAuctionStart;
import auctionplatform.protocol.ResponseConnectionData;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import frescoauction.auction.Auction;
import frescoauction.util.AuctionEvaluation;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of the client-worker, which is a part of the auction service (of the auction-client).
 * A client-worker participates in auctions, which are hosted by server-workers. A configuration
 * is possible through {@link ClientConfiguration}.
 */
public class ClientWorker extends Listener implements Runnable {
    private static final int CONNECTION_TIMEOUT_MS = 1000;
    /** Default ip address of the host machine (if running on the emulator).*/
    private static final String EMULATOR_HOST_IP_ADDRESS = "10.0.2.2";
    /** Kryo client, which is responsible for the communication with the server-worker during auction (his kryo server).*/
    private Client kryoClient;
    /**
     * Single use signal, which is triggered, when the message {@link RequestConnectionData}
     * is received from the server-worker.
     */
    private CountDownLatch receivedConnectionDataRequest = new CountDownLatch(1);
    /**
     * Single use signal, which is triggered, when the message {@link AuctionConfiguration}
     * is received from the server-worker.
     */
    private CountDownLatch receivedAuctionConfiguration = new CountDownLatch(1);
    /**
     * Single use signal, which is triggered, when the message {@link RequestAuctionStart}
     * is received from the server-worker.
     */
    private CountDownLatch receivedStartSignal = new CountDownLatch(1);
    /** Current state of the client worker.*/
    private volatile State state;
    /**
     * Network configuration, which is used to set up the smpc
     * network during the auction evaluation (fresco application).
     */
    private NetworkConfiguration networkConfiguration;
    /** Execution service with a single executor, which is used to run the kryo client listener.*/
    private ExecutorService esKryoClientListener;
    /**
     * Specifies the protocol suite and the preprocessing strategy
     * (the configuration is provided by the server-worker).
     */
    private AuctionConfiguration auctionConfiguration;
    /** Indicates, if a leave command was received.*/
    private AtomicBoolean receivedLeaveCommand = new AtomicBoolean(false);
    /** Indicates, if the connection to the server-worker was lost.*/
    private AtomicBoolean serverDisconnected = new AtomicBoolean(false);
    /** Auction task, which represents the local state of an active auction.*/
    private AuctionTask task;
    /**
     * Repository, which provides access to the database. Used for updating the state
     * of the auction task.
     */
    private AuctionRepository repository;
    /** Event listener.*/
    private WorkerListener listener;
    /** Configuration of the client-worker.*/
    private ClientConfiguration clientConfig;

    /**
     * @param clientConfig configuration of the client-worker.
     * @param task auction task, which represents the local state of an active auction.
     * @param repository repository, which provides access to the database.
     *                   Used for updating the state of the auction task.
     * @param listener event listener.
     */
    public ClientWorker(ClientConfiguration clientConfig,
                        AuctionTask task, AuctionRepository repository,
                        WorkerListener listener) {
        this.clientConfig = clientConfig;
        this.task = task;
        this.repository = repository;
        this.listener = listener;
    }

    /**
     * Commands the client-worker to change the current bid. Takes effect only,
     * if the client-workers current state is {@link State#Connecting}
     * or {@link State#Registration}.
     * @param bid new bid.
     */
    public void setBid(int bid){
        if(state == State.Connecting || state == State.Registration){
            task.setCurrentBid(bid);
            repository.updateAuctionTask(task);
        }
    }

    /**
     * Commands the client-worker to leave the auction. Takes effect only,
     * if the client-workers current state is {@link State#Connecting}
     * or {@link State#Registration}.
     */
    public void leaveAuction(){
        if(state == State.Connecting || state == State.Registration){
            receivedConnectionDataRequest.countDown();
            receivedLeaveCommand.set(true);
        }
    }

    /**
     * Sets up the network configuration from the given auction configuration, which is
     * provided by the server-worker. <br>
     * If {@link ClientConfiguration#isEmulator()} is set
     * to true, then the all the ip addresses will be replaced by {@link #EMULATOR_HOST_IP_ADDRESS}
     * (loopback ip of the host machine).
     * @param config auction configuration.
     */
    private void setUpParties(AuctionConfiguration config){
        auctionConfiguration = config;
        Map<Integer, Party> parties = new HashMap<>();
        for(String string : config.connectionData){
            String[] data = string.split(":");
            int id = Integer.parseInt(data[0]);
            String ip = data[1];
            int port = Integer.parseInt(data[2]);
            if(clientConfig.isEmulator()){
                ip = EMULATOR_HOST_IP_ADDRESS;
            }
            Party party = new Party(id, ip, port);
            parties.put(id, party);
        }
        networkConfiguration = new NetworkConfigurationImpl(config.partyId, parties);
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof RequestConnectionData){
            receivedConnectionDataRequest.countDown();
        }else if(object instanceof AuctionConfiguration){
            AuctionConfiguration config = (AuctionConfiguration) object;
            setUpParties(config);
            receivedAuctionConfiguration.countDown();
        }else if(object instanceof RequestAuctionStart){
            receivedStartSignal.countDown();
        }
    }

    @Override
    public void disconnected(Connection connection) {
        serverDisconnected.set(true);
        switch (state){
            case Registration:
                receivedConnectionDataRequest.countDown();
                break;
            case SMPC_SetUp:
                receivedAuctionConfiguration.countDown();
                break;
            case SMPC_SetUpFinish:
                receivedStartSignal.countDown();
                break;
        }
    }

    @Override
    public void run() {
        changeToState(State.Connecting);
    }

    /**
     * Executes state logic of {@link ClientWorker.State#Connecting}.
     * Starts the kryo client and tries to establish a connection to the server-worker (his kryo server).
     * Changes state to {@link State#Registration}, if a connection was established or calls
     * {@link #changeToErrorState(String)} otherwise.
     */
    private void connecting(){
        kryoClient = new Client();
        kryoClient.start();
        NetworkHelper.register(kryoClient);
        esKryoClientListener = Executors.newSingleThreadExecutor();
        kryoClient.addListener(new ThreadedListener(this, esKryoClientListener));
        boolean connected = false;
        try {
            String hostIp = clientConfig.isEmulator() ? EMULATOR_HOST_IP_ADDRESS : task.getHostIp();
            kryoClient.connect(CONNECTION_TIMEOUT_MS, hostIp, task.getHostPort());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if(connected) changeToState(State.Registration);
        else changeToErrorState("Connection could not be established.");
    }

    /**
     * Executes state logic of {@link ClientWorker.State#Registration}.
     * Sends a {@link Register} message and blocks until a {@link RequestConnectionData}
     * message was received from the server-worker.
     * Changes to the state {@link State#SMPC_SetUp}, if a request was received in the given
     * time frame or calls {@link #changeToErrorState(String)} otherwise.
     */
    private void registration(){
        task.setLocalPhase(AuctionPhase.Registration);
        repository.updateAuctionTask(task);
        kryoClient.sendTCP(new Register(clientConfig.getClientId()));
        boolean isNotified = false;
        try {
            isNotified = receivedConnectionDataRequest.await(clientConfig.getRegistrationDuration(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(receivedLeaveCommand.get()) changeToErrorState("Auction leaved.");
        else if(isNotified && !serverDisconnected.get()) changeToState(State.SMPC_SetUp);
        else changeToErrorState("RequestConnectionData timeout.");
    }

    /**
     * Executes state logic of {@link ClientWorker.State#SMPC_SetUp}.
     * Sends the connection data ({@link ResponseConnectionData}) and
     * blocks until a {@link AuctionConfiguration} message was received from the server-worker.
     * Changes to the state {@link State#SMPC_SetUpFinish}, if the request was received in the given
     * time frame or calls {@link #changeToErrorState(String)} otherwise.
     */
    private void smpcSetUp(){
        task.setLocalPhase(AuctionPhase.Running);
        repository.updateAuctionTask(task);
        kryoClient.sendTCP(new ResponseConnectionData(task.getSmpcPort()));
        boolean isNotified = false;
        try {
            isNotified = receivedAuctionConfiguration.await(clientConfig.getSmpcSetUpDuration(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(isNotified && !serverDisconnected.get()) changeToState(State.SMPC_SetUpFinish);
        else changeToErrorState("AuctionConfiguration timeout.");
    }

    /**
     * Executes state logic of {@link ClientWorker.State#SMPC_SetUpFinish}.
     * Sends a {@link AuctionReady} message to the server-worker and
     * blocks until a {@link RequestAuctionStart} message was received from the server-worker.
     * Changes to the state {@link State#SMPC_SetUpFinish}, if a request was received in the given
     * time frame or calls {@link #changeToErrorState(String)} otherwise.
     */
    private void smpcSetUpFinish(){
        kryoClient.sendTCP(new AuctionReady());
        boolean isNotified = false;
        try {
            isNotified = receivedStartSignal.await(clientConfig.getSmpcSetUpFinishDuration(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(isNotified && !serverDisconnected.get()) changeToState(State.SMPC_Running);
        else changeToErrorState("RequestAuctionStart timeout.");
    }

    /**
     * Executes state logic of {@link ClientWorker.State#SMPC_Running}.
     * Starts the auction evaluation and notifies the server-worker by sending a
     * {@link ResponseAuctionStart} message.
     * Updates the auction task and changes to state {@link State#Closure}, if the
     * evaluation succeeded or calls {@link #changeToErrorState(String)} otherwise.
     */
    private void smpcRunning(){
        Auction.AuctionResult result = null;
        ExecutorService esSmpc = Executors.newSingleThreadExecutor();
        try {
            Future<Auction.AuctionResult> r = esSmpc.submit(new AuctionEvaluation(auctionConfiguration.auctionType, task.getCurrentBid(),
                    auctionConfiguration.protocolSuite, auctionConfiguration.preprocessing, networkConfiguration));
            TimeUnit.SECONDS.sleep(1);
            kryoClient.sendTCP(new ResponseAuctionStart());
            result = r.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }finally {
            esSmpc.shutdown();
        }
        if(result != null){
            task.setHasWon(networkConfiguration.getMyId() == result.getWinnerId());
            task.setFinalPrice(result.getFinalPrice());
            changeToState(State.Closure);
        } else {
            changeToErrorState("Auction evaluation aborted.");
        }
    }

    /**
     * Shuts down the client-worker by stopping the kryo client and the corresponding listener.
     */
    private void shutdown(){
        if(kryoClient != null) kryoClient.stop();
        esKryoClientListener.shutdown();
        listener.onCompleteTask(task);
    }

    /**
     * Changes the current state to given target state.
     * @param targetState target state.
     */
    private void changeToState(State targetState){
        state = targetState;
        switch (state){
            case Connecting:
                connecting();
                break;
            case Registration:
                registration();
                break;
            case SMPC_SetUp:
                smpcSetUp();
                break;
            case SMPC_SetUpFinish:
                smpcSetUpFinish();
                break;
            case SMPC_Running:
                smpcRunning();
                break;
            case Closure:
                closure();
                break;
            default:
                changeToErrorState(String.format("Unknown target state %s.", state.name()));
                break;
        }
    }

    /**
     * Executes state logic of {@link ClientWorker.State#Closure}.
     * Updates the auction task and shuts down after.
     */
    private void closure(){
        task.setLocalPhase(AuctionPhase.Completion);
        repository.updateAuctionTask(task);
        shutdown();
    }

    /**
     * Changes to the error state.
     * @param errMsg error message, which should be displayed.
     */
    private void changeToErrorState(String errMsg){
        System.out.println(String.format("Client error occurred during state [%s]: %s", state == null ? "none" : state.name(), errMsg));
        task.setLocalPhase(AuctionPhase.Abortion);
        task.setErrorMessage(errMsg);
        repository.updateAuctionTask(task);
        shutdown();
    }

    /**
     * Represents the state of the client-worker.
     */
    private enum State{Connecting, Registration, SMPC_SetUp, SMPC_SetUpFinish, SMPC_Running, Closure}
}
