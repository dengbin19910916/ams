package io.xxx.ams.audience;

import com.alibaba.fastjson2.JSON;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.stub.StreamObserver;
import io.xxx.ams.common.RedisKeys;
import io.xxx.cms.grpc.AudienceBatchMatchRequest;
import io.xxx.cms.grpc.AudienceBatchMatchResponse;
import io.xxx.cms.grpc.AudienceMatchRequest;
import io.xxx.cms.grpc.AudienceMatchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class AudienceService extends io.xxx.cms.grpc.AudienceServiceGrpc.AudienceServiceImplBase {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void match(AudienceMatchRequest request, StreamObserver<AudienceMatchResponse> responseObserver) {
        long userId = request.getUserId();
        Struct tagValues = request.getTagValues();
        boolean matched = matchTags(userId, tagValues);

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

        Map<String, Boolean> results = groupTagValuesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> matchTags(userId, entry.getValue())
                ));

        AudienceBatchMatchResponse response = AudienceBatchMatchResponse.newBuilder()
                .putAllResults(results)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private boolean matchTags(long userId, Struct tagValues) {
        return ThreadLocalRandom.current().nextInt(3) % 2 == 0;
//        if (tagValues.getFieldsCount() == 0) {
//            return true;
//        }
//
//        String redisKey = RedisKeys.Tag.userTag(userId);
//        Map<Object, Object> userTags = redisTemplate.opsForHash().entries(redisKey);
//        if (userTags.isEmpty()) {
//            return false;
//        }
//
//        for (Map.Entry<String, Value> entry : tagValues.getFieldsMap().entrySet()) {
//            String tagKey = entry.getKey();
//            Set<String> requiredValues = extractValues(entry.getValue());
//            if (requiredValues.isEmpty()) {
//                continue;
//            }
//
//            Object raw = userTags.get(tagKey);
//            if (raw == null) {
//                return false;
//            }
//
//            List<String> userTagList = JSON.parseArray(raw.toString(), String.class);
//            if (userTagList == null || Collections.disjoint(userTagList, requiredValues)) {
//                return false;
//            }
//        }
//        return true;
    }

    private Set<String> extractValues(Value value) {
        if (value.getKindCase() == Value.KindCase.LIST_VALUE) {
            return value.getListValue().getValuesList().stream()
                    .map(this::valueToString)
                    .collect(Collectors.toSet());
        }
        return Set.of(valueToString(value));
    }

    private String valueToString(Value value) {
        return switch (value.getKindCase()) {
            case STRING_VALUE -> value.getStringValue();
            case NUMBER_VALUE -> String.valueOf(value.getNumberValue());
            case BOOL_VALUE -> String.valueOf(value.getBoolValue());
            default -> value.toString();
        };
    }
}
