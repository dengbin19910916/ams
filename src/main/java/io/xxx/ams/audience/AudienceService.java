package io.xxx.ams.audience;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.stub.StreamObserver;
import io.xxx.ams.grpc.AudienceBatchMatchRequest;
import io.xxx.ams.grpc.AudienceBatchMatchResponse;
import io.xxx.ams.grpc.AudienceMatchResponse;
import io.xxx.ams.grpc.AudienceServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class AudienceService extends AudienceServiceGrpc.AudienceServiceImplBase {

    private final UserTagCache userTagCache;

    @Override
    public void match(io.xxx.ams.grpc.AudienceMatchRequest request, StreamObserver<AudienceMatchResponse> responseObserver) {
        long userId = request.getUserId();
        Struct tagValues = request.getTagValues();
        UserTags tags = userTagCache.load(userId);
        boolean matched = matchTags(tags, tagValues);

        AudienceMatchResponse response = AudienceMatchResponse.newBuilder()
                .setMatched(matched)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void batchMatch(AudienceBatchMatchRequest request, StreamObserver<AudienceBatchMatchResponse> responseObserver) {
        long userId = request.getUserId();
        Map<String, Struct> groupTagValuesMap = request.getGroupTagValuesMap();

        UserTags tags = userTagCache.load(userId);
        Map<String, Boolean> results = groupTagValuesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> matchTags(tags, entry.getValue())
                ));

        AudienceBatchMatchResponse response = io.xxx.ams.grpc.AudienceBatchMatchResponse.newBuilder()
                .putAllResults(results)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    static boolean matchTags(UserTags userTags, Struct tagValues) {
        if (tagValues.getFieldsCount() == 0) {
            return true;
        }
        if (userTags.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, Value> entry : tagValues.getFieldsMap().entrySet()) {
            Set<String> requiredValues = extractValues(entry.getValue());
            if (requiredValues.isEmpty()) {
                continue;
            }
            Set<String> userValues = userTags.tags().get(entry.getKey());
            if (userValues == null || Collections.disjoint(userValues, requiredValues)) {
                return false;
            }
        }
        return true;
    }

    static Set<String> extractValues(Value value) {
        if (value.getKindCase() == Value.KindCase.LIST_VALUE) {
            return value.getListValue().getValuesList().stream()
                    .map(AudienceService::valueToString)
                    .collect(Collectors.toSet());
        }
        return Set.of(valueToString(value));
    }

    private static String valueToString(Value value) {
        return switch (value.getKindCase()) {
            case STRING_VALUE -> value.getStringValue();
            case NUMBER_VALUE -> String.valueOf(value.getNumberValue());
            case BOOL_VALUE -> String.valueOf(value.getBoolValue());
            default -> value.toString();
        };
    }
}
