package nl.dechateau.vertx.orchestration.http;

import nl.dechateau.vertx.orchestration.AbstractHttpServerRequestHandler;
import nl.dechateau.vertx.orchestration.builder.ExecutionUnit;
import nl.dechateau.vertx.orchestration.common.DecisionHandler;
import nl.dechateau.vertx.orchestration.common.DecreaseCallHandler;
import nl.dechateau.vertx.orchestration.common.IncreaseCallHandler;
import org.vertx.java.core.Vertx;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.vertx.testtools.VertxAssert.assertThat;

public class TwoWayConditionalRequestHandler extends AbstractHttpServerRequestHandler {
    protected TwoWayConditionalRequestHandler(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected void initializeContextVariables(Map<String, Object> contextVariables) {
        contextVariables.put("number", Integer.valueOf(getRequestParameter("number")));
        contextVariables.put("condition", Boolean.valueOf(getRequestParameter("condition")));
    }

    @Override
    protected ExecutionUnit<?> defineCallSequence() {
        return newCallSequence()
                .addDecision(DecisionHandler.class,
                        whenTrue(newCallSequence().addCall(IncreaseCallHandler.class).build()),
                        whenFalse(newCallSequence().addCall(DecreaseCallHandler.class).build()))
                .build();
    }

    @Override
    protected void callSequenceCompleted(Map<String, Object> contextVariables) {
        if ((Boolean) contextVariables.get("condition")) {
            assertThat((Integer) contextVariables.get("number"), is(equalTo(2)));
        } else {
            assertThat((Integer) contextVariables.get("number"), is(equalTo(0)));
        }
        endRequest();
    }
}
