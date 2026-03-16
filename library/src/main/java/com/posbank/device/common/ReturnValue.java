package com.posbank.device.common;


/**
 * Return Code Value
 */
public class ReturnValue {
    //==============================================================================================//
    //  RETURN VALUES
    //==============================================================================================//
    // Common Return Code
    public static final int
            RTN_TRUE                 = (1),     // 사실
            RTN_SUCCESS              = (1),     // 성공
            RTN_FALSE                = (0),     // 거짓
            RTN_FAIL                 = (0),     // 실패
            RTN_TIMEOUT              = (-1),    // 시간초과
            RTN_CANCEL               = (-2),    // 취소
            RTN_INVALID_PARAM        = (-3),    // 파라메타 오류
            RTN_CONTINUE             = (-4),    // 진행(대기중)
            RTN_STOP                 = (-5);    // 중지

    // 통신 리턴코드 정의
    public static final int
            RTN_COMM_OK              = (1),     // Communication OK
            RTN_ERROR                = (0),     // Communication Error
            RTN_COMM_ERROR           = (0),     // Communication Error
            RTN_NOT_CONNECT          = (-101),  // Connect Error
            RTN_INVALID_DATA         = (-102),  // DATA 이상
            RTN_SEND_FAIL            = (-103),  // 전송오류
            RTN_READ_FAIL            = (-104),  // 수신오류
            RTN_READ_NAK             = (-105),  // NAK 수신
            RTN_READ_EOT             = (-106),  // EOT 수신
            RTN_LRC_FAIL             = (-107);  // LRC 오류

    // 보안패드 리턴코드 정의
    public static final int
            RTN_SCR_SUCCESS          = (1),     // SCR OK
            RTN_SCR_FAIL             = (0),     // SCR FAIL
            RTN_SCR_FALLBACK_OCCUR   = (80),    // Fallback 발생
            RTN_SCR_AID_SELECT       = (82),    // AID Label 선택
            RTN_SCR_DEVICE_ERROR     = (90),    // SCR 장치 오류
            RTN_SCR_ERR_FIRST_IC     = (91),    // IC 카드 우선 결제요망
            RTN_SCR_ERR_WAIT_TIMEOUT = (93),    // 카드 대기 시간 초과
            RTN_SCR_ERR_CARD_NUMBER  = (95),    // 카드번호 전문 형태 오류
            RTN_SCR_ERR_PUBLIC_KEY   = (96),    // 공개키 주입 요망
            RTN_SCR_ERR_EXCHANGE_KEY = (97),    // 키 교환 후 거래요망
            RTN_SCR_ERR_INTEGRITY    = (99),    // 무결성 실패
            RTN_SCR_MUTUAL_AUTH_FAIL = (100);   // 상호 인증 실패

    // 서명패드 리턴코드 정의
    public static final int
            RTN_SIGN_COMPLETE        = (1),     // SIGN 완료
            RTN_SIGN_ERROR           = (0),     // SIGN-PAD Error
            RTN_SIGN_DEVICE_ERROR    = (-201),  // 장치오류
            RTN_SIGN_CANCEL          = (-202),  // 서명취소(종료)
            RTN_SIGN_RETRY           = (-203),  // 서명정정(재서명)
            RTN_SIGN_NOT_USE         = (-204);  // 장치 미사용

    // KIS VAN 리턴코드 정의
    public static final int
            RTN_VAN_REPLY_OK         = (1),     // VAN Reply OK
            RTN_VAN_REPLY_ERROR      = (200);   // VAN Reply Error

}
