#include "string_pool.hpp"

namespace native_jvm::string_pool {
    static char pool[$size] = $value;

    char *get_pool() {
        return pool;
    }
}