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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTagCacheTests {

    @Mock
    StringRedisTemplate redis;

    @Mock
    HashOperations<String, Object, Object> hashOps;

    @Test
    void parseUserTags_emptyMap_returnsEmpty() {
        assertThat(UserTagCache.parseUserTags(Map.of())).isEqualTo(UserTags.empty());
    }

    @Test
    void parseUserTags_parsesJsonArraysToSets() {
        Map<Object, Object> raw = Map.<Object, Object>of(
                "gender", "[\"male\"]",
                "age", "[\"18-24\",\"25-34\"]"
        );

        UserTags result = UserTagCache.parseUserTags(raw);

        assertThat(result.tags()).containsEntry("gender", Set.of("male"));
        assertThat(result.tags()).containsEntry("age", Set.of("18-24", "25-34"));
    }

    @Test
    void load_l1Miss_loadsFromRedisAndParses() {
        UserTagCache cache = new UserTagCache(redis);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries("audience:tags:123")).thenReturn(Map.<Object, Object>of("gender", "[\"male\"]"));

        UserTags result = cache.load(123);

        assertThat(result.tags()).containsEntry("gender", Set.of("male"));
    }

    @Test
    void load_l1Hit_doesNotHitRedis() {
        UserTagCache cache = new UserTagCache(redis);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries("audience:tags:123")).thenReturn(Map.<Object, Object>of("gender", "[\"male\"]"));

        cache.load(123);
        cache.load(123);

        verify(hashOps, times(1)).entries("audience:tags:123");
    }

    @Test
    void load_emptyRedisResult_cachedForPenetrationProtection() {
        UserTagCache cache = new UserTagCache(redis);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries("audience:tags:999")).thenReturn(Map.of());

        cache.load(999);
        cache.load(999);

        verify(hashOps, times(1)).entries("audience:tags:999");
    }
}
