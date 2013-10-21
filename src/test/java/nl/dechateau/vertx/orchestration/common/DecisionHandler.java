package nl.dechateau.vertx.orchestration.common;

import nl.dechateau.vertx.orchestration.AbstractDecisionHandler;
import nl.dechateau.vertx.orchestration.OrchestrationContext;

public class DecisionHandler extends AbstractDecisionHandler {
    public DecisionHandler(OrchestrationContext context) {
        super(context);
    }

    @Override
    public boolean makeDecision() {
        return (Boolean) getContextVar("condition");
    }
}
