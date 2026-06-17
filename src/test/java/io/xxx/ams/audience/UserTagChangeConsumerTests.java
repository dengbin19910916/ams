package io.xxx.ams.audience;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserTagChangeConsumerTests {

    @Mock
    UserTagCache userTagCache;

    @Test
    void onUserTagChange_invalidatesCacheForUserId() {
        UserTagChangeConsumer consumer = new UserTagChangeConsumer(userTagCache);

        consumer.onUserTagChange(new UserTagChangeEvent(123L));

        verify(userTagCache).invalidate(123L);
    }

    @Test
    void onUserTagChange_differentUserId_invalidatesCorrectId() {
        UserTagChangeConsumer consumer = new UserTagChangeConsumer(userTagCache);

        consumer.onUserTagChange(new UserTagChangeEvent(999L));

        verify(userTagCache).invalidate(999L);
    }
}
