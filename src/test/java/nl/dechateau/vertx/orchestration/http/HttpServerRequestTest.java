package nl.dechateau.vertx.orchestration.http;

import nl.dechateau.vertx.orchestration.AbstractHttpServerRequestHandler;
import nl.dechateau.vertx.orchestration.common.DecreasingVerticle;
import nl.dechateau.vertx.orchestration.common.IncreasingVerticle;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.testtools.TestVerticle;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.vertx.testtools.VertxAssert.testComplete;

public class HttpServerRequestTest extends TestVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerRequestTest.class);

    @Override
    public void start(final Future<Void> startResult) {
        Handler<AsyncResult<String>> handler = new Handler<AsyncResult<String>>() {
            private int waitFor = 2;

            @Override
            public void handle(AsyncResult<String> event) {
                if (--waitFor == 0) {
                    HttpServerRequestTest.super.start();
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
        makeRequest(new SingleRequestHandler(vertx));
    }

    @Test
    public void sequentialRequest() {
        makeRequest(new SequentialRequestHandler(vertx));
    }

    @Test
    public void parallelRequest() {
        makeRequest(new ParallelRequestHandler(vertx));
    }

    @Test
    public void conditionalTrueRequest() {
        makeRequest(new ConditionalRequestHandler(vertx), true);
    }

    @Test
    public void conditionalFalseRequest() {
        makeRequest(new ConditionalRequestHandler(vertx), false);
    }

    private void makeRequest(AbstractHttpServerRequestHandler requestHandler, boolean... condition) {
        // Set up a dummy request and contained response.
        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.method()).thenReturn("GET");
        when(request.path()).thenReturn("acme.com/stuff");
        MultiMap params = new CaseInsensitiveMultiMap().add("number", "1");
        if (condition != null && condition.length > 0) {
            params.add("condition", Boolean.toString(condition[0]));
        }
        when(request.params()).thenReturn(params);
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                // Have the test complete when the end() method on the response is called.
                testComplete();
                return null;
            }
        }).when(response).end();

        // Start the test.
        requestHandler.handle(request);
    }
}
