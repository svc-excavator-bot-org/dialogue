/*
 * (c) Copyright 2020 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.dialogue.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.palantir.dialogue.Channel;
import com.palantir.dialogue.Endpoint;
import com.palantir.dialogue.Request;
import com.palantir.dialogue.Response;
import com.palantir.logsafe.Preconditions;
import java.util.Optional;

/** Adapter from {@link Channel} to {@link LimitedChannel} which always returns a {@link Optional#isPresent() value}. */
final class ChannelToLimitedChannelAdapter implements LimitedChannel {

    private final Channel delegate;

    ChannelToLimitedChannelAdapter(Channel delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "Channel");
    }

    @Override
    public Optional<ListenableFuture<Response>> maybeExecute(Endpoint endpoint, Request request) {
        return Optional.of(delegate.execute(endpoint, request));
    }

    @Override
    public String toString() {
        return "ChannelToLimitedChannelAdapter{" + delegate + '}';
    }
}
