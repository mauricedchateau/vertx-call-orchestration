package nl.dechateau.vertx.orchestration;

import nl.dechateau.vertx.orchestration.builder.ExecutionUnit;

public abstract class AbstractDecisionHandler implements OrchestratedHandler, ResponseListener {
    private OrchestrationContext context;

    private ExecutionUnit<?> whenTrue;

    private ExecutionUnit<?> whenFalse;

    private ResponseListener responseListener;

    private boolean isCompleted = false;

    public AbstractDecisionHandler(final OrchestrationContext context) {
        this.context = context;
    }

    public final void setWhenTrue(final ExecutionUnit<?> whenTrue) {
        this.whenTrue = whenTrue;
    }

    public final void setWhenFalse(final ExecutionUnit<?> whenFalse) {
        this.whenFalse = whenFalse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute(final ResponseListener responseListener) {
        this.responseListener = responseListener;

        if (makeDecision()) {
            // Start whenTrue sequence.
            whenTrue.execute(this);
        } else {
            if (whenFalse != null) {
                // Start whenFalse sequence.
                whenFalse.execute(this);
            } else {
                // No calls to be made within this decision.
                completed();
            }
        }
    }

    /**
     * Make the decision (based on the values of the appropriate context variables).
     *
     * @return Whether the decision was <code>true</code> or <code>false</code>.
     */
    public abstract boolean makeDecision();

    /**
     * Convenience method for getting a parameter from the context.
     *
     * @param key The key of the parameter.
     */
    protected final Object getContextVar(final String key) {
        return context.getContextVar(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isCompleted() {
        return isCompleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void completed() {
        // The chosen path is completed.
        isCompleted = true;
        responseListener.completed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void error(final String errorMessage) {
        isCompleted = true;
        responseListener.error(errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void error(final ErrorType type, final String errorMessage) {
        isCompleted = true;
        responseListener.error(type, errorMessage);
    }
}
