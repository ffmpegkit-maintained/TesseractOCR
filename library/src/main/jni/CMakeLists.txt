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

# --- Tesseract -------------------------------------------------------------
set(BUILD_TRAINING_TOOLS OFF CACHE BOOL "" FORCE)
set(BUILD_TESTS          OFF CACHE BOOL "" FORCE)
set(DISABLE_CURL         ON  CACHE BOOL "" FORCE)
set(DISABLE_ARCHIVE      ON  CACHE BOOL "" FORCE)
set(GRAPHICS_DISABLED    ON  CACHE BOOL "" FORCE)
set(OPENMP_BUILD         OFF CACHE BOOL "" FORCE)   # Free = no OpenMP (Pro flips this)
# Point Tesseract at the Leptonica we just added (its find_package call).
set(Leptonica_DIR ${CMAKE_CURRENT_BINARY_DIR}/leptonica CACHE PATH "" FORCE)
set(LEPTONICA_INCLUDE_DIR ${LEPT_DIR}/src CACHE PATH "" FORCE)
add_subdirectory(${TESS_DIR} ${CMAKE_CURRENT_BINARY_DIR}/tesseract)

# NOTE: on Android, Tesseract's find_package(Leptonica) is finicky. If configure
# fails, the fallback (as used by Tesseract4Android) is ndk-build with Android.mk.
# To validate at first build; do not assume this compiles as-is.

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
