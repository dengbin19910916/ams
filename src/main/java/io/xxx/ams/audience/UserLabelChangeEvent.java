package io.xxx.ams.audience;

/**
 * 上游写入用户标签到 Redis 后发布的变更事件。
 * AMS 消费该事件失效本地 Caffeine 缓存。
 *
 * @param userId 发生变更的用户 ID
 */
public record UserLabelChangeEvent(long userId) {
}
