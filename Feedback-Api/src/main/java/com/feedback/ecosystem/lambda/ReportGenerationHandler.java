package com.feedback.ecosystem.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ReportGenerationHandler implements RequestHandler<Object, Void> {

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
    private final S3Client s3Client = S3Client.builder().build();
    private final String tableName = "feedbacks";
    private final String bucketName = "feedback-reports-bucket"; // Replace with your bucket name

    @Override
    public Void handleRequest(Object event, Context context) {
        long oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli();

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("timestamp >= :oneWeekAgo")
                .expressionAttributeValues(java.util.Collections.singletonMap(":oneWeekAgo", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().n(String.valueOf(oneWeekAgo)).build()))
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);

        // Simple CSV generation
        StringBuilder csv = new StringBuilder("id,usuario,comentario,nota,timestamp,ehCritico\n");
        response.items().forEach(item -> {
            csv.append(item.get("id").s()).append(",")
                    .append(item.get("usuario").s()).append(",")
                    .append(item.get("comentario").s()).append(",")
                    .append(item.get("nota").n()).append(",")
                    .append(item.get("timestamp").n()).append(",")
                    .append(item.get("ehCritico").bool()).append("\n");
        });

        String reportKey = "weekly-report-" + Instant.now().toString() + ".csv";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(reportKey)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(csv.toString()));

        return null;
    }
}
