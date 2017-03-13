package com.junmeng.api;

import com.junmeng.annotation.ConstantValue;
import com.junmeng.api.inter.IObjectInjector;

/**
 * Created by HWJ on 2017/3/12.
 */
public class AwesomeTool {

    public static void inject(Object target) {
        Class<?> clazz = target.getClass();
        String proxyClassFullName = clazz.getName() + ConstantValue.SUFFIX;
        Class<?> proxyClazz = null;
        try {
            proxyClazz = Class.forName(proxyClassFullName);
            IObjectInjector objectInjector = (IObjectInjector) proxyClazz.newInstance();
            objectInjector.inject(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
