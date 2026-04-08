package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException to HTTP 409 Conflict.
 * Triggered when attempting to delete a room that still has sensors assigned.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Conflict");
        error.put("status", 409);
        error.put("message", exception.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
