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

import nl.dechateau.vertx.orchestration.handler.IncreaseCallHandler;
import nl.dechateau.vertx.orchestration.verticle.IncreasingVerticle;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import java.util.Map;

import static nl.dechateau.vertx.orchestration.CallSequence.Builder.createCallSequence;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.vertx.testtools.VertxAssert.assertThat;
import static org.vertx.testtools.VertxAssert.testComplete;

public class SequenceTest extends TestVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(SequenceTest.class);

    @Override
    public void start(final Future<Void> startResult) {
        Handler<AsyncResult<String>> handler = new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> event) {
                SequenceTest.super.start();
                startResult.setResult(null);
                LOG.trace("HttpServerRequestTest verticle started.");
            }
        };
        container.deployVerticle(IncreasingVerticle.class.getName(), handler);
    }

    @Test
    public void singleRequests() {
        final CallSequence sequence = createCallSequence(vertx)
                .addCall(IncreaseCallHandler.class)
                .build();
        sequence.setContextVar("number", 1);

        final Integer expectedOutcome = 2;

        final ResponseListener listener = mock(ResponseListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Map<String, Object> contextVars = (Map<String, Object>) invocationOnMock.getArguments()[0];
                assertThat((Integer) contextVars.get("number"), is(equalTo(expectedOutcome)));

                // Have the test complete when the onCompleted() method on the listener is called.
                testComplete();
                return null;
            }
        }).when(listener).onCompleted(Matchers.<Map<String, Object>>any());

        // Start the test.
        sequence.execute(listener);
    }

    @Test
    public void sequentialRequests() {
        final CallSequence sequence = createCallSequence(vertx)
                .addCall(IncreaseCallHandler.class)
                .addCall(IncreaseCallHandler.class)
                .build();
        sequence.setContextVar("number", 1);

        final Integer expectedOutcome = 3;

        final ResponseListener listener = mock(ResponseListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Map<String, Object> contextVars = (Map<String, Object>) invocationOnMock.getArguments()[0];
                assertThat((Integer) contextVars.get("number"), is(equalTo(expectedOutcome)));

                // Have the test complete when the onCompleted() method on the listener is called.
                testComplete();
                return null;
            }
        }).when(listener).onCompleted(Matchers.<Map<String, Object>>any());

        // Start the test.
        sequence.execute(listener);
    }
}
