package de.tudresden.inf.verdatas.xapitools.datasim.validators;

import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimAlignment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator to check that a given {@link DatasimAlignment} has a valid weight.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public class AlignmentWeightValidator implements ConstraintValidator<AlignmentWeight, Float> {
    @Override
    public void initialize(AlignmentWeight constraintAnnotation) {
        // No fields to map
    }

    /**
     * An Alignment's weight is valid if and only if the weight w is in the compact interval [-1,+1].
     */
    @Override
    public boolean isValid(Float value, ConstraintValidatorContext constraintValidatorContext) {
        return Math.abs(value) <= 1;
    }
}
