#ifndef JNITEST_NATIVE_LIB_H
#define JNITEST_NATIVE_LIB_H

#include <jni.h>
#include <string>

extern "C" {
JNIEXPORT jstring JNICALL Java_com_practica1_androidgame_MainActivity_computeSha256(JNIEnv *env, jobject obj, jstring input);
JNIEXPORT jstring JNICALL Java_com_practica1_androidgame_MainActivity_generateHmac(JNIEnv *env, jobject obj, jstring message, jstring key);
}

#endif // JNITEST_NATIVE_LIB_H
