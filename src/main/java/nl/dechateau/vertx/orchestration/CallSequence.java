/*
 * Copyright 2013 Maurice de Chateau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.dechateau.vertx.orchestration;

import nl.dechateau.vertx.orchestration.handler.AbstractDecisionHandler;
import nl.dechateau.vertx.orchestration.handler.CallHandler;
import nl.dechateau.vertx.orchestration.handler.OrchestrationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Pivotal class for the orchestration: use to build call sequences and execute them.
 */
public final class CallSequence implements ResponseListener {
    private static final Logger LOG = LoggerFactory.getLogger(CallSequence.class);

    private static final long DEFAULT_REQUEST_TIMEOUT = 10000;

    private final Vertx vertx;

    private final OrchestrationContext context;

    private final ExecutionUnit<?> firstUnit;

    private long timeout;

    private ResponseListener listener;

    private boolean committed = false;

    private long timer;

    private CallSequence(final Builder builder) {
        vertx = builder.vertx;
        firstUnit = builder.firstUnit;

        context = new OrchestrationContext(vertx);

        timeout = DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Set a parameter in the orchestration context. Such values are for use by the call handlers.
     *
     * @param key   The name of the parameter.
     * @param value The value of the parameter.
     */
    public final Object setContextVar(final String key, final Object value) {
        return context.setContextVar(key, value);
    }

    /**
     * Override the default time value of 10 seconds.
     *
     * @param timeout The custom timeout value (in millisecs).
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Start the defined call sequence.
     *
     * @param listener The object that needs to be informed of the outcome.
     */
    public final void execute(final ResponseListener listener) {
        if (firstUnit == null) {
            throw new IllegalStateException("Executing call sequence without defining it first.");
        }

        // Set a timeout for the entire sequence of calls.
        timer = vertx.setTimer(timeout, new Handler<Long>() {
            @Override
            public void handle(final Long timedOut) {
                committed = true;
                listener.onError(ResponseListener.ErrorType.TIMEOUT, "Timeout occurred handling request.");
            }
        });

        // Start the sequence.
        this.listener = listener;
        firstUnit.execute(context, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onCompleted(Map<String, Object> vars) {
        if (committed) {
            LOG.warn("Response has already been committed, ignoring.");
            return;
        }
        committed = true;
        vertx.cancelTimer(timer);

        listener.onCompleted(vars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onError(final String errorMessage) {
        if (committed) {
            LOG.warn("Response has already been committed, ignoring.");
            return;
        }
        committed = true;
        vertx.cancelTimer(timer);

        listener.onError(errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onError(final ErrorType errorType, final String errorMessage) {
        if (committed) {
            LOG.warn("Response has already been committed, ignoring.");
            return;
        }
        committed = true;
        vertx.cancelTimer(timer);

        listener.onError(errorType, errorMessage);
    }

    public static class Builder implements CallSyntax {
        private final Vertx vertx;

        private ExecutionUnit<?> firstUnit;

        private Builder(Vertx vertx) {
            this.vertx = vertx;
        }

        public static CallSyntax createCallSequence(Vertx vertx) {
            return new Builder(vertx);
        }

        public static ExecutionUnit<?> whenTrue(final CallSequence callSequence) {
            return callSequence.firstUnit;
        }

        public static ExecutionUnit<?> whenFalse(final CallSequence callSequence) {
            return callSequence.firstUnit;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final CallSyntax addCall(final Class<? extends CallHandler> handler) {
            if (handler == null) {
                throw new IllegalArgumentException("Received NULL in an attempt to add a service call handler.");
            }

            // Instantiate an execution unit with this single call handler.
            ExecutionUnit<CallHandler> unit = new ExecutionUnit<>();
            try {
                unit.addHandler(handler.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
                throw new BuilderException("Cannot instantiate handler.", ex);
            }

            addUnitToSequence(unit);

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SafeVarargs
        public final CallSyntax addParallelCalls(final Class<? extends CallHandler>... handlers) {
            if (handlers == null || handlers.length == 0) {
                throw new IllegalArgumentException("Received NULL or empty list in an attempt to add a set of service call handlers.");
            }

            // Instantiate an execution unit with these call handlers.
            ExecutionUnit<CallHandler> unit = new ExecutionUnit<>();
            for (Class<? extends CallHandler> handler : handlers) {
                try {
                    unit.addHandler(handler.getConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                    LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
                    throw new BuilderException("Cannot instantiate handler.", ex);
                }
            }

            addUnitToSequence(unit);

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                                            final ExecutionUnit<?> whenTrue) {
            if (handler == null) {
                throw new IllegalArgumentException("Received NULL in an attempt to add a decision handler.");
            }
            if (whenTrue == null) {
                throw new IllegalArgumentException("Missing whenTrue option for decision handler.");
            }

            // Instantiate an execution unit with this decision handler.
            ExecutionUnit<AbstractDecisionHandler> unit = new ExecutionUnit<>();
            AbstractDecisionHandler decision = null;
            try {
                decision = handler.getConstructor().newInstance();
                unit.addHandler(decision);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
                throw new BuilderException("Cannot instantiate handler.", ex);
            }

            // Add whenTrue and whenFalse to the decision handler.
            if (decision != null) {
                decision.setWhenTrue(whenTrue);
            }

            addUnitToSequence(unit);

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                                            final ExecutionUnit<?> whenTrue, final ExecutionUnit<?> whenFalse) {
            if (handler == null) {
                throw new IllegalArgumentException("Received NULL in an attempt to add a decision handler.");
            }
            if (whenTrue == null || whenFalse == null) {
                throw new IllegalArgumentException("Missing whenTrue or/and whenFalse options for decision handler.");
            }

            // Instantiate an execution unit with this decision handler.
            ExecutionUnit<AbstractDecisionHandler> unit = new ExecutionUnit<>();
            AbstractDecisionHandler decision = null;
            try {
                decision = handler.getConstructor().newInstance();
                unit.addHandler(decision);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
                throw new BuilderException("Cannot instantiate handler.", ex);
            }

            // Add whenTrue and whenFalse to the decision handler.
            if (decision != null) {
                decision.setWhenTrue(whenTrue);
                decision.setWhenFalse(whenFalse);
            }

            addUnitToSequence(unit);

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final CallSequence build() {
            return new CallSequence(this);
        }

        private void addUnitToSequence(final ExecutionUnit<?> unit) {
            if (firstUnit == null) {
                firstUnit = unit;
                return;
            }

            ExecutionUnit<?> last = firstUnit;
            while (last.getNext() != null) {
                last = last.getNext();
            }
            last.setNext(unit);
        }
    }

    public static class BuilderException extends RuntimeException {
        public BuilderException(String errorMessage, Exception cause) {
            super(errorMessage, cause);
        }
    }
}
