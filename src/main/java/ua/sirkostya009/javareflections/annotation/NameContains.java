package ua.sirkostya009.javareflections.annotation;

import java.lang.annotation.*;

/**
 * A mandatory annotation for injecting a single file. Can be either {@code CSVParser} or {@code MultipartFile}.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NameContains {
    String value();
}
