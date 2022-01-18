package de.tudresden.inf.verdatas.xapitools.datasim.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AlignmentWeightValidator.class)
public @interface AlignmentWeight {
    String message() default "Alignment weights can have a value between -1 and 1";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
