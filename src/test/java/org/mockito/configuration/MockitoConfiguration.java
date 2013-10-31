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
package org.mockito.configuration;

/**
 * This class is added to override the default configuration of Mockito, by disabling the Objenesis cache which causes ClassLoader issues in the test.
 */
public class MockitoConfiguration extends DefaultMockitoConfiguration {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enableClassCache() {
        return false;
    }
}
