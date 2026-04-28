package com.feedback.ecosystem.resource;

import com.feedback.ecosystem.model.Feedback;
import com.feedback.ecosystem.service.FeedbackService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/feedback")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackResource {

    @Inject
    FeedbackService feedbackService;

    @POST
    @Path("/avaliacao")
    @RolesAllowed({"USER", "ADMIN"})
    public Response newFeedback(Feedback feedback) {
        feedbackService.saveFeedback(feedback);
        return Response.status(Response.Status.CREATED).entity(feedback).build();
    }

    @GET
    @Path("/relatorios/semanal")
    @RolesAllowed("ADMIN")
    public Response getWeeklyReport() {
        // Logic to get the report from S3 will be implemented in the service
        return Response.ok().build();
    }
}
