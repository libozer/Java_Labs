package com.postcode.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice // автоматический контролирует все ошибки
public class ExceptionApiHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionApiHandler.class);

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class, EntityNotFoundException.class}) // обробатывает ошибки в скобках
    public ErrorMessage handleInternalServerError(RuntimeException ex) {
        LOG.error("ERROR, 500 CODE");
        return new ErrorMessage(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({HttpClientErrorException.class, HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class, MissingServletRequestParameterException.class,
            ConstraintViolationException.class})
    public ErrorMessage handleBadRequestException(Exception ex) {
        LOG.error("ERROR, 400 CODE");
        return new ErrorMessage("400 ERROR, BAD REQUEST");
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorMessage handleMethodNotAllowed(Exception ex) {
        LOG.error("ERROR, 405 CODE");
        return new ErrorMessage("405 ERROR, METHOD NOT ALLOWED");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ErrorMessage handlerFoundException(Exception ex) {
        LOG.error("ERROR, 404 CODE");
        return new ErrorMessage("404 ERROR, NOT FOUND");
    }

}