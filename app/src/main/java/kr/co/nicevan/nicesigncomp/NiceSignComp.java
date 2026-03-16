package kr.co.nicevan.nicesigncomp;

/**
 * Created by LJY on 2017-12-14.
 */

public class NiceSignComp {
    static {
        System.loadLibrary("NiceSignComp");
    }

    public native int PdaToComp(byte[] PdaSignData, byte[] CompSignData);

    public native int BmpToComp(byte[] BmpSignData, byte[] CompSignData);
}
