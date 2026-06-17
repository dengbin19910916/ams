package io.xxx.ams.audience;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceServiceTests {

    @Test
    void matchTags_emptyTagValues_returnsTrue() {
        UserTags userTags = new UserTags(Map.of("gender", Set.of("male")));
        Struct tagValues = Struct.newBuilder().build();

        assertThat(AudienceService.matchTags(userTags, tagValues)).isTrue();
    }

    @Test
    void matchTags_emptyUserTags_returnsFalseWhenRequirementsExist() {
        UserTags userTags = UserTags.empty();
        Struct tagValues = Struct.newBuilder()
                .putFields("gender", Value.newBuilder().setStringValue("male").build())
                .build();

        assertThat(AudienceService.matchTags(userTags, tagValues)).isFalse();
    }

    @Test
    void matchTags_userHasAllRequiredTags_returnsTrue() {
        UserTags userTags = new UserTags(Map.of(
                "gender", Set.of("male"),
                "age", Set.of("18-24", "25-34")
        ));
        Struct tagValues = Struct.newBuilder()
                .putFields("gender", Value.newBuilder().setStringValue("male").build())
                .putFields("age", Value.newBuilder().setListValue(ListValue.newBuilder()
                        .addValues(Value.newBuilder().setStringValue("18-24").build())
                        .build()).build())
                .build();

        assertThat(AudienceService.matchTags(userTags, tagValues)).isTrue();
    }

    @Test
    void matchTags_userMissingTagKey_returnsFalse() {
        UserTags userTags = new UserTags(Map.of("gender", Set.of("male")));
        Struct tagValues = Struct.newBuilder()
                .putFields("city", Value.newBuilder().setStringValue("beijing").build())
                .build();

        assertThat(AudienceService.matchTags(userTags, tagValues)).isFalse();
    }

    @Test
    void matchTags_userHasTagKeyButNotRequiredValue_returnsFalse() {
        UserTags userTags = new UserTags(Map.of("gender", Set.of("female")));
        Struct tagValues = Struct.newBuilder()
                .putFields("gender", Value.newBuilder().setStringValue("male").build())
                .build();

        assertThat(AudienceService.matchTags(userTags, tagValues)).isFalse();
    }

    @Test
    void matchTags_skipsTagKeyWithEmptyRequiredValues() {
        UserTags userTags = new UserTags(Map.of("gender", Set.of("male")));
        Struct tagValues = Struct.newBuilder()
                .putFields("gender", Value.newBuilder()
                        .setListValue(ListValue.newBuilder().build()).build())
                .build();

        assertThat(AudienceService.matchTags(userTags, tagValues)).isTrue();
    }
}
