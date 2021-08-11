package org.prolog4j;

public class ProverCreationException extends RuntimeException {

    private static final long serialVersionUID = -310956776466335757L;

    public ProverCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProverCreationException(String message) {
        super(message);
    }

    public ProverCreationException(Throwable cause) {
        super(cause);
    }

}
