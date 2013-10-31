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

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * The context within which calls are orchestrated.
 */
public final class OrchestrationContext {
    private final Vertx vertx;

    private final Map<String, Object> contextVars = new HashMap<>();

    public OrchestrationContext(final Vertx vertx) {
        this.vertx = vertx;
    }

    final Vertx getVertx() {
        return vertx;
    }

    final EventBus getEventBus() {
        return vertx.eventBus();
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
