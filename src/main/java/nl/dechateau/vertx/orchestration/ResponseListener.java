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

import java.util.Map;

/**
 * Interface for reporting the outcome of a (sequence of) call(s).
 */
public interface ResponseListener {
    /**
     * Called by a handler when the service call was onCompleted successfully.
     *
     * @param contextVars The variables available in the context after the successful execution.
     */
    void onCompleted(Map<String, Object> contextVars);

    /**
     * Called by a handler when a service call encountered an exceptional situation.
     *
     * @param errorMessage The onError message indicating the reason for failure.
     */
    void onError(final String errorMessage);

    /**
     * Called by a handler when a service call encountered an exceptional situation.
     *
     * @param errorType    The type of the onError that occurred.
     * @param errorMessage The onError message indicating the reason for failure.
     */
    void onError(final ErrorType errorType, final String errorMessage);

    /**
     * Error types, corresponding to the most common problems.
     * <p/>
     * One of the intended uses is to be able to indicate the proper HTTP onError status, when the orchestration is used
     * by a verticle which exposes an HTTP interface.
     */
    enum ErrorType {
        BAD_REQUEST,
        CONTENT_MISSING,
        NOT_ALLOWED,
        TIMEOUT,
        CONFLICT,
        REQUEST_ENTITY_TOO_LARGE
    }
}
