package nl.dechateau.vertx.orchestration.builder;

import nl.dechateau.vertx.orchestration.AbstractDecisionHandler;
import nl.dechateau.vertx.orchestration.CallHandler;
import nl.dechateau.vertx.orchestration.OrchestrationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public final class CallSequenceBuilder implements CallSyntax {
    private static final Logger LOG = LoggerFactory.getLogger(CallSequenceBuilder.class);

    private OrchestrationContext context;

    private ExecutionUnit<?> first;

    public CallSequenceBuilder(OrchestrationContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CallSyntax addCall(final Class<? extends CallHandler> handler) {
        if (handler == null) {
            // Nothing to add.
            LOG.warn("Received NULL in an attempt to add a service call handler.");
            return this;
        }

        // Instantiate an execution unit with this single call handler.
        ExecutionUnit<CallHandler> unit = new ExecutionUnit<>();
        try {
            unit.addHandler(handler.getConstructor(OrchestrationContext.class).newInstance(context));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
        }

        addUnitToSequence(unit);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SafeVarargs
    public final CallSyntax addParallelCalls(final Class<? extends CallHandler>... handlers) {
        if (handlers == null || handlers.length == 0) {
            // Nothing to add.
            LOG.warn("Received NULL or empty list in an attempt to add a set of service call handlers.");
            return this;
        }

        // Instantiate an execution unit with these call handlers.
        ExecutionUnit<CallHandler> unit = new ExecutionUnit<>();
        for (Class<? extends CallHandler> handler : handlers) {
            try {
                unit.addHandler(handler.getConstructor(OrchestrationContext.class).newInstance(context));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
            }
        }

        addUnitToSequence(unit);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                                  final ExecutionUnit<?> whenTrue) {
        if (handler == null) {
            // Nothing to add.
            throw new IllegalStateException("Received NULL in an attempt to add a decision handler.");
        }
        if (whenTrue == null) {
            // Incomplete decision.
            throw new IllegalStateException("Missing whenTrue option for decision handler.");
        }

        // Instantiate an execution unit with this decision handler.
        ExecutionUnit<AbstractDecisionHandler> unit = new ExecutionUnit<>();
        AbstractDecisionHandler decision = null;
        try {
            decision = handler.getConstructor(OrchestrationContext.class).newInstance(context);
            unit.addHandler(decision);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
        }

        // Add whenTrue and whenFalse to the decision handler.
        if (decision != null) {
            decision.setWhenTrue(whenTrue);
        }

        addUnitToSequence(unit);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CallSyntax addDecision(final Class<? extends AbstractDecisionHandler> handler,
                                  final ExecutionUnit<?> whenTrue, final ExecutionUnit<?> whenFalse) {
        if (handler == null) {
            // Nothing to add.
            throw new IllegalStateException("Received NULL in an attempt to add a decision handler.");
        }
        if (whenTrue == null || whenFalse == null) {
            // Incomplete decision.
            throw new IllegalStateException("Missing whenTrue or/and whenFalse options for decision handler.");
        }

        // Instantiate an execution unit with this decision handler.
        ExecutionUnit<AbstractDecisionHandler> unit = new ExecutionUnit<>();
        AbstractDecisionHandler decision = null;
        try {
            decision = handler.getConstructor(OrchestrationContext.class).newInstance(context);
            unit.addHandler(decision);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            LOG.error("Problem instantiating handler for class {}: {}.", handler.getName(), ex.getMessage());
        }

        // Add whenTrue and whenFalse to the decision handler.
        if (decision != null) {
            decision.setWhenTrue(whenTrue);
            decision.setWhenFalse(whenFalse);
        }

        addUnitToSequence(unit);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ExecutionUnit<?> build() {
        return first;
    }

    private void addUnitToSequence(final ExecutionUnit<?> unit) {
        if (first == null) {
            first = unit;
            return;
        }

        ExecutionUnit<?> last = first;
        while (last.getNext() != null) {
            last = last.getNext();
        }
        last.setNext(unit);
    }
}
