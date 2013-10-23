package nl.dechateau.vertx.orchestration.eventbus;

import nl.dechateau.vertx.orchestration.AbstractMessageHandler;
import nl.dechateau.vertx.orchestration.builder.ExecutionUnit;
import nl.dechateau.vertx.orchestration.common.GetCalledCallHandler;
import org.vertx.java.core.Vertx;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.vertx.testtools.VertxAssert.assertThat;

public class OneWayMessageHandler extends AbstractMessageHandler {
    protected OneWayMessageHandler(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected void initializeContextVariables(Map<String, Object> contextVariables) {
        contextVariables.put("number", getMessageParameter("number"));
    }

    @Override
    protected ExecutionUnit<?> defineCallSequence() {
        return newCallSequence()
                .addCall(GetCalledCallHandler.class)
                .build();
    }

    @Override
    protected void callSequenceCompleted(Map<String, Object> contextVariables) {
        assertThat((Integer) contextVariables.get("number"), is(equalTo(1)));
        endRequest();
    }
}
