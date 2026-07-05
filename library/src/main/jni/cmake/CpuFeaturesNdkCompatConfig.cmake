# Shim so Tesseract's `find_package(CpuFeaturesNdkCompat REQUIRED)` succeeds using
# the in-tree cpu_features `ndk_compat` target (aliased to CpuFeatures::ndk_compat
# by the parent CMakeLists). Selected via `set(CpuFeaturesNdkCompat_DIR .../jni/cmake)`.
set(CpuFeaturesNdkCompat_FOUND TRUE)
