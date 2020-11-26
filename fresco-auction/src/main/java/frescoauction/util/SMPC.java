package frescoauction.util;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import frescoauction.configuration.ProtocolAssembler;
import frescoauction.configuration.ProtocolAssemblerFactory;
import frescoauction.configuration.ProtocolConfiguration;


import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;

/**
 * Helper class for running fresco applications.
 * @param <ResourcePoolT> resource pool, which will be used by the protocol suite.
 * @param <BuilderT> type of builder, which will be used by the protocol suite.
 * @param <OutputT> output type of the fresco application.
 */
public class SMPC<ResourcePoolT extends ResourcePool, BuilderT extends ProtocolBuilder, OutputT> {
    /** Configuration, which should be use for assembling the protocol suite.*/
    private final ProtocolConfiguration protocolConfig;
    /** Fresco application, which should be run.*/
    private final Application<OutputT, BuilderT> application;
    /** Network configuration, which should be used by the fresco application.*/
    private final NetworkConfiguration networkConfig;

    /**
     * @param protocolConfig protocol suite configuration.
     * @param application fresco application, which should be run.
     * @param networkConfig network configuration, which should be used by the fresco application.
     */
    public SMPC(ProtocolConfiguration protocolConfig, Application<OutputT, BuilderT> application, NetworkConfiguration networkConfig) {
        this.protocolConfig = protocolConfig;
        this.application = application;
        this.networkConfig = networkConfig;
    }

    /**
     * Starts and runs the fresco application with provided configuration. The calling thread will be blocked until
     * computation is finished.
     * @param duration timeout.
     * @return computed output.
     */
    public OutputT startComputation(Duration duration){
        ProtocolAssembler<ResourcePoolT, BuilderT> assembler = ProtocolAssemblerFactory
                .getAssembler(protocolConfig, networkConfig);
        assembler.assemble();
        OutputT result = assembler.getSecureComputationEngine().runApplication(
                        application,
                        assembler.getResourcePool(),
                        assembler.getNetwork(),
                        duration);
        assembler.getSecureComputationEngine().shutdownSCE();
        Closeable closeableNetwork = ( Closeable) assembler.getNetwork();
        try {
            closeableNetwork.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
