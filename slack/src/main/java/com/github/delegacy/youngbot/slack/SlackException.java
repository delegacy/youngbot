package com.github.delegacy.youngbot.slack;

/**
 * A {@link RuntimeException} that raises when an operation is not successfully completed.
 */
public class SlackException extends RuntimeException {
    private static final long serialVersionUID = 9152945647614644681L;

    /**
     * TBW.
     */
    public SlackException() {}

    /**
     * TBW.
     */
    public SlackException(String message) {
        super(message);
    }

    /**
     * TBW.
     */
    public SlackException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * TBW.
     */
    public SlackException(Throwable cause) {
        super(cause);
    }

    /**
     * TBW.
     */
    protected SlackException(String message, Throwable cause,
                             boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
