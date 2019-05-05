#include "jni.h"
#include "jvmti.h"
#include <cmath>
#include <cstring>
#include <string>
#include <cstdio>
#include <unordered_set>
#include <mutex>

#ifndef NATIVE_JVM_HPP_GUARD

#define NATIVE_JVM_HPP_GUARD

namespace native_jvm::utils {
    
    template <std::size_t N>
    struct jvm_stack {
        jobject refs[N];
        jint data[N];
        jint sptr = 0;

        void push2(jlong value) {
            *(jlong *)(&data[sptr]) = value;
            sptr += 2;
        }

        jlong pop2() {
            sptr -= 2;
            return *(jlong *)(&data[sptr]);
        }

        void push(jint value) {
            data[sptr] = value;
            sptr++;
        }

        jint pop() {
            sptr--;
            return data[sptr];
        }

        void pushref(jobject value) {
            refs[sptr] = value;
            sptr++;
        }

        jobject popref() {
            sptr--;
            return refs[sptr];
        }

        void popcnt(int cnt) {
            sptr -= cnt;
        }

        jint fetch(int sd) {
            return data[sptr - sd - 1];
        }

        jlong fetch2(int sd) {
            return *(jlong *)(&data[sptr - 2 * sd - 2]);
        }

        jlong fetch2raw(int sd) {
            return *(jlong *)(&data[sptr - sd - 1]);    
        }

        jobject fetchref(int sd) {
            return refs[sptr - sd - 1];
        }

        void set(int sd, jint value) {
            data[sptr - sd - 1] = value;
        }

        void set2(int sd, jlong value) {
            *(jlong *)(&data[sptr - 2 * sd - 2]) = value;
        }

        void set2raw(int sd, jlong value) {
            *(jlong *)(&data[sptr - sd - 1]) = value;    
        }

        void setref(int sd, jobject value) {
            refs[sptr - sd - 1] = value;
        }

        jint *getptr(int sd) {
            return data + (sptr - sd - 1);
        }

        jlong *getptr2(int sd) {
            return (jlong *)(data + (sptr - 2 * sd - 2));
        }

        void clear() {
            sptr = 0;
        }
    };

    template <std::size_t N>
    struct local_vars {
        jobject refs[N];
        jint data[N];

        jlong get2(jint index) {
            return *(jlong *)(&data[index]);
        }

        void set2(jint index, jlong value) {
            *(jlong *)(&data[index]) = value;
        }

        jint get(jint index) {
            return data[index];
        }

        void set(jint index, jint value) {
            data[index] = value;
        }

        jobject getref(jint index) {
            return refs[index];
        }

        void setref(jint index, jobject ref) {
            refs[index] = ref;
        }
    };

    jint cfi(jfloat f);
    jfloat cif(jint f);
    jlong cdl(jdouble f);
    jdouble cld(jlong f);

    void init_utils(JNIEnv *env);

    jobjectArray create_multidim_array(JNIEnv *env, jint count, jint *sizes, const char *class_name, int line);

    jclass find_class_wo_static(JNIEnv *env, const char *class_name);

    void throw_re(JNIEnv *env, const char *exception_class, const char *error, int line);

    void bastore(JNIEnv *env, jarray array, jint index, jint value);
    jbyte baload(JNIEnv *env, jarray array, jint index);

    jlong cast_dl(jdouble value);
    jlong cast_fl(jfloat value);
    jint cast_di(jdouble value);
    jint cast_fi(jfloat value);

    void clear_refs(JNIEnv *env, std::unordered_set<jobject> &refs);

    jstring get_interned(JNIEnv *env, jstring value);
}

#endif