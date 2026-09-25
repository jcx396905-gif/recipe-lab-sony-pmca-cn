#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <stdexcept>
#include <vector>
#include <jni.h>

#include "api/backup.hpp"

extern "C"
{
    #include "drivers/backup.h"
}

using namespace std;

/*
 * Generic settings-store access for com.voxivoid.recipelab.NativeBackup (same driver path as OpenMemories-Tweak).
 * Single settings bytes only; originals are known; firmware is never touched.
 */

static void throw_native(JNIEnv *env, const char *msg)
{
    jclass c = env->FindClass("com/voxivoid/recipelab/NativeException");
    if (c) env->ThrowNew(c, msg);
}

static int prop_size(int id)
{
    int res = Backup_get_datasize(id);
    if (res <= 0) throw backup_error("Backup_get_datasize failed");
    return res;
}

extern "C" JNIEXPORT jbyteArray Java_com_voxivoid_recipelab_NativeBackup_read(JNIEnv *env, jclass clazz, jint id)
{
    jbyteArray arr = NULL;
    try {
        int size = prop_size(id);
        vector<char> v(size);
        int res = Backup_read(id, &v[0]);
        if (res < 0 || res != size) throw backup_error("Backup_read failed");
        arr = env->NewByteArray(size);
        env->SetByteArrayRegion(arr, 0, size, (const jbyte *) &v[0]);
    } catch (const runtime_error &e) {
        throw_native(env, e.what());
    }
    return arr;
}

extern "C" JNIEXPORT void Java_com_voxivoid_recipelab_NativeBackup_write(JNIEnv *env, jclass clazz, jint id, jbyteArray data)
{
    try {
        jsize n = env->GetArrayLength(data);
        int size = prop_size(id);
        if (n != size) throw backup_error("size mismatch");
        vector<char> v(n);
        env->GetByteArrayRegion(data, 0, n, (jbyte *) &v[0]);
        int res = Backup_write(id >> 16, id, &v[0]);
        if (res == -BACKUP_ERROR_READ_ONLY) throw backup_protected_error();
        if (res < 0 || res != size) throw backup_error("Backup_write failed");
    } catch (const runtime_error &e) {
        throw_native(env, e.what());
    }
}

extern "C" JNIEXPORT jint Java_com_voxivoid_recipelab_NativeBackup_attr(JNIEnv *env, jclass clazz, jint id)
{
    int res = Backup_get_attribute(id);
    if (res < 0) throw_native(env, "Backup_get_attribute failed");
    return (jint) res;
}

extern "C" JNIEXPORT void Java_com_voxivoid_recipelab_NativeBackup_sync(JNIEnv *env, jclass clazz)
{
    Backup_sync_all();
}
