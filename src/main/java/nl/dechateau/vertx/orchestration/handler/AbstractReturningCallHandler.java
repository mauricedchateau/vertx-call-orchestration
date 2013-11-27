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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

/**
 * Base class for handlers that make calls over the event bus and expect an answer.
 */
public abstract class AbstractReturningCallHandler implements CallHandler, Handler<Message<JsonObject>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractReturningCallHandler.class);

    private ResponseListener responseListener;

    private OrchestrationContext context;

    private boolean isCompleted = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute(final OrchestrationContext context, final ResponseListener responseListener) {
        this.context = context;
        this.responseListener = responseListener;

        try {
            context.getEventBus().send(getDestination(), getCallMessage(), this);
        } catch (Exception ex) {
            LOG.error("Problem constructing/sending message:", ex);
            isCompleted = true;
            responseListener.onError(ex.getMessage());
        }
    }

    /**
     * @return The location of the verticle to which the call is to be made.
     */
    protected abstract String getDestination();

    /**
     * @return The JSON message containing the verticle call parameters.
     */
    protected abstract JsonObject getCallMessage();

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
    public final void handle(final Message<JsonObject> replyMessage) {
        final JsonObject reply = replyMessage.body();
        LOG.trace("Handle reply message from verticle call: {}", reply.encode());

        if (StringUtils.equals(reply.getString("status"), "ok")) {
            final JsonElement result = reply.getValue("result");

            if (result != null) {
                try {
                    if (result.isArray()) {
                        processResult((JsonArray) result);
                    } else {
                        processResult((JsonObject) result);
                    }
                } catch (Exception ex) {
                    LOG.error("Problem processing received data:", ex);
                    isCompleted = true;
                    responseListener.onError(ex.getMessage());
                    return;
                }
            } else {
                processEmptyResult();
            }
        } else {
            isCompleted = true;
            processErrorResult(reply.getString("message"));
            return;
        }

        isCompleted = true;
        responseListener.onCompleted(context.getVars());
    }

    /**
     * Process the result of the verticle call:
     * <ul>
     * <li>De-serialize the JSON object into a Java object, or get the value(s) directly from the JSON element;</li>
     * <li>If necessary, perform additional processing;</li>
     * <li>Store the result in the context for further processing by other call handlers and/or the request handler.</li>
     * </ul>
     * <b>Override this method or the next one when applicable.</b>
     *
     * @param result The JSON result object.
     */
    protected void processResult(final JsonObject result) {
        LOG.warn("This method should have been overridden! (AbstractReturningCallHandler.processResult(JsonObject))");
    }

    /**
     * Process the result of the verticle call:
     * <ul>
     * <li>De-serialize the JSON array into a Java object, or get the value(s) directly from the JSON element;</li>
     * <li>If necessary, perform additional processing;</li>
     * <li>Store the result in the context for further processing by other call handlers and/or the request handler.</li>
     * </ul>
     * <b>Override this method or the previous one when applicable.</b>
     *
     * @param result The JSON result array.
     */
    protected void processResult(final JsonArray result) {
        LOG.warn("This method should have been overridden! (AbstractReturningCallHandler.processResult(JsonArray))");
    }

    /**
     * Process the result of the service call in case it is empty:
     * <ul>
     * <li>If applicable, set indications of this empty result in the context.</li>
     * </ul>
     * <b>Override this method when something needs to be done in this case.</b>
     */
    protected void processEmptyResult() {
        LOG.debug("No result data in response (should there be?).");
    }

    /**
     * Process the result of the service call in case of an onError.
     * <p/>
     * <b>Override this method when something needs to be done in this case.</b>
     *
     * @param errorMessage The message that accompanied the onError that occurred.
     */
    protected void processErrorResult(final String errorMessage) {
        responseListener.onError("Service call returned onError: " + errorMessage);
    }

    /**
     * Convenience method for setting a parameter in the context.
     *
     * @param key   The name of the parameter.
     * @param value The value of the parameter.
     */
    protected final Object setContextVar(final String key, final Object value) {
        return context.setContextVar(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isCompleted() {
        return isCompleted;
    }
}
