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
package nl.dechateau.vertx.orchestration.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class IncreasingVerticle extends BusModBase {
    public static final String INCREASING_VERTICLE_ADDRESS = "INCREASING_VERTICLE_ADDRESS";

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
