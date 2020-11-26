package frescoauction.configuration.dummy;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import frescoauction.configuration.ProtocolAssembler;
import frescoauction.util.Utils;

/**
 * An assembler for constructing a protocol suite of the type {@link Utils.ProtocolSuite#DummyArithmetic}.
 * The initialization of the suite is based on the example code provided
 * by fresco (source: https://github.com/aicis/fresco/tree/master/demos/common).
 */
public class DummyArithmeticAssembler implements ProtocolAssembler<DummyArithmeticResourcePool, ProtocolBuilderNumeric> {
    private DummyArithmeticConfiguration dummyArithmeticConfiguration;
    private ProtocolSuite<DummyArithmeticResourcePool, ProtocolBuilderNumeric> protocolSuite;
    private NetworkConfiguration networkConfiguration;
    private SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce;
    private DummyArithmeticResourcePool resourcePool;
    private ProtocolEvaluator<DummyArithmeticResourcePool> evaluator;
    private Network network;

    /**
     * @param dummyArithmeticConfiguration
     * @param networkConfiguration
     */
    public DummyArithmeticAssembler(DummyArithmeticConfiguration dummyArithmeticConfiguration, NetworkConfiguration networkConfiguration) {
        this.dummyArithmeticConfiguration = dummyArithmeticConfiguration;
        this.networkConfiguration = networkConfiguration;
    }

    @Override
    public void assemble() {
        network = new SocketNetwork(networkConfiguration);
        BigIntegerFieldDefinition fieldDefinition = new BigIntegerFieldDefinition(dummyArithmeticConfiguration.getModulus());
        protocolSuite = new DummyArithmeticProtocolSuite(fieldDefinition,
                                dummyArithmeticConfiguration.getMaxBitLength(),
                                dummyArithmeticConfiguration.getFixedPointPrecision());
        resourcePool = new DummyArithmeticResourcePoolImpl(networkConfiguration.getMyId(), networkConfiguration.noOfParties(), fieldDefinition);
        BatchEvaluationStrategy<DummyArithmeticResourcePool> evalStrategy = dummyArithmeticConfiguration.getEvaluationStrategy().getStrategy();
        evaluator = new BatchedProtocolEvaluator<>(evalStrategy, protocolSuite, dummyArithmeticConfiguration.getMaxBatchSize());
        sce = new SecureComputationEngineImpl<>(protocolSuite, evaluator);
    }

    @Override
    public SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> getSecureComputationEngine() {
        return sce;
    }

    @Override
    public ProtocolSuite<DummyArithmeticResourcePool, ProtocolBuilderNumeric> getProtocolSuite() {
        return protocolSuite;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public DummyArithmeticResourcePool getResourcePool() {
        return resourcePool;
    }

    @Override
    public ProtocolEvaluator<DummyArithmeticResourcePool> getProtocolEvaluator() {
        return evaluator;
    }
}
