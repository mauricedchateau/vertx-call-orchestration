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
package nl.dechateau.vertx.orchestration.handler;

import nl.dechateau.vertx.orchestration.ResponseListener;
import nl.dechateau.vertx.orchestration.ExecutionUnit;

import java.util.Map;

/**
 * Base class for handlers that identify conditional paths within the call sequence.
 */
public abstract class AbstractDecisionHandler implements OrchestrationHandler, ResponseListener {
    private OrchestrationContext context;

    private ExecutionUnit<?> whenTrue;

    private ExecutionUnit<?> whenFalse;

    private ResponseListener responseListener;

    private boolean isCompleted = false;

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
    public final void execute(final OrchestrationContext context, final ResponseListener responseListener) {
        if (whenTrue == null) {
            throw new IllegalStateException("At least a call sequence for 'when true' should have been set.");
        }

        this.context = context;
        this.responseListener = responseListener;

        if (makeDecision()) {
            // Start whenTrue sequence.
            whenTrue.execute(context, this);
        } else {
            if (whenFalse != null) {
                // Start whenFalse sequence.
                whenFalse.execute(context, this);
            } else {
                // No calls to be made within this decision.
                onCompleted(context.getVars());
            }
        }
    }

    /**
     * Make the decision (based on the values of the appropriate context variables).
     *
     * @return Whether the decision was <code>true</code> or <code>false</code>.
     */
    protected abstract boolean makeDecision();

    /**
     * Convenience method for getting a parameter from the context.
     *
     * @param key The key of the parameter.
     */
    protected final Object getContextVar(final String key) {
        if (context == null) {
            return null;
        }

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
    public final void onCompleted(Map<String, Object> vars) {
        if (responseListener == null) {
            throw new IllegalStateException("Method onCompleted() called before handler was executed.");
        }

        // The chosen path is onCompleted.
        isCompleted = true;
        responseListener.onCompleted(vars);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onError(final String errorMessage) {
        if (responseListener == null) {
            throw new IllegalStateException("Method onError() called before handler was executed.");
        }

        isCompleted = true;
        responseListener.onError(errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onError(final ErrorType type, final String errorMessage) {
        if (responseListener == null) {
            throw new IllegalStateException("Method onError() called before handler was executed.");
        }

        isCompleted = true;
        responseListener.onError(type, errorMessage);
    }
}
