package com.wyh.aspect;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * @author WangYingHao
 * @since 2019-06-22
 */
@Target({TYPE, METHOD, CONSTRUCTOR})
@Retention(CLASS)
public @interface PugLog {
}
