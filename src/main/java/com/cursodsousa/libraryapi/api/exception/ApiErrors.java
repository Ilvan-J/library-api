package com.cursodsousa.libraryapi.api.exception;

import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

public class ApiErrors {

    private List<String> errors;
    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach( error -> this.errors.add(error.getDefaultMessage()) );
    }

    public List<String> getErrors() {
        return errors;
    }
}
