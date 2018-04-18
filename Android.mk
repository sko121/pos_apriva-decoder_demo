LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := PosDaemon

LOCAL_SDK_VERSION := current

LOCAL_STATIC_JAVA_LIBRARIES := libInterface0 libInterface1 libInterface2

LOCAL_JNI_SHARED_LIBRARIES := liba01jni

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PROGUARD_FLAG_FILES := proguard-project.txt
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)


LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libInterface0:libs/android-support-v4.jar \
	libInterface1:libs/dspread_android_sdk_2.2.0.jar  \
	libInterface2:libs/iBridge.jar

LOCAL_PREBUILT_LIBS := libs/armeabi/liba01jni.so

include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))



