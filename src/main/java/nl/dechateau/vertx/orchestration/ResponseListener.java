package nl.dechateau.vertx.orchestration;

public interface ResponseListener {
    /**
     * Called by a handler when the service call was completed successfully.
     */
    void completed();

    /**
     * Called by a handler when a service call encountered an exceptional situation.
     *
     * @param errorMessage The error message indicating the reason for failure.
     */
    void error(final String errorMessage);

    /**
     * Called by a handler when a service call encountered an exceptional situation.
     *
     * @param errorMessage The error message indicating the reason for failure.
     */
    void error(final ErrorType type, final String errorMessage);

    enum ErrorType {
        CONTENT_MISSING,
        TIMEOUT;
    }
}
