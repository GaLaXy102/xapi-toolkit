package de.tudresden.inf.verdatas.xapitools.datasim.validators;

import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator to check that a given {@link DatasimSimulation} is not finalized.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public class NonFinalizedValidator implements ConstraintValidator<NonFinalized, DatasimSimulation> {
    @Override
    public void initialize(NonFinalized constraintAnnotation) {
        // No fields to map
    }

    /**
     * A simulation is finalized when the corresponding flag is set.
     */
    @Override
    public boolean isValid(DatasimSimulation simulation, ConstraintValidatorContext constraintValidatorContext) {
        return !simulation.isFinalized();
    }
}
