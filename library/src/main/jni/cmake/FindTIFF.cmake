# Custom FindTIFF → in-tree libtiff target (tiff). See FindJPEG.cmake.
# Public headers (tiffio.h) live in <src>/libtiff; generated tif_config.h /
# tiffconf.h live in the build dir (TIFF_GEN_INCLUDE_DIR).
set(TIFF_FOUND TRUE)
set(TIFF_LIBRARY tiff)
set(TIFF_LIBRARIES tiff)
set(TIFF_INCLUDE_DIR "${TIFF_SRC_DIR}/libtiff" "${TIFF_GEN_INCLUDE_DIR}")
set(TIFF_INCLUDE_DIRS "${TIFF_SRC_DIR}/libtiff" "${TIFF_GEN_INCLUDE_DIR}")
set(TIFF_VERSION_STRING "4.7.0")
