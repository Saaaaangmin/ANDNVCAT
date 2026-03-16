package com.posbank.device.screader.kis.model;


import static com.posbank.device.common.AscII.CH_0;
import static com.posbank.device.common.AscII.CH_1;

public class ScrConstant {

    //==============================================================================================
    // SCR Command ID
    //==============================================================================================
    // Get System Information 요청
    public static final byte CMD_Get_System_Information_x31     = (byte)(0x31);
    // Reset 요청
    public static final byte CMD_Reset_x32                      = (byte)(0x32);
    // DIK Download 요청
    public static final byte CMD_Insert_DIK_x3D                 = (byte)(0x3D);
    // Mutual Authentication 요청
    public static final byte CMD_Mutual_Authentication_xA0      = (byte)(0xA0);
    // Mutual Authentication 요청
    public static final byte CMD_Self_Integrity_xA1             = (byte)(0xA1);
    // SafeCard 암호화 KEY 정보 동기화 요청
    public static final byte CMD_Encrypt_Key_Sync_x6A           = (byte)(0x6A);
    // SafeCard 암호화 KEY Download 동기화 요청
    public static final byte CMD_Encrypt_Key_Down_x6B           = (byte)(0x6B);
    // SafeCard 암호화 MS/IC/RF 신용거래 시작 요청
    public static final byte CMD_Encrypt_MS_IC_Credit_x6C       = (byte)(0x6C);
    // IC EMV 완료 요청
    public static final byte CMD_IC_EMV_Complete_x6D            = (byte)(0x6D);
    // SafeCard 암호화 MS Fallback 거래 요청
    public static final byte CMD_MS_Fallback_Credit_x6E         = (byte)(0x6E);
    // SafeCard 암호화 카드번호 수기입력 거래 요청
    public static final byte CMD_CardNumber_HandInput_x90       = (byte)(0x90);
    // SafeCard 암호화 MS/IC/RF 신용 & 포인트 거래 요청
    public static final byte CMD_Encrypt_MS_IC_Credit_Point_xBF = (byte)(0xBF);
    // SafeCard 암호화 MS/IC/RF 신용 & 포인트 거래 완료 요청
    public static final byte CMD_MS_Fallback_Credit_Point_xBE   = (byte)(0xBE);

    // Mutual Authentication 거래 구분 코드
    public static final String MutualAuth_Code_MSK_xF1        = "F1"; // Create Message Session Key
    public static final String MutualAuth_Code_Initialize_xF2 = "F2"; // Initialize Mutual Authentication
    public static final String MutualAuth_Code_Complete_xF3   = "F3"; // Complete Mutual Authentication

    // 상호인증 검증 거래결과
    public static final byte MutualAuth_Result_x30   = CH_0; // 검증 성공
    public static final byte MutualAuth_Result_x31   = CH_1; // 검증 실패

    // 암호화 구분
    public static final String NotEncryptCardNumber = "0";  // "0" or 없음 : 암호화 하지 않음
    public static final String EncryptCardNumber    = "1";  // "1" : SEED(키정통) 암호화

    // 키 갱신종류
    public static final String KeyRenewal_First   = "1";    // 최초(분배)
    public static final String KeyRenewal_Renewal = "2";    // 갱신, 유효기간 만료시

    //==============================================================================================
    // 응답 결과 코드
    //==============================================================================================
    public static final byte RC_SUCCESS_x00            = (byte)0x00;    // 성공
    public static final byte RC_APDU_ERROR_x8C         = (byte)0x8c;    // IC 카드 APDU 응답 오류
    public static final byte RC_INVALID_CONDITION_x8D  = (byte)0x8d;    // 거래 조건이 맞지 않음
    public static final byte RC_INVALID_PARAM_x95      = (byte)0x95;    // 명령/파라미터 오류
    public static final byte RC_REVERSAL_xCC           = (byte)0xCC;    // 망 취소(호스트 승인 후 카드 거절)
    public static final byte RC_CANCEL_xCD             = (byte)0xCD;    // 단말기나 POS가 취소 시
    public static final byte RC_DECLINE_xCE            = (byte)0xCE;    // 카드 거래 거절
    public static final byte RC_FALLBACK_xCF           = (byte)0xCF;    // IC EMV 거래 FALLBACK
    public static final byte RC_NO_SIGNATURE_xD0       = (byte)0xD0;    // 서명 값이 존재 하지 않음
    public static final byte RC_NO_SafeMSR_KEY_xD1     = (byte)0xD1;    // 암호화 KEY 가 존재하지 않음
    public static final byte RC_NO_ID_xD2              = (byte)0xD2;    // 단말기 ID가 일치하지 않음
    public static final byte RC_ERR_DEVICE_MSR_xE1     = (byte)0xE1;    // MSR 동작 오류
    public static final byte RC_ERR_DEVICE_IFM_xE2     = (byte)0xE2;    // IFM 동작 오류
    public static final byte RC_INVALID_KEY_PMK_xE4    = (byte)0xE4;    // PMF 검증 오류
    public static final byte RC_DIFF_SAFECARD_KEY_xE6  = (byte)0xE6;    // SafeCard Key 일련번호 불일치(최초)
                                                                        // PMK Index 불일치(갱신)
    public static final byte RC_FIRST_IC_INSERT_xE8    = (byte)0xE8;    // IC 거래 우선 요망
    public static final byte RC_FIRST_NOT_FALLBACK_xE9 = (byte)0xE9;    // FALLBACK 거래 아님
    public static final byte RC_ERR_DEVICE_INIT_xEC    = (byte)0xEC;    // SafeCard Key 일련번호가 없음
                                                                        // (공장초기화 안됨)
    public static final byte RC_NO_CARD_xF2            = (byte)0xF2;    // 카드가 존재하지 않음
    public static final byte RC_NOT_ACCEPT_xF5         = (byte)0xF5;    // 지원되지 않는 카드
    public static final byte RC_INVALID_DATA_xF8       = (byte)0xF8;    // 요청 Message 의 Data 오류
    public static final byte RC_AUTH_ERROR_xFA         = (byte)0xFA;    // Reader 인증 오류
    public static final byte RC_AUTH_NOT_PERFORMED_xFB = (byte)0xFB;    // Reader 인증이 되지 않음
    public static final byte RC_APP_INTEGRITY_FAIL_xFC = (byte)0xFC;    // 다운로드 프로그램 무결성 훼손
    public static final byte RC_KEY_INTEGRITY_FAIL_xFD = (byte)0xFD;    // 암호화 키 무결성 훼손
    public static final byte RC_FAILURE_xFF            = (byte)0xFF;    // 실패 Fallback 미처리

    //==============================================================================================//
    // 응답코드 메시지
    //==============================================================================================//
    public static final String RESP_CODE_MSG_x00 = "성공";
    public static final String RESP_CODE_MSG_x8C = "IC 카드 APDU 응답 오류";
    public static final String RESP_CODE_MSG_x8D = "거래 조건이 맞지 않음";
    public static final String RESP_CODE_MSG_x95 = "명령/파라미터 오류";
    public static final String RESP_CODE_MSG_xCC = "망 취소(호스트 승인 후 카드 거절)";
    public static final String RESP_CODE_MSG_xCD = "단말기나 POS가 취소 시";
    public static final String RESP_CODE_MSG_xCE = "카드 거래 거절";
    public static final String RESP_CODE_MSG_xCF = "IC EMV 거래 FALLBACK";
    public static final String RESP_CODE_MSG_xD0 = "서명 값이 존재 하지 않음";
    public static final String RESP_CODE_MSG_xD1 = "암호화 KEY 가 존재하지 않음";
    public static final String RESP_CODE_MSG_xD2 = "단말기 ID가 일치하지 않음";
    public static final String RESP_CODE_MSG_xE1 = "MSR 동작 오류";
    public static final String RESP_CODE_MSG_xE2 = "IFM 동작 오류";
    public static final String RESP_CODE_MSG_xE4 = "PMF 검증 오류";
    public static final String RESP_CODE_MSG_xE6 = "PMF Index 불일치";
    public static final String RESP_CODE_MSG_xE8 = "IC 거래 우선 요망";
    public static final String RESP_CODE_MSG_xE9 = "FALLBACK 거래 아님";
    public static final String RESP_CODE_MSG_xEC = "SafeCard Key 일련번호가 없음";
    public static final String RESP_CODE_MSG_xF2 = "카드가 존재하지 않음";
    public static final String RESP_CODE_MSG_xF5 = "지원되지 않는 카드";
    public static final String RESP_CODE_MSG_xF8 = "요청 Message 의 Data 오류";
    public static final String RESP_CODE_MSG_xFA = "Reader 인증 오류";
    public static final String RESP_CODE_MSG_xFB = "Reader 인증이 되지 않음";
    public static final String RESP_CODE_MSG_xFC = "다운로드 프로그램 무결성 훼손";
    public static final String RESP_CODE_MSG_xFD = "암호화 키 무결성 훼손";
    public static final String RESP_CODE_MSG_xFF = "실패 Fallback 미처리";

    public static final String RESP_CODE_MSG_xXX = "알 수 없는 오류코드 수신";

    //==============================================================================================//
    // TIME-OUT VALUE
    //==============================================================================================//
    public static final long TIMEOUT_1SEC  = 1000;
    public static final long TIMEOUT_2SEC  = 2*1000;
    public static final long TIMEOUT_3SEC  = 3*1000;
    public static final long TIMEOUT_5SEC  = 5*1000;
    public static final long TIMEOUT_60SEC  = 60*1000;
}
