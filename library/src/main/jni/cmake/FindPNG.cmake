# Custom FindPNG → in-tree libpng target (png_static). See FindJPEG.cmake.
set(PNG_FOUND TRUE)
set(PNG_LIBRARY png_static)
set(PNG_LIBRARIES png_static)
set(PNG_INCLUDE_DIR "${PNG_SRC_DIR}" "${PNG_GEN_INCLUDE_DIR}")
set(PNG_INCLUDE_DIRS "${PNG_SRC_DIR}" "${PNG_GEN_INCLUDE_DIR}")
set(PNG_PNG_INCLUDE_DIR "${PNG_SRC_DIR}" "${PNG_GEN_INCLUDE_DIR}")
set(PNG_DEFINITIONS "")
set(PNG_VERSION_STRING "1.6.44")
