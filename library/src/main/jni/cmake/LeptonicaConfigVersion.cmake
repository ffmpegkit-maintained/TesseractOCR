# Version file so `find_package(Leptonica 1.74 CONFIG)` accepts our shim. We ship
# Leptonica 1.84.1; treat any requested version <= that as compatible.
set(PACKAGE_VERSION "1.84.1")
set(PACKAGE_VERSION_COMPATIBLE TRUE)
if(PACKAGE_FIND_VERSION AND PACKAGE_FIND_VERSION VERSION_GREATER "1.84.1")
    set(PACKAGE_VERSION_COMPATIBLE FALSE)
endif()
if(PACKAGE_FIND_VERSION VERSION_EQUAL "1.84.1")
    set(PACKAGE_VERSION_EXACT TRUE)
endif()
