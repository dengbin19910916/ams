package io.xxx.ams.audience;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.xxx.ams.common.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserLabelCache {

    private final StringRedisTemplate redis;

    private final Cache<Long, UserLabels> local = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public UserLabels load(long userId) {
        return local.get(userId, _ -> {
            String redisKey = RedisKeys.Label.userLabels(userId);
            Map<String, String> raw = redis.<String, String>opsForHash().entries(redisKey);
            return parseUserLabels(raw);
        });
    }

    public void invalidate(long userId) {
        local.invalidate(userId);
    }

    static UserLabels parseUserLabels(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) {
            return UserLabels.empty();
        }
        Map<String, Set<String>> labels = new HashMap<>(raw.size());
        for (Map.Entry<String, String> e : raw.entrySet()) {
            String lavelKey = e.getKey();
            List<String> values = JSON.parseArray(e.getValue(), String.class);
            labels.put(lavelKey, values == null || values.isEmpty() ? Set.of() : Set.copyOf(values));
        }
        return new UserLabels(Map.copyOf(labels));
    }
}
