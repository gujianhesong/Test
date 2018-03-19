cmake_minimum_required(VERSION 3.4.1)

set(path_project E:/studio/Test/FFmpegTest)

add_library(zp_decode
            SHARED
            src/main/cpp/zp_decode.c)

#引用的头文件
include_directories(
                    src/main/cpp/include
                    )

add_library(avcodec SHARED IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libavcodec57.so)

add_library(avdevice SHARED IMPORTED)
set_target_properties(avdevice PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libavdevice57.so)

add_library(avfilter SHARED IMPORTED)
set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libavfilter6.so)

add_library(avformat SHARED IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libavformat57.so)

add_library(avutil SHARED IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libavutil55.so)

add_library(postproc SHARED IMPORTED)
set_target_properties(postproc PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libpostproc54.so)

add_library(swresample SHARED IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libswresample2.so)

add_library(swscale SHARED IMPORTED)
set_target_properties(swscale PROPERTIES IMPORTED_LOCATION
                      ${path_project}/app/libs/${ANDROID_ABI}/libswscale4.so)

include_directories(src/main/cpp/include)

find_library(log-lib
             log )

target_link_libraries(zp_decode
                      avutil
                      avcodec
                      avdevice
                      swresample
                      swscale
                      avfilter
                      avformat
                      postproc
                      ${log-lib})
