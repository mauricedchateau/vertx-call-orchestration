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
package nl.dechateau.vertx.orchestration;

import nl.dechateau.vertx.orchestration.handler.AbstractDecisionHandler;
import nl.dechateau.vertx.orchestration.handler.CallHandler;

/**
 * Interface for the fluent API of the call sequence builder.
 */
public interface CallSyntax {
    CallSyntax addCall(final Class<? extends CallHandler> handler);

    CallSyntax addParallelCalls(final Class<? extends CallHandler>... handlers);

    CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                           final ExecutionUnit<?> whenTrue);

    CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                           final ExecutionUnit<?> whenTrue, final ExecutionUnit<?> whenFalse);

    CallSequence build();
}
