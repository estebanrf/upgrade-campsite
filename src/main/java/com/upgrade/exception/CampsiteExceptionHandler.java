package com.upgrade.exception;

import javax.ws.rs.core.Response;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class CampsiteExceptionHandler {

	@ExceptionHandler({ Exception.class })
	public ResponseEntity handleException(Exception e) {

		if (e instanceof IllegalStateException) {
			return ResponseEntity.status(Response.Status.CONFLICT.getStatusCode()).body(e.getMessage());
		}

		if (e instanceof IllegalArgumentException) {
			return ResponseEntity.status(Response.Status.BAD_REQUEST.getStatusCode()).body(e.getMessage());
		}

		if (e instanceof HttpMessageNotReadableException) {
			return ResponseEntity.status(Response.Status.BAD_REQUEST.getStatusCode()).body("Request payload can not be null.");
		}

		if (e instanceof MethodArgumentNotValidException) {
			return ResponseEntity.status(Response.Status.BAD_REQUEST.getStatusCode()).body("There's at least one null field in request payload.");
		}
		return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).body(e.getMessage());

	}
}
