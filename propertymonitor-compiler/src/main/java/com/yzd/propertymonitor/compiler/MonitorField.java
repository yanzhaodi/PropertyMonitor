package com.yzd.propertymonitor.compiler;

import com.yzd.propertymonitor.annotation.Monitor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * 封装Monitor注解的对象
 *
 * Created by yanzhaodi on 2016/10/21.
 */
public class MonitorField {

    private String willSet;
    private String didSet;

    // 被Monitor注解的变量
    private VariableElement mField;

    public MonitorField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                    String.format("Only fields can be annotated with @%s", Monitor.class.getSimpleName()));
        }

        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            throw new IllegalArgumentException(
                    String.format("@%s can't used on private", Monitor.class.getSimpleName()));
        }

        mField = (VariableElement) element;

        Monitor monitor = mField.getAnnotation(Monitor.class);
        willSet = monitor.willSet();
        didSet = monitor.didSet();
    }

    public String getWillSet() {
        return willSet;
    }

    public String getDidSet() {
        return didSet;
    }

    public String getFieldName() {
        return mField.getSimpleName().toString();
    }

    public TypeMirror getFieldType() {
        return mField.asType();
    }
}
