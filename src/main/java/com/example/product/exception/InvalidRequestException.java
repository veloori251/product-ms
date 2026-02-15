package com.example.product.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidRequestException extends RuntimeException{

    private List<FieldValidationError> errors;

    public InvalidRequestException(String message,List<FieldValidationError> errors){
        super(message);
        this.errors = errors;
    }

}
