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
package nl.dechateau.vertx.orchestration.handler;

import nl.dechateau.vertx.orchestration.verticle.IncreasingVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

public class IncreaseCallHandler extends AbstractReturningCallHandler {
    private static final Logger LOG = LoggerFactory.getLogger(IncreaseCallHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDestination() {
        return IncreasingVerticle.INCREASING_VERTICLE_ADDRESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JsonObject getCallMessage() {
        return new JsonObject().putNumber("input", (Integer) getContextVar("number"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processResult(JsonObject result) {
        LOG.info("Retrieved result: " + result.encode());
        setContextVar("number", result.getInteger("output"));
    }
}
