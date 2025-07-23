#include "native_lib.h"
#include "picosha2.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_practica1_androidgame_MainActivity_computeSha256(JNIEnv *env, jobject obj, jstring input) {
    // Convertir el string de Java a C++
    const char *nativeInput = env->GetStringUTFChars(input, nullptr);

    // Crear std::string a partir del puntero
    std::string inputStr(nativeInput);

    // Generar el hash SHA256 usando picosha2
    std::string hashHex = picosha2::hash256_hex_string(inputStr);

    // Liberar el string de Java
    env->ReleaseStringUTFChars(input, nativeInput);

    // Retornar el hash como un string de Java
    return env->NewStringUTF(hashHex.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_practica1_androidgame_MainActivity_generateHmac(JNIEnv *env, jobject obj, jstring message, jstring key) {
    // Convertir los strings de Java a C++
    const char *nativeMessage = env->GetStringUTFChars(message, nullptr);
    const char *nativeKey = env->GetStringUTFChars(key, nullptr);

    // Crear std::string a partir de los punteros
    std::string messageStr(nativeMessage);
    std::string keyStr(nativeKey);

    // Concatenar clave y mensaje
    std::string combinedInput = keyStr + messageStr;

    // Generar el hash SHA256 usando picosha2
    std::string hmacHex = picosha2::hash256_hex_string(combinedInput);

    // Liberar los strings de Java
    env->ReleaseStringUTFChars(message, nativeMessage);
    env->ReleaseStringUTFChars(key, nativeKey);

    // Retornar el HMAC generado como un string de Java
    return env->NewStringUTF(hmacHex.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_practica1_androidengine_AndroidEngine_hashThis(JNIEnv *env, jobject thiz, jstring data) {
    // TODO: implement hashThis()
}