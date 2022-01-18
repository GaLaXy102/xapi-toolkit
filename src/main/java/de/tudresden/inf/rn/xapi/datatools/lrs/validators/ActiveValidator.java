package de.tudresden.inf.rn.xapi.datatools.lrs.validators;

import de.tudresden.inf.rn.xapi.datatools.lrs.LrsConnection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ActiveValidator implements ConstraintValidator<Active, LrsConnection> {
    @Override
    public void initialize(Active constraintAnnotation) {
        // No fields to map
    }

    @Override
    public boolean isValid(LrsConnection connection, ConstraintValidatorContext constraintValidatorContext) {
        return connection.isEnabled();
    }
}
