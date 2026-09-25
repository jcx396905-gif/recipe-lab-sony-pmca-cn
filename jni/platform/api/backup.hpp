#pragma once
#include <stdexcept>
#include <string>
#include <vector>

class backup_error : public std::runtime_error
{
public:
    backup_error(const std::string& msg) : std::runtime_error(msg) {}
};

class backup_protected_error : public backup_error
{
public:
    backup_protected_error() : backup_error("Protection enabled") {}
};

class BackupProperty
{
public:
    virtual ~BackupProperty() {}
    virtual bool exists() = 0;
    virtual bool is_valid() = 0;
    virtual size_t get_size() = 0;
    virtual int get_attr() = 0;
    virtual std::vector<char> read() = 0;
    virtual void write(std::vector<char>) = 0;
};

bool Backup_guess_protection();
std::vector<char> Backup_read_data();
std::string Backup_get_region();
const char *Backup_get_languages_for_region(const char *region);

#define BACKUP_NUM_LANGS 35
#define BACKUP_LANG_ENABLED 1
#define BACKUP_LANG_DISABLED 2

BackupProperty &bkprop_android_platform_version();
BackupProperty &bkprop_model_code();
BackupProperty &bkprop_model_name();
BackupProperty &bkprop_serial_number();
BackupProperty &bkprop_rec_limit();
BackupProperty &bkprop_rec_limit_4k();
BackupProperty &bkprop_pal_ntsc_selector();
BackupProperty &bkprop_language();
