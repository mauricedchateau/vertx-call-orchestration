package nl.dechateau.vertx.orchestration.builder;

import nl.dechateau.vertx.orchestration.Handler;
import nl.dechateau.vertx.orchestration.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public final class ExecutionUnit<T extends Handler> implements ResponseListener {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionUnit.class);

    private final Set<T> handlers;

    private ExecutionUnit<? extends Handler> next;

    private ResponseListener responseListener;

    public ExecutionUnit() {
        handlers = new HashSet<>();
    }

    public boolean addHandler(final T handler) {
        return handlers.add(handler);
    }

    ExecutionUnit<?> getNext() {
        return next;
    }

    public void setNext(final ExecutionUnit<? extends Handler> next) {
        if (this.next != null) {
            throw new IllegalStateException("Next unit already set.");
        }

        this.next = next;
    }

    public void execute(final ResponseListener responseListener) {
        if (handlers.isEmpty()) {
            throw new IllegalStateException("No handlers added yet.");
        }

        this.responseListener = responseListener;

        for (T handler : handlers) {
            handler.execute(this);
        }
    }

    @Override
    public void completed() {
        for (T handler : handlers) {
            if (!handler.isCompleted()) {
                // At least one handler is not ready yet, so don't proceed.
                LOG.trace("Method completed() called, but waiting for other handlers in the group to complete.");
                return;
            }
        }

        // All handlers completed; move to the next unit.
        if (next != null) {
            next.execute(responseListener);
            return;
        }

        // End of the line; report to the caller.
        responseListener.completed();
    }

    @Override
    public void error(final String errorMessage) {
        responseListener.error(errorMessage);
    }

    @Override
    public void error(final ErrorType type, final String errorMessage) {
        responseListener.error(type, errorMessage);
    }
}
