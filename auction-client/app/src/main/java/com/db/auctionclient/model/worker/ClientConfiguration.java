package com.db.auctionclient.model.worker;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

import lombok.Getter;

/**
 * Contains a set of parameters for configuring a {@link ClientWorker}. Also contains
 * a pool of ports, which should be managed by the {@link ClientMaster}.
 */
@Getter
public class ClientConfiguration {
    /** Id of the client. */
    private int clientId;
    /**
     * Indicates, if the app is running on an emulator.
     * (is set to false, if running on a physical device)
     */
    private boolean isEmulator;
    /**
     * Pool of ports, which should be used by the client-workers
     * during the auction evaluation (fresco application). 
     */
    private int[] frescoPortPool;
    /** Duration of the registration state (seconds).*/
    private int registrationDuration;
    /** Duration of the smpc-set-up state (seconds).*/
    private int smpcSetUpDuration;
    /** Duration of the smpc-set-up-finish state (seconds).*/
    private int smpcSetUpFinishDuration;

    /**
     * @param clientId id of the client.
     * @param isEmulator indicates, if the app is running on an emulator.
     *                   (is set to false if running on a physical device)
     * @param frescoPortPool pool of ports, which should be used by the client-workers
     *                       during the auction evaluation (fresco application).
     * @param registrationDuration duration of the registration state (seconds).
     * @param smpcSetUpDuration duration of the smpc-set-up phase (seconds).
     * @param smpcSetUpFinishDuration duration of the smpc-set-up-finish phase (seconds).
     */
    public ClientConfiguration(int clientId, boolean isEmulator, int[] frescoPortPool,
                         int registrationDuration, int smpcSetUpDuration, int smpcSetUpFinishDuration) {
        this.clientId = clientId;
        this.isEmulator = isEmulator;
        this.frescoPortPool = frescoPortPool;
        this.registrationDuration = registrationDuration;
        this.smpcSetUpDuration = smpcSetUpDuration;
        this.smpcSetUpFinishDuration = smpcSetUpFinishDuration;
    }

    /**
     * Loads a configuration from a json file.
     * @param path path to the json file.
     * @return a instance of {@link ClientConfiguration}.
     * @throws IOException
     */
    public static ClientConfiguration loadFromJson(String path) throws IOException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(path);
        ClientConfiguration config = gson.fromJson(reader, ClientConfiguration.class);
        reader.close();
        return config;
    }

}
