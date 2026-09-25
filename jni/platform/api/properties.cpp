#include <fcntl.h>
#include <stdexcept>
#include <unistd.h>
#include <vector>

#include "backup.hpp"
#include "properties.hpp"
#include "util.hpp"

#define SINGLETON(name, type, ...) Property &name() \
{ \
    static type inst(__VA_ARGS__); \
    return inst; \
}

#define NOARG_SINGLETON(name, type) Property &name() \
{ \
    static type inst; \
    return inst; \
}

#define UNDEFINED_SINGLETON(name) NOARG_SINGLETON(name, UndefinedProperty)

using namespace std;

class UndefinedProperty : public Property
{
public:
    virtual bool is_available() {return false;}
    virtual string get_string_value() {return "";}
};

class FirmwareVersionProperty : public Property
{
public:
    virtual bool is_available();
    virtual string get_string_value();
};

bool FirmwareVersionProperty::is_available()
{
    try {
        get_string_value();
        return true;
    } catch (const runtime_error &) {
        return false;
    }
}

string FirmwareVersionProperty::get_string_value()
{
    int fd = open("/setting/updater/dat4", O_RDONLY);
    if (fd < 0)
        throw runtime_error(string_format("Open failed: %d", fd));

    unsigned char version[2];
    if (read(fd, version, sizeof(version)) != sizeof(version))
        throw runtime_error("Read failed");

    if (close(fd))
        throw runtime_error("Close failed");

    return string_format("%x.%02x", version[1], version[0]);
}

NOARG_SINGLETON(prop_firmware_version, FirmwareVersionProperty)

#ifdef API_backup

class BkProperty : public Property
{
protected:
    BackupProperty &property;
public:
    BkProperty(BackupProperty &property) : property(property) {}
    virtual bool is_available();
};

class StringBkProperty : public BkProperty
{
public:
    StringBkProperty(BackupProperty &property) : BkProperty(property) {}
    virtual string get_string_value();
};

class HexBkProperty : public BkProperty
{
public:
    HexBkProperty(BackupProperty &property) : BkProperty(property) {}
    virtual string get_string_value();
};

class BackupRegionProperty : public Property
{
public:
    virtual bool is_available();
    virtual string get_string_value();
};

bool BkProperty::is_available()
{
    return property.is_valid();
}

string StringBkProperty::get_string_value()
{
    return string(&property.read()[0]);
}

string HexBkProperty::get_string_value()
{
    vector<char> value = property.read();
    string str;
    str.reserve(2 * value.size());
    for (vector<char>::iterator it = value.begin(); it != value.end(); it++)
        str += string_format("%02x", *it);
    const char *p = str.c_str();
    while (*p == '0')
        p++;
    return string(p);
}

bool BackupRegionProperty::is_available()
{
    try {
        get_string_value();
        return true;
    } catch (const backup_error &) {
        return false;
    }
}

string BackupRegionProperty::get_string_value()
{
    return Backup_get_region();
}

SINGLETON(prop_android_platform_version, StringBkProperty, bkprop_android_platform_version())
NOARG_SINGLETON(prop_backup_region, BackupRegionProperty)
SINGLETON(prop_model_code, HexBkProperty, bkprop_model_code())
SINGLETON(prop_model_name, StringBkProperty, bkprop_model_name())
SINGLETON(prop_serial_number, HexBkProperty, bkprop_serial_number())

#else

UNDEFINED_SINGLETON(prop_android_platform_version)
UNDEFINED_SINGLETON(prop_backup_region)
UNDEFINED_SINGLETON(prop_model_code)
UNDEFINED_SINGLETON(prop_model_name)
UNDEFINED_SINGLETON(prop_serial_number)

#endif
