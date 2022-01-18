package de.tudresden.inf.verdatas.xapitools.lrs.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ActiveValidator.class)
public @interface Active {
    String message() default "Used or produced inactive LRS Connection when active was required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
