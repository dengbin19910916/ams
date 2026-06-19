package io.xxx.ams.common;

public interface RedisKeys {

    interface Label {

        static String userLabels(Long userId) {
            return "audience:labels:" + userId;
        }
    }
}
