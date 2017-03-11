package com.yzd.propertymonitor.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * 带Monitor的注解的类
 *
 * Created by yanzhaodi on 2016/10/21.
 */
public class AnnotatedClass {

    private TypeElement mClassElement;

    private List<MonitorField> mMonitorFields;

    private Elements mElementUtils;

    public AnnotatedClass(TypeElement classElement, Elements elementUtils) {
        this.mClassElement = classElement;
        this.mElementUtils = elementUtils;

        mMonitorFields = new ArrayList<>();
    }

    public void addMonitorField(MonitorField field) {
        mMonitorFields.add(field);
    }

    /**
     * 生成Check代码
     */
    public JavaFile generateCheck() {
        String className = mClassElement.getSimpleName() + "Helper";
        TypeSpec.Builder helper = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(TypeUtil.HELPER)
                .addSuperinterface(PropertyChangeListener.class);
        helper.addField(TypeName.get(mClassElement.asType()), "target", Modifier.PRIVATE);
        helper.addField(PropertyChangeSupport.class, "ps", Modifier.PRIVATE);

        MethodSpec.Builder changeListener = MethodSpec.methodBuilder("propertyChange")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PropertyChangeEvent.class, "evt")
                .returns(TypeName.VOID);

        MethodSpec.Builder checkBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addParameter(TypeName.get(mClassElement.asType()), "target", Modifier.FINAL)
                .returns(TypeUtil.HELPER);

        checkBuilder.addStatement(className + " helper = new " + className + "()");
        checkBuilder.addStatement("helper.target = target");
        checkBuilder.addStatement("helper.ps = new PropertyChangeSupport(target)");
        checkBuilder.addStatement("helper.ps.addPropertyChangeListener(helper)");
        checkBuilder.addStatement("return helper");

        for (MonitorField field : mMonitorFields) {
            String fieldName = field.getFieldName();
            String willSet = field.getWillSet();
            String didSet = field.getDidSet();
            TypeName fieldType = TypeName.get(field.getFieldType());

            if (!didSet.equals("")) {
                changeListener.beginControlFlow("if (evt.getPropertyName().equals($S))", fieldName);
                changeListener.addStatement("target.$L(($L)evt.getOldValue(), ($L)evt.getNewValue())", didSet, fieldType.toString(), fieldType.toString());
                changeListener.addStatement("return");
                changeListener.endControlFlow();
            }

            MethodSpec.Builder setBuilder = MethodSpec.methodBuilder("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(fieldType, fieldName)
                    .returns(TypeName.VOID);
            if (!willSet.equals("")) {
                setBuilder.beginControlFlow("if (target.$L())", willSet);
                setBuilder.addStatement("return");
                setBuilder.endControlFlow();
            }
            if (!didSet.equals("")) {
                setBuilder.addStatement(fieldType.toString() + " oldValue = target.$L", fieldName);
            }
            setBuilder.addStatement("target.$L = $L", fieldName, fieldName);
            if (!didSet.equals("")) {
                setBuilder.addStatement("ps.firePropertyChange($S, oldValue, $L)", fieldName, fieldName);
            }
            helper.addMethod(setBuilder.build());
        }

        helper.addMethod(changeListener.build()).addMethod(checkBuilder.build());

        String packageName = mElementUtils.getPackageOf(mClassElement).getQualifiedName().toString();
        return JavaFile.builder(packageName, helper.build()).build();
    }

}
