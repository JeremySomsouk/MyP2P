package com.streaming.myp2p.controller;

import com.streaming.myp2p.controller.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class GlobalControllerExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request")
    @ExceptionHandler(BadRequestException.class)
    public void handleBadRequest() {
    }
}