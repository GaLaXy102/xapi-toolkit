package de.tudresden.inf.rn.xapi.datatools.datasim.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NonFinalizedValidator.class)
public @interface NonFinalized {
    String message() default "Used or produced finalized Simulation where non-finalized was required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
