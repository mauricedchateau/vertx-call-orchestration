package nl.dechateau.vertx.orchestration;

import nl.dechateau.vertx.serialization.SerializationException;
import nl.dechateau.vertx.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public abstract class AbstractMessageHandler extends AbstractEventHandler<Message<JsonObject>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageHandler.class);

    protected AbstractMessageHandler(final Vertx vertx) {
        super(vertx);
    }

    /**
     * Convenience method for retrieval of a message parameter.
     *
     * @param name The name of the parameter.
     * @return The parameter value.
     */
    protected final <T> T getMessageParameter(final String name) {
        return getEvent().body().getValue(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void respondToEvent() {
        sendReply(getEvent(), null);

        LOG.debug("Finished handling message: {}; no response object sent.", getEvent().body());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void respondToEvent(final Object responseObject) {
        try {
            String responseString = Serializer.write(responseObject);
            sendReply(getEvent(), new JsonObject(responseString));
            LOG.trace("Sent response {}.", responseString);
        } catch (SerializationException serEx) {
            LOG.warn("Problem writing response data: {}.", serEx.getMessage());
            sendError(getEvent(), serEx.getMessage());
        }

        LOG.trace("Finished handling message: {}.", getEvent().body());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void respondToEventInError(final ErrorType errorType, final String errorMessage) {
        StringBuilder sb = new StringBuilder("Problem encountered during the handling of a call for message: ")
                .append(getEvent().body()).append(" with error: ");
        if (errorType != null) {
            switch (errorType) {
                case CONTENT_MISSING:
                    sb.append("Requested content was not found: ").append(errorMessage);
                    break;
                case TIMEOUT:
                    sb.append("The request could not be completed in time: ").append(errorMessage);
                    break;
                default:
                    sb.append(errorMessage);
                    break;
            }
        }
        sendError(getEvent(), sb.toString());

        LOG.debug("Finished handling message: {} in error: {}.", getEvent().body(), sb);
    }

    private void sendReply(Message<JsonObject> message, JsonObject json) {
        if (json == null) {
            json = new JsonObject();
        }
        json.putString("status", "ok");
        message.reply(json);
    }

    private void sendError(Message<JsonObject> message, String error) {
        message.reply(new JsonObject().putString("status", "error").putString("message", error));
    }
}
