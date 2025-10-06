#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_aagnar_MainActivity_stringFromJNI(JNIEnv* env, jobject) {
    return env->NewStringUTF("Hello from PJSIP");
}