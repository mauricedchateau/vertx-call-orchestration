package nl.dechateau.vertx.orchestration.builder;

import nl.dechateau.vertx.orchestration.AbstractCallHandler;
import nl.dechateau.vertx.orchestration.AbstractDecisionHandler;

public interface CallSyntax {
    CallSyntax addCall(final Class<? extends AbstractCallHandler> handler);

    CallSyntax addParallelCalls(final Class<? extends AbstractCallHandler>... handlers);

    CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                           final ExecutionUnit<?> whenTrue);

    CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                           final ExecutionUnit<?> whenTrue, final ExecutionUnit<?> whenFalse);

    ExecutionUnit<?> build();
}
