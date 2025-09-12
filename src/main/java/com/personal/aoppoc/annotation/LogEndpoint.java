package com.personal.aoppoc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogEndpoint {
    boolean logHeaders() default true;
    boolean logParams() default true;
    boolean logBody() default true;
    boolean logPathVariables() default true;
    String[] excludeHeaders() default {"authorization"};
    String[] excludeParams() default {};
    String[] excludePathVariables() default {};
}