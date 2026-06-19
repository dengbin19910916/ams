package io.xxx.ams.audience;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.stub.StreamObserver;
import io.xxx.ams.grpc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class AudienceService extends AudienceServiceGrpc.AudienceServiceImplBase {

    private final UserLabelCache userLabelCache;

    @Override
    public void match(AudienceMatchRequest request, StreamObserver<AudienceMatchResponse> responseObserver) {
        long userId = request.getUserId();
        Struct labels = request.getLabels();
        UserLabels userLabels = userLabelCache.load(userId);
        boolean matched = matchLabels(userLabels, labels);

        AudienceMatchResponse response = AudienceMatchResponse.newBuilder()
                .setMatched(matched)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void batchMatch(AudienceBatchMatchRequest request, StreamObserver<AudienceBatchMatchResponse> responseObserver) {
        long userId = request.getUserId();
        Map<String, Struct> groupLabelsMap = request.getGroupLabelsMap();

        UserLabels userLabels = userLabelCache.load(userId);
        Map<String, Boolean> results = groupLabelsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> matchLabels(userLabels, entry.getValue())
                ));

        AudienceBatchMatchResponse response = io.xxx.ams.grpc.AudienceBatchMatchResponse.newBuilder()
                .putAllResults(results)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    static boolean matchLabels(UserLabels userLabels, Struct labels) {
        if (labels.getFieldsCount() == 0) {
            return true;
        }
        if (userLabels.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, Value> entry : labels.getFieldsMap().entrySet()) {
            Set<String> requiredValues = extractValues(entry.getValue());
            if (requiredValues.isEmpty()) {
                continue;
            }
            Set<String> userValues = userLabels.labels().get(entry.getKey());
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
