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
    void matchLabels_emptyLabel_returnsTrue() {
        UserLabels userLabels = new UserLabels(Map.of("gender", Set.of("male")));
        Struct labels = Struct.newBuilder().build();

        assertThat(AudienceService.matchLabels(userLabels, labels)).isTrue();
    }

    @Test
    void matchLabels_emptyUserLabels_returnsFalseWhenRequirementsExist() {
        UserLabels userLabels = UserLabels.empty();
        Struct labels = Struct.newBuilder()
                .putFields("gender", Value.newBuilder().setStringValue("male").build())
                .build();

        assertThat(AudienceService.matchLabels(userLabels, labels)).isFalse();
    }

    @Test
    void matchLabels_userHasAllRequiredLabels_returnsTrue() {
        UserLabels userLabels = new UserLabels(Map.of(
                "gender", Set.of("male"),
                "age", Set.of("18-24", "25-34")
        ));
        Struct labels = Struct.newBuilder()
                .putFields("gender", Value.newBuilder().setStringValue("male").build())
                .putFields("age", Value.newBuilder().setListValue(ListValue.newBuilder()
                        .addValues(Value.newBuilder().setStringValue("18-24").build())
                        .build()).build())
                .build();

        assertThat(AudienceService.matchLabels(userLabels, labels)).isTrue();
    }

    @Test
    void matchLabels_userMissingLabelKey_returnsFalse() {
        UserLabels userLabels = new UserLabels(Map.of("gender", Set.of("male")));
        Struct labels = Struct.newBuilder()
                .putFields("city", Value.newBuilder().setStringValue("beijing").build())
                .build();

        assertThat(AudienceService.matchLabels(userLabels, labels)).isFalse();
    }

    @Test
    void matchLabels_userHasLabelKeyButNotRequiredValue_returnsFalse() {
        UserLabels userLabels = new UserLabels(Map.of("gender", Set.of("female")));
        Struct labels = Struct.newBuilder()
                .putFields("gender", Value.newBuilder().setStringValue("male").build())
                .build();

        assertThat(AudienceService.matchLabels(userLabels, labels)).isFalse();
    }

    @Test
    void matchLabels_skipsLabelKeyWithEmptyRequiredValues() {
        UserLabels userLabels = new UserLabels(Map.of("gender", Set.of("male")));
        Struct labels = Struct.newBuilder()
                .putFields("gender", Value.newBuilder()
                        .setListValue(ListValue.newBuilder().build()).build())
                .build();

        assertThat(AudienceService.matchLabels(userLabels, labels)).isTrue();
    }
}
