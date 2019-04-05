#include "jni.h"
#include <cmath>
#include <cstring>
#include <string>

namespace native_jvm::utils {
	
	template <std::size_t N>
	struct jvm_stack {

		jobject refs[2 * N];
		jint data[2 * N];
		jint sptr = 0;

		void push2(jlong value);
		void push(jint value);
		void pushref(jobject value);

		jlong pop2();
		jint pop();	
		jobject popref();

		void popcnt(int cnt);

		jint fetch(int sd);
		jlong fetch2(int sd);
		jlong fetch2raw(int sd);
		jobject fetchref(int sd);

		void set(int sd, jint value);
		void set2(int sd, jlong value);
		void set2raw(int sd, jlong value);
		void setref(int sd, jobject value);

		jint *getptr(int sd);
		jlong *getptr2(int sd);

		void clear();
	};

	template <std::size_t N>
	struct local_vars {

	    jobject refs[N];
	    jint data[N];

	    jlong get2(jint index);
	    void set2(jint index, jlong value);

	    jint get(jint index);
	    void set(jint index, jint value);

	    jobject getref(jint index);
	    void setref(jint index, jobject ref);
	};

	jint cfi(jfloat f);
	jfloat cif(jint f);
	jlong cdl(jdouble f);
	jdouble cld(jlong f);

	jobjectArray create_multidim_array(JNIEnv *env, jint count, jint *sizes, std::string clazz);
}