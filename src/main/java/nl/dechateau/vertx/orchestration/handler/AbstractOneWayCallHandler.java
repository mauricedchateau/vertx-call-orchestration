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
import nl.dechateau.vertx.serialization.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

/**
 * Base class for handlers that make calls over the event bus but don't expect an answer.
 */
public abstract class AbstractOneWayCallHandler implements CallHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOneWayCallHandler.class);

    private OrchestrationContext context;

    private boolean isCompleted = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute(final OrchestrationContext context, final ResponseListener responseListener) {
        this.context = context;

        try {
            context.getEventBus().send(getDestination(), getCallMessage());
        } catch (SerializationException serEx) {
            LOG.error("Problem de-serializing received data:", serEx);
            isCompleted = true;
            responseListener.error(serEx.getMessage());
            return;
        }

        isCompleted = true;
        responseListener.completed(context.getVars());
    }

    /**
     * @return The location of the verticle to which the call is to be made.
     */
    protected abstract String getDestination();

    /**
     * @return The JSON message containing the verticle call parameters.
     */
    protected abstract JsonObject getCallMessage() throws SerializationException;

    /**
     * Convenience method for getting a parameter from the context.
     *
     * @param key The key of the parameter.
     * @return The value of the context parameter, or <code>null</code> if there is no parameter with the given key.
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
}
