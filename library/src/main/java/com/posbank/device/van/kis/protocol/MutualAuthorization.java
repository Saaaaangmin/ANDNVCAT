package com.posbank.device.van.kis.protocol;


import com.posbank.device.screader.kis.protocol.ScrProtocolCom;

import java.util.Arrays;

import static com.posbank.device.common.ReturnValue.RTN_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_INVALID_DATA;
import static com.posbank.device.common.ReturnValue.RTN_SCR_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_SCR_MUTUAL_AUTH_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_SCR_SUCCESS;
import static com.posbank.device.common.ReturnValue.RTN_SUCCESS;
import static com.posbank.device.screader.kis.model.ScrConstant.MutualAuth_Result_x30;
import static com.posbank.device.screader.kis.model.ScrConstant.MutualAuth_Result_x31;
import static kisvan.Kisvan.GF_SCRDecrypt;
import static kisvan.Kisvan.GF_SCREncyrpt;
import static kisvan.Kisvan.GF_SCRMakeASK;
import static kisvan.Kisvan.GF_SCRMakeMSK;
import static kisvan.Kisvan.GF_SCRRandomNumber;


public class MutualAuthorization {
    // ScrProtocolCom
    private ScrProtocolCom scr;

    /**
     * Constructor
     * @param _ScrProtocolCom : ScrProtocolCom
     */
    public MutualAuthorization(ScrProtocolCom _ScrProtocolCom) {
        this.scr = _ScrProtocolCom;
    }

    /**
     * Execute Mutual Authorization
     */
    public int runMutualAuthorization() {
        /*
         * Algorithm to Mutual Authentication 문서 참조(대외비)
         */
        // 1st POS Random Number
        byte[] baRND_P1 = new byte[8];
        // 2nd POS Random Number
        byte[] baRND_P2 = new byte[8];

        byte[] baMSK = new byte[32];            // MSK = SEED(CSN, BSeed12)
        byte[] baASK = new byte[32];            // ASK = SEED(MSK, RND_FORM)
        byte[] baCryptoAlg1 = new byte[32];     // CryptoAlg1 = SEED(ASK, RND_R + RND_P)
        byte[] baCryptoAlg2 = new byte[32];     // CryptoAlg2 = SEED(ASK, RND_FORM 2)

        int resultCode;

        /*
         * CSN 정보 가져오기
         */
        resultCode = scr.getSystemInformation();
        if (resultCode != RTN_SUCCESS) {
            scr.setReturnCodeMsg(resultCode);
            return RTN_FAIL;
        }

        // CSN = DIK 일련번호
        byte[] baChipSerialNumber = scr.rxF.mDIKnSerialNo;
        scr.DEBUG_FIELD_HEX("CSN", baChipSerialNumber, baChipSerialNumber.length);

        /*
         * Create 1st POS Random Number(RND_P1 생성)
         */
        GF_SCRRandomNumber(baRND_P1);
        scr.DEBUG_FIELD_HEX("RND_P1 생성", baRND_P1, baRND_P1.length);

        /*
         * MSK(Message Session Key) 생성 요청
         */
        resultCode = scr.requestCreateMSK(baRND_P1);
        if (resultCode != RTN_SCR_SUCCESS) {
            if (resultCode != RTN_SCR_FAIL) {
                scr.setReturnCodeMsg(resultCode);
            }
            return RTN_FAIL;
        }

        // RND_R1 전달
        byte[] baRND_R1 = scr.rxF.mRND_R1;        // 1st Reader Random Number
        scr.DEBUG_FIELD_HEX("RND_R1 전달", baRND_R1, baRND_R1.length);

        /*
         * RND_P1 와 RND_R1 로 MSK(Message Session Key) 생성
         */
        GF_SCRMakeMSK(baChipSerialNumber, baRND_P1, baRND_R1, baMSK);
        scr.DEBUG_FIELD_HEX("MSK 생성", baMSK, baMSK.length);

        /*
         * Create 2nd POS Random Number(RND_P2 생성)
         */
        GF_SCRRandomNumber(baRND_P2);
        scr.DEBUG_FIELD_HEX("RND_P2 생성", baRND_P2, baRND_P2.length);

        /*
         * 상호 인증 초기화
         */
        resultCode = scr.initializeMutualAuth(baRND_P2);
        if (resultCode != RTN_SCR_SUCCESS) {
            if (resultCode != RTN_SCR_FAIL) {
                scr.setReturnCodeMsg(resultCode);
            }
            return RTN_FAIL;
        }
        // RND_R2 전달
        byte[] baRND_R2 = scr.rxF.mRND_R2;              // 2nd Reader Random Number
        byte[] baEncryptRND = scr.rxF.mEncrypted_RND;   // 암호화된 Random Number
        scr.DEBUG_FIELD_HEX("RND_R2 전달", baRND_R2, baRND_R2.length);
        scr.DEBUG_FIELD_HEX("암호화된 RND 전달", baEncryptRND, baEncryptRND.length);

        /*
         * 1. RND_P2 와 RND_R2 로 ASK(Authentication Session Key) 생성
         */
        GF_SCRMakeASK(baMSK, baRND_P2, baRND_R2, baASK);
        scr.DEBUG_FIELD_HEX("ASK 생성", baASK, baASK.length);

        /*
         * 2. RND_P2 와 RND_R2 를 ASK 로 복호화
         */
        GF_SCREncyrpt(baASK, baRND_P2, baRND_R2, baCryptoAlg1);

        /*
         * 3. 암호화 된 RND 를 복호화하여 RND_P2를 검증
         */
        byte[] baDecryptRND_P2 = new byte[8];
        GF_SCRDecrypt(baASK, baEncryptRND, baDecryptRND_P2);
        if (!Arrays.equals(baRND_P2, baDecryptRND_P2)) {
            return RTN_FAIL;
        }

        /*
         * 4. RND_P2 와 RND_R2 를 ASK 로 암호화
         */
        GF_SCRMakeASK(baASK, baRND_R2, baRND_P2, baCryptoAlg2);
        scr.DEBUG_FIELD_HEX("Alg2", baCryptoAlg2, baCryptoAlg2.length);

        /*
         * 상호 인증 완료
         */
        resultCode = scr.completeMutualAuth(baCryptoAlg2);
        if (resultCode != RTN_SCR_SUCCESS) {
            if (resultCode != RTN_SCR_FAIL) {
                scr.setReturnCodeMsg(resultCode);
                return RTN_FAIL;
            }
        }

        /*
         * 거래결과 체크
         */
        // 검증 성공
        if (scr.rxF.mMutualAuth_Result[0] == MutualAuth_Result_x30) {
            return RTN_SCR_SUCCESS;
        }
        // 검증 실패
        else if (scr.rxF.mMutualAuth_Result[0] == MutualAuth_Result_x31) {
            return RTN_SCR_MUTUAL_AUTH_FAIL;
        }
        //
        else {
            return RTN_INVALID_DATA;
        }
    }
}
