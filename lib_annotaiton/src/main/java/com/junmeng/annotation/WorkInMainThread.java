package com.junmeng.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * UI线程注解
 * Created by HWJ on 2017/3/12.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface WorkInMainThread {
}
