#include "native_jvm.hpp"

namespace native_jvm::utils {
	
	template <std::size_t N>
	void jvm_stack<N>::push2(jlong value) {
		*(jlong *)(&data[sptr]) = value;
		sptr += 2;
	}

	template <std::size_t N>
	jlong jvm_stack<N>::pop2() {
		sptr -= 2;
		return *(jlong *)(&data[sptr]);
	}

	template <std::size_t N>
	void jvm_stack<N>::push(jint value) {
		data[sptr] = value;
		sptr++;
	}

	template <std::size_t N>
	jint jvm_stack<N>::pop() {
		sptr--;
		return data[sptr];
	}

	template <std::size_t N>
	void jvm_stack<N>::pushref(jobject value) {
		refs[sptr] = value;
		sptr++;
	}

	template <std::size_t N>
	jobject jvm_stack<N>::popref() {
		sptr--;
		return refs[sptr];
	}

	template <std::size_t N>
	void jvm_stack<N>::popcnt(int cnt) {
		sptr -= cnt;
	}

	template <std::size_t N>
	jint jvm_stack<N>::fetch(int sd) {
		return data[sptr - sd - 1];
	}

	template <std::size_t N>
	jlong jvm_stack<N>::fetch2(int sd) {
		return *(jlong *)(&data[sptr - 2 * sd - 2]);
	}

	template <std::size_t N>
	jlong jvm_stack<N>::fetch2raw(int sd) {
		return *(jlong *)(&data[sptr - sd - 1]);	
	}

	template <std::size_t N>
	jobject jvm_stack<N>::fetchref(int sd) {
		return refs[sptr - sd - 1];
	}

	template <std::size_t N>
	void jvm_stack<N>::set(int sd, jint value) {
		data[sptr - sd - 1] = value;
	}

	template <std::size_t N>
	void jvm_stack<N>::set2(int sd, jlong value) {
		*(jlong *)(&data[sptr - 2 * sd - 2]) = value;
	}

	template <std::size_t N>
	void jvm_stack<N>::set2raw(int sd, jlong value) {
		*(jlong *)(&data[sptr - sd - 1]) = value;	
	}

	template <std::size_t N>
	void jvm_stack<N>::setref(int sd, jobject value) {
		refs[sptr - sd - 1] = value;
	}

	template <std::size_t N>
	jint *jvm_stack<N>::getptr(int sd) {
		return data + (sptr - sd - 1);
	}

	template <std::size_t N>
	jlong *jvm_stack<N>::getptr2(int sd) {
		return (jlong *)(data + (sptr - 2 * sd - 2));
	}

	template <std::size_t N>
	void jvm_stack<N>::clear() {
		sptr = 0;
	}

	template <std::size_t N>
    jlong local_vars<N>::get2(jint index) {
    	return *(jlong *)(&data[index]);
    }

	template <std::size_t N>
    void local_vars<N>::set2(jint index, jlong value) {
    	*(jlong *)(&data[index]) = value;
    }

	template <std::size_t N>
    jint local_vars<N>::get(jint index) {
    	return data[index];
    }

	template <std::size_t N>
    void local_vars<N>::set(jint index, jint value) {
    	data[index] = value;
    }

	template <std::size_t N>
    jobject local_vars<N>::getref(jint index) {
    	return refs[index];
    }

	template <std::size_t N>
    void local_vars<N>::setref(jint index, jobject ref) {
    	refs[index] = ref;
    }

	union __fi_conv { 
		jfloat m_jfloat; 
		jint m_jint; 
	};

	jint cfi(jfloat f) { 
		__fi_conv fi; 
		fi.m_jfloat = f;
		return fi.m_jint; 
	}

	jfloat cif(jint i) { 
		__fi_conv fi; 
		fi.m_jint = i; 
		return fi.m_jfloat; 
	}


	union __dl_conv { 
		jdouble m_jdouble; 
		jlong m_jlong; 
	};

	jlong cdl(jdouble d) { 
		__dl_conv dl; 
		dl.m_jdouble = d; 
		return dl.m_jlong;
	}

	jdouble cld(jlong l) { 
		__dl_conv dl; 
		dl.m_jlong = l; 
		return dl.m_jdouble; 
	}


	jobjectArray create_multidim_array(JNIEnv *env, jint count, jint *sizes, std::string clazz) {
		if (count == 0)
			return (jobjectArray) nullptr;
		jobjectArray resultArray = env->NewObjectArray(*sizes, env->FindClass((std::string(count, '[') + clazz).c_str()), nullptr);
		for (jint i = 0; i < *sizes; i++)
			env->SetObjectArrayElement(resultArray, i, create_multidim_array(env, count - 1, sizes + 1, clazz));
		return resultArray;
	}
}