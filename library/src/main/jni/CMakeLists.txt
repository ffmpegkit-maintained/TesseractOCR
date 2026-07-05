cmake_minimum_required(VERSION 3.22.1)
project(tesseract_android LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# --- 16 KB page alignment (Android 15 ready) -------------------------------
add_link_options(
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384")

# --- Submodules at repo root: leptonica/ and tesseract/ --------------------
# jni dir = library/src/main/jni  →  four levels up is the repo root.
set(LEPT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../leptonica)
set(TESS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../tesseract)

# --- Leptonica (built first — Tesseract depends on it) ---------------------
set(BUILD_SHARED_LIBS ON  CACHE BOOL "" FORCE)   # → libleptonica.so
set(BUILD_PROG        OFF CACHE BOOL "" FORCE)   # no CLI tools
set(SW_BUILD          OFF CACHE BOOL "" FORCE)   # don't fetch external deps
add_subdirectory(${LEPT_DIR} ${CMAKE_CURRENT_BINARY_DIR}/leptonica)

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
    ${LEPT_DIR}/src)

target_link_libraries(tesseract_jni
    libtesseract
    leptonica
    ${log-lib}
    ${jnigraphics-lib})
