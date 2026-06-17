package io.xxx.ams.audience;

import io.xxx.ams.common.Topics;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

@Component
public class UserTagChangeConsumer {

    private final UserTagCache userTagCache;

    public UserTagChangeConsumer(UserTagCache userTagCache) {
        this.userTagCache = userTagCache;
    }

    @PulsarListener(
            topics = Topics.Tag.CHANGE_TOPIC,
            subscriptionName = "${ams.audience.tag-invalidate.subscription-name}",
            schemaType = SchemaType.JSON
    )
    public void onUserTagChange(UserTagChangeEvent event) {
        userTagCache.invalidate(event.userId());
    }
}
