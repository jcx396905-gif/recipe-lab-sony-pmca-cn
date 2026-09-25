#pragma once
#include <string>

typedef struct {
    const char *key;
    const void *value;
} static_map_entry;

std::string string_format(const char *format, ...);

const void *static_map_find(const char *key, const static_map_entry table[], size_t table_size);
