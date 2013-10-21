package nl.dechateau.vertx.orchestration.common;

import nl.dechateau.vertx.orchestration.AbstractCallHandler;
import nl.dechateau.vertx.orchestration.OrchestrationContext;
import nl.dechateau.vertx.serialization.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

public class DecreaseCallHandler extends AbstractCallHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DecreaseCallHandler.class);

    public DecreaseCallHandler(OrchestrationContext context) {
        super(context);
    }

    @Override
    protected String getDestination() {
        return DecreasingVerticle.DECREASING_VERTICLE_ADDRESS;
    }

    @Override
    protected JsonObject getCallMessage() throws SerializationException {
        return new JsonObject().putNumber("input", (Integer) getContextVar("number"));
    }

    @Override
    protected void processResult(JsonObject result) {
        LOG.info("Retrieved result: " + result.encode());
        setContextVar("number", result.getInteger("output"));
    }
}
