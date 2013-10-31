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

import nl.dechateau.vertx.orchestration.handler.OrchestrationContext;
import nl.dechateau.vertx.orchestration.handler.OrchestrationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unit in which a number (>= 1) of handlers are executed in parallel.
 *
 * @param <T> The type of the handlers.
 */
public final class ExecutionUnit<T extends OrchestrationHandler> implements ResponseListener {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionUnit.class);

    private final Set<T> handlers;

    private ExecutionUnit<? extends OrchestrationHandler> next;

    private OrchestrationContext context;

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

    public void setNext(final ExecutionUnit<? extends OrchestrationHandler> next) {
        if (this.next != null) {
            throw new IllegalStateException("Next unit already set.");
        }

        this.next = next;
    }

    public void execute(final OrchestrationContext orchestrationContext, final ResponseListener responseListener) {
        this.context = orchestrationContext;

        if (handlers.isEmpty()) {
            throw new IllegalStateException("No handlers added yet.");
        }

        this.responseListener = responseListener;

        for (T handler : handlers) {
            handler.execute(orchestrationContext, this);
        }
    }

    @Override
    public void completed(Map<String, Object> vars) {
        for (T handler : handlers) {
            if (!handler.isCompleted()) {
                // At least one handler is not ready yet, so don't proceed.
                LOG.trace("Method completed() called, but waiting for other handlers in the group to complete.");
                return;
            }
        }

        // All handlers completed; move to the next unit.
        if (next != null) {
            next.execute(context, responseListener);
            return;
        }

        // End of the line; report to the caller.
        responseListener.completed(vars);
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
