package io.xxx.ams.audience;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserLabelChangeConsumerTests {

    @Mock
    UserLabelCache userLabelCache;

    @Test
    void onUserLabelChange_invalidatesCacheForUserId() {
        UserLabelChangeConsumer consumer = new UserLabelChangeConsumer(userLabelCache);

        consumer.onUserLabelChange(new UserLabelChangeEvent(123L));

        verify(userLabelCache).invalidate(123L);
    }

    @Test
    void onUserLabelChange_differentUserId_invalidatesCorrectId() {
        UserLabelChangeConsumer consumer = new UserLabelChangeConsumer(userLabelCache);

        consumer.onUserLabelChange(new UserLabelChangeEvent(999L));

        verify(userLabelCache).invalidate(999L);
    }
}
