package ua.sirkostya009.javareflections.annotation;

import java.lang.annotation.*;

/**
 * Methods that are annotated with this annotations inside a parser class will be invoked.
 * <p>
 * You can use dependency injection with these methods. Currently, you can only inject {@code CSVParser, CSVParser[],
 * MultipartFile[], MultipartFile, List<MultipartFile>, Writer, OutputStreamWriter, FileWriter, CSVFormat, Customer,
 * String} classes.
 * @see ua.sirkostya009.javareflections.service.ParserServiceImpl#injectParameter
 * @see ua.sirkostya009.javareflections.parser.SampleParser SampleParser
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parse {
    /**
     * Order in which method should be executed. If you have multiple parse methods, change this value accordingly.
     * Otherwise, execution order will not be guaranteed.
     */
    int pass() default 1;
}
