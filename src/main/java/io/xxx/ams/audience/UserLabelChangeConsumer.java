package io.xxx.ams.audience;

import io.xxx.ams.common.Topics;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

@Component
public class UserLabelChangeConsumer {

    private final UserLabelCache userLabelCache;

    public UserLabelChangeConsumer(UserLabelCache userLabelCache) {
        this.userLabelCache = userLabelCache;
    }

    @PulsarListener(
            topics = Topics.Label.CHANGE_TOPIC,
            subscriptionName = "${ams.audience.lavel-invalidate.subscription-name}",
            schemaType = SchemaType.JSON
    )
    public void onUserLabelChange(UserLabelChangeEvent event) {
        userLabelCache.invalidate(event.userId());
    }
}
