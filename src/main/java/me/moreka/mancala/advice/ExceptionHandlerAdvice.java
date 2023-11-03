package me.moreka.mancala.advice;

import me.moreka.mancala.dto.Result;
import lombok.extern.log4j.Log4j2;
import me.moreka.mancala.exception.InvalidMoveException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Log4j2
class ExceptionHandlerAdvice {

    @ResponseBody
    @ExceptionHandler(InvalidMoveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result<Object> invalidMove(InvalidMoveException e) {
        return new Result<>(false, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result<Object> generalError(Exception e) {
        log.error("error: ", e);
        return new Result<>(false, e.getMessage());
    }
}