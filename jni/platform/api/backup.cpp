#include "backup.hpp"
#include "util.hpp"

extern "C"
{
    #include "drivers/backup.h"
    #include "drivers/backup_senser.h"
}

#define BASE_SINGLETON(name, id, size) BackupProperty &name() \
{ \
    static BaseBackupProperty inst(id, size); \
    return inst; \
}

#define COMPOUND_SINGLETON(name, ...) BackupProperty &name() \
{ \
    static BaseBackupProperty props[] = {__VA_ARGS__}; \
    static CompoundBackupProperty inst(from_property_list(props, sizeof(props) / sizeof(BaseBackupProperty))); \
    return inst; \
}

using namespace std;

class BaseBackupProperty : public BackupProperty
{
protected:
    const int id;
    const size_t checked_size;
public:
    BaseBackupProperty(int id, size_t checked_size) : id(id), checked_size(checked_size) {}
    virtual bool exists();
    virtual bool is_valid();
    virtual size_t get_size();
    virtual int get_attr();
    virtual vector<char> read();
    virtual void write(vector<char> value);
};

class CompoundBackupProperty : public BackupProperty
{
protected:
    vector<BackupProperty *> properties;
public:
    CompoundBackupProperty(vector<BackupProperty *> properties) : properties(properties) {}
    virtual bool exists();
    virtual bool is_valid();
    virtual size_t get_size();
    virtual int get_attr() {return 0;}
    virtual vector<char> read();
    virtual void write(vector<char> value);
};

static vector<BackupProperty *> from_property_list(BaseBackupProperty list[], size_t n)
{
    vector<BackupProperty *> vec(n);
    for (size_t i = 0; i < n; i++)
        vec[i] = list + i;
    return vec;
}

bool BaseBackupProperty::exists()
{
    return Backup_get_datasize(id) > 0;
}

bool BaseBackupProperty::is_valid()
{
    return exists() && get_size() == checked_size;
}

size_t BaseBackupProperty::get_size()
{
    int res = Backup_get_datasize(id);
    if (res <= 0)
        throw backup_error(string_format("Backup_get_datasize returned %d", res));
    return (size_t) res;
}

int BaseBackupProperty::get_attr()
{
    int res = Backup_get_attribute(id);
    if (res < 0)
        throw backup_error(string_format("Backup_get_attribute returned %d", res));
    return res;
}

vector<char> BaseBackupProperty::read()
{
    vector<char> value(get_size());
    int res = Backup_read(id, &value[0]);
    if (res < 0 || ((size_t) res) != value.size())
        throw backup_error(string_format("Backup_read returned %d", res));
    return value;
}

void BaseBackupProperty::write(vector<char> value)
{
    if (value.size() != get_size())
        throw backup_error("Wrong vector size");
    int res = Backup_write(id >> 16, id, &value[0]);
    if (res < 0 || ((size_t) res) != value.size()) {
        if (res == -BACKUP_ERROR_READ_ONLY)
            throw backup_protected_error();
        else
            throw backup_error(string_format("Backup_write returned %d", res));
    }
}

bool CompoundBackupProperty::exists()
{
    for (vector<BackupProperty *>::iterator it = properties.begin(); it != properties.end(); it++) {
        if (!(*it)->exists())
            return false;
    }
    return true;
}

bool CompoundBackupProperty::is_valid()
{
    for (vector<BackupProperty *>::iterator it = properties.begin(); it != properties.end(); it++) {
        if (!(*it)->is_valid())
            return false;
    }
    return true;
}

size_t CompoundBackupProperty::get_size()
{
    size_t size = 0;
    for (vector<BackupProperty *>::iterator it = properties.begin(); it != properties.end(); it++)
        size += (*it)->get_size();
    return size;
}

vector<char> CompoundBackupProperty::read()
{
    vector<char> value;
    value.reserve(get_size());
    for (vector<BackupProperty *>::iterator it = properties.begin(); it != properties.end(); it++) {
        vector<char> v = (*it)->read();
        value.insert(value.end(), v.begin(), v.end());
    }
    return value;
}

void CompoundBackupProperty::write(vector<char> value)
{
    if (value.size() != get_size())
        throw backup_error("Wrong vector size");
    vector<char>::iterator vit = value.begin();
    for (vector<BackupProperty *>::iterator it = properties.begin(); it != properties.end(); it++) {
        size_t size = (*it)->get_size();
        (*it)->write(vector<char>(vit, vit + size));
        vit += size;
    }
}

bool Backup_guess_protection()
{
    BaseBackupProperty bkprop_read_only_test = BaseBackupProperty(0x010d008f, 1);
    if (!bkprop_read_only_test.is_valid())
        throw backup_error("Backup protection test property not found");
    if (!(bkprop_read_only_test.get_attr() & BACKUP_ATTR_READ_ONLY))
        throw backup_error("Backup protection test property not read-only");
    vector<char> value = bkprop_read_only_test.read();
    try {
        bkprop_read_only_test.write(value);
        return false;
    } catch (const backup_protected_error &) {
        return true;
    }
}

vector<char> Backup_read_data()
{
    size_t size = BACKUP_SENSER_PRESET_DATA_MAX_SIZE;
    vector<char> data(size);
    int res = backup_senser_cmd_preset_data_read(1, &data[0], &size);
    if (res)
        throw backup_error(string_format("backup_senser_cmd_preset_data_read returned %d", res));
    if (size < 0x100 || size > BACKUP_SENSER_PRESET_DATA_MAX_SIZE)
        throw backup_error(string_format("Wrong backup data size: %d", size));
    data.resize(size);

    string version(&data[BACKUP_PRESET_DATA_OFFSET_VERSION]);
    if (version != "BK2" && version != "BK3" && version != "BK4")
        throw backup_error(string_format("Unsupported backup version: %s", version.c_str()));

    return data;
}

string Backup_get_region()
{
    return string(&Backup_read_data()[BACKUP_PRESET_DATA_OFFSET_REGION]);
}

static const char langs_all[BACKUP_NUM_LANGS] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
static const char langs_ap2[BACKUP_NUM_LANGS] = {1, 2, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
static const char langs_au2[BACKUP_NUM_LANGS] = {1, 2, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2, 1, 2, 2, 1, 1, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2};
static const char langs_ca2[BACKUP_NUM_LANGS] = {1, 2, 1, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
static const char langs_ce [BACKUP_NUM_LANGS] = {1, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2};
static const char langs_ce3[BACKUP_NUM_LANGS] = {1, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2};
static const char langs_cec[BACKUP_NUM_LANGS] = {1, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 2, 1, 2, 2, 1, 2, 2, 2, 2};
static const char langs_cn2[BACKUP_NUM_LANGS] = {1, 2, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2};
static const char langs_j1 [BACKUP_NUM_LANGS] = {2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
static const char langs_ru3[BACKUP_NUM_LANGS] = {1, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2, 2, 2, 2};
static const char langs_u2 [BACKUP_NUM_LANGS] = {1, 2, 1, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
static const char langs_uc2[BACKUP_NUM_LANGS] = {1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};

static const static_map_entry langs_map[] = {
    // Keys have to be sorted alphabetically!
    {"ALLLANG", langs_all},
    {"AP2", langs_ap2},
    {"AU2", langs_au2},
    {"CA2", langs_ca2},
    {"CE",  langs_ce },
    {"CE3", langs_ce3},
    {"CE7", langs_au2},
    {"CEC", langs_cec},
    {"CEH", langs_ce3},
    {"CN1", langs_au2},
    {"CN2", langs_cn2},
    {"E32", langs_au2},
    {"E33", langs_au2},
    {"E37", langs_au2},
    {"E38", langs_cn2},
    {"EA8", langs_au2},
    {"HK1", langs_au2},
    {"IN5", langs_ap2},
    {"J1",  langs_j1 },
    {"JE3", langs_ap2},
    {"KR2", langs_au2},
    {"RU2", langs_ce },
    {"RU3", langs_ru3},
    {"TW6", langs_au2},
    {"U2",  langs_u2 },
    {"UC2", langs_uc2},
};

const char *Backup_get_languages_for_region(const char *region)
{
    return (const char *) static_map_find(region, langs_map, sizeof(langs_map));
}

BASE_SINGLETON(bkprop_android_platform_version, 0x01660024, 8)

BASE_SINGLETON(bkprop_model_code, 0x00e70000, 5)

BASE_SINGLETON(bkprop_model_name, 0x003e0005, 16)

BASE_SINGLETON(bkprop_serial_number, 0x00e70003, 4)

COMPOUND_SINGLETON(bkprop_rec_limit,
    BaseBackupProperty(0x003c0373, 1), // H
    BaseBackupProperty(0x003c0374, 1), // M
    BaseBackupProperty(0x003c0375, 1), // S
)

BASE_SINGLETON(bkprop_rec_limit_4k, 0x003c04b6, 2)

BASE_SINGLETON(bkprop_pal_ntsc_selector, 0x01070148, 1)

COMPOUND_SINGLETON(bkprop_language,
    BaseBackupProperty(0x010d008f, 1),
    BaseBackupProperty(0x010d0090, 1),
    BaseBackupProperty(0x010d0091, 1),
    BaseBackupProperty(0x010d0092, 1),
    BaseBackupProperty(0x010d0093, 1),
    BaseBackupProperty(0x010d0094, 1),
    BaseBackupProperty(0x010d0095, 1),
    BaseBackupProperty(0x010d0096, 1),
    BaseBackupProperty(0x010d0097, 1),
    BaseBackupProperty(0x010d0098, 1),
    BaseBackupProperty(0x010d0099, 1),
    BaseBackupProperty(0x010d009a, 1),
    BaseBackupProperty(0x010d009b, 1),
    BaseBackupProperty(0x010d009c, 1),
    BaseBackupProperty(0x010d009d, 1),
    BaseBackupProperty(0x010d009e, 1),
    BaseBackupProperty(0x010d009f, 1),
    BaseBackupProperty(0x010d00a0, 1),
    BaseBackupProperty(0x010d00a1, 1),
    BaseBackupProperty(0x010d00a2, 1),
    BaseBackupProperty(0x010d00a3, 1),
    BaseBackupProperty(0x010d00a4, 1),
    BaseBackupProperty(0x010d00a5, 1),
    BaseBackupProperty(0x010d00a6, 1),
    BaseBackupProperty(0x010d00a7, 1),
    BaseBackupProperty(0x010d00a8, 1),
    BaseBackupProperty(0x010d00a9, 1),
    BaseBackupProperty(0x010d00aa, 1),
    BaseBackupProperty(0x010d00ab, 1),
    BaseBackupProperty(0x010d00ac, 1),
    BaseBackupProperty(0x010d00ad, 1),
    BaseBackupProperty(0x010d00ae, 1),
    BaseBackupProperty(0x010d00af, 1),
    BaseBackupProperty(0x010d00b0, 1),
    BaseBackupProperty(0x010d00b1, 1),
)
