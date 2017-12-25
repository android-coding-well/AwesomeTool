package com.junmeng.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入对象实例,
 * Created by HWJ on 2017/3/12.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface InjectObject {

    /**
     * 线程优先级-20~19,-20代表优先级最高，详见android.os.Process,默认为THREAD_PRIORITY_DEFAULT(0)
     * @return
     */
    int priority() default 0;
}
