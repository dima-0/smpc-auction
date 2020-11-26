package frescoauction.configuration;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;

/**
 * Interface for configuration implementations,
 * which are used by {@link ProtocolAssembler} when assembling a protocol suite.
 */
public interface ProtocolConfiguration {
    /**
     * Default batch size (is used in default configuration).
     */
    public static final int DEFAULT_MAX_BATCH_SIZE = 4096;
    public EvaluationStrategy getEvaluationStrategy();
}
