package com.feedback.ecosystem.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

public class NotificationHandler implements RequestHandler<SNSEvent, Void> {

    private final SesClient sesClient = SesClient.builder().build();
    private final String fromEmail = "noreply@yourdomain.com"; // Replace with your SES verified email
    private final String toEmail = "admin@yourdomain.com"; // Replace with admin email

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            String message = record.getSNS().getMessage();
            context.getLogger().log("Received message: " + message);

            sendEmail(message);
        }
        return null;
    }

    private void sendEmail(String message) {
        Destination destination = Destination.builder()
                .toAddresses(toEmail)
                .build();

        Content subject = Content.builder()
                .data("Critical Feedback Received")
                .build();

        Body body = Body.builder()
                .text(Content.builder().data("A new critical feedback has been received:\n\n" + message).build())
                .build();

        Message sesMessage = Message.builder()
                .subject(subject)
                .body(body)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(destination)
                .message(sesMessage)
                .build();

        sesClient.sendEmail(request);
    }
}
