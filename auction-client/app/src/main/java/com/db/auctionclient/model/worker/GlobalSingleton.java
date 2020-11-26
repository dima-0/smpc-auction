package com.db.auctionclient.model.worker;

/**
 * A global singleton, which contains an instance of {@link ClientMaster}.
 * The instance can be initialized with a custom configuration
 * by calling {@link #initInstance(ClientConfiguration)}.
 */
public class GlobalSingleton {
    private static ClientMaster clientMasterInstance;

    /**
     * Initializes the {@link ClientMaster} instance with given configuration.
     * @param config configuration.
     */
    public static void initInstance(ClientConfiguration config){
        if(clientMasterInstance == null){
            synchronized (GlobalSingleton.class){
                clientMasterInstance = new ClientMaster(config);
            }
        }
    }

    /**
     * Returns an instance of {@link ClientMaster}. Initializes the instance with default
     * configuration, if not already initialized.<br>
     * <pre>{@code
     * // Default configuration
     * clientId=1
     * isEmulator=true
     * portPool={5000}
     * registrationDuration=30
     * smpcSetUpDuration=30
     * smpcSetUpFinishDuration=60
     * }</pre>
     * @return an instance of {@link ClientMaster}.
     */
    public static ClientMaster getInstance(){
        if(clientMasterInstance == null){
            synchronized (GlobalSingleton.class){
                ClientConfiguration config = new ClientConfiguration(
                        1, true, new int[]{5000},
                        30, 30, 60);
                clientMasterInstance = new ClientMaster(config);
            }
        }
        return clientMasterInstance;
    }
}
