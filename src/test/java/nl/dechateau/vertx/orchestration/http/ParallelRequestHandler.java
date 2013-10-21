package nl.dechateau.vertx.orchestration.http;

import nl.dechateau.vertx.orchestration.AbstractHttpServerRequestHandler;
import nl.dechateau.vertx.orchestration.builder.ExecutionUnit;
import nl.dechateau.vertx.orchestration.common.IncreaseCallHandler;
import org.vertx.java.core.Vertx;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.vertx.testtools.VertxAssert.assertThat;

public class ParallelRequestHandler extends AbstractHttpServerRequestHandler {
    protected ParallelRequestHandler(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected void initializeContextVariables(Map<String, Object> contextVariables) {
        contextVariables.put("number", Integer.valueOf(getRequestParameter("number")));
    }

    @Override
    protected ExecutionUnit<?> defineCallSequence() {
        return newCallSequence()
                .addParallelCalls(IncreaseCallHandler.class, IncreaseCallHandler.class)
                .build();
    }

    @Override
    protected void callSequenceCompleted(Map<String, Object> contextVariables) {
        assertThat((Integer) contextVariables.get("number"), is(equalTo(2)));
        endRequest();
    }
}
