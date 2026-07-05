# Shim so Tesseract's `find_package(Leptonica <ver> CONFIG)` consumes the in-tree
# Leptonica target built by `add_subdirectory(leptonica)` — no install step needed.
# Selected via `set(Leptonica_DIR .../jni/cmake)` in the parent CMakeLists.
set(Leptonica_FOUND TRUE)
set(Leptonica_VERSION "1.84.1")

# Repo root is 5 levels up from this file (jni/cmake → jni → main → src → library → root).
get_filename_component(_lept_root "${CMAKE_CURRENT_LIST_DIR}/../../../../../leptonica" ABSOLUTE)
set(Leptonica_INCLUDE_DIRS "${_lept_root}/src")

# The `leptonica` target (defined by add_subdirectory) carries its own PUBLIC
# include dirs, so linking it propagates the generated headers too.
set(Leptonica_LIBRARIES leptonica)
