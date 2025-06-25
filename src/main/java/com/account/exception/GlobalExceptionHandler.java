package com.account.exception;

import com.account.dto.ErrorResponse;

import java.nio.file.AccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .code("ACCOUNT_NOT_FOUND")
                        .message("The account ID does not exist")
                        .details(ex.getMessage())
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(AccountNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .code("CUSTOMER_NOT_FOUND")
                        .message("The customer ID does not exist")
                        .details(ex.getMessage())
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(AccountNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .code("INSUFFICIENT_FUNDS")
                        .message("Insufficient Funds")
                        .details(ex.getMessage())
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse>  handleAccessDenied(AccessDeniedException ex) {
    	   ErrorResponse response = ErrorResponse.builder()
                   .error(ErrorResponse.ErrorDetail.builder()
                           .code("FORBIDDEN")
                           .message("Unauthorized")
                           .details(ex.getMessage())
                           .build())
                   .build();

           return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error(ErrorResponse.ErrorDetail.builder()
                        .code("INTERNAL_ERROR")
                        .message("Something went wrong")
                        .details(ex.getMessage())
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
