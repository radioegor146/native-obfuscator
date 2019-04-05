#include "native_jvm.hpp"
#include "native_jvm_output.hpp"

$includes

namespace native_jvm {

	void register_all_classes(JNIEnv *env) {
$register_code
	}
}