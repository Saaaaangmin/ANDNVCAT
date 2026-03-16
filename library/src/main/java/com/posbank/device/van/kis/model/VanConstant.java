package com.posbank.device.van.kis.model;

/**
 *
 */

public class VanConstant {
    /*
     * TEST 가맹점 정보
     */
    // 사업자 번호
    public static final String SHOP_NUMBER       = "1168143939";
    // TID
    public static final String TERMINAL_ID       = "90100546";
    // 단말기 일련번호
    public static final String TERMINAL_NUMBER   = "13";
    // 단말기 비밀번호(GUI 입력처리 )
    public static final String TERMINAL_PASSWORD = "0054";
    // 지역번호(GUI 입력처리 )
    public static final String LOCAL_NUMBER      = "02";

    /*
     * 결제요청 VAN Server 접속정보
     */
    // TEST
    public static final String APPROVAL_HOST_IP_ADDRESS_TEST = "210.112.100.97";
    public static final String APPROVAL_HOST_PORT_TEST = "9019";
    // REAL
    public static final String APPROVAL_HOST_IP_ADDRESS_REAL = "210.112.100.63";
    public static final String APPROVAL_HOST_PORT_REAL = "9011";

    /*
     * 가맹점정보 다운로드 VAN Server 접속정보
     */
    // TEST
    public static final String DOWNLOAD_HOST_IP_ADDRESS_TEST = "210.112.100.97";
    public static final String DOWNLOAD_HOST_PORT_TEST = "9002";
    // REAL
    public static final String DOWNLOAD_HOST_IPADDRESS_REAL = "210.112.100.63";
    public static final String DOWNLOAD_HOST_PORT_REAL = "9001";

    /*
     * 서비스별 전문 구분값 (NACF3에 한함)
     */
    // IC 암호화키 다운로드
    public static final String KIS_DEAL_CODE_KEY_DOWN_REQ = "KD";    // IC 암호키 다운로드

    // 신용
    public static final String KIS_DEAL_CODE_CREDIT_REQ   = "D1";    // 신용카드 승인
    public static final String KIS_DEAL_CODE_CREDIT_CAN   = "D2";    // 신용카드 취소
    public static final String KIS_DEAL_CODE_CREDIT_R_CAN = "R2";    // 신용카드 망취소

    // 현금(OffLine)
    public static final String KIS_DEAL_CODE_MONEY_REQ    = "K1";    // 현금 승인
    public static final String KIS_DEAL_CODE_MONEY_CAN    = "K2";    // 현금 취소

    // 현금영수증
    public static final String KIS_DEAL_CODE_CASH_REQ     = "H1";    // 현금영수증 승인
    public static final String KIS_DEAL_CODE_CASH_CAN     = "H2";    // 현금영수증 취소
    public static final String KIS_DEAL_CODE_CASH_R_CAN   = "H4";    // 현금영수증 망취소

    // 수표조회
    public static final String KIS_DEAL_CODE_CHECKER_INQ  = "C1";    // 수표조회

    // OCB
    public static final String KIS_DEAL_CODE_OCB_INQ      = "MQ";    // 조회
    public static final String KIS_DEAL_CODE_OCB_ACC_REQ  = "MS";    // 적립
    public static final String KIS_DEAL_CODE_OCB_ACC_CAN  = "MR";    // 적립취소
    public static final String KIS_DEAL_CODE_OCB_USE_REQ  = "M3";    // 사용
    public static final String KIS_DEAL_CODE_OCB_USE_CAN  = "H4";    // 사용취소

    //==============================================================================================//
    // 동글입력구분(MultiPAD)
    //==============================================================================================//
    // 거래구분
    public static final String KIS_DONGLE_TRANS_RF        = "RF";
    public static final String KIS_DONGLE_TRANS_IR        = "IR";
    // 이통사구분
    public static final char KIS_DONGLE_AGENCY_SK         = 'S',
                             KIS_DONGLE_AGENCY_KTF        = 'K',
                             KIS_DONGLE_AGENCY_LGT        = 'L';
    // 카드발급방식
    public static final char KIS_DONGLE_CARD_TYPE_MOBILE  = 'M',
                             KIS_DONGLE_CARD_TYPE_PLASTIC = 'P';
    // 카드종류
    public static final char KIS_DONGE_CARD_KIND_VISAWAVE = 'V',
                             KIS_DONGE_CARD_KIND_PAYPASS  = 'P',
                             KIS_DONGE_CARD_KIND_SKT_RF   = 'S',
                             KIS_DONGE_CARD_KIND_KTF_RF   = 'K',
                             KIS_DONGE_CARD_KIND_LGT_RF   = 'L',
                             KIS_DONGE_CARD_KIND_IR       = 'I',
                             KIS_DONGE_CARD_KIND_MIFARE   = 'M';

    //==============================================================================================//
    // 유종정보
    //==============================================================================================//
    // 면세유공급구분
    public static final char KIS_DUTYFREE_OIL_KIND_FARMMECA    = '1',    // 농기계
                             KIS_DUTYFREE_OIL_KIND_SHIP        = '2',    // 선박
                             KIS_DUTYFREE_OIL_KIND_FARMING     = '3',    // 양식
                             KIS_DUTYFREE_OIL_KIND_SHINHANTAXI = '4';    // 신한택시
    // 유종코드
    public static final char KIS_OIL_KIND_GASOLINE   = '1',  // 휘발유
                             KIS_OIL_KIND_LEROSENE_H = '2',  // 실내등유
                             KIS_OIL_KIND_LEROSENE_B = '3',  // 보일러등유
                             KIS_OIL_KIND_DIESEL     = '4',  // 경유
                             KIS_OIL_KIND_LUBRICANT  = '5',  // 윤활유
                             KIS_OIL_KIND_HEAVYOIL   = '6',  // 중유
                             KIS_OIL_KIND_GAS        = '7';  // 가스

    //==============================================================================================//
    // 프로토콜 필드
    //==============================================================================================//
    // 그룹코드
    public static final String GROUPCODE_NORMAL = "000000";
    public static final String GROUPCODE_SSG    = "000002";

    // 스펙구분
    public static final String SPEC_TYPE_NACF      = "NACF";
    public static final String SPEC_TYPE_NEWCAT    = "NEWCAT";
    public static final String SPEC_TYPE_CATUPLOAD = "CATUPLOAD";

    // 전자서명 여부
    public static final String SIGN_YES = "Y";  // 전자서명 포함
    public static final String SIGN_NO  = "N";  // 전자서명 없음

    // 상세전문구분
    public static final String KEY_GUBUN_0200 = "0200";  // PMK 분배 - 최초
    public static final String KEY_GUBUN_0400 = "0400";  // PMK 갱신 - 유효기간 만료시

    // 기관코드
    public static final String VAN_ID_KIS = "09";  // KIS 정보통신, 기관코드

    // 개발업체구분
    public static final String DEVELOPER_NAME = "POSBANK"; // POS 개발업체명

    // WCC
    public static final String WCC_SWIPE = "S"; // Swipe
    public static final String WCC_KEYIN = "K"; // Key-in
    public static final String WCC_IC    = "C"; // IC
    public static final String WCC_FB    = "F"; // Fallback

    // 현금영수증 발급구분
    public static final String DEDUCT_PERSONAL = "00"; // 개인
    public static final String DEDUCT_BUSINESS = "01"; // 사업자

    // 카드브랜드 해외은련(CUP)
    public static final byte CARD_BRAND_CUP = 'C'; // 해외은련(CUP)

    // CVM
    public static final byte CVM_NO    = '0'; // NO CVM(No Pin & No Sign)
    public static final byte CVM_PIN   = '1'; // Online Pin 필요
    public static final byte CVM_SIGN  = '2'; // Signature 필요
    public static final byte CVM_SIGN2 = '3'; // Signature + 자필서명 확인

    //==============================================================================================//
    // 응답코드
    //==============================================================================================//
    public static final String REPLY_CODE_OK = "0000";          // 성공
    public static final String ALREADY_CANCEL_CREDIT = "5006";  // 기취소
    public static final String ALREADY_CANCEL_CASH   = "7573";  // 현금영수증 기취소

    //==============================================================================================//
    // SCR WCC
    //==============================================================================================//
    public static final byte SCR_WCC_IC  = 'I';     // IC
    public static final byte SCR_WCC_RF  = 'R';     // RF
    public static final byte SCR_WCC_MSR = 'M';     // MSR

    //==============================================================================================//
    //  Kisvan 메서드 RETURN VALUES
    //==============================================================================================//
    // Common Return Code
    public static final int KIS_RTN_SUCCESS = (0);      // 성공

    public static final int KIS_RTN_NET_CANCEL_T1 = (97);   // TIMEOUT(망취소)
    public static final int KIS_RTN_NET_CANCEL_T2 = (98);   // TIMEOUT(망취소)
    public static final int KIS_RTN_NET_CANCEL_T3 = (99);   // TIMEOUT(망취소)

    public static final int KIS_RTN_HOST_CONNECT_FAIL = (-23);  // 접속실패
}
