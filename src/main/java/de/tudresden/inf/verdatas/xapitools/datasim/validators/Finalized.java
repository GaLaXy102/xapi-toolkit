package de.tudresden.inf.verdatas.xapitools.datasim.validators;

import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable the validation that the weight of a {@link DatasimSimulation} is finalized.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FinalizedValidator.class)
public @interface Finalized {
    String message() default "Used or produced non-finalized Simulation where finalized was required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
