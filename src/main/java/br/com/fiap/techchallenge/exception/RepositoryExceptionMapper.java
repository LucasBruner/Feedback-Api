package br.com.fiap.techchallenge.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RepositoryExceptionMapper implements ExceptionMapper<RepositoryException> {

    @Override
    public Response toResponse(RepositoryException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
