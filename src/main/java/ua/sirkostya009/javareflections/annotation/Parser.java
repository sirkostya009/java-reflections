package ua.sirkostya009.javareflections.annotation;

import org.springframework.stereotype.Component;
import ua.sirkostya009.javareflections.model.Customer;

import java.lang.annotation.*;

@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parser {
    Customer customer();

    String name();

    String sourceFormatBeanQualifier() default "default";

    String resultFormatBeanQualifier() default "default";
}
