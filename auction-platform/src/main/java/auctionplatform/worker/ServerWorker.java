package auctionplatform.worker;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import auctionplatform.protocol.*;
import frescoauction.auction.Auction;
import frescoauction.util.AuctionEvaluation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Implementation of the server-worker, which is part of the auction service (of the auction-platform).
 * A server-worker is responsible for hosting and coordinating an auction. He also takes parts
 * in the auction evaluation with the starting price as his bid. The server-worker can be configured
 * with {@link ServerConfiguration}.
 */
public class ServerWorker extends Listener implements Runnable {
    /** Maximum waiting time for all clients to start (seconds).*/
    private static final int EVALUATION_START_TIMEOUT = 60;
    /** Kryo server, which is responsible for the communication with the client-workers during auction (their kryo clients).*/
    private Server kryoServer;
    /** Single use signal, which is triggered at the end of {@link State#SMPC_SetUp}.*/
    private CountDownLatch smpcSetUpComplete = new CountDownLatch(1);
    /** Single use signal, which is triggered at the end of {@link State#SMPC_SetUpFinish}.*/
    private CountDownLatch smpcSetupFinishComplete = new CountDownLatch(1);
    /** Single use signal, which is triggered when the server-worker should start the auction evaluation.*/
    private CountDownLatch auctionStartComplete = new CountDownLatch(1);
    /** Contains data of all registered clients mapped by their connection id.*/
    private Map<Integer, ClientData> registeredClients = new ConcurrentHashMap<>();
    /** Current state of the server-worker.*/
    private volatile State state;
    /** Execution service with a single executor, which is used to run the kryo server listener.*/
    private ExecutorService esKryoServerListener;
    /**
     * Network configuration, which is used to set up the smpc
     * network during the auction evaluation (fresco application).
     */
    private NetworkConfiguration networkConfig = new NetworkConfigurationImpl(1, Collections.EMPTY_MAP);
    /** Configuration of the server-worker.*/
    private ServerConfiguration config;
    /**
     * Deque used as a stack, which contains connection ids of all registered clients
     * (in descending order of their party ids).
     */
    private Deque<Integer> startingOrder = new ConcurrentLinkedDeque<>();
    /** Event listener.*/
    private WorkerListener listener;

    /**
     * @param config configuration of the server-worker.
     * @param listener event listener.
     */
    public ServerWorker(ServerConfiguration config, WorkerListener listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void received(Connection c, Object msg) {
        int connectionId = c.getID();
        switch (state){
            case Registration:
                if(msg instanceof Register){
                    Register request = (Register) msg;
                    ClientData clientData = new ClientData(request.clientId);
                    if(clientAlreadyRegistered(clientData.getClientId())) {
                        c.close();
                    } else {
                        registeredClients.put(connectionId, clientData);
                        log(String.format("Client[clientId=%d] entered the auction.", clientData.getClientId()));
                    }
                }
                break;
            case SMPC_SetUp:
                if(msg instanceof ResponseConnectionData){
                    if(registeredClients.containsKey(connectionId)){
                        ResponseConnectionData response = (ResponseConnectionData) msg;
                        ClientData clientData = registeredClients.get(c.getID());
                        clientData.setFrescoPort(response.frescoPort);
                        if(allConnectionDataReceived()){
                            smpcSetUpComplete.countDown();
                        }
                    }else c.close();
                }
                break;
            case SMPC_SetUpFinish:
                if(msg instanceof AuctionReady){
                    if(registeredClients.containsKey(connectionId)){
                        registeredClients.get(c.getID()).setReadyForAuctionEvaluation(true);
                        if(allClientsReady()){
                            smpcSetupFinishComplete.countDown();
                        }
                    } else c.close();
                }
                break;
            case SMPC_Running:
                if(msg instanceof ResponseAuctionStart){
                    if(!startingOrder.isEmpty() && startingOrder.peek() == connectionId){
                        startingOrder.pop();
                        ClientData clientData = registeredClients.get(connectionId);
                        log(String.format("Client[clientId=%d, partyId=%d] started the auction evaluation.",
                                clientData.getClientId(), clientData.getFrescoPartyId()));
                        if(startingOrder.isEmpty()){
                            auctionStartComplete.countDown();
                        } else {
                            kryoServer.sendToTCP(startingOrder.peek(), new RequestAuctionStart());
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void disconnected(Connection c) {
        if (state == State.Registration) {
            ClientData clientData = registeredClients.remove(c.getID());
            log(String.format("Client[clientId=%d] leaved the auction.",
                    clientData.getClientId(), clientData.getFrescoPartyId()));
        }
    }

    /**
     * Assigns party ids to clients und sets up the network configuration.
     */
    private void setUpParties(){
        Map<Integer, Party> parties = new HashMap<>();
        Party serverParty = new Party(1, config.getHostIp(), config.getFrescoPort());
        parties.put(serverParty.getPartyId(), serverParty);
        List<Integer> connectionIds = new ArrayList<>(registeredClients.keySet());
        Collections.shuffle(connectionIds);
        int nextPartyId = 2;
        for(int connectionId : connectionIds){
            String clientIp = getClientIpAddress(connectionId);
            if(!clientIp.isEmpty()){
                int clientSMPCPort = registeredClients.get(connectionId).getFrescoPort();
                Party party = new Party(nextPartyId, clientIp, clientSMPCPort);
                parties.put(party.getPartyId(), party);
                registeredClients.get(connectionId).setFrescoPartyId(party.getPartyId());
                nextPartyId++;
            }
        }
        networkConfig = new NetworkConfigurationImpl(serverParty.getPartyId(), parties);

    }

    /**
     * Determines the ip address of the client by the given connection id.
     * @param connectionId connection id of the client.
     * @return the ip address as a string or null, if the given connection id does not exist.
     */
    private String getClientIpAddress(int connectionId) {
        for(Connection connection : kryoServer.getConnections()){
            if(connection.getID() == connectionId) {
                return  connection.getRemoteAddressTCP().getAddress().getHostAddress();
            }
        }
        return "";
    }

    /**
     * Sends {@link AuctionConfiguration} to all registered clients.
     * @param mapping connection data as a string array.
     */
    private void sendConfig(String[] mapping){
        for(int connectionId : registeredClients.keySet()){
            ClientData clientData = registeredClients.get(connectionId);
            AuctionConfiguration auctionConfig = new AuctionConfiguration(
                    clientData.getFrescoPartyId(),
                    config.getAuctionType(),
                    config.getProtocolSuite(),
                    config.getPreprocessing(),
                    mapping);
            kryoServer.sendToTCP(connectionId, auctionConfig);
        }
    }

    /**
     * Extracts the connection data from the network configuration.
     * @return connection data as array.
     */
    private String[] configToStringArray(){
        int numberOfParties = networkConfig.noOfParties();
        String[] mapping = new String[numberOfParties];
        for(int i = 0; i < numberOfParties; i++){
            int partyId = i + 1;
            Party party = networkConfig.getParty(partyId);
            String entry = String.format("%d:%s:%d", party.getPartyId(), party.getHostname(), party.getPort());
            mapping[i] = entry;
        }
        return mapping;
    }

    /**
     * Checks if all clients transmitted their connection data.
     * @return true if all clients transmitted their connection data or false otherwise.
     */
    private boolean allConnectionDataReceived(){
        return registeredClients.values().stream()
                .allMatch(ClientData::frescoPortAvailable);
    }

    /**
     * Checks if all clients are ready for auction evaluation.
     * @return true if all clients ready or false otherwise.
     */
    private boolean allClientsReady(){
        return registeredClients.values().stream()
                .allMatch(ClientData::isReadyForAuctionEvaluation);
    }

    /**
     * Checks if a client is already registered.
     * @param clientId id of the client.
     * @return true if a client with the given id is already registered or false otherwise.
     */
    private boolean clientAlreadyRegistered(int clientId){
        return registeredClients.values().stream()
                .anyMatch(c -> c.getClientId() == clientId);
    }

    @Override
    public void run() {
        if(startKryoServer()){
            changeToState(State.Registration);
        }else{
            changeToErrorState("Server cannot be started.");
        }
    }

    /**
     * Executes state logic of {@link State#Registration}.
     * Blocks until the registration phase is over. Changes state to {@link State#SMPC_SetUp} if at least 1
     * client registered for the auction or calls {@link #changeToErrorState(String)} otherwise.
     */
    private void registration(){
        try {
            TimeUnit.SECONDS.sleep(config.getRegistrationDuration());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!registeredClients.isEmpty()) changeToState(State.SMPC_SetUp);
        else changeToErrorState("No clients registered.");
    }

    /**
     * Executes state logic of {@link State#SMPC_SetUp}.
     * Sends {@link RequestConnectionData} to all registered clients and blocks until all clients respond with {@link ResponseConnectionData}.
     * Changes state to {@link State#SMPC_SetUpFinish}, if all clients respond in a given time frame or calls
     * {@link #changeToErrorState(String)} otherwise.
     */
    private void smpcSetUp(){
        requestSMPCPorts();
        boolean isNotified = false;
        try {
            isNotified = smpcSetUpComplete.await(config.getSmpcSetUpDuration(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(isNotified) changeToState(State.SMPC_SetUpFinish);
        else changeToErrorState("At least one client does not respond to RequestConnectionData.");
    }

    /**
     * Sends a {@link RequestConnectionData} message to all registered clients.
     */
    private void requestSMPCPorts(){
        for(int connectionId : registeredClients.keySet()){
            kryoServer.sendToTCP(connectionId, new RequestConnectionData());
        }
    }

    /**
     * Executes state logic of {@link State#SMPC_SetUpFinish}.
     * Sets up {@link AuctionConfiguration} and sends it to all registered clients and blocks until all clients respond
     * with {@link AuctionReady}.
     * Changes state to {@link State#SMPC_Running} if all clients respond in a given time frame or calls
     * {@link #changeToErrorState(String)} otherwise.
     */
    private void smpcSetUpFinish() {
        setUpParties();
        if(networkConfig.noOfParties() > 1){
            sendConfig(configToStringArray());
            boolean isNotified = false;
            try {
                isNotified = smpcSetupFinishComplete.await(config.getSmpcSetUpFinishDuration(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(isNotified) changeToState(State.SMPC_Running);
            else changeToErrorState("At least one client is not ready for auction evaluation.");
        } else changeToErrorState("Number of parties is below 2.");
    }

    /**
     * Executes state logic of {@link State#SMPC_Running}.
     * Sets up the starting order of the auction evaluation and blocks until all clients started the evaluation and
     * then starts himself.
     * Changes state to {@link State#SMPC_SetUpFinish}, if the evaluation succeeds or calls
     * {@link #changeToErrorState(String)} otherwise.
     */
    private void smpcRunning(){
        setUpStartingOrder();
        if(!startingOrder.isEmpty()){
            int connectionId = startingOrder.peek();
            kryoServer.sendToTCP(connectionId, new RequestAuctionStart());
            Auction.AuctionResult result = null;
            ExecutorService singleSMPCExecutor = Executors.newSingleThreadExecutor();
            boolean isNotified = false;
            try {
                isNotified = auctionStartComplete.await(EVALUATION_START_TIMEOUT, TimeUnit.SECONDS);
                if(isNotified){
                    Callable<Auction.AuctionResult> task = new AuctionEvaluation(config.getAuctionType(), config.getStartingPrice(),
                            config.getProtocolSuite(), config.getPreprocessing(), networkConfig);
                    Future<Auction.AuctionResult> r = singleSMPCExecutor.submit(task);
                    result = r.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                singleSMPCExecutor.shutdown();
            }
            if(result != null) {
                listener.onCompletion(result, findClientIdByPartyId(result.getWinnerId()));
                changeToState(State.Closure);
            } else if(!isNotified) changeToErrorState("Auction evaluation is not started by all parties.");
            else changeToErrorState("Auction evaluation aborted.");
        }else changeToErrorState("Auction evaluation cannot be started.");
    }

    /**
     * Searches for the client by his party id.
     * @param partyId party id otf the client.
     * @return the id of the client if a client with the given party id exists or -1 otherwise.
     */
    private int findClientIdByPartyId(int partyId){
        for(ClientData clientData : registeredClients.values()){
            if(clientData.getFrescoPartyId() == partyId) return clientData.getClientId();
        }
        return -1;
    }

    /**
     * Sets up the starting order of the auction evaluation. Clients start in descending order of their party ids.
     */
    private void setUpStartingOrder(){
        startingOrder = registeredClients.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getFrescoPartyId(), e1.getValue().getFrescoPartyId()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
    }

    /**
     * Executes state logic of {@link State#Closure}.
     * Waits a certain amount of time and shuts down after.
     */
    private void closure(){
        try {
            TimeUnit.SECONDS.sleep(config.getClosureDuration());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * Shuts down the server-worker by stopping the kryo server and the corresponding listener.
     */
    private void shutdown(){
        log(String.format("Shutting down..."));
        kryoServer.stop();
        esKryoServerListener.shutdown();
    }

    /**
     * Starts the kryo server, which is responsible for communication with other clients (kryo clients).
     * @return true if kryo server was started successfully or false otherwise.
     */
    private boolean startKryoServer(){
        kryoServer = new Server();
        NetworkHelper.register(kryoServer);
        esKryoServerListener = Executors.newSingleThreadExecutor();
        kryoServer.addListener(new ThreadedListener(this, esKryoServerListener));
        boolean messageServerStarted = false;
        try {
            kryoServer.bind(config.getHostPort());
            kryoServer.start();
            messageServerStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageServerStarted;
    }

    /**
     * Changes the current state to given target state.
     * @param targetState target state.
     */
    private void changeToState(State targetState){
        if(this.state != null) {
            log(String.format("leaves state: %s", this.state.name()));
        }
        this.state = targetState;
        log(String.format("enters state: %s", this.state.name()));
        switch (this.state){
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
                changeToErrorState("Unknown state.");
                break;
        }
    }

    /**
     * Changes to the error state.
     * @param errMsg error message, which should be displayed.
     */
    private void changeToErrorState(String errMsg){
        log(String.format("Error occurred: %s", errMsg));
        listener.onError(errMsg);
        shutdown();
    }

    private void log(String msg){
        System.out.println(String.format("Server-Worker[auctionId=%d]: %s", config.getAuctionId(), msg));
    }

    /**
     * Represents the state of the server-worker.
     */
    private enum State{Registration, SMPC_SetUp, SMPC_SetUpFinish, SMPC_Running, Closure}
}
