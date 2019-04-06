#include "jni.h"
#include <cmath>
#include <cstring>
#include <string>

#ifndef NATIVE_JVM_HPP_GUARD

#define NATIVE_JVM_HPP_GUARD

namespace native_jvm::utils {
	
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

	jint cfi(jfloat f);
	jfloat cif(jint f);
	jlong cdl(jdouble f);
	jdouble cld(jlong f);

	jobjectArray create_multidim_array(JNIEnv *env, jint count, jint *sizes, std::string clazz);
}

#endif