# Custom FindJPEG that resolves to the in-tree libjpeg-turbo target (jpeg-static),
# built by the parent CMakeLists via add_subdirectory. Selected because jni/cmake is
# on CMAKE_MODULE_PATH. Dir vars are set by the parent before the dependent
# find_package(JPEG) runs (Leptonica, libtiff).
set(JPEG_FOUND TRUE)
set(JPEG_LIBRARY jpeg-static)
set(JPEG_LIBRARIES jpeg-static)
set(JPEG_INCLUDE_DIR "${JPEG_SRC_DIR}" "${JPEG_GEN_INCLUDE_DIR}")
set(JPEG_INCLUDE_DIRS "${JPEG_SRC_DIR}" "${JPEG_GEN_INCLUDE_DIR}")
set(JPEG_VERSION "3.0.4")
