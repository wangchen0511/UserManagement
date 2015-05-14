package com.user.management.services;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.user.management.exceptions.ValidationException;

import java.util.Set;

public abstract class BaseService {

    private Validator validator;

    public BaseService(Validator validator) {
        this.validator = validator;
    }

    protected void validate(Object request) {
        Set<? extends ConstraintViolation<?>> constraintViolations = validator.validate(request);
        if (constraintViolations.size() > 0) {
            System.out.println("validate fialed!!!!!!!!!!!!");
            throw new ValidationException(constraintViolations);
        }
    }

}
