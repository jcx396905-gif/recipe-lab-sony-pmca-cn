package com.voxivoid.recipelab;

/** JNI binding to the camera settings store (librecipelab.so). */
public class NativeBackup {
    static { System.loadLibrary("recipelab"); }

    public static native byte[] read(int id) throws NativeException;
    public static native void write(int id, byte[] data) throws NativeException;
    public static native int attr(int id) throws NativeException;
    public static native void sync();

    public static int readByte(int id) throws NativeException {
        byte[] b = read(id);
        return b.length > 0 ? b[0] : 0;
    }
    public static void writeByte(int id, int value) throws NativeException {
        write(id, new byte[] { (byte) value });
    }
}
