package com.lagou.edu.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {

    @AliasFor("transactionManager")
    String value() default "";

    boolean readOnly() default false;
}
