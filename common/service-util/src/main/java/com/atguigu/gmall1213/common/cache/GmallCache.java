package com.atguigu.gmall1213.common.cache;


import java.lang.annotation.*;

@Target(ElementType.METHOD)  // 目标地方   注解在方法上使用
@Retention(RetentionPolicy.RUNTIME)  //这个注解的生命周期
@Documented
public @interface GmallCache {
    String prefix() default "cache";
}
