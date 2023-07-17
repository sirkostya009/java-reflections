package ua.sirkostya009.javareflections.annotation;

import org.springframework.stereotype.Component;
import ua.sirkostya009.javareflections.model.Customer;

import java.lang.annotation.*;

/**
 * Parser is spring-component annotation meant for annotating classes that have methods annotated with @Parse.
 * Parser-annotated classes are scanned for Parse methods that are invoked procedurally (e.g. from first pass to last).
 */
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parser {
    /**
     * Customer this parser belongs to.
     */
    Customer customer();

    /**
     * Name of the parser. E.g. short description of its basic operation.
     */
    String name();

    /**
     * Bean qualifier for CSVFormat for source files.
     */
    String sourceFormatBeanQualifier() default "default";

    /**
     * Bean qualifier for CSVFormat for result files.
     */
    String resultFormatBeanQualifier() default "default";
}
