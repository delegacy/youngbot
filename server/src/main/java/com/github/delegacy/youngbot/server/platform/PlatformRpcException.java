package com.github.delegacy.youngbot.server.platform;

/**
 * TBW.
 */
public class PlatformRpcException extends RuntimeException {
    private static final long serialVersionUID = 5195949896344225784L;

    /**
     * TBW.
     */
    public PlatformRpcException() {}

    /**
     * TBW.
     */
    public PlatformRpcException(String message) {
        super(message);
    }

    /**
     * TBW.
     */
    public PlatformRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * TBW.
     */
    public PlatformRpcException(Throwable cause) {
        super(cause);
    }

    /**
     * TBW.
     */
    public PlatformRpcException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}
