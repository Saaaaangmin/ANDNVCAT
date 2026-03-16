package com.posbank.device.screader.kis.protocol;


import com.posbank.device.screader.kis.model.ScrResponseFields;

import java.nio.ByteBuffer;

import static com.posbank.device.common.AscII.CH_ACK;
import static com.posbank.device.common.AscII.CH_NAK;
import static com.posbank.device.common.AscII.CH_STX;
import static com.posbank.device.common.ReturnValue.RTN_COMM_OK;
import static com.posbank.device.common.ReturnValue.RTN_CONTINUE;
import static com.posbank.device.common.ReturnValue.RTN_FALSE;
import static com.posbank.device.common.ReturnValue.RTN_INVALID_DATA;
import static com.posbank.device.common.ReturnValue.RTN_LRC_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_TRUE;



public class ResponseMsgContain {
    //==============================================================================================//
    public byte rxCommandID;
    public ByteBuffer rxDataValue;
    public byte[] rxDataValuebyte = new byte[4096];
    public int rxDataValueLength;

    //==============================================================================================//
    public boolean remainMsg = false;
    public int remainMsgLen;
    public int remainIndex;

    //==============================================================================================//
    ScrResponseFields rxFields = new ScrResponseFields();

    //==============================================================================================//
    //
    //==============================================================================================//
    public void initRemainValue() {
        this.remainMsg    = false;
        this.remainMsgLen = 0;
        this.remainIndex  = 0;
    }

    //==============================================================================================//
    // Calculation LRC (STX 다음부터 ETX 까지 계산)
    //==============================================================================================//
    // @param frame  = 전문 패킷 데이타
    // @param size   = STX 부터 ETX 까지 패킷길이
    // @param inLrc  = LRC(수신된 LRC)
    //==============================================================================================//
    private int checkLRC(byte[] frame, int size, byte inLrc) {
        byte lrc = 0;
        for (int i=0; i<size-1; i++) {
            lrc ^= frame[i];
        }

        if (lrc != inLrc) return RTN_FALSE;

        return RTN_TRUE;
    }

    //==============================================================================================//
    // 응답전문 포맷 CHECK
    //==============================================================================================//
    // @param rxMsg    = 수신된 응답전문
    // @param rxMsgLen = 수신된 응답전문 길이
    //==============================================================================================//
    int frameCheck(byte[] rxMsg, int rxMsgLen) {
        boolean	stx = false, lrc = false;
        int stxIndex=0, cmdIndex, lrcIndex=0;
        int iDataSize=0;
        //==========================================================================================
        // HeaderID(STX), CommandID(1), Data Size(2), DATA(n), LRC 전문포맷 Check
        //==========================================================================================
        for (int i=0; i<rxMsgLen; i++) {
            // Header ID 검색
            // 패킷 포맷 Check
            if (rxMsg[i] == CH_STX) {
                stx = true;     // STX 수신
                // STX Index 저장
                stxIndex = i;

                // Command ID Index 저장
                if (rxMsgLen - i > stxIndex + 1) {
                    cmdIndex = stxIndex + 1;
                } else {
                    // 계속해서 RxData 수신...
                    return RTN_CONTINUE;
                }

                // Data Size 계산
                if (rxMsgLen - i > cmdIndex + 2) {
                    byte[] baDataSize = new byte[2];
                    baDataSize[0] = rxMsg[cmdIndex + 1];
                    baDataSize[1] = rxMsg[cmdIndex + 2];
                    iDataSize = convertBcdToInteger(baDataSize);
                } else {
                    // 계속해서 RxData 수신...
                    return RTN_CONTINUE;
                }

                // 패킷포맷 전체길이를 구한다.
                // HeaderID(STX), CommandID(1), Data Size(2), DATA(n), LRC
                int frameLen = 1 + 1 + 2 + iDataSize + 1;
                byte[] frame;
                if (rxMsgLen - i >= frameLen) {
                    // 수신된 전문에서 HeaderID(STX) ~ DATA(n) LRC 만 추출한다.
                    frame = new byte[frameLen];
                    System.arraycopy(rxMsg, stxIndex, frame, 0, frameLen);
                } else {
                    // 계속해서 RxData 수신...
                    return RTN_CONTINUE;
                }

                // LRC Index 저장
                lrcIndex = cmdIndex + 2 + iDataSize + 1;
                if (checkLRC(frame, frameLen, frame[frameLen - 1]) == RTN_FALSE) {
                    lrc = false;
                    break;
                } else {
                    lrc = true;
                    break;
                }
            }
        }

        // STX 검출
        if (stx) {
            // LRC Check 결과
            if (lrc) {
                // 정상.
                // 길이 : 구분코드부터 ETX 까지의 바이트 수
                // 데이타 길이 : 구분코드 제외

                // DATA
//                byte[] data = new byte[iDataSize];
//                System.arraycopy(rxMsg, stxIndex + 4, data, 0, iDataSize);
                byte[] data = new byte[iDataSize+4];
                System.arraycopy(rxMsg, stxIndex, data, 0, iDataSize+4);
                // 응답전문 커멘드와 DATA 저장.
//                setContain(rxMsg[stxIndex + 1], iDataSize, data);
                setContain(rxMsg[stxIndex + 1], iDataSize+4, data);

                // 뒤에 남은 전문 Data Check
                if (rxMsgLen > lrcIndex+1) {
                    remainMsg = true;
                    remainMsgLen = rxMsgLen-(lrcIndex+1);
                    remainIndex = lrcIndex+1;
                } else {
                    remainMsg = false;
                    remainMsgLen = 0;
                }

                return RTN_COMM_OK;
            } else {
                // LRC Check 오류()
                // 뒤로 계속 전문 수신 받아서 처리 필요...
                return RTN_LRC_FAIL;
            }
        }

        //==========================================================================================
        // ACK(1)/NAK(1) Check
        //==========================================================================================
        boolean	ack = false, nak = false;
        for (int i=0; i<rxMsgLen; i++) {
            if (!ack && rxMsg[i] == CH_ACK) {
                ack = true;
            } else if (!nak && rxMsg[i] == CH_NAK) {
                nak = true;
            }
        }

        if (ack) {
            setContain(CH_ACK, 0, null);
            return RTN_COMM_OK;
        } else if (nak) {
            setContain(CH_NAK, 0, null);
            return RTN_COMM_OK;
        }

        return RTN_INVALID_DATA;
    }

    //==============================================================================================//
    // 응답전문 커멘드와 DATA 저장
    //==============================================================================================//
    private void setContain(byte cmd, int dataLen, byte[] dataValue) {
        // Command ID
        rxCommandID = cmd;
        // Data size
        rxDataValueLength = dataLen;
        // Data Value
        if (null != dataValue) {
            rxDataValue = ByteBuffer.wrap(dataValue);
            System.arraycopy(dataValue, 0, rxDataValuebyte, 0, dataLen);
        }
    }

    /**
     * Convert BCD to Integer
     * @param bcd : Data Length BCD Type
     * @return : Data Length integer Type
     */
    private int convertBcdToInteger(byte[] bcd) {
        StringBuilder sb = new StringBuilder();
        for (byte bBcd: bcd) {
            sb.append(BCDtoString(bBcd));
        }
        return Integer.valueOf(sb.toString());
    }

    /**
     * Convert BCD to String
     * @param bcd :
     * @return :
     */
    private String BCDtoString(byte bcd) {
        StringBuilder sb = new StringBuilder();

        byte highNibble = (byte) (bcd & 0xf0);
        highNibble >>>= (byte) 4;
        highNibble = (byte) (highNibble & 0x0f);
        byte lowNibble = (byte) (bcd & 0x0f);

        sb.append(highNibble);
        sb.append(lowNibble);

        return sb.toString();
    }
}
