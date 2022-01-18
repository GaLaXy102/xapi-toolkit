package de.tudresden.inf.verdatas.xapitools.datasim.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AlignmentWeightValidator implements ConstraintValidator<AlignmentWeight, Float> {
    @Override
    public void initialize(AlignmentWeight constraintAnnotation) {
        // No fields to map
    }

    @Override
    public boolean isValid(Float value, ConstraintValidatorContext constraintValidatorContext) {
        return Math.abs(value) <= 1;
    }
}
