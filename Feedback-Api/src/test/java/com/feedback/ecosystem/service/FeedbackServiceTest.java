package com.feedback.ecosystem.service;

import com.feedback.ecosystem.model.Feedback;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@QuarkusTest
class FeedbackServiceTest {

    @Inject
    FeedbackService feedbackService;

    @InjectMock
    DynamoDbClient dynamoDbClient;

    @InjectMock
    SnsClient snsClient;

    @Test
    void testSaveFeedback_Critical() {
        Feedback feedback = new Feedback();
        feedback.setUsuario("testuser");
        feedback.setComentario("terrible service");
        feedback.setNota(1);

        feedbackService.saveFeedback(feedback);

        ArgumentCaptor<PutItemRequest> dynamoCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(dynamoCaptor.capture());
        PutItemRequest capturedDynamoRequest = dynamoCaptor.getValue();
        assertEquals("feedbacks", capturedDynamoRequest.tableName());
        assertTrue(capturedDynamoRequest.item().get("ehCritico").bool());

        ArgumentCaptor<PublishRequest> snsCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(snsCaptor.capture());
        PublishRequest capturedSnsRequest = snsCaptor.getValue();
        assertTrue(capturedSnsRequest.message().contains("terrible service"));
    }

    @Test
    void testSaveFeedback_NotCritical() {
        Feedback feedback = new Feedback();
        feedback.setUsuario("testuser");
        feedback.setComentario("good service");
        feedback.setNota(5);

        feedbackService.saveFeedback(feedback);

        ArgumentCaptor<PutItemRequest> dynamoCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(dynamoCaptor.capture());
        PutItemRequest capturedDynamoRequest = dynamoCaptor.getValue();
        assertEquals("feedbacks", capturedDynamoRequest.tableName());
        assertEquals(false, capturedDynamoRequest.item().get("ehCritico").bool());

        verify(snsClient, times(0)).publish(ArgumentCaptor.forClass(PublishRequest.class).capture());
    }
}
