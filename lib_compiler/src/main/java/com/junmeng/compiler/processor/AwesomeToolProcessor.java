package com.junmeng.compiler.processor;

import com.google.auto.service.AutoService;
import com.junmeng.annotation.InjectObject;
import com.junmeng.annotation.WorkInBackground;
import com.junmeng.annotation.WorkInMainThread;
import com.junmeng.compiler.info.AwesomeToolProxyInfo;
import com.junmeng.compiler.tool.ClassValidator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class AwesomeToolProcessor extends AbstractProcessor {
    private static final String TAG = "AwesomeToolProcessor";
    private Filer mFileUtils;//跟文件相关的辅助类，生成JavaSourceCode
    private Elements elementUtils;//跟元素相关的辅助类，帮助我们去获取一些元素相关的信息
    private Messager messager;//跟日志相关的辅助类
    private Map<String, AwesomeToolProxyInfo> proxyInfoMap = new HashMap<String, AwesomeToolProxyInfo>();//key为注解所在类的全名

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    /**
     * 支持的注解类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(InjectObject.class.getCanonicalName());
        supportTypes.add(WorkInBackground.class.getCanonicalName());
        supportTypes.add(WorkInMainThread.class.getCanonicalName());
        return supportTypes;
    }

    /**
     * 注解处理器支持到的JAVA版本
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        printMessage("SupportedSourceVersion=%s",SourceVersion.latestSupported().name());
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        printMessage("process:annotations size=%d", annotations.size());
        proxyInfoMap.clear();
        handleInjectObjectAnnotation(roundEnv);
        handleWorkInBackgroundAnnotation(roundEnv);
        handleWorkInMainThreadAnnotation(roundEnv);

        printMessage("AwesomeToolProxyInfo Map size=%d", proxyInfoMap.size());

        generateSourceFiles();
        return false;//如果返回true,当有两个注解作用在同一方法上，那么第一个处理完了之后就不会再处理第二个
    }

    /**
     * 生成类文件
     */
    private void generateSourceFiles() {
        for (String key : proxyInfoMap.keySet()) {
            AwesomeToolProxyInfo proxyInfo = proxyInfoMap.get(key);
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                        proxyInfo.getProxyClassFullName(),//类名全称
                        proxyInfo.getTypeElement());//类元素
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }

        }
    }

    private void handleWorkInMainThreadAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elesWithBind = roundEnv.getElementsAnnotatedWith(WorkInMainThread.class);
        for (Element element : elesWithBind) {
            if (!checkMethodAnnotationValid(element, WorkInMainThread.class)) {
                return;
            }
            ExecutableElement variableElement = (ExecutableElement) element;
            String simpleName = variableElement.getSimpleName().toString();
            //printMessage("WorkInMainThread注解的方法名称：" + simpleName);
            //get class type
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            //get full class name
            String fqClassName = classElement.getQualifiedName().toString();
            //printMessage("WorkInMainThread注解所在类的全名：" + fqClassName);

            AwesomeToolProxyInfo proxyInfo = getAwesomeToolProxyInfo(classElement, fqClassName);
            if (proxyInfo.getInjectObjectVariableElement() == null) {
                error(classElement, "%s must have a InjectObject annotation", classElement.getSimpleName());
            }
            proxyInfo.workInMainThreadMethods.put(simpleName, variableElement);
        }
    }

    private void handleWorkInBackgroundAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(WorkInBackground.class);
        for (Element element : elements) {
            //判断InjectObject的作用域
            if (!checkMethodAnnotationValid(element, WorkInBackground.class)) {
                return;
            }
            ExecutableElement variableElement = (ExecutableElement) element;
            //printMessage("WorkInBackground注解的方法名称：" + variableElement.getSimpleName().toString());
            //printMessage("WorkInBackground注解的方法返回类型：" + variableElement.getReturnType().getKind().toString());
            //printMessage("WorkInBackground注解的方法参数列表：" + variableElement.getParameters().toString());

            //get class type
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();

            //get class full name
            String fqClassName = classElement.getQualifiedName().toString();
            printMessage("WorkInBackground注解所在类的全名：" + fqClassName);

            //find AwesomeToolProxyInfo,create one if not find
            AwesomeToolProxyInfo proxyInfo = getAwesomeToolProxyInfo(classElement, fqClassName);

            if (proxyInfo.getInjectObjectVariableElement() == null) {
                error(classElement, "%s must have a InjectObject annotation", classElement.getSimpleName());
            }
            proxyInfo.workInBackgroundMethods.put(variableElement.getSimpleName().toString(), variableElement);
        }

    }

    private void handleInjectObjectAnnotation(RoundEnvironment roundEnv) {
        //get InjectObject elements
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(InjectObject.class);
        printMessage("InjectObject size：" + elements.size());
        for (Element element : elements) {
            if (!checkFieldAnnotationValid(element, InjectObject.class)) {
                return;
            }

            VariableElement variableElement = (VariableElement) element;
            //printMessage("InjectObject注解的对象名称："+variableElement.getSimpleName());

            //get class type
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            //get class full name
            String fullClassName = classElement.getQualifiedName().toString();
            //printMessage("InjectObject注解所在类的全名："+fullClassName);

            //find AwesomeToolProxyInfo,create one if not find
            AwesomeToolProxyInfo proxyInfo = getAwesomeToolProxyInfo(classElement, fullClassName);
            proxyInfo.setInjectObjectVariableElement(variableElement);
        }
    }

    /**
     * find AwesomeToolProxyInfo,create one if not find
     *
     * @param classElement
     * @param fqClassName
     * @return
     */
    private AwesomeToolProxyInfo getAwesomeToolProxyInfo(TypeElement classElement, String fqClassName) {
        AwesomeToolProxyInfo proxyInfo = proxyInfoMap.get(fqClassName);
        if (proxyInfo == null) {
            proxyInfo = new AwesomeToolProxyInfo(elementUtils, classElement);
            proxyInfoMap.put(fqClassName, proxyInfo);
        }
        return proxyInfo;
    }

    /**
     * 检查成员注解的合法性
     *
     * @param annotatedElement 注解元素
     * @param clazz            注解
     * @return
     */
    private boolean checkFieldAnnotationValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.FIELD) {
            error(annotatedElement, "%s must be declared on field.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement)) {
            error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
            return false;
        }

        return true;
    }

    /**
     * 检查方法注解的合法性
     *
     * @param annotatedElement 注解元素
     * @param clazz            注解
     * @return
     */
    private boolean checkMethodAnnotationValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.METHOD) {
            error(annotatedElement, "%s must be declared on method.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement)) {
            error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
            return false;
        }

        return true;
    }

    /**
     * 产生错误提示
     *
     * @param element 注解元素
     * @param message
     * @param args
     */
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    /**
     * 打印一般信息，在Gradle Console中可见到打印的信息
     *
     * @param message
     * @param args
     */
    private void printMessage(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

}