package com.wlt.wallet.exception;

import com.wlt.wallet.dto.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<SuccessResponse<?>> handleCustomException(CustomException ex) {
//        Map<String, Object> errorResponse = new HashMap<>();
        SuccessResponse<Object> successResponse = new SuccessResponse<>();
        successResponse.setCode(ex.getErrorCode());
        successResponse.setMessage(ex.getErrorMessage());

        // You can customize the HTTP status code based on your error type
        HttpStatus status = HttpStatus.BAD_REQUEST;
//        if (ex.getErrorCode() >= 4000) {
//            status = HttpStatus.UNAUTHORIZED;
//        } else if (ex.getErrorCode() >= 3000) {
//            status = HttpStatus.INTERNAL_SERVER_ERROR;
//        } else if (ex.getErrorCode() >= 1000 && ex.getErrorCode() < 2000) {
//            status = HttpStatus.NOT_FOUND;
//        }

        return new ResponseEntity<>(successResponse, status);
    }

}
