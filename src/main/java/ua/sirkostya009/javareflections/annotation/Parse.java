package ua.sirkostya009.javareflections.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parse {
    int pass() default 1;
}
