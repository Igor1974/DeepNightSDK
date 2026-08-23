cmake_minimum_required(VERSION 3.22.1)

project("text-tools")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math")

add_library(
    text-tools
    SHARED
    text_tools.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    text-tools
    ${log-lib}
)
