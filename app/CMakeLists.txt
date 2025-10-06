cmake_minimum_required(VERSION 3.18.1)
project("aagnar")

# Создайте нативную библиотеку
add_library(aagnar-native SHARED
    src/main/cpp/native-lib.cpp)

# Добавьте PJSIP библиотеки
add_library(pj STATIC IMPORTED)
set_target_properties(pj PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/libs/pjsip/libpj-aarch64-unknown-linux-android.a)

add_library(pjsip STATIC IMPORTED)
set_target_properties(pjsip PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/libs/pjsip/libpjsip-aarch64-unknown-linux-android.a)

# Линкуйте
target_link_libraries(aagnar-native pj pjsip)