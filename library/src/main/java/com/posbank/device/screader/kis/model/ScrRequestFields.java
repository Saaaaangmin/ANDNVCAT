package com.posbank.device.screader.kis.model;

/**
 * 보안패드 요청전문 필드 데이터
 */
public class ScrRequestFields {

    //==============================================================================================
    // Wait Time
    public String mRebootWaitTime = "";     // Reboot Wait Time(0~9 /Sec)

    //==============================================================================================
    // DIK 일련번호, AN(16)
    public String mDIK_SerialNo = "";       // DIK0 의 일련번호
    // DIK, AN(32)
    public String mDIK_KeyData = "";        // DIK0 의 Key Data

    //==============================================================================================
    // RND_P1, AN(8)
    public byte[] mRND_P1 = null;           // 1st POS Random Number(0-9, A-F)
    // RND_P2, AN(8)
    public byte[] mRND_P2 = null;           // 2nd POS Random Number
    // Encrypted RND, AN(32)
    public byte[] mEncrypted_RND = null;    // 암호화된 Random Number

    //==============================================================================================
    // 키 갱신종류, AN(1)
    public String mKey_Renewal_Kind = "";   // 최초(분배):"1", 갱신:"2"
    // VAN-ID, AN(2)
    public byte[] mVanID = new byte[2];             // 기관 코드
    // TID, AN(10)
    public byte[] mTerminalID = new byte[10];       // 가맹점 TID
    // DIKn 일련번호, AN(16)
    public byte[] mDIKnSerialNo = new byte[16];     // DIKn 일련번호
    // PMK 일련번호, AN(6)
    public byte[] mPMKSerialNo = new byte[6];       // PMK 일련번호
    // 암호값, AN(256)
    public byte[] mEncryptedValue = new byte[256];  // 암호화 정보
    // PMK 유효기간, AN(6)
    public byte[] mPMKValidity = new byte[6];       // PMK 유효기간
    // TPL, AN(3)
    public byte[] mTPL = new byte[3];               // 암호화 범위 설정

    //==============================================================================================
    // Card 대기시간, N(2)
    public int mCardWait_Time;              // Card 대기시간(30초 인 경우 "30")
    // Display Message, AN(64)
    public String mDisplayMessage = "";     // 16Bytes * 4 Lines 빈라인은 Space 로 Padding
    // 거래 일시, ANS(14)
    public String mTransDate = "";          // YYYYMMDDhhmmss
    // 거래 금액, AN(12)
    public int mTransAmount;                // Ex) 1,000원 인 경우, 0x000000001000
    // 거래 종류, N(1)
    public String mTransType = "";          // 구매:"0", 취소:"1"

    //==============================================================================================
    // 전문길이, ASC(4)
    public byte[] mEmvLength = new byte[4]; // 전문길이,
                                            // 응답코드 + Additional response data + IAD + Issuer Script
    // 응답코드, ASC(2)
    public byte[] mResponseCode = new byte[2];  // Authorization Response Code
    // Additional response data, ASC(27)
    public byte[] mARD = new byte[27];      // Additional response date
    // IAD, ASC(34)
    public byte[] mIAD = new byte[34];      // Issuer Authentication Data
    // Issuer Script, ASC(259)
    public byte[] mIS = new byte[259];      // Issuer Script

    //==============================================================================================
    // 암호화 구분, N(1)
    public String mIsEncrypt = "";          // "0" or 없음:암호화 하지 않음, "1":SEED(키정통) 암호화
    // 카드번호 암호화 키, AN(32)
    public byte[] mEncryptKeyForCardNo = new byte[32];  // 암호화 구분 "0" : Key 없음
                                                        // 암호화 구분 "1" : 16 bytes Random Key
                                                        // (16진수 문자열 32자리)
    /**
     * SCR(보안리더) 요청 전문 Field Erase
     */
    public void sanitizeScrRequestFields() {
        // Wait Time
        mRebootWaitTime = "";               // Reboot Wait Time(0~9 /Sec)
        mRebootWaitTime = null;
        // DIK 일련번호, AN(16)
        mDIK_SerialNo = "";                 // DIK0 의 일련번호
        mDIK_SerialNo = null;
        // DIK, AN(32)
        mDIK_KeyData = "";                  // DIK0 의 Key Data
        mDIK_KeyData = null;
        // RND_P1, AN(8)
        secureErase(mRND_P1);
        mRND_P1 = null;                     // 1st POS Random Number(0-9, A-F)
        // RND_P2, AN(8)
        secureErase(mRND_P2);
        mRND_P2 = null;                     // 2nd POS Random Number
        // Encrypted RND, AN(32)
        secureErase(mEncrypted_RND);
        mEncrypted_RND = null;              // 암호화된 Random Number
        // 키 갱신종류, AN(1)
        mKey_Renewal_Kind = "";             // 최초(분배):"1", 갱신:"2"
        mKey_Renewal_Kind = null;
        // VAN-ID, AN(2)
        secureErase(mVanID);
        mVanID = null;                      // 기관 코드
        // TID, AN(10)
        secureErase(mTerminalID);
        mTerminalID = null;                 // 가맹점 TID
        // DIKn 일련번호, AN(16)
        secureErase(mDIKnSerialNo);
        mDIKnSerialNo = null;               // DIKn 일련번호
        // PMK 일련번호, AN(6)
        secureErase(mPMKSerialNo);
        mPMKSerialNo = null;                // PMK 일련번호
        // 암호값, AN(256)
        secureErase(mEncryptedValue);
        mEncryptedValue = null;             // 암호화 정보
        // PMK 유효기간, AN(6)
        secureErase(mPMKValidity);
        mPMKValidity = null;                // PMK 유효기간
        // TPL, AN(3)
        secureErase(mTPL);
        mTPL = null;                        // 암호화 범위 설정
        // 전문길이, ASC(4)
        secureErase(mEmvLength);
        mEmvLength = null;                  // 전문길이,
        // 응답코드 + Additional response data + IAD + Issuer Script
        // 응답코드, ASC(2)
        secureErase(mResponseCode);
        mResponseCode = null;               // Authorization Response Code
        // Additional response data, ASC(27)
        secureErase(mARD);
        mARD = null;                        // Additional response date
        // IAD, ASC(34)
        secureErase(mIAD);
        mIAD = null;                        // Issuer Authentication Data
        // Issuer Script, ASC(259)
        secureErase(mIS);
        mIS = null;                         // Issuer Script
        // 암호화 구분, N(1)
        mIsEncrypt = "";                    // "0" or 없음:암호화 하지 않음, "1":SEED(키정통) 암호화
        mIsEncrypt = null;
        // 카드번호 암호화 키, AN(32)
        secureErase(mEncryptKeyForCardNo);
        mEncryptKeyForCardNo = null;        // 암호화 구분 "0" : Key 없음
                                            // 암호화 구분 "1" : 16 bytes Random Key
                                            // (16진수 문자열 32자리)
    }

    /**
     * 메모리 안전한 삭제
     * @param srcBuff : Buffer
     */
    private void secureErase(byte[] srcBuff) {
        if (null == srcBuff) return;

        byte clearVal;
        int iCapacity = srcBuff.length;

        for(int i=0; i<3; i++) {
            if (i==1) clearVal = (byte)(0xFF);
            else clearVal = (byte)(0x00);

            for(int j = 0; j < iCapacity; j++){
                srcBuff[j] = clearVal;
            }
        }
    }
}
