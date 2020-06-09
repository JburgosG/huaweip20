package com.testacuant.p20.utils;

public final class DataContainer {

    private byte[] mDocBack;
    private byte[] mDocFront;
    private static DataContainer sInstance = null;

    /**
     * Private constructor.
     */
    private DataContainer() {
    }

    /**
     * Retrieves the singleton instance.
     * @return Singleton instance of {@code DataContainer}.
     */
    public static synchronized DataContainer instance() {
        if (sInstance == null) {
            sInstance = new DataContainer();
        }
        return sInstance;
    }

    /**
     * Clears all the data.
     */
    public void clearDocData() {
        mDocBack = null;
        mDocFront = null;
    }


    public byte[] getmDocBack() {
        return mDocBack;
    }

    public void setmDocBack(byte[] mDocBack) {
        this.mDocBack = mDocBack;
    }

    public byte[] getmDocFront() {
        return mDocFront;
    }

    public void setmDocFront(byte[] mDocFront) {
        this.mDocFront = mDocFront;
    }


}
