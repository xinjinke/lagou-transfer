package com.lagou.edu.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)//-- 作用目标，此处为class
@Retention(RetentionPolicy.RUNTIME) //保留多久，此处运行期间
@Documented //注解表明这个注解应该被 javadoc工具记录
@Component
public @interface Service {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
