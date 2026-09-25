#include <cstring>
#include <sys/stat.h>
#include <unistd.h>
#include <vector>

#include "backup.hpp"
#include "tweaks.hpp"
#include "util.hpp"

extern "C"
{
    #include "drivers/backup.h"
    #include "drivers/backup_senser.h"
}

#define SINGLETON(name, type, ...) Tweak &name() \
{ \
    static type inst(__VA_ARGS__); \
    return inst; \
}

#define NOARG_SINGLETON(name, type) Tweak &name() \
{ \
    static type inst; \
    return inst; \
}

#define UNDEFINED_SINGLETON(name) NOARG_SINGLETON(name, UndefinedTweak)

using namespace std;

class UndefinedTweak : public Tweak
{
public:
    virtual bool is_available() {return false;}
    virtual bool is_enabled() {return false;}
    virtual void set_enabled(bool enabled) {}
    virtual string get_string_value() {return "";}
};

#ifdef API_backup

class BackupTweak : public Tweak
{
protected:
    BackupProperty &property;
    const bool check_value;
    string (*const to_string)(vector<char> value);
    vector<char> read_value();
    virtual vector<char> get_off_value() = 0;
    virtual vector<char> get_on_value() = 0;
public:
    BackupTweak(BackupProperty &property, bool check_value,
                string (*to_string)(vector<char> value) = NULL) :
        property(property), check_value(check_value), to_string(to_string)
        {}
    virtual bool is_available();
    virtual bool is_enabled();
    virtual void set_enabled(bool enabled);
    virtual string get_string_value();
};

class ConstantBackupTweak : public BackupTweak
{
protected:
    const vector<char> off_value;
    const vector<char> on_value;
    virtual vector<char> get_off_value() {return off_value;}
    virtual vector<char> get_on_value() {return on_value;}
public:
    ConstantBackupTweak(BackupProperty &property, bool check_value,
                        vector<char> off_value, vector<char> on_value,
                        string (*to_string)(vector<char> value) = NULL) :
        BackupTweak(property, check_value, to_string), off_value(off_value), on_value(on_value)
        {}
};

class LanguageBackupTweak : public BackupTweak
{
protected:
    virtual vector<char> get_off_value();
    virtual vector<char> get_on_value();
public:
    LanguageBackupTweak(BackupProperty &property) : BackupTweak(property, false) {}
    virtual bool is_available();
    virtual string get_string_value();
};

class ProtectionTweak : public Tweak
{
private:
    void cleanup_android();
public:
    ProtectionTweak() {}
    virtual bool is_available();
    virtual bool is_enabled();
    virtual void set_enabled(bool enabled);
    virtual string get_string_value();
};

vector<char> BackupTweak::read_value()
{
    return property.read();
}

bool BackupTweak::is_available()
{
    if (!property.is_valid())
        return false;
    if (check_value) {
        vector<char> value = read_value();
        if (value != get_off_value() && value != get_on_value())
            return false;
    }
    return true;
}

bool BackupTweak::is_enabled()
{
    return read_value() == get_on_value();
}

void BackupTweak::set_enabled(bool enabled)
{
    property.write(enabled ? get_on_value() : get_off_value());
}

string BackupTweak::get_string_value()
{
    if (to_string)
        return to_string(read_value());
    else
        return "";
}

vector<char> LanguageBackupTweak::get_off_value()
{
    string region = Backup_get_region();
    const char *c = strchr(region.c_str(), '_');
    if (!c)
        throw tweak_error(string_format("No underscore in region: %s", region.c_str()));
    const char *langs = Backup_get_languages_for_region(c + 1);
    if (!langs)
        throw tweak_error(string_format("Unknown backup region: %s", region.c_str()));
    return vector<char>(langs, langs + BACKUP_NUM_LANGS);
}

vector<char> LanguageBackupTweak::get_on_value()
{
    const char *langs = Backup_get_languages_for_region("ALLLANG");
    return vector<char>(langs, langs + BACKUP_NUM_LANGS);
}

bool LanguageBackupTweak::is_available()
{
    vector<char> value = read_value();
    for (vector<char>::iterator it = value.begin(); it != value.end(); it++) {
        if (*it != BACKUP_LANG_DISABLED && *it != BACKUP_LANG_ENABLED)
            return false;
    }
    return BackupTweak::is_available();
}

string LanguageBackupTweak::get_string_value()
{
    vector<char> value = read_value();
    size_t count = 0;
    for (vector<char>::iterator it = value.begin(); it != value.end(); it++) {
        if (*it == BACKUP_LANG_ENABLED)
            count++;
    }
    return string_format("%d / %d languages activated", count, value.size());
}

bool ProtectionTweak::is_available()
{
    try {
        is_enabled();
        return true;
    } catch (const backup_error &) {
        return false;
    }
}

bool ProtectionTweak::is_enabled()
{
    return !Backup_guess_protection();
}

void ProtectionTweak::cleanup_android()
{
    unlink("/setting/Backup.bin");
    unlink("/setting/Backup.bak");
    rmdir("/setting");
    if (!access("/setting", F_OK))
        throw tweak_error("Cannot delete backup data directory");
}

void ProtectionTweak::set_enabled(bool enabled)
{
#ifdef MODE_ANDROID
    // Since android is running in a chrooted environment, /setting is
    // interpreted as /android/setting, a folder which doesn't exist. On
    // android 2, we have write access to /android and we can create that
    // folder. On android 4, /android is read-only and mkdir will fail.
    cleanup_android();
    mkdir("/setting", 0777);
    // The following call writes the settings to /android/setting/Backup.bin
    // and /android/setting/Backup.bak on android 2, but fails silently on
    // android 4:
#endif

    // Save the current settings to the disk:
    Backup_sync_all();

    // Let's try to set the protection flag.
    // On android 2, this call needs /android/setting/Backup.bin to exist,
    // otherwise the flag isn't set. On android 4, the flag set even if
    // Backup.bin doesn't exist.
    // On Japanese-only cameras (region J1), protection can only be enabled
    // using this method, disabling it is impossible.
    backup_senser_cmd_ID1(!enabled, NULL);

#ifdef MODE_ANDROID
    // On android 2, delete the /android/setting folder again.
    cleanup_android();
#endif

    // Since we ignored all errors above, let's check if the call succeeded.
    if (is_enabled() != enabled) {
#ifndef MODE_ANDROID
        // Try advanced mode for Japanese cameras
        tweak_protection_advanced().set_enabled(enabled);
#else
        throw tweak_error("Failed to set backup protection");
#endif
    }
}

string ProtectionTweak::get_string_value()
{
    return is_enabled() ? "Protection disabled" : "Protection enabled";
}

static string rec_limit_to_string(vector<char> value)
{
    return string_format("%dh %02dm %02ds", value[0], value[1], value[2]);
}
static const char tweak_rec_limit_off_value[] = {0, 29, 50};// 29m50s
static const char tweak_rec_limit_on_value[] = {13, 1, 0};// 13h01m00s
// The recording limit is converted to 90KHz. The theoretical limit is thus
// 13h15m21s (just before a 32bit unsigned overflow). However, all camcorders
// have this value set to 13h01m00s, so let's just use that.
SINGLETON(tweak_rec_limit, ConstantBackupTweak,
    bkprop_rec_limit(),
    false,
    vector<char>(tweak_rec_limit_off_value, tweak_rec_limit_off_value + sizeof(tweak_rec_limit_off_value)),
    vector<char>(tweak_rec_limit_on_value, tweak_rec_limit_on_value + sizeof(tweak_rec_limit_on_value)),
    rec_limit_to_string
)

static string rec_limit_4k_to_string(vector<char> value)
{
    int limit = *(short *) &value[0];
    int hours = limit / 3600;
    int minutes = (limit - (hours * 3600)) / 60;
    int seconds = limit % 60;
    return string_format("%dh %02dm %02ds", hours, minutes, seconds);
}
static const char tweak_rec_limit_4k_off_value[] = {0x2c, 0x01};// 5m00s
static const char tweak_rec_limit_4k_on_value[] = {0xff, 0x7f};// 9h06m07s
SINGLETON(tweak_rec_limit_4k, ConstantBackupTweak,
    bkprop_rec_limit_4k(),
    false,
    vector<char>(tweak_rec_limit_4k_off_value, tweak_rec_limit_4k_off_value + sizeof(tweak_rec_limit_4k_off_value)),
    vector<char>(tweak_rec_limit_4k_on_value, tweak_rec_limit_4k_on_value + sizeof(tweak_rec_limit_4k_on_value)),
    rec_limit_4k_to_string
)

SINGLETON(tweak_language, LanguageBackupTweak, bkprop_language())

SINGLETON(tweak_pal_ntsc_selector, ConstantBackupTweak,
    bkprop_pal_ntsc_selector(),
    true,
    vector<char>(1, 0),
    vector<char>(1, 1)
)

NOARG_SINGLETON(tweak_protection, ProtectionTweak)

#ifndef MODE_ANDROID
class AdvancedProtectionTweak : public ProtectionTweak
{
private:
    string patch_region(string new_region);
public:
    AdvancedProtectionTweak() {}
    virtual void set_enabled(bool enabled);
};

string AdvancedProtectionTweak::patch_region(string new_region)
{
    vector<char> data = Backup_read_data();
    char *region = &data[BACKUP_PRESET_DATA_OFFSET_REGION];
    string old_region = string(region);
    strcpy(region, new_region.c_str());

    int res = Backup_protect(0, &data[0], data.size());
    if (res)
        throw backup_error(string_format("Backup_protect returned %d", res));

    return old_region;
}

void AdvancedProtectionTweak::set_enabled(bool enabled)
{
    Backup_sync_all();

    string region = patch_region("");
    backup_senser_cmd_ID1(!enabled, NULL);
    patch_region(region);

    if (is_enabled() != enabled)
        throw tweak_error("Failed to set backup protection");
}

NOARG_SINGLETON(tweak_protection_advanced, AdvancedProtectionTweak)
#else
UNDEFINED_SINGLETON(tweak_protection_advanced)
#endif
#else

UNDEFINED_SINGLETON(tweak_rec_limit)
UNDEFINED_SINGLETON(tweak_rec_limit_4k)
UNDEFINED_SINGLETON(tweak_language)
UNDEFINED_SINGLETON(tweak_pal_ntsc_selector)
UNDEFINED_SINGLETON(tweak_protection)
UNDEFINED_SINGLETON(tweak_protection_advanced)

#endif
