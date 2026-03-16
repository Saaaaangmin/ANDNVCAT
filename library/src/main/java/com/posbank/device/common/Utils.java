package com.posbank.device.common;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.posbank.device.common.AscII.CH_SPACE;


public class Utils {

    /**
     * Returns the current time in milliseconds.
     * @return : current time
     */
    public static long GetStartTimeTick() {
        return System.currentTimeMillis();
    }

    /**
     * TimeOut Checker
     * @param startTimeTick :
     * @param timeOverTime :
     * @return true/false
     */
    public static boolean CheckTickTimeOut(long startTimeTick, long timeOverTime) {
        long currentTime = System.currentTimeMillis();
        return currentTime < (startTimeTick + timeOverTime);
    }

    /**
     * 전문관리번호 : 거래일자(yyMMdd) + 일련번호(6)
     * @param iTransactionNo : 일련번호
     * @return : byte[] 거래일자(yyMMdd) + 일련번호(6)
     */
    public static byte[] MakeMsgManagementNo(int iTransactionNo) {
        // 거래일자(yyMMdd)
        SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd", Locale.KOREA);

        // 일련번호(6)
        DecimalFormat df000000 = new DecimalFormat("000000");
        String sNum = df000000.format(iTransactionNo);

        return (yyMMdd.format(new Date()) + sNum).getBytes();
    }

    /**
     * 전문전송일자
     * Get Current Date and Time
     * @return : byte[]YYMMDD
     */
    public static byte[] MakeMsgDateTime_yyMMdd() {
        SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd", Locale.KOREA);
        return yyMMdd.format(new Date()).getBytes();
    }

    /**
     * 전문전송일자
     * Get Current Date and Time
     * @return : byte[]YYMMDDhhmmss
     */
    public static byte[] MakeMsgDateTime_yyMMddHHmmss() {
        SimpleDateFormat yyMMddHHmmss = new SimpleDateFormat("yyMMddHHmmss", Locale.KOREA);
        return yyMMddHHmmss.format(new Date()).getBytes();
    }

    /**
     * 전문전송일자
     * Get Current Date and Time
     * @return : (String)YYYYMMDDhhmmss
     */
    public static String GetCurrentDateTime() {
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        return yyyyMMddHHmmss.format(new Date());
    }

    /**
     * ByteBuffer -> Array 데이타 복사(End 문자까지)
     * @param dest      array
     * @param src       ByteBuffer
     * @param size      최대사이즈
     * @param endChar   End char
     * @return          Copy length
     */
    public static int ArrayCopyUntilChar(byte[] dest, ByteBuffer src, int size, byte endChar) {
        if (src.remaining() == 0) return 0;
        int i;
        for(i=0; i<size; i++) {
            int pos = src.position();       // ByteBuffer 현재 position
            byte data = src.get(pos);       // 절대적 메소드
            if(data == endChar) break;
            dest[i] = data;
            src.position(pos+1);            // position 증가
        }
        return i;
    }

    /**
     * ByteBuffer -> Array 데이타 복사(Size 만큼)
     * @param dest      array
     * @param src       ByteBuffer
     * @param size      사이즈
     * @return          Copy length
     */
    public static int ArrayCopyAsSize(byte[] dest, ByteBuffer src, int size) {
        if (src.remaining() < size) return 0;
        int i;
        for(i=0; i<size; i++) {
            int pos = src.position();       // ByteBuffer 현재 position
            byte data = src.get(pos);       // 절대적 메소드
            dest[i] = data;
            src.position(pos+1);            // position 증가
        }
        return i;
    }

    /**
     * Byte ArrayCopy byte[len] -> byte[size]
     * Left Alignment, Space Padding
     * @param src : byte Array
     * @param size : length
     * @return : byte Array
     */
    public static byte[] ArrayCopyAndFillSpace(byte[] src, int size) {
        byte[] oSrc = new byte[size];
        int srcLength = new String(src).trim().length();
        if(srcLength > size) {
            srcLength = size;
        }
        // 데이타 복사
        System.arraycopy(src, 0, oSrc, 0, srcLength);
        // Fill Space Padding
        for(int i = 0; i < (size-srcLength); i++) {
            oSrc[i+srcLength] = CH_SPACE;
        }

        return oSrc;
    }

    /**
     * Get Bytes String -> byte[]
     * Left Alignment, Space Padding
     * @param src : String
     * @param size : length
     * @return : byte Array
     */
    public static byte[] GetBytesAndFillSpace(String src, int size) {
        byte[] oSrc = new byte[size];
        byte[] baSrc;

        try {
            baSrc = src.getBytes("EUC-KR");
        } catch (UnsupportedEncodingException e) {
            for (int i=0; i<size; i++) {
                oSrc[i] = CH_SPACE;
            }
            return oSrc;
        }

        int baSrcLen = baSrc.length;

        if (baSrcLen == size) {
            return baSrc;
        } else if(baSrcLen > size) {
            System.arraycopy(baSrc, 0, oSrc, 0, size);
        } else {
            System.arraycopy(baSrc, 0, oSrc, 0, baSrcLen);
            for (int i = 0; i < (size-baSrcLen); i++) {
                oSrc[i+baSrcLen] = CH_SPACE;
            }
        }

        return oSrc;
    }

    /**
     * Leading Zeros int -> ASCII
     * @param iValue : Integer Value
     * @param size : length
     * @return : byte Array
     */
    public static byte[] LeadingZerosItoA(int iValue, int size) {
        char[] patternArr = new char[size];

        for (int i=0; i<size; i++) {
            patternArr[i] = '0';
        }

        String sPattern = new String(patternArr, 0, size);
        DecimalFormat df = new DecimalFormat(sPattern);

        String sValue = df.format(iValue);

        return sValue.getBytes();
    }

    /**
     * Leading Zeros Long -> ASCII
     * @param lValue : Long Value
     * @param size : length
     * @return : byte Array
     */
    public static byte[] LeadingZerosLtoA(long lValue, int size) {
        char[] patternArr = new char[size];

        for (int i=0; i<size; i++) {
            patternArr[i] = '0';
        }

        String sPattern = new String(patternArr, 0, size);
        DecimalFormat df = new DecimalFormat(sPattern);

        String sValue = df.format(lValue);

        return sValue.getBytes();
    }

    /**
     * Leading Zeros String -> String
     * @param src : String Value
     * @param size : length
     * @return : String
     */
    public static String LeadingZerosString(String src, int size) {
        StringBuilder sb = new StringBuilder();
        char[] patternArr = new char[size];

        for (int i=0; i < size; i++)
            patternArr[i] = '0';

        String sp = new String(patternArr, 0, size);
        sb.append(sp);
        sb.replace(size-src.length(), size, src);

        return sb.toString();
    }


}
