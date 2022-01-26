package de.tudresden.inf.verdatas.xapitools.datasim.validators;

import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimAlignment;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable the validation that the weight of a {@link DatasimAlignment} is valid.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AlignmentWeightValidator.class)
public @interface AlignmentWeight {
    String message() default "Alignment weights can have a value between -1 and 1";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
