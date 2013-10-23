package nl.dechateau.vertx.orchestration;

import nl.dechateau.vertx.orchestration.builder.CallSequenceBuilder;
import nl.dechateau.vertx.orchestration.builder.CallSyntax;
import nl.dechateau.vertx.orchestration.builder.ExecutionUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import java.util.Map;

public abstract class AbstractEventHandler<E> implements ResponseListener, Handler<E> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventHandler.class);

    private static final int DEFAULT_REQUEST_TIMEOUT = 10000;

    private Vertx vertx;

    private E event;

    private OrchestrationContext context;

    private boolean committed = false;

    private long timer;

    protected AbstractEventHandler(final Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void handle(final E event) {
        this.event = event;

        // Initialize the request context.
        context = new OrchestrationContext(vertx, event);
        initializeContextVariables(context.getVars());

        // Allow for the definition of the call sequence.
        ExecutionUnit<?> unit = defineCallSequence();

        // Set a timeout for the entire sequence of calls.
        timer = vertx.setTimer(DEFAULT_REQUEST_TIMEOUT, new Handler<Long>() {
            @Override
            public void handle(final Long timedOut) {
                endRequestInError(ErrorType.TIMEOUT, "Timeout occurred handling request.");
            }
        });

        // Start the sequence.
        unit.execute(this);
    }

    E getEvent() {
        return event;
    }

    /**
     * This method (the default implementation is empty) can be overridden to allow for request specific initialization of the context.
     *
     * @param contextVariables The <code>Map</code> of variables in the request context to be (further) initialized (i.e. by adding them).
     */
    protected void initializeContextVariables(final Map<String, Object> contextVariables) {
        // Default behaviour: do nothing here.
    }

    /**
     * Allows for the concrete request handler to define the sequence of service calls (handlers) required for handling the complete request.
     *
     * @return The execution context in which the handlers were added.
     */
    protected abstract ExecutionUnit<?> defineCallSequence();

    /**
     * Convenience method for the concrete request handler to start the definition of a new call sequence.
     *
     * @return The context in which to add handlers.
     */
    protected final CallSyntax newCallSequence() {
        return new CallSequenceBuilder(context);
    }

    /**
     * {@inheritDoc}
     */
    public final ExecutionUnit<?> whenTrue(final ExecutionUnit<?> callSequence) {
        return callSequence;
    }

    /**
     * {@inheritDoc}
     */
    public final ExecutionUnit<?> whenFalse(final ExecutionUnit<?> callSequence) {
        return callSequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void completed() {
        callSequenceCompleted(context.getVars());
    }

    /**
     * Allows for the concrete request handler to determine how the request was fulfilled by the sequence of service calls.
     *
     * @param contextVariables The <code>Map</code> of variables in the request context to determine the outcome from.
     */
    protected abstract void callSequenceCompleted(final Map<String, Object> contextVariables);

    /**
     * {@inheritDoc}
     */
    @Override
    public final void error(final String errorMessage) {
        error(null, errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void error(final ErrorType errorType, final String errorMessage) {
        endRequestInError(errorType, errorMessage);
    }

    /**
     * Convenience method for ending a request without a response object.
     */
    protected final void endRequest() {
        if (committed) {
            LOG.warn("Response has already been committed, ignoring.");
            return;
        }
        committed = true;
        vertx.cancelTimer(timer);

        respondToEvent();
    }

    /**
     * Specific implementation for ending a request without a response object.
     */
    abstract void respondToEvent();

    /**
     * Convenience method for ending a request with a response object.
     *
     * @param responseObject The object representing the actual response content.
     */
    protected final void endRequest(final Object responseObject) {
        if (committed) {
            LOG.warn("Response has already been committed, ignoring.");
            return;
        }
        committed = true;
        vertx.cancelTimer(timer);

        respondToEvent(responseObject);
    }

    /**
     * Specific implementation for ending a request with a response object.
     *
     * @param content The object representing the actual response content.
     */
    abstract void respondToEvent(final Object content);

    /**
     * Convenience method for ending a request in failure, with both an error message and an explicit HTTP response status.
     *
     * @param errorType    The type of the error that occurred.
     * @param errorMessage The error message indicating the reason for failure.
     */
    protected final void endRequestInError(final ErrorType errorType, final String errorMessage) {
        if (committed) {
            LOG.warn("Response has already been committed, ignoring.");
            return;
        }
        committed = true;
        vertx.cancelTimer(timer);

        respondToEventInError(errorType, errorMessage);
    }

    /**
     * Specific implementation for ending a request in failure, with an error message and a type for which an event
     * type specific handling may be necessary.
     *
     * @param errorType    The type of the error that occurred.
     * @param errorMessage The error message indicating the reason for failure.
     */
    abstract void respondToEventInError(final ErrorType errorType, final String errorMessage);
}
