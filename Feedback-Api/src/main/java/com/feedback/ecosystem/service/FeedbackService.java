package com.feedback.ecosystem.service;

import com.feedback.ecosystem.model.Feedback;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FeedbackService {

    @Inject
    DynamoDbClient dynamoDbClient;

    @Inject
    SnsClient snsClient;

    private final String tableName = "feedbacks";
    private final String topicArn = "arn:aws:sns:us-east-1:123456789012:critical-feedbacks"; // Replace with your topic ARN

    public void saveFeedback(Feedback feedback) {
        feedback.setId(UUID.randomUUID().toString());
        feedback.setTimestamp(System.currentTimeMillis());
        feedback.setEhCritico(feedback.getNota() < 3);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(feedback.getId()).build());
        item.put("usuario", AttributeValue.builder().s(feedback.getUsuario()).build());
        item.put("comentario", AttributeValue.builder().s(feedback.getComentario()).build());
        item.put("nota", AttributeValue.builder().n(String.valueOf(feedback.getNota())).build());
        item.put("timestamp", AttributeValue.builder().n(String.valueOf(feedback.getTimestamp())).build());
        item.put("ehCritico", AttributeValue.builder().bool(feedback.isEhCritico()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        dynamoDbClient.putItem(request);

        if (feedback.isEhCritico()) {
            publishToSns(feedback);
        }
    }

    private void publishToSns(Feedback feedback) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message("New critical feedback received: " + feedback.toString())
                .build();
        snsClient.publish(request);
    }
}
