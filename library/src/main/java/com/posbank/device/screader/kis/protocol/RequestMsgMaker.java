package com.posbank.device.screader.kis.protocol;


import com.posbank.device.screader.kis.model.ScrRequestFields;

import static com.posbank.device.common.AscII.CH_STX;
import static com.posbank.device.common.Utils.GetBytesAndFillSpace;
import static com.posbank.device.common.Utils.LeadingZerosItoA;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Encrypt_Key_Down_x6B;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Encrypt_Key_Sync_x6A;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Encrypt_MS_IC_Credit_Point_xBF;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Encrypt_MS_IC_Credit_x6C;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Get_System_Information_x31;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_IC_EMV_Complete_x6D;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Insert_DIK_x3D;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_MS_Fallback_Credit_Point_xBE;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_MS_Fallback_Credit_x6E;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Mutual_Authentication_xA0;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Reset_x32;
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Self_Integrity_xA1;
import static com.posbank.device.screader.kis.model.ScrConstant.MutualAuth_Code_Complete_xF3;
import static com.posbank.device.screader.kis.model.ScrConstant.MutualAuth_Code_Initialize_xF2;
import static com.posbank.device.screader.kis.model.ScrConstant.MutualAuth_Code_MSK_xF1;


public class RequestMsgMaker {
    //==============================================================================================
    // 요청전문 필드 객체 생성
    ScrRequestFields txFields = new ScrRequestFields();
    
    //==============================================================================================
    /**
     * Calculation LRC
     * @param buffer : 패킷 데이타 버퍼
     * @param size : 사이즈
     * @return : lrc
     */
    private byte calcLRC(byte[] buffer, int size) {
        byte lrc = 0;
        for (int i=0; i<size; i++) {
            lrc ^= buffer[i];
        }
        return lrc;
    }

    /**
     * Request Message Make
     * @param cmd : CMD
     * @param data : Data
     * @param dataLen : Data 길이
     * @param outFrame : 완성된 전문
     * @return : 완성된 전문 길이
     */
    private int makeRequestMsg(byte cmd, byte[] data, int dataLen, byte[] outFrame) {
        int idx = 0;

        // 일반적인 패킷 포맷
        // Header ID(1) + Command ID(1) + Data size(2) + DATA Value(Data size(n)) + LRC(1)
        // Header(4) + DATA Value(Data size(n)) + Tailor(1)
        // - Header ID : STX(0x02)를 사용하며 Reader 와 POS 간 인터페이스 전문 데이터 패킷의 시작을 나타냄
        // - Data size : Header size, Command ID, LRC 를 제외한 Data Field 의 size 를 표기하며
        //               2Bytes 로 표기한다(예: 678byte 인 경우 = '0x06 0x78'로 data 를 전송함)
        // - CommandID : ACK 여부가 YES 인 경우는 Command 요청 후 ACK 를 수신하고 응답을 대기해야 하며,
        //               NO 인 경우는 응답이 바로 오거나, 응답이 없는 경우 임
        // - Data      : Data 는 POS 와 Reader 사이에 전송되는 실제 Data 를 말함.
        // - 기타메시지 :
        //   초기화 메시지 : POS 에서 Reader 로 초기화 요청 시 EOT 1Byte 전송
        //                  (서명 입력 중 POS 에서 취소키 눌렸을 경우 포함)
        //   Reader 에서 전송한 Data 오류(LRC Fail 포함) : POS 에서 Reader 로 NAK 전송
        // - LRC(Longitudinal Redundancy Check) : LRC 는 Header 와 Data 를 포함한 데이터 값들의 전송상태를
        //   체크하기 위한 값으로 Header ID(STX) 부터 LRC 직전의 값까지 계산됨
        // Header ID(1)
        outFrame[idx] = CH_STX;                                     idx++;
        // Command ID(1)
        outFrame[idx] = cmd;                                        idx++;
        // Data size(2)
        byte[] baDataSize = convertIntegerToBCD(dataLen);
        outFrame[idx] = baDataSize[0];                              idx++;
        outFrame[idx] = baDataSize[1];                              idx++;

        // DATA(dataLen)
        for(int i=0; i<dataLen; i++) {
            outFrame[idx] = data[i];
            idx++;
        }
        // LRC(1)
        outFrame[idx] = calcLRC(outFrame, idx);                     idx++;

        return idx;
    }

    /**
     * Convert Integer to BCD Array
     * e.g : dataLength = 1234 --> bcd[]{0x12, 0x34}
     * @param dataLength : Integer Value
     * @return : BCD Array
     */
    private byte[] convertIntegerToBCD(int dataLength) {
        int num = dataLength, digits = 4, byteLen = 2;
        byte bcd[] = new byte[byteLen];

        for (int i=0; i<digits; i++) {
            byte nibble = (byte) (num % 10);

            if (i % 2 == 0)
                bcd[i / 2] = nibble;
            else {
                byte high = (byte) (nibble << 4);
                bcd[i / 2] |= high;
            }

            num /= 10;
        }

        // 위치 교환
        for (int i=0; i<byteLen/2; i++) {
            byte tmp = bcd[i];
            bcd[i] = bcd[byteLen-i-1];
            bcd[byteLen-i-1] = tmp;
        }

        return bcd;
    }

    //==============================================================================================//
    // Get System Information 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x31_MakeMessage(byte[] msg) {
        return makeRequestMsg(CMD_Get_System_Information_x31, null, 0, msg);
    }

    //==============================================================================================//
    // Reset 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x32_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // Wait Time
        byte[] baWaitTime = txFields.mRebootWaitTime.getBytes();
        data[dataLen] = baWaitTime[0];          dataLen++;

        return makeRequestMsg(CMD_Reset_x32, data, dataLen, msg);
    }

    //==============================================================================================//
    // DIK Download 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x3D_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // DIK 일련번호, AN(16)
        System.arraycopy(txFields.mDIK_SerialNo.getBytes(), 0, data, dataLen, 16);
        dataLen += 16;

        // DIK, AN(32)
        System.arraycopy(txFields.mDIK_KeyData.getBytes(), 0, data, dataLen, 32);
        dataLen += 32;

        return makeRequestMsg(CMD_Insert_DIK_x3D, data, dataLen, msg);
    }

    //==============================================================================================//
    // POS 와 Reader 상호 인증
    // MSK 생성 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int xA0F1_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // 거래구분, AN(2)
        System.arraycopy(MutualAuth_Code_MSK_xF1.getBytes(), 0, data, dataLen, 2);
        dataLen += 2;

        // RND_P1, AN(8)
        System.arraycopy(txFields.mRND_P1, 0, data, dataLen, 8);
        dataLen += 8;

        return makeRequestMsg(CMD_Mutual_Authentication_xA0, data, dataLen, msg);
    }

    //==============================================================================================//
    // POS 와 Reader 상호 인증 초기화 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int xA0F2_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // 거래구분, AN(2)
        System.arraycopy(MutualAuth_Code_Initialize_xF2.getBytes(), 0, data, dataLen, 2);
        dataLen += 2;

        // RND_P2, AN(8)
        System.arraycopy(txFields.mRND_P2, 0, data, dataLen, 8);
        dataLen += 8;

        return makeRequestMsg(CMD_Mutual_Authentication_xA0, data, dataLen, msg);
    }

    //==============================================================================================//
    // POS 와 Reader 상호 인증 완료 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int xA0F3_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // 거래구분, AN(2)
        System.arraycopy(MutualAuth_Code_Complete_xF3.getBytes(), 0, data, dataLen, 2);
        dataLen += 2;

        // Encrypted RND, AN(32)
        System.arraycopy(txFields.mEncrypted_RND, 0, data, dataLen, 32);
        dataLen += 32;

        return makeRequestMsg(CMD_Mutual_Authentication_xA0, data, dataLen, msg);
    }

    //==============================================================================================//
    // Reader 무결성 검증 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int xA1_MakeMessage(byte[] msg) {
        return makeRequestMsg(CMD_Self_Integrity_xA1, null, 0, msg);
    }

    //==============================================================================================//
    // SafeCard 암호화 KEY 정보 동기화 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x6A_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // 키 갱신종류, AN(1)
        System.arraycopy(GetBytesAndFillSpace(txFields.mKey_Renewal_Kind, 1), 0, data, dataLen, 1);
        dataLen++;

        // CAT-ID, AN(10)
        System.arraycopy(txFields.mTerminalID, 0, data, dataLen, 10);
        dataLen += 10;

        return makeRequestMsg(CMD_Encrypt_Key_Sync_x6A, data, dataLen, msg);
    }

    //==============================================================================================//
    // SafeCard 암호화 KEY 정보 Download 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x6B_MakeMessage(byte[] msg) {
        byte[] data = new byte[512];
        int dataLen = 0;

        // 키 갱신종류, AN(1)
        System.arraycopy(GetBytesAndFillSpace(txFields.mKey_Renewal_Kind, 1), 0, data, dataLen, 1);
        dataLen++;

        // VAN ID, AN(2)
        System.arraycopy(txFields.mVanID, 0, data, dataLen, 2);
        dataLen += 2;

        // TID, AN(10)
        System.arraycopy(txFields.mTerminalID, 0, data, dataLen, 10);
        dataLen += 10;

        // DIKn 일련번호, AN(16)
        System.arraycopy(txFields.mDIKnSerialNo, 0, data, dataLen, 16);
        dataLen += 16;

        // PMK 일련번호, AN(6)
        System.arraycopy(txFields.mPMKSerialNo, 0, data, dataLen, 6);
        dataLen += 6;

        // 암호값, AN(256)
        System.arraycopy(txFields.mEncryptedValue, 0, data, dataLen, 256);
        dataLen += 256;

        // PMK 유효기간, AN(6)
        System.arraycopy(txFields.mPMKValidity, 0, data, dataLen, 6);
        dataLen += 6;

        // TPL, AN(3)
        System.arraycopy(txFields.mTPL, 0, data, dataLen, 3);
        dataLen += 3;

        return makeRequestMsg(CMD_Encrypt_Key_Down_x6B, data, dataLen, msg);
    }

    //==============================================================================================//
    // SafeCard 암호화 MS/IC 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x6C_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // Card 대기 시간, N(2)
        System.arraycopy(LeadingZerosItoA(txFields.mCardWait_Time, 2), 0, data, dataLen, 2);
        dataLen += 2;

        // 거래 일시, ANS(14)
        System.arraycopy(GetBytesAndFillSpace(txFields.mTransDate, 14), 0, data, dataLen, 14);
        dataLen += 14;

        // 거래 금액, AN(12)
        System.arraycopy(LeadingZerosItoA(txFields.mTransAmount, 12), 0, data, dataLen, 12);
        dataLen += 12;

        // TID, AN(10), 오른쪽 정렬, CAT ID(TID) : "0"으로 Padding
        System.arraycopy(txFields.mTerminalID, 0, data, dataLen, 10);
        dataLen += 10;

        // 거래종류, AN(1)
        System.arraycopy(GetBytesAndFillSpace(txFields.mTransType, 1), 0, data, dataLen, 1);
        dataLen += 1;

        return makeRequestMsg(CMD_Encrypt_MS_IC_Credit_x6C, data, dataLen, msg);
    }

    //==============================================================================================//
    // SafeCard 암호화 IC EMV 완료 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x6D_MakeMessage(byte[] msg) {
        byte[] data = new byte[512];
        int dataLen = 0;

        // 전문 길이, ASC(4)
        System.arraycopy(txFields.mEmvLength, 0, data, dataLen, 4);
        dataLen += 4;

        // 응답 코드, ASC(2)
        System.arraycopy(txFields.mResponseCode, 0, data, dataLen, 2);
        dataLen += 2;

        // Additional response data, ASC(27)
        System.arraycopy(txFields.mARD, 0, data, dataLen, 27);
        dataLen += 27;

        // IAD, ASC(34)
        System.arraycopy(txFields.mIAD, 0, data, dataLen, 34);
        dataLen += 34;

        // Issuer Script, ASC(259)
        System.arraycopy(txFields.mIS, 0, data, dataLen, 259);
        dataLen += 259;

        return makeRequestMsg(CMD_IC_EMV_Complete_x6D, data, dataLen, msg);
    }

    //==============================================================================================//
    // SafeCard 암호화 MS Fallback 거래 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int x6E_MakeMessage(byte[] msg) {
        byte[] data = new byte[128];
        int dataLen = 0;

        // Card 대기 시간, N(2)
        System.arraycopy(LeadingZerosItoA(txFields.mCardWait_Time, 2), 0, data, dataLen, 2);
        dataLen += 2;

        // Display Message, AN(64)
        System.arraycopy(GetBytesAndFillSpace(txFields.mDisplayMessage, 64), 0, data, dataLen, 64);
        dataLen += 64;

        return makeRequestMsg(CMD_MS_Fallback_Credit_x6E, data, dataLen, msg);
    }

    //==============================================================================================//
    // SafeCard 암호화 MS/IC 신용 & 포인트 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int xBF_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // Card 대기 시간, N(2)
        System.arraycopy(LeadingZerosItoA(txFields.mCardWait_Time, 2), 0, data, dataLen, 2);
        dataLen += 2;

        // 거래 일시, ANS(14)
        System.arraycopy(GetBytesAndFillSpace(txFields.mTransDate, 14), 0, data, dataLen, 14);
        dataLen += 14;

        // 거래 금액, AN(12)
        System.arraycopy(LeadingZerosItoA(txFields.mTransAmount, 12), 0, data, dataLen, 12);
        dataLen += 12;

        // TID, AN(10), 오른쪽 정렬, CAT ID(TID) : "0"으로 Padding
        System.arraycopy(txFields.mTerminalID, 0, data, dataLen, 10);
        dataLen += 10;

        // 거래종류, AN(1)
        System.arraycopy(GetBytesAndFillSpace(txFields.mTransType, 1), 0, data, dataLen, 1);
        dataLen += 1;

        return makeRequestMsg(CMD_Encrypt_MS_IC_Credit_Point_xBF, data, dataLen, msg);
    }

    //==============================================================================================//
    // SafeCard 암호화 MS Point Fallback 거래 요청 전문
    // @param msg : 완성된 전문 버퍼
    //==============================================================================================//
    // return     : 완성된 전문 길이
    //==============================================================================================//
    int xBE_MakeMessage(byte[] msg) {
        byte[] data = new byte[64];
        int dataLen = 0;

        // Card 대기 시간, N(2)
        System.arraycopy(LeadingZerosItoA(txFields.mCardWait_Time, 2), 0, data, dataLen, 2);
        dataLen += 2;

        // 암호화 구분, N(1)
        System.arraycopy(GetBytesAndFillSpace(txFields.mIsEncrypt, 1), 0, data, dataLen, 1);
        dataLen += 1;

        // 카드번호 암호화 키, AN(32)
        System.arraycopy(txFields.mEncryptKeyForCardNo, 0, data, dataLen, 32);
        dataLen += 32;

        return makeRequestMsg(CMD_MS_Fallback_Credit_Point_xBE, data, dataLen, msg);
    }
}
