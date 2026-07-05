cmake_minimum_required(VERSION 3.22.1)
project(tesseract_android LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# --- 16 KB page alignment (Android 15 ready) -------------------------------
add_link_options(
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384")

# --- Submodules at repo root ------------------------------------------------
# jni dir = library/src/main/jni  →  four levels up is the repo root.
set(LEPT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../leptonica)
set(TESS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../tesseract)
set(JPEG_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../libjpeg-turbo)
set(PNG_DIR  ${CMAKE_CURRENT_SOURCE_DIR}/../../../../libpng)
set(TIFF_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../libtiff)

# Our custom Find{PNG,JPEG,TIFF}.cmake resolve to the in-tree targets below.
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/cmake)

# --- Image libraries for Leptonica -----------------------------------------
# Tesseract's Init needs a Leptonica with working image I/O (otherwise it fails
# with "pixReadMemTiff: function not present"). Build libjpeg-turbo + libpng +
# libtiff STATIC (baked into libleptonica.so); zlib comes from the NDK sysroot.
set(BUILD_SHARED_LIBS OFF)   # image libs → static

# libjpeg-turbo → jpeg-static
set(ENABLE_SHARED  OFF CACHE BOOL "" FORCE)
set(ENABLE_STATIC  ON  CACHE BOOL "" FORCE)
set(WITH_TURBOJPEG OFF CACHE BOOL "" FORCE)
set(WITH_SIMD      OFF CACHE BOOL "" FORCE)   # skip NEON asm on first pass
add_subdirectory(${JPEG_DIR} ${CMAKE_CURRENT_BINARY_DIR}/libjpeg-turbo)
set(JPEG_SRC_DIR         ${JPEG_DIR})
set(JPEG_GEN_INCLUDE_DIR ${CMAKE_CURRENT_BINARY_DIR}/libjpeg-turbo)

# libpng → png_static (uses NDK zlib)
set(PNG_SHARED    OFF CACHE BOOL "" FORCE)
set(PNG_STATIC    ON  CACHE BOOL "" FORCE)
set(PNG_TESTS     OFF CACHE BOOL "" FORCE)
set(PNG_TOOLS     OFF CACHE BOOL "" FORCE)
set(PNG_FRAMEWORK OFF CACHE BOOL "" FORCE)
add_subdirectory(${PNG_DIR} ${CMAKE_CURRENT_BINARY_DIR}/libpng)
set(PNG_SRC_DIR         ${PNG_DIR})
set(PNG_GEN_INCLUDE_DIR ${CMAKE_CURRENT_BINARY_DIR}/libpng)

# libtiff → tiff (uses NDK zlib + our jpeg via FindJPEG.cmake)
set(tiff-tools   OFF CACHE BOOL "" FORCE)
set(tiff-tests   OFF CACHE BOOL "" FORCE)
set(tiff-contrib OFF CACHE BOOL "" FORCE)
set(tiff-docs    OFF CACHE BOOL "" FORCE)
add_subdirectory(${TIFF_DIR} ${CMAKE_CURRENT_BINARY_DIR}/libtiff)
set(TIFF_SRC_DIR         ${TIFF_DIR})
set(TIFF_GEN_INCLUDE_DIR ${CMAKE_CURRENT_BINARY_DIR}/libtiff ${CMAKE_CURRENT_BINARY_DIR}/libtiff/libtiff)

# --- Leptonica (links the image libs; Tesseract depends on it) -------------
set(BUILD_SHARED_LIBS ON  CACHE BOOL "" FORCE)   # → libleptonica.so
set(BUILD_PROG        OFF CACHE BOOL "" FORCE)   # no CLI tools
set(SW_BUILD          OFF CACHE BOOL "" FORCE)   # don't fetch external deps
add_subdirectory(${LEPT_DIR} ${CMAKE_CURRENT_BINARY_DIR}/leptonica)
# Leptonica generates config_auto.h / endianness.h into its build dir. Track that
# dir so the Leptonica CONFIG shim (for libtesseract) and the JNI target below can
# add it to their include paths. (Don't touch the leptonica target's INTERFACE —
# it has install/export rules that reject raw source/build-tree paths.)
set(LEPT_GEN_INCLUDE_DIR ${CMAKE_CURRENT_BINARY_DIR}/leptonica/src)

# --- cpu_features → ndk_compat (Tesseract requires it on Android) ----------
# Tesseract 5.5.0 does find_package(CpuFeaturesNdkCompat REQUIRED) + links
# CpuFeatures::ndk_compat. Build it STATIC so it bakes into libtesseract.so.
set(CF_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../cpu_features)
set(BUILD_TESTING OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF)               # cpu_features only → static
add_subdirectory(${CF_DIR} ${CMAKE_CURRENT_BINARY_DIR}/cpu_features)
set(BUILD_SHARED_LIBS ON)                # restore shared for Tesseract
# cpu_features defines target `ndk_compat`; Tesseract wants CpuFeatures::ndk_compat.
if(TARGET ndk_compat AND NOT TARGET CpuFeatures::ndk_compat)
    add_library(CpuFeatures::ndk_compat ALIAS ndk_compat)
endif()

# --- Tesseract -------------------------------------------------------------
set(BUILD_TRAINING_TOOLS OFF CACHE BOOL "" FORCE)
set(BUILD_TESTS          OFF CACHE BOOL "" FORCE)
set(DISABLE_CURL         ON  CACHE BOOL "" FORCE)
set(DISABLE_ARCHIVE      ON  CACHE BOOL "" FORCE)
set(GRAPHICS_DISABLED    ON  CACHE BOOL "" FORCE)
set(OPENMP_BUILD         OFF CACHE BOOL "" FORCE)   # Free = no OpenMP (Pro flips this)
# Consume our in-tree Leptonica + cpu_features through local CONFIG-package shims
# in jni/cmake/, so Tesseract's find_package() calls succeed without any install.
set(Leptonica_DIR            ${CMAKE_CURRENT_SOURCE_DIR}/cmake CACHE PATH "" FORCE)
set(CpuFeaturesNdkCompat_DIR ${CMAKE_CURRENT_SOURCE_DIR}/cmake CACHE PATH "" FORCE)
add_subdirectory(${TESS_DIR} ${CMAKE_CURRENT_BINARY_DIR}/tesseract)

# --- JNI bridge → libtesseract_jni.so --------------------------------------
add_library(tesseract_jni SHARED tesseract_jni.cpp)
find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)   # Android Bitmap access

target_include_directories(tesseract_jni PRIVATE
    ${TESS_DIR}/include
    ${CMAKE_CURRENT_BINARY_DIR}/tesseract/include   # generated headers (tesseract/version.h)
    ${LEPT_DIR}/src
    ${LEPT_GEN_INCLUDE_DIR})                          # generated leptonica headers (endianness.h)

target_link_libraries(tesseract_jni
    libtesseract
    leptonica
    ${log-lib}
    ${jnigraphics-lib})
