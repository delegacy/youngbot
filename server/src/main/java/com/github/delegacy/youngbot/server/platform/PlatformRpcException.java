package com.github.delegacy.youngbot.server.platform;

public class PlatformRpcException extends RuntimeException {
    private static final long serialVersionUID = 5195949896344225784L;

    public PlatformRpcException() {
    }

    public PlatformRpcException(String message) {
        super(message);
    }

    public PlatformRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlatformRpcException(Throwable cause) {
        super(cause);
    }

    public PlatformRpcException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
