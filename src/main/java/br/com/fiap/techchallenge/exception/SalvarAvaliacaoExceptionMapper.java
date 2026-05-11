package br.com.fiap.techchallenge.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SalvarAvaliacaoExceptionMapper implements ExceptionMapper<SalvarAvaliacaoException> {

    @Override
    public Response toResponse(SalvarAvaliacaoException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
