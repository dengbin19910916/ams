package io.xxx.ams.audience;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.xxx.ams.common.RedisKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class UserTagCache {

    private final StringRedisTemplate redis;

    private final Cache<Long, UserTags> l1 = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public UserTagCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public UserTags load(long userId) {
        return l1.get(userId, k -> {
            Map<Object, Object> raw = redis.opsForHash().entries(RedisKeys.Tag.userTag(userId));
            return parseUserTags(raw);
        });
    }

    public void invalidate(long userId) {
        l1.invalidate(userId);
    }

    static UserTags parseUserTags(Map<Object, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return UserTags.empty();
        }
        Map<String, Set<String>> tags = new HashMap<>(raw.size());
        for (Map.Entry<Object, Object> e : raw.entrySet()) {
            String tagKey = String.valueOf(e.getKey());
            List<String> values = JSON.parseArray(String.valueOf(e.getValue()), String.class);
            tags.put(tagKey, values == null || values.isEmpty() ? Set.of() : Set.copyOf(values));
        }
        return new UserTags(Map.copyOf(tags));
    }
}
