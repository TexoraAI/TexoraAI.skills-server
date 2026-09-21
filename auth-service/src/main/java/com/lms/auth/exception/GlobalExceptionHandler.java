//package com.lms.auth.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
//        return ResponseEntity
//                .status(HttpStatus.UNAUTHORIZED)
//                .body(Map.of(
//                        "message", ex.getMessage()
//                ));
//    }
//}
package com.lms.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SeatLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleSeatLimitExceeded(SeatLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "SEAT_LIMIT_REACHED");
        body.put("orgId", ex.getOrgId());
        body.put("limitType", ex.getLimitType());
        body.put("currentCount", ex.getCurrentCount());
        body.put("maxAllowed", ex.getMaxAllowed());
        body.put("message", "Seat limit reached. Upgrade required.");

        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message", ex.getMessage()
                ));
    }
}