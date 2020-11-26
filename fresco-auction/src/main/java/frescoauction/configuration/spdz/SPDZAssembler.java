package frescoauction.configuration.spdz;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzMascotDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import dk.alexandra.fresco.tools.ot.base.DhParameters;
import dk.alexandra.fresco.tools.ot.base.NaorPinkasOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import frescoauction.configuration.ProtocolAssembler;
import frescoauction.util.Utils;

import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * An assembler for constructing a protocol suit of the type {@link Utils.ProtocolSuite#Spdz}.
 * Calling the method {@link #assemble()} also executes the offline phase of the Spdz protocol suite.
 * Methods, which handle the initialization of the offline phase, are based on the example code provided
 * by fresco (source: https://github.com/aicis/fresco/tree/master/demos/common).
 */
public class SPDZAssembler implements ProtocolAssembler<SpdzResourcePool, ProtocolBuilderNumeric> {
    private SpdzConfiguration spdzConfiguration;
    private NetworkConfiguration networkConfiguration;
    private SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce;
    private SpdzResourcePool resourcePool;
    private SpdzProtocolSuite protocolSuite;
    ProtocolEvaluator<SpdzResourcePool> evaluator;
    private Network network;

    /**
     * @param protocolConfiguration protocol suite configuration.
     * @param networkConfiguration network configuration.
     */
    public SPDZAssembler(SpdzConfiguration protocolConfiguration, NetworkConfiguration networkConfiguration) {
        spdzConfiguration = protocolConfiguration;
        this.networkConfiguration = networkConfiguration;
    }

    @Override
    public void assemble() {
        network = new SocketNetwork(networkConfiguration);
        protocolSuite = new SpdzProtocolSuite(spdzConfiguration.getMaxBitLength());
        switch (spdzConfiguration.getPreprocessingStrategy()){
            case Mascot:
                initMascotResourcePool();
                break;
            case Dummy:
                initDummyResourcePool();
                break;
        }
        BatchEvaluationStrategy<SpdzResourcePool> evalStrategy = spdzConfiguration.getEvaluationStrategy().getStrategy();
        evaluator = new BatchedProtocolEvaluator<>(evalStrategy, protocolSuite, spdzConfiguration.getMaxBatchSize());
        sce = new SecureComputationEngineImpl<>(protocolSuite, evaluator);
    }

    @Override
    public SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> getSecureComputationEngine() {
        return sce;
    }

    @Override
    public ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> getProtocolSuite() {
        return protocolSuite;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public SpdzResourcePool getResourcePool() {
        return resourcePool;
    }

    private void initMascotResourcePool(){
        BigInteger modulus = ModulusFinder.findSuitableModulus(spdzConfiguration.getModBitLength());
        BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(modulus);
        Drbg drbg = getDrbg();
        Map<Integer, RotList> seedOts = getSeedOts(drbg);
        FieldElement ssk = SpdzMascotDataSupplier.createRandomSsk(definition, spdzConfiguration.getMascotPrgSeedLength());
        SpdzDataSupplier preprocessedValuesDataSupplier = SpdzMascotDataSupplier.createSimpleSupplier(
                networkConfiguration.getMyId(), networkConfiguration.noOfParties(),
                () -> network, spdzConfiguration.getModBitLength(), definition, null, seedOts, drbg, ssk);
        SpdzResourcePool preprocessedValuesResourcePool = new SpdzResourcePoolImpl(
                networkConfiguration.getMyId(), networkConfiguration.noOfParties(), new SpdzOpenedValueStoreImpl(),
                preprocessedValuesDataSupplier, AesCtrDrbg::new);
        SpdzDataSupplier supplier = SpdzMascotDataSupplier.createSimpleSupplier(
                networkConfiguration.getMyId(), networkConfiguration.noOfParties(), () -> network,
                spdzConfiguration.getModBitLength(), definition, new Function<Integer, SpdzSInt[]>() {

                    @Override
                    public SpdzSInt[] apply(Integer pipeLength) {
                        DRes<List<DRes<SInt>>> pipe = createPipe(pipeLength, preprocessedValuesResourcePool);
                        return computeSInts(pipe);
                    }

                }, seedOts, drbg, ssk);

        resourcePool =  new SpdzResourcePoolImpl(networkConfiguration.getMyId(), networkConfiguration.noOfParties(),
                new SpdzOpenedValueStoreImpl(), supplier, AesCtrDrbg::new);
    }

    @Override
    public ProtocolEvaluator<SpdzResourcePool> getProtocolEvaluator() {
        return evaluator;
    }

    private Drbg getDrbg() {
        byte[] seed = new byte[spdzConfiguration.getMascotPrgSeedLength() / 8];
        new Random(networkConfiguration.getMyId()).nextBytes(seed);
        return AesCtrDrbgFactory.fromDerivedSeed(seed);
    }

    private  Map<Integer, RotList> getSeedOts(Drbg drbg) {
        Map<Integer, RotList> seedOts = new HashMap<>();
        for (int otherId = 1; otherId <= networkConfiguration.noOfParties(); otherId++) {
            if (networkConfiguration.getMyId() != otherId) {
                DHParameterSpec dhSpec = DhParameters.getStaticDhParams();
                Ot ot = new NaorPinkasOt(otherId, drbg, network, dhSpec);
                RotList currentSeedOts = new RotList(drbg, spdzConfiguration.getMascotPrgSeedLength());
                if (networkConfiguration.getMyId() < otherId) {
                    currentSeedOts.send(ot);
                    currentSeedOts.receive(ot);
                } else {
                    currentSeedOts.receive(ot);
                    currentSeedOts.send(ot);
                }
                seedOts.put(otherId, currentSeedOts);
            }
        }
        return seedOts;
    }

    private  DRes<List<DRes<SInt>>> createPipe(int pipeLength, SpdzResourcePool resourcePool) {
        SpdzProtocolSuite spdzProtocolSuite = protocolSuite;
        ProtocolBuilderNumeric sequential = spdzProtocolSuite.init(resourcePool).createSequential();
        Application<List<DRes<SInt>>, ProtocolBuilderNumeric> expPipe = builder ->
                new DefaultPreprocessedValues(builder).getExponentiationPipe(pipeLength);
        DRes<List<DRes<SInt>>> exponentiationPipe = expPipe.buildComputation(sequential);
        evaluate(sequential, resourcePool, network, spdzProtocolSuite);
        return exponentiationPipe;
    }

    private static void evaluate(ProtocolBuilderNumeric spdzBuilder, SpdzResourcePool tripleResourcePool,
                                 Network network, SpdzProtocolSuite suite) {
        BatchedStrategy<SpdzResourcePool> batchedStrategy = new BatchedStrategy<>();
        SpdzProtocolSuite spdzProtocolSuite = suite;
        BatchedProtocolEvaluator<SpdzResourcePool> batchedProtocolEvaluator =
                new BatchedProtocolEvaluator<>(batchedStrategy, spdzProtocolSuite);
        batchedProtocolEvaluator.eval(spdzBuilder.build(), tripleResourcePool, network);
    }

    private static SpdzSInt[] computeSInts(DRes<List<DRes<SInt>>> pipe) {
        List<DRes<SInt>> out = pipe.out();
        SpdzSInt[] result = new SpdzSInt[out.size()];
        for (int i = 0; i < out.size(); i++) {
            DRes<SInt> sIntResult = out.get(i);
            result[i] = (SpdzSInt) sIntResult.out();
        }
        return result;
    }

    private void initDummyResourcePool(){
        BigInteger modulus = ModulusFinder.findSuitableModulus(spdzConfiguration.getModBitLength());
        SpdzDataSupplier supplier = new SpdzDummyDataSupplier(networkConfiguration.getMyId(), networkConfiguration.noOfParties(),
                new BigIntegerFieldDefinition(modulus), modulus);
        resourcePool = new SpdzResourcePoolImpl(networkConfiguration.getMyId(), networkConfiguration.noOfParties(),
                new SpdzOpenedValueStoreImpl(), supplier,
                AesCtrDrbg::new);
    }
}
