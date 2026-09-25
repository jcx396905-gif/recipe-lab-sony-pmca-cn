#include <cstdarg>
#include <cstdlib>
#include <cstring>
#include "util.hpp"

// vsnprintf is not declared in C++98, but available in the C standard library
extern "C" int vsnprintf(char *, size_t, const char *, va_list);

using namespace std;

string string_format(const char *format, ...)
{
    va_list args;
    va_start(args, format);
    size_t size = vsnprintf(NULL, 0, format, args) + 1u;
    char buf[size];
    vsnprintf(buf, size, format, args);
    return string(buf);
}

static int static_map_comp(const void *a, const void *b)
{
    return strcmp(((static_map_entry *) a)->key, ((static_map_entry *) b)->key);
}

const void *static_map_find(const char *key, const static_map_entry table[], size_t table_size)
{
    static_map_entry needle = {key, NULL};
    static_map_entry *e = (static_map_entry *) bsearch(&needle, table, table_size / sizeof(static_map_entry), sizeof(static_map_entry), static_map_comp);
    return e ? e->value : NULL;
}
