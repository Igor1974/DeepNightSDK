#include <jni.h>
#include <string>
#include <vector>
#include <algorithm>
#include <sstream>
#include <map>

std::string toLowerCase(const std::string& str) {
    std::string result = str;
    std::transform(result.begin(), result.end(), result.begin(), [](unsigned char c){ return std::tolower(c); });
    return result;
}

std::string stemRussianWord(const std::string& word) {
    if (word.length() < 6) return word;

    std::string result = word;
    static const std::vector<std::string> endings = {
        "иями", "ями", "ами", "ие", "ия", "ья", "ов", "ев", "ам", "ем", "ом", "ах", "их", "ых",
        "ый", "ий", "ой", "ая", "яя", "ое", "ее", "ы", "и", "а", "я", "о", "е", "у", "ь"
    };

    for (const auto& end : endings) {
        if (result.length() > end.length() + 4) {
            if (result.compare(result.length() - end.length(), end.length(), end) == 0) {
                return result.substr(0, result.length() - end.length());
            }
        }
    }
    return result;
}

std::string toPhonetic(const std::string& str) {
    const std::string result = toLowerCase(str);
    static std::map<std::string, std::string> phoneticMap = {
        {"о", "а"}, {"е", "и"}, {"ё", "и"}, {"э", "и"}, {"ы", "и"}, {"я", "а"}, {"ю", "у"},
        {"б", "п"}, {"в", "ф"}, {"г", "к"}, {"д", "т"}, {"ж", "ш"}, {"з", "с"}
    };

    std::string phonetic;
    for (size_t i = 0; i < result.length(); ) {
        const auto c = static_cast<unsigned char>(result[i]);
        if (c >= 0x80) {
            if (i + 1 < result.length()) {
                const std::string utf8_char = result.substr(i, 2);
                if (phoneticMap.count(utf8_char)) {
                    phonetic += phoneticMap[utf8_char];
                } else {
                    phonetic += utf8_char;
                }
                i += 2;
            } else {
                i += 1;
            }
        } else {
            if (isalnum(c)) {
                phonetic += static_cast<char>(c);
            }
            i += 1;
        }
    }
    return phonetic;
}

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_stemWord(JNIEnv *env, jobject thiz, jstring word) {
    const char *nativeWord = env->GetStringUTFChars(word, nullptr);
    std::string result = stemRussianWord(std::string(nativeWord));
    env->ReleaseStringUTFChars(word, nativeWord);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_toPhonetic(JNIEnv *env, jobject thiz, jstring str) {
    const char *nativeStr = env->GetStringUTFChars(str, nullptr);
    std::string result = toPhonetic(std::string(nativeStr));
    env->ReleaseStringUTFChars(str, nativeStr);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jint JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_calculateMatchScore(
        JNIEnv *env, jobject thiz,
        jstring title1, jstring phonetic1, jint year1,
        jstring title2, jstring phonetic2, jint year2) {

    const char *nt1 = env->GetStringUTFChars(title1, nullptr);
    const char *np1 = env->GetStringUTFChars(phonetic1, nullptr);
    const char *nt2 = env->GetStringUTFChars(title2, nullptr);
    const char *np2 = env->GetStringUTFChars(phonetic2, nullptr);

    std::string t1 = toLowerCase(std::string(nt1));
    std::string p1 = std::string(np1);
    std::string t2 = toLowerCase(std::string(nt2));
    std::string p2 = std::string(np2);

    int score = 0;
    if (t1 == t2 && !t1.empty()) {
        score += 150;
    } else if (!t1.empty() && !t2.empty()) {
        if (t1.find(t2) != std::string::npos || t2.find(t1) != std::string::npos) {
            score += 100;
        }
    }

    if (score < 100 && !p1.empty() && !p2.empty()) {
        if (p1 == p2 || p1.find(p2) != std::string::npos || p2.find(p1) != std::string::npos) {
            score += 70;
        }
    }

    if (year1 > 0 && year2 > 0) {
        if (year1 == year2) score += 100;
        else if (std::abs(year1 - year2) <= 1) score += 50;
        else score -= 30;
    }

    env->ReleaseStringUTFChars(title1, nt1);
    env->ReleaseStringUTFChars(phonetic1, np1);
    env->ReleaseStringUTFChars(title2, nt2);
    env->ReleaseStringUTFChars(phonetic2, np2);

    return score;
}

JNIEXPORT jstring JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_cleanTitle(JNIEnv *env, jobject thiz, jstring title) {
    const char *nativeTitle = env->GetStringUTFChars(title, nullptr);
    std::string result = toLowerCase(std::string(nativeTitle));

    std::string cleaned;
    int bracketLevel = 0;
    for (const char c : result) {
        if (c == '[' || c == '(') bracketLevel++;
        else if (c == ']' || c == ')') {
            if (bracketLevel > 0) bracketLevel--;
        } else if (bracketLevel == 0) {
            cleaned += c;
        }
    }

    static const std::vector<std::string> redundant = {
        "сезон", "серия", "полная", "версия", "перевод", "дубляж", "лицензия", "itunes", "звук",
        "1080p", "720p", "2160p", "4k", "fhd", "uhd", "web-dl", "bluray", "bdrip", "dvdrip"
    };

    for (const auto& word : redundant) {
        size_t pos = 0;
        while ((pos = cleaned.find(word, pos)) != std::string::npos) {
            cleaned.replace(pos, word.length(), " ");
            pos += 1;
        }
    }

    std::string finalStr;
    bool lastWasSpace = false;
    for (const auto c : cleaned) {
        const auto uc = static_cast<unsigned char>(c);
        if ((uc >= 'a' && uc <= 'z') || (uc >= '0' && uc <= '9') || uc > 127) {
            finalStr += static_cast<char>(uc);
            lastWasSpace = false;
        } else if (!lastWasSpace) {
            finalStr += ' ';
            lastWasSpace = true;
        }
    }

    env->ReleaseStringUTFChars(title, nativeTitle);
    return env->NewStringUTF(finalStr.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_extractQuality(JNIEnv *env, jobject thiz, jstring title) {
    const char *nativeTitle = env->GetStringUTFChars(title, nullptr);
    std::string t = std::string(nativeTitle);
    std::transform(t.begin(), t.end(), t.begin(), ::toupper);

    std::string quality = "";
    if (t.find("2160") != std::string::npos || t.find("4K") != std::string::npos || t.find("UHD") != std::string::npos) quality = "4K UHD";
    else if (t.find("BDREMUX") != std::string::npos || t.find("REMUX") != std::string::npos) quality = "REMUX";
    else if (t.find("1080") != std::string::npos || t.find("FHD") != std::string::npos) quality = "1080p";
    else if (t.find("720") != std::string::npos || t.find("HD") != std::string::npos) quality = "720p";
    else if (t.find("WEB-DL") != std::string::npos || t.find("WEBRIP") != std::string::npos) quality = "WEB-DL";

    env->ReleaseStringUTFChars(title, nativeTitle);
    return env->NewStringUTF(quality.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_extractYear(JNIEnv *env, jobject thiz, jstring title) {
    const char *nativeTitle = env->GetStringUTFChars(title, nullptr);
    std::string s = std::string(nativeTitle);
    std::string year = "";
    for (size_t i = 0; i + 3 < s.length(); ++i) {
        if (isdigit(s[i]) && isdigit(s[i+1]) && isdigit(s[i+2]) && isdigit(s[i+3])) {
            std::string possible = s.substr(i, 4);
            if (possible.find("19") == 0 || possible.find("20") == 0) {
                year = possible;
                break;
            }
        }
    }
    env->ReleaseStringUTFChars(title, nativeTitle);
    return env->NewStringUTF(year.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_stemHugeBlock(JNIEnv *env, jobject thiz, jstring block) {
    const char *nativeBlock = env->GetStringUTFChars(block, nullptr);
    std::stringstream input(nativeBlock);
    std::stringstream output;
    std::string word;

    while (input >> word) {
        output << stemRussianWord(word) << " ";
    }

    env->ReleaseStringUTFChars(block, nativeBlock);
    return env->NewStringUTF(output.str().c_str());
}

JNIEXPORT jlong JNICALL
Java_com_deepnight_sdk_text_TextToolsNative_runHeavyBenchmark(JNIEnv *env, jobject thiz, jint iterations) {
    auto start = std::chrono::high_resolution_clock::now();
    double dummy = 0.0;
    // More complex math to prevent easy JIT optimization
    for (int i = 0; i < iterations; i++) {
        double x = (double)i * 0.001;
        dummy += std::sin(x) * std::cos(x * 1.5) + std::sqrt(std::abs(std::tan(x * 0.5)));
        if (dummy > 1e10) dummy = 0.0; // Prevent overflow
    }
    auto end = std::chrono::high_resolution_clock::now();
    return std::chrono::duration_cast<std::chrono::microseconds>(end - start).count();
}

}
