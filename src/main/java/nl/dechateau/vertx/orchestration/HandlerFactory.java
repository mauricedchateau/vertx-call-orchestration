package nl.dechateau.vertx.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class HandlerFactory<T extends Handler<HttpServerRequest>> implements Handler<HttpServerRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(HandlerFactory.class);

    private final Vertx vertx;

    private final Class<T> handlerType;

    public HandlerFactory(final Vertx vertx, final Class<T> handlerType) {
        this.vertx = vertx;
        this.handlerType = handlerType;
    }

    @Override
    public void handle(final HttpServerRequest request) {
        // Create a request handler of the given type, and relay the request to it.
        try {
            final Constructor<T> constructor = handlerType.getDeclaredConstructor(Vertx.class);
            final T handler = constructor.newInstance(vertx);
            handler.handle(request);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LOG.error("Unable to instantiate handler of type {} with error {}.", handlerType.getClass().getName(), e
                    .getMessage());
        }
    }
}
