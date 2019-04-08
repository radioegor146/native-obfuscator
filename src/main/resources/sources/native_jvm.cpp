#include "native_jvm.hpp"

namespace native_jvm::utils {

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


	jobjectArray create_multidim_array(JNIEnv *env, jint count, jint *sizes, std::string className, int line) {
		if (count == 0)
			return (jobjectArray) nullptr;
		if (*sizes < 0) {
			throw_re(env, "java/lang/NegativeArraySizeException", "MULTIANEWARRAY size < 0", line);
			return (jobjectArray) nullptr;
		}
		jobjectArray resultArray = nullptr;
		if (jclass clazz = env->FindClass((std::string(count, '[') + className).c_str()))
			resultArray = env->NewObjectArray(*sizes, clazz, nullptr);
		else
			return (jobjectArray) nullptr;
		for (jint i = 0; i < *sizes; i++) {
			env->SetObjectArrayElement(resultArray, i, create_multidim_array(env, count - 1, sizes + 1, className, line));
			if (env->ExceptionCheck())
				return (jobjectArray) nullptr;
		}
		return resultArray;
	}

	jclass find_class_wo_static(JNIEnv *env, std::string class_name) {
		jclass threadClass = env->FindClass("java/lang/Thread");
		return (jclass) env->CallObjectMethod(
			env->CallObjectMethod(
				env->CallStaticObjectMethod(
					threadClass, 
					env->GetStaticMethodID(threadClass, "currentThread", "()Ljava/lang/Thread;")
				), 
				env->GetMethodID(threadClass, "getContextClassLoader", "()Ljava/lang/ClassLoader;")
			),
			env->GetMethodID(env->FindClass("java/lang/ClassLoader"), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;"),
			env->NewStringUTF(class_name.c_str())
		);
	}

	void throw_re(JNIEnv *env, std::string exception_class, std::string error, int line) {
		env->ThrowNew(env->FindClass(exception_class.c_str()), ("\"" + error + "\" on " + std::to_string(line)).c_str());
	}
}