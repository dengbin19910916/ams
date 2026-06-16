package io.xxx.ams.common;

public interface RedisKeys {

    interface Tag {

        static String userTag(Long userId) {
            return "audience:tags:" + userId;
        }
    }
}
