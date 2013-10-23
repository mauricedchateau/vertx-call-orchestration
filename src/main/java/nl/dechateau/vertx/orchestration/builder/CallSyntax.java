package nl.dechateau.vertx.orchestration.builder;

import nl.dechateau.vertx.orchestration.AbstractDecisionHandler;
import nl.dechateau.vertx.orchestration.CallHandler;

public interface CallSyntax {
    CallSyntax addCall(final Class<? extends CallHandler> handler);

    CallSyntax addParallelCalls(final Class<? extends CallHandler>... handlers);

    CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                           final ExecutionUnit<?> whenTrue);

    CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                           final ExecutionUnit<?> whenTrue, final ExecutionUnit<?> whenFalse);

    ExecutionUnit<?> build();
}
