package nl.dechateau.vertx.orchestration;

import nl.dechateau.vertx.serialization.SerializationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractReturningCallHandler implements CallHandler, org.vertx.java.core.Handler<Message<JsonObject>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractReturningCallHandler.class);

    protected ResponseListener responder;

    private OrchestrationContext context;

    private boolean isCompleted = false;

    protected AbstractReturningCallHandler(final OrchestrationContext context) {
        this.context = context;
    }

    @Override
    public final void execute(final ResponseListener responder) {
        this.responder = responder;
        try {
            context.getEventBus().send(getDestination(), getCallMessage(), this);
        } catch (SerializationException serEx) {
            LOG.error("Problem de-serializing received data:", serEx);
            isCompleted = true;
            responder.error(serEx.getMessage());
        }
    }

    /**
     * @return The location of the service to which the call is to be made.
     */
    protected abstract String getDestination();

    /**
     * @return The JSON message containing the service call parameters.
     */
    protected abstract JsonObject getCallMessage() throws SerializationException;

    /**
     * Convenience method for retrieval of a request parameter.
     *
     * @param name The name of the parameter.
     * @return The parameter value.
     */
    protected final String getRequestParameter(final String name) {
        try {
            return URLDecoder.decode(context.getRequestParameter(name), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to decode request parameter: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Convenience method for retrieval of a parameter from request header.
     *
     * @param name The name of the header parameter.
     * @return The header parameter value.
     */
    protected final String getHeaderParameter(final String name) {
        return context.getHeaderParameter(name);
    }

    /**
     * Convenience method for retrieval of the data received with a POST request.
     *
     * @return The data from the POST request.
     */
    protected final String getPostData() {
        return getRequestParameter(AbstractHttpServerRequestHandler.POST_DATA_KEY);
    }

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
    public final void handle(final Message<JsonObject> replyMessage) {
        final JsonObject reply = replyMessage.body();
        LOG.trace("Handle reply message from service call: {}", reply.encode());

        if (StringUtils.equals(reply.getString("status"), "ok")) {
            final JsonElement result = reply.getValue("result");

            if (result != null) {
                try {
                    if (result.isArray()) {
                        processResult((JsonArray) result);
                    } else {
                        processResult((JsonObject) result);
                    }
                } catch (SerializationException serEx) {
                    LOG.error("Problem deserializing received data:", serEx);
                    isCompleted = true;
                    responder.error(serEx.getMessage());
                    return;
                } catch (Throwable throwable) {
                    isCompleted = true;
                    responder.error(throwable.getMessage());
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
        responder.completed();
    }

    /**
     * Process the result of the service call:
     * <ul>
     * <li>De-serialize the JSON object into a Java object, or get the value(s) directly from the JSON element;</li>
     * <li>If necessary, perform additional processing;</li>
     * <li>Store the result in the context for further processing by other call handlers and/or the request handler.</li>
     * </ul>
     * <b>Override this method or the next one when applicable.</b>
     *
     * @param result The JSON result object.
     * @throws SerializationException If the JSON object could not be de-serialized into a Java object properly.
     */
    protected void processResult(final JsonObject result) throws SerializationException {
        LOG.warn("This method should be overridden! (AbstractReturningCallHandler.processResult)");
    }

    /**
     * Process the result of the service call:
     * <ul>
     * <li>De-serialize the JSON array into a Java object, or get the value(s) directly from the JSON element;</li>
     * <li>If necessary, perform additional processing;</li>
     * <li>Store the result in the context for further processing by other call handlers and/or the request handler.</li>
     * </ul>
     * <b>Override this method or the previous one when applicable.</b>
     *
     * @param result The JSON result array.
     * @throws SerializationException If the JSON array could not be de-serialized into a Java object properly.
     */
    protected void processResult(final JsonArray result) throws SerializationException {
        LOG.warn("This method should be overridden! (AbstractReturningCallHandler.processResult)");
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
     * Process the result of the service call in case of an error.
     * <p/>
     * <b>Override this method when something needs to be done in this case.</b>
     *
     * @param errorMessage The message that accompanied the error that occurred.
     */
    protected void processErrorResult(final String errorMessage) {
        responder.error("Service call returned error: " + errorMessage);
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
     * Called by the <code>ServiceCallOrchestrator</code> to confirm whether this handler has completed its task yet.
     *
     * @return Whether this handler has made its service call <i>and</i> processed the corresponding result.
     */
    @Override
    public final boolean isCompleted() {
        return isCompleted;
    }
}
