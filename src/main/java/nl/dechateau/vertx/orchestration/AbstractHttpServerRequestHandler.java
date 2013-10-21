package nl.dechateau.vertx.orchestration;

import io.netty.handler.codec.http.HttpResponseStatus;
import nl.dechateau.vertx.serialization.SerializationException;
import nl.dechateau.vertx.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractHttpServerRequestHandler extends AbstractEventHandler<HttpServerRequest> {
    static final String POST_DATA_KEY = "POST_DATA";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpServerRequestHandler.class);

    protected AbstractHttpServerRequestHandler(final Vertx vertx) {
        super(vertx);
    }

    /**
     * Convenience method for retrieval of a request parameter.
     *
     * @param name The name of the parameter.
     * @return The parameter value.
     */
    protected final String getRequestParameter(final String name) {
        try {
            return URLDecoder.decode(getEvent().params().get(name), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to decode request parameter: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Convenience method for retrieval of a parameter from the request's header.
     *
     * @param name The name of the header parameter.
     * @return The header parameter value.
     */
    protected final String getHeaderParameter(final String name) {
        return getEvent().headers().get(name);
    }

    /**
     * Convenience method for retrieval of the data received with a POST request.
     *
     * @return The data from the POST request.
     */
    protected final String getPostData() {
        return getRequestParameter(POST_DATA_KEY);
    }

    /**
     * Convenience method for setting a parameter in the response header.
     *
     * @param name  The name of the parameter.
     * @param value The value of the parameter.
     */
    protected final void setResponseHeaderParameter(final String name, final String value) {
        getEvent().response().headers().set(name, value);
    }

    /**
     * Convenience method for removing a parameter in the response header.
     *
     * @param name The name of the parameter.
     */
    protected final void removeResponseHeaderParameter(final String name) {
        getEvent().response().headers().remove(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void respondToEvent() {
        getEvent().response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end();

        LOG.debug("Finished handling {} request for {}; no response object sent.", getEvent().method(),
                getEvent().path());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void respondToEvent(final Object responseObject) {
        getEvent().response().putHeader("content-type", "application/json");
        try {
            String responseString = Serializer.write(responseObject);
            getEvent().response().end(responseString);
            LOG.trace("Sent response {}.", responseString);
        } catch (SerializationException serEx) {
            LOG.warn("Problem writing response data: {}.", serEx.getMessage());
            getEvent().response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
        }

        LOG.trace("Finished handling {} request for {}.", getEvent().method(), getEvent().path());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void respondToEventInError(final ErrorType errorType, final String errorMessage) {
        String message = "Problem encountered during the handling of a call for a " + getEvent().method()
                + " request: " + errorMessage;
        HttpResponseStatus statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        if (errorType != null) {
            switch (errorType) {
                case CONTENT_MISSING:
                    statusCode = HttpResponseStatus.NO_CONTENT;
                    break;
                case TIMEOUT:
                    statusCode = HttpResponseStatus.SERVICE_UNAVAILABLE;
                    break;
                default:
                    // Keep 500.
                    break;
            }
        }
        getEvent().response().setStatusCode(statusCode.code()).end(message);

        LOG.debug("Finished handling {} request for {} in error: {}.",
                new Object[]{getEvent().method(), getEvent().path(), message});
    }
}
