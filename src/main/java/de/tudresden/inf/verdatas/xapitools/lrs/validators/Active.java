package de.tudresden.inf.verdatas.xapitools.lrs.validators;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable the validation that a {@link LrsConnection} is enabled.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ActiveValidator.class)
public @interface Active {
    String message() default "Used or produced inactive LRS Connection when active was required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
