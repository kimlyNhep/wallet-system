package com.wlt.wallet.exception;

import com.wlt.wallet.dto.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<SuccessResponse<?>> handleCustomException(CustomException ex) {
        SuccessResponse<Object> successResponse = new SuccessResponse<>();
        successResponse.setCode(ex.getErrorCode());
        successResponse.setMessage(ex.getErrorMessage());
        successResponse.setCorrelationId(UUID.randomUUID().toString());


        HttpStatus status = HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(successResponse, status);
    }

}
