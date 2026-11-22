/*
 * Emitted runtime of native-obfuscator.
 *
 * Licensed under GPL-3.0 WITH an Output Exception: you may link, embed,
 * compile and distribute this file in your own programs under terms of
 * your choice. See the "Output Exception" section in the project's
 * LICENSE for the full terms.
 *
 * https://github.com/radioegor146/native-obfuscator#licensing
 */

#include "jni.h"

// Fix https://developercommunity.visualstudio.com/t/Access-violation-in-_Thrd_yield-after-up/10664660#T-N10668856
// Thanks, microsoft!
#define _DISABLE_CONSTEXPR_MUTEX_CONSTRUCTOR

#include <cmath>
#include <cstdint>
#include <cstring>
#include <limits>
#include <string>
#include <cstdio>
#include <unordered_map>
#include <mutex>
#include <initializer_list>

#ifndef NATIVE_JVM_HPP_GUARD

#define NATIVE_JVM_HPP_GUARD

namespace native_jvm::utils {

    // JVM conversion truncates toward zero and saturates beyond the signed range.
    template <typename Integer, typename Float>
    Integer float_to_integer(Float value) {
        if (std::isnan(value)) {
            return 0;
        }
        constexpr Integer minimum = (std::numeric_limits<Integer>::min)();
        constexpr Float lower_bound = static_cast<Float>(minimum);
        if (value <= lower_bound) {
            return minimum;
        }
        if (value >= -lower_bound) {
            return (std::numeric_limits<Integer>::max)();
        }
        return static_cast<Integer>(value);
    }

    void init_utils(JNIEnv *env);

    void throw_re(JNIEnv *env, const char *exception_class, const char *error, int line);

    jarray create_multidim_array(JNIEnv *env, jobject classloader, const char *descriptor, int line,
        std::initializer_list<jint> sizes);

#ifdef USE_HOTSPOT
    jobject link_call_site(JNIEnv *env, jobject caller_obj, jobject bootstrap_method_obj,
            jobject name_obj, jobject type_obj, jobject static_arguments, jobject appendix_result);
#endif

    jclass find_class_wo_static(JNIEnv *env, jobject classloader, jstring class_name);

    jclass get_class_from_object(JNIEnv *env, jobject object);

    jobject get_classloader_from_class(JNIEnv *env, jclass clazz);

    jobject get_lookup(JNIEnv *env, jclass clazz);

    void bastore(JNIEnv *env, jarray array, jint index, jint value);
    jbyte baload(JNIEnv *env, jarray array, jint index);

    class local_refs {
        std::unordered_map<jobject, bool> tracked;

    public:
        void add(jobject ref) {
            if (ref != nullptr) {
                tracked.try_emplace(ref, false);
            }
        }

        void keep_live(JNIEnv *env, std::initializer_list<jobject> live) {
            if (tracked.empty()) {
                return;
            }
            for (jobject ref : live) {
                auto entry = tracked.find(ref);
                if (entry != tracked.end()) {
                    entry->second = true;
                }
            }
            for (auto entry = tracked.begin(); entry != tracked.end();) {
                if (entry->second) {
                    entry->second = false;
                    ++entry;
                } else {
                    env->DeleteLocalRef(entry->first);
                    entry = tracked.erase(entry);
                }
            }
        }
    };

    jstring get_interned(JNIEnv *env, jstring value);
}

#endif
