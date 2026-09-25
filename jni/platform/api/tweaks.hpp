#pragma once
#include <stdexcept>
#include <string>

class tweak_error : public std::runtime_error
{
public:
    tweak_error(const std::string& msg) : std::runtime_error(msg) {}
};

class Tweak
{
public:
    virtual ~Tweak() {}
    virtual bool is_available() = 0;
    virtual bool is_enabled() = 0;
    virtual void set_enabled(bool enabled) = 0;
    virtual std::string get_string_value() = 0;
};

Tweak &tweak_rec_limit();
Tweak &tweak_rec_limit_4k();
Tweak &tweak_language();
Tweak &tweak_pal_ntsc_selector();
Tweak &tweak_protection();
Tweak &tweak_protection_advanced();
