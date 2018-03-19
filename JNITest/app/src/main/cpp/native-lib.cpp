#include <jni.h>
#include <string>
#include <sstream>

#ifdef _cplusplus
extern "C" {
#endif

//动态注册的方法要放在registerMethods前面，否则无法编译通过
JNIEXPORT jstring JNICALL int2String(JNIEnv *env,
                                         jobject instance, jint value){
//    char buf[64];
//    sprintf(buf, "Hello from C++ %d", value);
//    return env->NewStringUTF(buf);

    std::string str = "Hello from C++";
    str += value;
    return env->NewStringUTF(str.c_str());

    std::stringstream sss;
    sss << "Hello from C++" << value;
    return env->NewStringUTF(sss.str().c_str());
}

JNIEXPORT jint JNICALL getJavaFieldValue(JNIEnv *env,
                                     jobject instance){
    jclass jclazz = env->GetObjectClass(instance);
    if(jclazz == NULL){
        return NULL;
    }

    jfieldID fieldId = env->GetFieldID(jclazz, "page", "I");
    if(fieldId == NULL){
        return NULL;
    }

    int fieldValue = env->GetIntField(instance, fieldId);

    return fieldValue;

}

JNIEXPORT void JNICALL editJavaFieldValue(JNIEnv *env,
                                     jobject instance){
    jclass jclazz = env->GetObjectClass(instance);
    if(jclazz == NULL){
        return;
    }

    jfieldID fieldId = env->GetFieldID(jclazz, "page", "I");
    if(fieldId == NULL){
        return;
    }

    int fieldValue = env->GetIntField(instance, fieldId);

    env->SetIntField(instance, fieldId, fieldValue + 1);

}

JNIEXPORT void JNICALL callJavaMethod(JNIEnv *env,
                                          jobject instance){
    jclass jclazz = env->GetObjectClass(instance);
    if(jclazz == NULL){
        return;
    }

    jmethodID methodID = env->GetMethodID(jclazz, "showMessage", "(Ljava/lang/String;)V");
    if(methodID == NULL){
        return;
    }

    env->CallVoidMethod(instance, methodID, env->NewStringUTF("我是C++的数据"));
}

JNINativeMethod registerMethods[] = {
        {"int2String", "(I)Ljava/lang/String;", (void*)int2String},
        {"getJavaFieldValue", "()I", (void*)getJavaFieldValue},
        {"editJavaFieldValue", "()V", (void*)editJavaFieldValue},
        {"callJavaMethod", "()V", (void*)callJavaMethod},
};


JNIEXPORT jstring JNICALL
Java_com_pinery_jni_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject instance) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env = NULL;

    if(vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK){
        return JNI_EVERSION;
    }

    jclass jclazz = env->FindClass("com/pinery/jni/MainActivity");
    if(jclazz == NULL){
        return JNI_ERR;
    }

    if(env->RegisterNatives(jclazz, registerMethods, sizeof(registerMethods) / sizeof(JNINativeMethod)) < 0){
        return JNI_ERR;
    }

    env->DeleteLocalRef(jclazz);

    return JNI_VERSION_1_4;
}

#ifdef _cplusplus
}
#endif