package com.pinery.compile;

import com.google.auto.service.AutoService;
import com.pinery.annotations.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 自定义的AbstractProcessor，用于编译时处理注解
 */
@AutoService(Processor.class) //添加AutoService注解，自动注册ViewBindProcessor注解处理器
public class ViewBindProcessor extends AbstractProcessor{

    private Map<TypeElement, List<ViewBindInfo>> bindMap = new HashMap<>();

    //用来处理类型数据的工具类
    private Types typeUtils;
    //用来处理程序元素的工具类
    private Elements elementUtils;
    //用来给注解处理器创建文件
    private Filer filer;
    //用来给注解处理器报告错误，警告，提示等消息。
    private Messager messager;

    /**
     * 会被注解处理工具调用，参数ProcessingEnvironment提供了Elements，Types，Filer，Messager 等。
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    /**
     * 指定注解处理器是注册给那一个注解的，它是一个字符串的集合，意味着可以支持多个类型的注解，并且字符串是合法全名。
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();

        //添加自定义的BindView注解
        annotataions.add(BindView.class.getCanonicalName());

        return annotataions;
    }

    /**
     * 指定使用的Java版本，通常这里返回SourceVersion.latestSupported()
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 相当于每个处理器的main函数，在这里可以做扫描、评估和处理注解代码的操作，以及生成Java文件。
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        collectBindViewAnnotations(roundEnvironment);

        generateJavaFilesWithJavaPoet();

        return false;
    }

    /**
     * 收集BindView注解
     * @param roundEnvironment
     * @return
     */
    private boolean collectBindViewAnnotations(RoundEnvironment roundEnvironment){
        //查找所有添加了注解BindView的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        if(elements == null || elements.isEmpty()){
            return false;
        }

        for(Element element : elements){
            //注解BindView必须添加在属性上
            if(element.getKind() != ElementKind.FIELD){
                error(element, "只有类的属性可以添加@%s注解", BindView.class.getCanonicalName());
                return false;
            }

            //获取注解的值
            int viewId = element.getAnnotation(BindView.class).value();
            //这个元素是属性类型的元素
            VariableElement viewElement = (VariableElement) element;
            //获取直接包含属性元素的元素，即类元素
            TypeElement typeElement = (TypeElement) viewElement.getEnclosingElement();

            //将类型元素作为key，保存到bindMap暂存
            List<ViewBindInfo> viewBindInfoList = bindMap.get(typeElement);
            if(viewBindInfoList == null){
                viewBindInfoList = new ArrayList<>();
                bindMap.put(typeElement, viewBindInfoList);
            }

            info("注解信息：viewId=%d, name=%s, type=%s", viewId, viewElement.getSimpleName().toString(), viewElement.asType().toString());

            viewBindInfoList.add(new ViewBindInfo(viewId, viewElement.getSimpleName().toString(), viewElement.asType()));
        }

        return true;
    }

    /**
     * 生成注解处理之后的Java文件
     */
    private void generateJavaFilesWithJavaPoet(){
        if(bindMap.isEmpty()){
            return;
        }

        //针对每个类型元素，生成一个新文件，例如，针对MainActivity，生成MainActivity_ViewBind文件
        for(Map.Entry<TypeElement, List<ViewBindInfo>> entry : bindMap.entrySet()){
            TypeElement typeElement = entry.getKey();
            List<ViewBindInfo> list = entry.getValue();

            //获取当前类型元素所在的包名
            String pkgName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();

            //获取类的全名称
            ClassName t = ClassName.bestGuess("T");
            ClassName viewBinder = ClassName.bestGuess("com.pinery.bind_lib.ViewBinder");

            //定义方法结构
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("bind")
                    .addAnnotation(Override.class)//Override注解
                    .addModifiers(Modifier.PUBLIC)//public修饰符
                    .returns(void.class)//返回类型void
                    .addParameter(t, "activity")//参数类型
                    ;
            //为方法添加实现
            for(ViewBindInfo info : list){
                methodSpecBuilder.addStatement("activity.$L = activity.findViewById($L)", info.viewName, info.viewId);
            }

            //定义类结构
            TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + "_ViewBinder")
                    .addModifiers(Modifier.PUBLIC)//public修饰符
                    .addTypeVariable(TypeVariableName.get("T", TypeName.get(typeElement.asType())))//泛型声明
                    .addSuperinterface(ParameterizedTypeName.get(viewBinder, t))
                    .addMethod(methodSpecBuilder.build())//方法
                    .build();

            //定义一个Java文件结构
            JavaFile javaFile = JavaFile.builder(pkgName, typeSpec).build();
            try {
                //写入到filer中
                javaFile.writeTo(filer);
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

    }

    /**
     * 错误提示
     * @param element
     * @param msg
     * @param args
     */
    private void error(Element element, String msg, Object... args){
        //输出错误提示
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
    }

    /**
     * 信息提示
     * @param msg
     * @param args
     */
    private void info(String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args));
    }

}
