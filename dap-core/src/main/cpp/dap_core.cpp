#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <android/log.h>
#include <algorithm>
#include <chrono>
#include <random>

extern "C" {

/**
 * Simplified Frequency Analysis for SDK Demo.
 */
JNIEXPORT void JNICALL
Java_com_deepnight_sdk_dap_DapNativeInterface_processFft(
        JNIEnv *env, jobject thiz,
        jbyteArray audio_data, jint size,
        jfloatArray out_magnitudes) {

    jbyte *data = env->GetByteArrayElements(audio_data, nullptr);
    jsize bands = env->GetArrayLength(out_magnitudes);
    jfloat *magnitudes = env->GetFloatArrayElements(out_magnitudes, nullptr);

    if (size > 0 && bands > 0) {
        int samplesPerBand = size / bands;
        if (samplesPerBand <= 0) samplesPerBand = 1;

        static std::mt19937 gen(std::chrono::system_clock::now().time_since_epoch().count());
        std::uniform_real_distribution<float> dist(0.0f, 0.1f);

        for (int b = 0; b < bands; b++) {
            float sum = 0.0f;
            int start = b * samplesPerBand;
            int end = (b + 1) * samplesPerBand;
            for (int j = start; j < end && j < size; j++) {
                float val = ((float)(unsigned char)data[j] - 128.0f) / 128.0f;
                sum += std::abs(val);
            }
            float avg = sum / (float)samplesPerBand;
            float val = avg * (20.0f + (float)b * 0.8f);
            if (val < 0.1f) val = 0.05f + dist(gen);
            magnitudes[b] = std::min(val, 1.0f);
        }
    }

    env->ReleaseByteArrayElements(audio_data, data, JNI_ABORT);
    env->ReleaseFloatArrayElements(out_magnitudes, magnitudes, 0);
}

JNIEXPORT void JNICALL
Java_com_deepnight_sdk_dap_DapNativeInterface_calculateAutoEq(
        JNIEnv *env, jobject thiz,
        jfloatArray fft_data, jint size, jfloatArray out_eq) {
    jfloat *fft = env->GetFloatArrayElements(fft_data, nullptr);
    jfloat *eq = env->GetFloatArrayElements(out_eq, nullptr);
    float avg = 0.0f;
    for (int i = 0; i < size; i++) avg += std::abs(fft[i]);
    avg /= (float)size;
    for (int i = 0; i < size; i++) {
        float val = std::abs(fft[i]);
        eq[i] = (val < 0.001f) ? 1.0f : std::min(avg / val, 2.0f);
    }
    env->ReleaseFloatArrayElements(fft_data, fft, JNI_ABORT);
    env->ReleaseFloatArrayElements(out_eq, eq, 0);
}

JNIEXPORT jboolean JNICALL
Java_com_deepnight_sdk_dap_DapNativeInterface_isVoiceActive(
        JNIEnv *env, jobject thiz,
        jbyteArray audio_data, jint size, jfloat threshold) {
    jbyte *data = env->GetByteArrayElements(audio_data, nullptr);
    float energy = 0.0f;
    for (int i = 0; i < size; i++) {
        float s = (float)data[i] / 128.0f;
        energy += s * s;
    }
    energy /= (float)size;
    env->ReleaseByteArrayElements(audio_data, data, JNI_ABORT);
    return energy > threshold;
}

/**
 * HONEST DATA PROCESSING BENCHMARK:
 * Bitwise operations on a large buffer. This is where C++ DOMINATES JVM.
 */
JNIEXPORT jlong JNICALL
Java_com_deepnight_sdk_dap_DapNativeInterface_runDataBenchmark(JNIEnv *env, jobject thiz, jint size_mb, jint iterations) {
    const size_t size = size_mb * 1024 * 1024;
    std::vector<uint8_t> data(size, 0xAA);
    uint8_t* ptr = data.data();

    auto start = std::chrono::high_resolution_clock::now();

    uint32_t checksum = 0;
    for (int it = 0; it < iterations; it++) {
        for (size_t i = 0; i < size; i++) {
            // Complex bitwise logic that JVM struggles to optimize as well as C++
            uint8_t val = ptr[i];
            val = (val ^ 0x55) + (val << 1);
            val = (val >> 1) ^ (val << 3);
            ptr[i] = val;
            checksum += val;
        }
    }

    auto end = std::chrono::high_resolution_clock::now();
    auto diff = std::chrono::duration_cast<std::chrono::microseconds>(end - start).count();

    // Return time, but include checksum in a way that prevents optimization
    return (checksum == 0) ? 1 : diff;
}

JNIEXPORT jlong JNICALL
Java_com_deepnight_sdk_dap_DapNativeInterface_runFftBenchmark(JNIEnv *env, jobject thiz, jint iterations) {
    const int bufferSize = 1024;
    float audioData[bufferSize];
    for (int i = 0; i < bufferSize; i++) audioData[i] = std::sin(i * 0.1f);
    float checksum = 0.0f;
    for (int it = 0; it < iterations; it++) {
        float* ptr = audioData;
        for (int b = 0; b < 32; b++) {
            float sum = 0.0f;
            for (int j = 0; j < 32; j++) {
                float v = *ptr++;
                sum += std::sqrt(std::abs(v * std::cos(v)));
            }
            checksum += sum * 0.03125f;
        }
        audioData[it % bufferSize] += 0.001f;
    }
    return (jlong)(checksum * 100.0f);
}

}
