package nl.dechateau.vertx.orchestration.common;

import nl.dechateau.vertx.orchestration.AbstractOneWayCallHandler;
import nl.dechateau.vertx.orchestration.OrchestrationContext;
import nl.dechateau.vertx.serialization.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

public class GetCalledCallHandler extends AbstractOneWayCallHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GetCalledCallHandler.class);

    public GetCalledCallHandler(OrchestrationContext context) {
        super(context);
    }

    @Override
    protected String getDestination() {
        return IncreasingVerticle.INCREASING_VERTICLE_ADDRESS;
    }

    @Override
    protected JsonObject getCallMessage() throws SerializationException {
        return new JsonObject().putNumber("input", (Integer) getContextVar("number"));
    }
}
