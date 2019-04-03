#include "jni.h"
#include <cmath>
#include <cstring>
#include <string>

template <std::size_t N>
struct jvm_stack {
	jobject refs[2 * N];
	jint data[2 * N];
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

union FI { jfloat m_jfloat; jint m_jint; };
jint cfi(jfloat f) { FI fi; fi.m_jfloat = f; return fi.m_jint; }
jfloat cif(jint f) { FI fi; fi.m_jint = f; return fi.m_jfloat; }
union DL { jfloat m_jdouble; jlong m_jlong; };
jlong cdl(jdouble f) { DL dl; dl.m_jdouble = f; return dl.m_jlong; }
jdouble cld(jlong f) { DL dl; dl.m_jlong = f; return dl.m_jdouble; }

jobjectArray CreateMultiDimArray(JNIEnv *env, jint count, jint *sizes, std::string clazz) {
	if (count == 0)
		return (jobjectArray) nullptr;
	jobjectArray resultArray = env->NewObjectArray(*sizes, env->FindClass((std::string(count, '[') + clazz).c_str()), nullptr);
	for (jint i = 0; i < *sizes; i++)
		env->SetObjectArrayElement(resultArray, i, CreateMultiDimArray(env, count - 1, sizes + 1, clazz));
	return resultArray;
}

