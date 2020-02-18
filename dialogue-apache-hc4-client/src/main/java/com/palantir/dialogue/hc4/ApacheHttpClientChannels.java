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
package com.palantir.dialogue.hc4;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.palantir.conjure.java.api.config.service.UserAgent;
import com.palantir.conjure.java.client.config.CipherSuites;
import com.palantir.conjure.java.client.config.ClientConfiguration;
import com.palantir.dialogue.Channel;
import com.palantir.dialogue.blocking.BlockingChannelAdapter;
import com.palantir.dialogue.core.Channels;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

public final class ApacheHttpClientChannels {
    private ApacheHttpClientChannels() {}

    public static Channel create(ClientConfiguration conf, UserAgent baseAgent) {
        Preconditions.checkArgument(
                !conf.fallbackToCommonNameVerification(), "fallback-to-common-name-verification is not supported");
        Preconditions.checkArgument(!conf.meshProxy().isPresent(), "Mesh proxy is not supported");
        Preconditions.checkArgument(
                conf.serverQoS() == ClientConfiguration.ServerQoS.AUTOMATIC_RETRY,
                "Propagating QoS exceptions is not supported");
        Preconditions.checkArgument(!conf.proxyCredentials().isPresent(), "Proxy credentials are not supported");
        long socketTimeoutMillis =
                Math.max(conf.readTimeout().toMillis(), conf.writeTimeout().toMillis());
        int connectTimeout = Ints.checkedCast(conf.connectTimeout().toMillis());
        // TODO(ckozak): close resources?
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(Ints.checkedCast(socketTimeoutMillis))
                        .setConnectTimeout(connectTimeout)
                        // Don't allow clients to block forever waiting on a connection to become available
                        .setConnectionRequestTimeout(connectTimeout)
                        // Match okhttp, disallow redirects
                        .setRedirectsEnabled(false)
                        .setRelativeRedirectsAllowed(false)
                        .build())
                .setDefaultSocketConfig(
                        SocketConfig.custom().setSoKeepAlive(true).build())
                .evictIdleConnections(55, TimeUnit.SECONDS)
                .setMaxConnPerRoute(1000)
                .setMaxConnTotal(Integer.MAX_VALUE)
                // TODO(ckozak): proxy credentials
                .setRoutePlanner(new SystemDefaultRoutePlanner(null, conf.proxy()))
                .setProxyAuthenticationStrategy(ProxyAuthenticationStrategy.INSTANCE)
                .disableAutomaticRetries()
                // Must be disabled otherwise connections are not reused when client certificates are provided
                .disableConnectionState()
                // Match okhttp behavior disabling cookies
                .disableCookieManagement()
                // Dialogue handles content-compression with ContentDecodingChannel
                .disableContentCompression()
                .setSSLSocketFactory(
                        new SSLConnectionSocketFactory(
                                conf.sslSocketFactory(),
                                new String[] {"TLSv1.2"},
                                conf.enableGcmCipherSuites()
                                        ? CipherSuites.allCipherSuites()
                                        : CipherSuites.fastCipherSuites(),
                                new DefaultHostnameVerifier()))
                .build();
        ImmutableList<Channel> channels = conf.uris().stream()
                .map(uri -> BlockingChannelAdapter.of(new ApacheHttpClientBlockingChannel(client, url(uri))))
                .collect(ImmutableList.toImmutableList());

        return Channels.create(channels, baseAgent, conf);
    }

    private static URL url(String uri) {
        try {
            return new URL(uri);
        } catch (MalformedURLException e) {
            throw new SafeIllegalArgumentException("Failed to parse URL", e);
        }
    }
}
