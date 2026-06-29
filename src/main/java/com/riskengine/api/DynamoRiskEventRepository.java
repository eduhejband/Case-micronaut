package com.riskengine.api;

import com.riskengine.domain.enums.EventType;
import com.riskengine.domain.enums.RiskDecision;
import com.riskengine.domain.enums.RiskReason;
import com.riskengine.domain.model.RiskEvent;
import com.riskengine.domain.model.RiskResult;
import com.riskengine.domain.repository.RiskEventRepository;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@Primary
public class DynamoRiskEventRepository implements RiskEventRepository {

    private static final String CUSTOMER_INDEX = "customerId-processedAt-index";

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoRiskEventRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${riskengine.dynamodb.table-name}") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public RiskResult save(RiskEvent event, RiskResult result) {
        Map<String, AttributeValue> item = Map.of(
                "eventId", AttributeValue.fromS(result.eventId()),
                "customerId", AttributeValue.fromS(result.customerId()),
                "eventType", AttributeValue.fromS(result.eventType().name()),
                "riskScore", AttributeValue.fromN(String.valueOf(result.riskScore())),
                "decision", AttributeValue.fromS(result.decision().name()),
                "reasons", AttributeValue.fromS(joinReasons(result.reasons())),
                "processedAt", AttributeValue.fromS(result.processedAt().toString())
        );

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);

        return result;
    }

    @Override
    public Optional<RiskResult> findById(String eventId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "eventId", AttributeValue.fromS(eventId)
                ))
                .build();

        Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toRiskResult(item));
    }

    @Override
    public List<RiskResult> findByCustomerId(String customerId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(CUSTOMER_INDEX)
                .keyConditionExpression("customerId = :customerId")
                .expressionAttributeValues(Map.of(
                        ":customerId", AttributeValue.fromS(customerId)
                ))
                .scanIndexForward(false)
                .build();

        return dynamoDbClient.query(request)
                .items()
                .stream()
                .map(this::toRiskResult)
                .toList();
    }

    private RiskResult toRiskResult(Map<String, AttributeValue> item) {
        return new RiskResult(
                item.get("eventId").s(),
                item.get("customerId").s(),
                EventType.valueOf(item.get("eventType").s()),
                Integer.parseInt(item.get("riskScore").n()),
                RiskDecision.valueOf(item.get("decision").s()),
                parseReasons(item.get("reasons").s()),
                Instant.parse(item.get("processedAt").s())
        );
    }

    private String joinReasons(List<RiskReason> reasons) {
        return reasons.stream()
                .map(Enum::name)
                .reduce((first, second) -> first + "," + second)
                .orElse("");
    }

    private List<RiskReason> parseReasons(String reasons) {
        if (reasons == null || reasons.isBlank()) {
            return List.of();
        }

        return Arrays.stream(reasons.split(","))
                .map(RiskReason::valueOf)
                .toList();
    }
}