package frescoauction.configuration;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;

/**
 * Interface for concrete implementations of assemblers, which should handle the configuration and construction
 * of a protocol suite and other components, which are needed to run a fresco application.
 * The idea behind this class is to hide all the boilerplate code and keep the initialization simple and short.<br>
 * Before usage, the components should be initialized. This can be done by calling {@link #assemble()}.
 * @param <ResourcePoolT> resource pool, which will be used by the protocol suite.
 * @param <BuilderT> type of builder, which will be used by the protocol suite.
 */
public interface ProtocolAssembler<ResourcePoolT extends ResourcePool, BuilderT extends ProtocolBuilder> {
    /**
     * Configures and initializes all important components, which are used to run a fresco application.
     */
    public void assemble();
    public SecureComputationEngine<ResourcePoolT, BuilderT> getSecureComputationEngine();
    public ProtocolSuite<ResourcePoolT, BuilderT> getProtocolSuite();
    public Network getNetwork();
    public ResourcePoolT getResourcePool();
    public ProtocolEvaluator<ResourcePoolT> getProtocolEvaluator();
}
