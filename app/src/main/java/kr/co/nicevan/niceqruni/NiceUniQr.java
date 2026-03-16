package kr.co.nicevan.niceqruni;

public class NiceUniQr {

    static {
        System.loadLibrary("NiceUniQr");
    }

    public native int Uni_QR(byte[] Input_QR, byte[] Output_AID_FIRST, byte[] Output_PAN, byte[] Output_EMV_NICE, byte[] Output_TLV, byte[] Output_UPLAN);
}
