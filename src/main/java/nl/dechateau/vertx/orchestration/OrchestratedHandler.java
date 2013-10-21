package nl.dechateau.vertx.orchestration;

/**
 * Marker interface for handlers within the orchestration.
 */
public interface OrchestratedHandler {
    /**
     * Called to initiate the execution of the handler.
     *
     * @param responseListener The instance to which the result must be responded.
     */
    void execute(final ResponseListener responseListener);

    /**
     * Called to confirm whether this handler has completed its task yet.
     *
     * @return Whether this handler has completed its task.
     */
    public boolean isCompleted();
}
