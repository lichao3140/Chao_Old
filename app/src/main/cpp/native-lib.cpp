#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "lichao"

#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, FORMAT, ##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, FORMAT, ##__VA_ARGS__);
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, FORMAT, ##__VA_ARGS__);
#define LOGW(FORMAT,...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, FORMAT, ##__VA_ARGS__);


extern "C"
JNIEXPORT jboolean JNICALL Java_com_runvision_utils_JniUtil_AuthorizeSN(
		JNIEnv *env, jobject obj, jstring jstr) {
    const char *cstr = env->GetStringUTFChars(jstr, 0);
	char* snArray[2] = {"ML5RR7C92H", "R50A20180807002"};

	LOGI("SN= %s", cstr);
	int i;
	for (i = 0; i < 2; i++) {
		if (strcmp(snArray[i], cstr) == 0) {
			return 1;
		} else {
			return 0;
		}
	}
	return 0;
}

extern "C"
JNIEXPORT jstring JNICALL Java_com_runvision_utils_JniUtil_getAPPID(
		JNIEnv * env, jobject obj) {
    std::string id = "J3Yscp63XC1M1ut6Fk6DguUPSEtRti99pkAagPdxd88j";
	return env->NewStringUTF(id.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL Java_com_runvision_utils_JniUtil_getSDKKEY(
		JNIEnv * env, jobject obj) {
    std::string key = "B6ZbvRk5qchXtssHMSE6dmUxDDnLtoiHaB1CTXnhdcip";
	return env->NewStringUTF(key.c_str());
}

