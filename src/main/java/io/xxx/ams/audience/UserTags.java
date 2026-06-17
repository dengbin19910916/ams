package io.xxx.ams.audience;

import java.util.Map;
import java.util.Set;

/**
 * 用户标签的内存表示。Redis 中以 JSON 数组字符串存储，加载到 L1 缓存时解析为该结构。
 *
 * @param tags tagKey -> 标签值集合（不可变）
 */
public record UserTags(Map<String, Set<String>> tags) {

    public static UserTags empty() {
        return new UserTags(Map.of());
    }

    public boolean isEmpty() {
        return tags == null || tags.isEmpty();
    }
}
