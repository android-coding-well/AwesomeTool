package com.junmeng.compiler.tool;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.PRIVATE;

final public class ClassValidator {
    /**
     * 是否私有
     *
     * @param annotatedClass
     * @return
     */
    public static boolean isPrivate(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(PRIVATE);
    }

    /**
     * 获得类名
     *
     * @param type
     * @param packageName
     * @return
     */
    public static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen);
    }
}