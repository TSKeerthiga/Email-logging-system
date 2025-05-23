package com.ideas2it.emailLoggingSystem.exception;

import com.amazonaws.Response;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

public class GlobalExceptionHandler {

    /**
     * Handles validation errors by extracting the error messages from the FieldErrors
     * in the MethodArgumentNotValidException and returning a bad request response.
     *
     * @param ex the MethodArgumentNotValidException
     * @return a ResponseEntity containing the validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseResult> handleValidation(MethodArgumentNotValidException ex) {
        List<String> error =  ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String errorMessage = String.join(", ", error);

        return new ResponseEntity<>(new ResponseResult(false, errorMessage), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other types of exceptions by returning an internal server error
     * with a generic error message.
     *
     * @param ex the generic exception
     * @return a ResponseEntity containing a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult> handleException(Exception ex) {
        return new ResponseEntity<>(new ResponseResult(false, "An unexpected error occured" + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
