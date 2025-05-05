cmake_minimum_required(VERSION 3.22.1)
project("native-audio-mixer" LANGUAGES CXX)
add_library(native-audio-mixer SHARED MixerCore.cpp)

target_link_libraries(native-audio-mixer PRIVATE oboe android log) # Link directly
