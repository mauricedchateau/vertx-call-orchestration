package nl.dechateau.vertx.orchestration;

import nl.dechateau.vertx.serialization.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractOneWayCallHandler implements CallHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOneWayCallHandler.class);

    private OrchestrationContext context;

    private boolean isCompleted = false;

    protected AbstractOneWayCallHandler(final OrchestrationContext context) {
        this.context = context;
    }

    @Override
    public final void execute(final ResponseListener responder) {
        try {
            context.getEventBus().send(getDestination(), getCallMessage());
        } catch (SerializationException serEx) {
            LOG.error("Problem de-serializing received data:", serEx);
            isCompleted = true;
            responder.error(serEx.getMessage());
            return;
        }

        isCompleted = true;
        responder.completed();
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
    public final boolean isCompleted() {
        return isCompleted;
    }
}
