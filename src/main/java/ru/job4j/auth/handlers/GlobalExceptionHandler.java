package ru.job4j.auth.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            GlobalExceptionHandler.class.getSimpleName());

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(value = {NullPointerException.class})
    public void handleNullPointerException(Exception e,
                                           HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        Map<String, String> source = new HashMap<>();
        source.put("message", "Some of fields empty");
        source.put("details", e.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(source));
        LOGGER.error(e.getMessage());
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public void handleNotValidException(MethodArgumentNotValidException e,
                                        HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        Map<String, String> source = new HashMap<>();
        for (FieldError err: e.getFieldErrors()) {
            source.put(err.getField(), String.format("%s. Actual value: %s",
                    err.getDefaultMessage(), err.getRejectedValue()));
        }
        response.getWriter().write(objectMapper.writeValueAsString(source));
        LOGGER.error(e.getMessage());
    }
}
