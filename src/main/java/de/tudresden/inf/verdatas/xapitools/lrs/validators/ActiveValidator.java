package de.tudresden.inf.verdatas.xapitools.lrs.validators;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator to check that a given {@link LrsConnection} is enabled.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public class ActiveValidator implements ConstraintValidator<Active, LrsConnection> {
    @Override
    public void initialize(Active constraintAnnotation) {
        // No fields to map
    }

    /**
     * An LRS connection is considered Active when it is enabled.
     */
    @Override
    public boolean isValid(LrsConnection connection, ConstraintValidatorContext constraintValidatorContext) {
        return connection.isEnabled();
    }
}
