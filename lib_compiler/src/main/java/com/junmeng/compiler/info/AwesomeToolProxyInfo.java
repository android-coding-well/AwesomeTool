package com.junmeng.compiler.info;

import com.junmeng.annotation.ConstantValue;
import com.junmeng.compiler.tool.ClassValidator;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by HWJ on 2017/3/12.
 */

public class AwesomeToolProxyInfo {

    private TypeElement typeElement;

    private String packageName;//注解所在类的包名,如com.junmeng.aad
    private String className;//注解所在类的类名,如MainActivity
    private String proxyClassName;//代理类的名称,如MainActivityHelper
    private String proxyClassSimpleName;//代理类声明对象的名称


    private VariableElement injectObjectElement;//InjectObject注解的元素
    public Map<String, ExecutableElement> workInBackgroundMethods = new HashMap<>();//WorkInBackground
    public Map<String, ExecutableElement> workInMainThreadMethods = new HashMap<>();

    public AwesomeToolProxyInfo(Elements elementUtils, TypeElement classElement) {
        this.typeElement = classElement;
        System.out.println("类全路径：" + classElement.getQualifiedName());

        //获得类所在的包地址
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        System.out.println("包名：" + packageName);

        //classname
        String className = ClassValidator.getClassName(classElement, packageName);
        System.out.println("类名：" + className);

        this.packageName = packageName;
        this.className = className;
        this.proxyClassName = className + ConstantValue.SUFFIX;
    }

    /**
     * 设置InjectObject注解元素
     * @param variableElement
     */
    public void setInjectObjectVariableElement(VariableElement variableElement) {
        this.injectObjectElement = variableElement;
        proxyClassSimpleName = variableElement.getSimpleName().toString();
    }

    /**
     * 获得InjectObject注解元素
     * @return
     */
    public VariableElement getInjectObjectVariableElement() {
        return this.injectObjectElement ;
    }

    /**
     * 获得代理类全称
     * @return
     */
    public String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    /**
     * 获得注解类元素
     * @return
     */
    public TypeElement getTypeElement() {
        return typeElement;
    }

    /**
     * 生成源代码
     *
     * @return
     */
    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("//Generated code. Do not modify!").append("\n")
                .append("//自动生成代码，请勿修改！").append("\n")
                .append("package ").append(packageName).append(";").append("\n").append("\n")
                .append("import android.os.Handler;").append("\n")
                .append("import android.os.HandlerThread;").append("\n")
                .append("import android.os.Message;").append("\n").append("\n")
                .append("import com.junmeng.api.inter.IObjectInjector;").append("\n").append("\n")
                .append("import java.lang.ref.WeakReference;").append("\n").append("\n")

                .append("public class ").append(proxyClassName).append(" implements IObjectInjector<").append(className).append(">").append(" {").append("\n")

                .append(generateFinalMessageType())

                .append("private Handler mainHandler;").append("\n")
                .append("private Handler workHandler;").append("\n")
                .append("private HandlerThread handlerThread;").append("\n")
                .append("private ").append("WeakReference<").append(className).append("> target;").append("\n")

                .append(generateInjectMethod())
                .append(generateInitMethod())
                .append(generateMethods())
                .append(generateQuitMethod())

                .append("}").append("\n");
        return builder.toString();
    }


    /**
     * 生成inject方法
     *
     * @return
     */
    private String generateInjectMethod() {
        StringBuilder builder = new StringBuilder();
        builder.append("@Override").append("\n")
                .append("public void inject(final ").append(className).append(" target){").append("\n")
                .append("if(target.").append(proxyClassSimpleName).append("!=null){").append("\n")
                .append("target.").append(proxyClassSimpleName).append(".quit();").append("\n")
                .append("}").append("\n")
                .append("target.").append(proxyClassSimpleName).append("=new ").append(proxyClassName).append("();").append("\n")
                .append("target.").append(proxyClassSimpleName).append(".init(target);").append("\n")
                .append("}").append("\n");
        return builder.toString();
    }


    private String generateMethods() {
        StringBuilder builder = new StringBuilder();
        for (String key : workInBackgroundMethods.keySet()) {
            builder.append("public void ").append(key).append("(){").append("\n")
                    .append("workHandler.sendEmptyMessage(MESSAGE_").append(key.toUpperCase()).append(");").append("\n")
                    .append("}").append("\n");
        }
        for (String key : workInMainThreadMethods.keySet()) {
            builder.append("public void ").append(key).append("(){").append("\n")
                    .append("mainHandler.sendEmptyMessage(MESSAGE_").append(key.toUpperCase()).append(");").append("\n")
                    .append("}").append("\n");
        }
        return builder.toString();
    }


    /**
     * 生成消息类型
     *
     * @return
     */
    private String generateFinalMessageType() {
        StringBuilder builder = new StringBuilder();
        int i = 1;
        for (String key : workInBackgroundMethods.keySet()) {
            builder.append("public static final int MESSAGE_").append(key.toUpperCase()).append("=").append(i++).append(";").append("\n");
        }
        for (String key : workInMainThreadMethods.keySet()) {
            builder.append("public static final int MESSAGE_").append(key.toUpperCase()).append("=").append(i++).append(";").append("\n");
        }
        return builder.toString();
    }

    /**
     * 生成初始化方法代码
     *
     * @return
     */
    private String generateInitMethod() {
        StringBuilder builder = new StringBuilder();
        builder.append("public void init(final ").append(className).append(" target){\n")
                .append("this.target=new WeakReference<").append(className).append(">(target);").append("\n")
                .append("handlerThread = new HandlerThread(\"").append("ht_").append(proxyClassName).append("\");").append("\n")
                .append("handlerThread.start();").append("\n")
                .append("mainHandler = new Handler() {").append("\n")
                .append("@Override").append("\n")
                .append("public void handleMessage(Message msg) {").append("\n")

                .append(generateMainThreadSwitch())

                .append("}").append("\n")
                .append("};").append("\n")

                .append("workHandler = new Handler(handlerThread.getLooper()) {").append("\n")
                .append("@Override").append("\n")
                .append("public void handleMessage(Message msg) {").append("\n")

                .append(generateBackgroundSwitch())

                .append("}").append("\n")
                .append("};").append("\n")

                .append("}").append("\n");

        return builder.toString();
    }

    /**
     * 生成退出方法代码
     *
     * @return
     */
    private String generateQuitMethod() {
        StringBuilder builder = new StringBuilder();
        builder.append("/**").append("\n")
                .append("* ").append("在不用时务必调用此方法，防止内存泄漏").append("\n")
                .append("*/ ").append("\n")
                .append("public void quit(){\n")
                .append("if(handlerThread!=null&&handlerThread.isAlive()){").append("\n")
                .append("handlerThread.quitSafely();").append("\n")
                .append("}").append("\n")
                .append("}").append("\n");
        return builder.toString();
    }

    /**
     * 生成主线程switch代码
     *
     * @return
     */
    private String generateMainThreadSwitch() {
        StringBuilder builder = new StringBuilder();
        builder.append("switch (msg.what) {").append("\n");
        for (String key : workInMainThreadMethods.keySet()) {
            builder.append("case MESSAGE_").append(key.toUpperCase()).append(":").append("\n")
                    .append("target.").append(workInMainThreadMethods.get(key).getSimpleName()).append("();").append("\n")
                    .append("break;").append("\n");
        }

        builder.append("}").append("\n");
        return builder.toString();
    }

    /**
     * 生成子线程switch代码
     *
     * @return
     */
    private String generateBackgroundSwitch() {
        StringBuilder builder = new StringBuilder();
        builder.append("switch (msg.what) {").append("\n");
        for (String key : workInBackgroundMethods.keySet()) {
            builder.append("case MESSAGE_").append(key.toUpperCase()).append(":").append("\n")
                    .append("target.").append(workInBackgroundMethods.get(key).getSimpleName()).append("();").append("\n")
                    .append("break;").append("\n");
        }

        builder.append("}").append("\n");
        return builder.toString();
    }


}
