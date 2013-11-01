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

import nl.dechateau.vertx.orchestration.ResponseListener;

/**
 * Interface for all <code>handler</code>s within the orchestration.
 */
public interface OrchestrationHandler {
    /**
     * Called to initiate the execution of the handler.
     *
     * @param orchestrationContext The context within which the orchestration runs.
     * @param responseListener     The instance to which the result must be responded.
     */
    void execute(final OrchestrationContext orchestrationContext, final ResponseListener responseListener);

    /**
     * Called to confirm whether this handler has completed its task yet.
     *
     * @return Whether this handler has completed its task.
     */
    boolean isCompleted();
}
