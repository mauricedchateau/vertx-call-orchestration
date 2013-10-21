package nl.dechateau.vertx.orchestration;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class OrchestrationContext {
    private final Vertx vertx;

    private final Map<String, Object> contextVars = new HashMap<>();

    private HttpServerRequest request;

    private Message<JsonObject> message;

    OrchestrationContext(final Vertx vertx, final Object event) {
        this.vertx = vertx;
        if (event instanceof HttpServerRequest) {
            this.request = (HttpServerRequest) event;
        } else if (event instanceof Message) {
            this.message = (Message<JsonObject>) event;
        }
    }

    final Vertx getVertx() {
        return vertx;
    }

    final EventBus getEventBus() {
        return vertx.eventBus();
    }

    public final String getRequestParameter(final String name) {
        if (request == null) {
            return null;
        }
        return request.params().get(name);
    }

    public final String getHeaderParameter(final String name) {
        if (request == null) {
            return null;
        }
        return request.headers().get(name);
    }

    public final <T> T getMessageParameter(final String name) {
        if (message == null) {
            return null;
        }
        return message.body().getValue(name);
    }

    public final Object getContextVar(final String key) {
        return contextVars.get(key);
    }

    public final Object setContextVar(final String key, final Object value) {
        return contextVars.put(key, value);
    }

    final Map<String, Object> getVars() {
        return contextVars;
    }
}
