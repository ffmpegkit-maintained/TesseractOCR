// JNI bridge between the Kotlin API (dev.ffmpegkit.tesseract.TesseractJNI) and
// Tesseract 5.x. Produces libtesseract_jni.so; Kotlin loads leptonica, tesseract,
// then tesseract_jni.
//
// Native entry points (must match TesseractJNI.kt):
//   nativeInit(dataPath, language, oem)                 -> jlong handle
//   nativeSetPageSegMode(handle, mode)                  -> void
//   nativeSetImage(handle, pixels, w, h, bpp, bpl)      -> void
//   nativeGetUTF8Text(handle)                           -> jstring
//   nativeGetMeanConfidence(handle)                     -> jint
//   nativeGetWords(handle)                              -> jstring (JSON)
//   nativeEnd(handle)                                   -> void
//   nativeGetVersion()                                  -> jstring

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <unistd.h>
#include <fcntl.h>

#include <tesseract/baseapi.h>
#include <tesseract/resultiterator.h>

#define LOG_TAG "tesseract-jni"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using tesseract::TessBaseAPI;

namespace {

// SetImage does NOT copy the pixel data — keep it alive alongside the API.
struct OcrCtx {
    TessBaseAPI api;
    std::vector<unsigned char> img;
};

std::string jstr(JNIEnv *env, jstring s) {
    if (!s) return {};
    const char *c = env->GetStringUTFChars(s, nullptr);
    std::string out = c ? c : "";
    if (c) env->ReleaseStringUTFChars(s, c);
    return out;
}

std::string json_escape(const std::string &in) {
    std::string o; o.reserve(in.size() + 8);
    for (char c : in) {
        switch (c) {
            case '"':  o += "\\\""; break;
            case '\\': o += "\\\\"; break;
            case '\n': o += "\\n";  break;
            case '\r': o += "\\r";  break;
            case '\t': o += "\\t";  break;
            default:   o += c;      break;
        }
    }
    return o;
}

} // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeInit(
        JNIEnv *env, jobject, jstring dataPath, jstring language, jint oem) {
    auto *ctx = new OcrCtx();
    std::string dp = jstr(env, dataPath);
    std::string lang = jstr(env, language);
    std::string model = dp + "/tessdata/" + lang + ".traineddata";
    LOGE("[diag] nativeInit datapath='%s' lang='%s' oem=%d model_access=%d",
         dp.c_str(), lang.c_str(), oem, access(model.c_str(), R_OK));

    // Capture Tesseract's own stderr message during Init so we see the real reason.
    int pipefd[2]; int saved = -1;
    if (pipe(pipefd) == 0) {
        saved = dup(STDERR_FILENO);
        dup2(pipefd[1], STDERR_FILENO);
        close(pipefd[1]);
        fcntl(pipefd[0], F_SETFL, O_NONBLOCK);
    }
    int rc = ctx->api.Init(dp.c_str(), lang.c_str(),
                           static_cast<tesseract::OcrEngineMode>(oem));
    if (saved != -1) {
        fflush(stderr);
        dup2(saved, STDERR_FILENO);
        close(saved);
        char buf[4096];
        ssize_t n = read(pipefd[0], buf, sizeof(buf) - 1);
        close(pipefd[0]);
        if (n > 0) { buf[n] = 0; LOGE("[diag] tesseract stderr: %s", buf); }
    }
    if (rc != 0) {
        LOGE("Tesseract Init failed (rc=%d)", rc);
        delete ctx;
        return 0;
    }
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT void JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeSetPageSegMode(
        JNIEnv *, jobject, jlong handle, jint mode) {
    auto *ctx = reinterpret_cast<OcrCtx *>(handle);
    if (ctx) ctx->api.SetPageSegMode(static_cast<tesseract::PageSegMode>(mode));
}

JNIEXPORT void JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeSetImage(
        JNIEnv *env, jobject, jlong handle, jbyteArray pixels,
        jint width, jint height, jint bpp, jint bpl) {
    auto *ctx = reinterpret_cast<OcrCtx *>(handle);
    if (!ctx) return;
    const jsize n = env->GetArrayLength(pixels);
    ctx->img.resize(n);
    env->GetByteArrayRegion(pixels, 0, n, reinterpret_cast<jbyte *>(ctx->img.data()));
    ctx->api.SetImage(ctx->img.data(), width, height, bpp, bpl);
}

JNIEXPORT jstring JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeGetUTF8Text(
        JNIEnv *env, jobject, jlong handle) {
    auto *ctx = reinterpret_cast<OcrCtx *>(handle);
    if (!ctx) return env->NewStringUTF("");
    char *text = ctx->api.GetUTF8Text();
    jstring out = env->NewStringUTF(text ? text : "");
    delete[] text;
    return out;
}

JNIEXPORT jint JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeGetMeanConfidence(
        JNIEnv *, jobject, jlong handle) {
    auto *ctx = reinterpret_cast<OcrCtx *>(handle);
    return ctx ? ctx->api.MeanTextConf() : 0;
}

JNIEXPORT jstring JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeGetWords(
        JNIEnv *env, jobject, jlong handle) {
    auto *ctx = reinterpret_cast<OcrCtx *>(handle);
    if (!ctx) return env->NewStringUTF("[]");
    std::string out = "[";
    tesseract::ResultIterator *it = ctx->api.GetIterator();
    const auto level = tesseract::RIL_WORD;
    bool first = true;
    if (it) {
        do {
            const char *w = it->GetUTF8Text(level);
            if (!w) continue;
            int x1, y1, x2, y2;
            it->BoundingBox(level, &x1, &y1, &x2, &y2);
            int conf = static_cast<int>(it->Confidence(level));
            if (!first) out += ",";
            first = false;
            out += "{\"text\":\"" + json_escape(w) + "\",\"confidence\":" + std::to_string(conf) +
                   ",\"left\":" + std::to_string(x1) + ",\"top\":" + std::to_string(y1) +
                   ",\"right\":" + std::to_string(x2) + ",\"bottom\":" + std::to_string(y2) + "}";
            delete[] w;
        } while (it->Next(level));
        delete it;
    }
    out += "]";
    return env->NewStringUTF(out.c_str());
}

JNIEXPORT void JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeEnd(JNIEnv *, jobject, jlong handle) {
    auto *ctx = reinterpret_cast<OcrCtx *>(handle);
    if (ctx) { ctx->api.End(); delete ctx; }
}

JNIEXPORT jstring JNICALL
Java_dev_ffmpegkit_tesseract_TesseractJNI_nativeGetVersion(JNIEnv *env, jobject) {
    return env->NewStringUTF(TessBaseAPI::Version());
}

} // extern "C"
