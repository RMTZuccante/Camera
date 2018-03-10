cmake_minimum_required(VERSION 3.8)
project(Camera)

set(CMAKE_CXX_STANDARD 11)

FIND_PACKAGE(OpenCV REQUIRED)
set(OpenCV_DIR "%OPENCV_DIR%\\x64\\vc15\\bin")
set(SOURCE_FILES main.cpp Config.h Camera.cpp Camera.h ImageProcessor.cpp ImageProcessor.h KNearestOcr.cpp KNearestOcr.h)
add_executable(Camera ${SOURCE_FILES})

include_directories(${OpenCV_INCLUDE_DIRS})
target_link_libraries(Camera ${OpenCV_LIBS})