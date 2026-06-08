package com.example.template.exception;

import lombok.Value;

@Value
public class FieldError {
    String field;
    String message;
}
