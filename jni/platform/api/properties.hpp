#pragma once
#include <string>

class Property
{
public:
    virtual ~Property() {}
    virtual bool is_available() = 0;
    virtual std::string get_string_value() = 0;
};

Property &prop_firmware_version();
Property &prop_android_platform_version();
Property &prop_backup_region();
Property &prop_model_code();
Property &prop_model_name();
Property &prop_serial_number();
