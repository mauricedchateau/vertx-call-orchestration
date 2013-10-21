package nl.dechateau.vertx.orchestration.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class IncreasingVerticle extends BusModBase {
    static final String INCREASING_VERTICLE_ADDRESS = "INCREASING_VERTICLE_ADDRESS";

    private static final Logger LOG = LoggerFactory.getLogger(IncreasingVerticle.class);

    @Override
    public void start() {
        super.start();
        eb.registerHandler(INCREASING_VERTICLE_ADDRESS, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                LOG.info("Message received in IncreasingVerticle: " + message.body().encode());
                Integer number = message.body().getInteger("input");
                sendOK(message, new JsonObject().putObject("result", new JsonObject().putNumber("output", number + 1)));
            }
        });
        LOG.trace("IncreasingVerticle started.");
    }
}
