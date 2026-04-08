package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "catch-all" exception mapper.
 * Intercepts any unexpected runtime errors (e.g., NullPointerException,
 * IndexOutOfBoundsException)
 * and returns a generic HTTP 500 Internal Server Error without exposing stack
 * traces.
 * JAX-RS built-in exceptions (like NotFoundException) are passed through with
 * their original status.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // If this is a JAX-RS WebApplicationException (e.g., NotFoundException,
        // BadRequestException),
        // preserve its original status code and return a clean JSON error
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            int statusCode = webEx.getResponse().getStatus();

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", webEx.getResponse().getStatusInfo().getReasonPhrase());
            error.put("status", statusCode);
            error.put("message", exception.getMessage());

            return Response.status(statusCode)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Log the full stack trace internally for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by GenericExceptionMapper", exception);

        // Return a safe, generic error response to the client (no stack trace exposure)
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Internal Server Error");
        error.put("status", 500);
        error.put("message", "An unexpected error occurred. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
