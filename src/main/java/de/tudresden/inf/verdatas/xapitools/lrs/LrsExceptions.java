package de.tudresden.inf.verdatas.xapitools.lrs;

/**
 * Customized Exceptions for LRS handling
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public class LrsExceptions {
    /**
     * No Connection to LRS
     */
    public static class NoLrsConnection extends RuntimeException {
        public NoLrsConnection(String message) {
            super(message);
        }
    }

    /**
     * Bad data stream (input)
     */
    public static class BadInputData extends IllegalArgumentException {
        public BadInputData(String s) {
            super(s);
        }
    }
}
