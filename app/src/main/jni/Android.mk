LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCVROOT:= C:\OpenCV-android-sdk
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := DetectionBasedTracker_jni.cpp
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := detection_based_tracker

include $(BUILD_SHARED_LIBRARY)
