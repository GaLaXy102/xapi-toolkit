package de.tudresden.inf.rn.xapi.datatools.datasim.validators;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FinalizedValidator implements ConstraintValidator<Finalized, DatasimSimulation> {
    @Override
    public void initialize(Finalized constraintAnnotation) {
        // No fields to map
    }

    @Override
    public boolean isValid(DatasimSimulation simulation, ConstraintValidatorContext constraintValidatorContext) {
        return simulation.isFinalized();
    }
}
