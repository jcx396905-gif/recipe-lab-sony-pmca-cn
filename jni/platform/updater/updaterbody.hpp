#pragma once

namespace UpdaterAPI
{
    namespace firmware_information_t
    {
        struct information {
            int valid;
            int version_major;
            int version_minor;
            int model;
            int region;
        };
    }
}

namespace Updater
{
    enum CALLBACK_LOCAL_ID {};
    enum UPDATER_ACTION_MODE {};

    class RingBuffer
    {
    public:
        virtual ~RingBuffer() {};
        virtual void Initialize(void *, int) = 0;
        virtual void Finalize() = 0;
        virtual void Reset() = 0;
        virtual void Stop() = 0;
        virtual bool IsEnd() = 0;
        virtual int WriteData(void *, int, bool) = 0;
        virtual int ReadData(void *, int) = 0;
        virtual int ReadDataWithoutCopy(void **, int) = 0;
        virtual int SkipData(int) = 0;
        virtual int GetDataSize() = 0;
        virtual int GetEmptySize() = 0;
    };

    class CallbackInterface
    {
    public:
        virtual ~CallbackInterface() {};
        virtual void Callback(CALLBACK_LOCAL_ID, void *, void *) = 0;
    };

    class UpdaterBody
    {
    public:
        virtual ~UpdaterBody() {};
        virtual bool Execute(RingBuffer *, CallbackInterface *) = 0;
        virtual void Stop() = 0;
    };
}

extern "C"
{
    Updater::UpdaterBody *GetBody(bool, Updater::UPDATER_ACTION_MODE, UpdaterAPI::firmware_information_t::information *);
    void ReleaseBody(Updater::UpdaterBody *);
}
