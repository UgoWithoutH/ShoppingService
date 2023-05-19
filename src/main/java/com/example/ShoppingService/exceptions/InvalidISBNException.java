package com.example.ShoppingService.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidISBNException extends Exception{
    public InvalidISBNException(String message) {
        super(message);
    }
}
