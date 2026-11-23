package net.godlycow.org.essc.exception;

/** converting items or inventories to/from their stored (base64) form failed */
public class SerializationException extends RuntimeException {

    public SerializationException(String message, Throwable cause) {
        super( message , cause );
    }
}
