package io.enotes.sdk.core;


/**
 * init some config in app application
 */
public class ENotesSDK {
    private static ENotesSDK eNotesSDK;
    public static Config config = new Config();;

    private ENotesSDK() {

    }

    public static ENotesSDK init() {
        if (eNotesSDK == null) {
            eNotesSDK = new ENotesSDK();
        }
        return eNotesSDK;
    }

}
