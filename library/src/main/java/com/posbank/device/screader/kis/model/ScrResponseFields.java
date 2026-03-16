package com.posbank.device.screader.kis.model;


/**
 * 보안패드 응답전문 필드 데이터
 */
public class ScrResponseFields {
    //==============================================================================================
    // 단말기 일련번호, AN(10)
    public byte[] mTerminalSerialNo = new byte[10];     // 단말기 일련번호
                                                        // GroupID[2]+생산년월[2]+일련번호[6]
    // DIK 일련번호, AN(16)
    public byte[] mDIKnSerialNo = new byte[16];         // DIKn 일련 번호
    // 모델명, AN(12)
    public byte[] mModelName = new byte[12];            // 모델명
    // SW 버전, AN(4)
    public byte[] mSWVersion = new byte[4];             // SW 버전, 대분류[1]+중분류[1]+소분류[2]
    // DL 버전, AN(2)
    public byte[] mDownloadVersion = new byte[2];       // 다운로드 버전
    // 리더기 일련번호(Option), AN(20)
    public byte[] mReaderSerialNo = new byte[20];       // 리더기 일련번호

    //==============================================================================================
    // 거래 구분, AN(2)
    public byte[] mGuBoonCode = new byte[2];            // 거래 구분
    // RND_R1, AN(8)
    public byte[] mRND_R1 = new byte[8];                // 1st Reader Random Number(0-9, A-F)
    // RND_R2, AN(8)
    public byte[] mRND_R2 = new byte[8];                // 2nd Reader Random Number
    // Encrypted RND, AN(32)
    public byte[] mEncrypted_RND = new byte[32];        // 암호화된 Random Number
    // 거래결과, AN(1)
    public byte[] mMutualAuth_Result = new byte[1];     // 0x30=검증성공, 0x31=검증실패

    //==============================================================================================
    // Result Code, AN(1)
    public byte[] mResult_Code = new byte[1];       // 거래결과

    //==============================================================================================
    // TID, AN(10)
    public byte[] mTerminalID = new byte[10];       // 가맹점 TID or CAT-ID
    // Device ID, AN(10)
    public byte[] mDeviceID = new byte[10];         // 제조사 ID(8bit) + 그룹 ID(8bit) + Device ID(19bit) + 5bit(0 세팅)
    // PMK 일련번호, AN(6)
    public byte[] mPMKSerialNo = new byte[6];       // PMK 일련번호
    // Random, AN(32)
    public byte[] mRandom = new byte[32];           // Device 가 생성한 Random Number(ASCII)

    //==============================================================================================
    // 카드구분자, AN(4)
    public byte[] mCardProperty = new byte[4];  // [1]매체구분, [2]결제구분, [3]이통사구분, [4]카드브랜드
    // CVM, AN(1)
    public byte[] mCVM = new byte[1];           // '0x30':No CVM, '0x31':Online Pin,
                                                // '0x32':Signature, '0x33':Signature + 자필서명
    // Fallback 구분 코드, AN(2)
    public byte[] mFallbackReasonCode = new byte[2];    // Fallback 구분 코드
    // VAN-ID, N(2)
    public byte[] mVanID = new byte[2];             // 기관코드
    // 암호화 정보, AN(96)
    public byte[] mEncryptedInfo = new byte[96];    // 암호화된 MS 정보
    // 생성된 MAC, AN(8)
    public byte[] mCreatedMAC = new byte[8];        // 위/변조 방지용 MAC 값
    // KSN, AN(20)
    public byte[] mKSN = new byte[20];              // KSN 의 ASCII 값
    // TPL, AN(3)
    public byte[] mTPL = new byte[3];               // Tag, Position, Length
    // 일련번호(리더기 식별번호), AN(16)
    public byte[] mReaderAuthID = new byte[16];     // 리더기 식별번호(모델명+SW 버전)
    // 유효기간, AN(6)
    public byte[] mPMKValidity = new byte[6];       // PMK 유효기간
    // fallback, N(3)
    public byte[] mFallbackCode = new byte[3];      // MSR 서비스 코드
    // Card BIN Number, N(6)
    public byte[] mCardBinNo = new byte[6];         // 카드번호 앞 6자리
    // Card BIN Number, N(19)
    public byte[] mCardBinNoPlain = new byte[19];   // 암호화 하지 않은 카드 번호 19자리, Padding Space
    // Card BIN Number, N(64)
    public byte[] mCardBinNoEncrypt = new byte[64]; // 암호화 된 카드번호
    // Chip Data Length, N(4)
    public int miChipDataLength;                    // Chip Data Length
    // Chip Data Length, N(4)
    public byte[] mChipDataLength = new byte[4];    // Chip Data Length
    // Chip Data, var(mChipDataLength)
    public byte[] mICEmvChipData;                   // Chip Data Length
    // TVR, ANS(10)
    public byte[] mTVR = new byte[10];              // Terminal Verification Results
    // AC, AN(16)
    public byte[] mAC = new byte[16];               // Application Cryptogram
    // TVR, AN(10)
    public byte[] mISR = new byte[40];              //

    /**
     * SCR(보안리더) 응답 전문 Field Erase
     */
    public void sanitizeScrResponseFields() {
        // 단말기 일련번호, AN(10)
        secureErase(mTerminalSerialNo);     mTerminalSerialNo = null;   // 단말기 일련번호
                                                                        // GroupID[2]+생산년월[2]+일련번호[6]
        // DIK 일련번호, AN(16)
        secureErase(mDIKnSerialNo);         mDIKnSerialNo = null;       // DIKn 일련 번호
        // 모델명, AN(12)
        secureErase(mModelName);            mModelName = null;          // 모델명
        // SW 버전, AN(4)
        secureErase(mSWVersion);            mSWVersion = null;          // SW 버전, 대분류[1]+중분류[1]+소분류[2]
        // DL 버전, AN(2)
        secureErase(mDownloadVersion);      mDownloadVersion = null;    // 다운로드 버전
        // 리더기 일련번호(Option), AN(20)
        secureErase(mReaderSerialNo);       mReaderSerialNo = null;     // 리더기 일련번호
        // 거래 구분, AN(2)
        secureErase(mGuBoonCode);           mGuBoonCode = null;         // 거래 구분
        // RND_R1, AN(8)
        secureErase(mRND_R1);               mRND_R1 = null;             // 1st Reader Random Number(0-9, A-F)
        // RND_R2, AN(8)
        secureErase(mRND_R2);               mRND_R2 = null;             // 2nd Reader Random Number
        // Encrypted RND, AN(32)
        secureErase(mEncrypted_RND);        mEncrypted_RND = null;      // 암호화된 Random Number
        // 거래결과, AN(1)
        secureErase(mMutualAuth_Result);    mMutualAuth_Result = null;  // 0x30=검증성공, 0x31=검증실패
        // Result Code, AN(1)
        secureErase(mResult_Code);          mResult_Code = null;        // 거래결과
        // TID, AN(10)
        secureErase(mTerminalID);           mTerminalID = null;         // 가맹점 TID or CAT-ID
        // Device ID, AN(10)
        secureErase(mDeviceID);             mDeviceID = null;           // 제조사 ID(8bit) + 그룹 ID(8bit) + Device ID(19bit) + 5bit(0 세팅)
        // PMK 일련번호, AN(6)
        secureErase(mPMKSerialNo);          mPMKSerialNo = null;        // PMK 일련번호
        // Random, AN(32)
        secureErase(mRandom);               mRandom = null;             // Device 가 생성한 Random Number(ASCII)
        // 카드구분자, AN(4)
        secureErase(mCardProperty);         mCardProperty = null;       // [1]매체구분, [2]결제구분, [3]이통사구분, [4]카드브랜드
        // CVM, AN(1)
        secureErase(mCVM);                  mCVM = null;                // '0x30':No CVM, '0x31':Online Pin,
        // '0x32':Signature, '0x33':Signature + 자필서명
        // Fallback 구분 코드, AN(2)
        secureErase(mFallbackReasonCode);   mFallbackReasonCode = null; // Fallback 구분 코드
        // VAN-ID, N(2)
        secureErase(mVanID);                mVanID = null;              // 기관코드
        // 암호화 정보, AN(96)
        secureErase(mEncryptedInfo);        mEncryptedInfo = null;      // 암호화된 MS 정보
        // 생성된 MAC, AN(8)
        secureErase(mCreatedMAC);           mCreatedMAC = null;         // 위/변조 방지용 MAC 값
        // KSN, AN(20)
        secureErase(mKSN);                  mKSN = null;                // KSN 의 ASCII 값
        // TPL, AN(3)
        secureErase(mTPL);                  mTPL = null;                // Tag, Position, Length
        // 일련번호(리더기 식별번호), AN(16)
        secureErase(mReaderAuthID);         mReaderAuthID = null;       // 리더기 식별번호(모델명+SW 버전)
        // 유효기간, AN(6)
        secureErase(mPMKValidity);          mPMKValidity = null;        // PMK 유효기간
        // fallback, N(3)
        secureErase(mFallbackCode);         mFallbackCode = null;       // MSR 서비스 코드
        // Card BIN Number, N(6)
        secureErase(mCardBinNo);            mCardBinNo = null;          // 카드번호 앞 6자리
        // Card BIN Number, N(19)
        secureErase(mCardBinNoPlain);       mCardBinNoPlain = null;     // 암호화 하지 않은 카드 번호 19자리, Padding Space
        // Card BIN Number, N(64)
        secureErase(mCardBinNoEncrypt);     mCardBinNoEncrypt = null;   // 암호화 된 카드번호
        // Chip Data Length, N(4)
        secureErase(mChipDataLength);       mChipDataLength = null;     // Chip Data Length
        // Chip Data, var(mChipDataLength)
        secureErase(mICEmvChipData);        mICEmvChipData = null;      // Chip Data Length
        // TVR, ANS(10)
        secureErase(mTVR);                  mTVR = null;                // Terminal Verification Results
        // AC, AN(16)
        secureErase(mAC);                   mAC = null;                 // Application Cryptogram
        // TVR, AN(10)
        secureErase(mISR);                  mISR = null;                //
    }

    /**
     * 메모리 안전한 삭제
     * @param srcBuff : Buffer
     */
    private void secureErase(byte[] srcBuff) {
        if (null == srcBuff) return;

        byte clearVal;
        int iCapacity = srcBuff.length;

        for(int i = 0; i < 3; i++) {
            if (i == 1) clearVal = (byte)(0xFF);
            else clearVal = (byte)(0x00);

            for(int j = 0; j < iCapacity; j++){
                srcBuff[j] = clearVal;
            }
        }
    }
}
