package io.xxx.ams.audience;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLabelCacheTests {

    @Mock
    StringRedisTemplate redis;

    @Mock
    HashOperations<String, Object, Object> hashOps;

    @Test
    void parseUserLabels_emptyMap_returnsEmpty() {
        assertThat(UserLabelCache.parseUserLabels(Map.of())).isEqualTo(UserLabels.empty());
    }

    @Test
    void parseUserLabels_parsesJsonArraysToSets() {
        Map<String, String> raw = Map.of(
                "gender", "[\"male\"]",
                "age", "[\"18-24\",\"25-34\"]"
        );

        UserLabels result = UserLabelCache.parseUserLabels(raw);

        assertThat(result.labels()).containsEntry("gender", Set.of("male"));
        assertThat(result.labels()).containsEntry("age", Set.of("18-24", "25-34"));
    }

    @Test
    void load_l1Miss_loadsFromRedisAndParses() {
        UserLabelCache cache = new UserLabelCache(redis);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries("audience:labels:123")).thenReturn(Map.<Object, Object>of("gender", "[\"male\"]"));

        UserLabels result = cache.load(123);

        assertThat(result.labels()).containsEntry("gender", Set.of("male"));
    }

    @Test
    void load_l1Hit_doesNotHitRedis() {
        UserLabelCache cache = new UserLabelCache(redis);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries("audience:labels:123")).thenReturn(Map.<Object, Object>of("gender", "[\"male\"]"));

        cache.load(123);
        cache.load(123);

        verify(hashOps, times(1)).entries("audience:labels:123");
    }

    @Test
    void load_emptyRedisResult_cachedForPenetrationProtection() {
        UserLabelCache cache = new UserLabelCache(redis);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries("audience:labels:999")).thenReturn(Map.of());

        cache.load(999);
        cache.load(999);

        verify(hashOps, times(1)).entries("audience:labels:999");
    }
}
