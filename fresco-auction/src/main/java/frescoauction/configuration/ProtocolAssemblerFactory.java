package frescoauction.configuration;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import frescoauction.configuration.dummy.DummyArithmeticAssembler;
import frescoauction.configuration.dummy.DummyArithmeticConfiguration;
import frescoauction.configuration.spdz.SPDZAssembler;
import frescoauction.configuration.spdz.SpdzConfiguration;

/**
 * Contains a factory method for constructing a {@link ProtocolAssembler}.
 */
public class ProtocolAssemblerFactory {

    /**
     * Provides a {@link ProtocolAssembler}, which implementation is depending on the provided configuration.
     * So far supported configurations are {@link DummyArithmeticConfiguration} and {@link SpdzConfiguration}.
     * @param protocolConfiguration supported configuration.
     * @param networkConfiguration network configuration, which should be used by the fresco application.
     * @param <ResourcePoolT> resource pool, which will be used by the protocol suite.
     * @param <BuilderT> type of builder, which will be used by the protocol suite.
     * @return an instance of an {@link ProtocolAssembler}. If the provided configuration is not supported, than null.
     */
    public static <ResourcePoolT extends ResourcePool, BuilderT extends ProtocolBuilder> ProtocolAssembler<ResourcePoolT, BuilderT> getAssembler(ProtocolConfiguration protocolConfiguration, NetworkConfiguration networkConfiguration){
        ProtocolAssembler assembler = null;
        if (protocolConfiguration instanceof SpdzConfiguration){
            assembler = new SPDZAssembler((SpdzConfiguration) protocolConfiguration, networkConfiguration);
        }else if (protocolConfiguration instanceof DummyArithmeticConfiguration){
            assembler = new DummyArithmeticAssembler((DummyArithmeticConfiguration) protocolConfiguration, networkConfiguration);
        }
        return assembler;
    }
}
