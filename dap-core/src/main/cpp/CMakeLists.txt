cmake_minimum_required(VERSION 3.22.1)

project("dap-core")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math")

add_library(
    dap-core
    SHARED
    dap_core.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    dap-core
    ${log-lib}
)
