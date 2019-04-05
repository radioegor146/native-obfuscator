#include "native_jvm.hpp"
#include "native_output.hpp"

$includes

namespace native_jvm {

	void register_all_classes(JNIENV *env) {
$register_code
	}
}