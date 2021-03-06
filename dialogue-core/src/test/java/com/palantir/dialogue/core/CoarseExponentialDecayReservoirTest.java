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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class CoarseExponentialDecayReservoirTest {

    @Test
    void testDecay_toZero() {
        AtomicLong clock = new AtomicLong();
        CoarseExponentialDecayReservoir decay = new CoarseExponentialDecayReservoir(clock::get, Duration.ofNanos(10));
        assertThat(decay.get()).isZero();
        decay.update(1);
        assertThat(decay.get()).isOne();
        clock.set(300000);
        assertThat(decay.get()).isZero();
    }

    @Test
    void testDecay_byHalf() {
        AtomicLong clock = new AtomicLong();
        CoarseExponentialDecayReservoir decay = new CoarseExponentialDecayReservoir(clock::get, Duration.ofNanos(10));
        decay.update(2);
        assertThat(decay.get()).isEqualTo(2);
        clock.set(10);
        assertThat(decay.get()).isEqualTo(1.0, Offset.offset(.001));
        clock.set(20);
        assertThat(decay.get()).isEqualTo(0.5, Offset.offset(.001));
    }

    @Test
    void testDecay_toZero_intervalsWithoutInteraction() {
        AtomicLong clock = new AtomicLong();
        CoarseExponentialDecayReservoir decay = new CoarseExponentialDecayReservoir(clock::get, Duration.ofNanos(10));
        decay.update(2);
        assertThat(decay.get()).isEqualTo(2);
        clock.set(300000);
        assertThat(decay.get()).isZero();
    }

    @Test
    void testDecay_intermediateDecay() {
        AtomicLong clock = new AtomicLong();
        CoarseExponentialDecayReservoir decay = new CoarseExponentialDecayReservoir(clock::get, Duration.ofNanos(10));
        decay.update(100);
        assertThat(decay.get()).isEqualTo(100);
        clock.set(2);
        assertThat(decay.get())
                .as("Expected partial decay after a slice of the half life")
                .isLessThan(100)
                .isGreaterThan(50);
        clock.set(10);
        assertThat(decay.get()).isEqualTo(50, Offset.offset(.001));
    }
}
