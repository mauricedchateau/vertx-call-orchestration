package nl.dechateau.vertx.orchestration.eventbus;

import nl.dechateau.vertx.orchestration.AbstractMessageHandler;
import nl.dechateau.vertx.orchestration.common.DecreasingVerticle;
import nl.dechateau.vertx.orchestration.common.IncreasingVerticle;
import nl.dechateau.vertx.orchestration.http.ConditionalRequestHandler;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.mockito.Mockito.*;
import static org.vertx.testtools.VertxAssert.testComplete;

public class MessageTest extends TestVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MessageTest.class);

    @Override
    public void start(final Future<Void> startResult) {
        Handler<AsyncResult<String>> handler = new Handler<AsyncResult<String>>() {
            private int waitFor = 2;

            @Override
            public void handle(AsyncResult<String> event) {
                if (--waitFor == 0) {
                    MessageTest.super.start();
                    startResult.setResult(null);
                    LOG.trace("HttpServerRequestTest verticle started.");
                }
            }
        };
        container.deployVerticle(IncreasingVerticle.class.getName(), handler);
        container.deployVerticle(DecreasingVerticle.class.getName(), handler);
    }

    @Test
    public void singleRequest() {
        makeRequest(new SingleMessageHandler(vertx));
    }

    @Test
    public void sequentialRequest() {
        makeRequest(new SequentialMessageHandler(vertx));
    }

    @Test
    public void parallelRequest() {
        makeRequest(new ParallelMessageHandler(vertx));
    }

    @Test
    public void conditionalTrueRequest() {
        makeRequest(new ConditionalMessageHandler(vertx), true);
    }

    @Test
    public void conditionalFalseRequest() {
        makeRequest(new ConditionalMessageHandler(vertx), false);
    }

    private void makeRequest(AbstractMessageHandler messageHandler, boolean... condition) {
        // Set up a dummy message and contained response.
        @SuppressWarnings("unchecked")
        Message<JsonObject> message = (Message<JsonObject>) mock(Message.class);
        JsonObject body = new JsonObject().putNumber("number", 1);
        if (condition != null && condition.length > 0) {
            body.putBoolean("condition", condition[0]);
        }
        when(message.body()).thenReturn(body);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                // Have the test complete when the end() method on the response is called.
                testComplete();
                return null;
            }
        }).when(message).reply(any(JsonObject.class));

        // Start the test.
        messageHandler.handle(message);
    }
}
