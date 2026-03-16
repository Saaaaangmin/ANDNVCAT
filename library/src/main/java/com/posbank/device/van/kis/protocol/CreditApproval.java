package com.posbank.device.van.kis.protocol;


import com.posbank.device.screader.kis.protocol.ScrProtocolCom;
import com.posbank.device.van.kis.model.KisInstance;

import java.text.DecimalFormat;

import static com.posbank.device.common.AscII.CH_SPACE;
import static com.posbank.device.common.ReturnValue.RTN_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_SCR_ERR_EXCHANGE_KEY;
import static com.posbank.device.common.ReturnValue.RTN_SCR_ERR_FIRST_IC;
import static com.posbank.device.common.ReturnValue.RTN_SCR_FALLBACK_OCCUR;
import static com.posbank.device.common.ReturnValue.RTN_SCR_MUTUAL_AUTH_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_SCR_SUCCESS;
import static com.posbank.device.common.ReturnValue.RTN_SUCCESS;
import static com.posbank.device.common.ReturnValue.RTN_VAN_REPLY_ERROR;
import static com.posbank.device.common.Utils.MakeMsgDateTime_yyMMdd;
import static com.posbank.device.screader.kis.model.ScrConstant.KeyRenewal_Renewal;
import static com.posbank.device.van.kis.model.VanConstant.CARD_BRAND_CUP;
import static com.posbank.device.van.kis.model.VanConstant.DEVELOPER_NAME;
import static com.posbank.device.van.kis.model.VanConstant.GROUPCODE_NORMAL;
import static com.posbank.device.van.kis.model.VanConstant.KIS_DEAL_CODE_CREDIT_CAN;
import static com.posbank.device.van.kis.model.VanConstant.KIS_DEAL_CODE_CREDIT_REQ;
import static com.posbank.device.van.kis.model.VanConstant.KIS_DEAL_CODE_CREDIT_R_CAN;
import static com.posbank.device.van.kis.model.VanConstant.KIS_RTN_HOST_CONNECT_FAIL;
import static com.posbank.device.van.kis.model.VanConstant.KIS_RTN_NET_CANCEL_T1;
import static com.posbank.device.van.kis.model.VanConstant.KIS_RTN_NET_CANCEL_T2;
import static com.posbank.device.van.kis.model.VanConstant.KIS_RTN_NET_CANCEL_T3;
import static com.posbank.device.van.kis.model.VanConstant.KIS_RTN_SUCCESS;
import static com.posbank.device.van.kis.model.VanConstant.REPLY_CODE_OK;
import static com.posbank.device.van.kis.model.VanConstant.SCR_WCC_IC;
import static com.posbank.device.van.kis.model.VanConstant.SCR_WCC_MSR;
import static com.posbank.device.van.kis.model.VanConstant.SIGN_NO;
import static com.posbank.device.van.kis.model.VanConstant.SPEC_TYPE_NACF;
import static com.posbank.device.van.kis.model.VanConstant.WCC_FB;
import static com.posbank.device.van.kis.model.VanConstant.WCC_IC;
import static com.posbank.device.van.kis.model.VanConstant.WCC_KEYIN;
import static com.posbank.device.van.kis.model.VanConstant.WCC_SWIPE;
import static kisvan.Kisvan.Init;
import static kisvan.Kisvan.KIS_Approval;
import static kisvan.Kisvan.inCatId;
import static kisvan.Kisvan.inDeveloperName;
import static kisvan.Kisvan.inDeviceAuthValue;
import static kisvan.Kisvan.inGroupCode;
import static kisvan.Kisvan.inInstallment;
import static kisvan.Kisvan.inOrgAuthDate;
import static kisvan.Kisvan.inOrgAuthNo;
import static kisvan.Kisvan.inSafeCardICData;
import static kisvan.Kisvan.inSafeCardMSData;
import static kisvan.Kisvan.inServerIP;
import static kisvan.Kisvan.inServerPort;
import static kisvan.Kisvan.inSpecType;
import static kisvan.Kisvan.inSvcAmt;
import static kisvan.Kisvan.inTranAmt;
import static kisvan.Kisvan.inTranCode;
import static kisvan.Kisvan.inVatAmt;
import static kisvan.Kisvan.inWCC;
import static kisvan.Kisvan.inYNSign;
import static kisvan.Kisvan.outAccepterCode;
import static kisvan.Kisvan.outAccepterName;
import static kisvan.Kisvan.outAuthNo;
import static kisvan.Kisvan.outDisplayMsg;
import static kisvan.Kisvan.outEMVData;
import static kisvan.Kisvan.outIssuerCode;
import static kisvan.Kisvan.outIssuerName;
import static kisvan.Kisvan.outJanAmt;
import static kisvan.Kisvan.outMerchantRegNo;
import static kisvan.Kisvan.outRecvData;
import static kisvan.Kisvan.outReplyCode;

public class CreditApproval {
    //
    private KisInstance vanInstance;

    // ScrProtocolCom
    private ScrProtocolCom scr;

    //
    private String tcpAddress;      // 결제요청 Server IP
    private String tcpPort;         // 결제요청 Server Port
    private String terminalID;      // 가맹점 TID

    // Reply Message
    private String replyMessage;    // 응답 메시지

    private String replyCode;       // 응답코드
    private String janAmt;          // GIFT 카드잔액,
    private String accepterCode;    // 매입사코드
    private String accepterName;    // 매입사명
    private String authNo;          // 승인번호
    private String issuerCode;      // 발급사코드
    private String issuerName;      // 발급사명
    private String merchantRegNo;   // 카드사 가맹점번호
    private String displayMsg;      // 거래 메시지

    /**
     * Constructor
     * @param _VanInstance : KIS VAN Instance
     * @param _Scr : SCR Protocol
     */
    public CreditApproval(KisInstance _VanInstance,
                          ScrProtocolCom _Scr) {
        this.vanInstance = _VanInstance;
        this.scr = _Scr;

        this.tcpAddress = _VanInstance.getTcpAddress();
        this.tcpPort    = _VanInstance.getTcpPort();
        this.terminalID = _VanInstance.getTerminalID();
    }

    /**
     * Run Approval Credit Card Transaction
     */
    public int runCreditApproval(String _transType,
                                 int _Amount,
                                 int _Tax,
                                 int _Tip,
                                 int _Installment,
                                 int _CardWaitTime) {
        /*
         * 초기화.
         */
        Init();

        // KIS 서버 주소, AN
        inServerIP = tcpAddress;
        // KIS 서버 포트, AN
        inServerPort = Integer.parseInt(tcpPort);
        // 스펙구분, AN
        inSpecType = SPEC_TYPE_NACF;
        // 그룹코드, AN
        inGroupCode = GROUPCODE_NORMAL;
        // 개발업체 구분, AN
        inDeveloperName = DEVELOPER_NAME;
        // 전문구분코드, AN
        inTranCode = KIS_DEAL_CODE_CREDIT_REQ;

        /*
         * Get SCR System Information
         */
        int scrResultCode = scr.getSystemInformation();
        if (scrResultCode != RTN_SUCCESS) {
            scr.setReturnCodeMsg(scrResultCode);
            return RTN_FAIL;
        }

        // 단말기인증값, AN(Max:32)
        StringBuilder sbAuthValue;
        sbAuthValue = new StringBuilder();
        sbAuthValue.append(new String(scr.rxF.mModelName));
        sbAuthValue.append(new String(scr.rxF.mSWVersion));
        sbAuthValue.append(vanInstance.getAppCertification());
        sbAuthValue.append(vanInstance.getAppVersion());
        inDeviceAuthValue = sbAuthValue.toString();

        // Fallback 거래 여부
        boolean isFallbackTrans = false;
        // 고객 카드 브랜드
        byte bCardBrand = CH_SPACE;

        /*
         * 고객 신용카드 대기
         */
        while(true) {
            scrResultCode = scr.startCreditTransaction(terminalID, _transType, _Amount, _CardWaitTime);
            if (scrResultCode != RTN_SCR_SUCCESS) {
                // IC 카드 우선거래
                if (scrResultCode == RTN_SCR_ERR_FIRST_IC) {
                    continue;
                }
                // Fallback
                else if (scrResultCode == RTN_SCR_FALLBACK_OCCUR) {
                    // Fallback 발생... Fallback 거래로 계속 진행
                    isFallbackTrans = true;
                    break;
                }
                // 상호 인증 되지 않음
                else if (scrResultCode == RTN_SCR_MUTUAL_AUTH_FAIL) {
                    // 상호인증
                    MutualAuthorization mutualAuth = new MutualAuthorization(scr);
                    int resultCode = mutualAuth.runMutualAuthorization();
                    if (resultCode == RTN_SUCCESS) {
                        // 상호인증 검증성공.
                        continue;
                    }
                    else {
                        // 상호인증 검증실패
                        scr.setReturnCodeMsg(scrResultCode);
                        return RTN_FAIL;
                    }
                }
                // 키 갱신 거래진행
                else if (scrResultCode == RTN_SCR_ERR_EXCHANGE_KEY ) {
                    // Key 갱신 거래
                    KeyRenewalSynchronize renewalKey = new KeyRenewalSynchronize(vanInstance, scr);
                    int resultCode = renewalKey.runKeyRenewal(KeyRenewal_Renewal);
                    if (resultCode == RTN_SUCCESS) {
                        // 성공
                        continue;
                    } else {
                        // 실패
                        scr.setReturnCodeMsg(scrResultCode);
                        return RTN_FAIL;
                    }
                }

                scr.setReturnCodeMsg(scrResultCode);
                return RTN_FAIL;
            }
            // 카드 Reading 완료... 계속 진행
            break;
        }

        /*
         * Fallback 거래(MS 카드 대기)
         */
        if (isFallbackTrans) {
            // MS 카드대기...
            scrResultCode = scr.fallbackTransaction("", _CardWaitTime);
            if (scrResultCode != RTN_SCR_SUCCESS) {
                scr.setReturnCodeMsg(scrResultCode);
                return RTN_FAIL;
            }

            /*
             * MSR 신용카드 리딩 성공
             */
            // 암호화 MS 데이터(fallback 거래에 한함)
            StringBuilder sbSafeCardMSData = new StringBuilder();
            sbSafeCardMSData.append(new String(scr.rxF.mFallbackReasonCode));
            sbSafeCardMSData.append(new String(scr.rxF.mVanID));
            sbSafeCardMSData.append(new String(scr.rxF.mEncryptedInfo));
            sbSafeCardMSData.append(new String(scr.rxF.mCreatedMAC));
            sbSafeCardMSData.append(new String(scr.rxF.mTerminalID));
            sbSafeCardMSData.append(new String(scr.rxF.mKSN));
            sbSafeCardMSData.append(new String(scr.rxF.mTPL));
            sbSafeCardMSData.append(new String(scr.rxF.mReaderAuthID));
            sbSafeCardMSData.append(new String(scr.rxF.mPMKValidity));
            sbSafeCardMSData.append(new String(scr.rxF.mFallbackCode));
            sbSafeCardMSData.append(new String(scr.rxF.mCardBinNo));
            inSafeCardMSData = sbSafeCardMSData.toString();

            // Wcc
            inWCC = WCC_FB;
        }
        /*
         * IC/MS 신용거래
         */
        else {
            /*
             * 신용카드(IC/MSR) 리딩 성공
             */
            // 암호화 IC 데이터
            StringBuilder sbSafeCardICData = new StringBuilder();
            sbSafeCardICData.append(new String(scr.rxF.mCardProperty));
            sbSafeCardICData.append(new String(scr.rxF.mCVM));
            sbSafeCardICData.append(new String(scr.rxF.mVanID));
            sbSafeCardICData.append(new String(scr.rxF.mEncryptedInfo));
            sbSafeCardICData.append(new String(scr.rxF.mCreatedMAC));
            sbSafeCardICData.append(new String(scr.rxF.mTerminalID));
            sbSafeCardICData.append(new String(scr.rxF.mKSN));
            sbSafeCardICData.append(new String(scr.rxF.mTPL));
            sbSafeCardICData.append(new String(scr.rxF.mReaderAuthID));
            sbSafeCardICData.append(new String(scr.rxF.mPMKValidity));
            sbSafeCardICData.append(new String(scr.rxF.mFallbackCode));
            sbSafeCardICData.append(new String(scr.rxF.mCardBinNo));
            sbSafeCardICData.append(new String(scr.rxF.mChipDataLength));
            if (scr.rxF.miChipDataLength > 0) {
                sbSafeCardICData.append(new String(scr.rxF.mICEmvChipData));
            }
            inSafeCardICData = sbSafeCardICData.toString();

            // Wcc("C"/"S"/"K")
            inWCC = setPropertyWcc(scr.rxF.mCardProperty[1]);

            // 카드브랜드
            bCardBrand = scr.rxF.mCardProperty[3];
        }

        // 단말기번호(TID)
        if (bCardBrand == CARD_BRAND_CUP) {
            inCatId = terminalID + "CU";    // 은련카드 승인
        } else {
            inCatId = terminalID;           // 일반카드 승인
        }

        // 결제금액
        inTranAmt = String.valueOf(_Amount);
        // 부가세액
        inVatAmt = String.valueOf(_Tax);
        // 봉사료
        if (_Tip > 0) {
            inSvcAmt = String.valueOf(_Tip);
        }
        // 할부개월
        DecimalFormat df = new DecimalFormat("00");
        inInstallment = df.format(_Installment);

        // 전자서명 여부
        inYNSign = SIGN_NO;     // 전자서명 없음

        /*
         * 결제요청...
         */
        int iKISReplyCode = KIS_Approval();
        if (iKISReplyCode == KIS_RTN_SUCCESS) {
            /*
             * VAN 응답코드 "0000"
             */
            if (outReplyCode.equals(REPLY_CODE_OK)) {
                /*
                 * IC EMV 후처리
                 */
                if (!isFallbackTrans && inWCC.equals(WCC_IC)) {
                    // EMV Length
                    String emvLength = outEMVData.substring(0, 4);
                    scr.txF.mEmvLength = emvLength.getBytes();
                    // Authorization Response Code(2)
                    String emvResCode = outEMVData.substring(4, 6);
                    scr.txF.mResponseCode = emvResCode.getBytes();
                    // AddResData
                    String emvAddResData = outEMVData.substring(6, 33);
                    scr.txF.mARD = emvAddResData.getBytes();
                    // IAD
                    String emvIAD = outEMVData.substring(33, 67);
                    scr.txF.mIAD = emvIAD.getBytes();
                    // IssueScript
                    String emvIssueScript = outEMVData.substring(67, 326);
                    scr.txF.mIS = emvIssueScript.getBytes();

                    scrResultCode = scr.completeEmvTransaction(scr.txF.mEmvLength,
                                                            scr.txF.mResponseCode,
                                                            scr.txF.mARD,
                                                            scr.txF.mIAD,
                                                            scr.txF.mIS);
                    if (scrResultCode != RTN_SUCCESS) {
                        setReplyMessage("IC 카드 Online 데이터 처리오류");
                        // 취소 실행...
                        //======================================================================
                        // 전문구분코드
                        inTranCode = KIS_DEAL_CODE_CREDIT_CAN;
                        // 승인번호
                        inOrgAuthNo = outAuthNo.trim();
                        // 원거래일자(YYMMDD)
                        inOrgAuthDate = outRecvData.substring(14, 20);
                        //======================================================================
                        // 취소요청...
                        iKISReplyCode = KIS_Approval();
                        if (iKISReplyCode == KIS_RTN_SUCCESS) {
                            setReplyMessage("IC 카드 Online 데이터 처리오류");
                            if (outReplyCode.equals(REPLY_CODE_OK)) {
                                setReplyMessage("다시거래해 주세요.");
                                return RTN_VAN_REPLY_ERROR;
                            } else {
                                setReplyMessage("고객센터 문의요망!!!");
                                return RTN_VAN_REPLY_ERROR;
                            }
                        }
                        else if (iKISReplyCode == KIS_RTN_HOST_CONNECT_FAIL) {
                            setReplyMessage("인터넷 연결 확인필요!");
                            return RTN_VAN_REPLY_ERROR;
                        }
                        else {
                            // 망취소
                            setReplyMessage("");
                            return RTN_VAN_REPLY_ERROR;
                        }
                    }
                }

                /*
                 * 정상승인 완료
                 */
                setReplyMessage("승인번호 : " + outAuthNo.trim());

                setReplyCode(outReplyCode.trim());          // 응답코드
                setJanAmt(outJanAmt.trim());                // GIFT 카드잔액
                setAccepterCode(outAccepterCode.trim());    // 매입사코드
                setAccepterName(outAccepterName.trim());    // 매입사명
                setAuthNo(outAuthNo.trim());                // 승인번호
                setIssuerCode(outIssuerCode.trim());        // 발급사코드
                setIssuerName(outIssuerName.trim());        // 발급사명
                setMerchantRegNo(outMerchantRegNo.trim());  // 가맹점번호
                setDisplayMsg(outDisplayMsg.trim());        // 메시지

                return RTN_SUCCESS;
            } else {
                setReplyMessage(outDisplayMsg.trim() + "(" + outReplyCode + ")");
                return RTN_VAN_REPLY_ERROR;
            }
        }
        // KIS Host 접속실패
        else if (iKISReplyCode == KIS_RTN_HOST_CONNECT_FAIL) {
            setReplyMessage("인터넷 연결 확인필요!");
            return RTN_VAN_REPLY_ERROR;
        }
        // 망취소
        else if (iKISReplyCode == KIS_RTN_NET_CANCEL_T1 ||
                iKISReplyCode == KIS_RTN_NET_CANCEL_T2 ||
                iKISReplyCode == KIS_RTN_NET_CANCEL_T3) {
            //======================================================================================
            // 전문구분코드
            inTranCode = KIS_DEAL_CODE_CREDIT_R_CAN;
            // 원거래일자
            inOrgAuthDate = new String(MakeMsgDateTime_yyMMdd());
            //======================================================================================
            iKISReplyCode = KIS_Approval();
            if (iKISReplyCode == KIS_RTN_SUCCESS) {

                setReplyMessage(outDisplayMsg.trim());

                if (outReplyCode.equals(REPLY_CODE_OK)) {
                    setReplyMessage("망취소. 다시거래해 주세요.");
                    return RTN_VAN_REPLY_ERROR;
                } else {
                    setReplyMessage("망취소 오류. 카드사 문의요망.");
                    return RTN_VAN_REPLY_ERROR;
                }
            }
            else if (iKISReplyCode == KIS_RTN_HOST_CONNECT_FAIL) {
                setReplyMessage("인터넷 연결 확인필요!");
                return RTN_VAN_REPLY_ERROR;
            }
            else {
                setReplyMessage("");
                return RTN_VAN_REPLY_ERROR;
            }
        }
        // 실패
        else {
            setReplyMessage("처리오류! 다시거래해 주세요.");
            return RTN_VAN_REPLY_ERROR;
        }
    }

    /**
     * Set Property WCC, SCR WCC 값으로 KIS VAN WCC 설정
     * @param scrWcc : 결제구분
     * @return : String
     */
    private String setPropertyWcc(byte scrWcc) {
        String sWcc;
        if (scrWcc ==  SCR_WCC_IC) {
            sWcc = WCC_IC;
        } else if (scrWcc ==  SCR_WCC_MSR) {
            sWcc = WCC_SWIPE;
        } else {
            sWcc = WCC_KEYIN;
        }
        return sWcc;
    }

    /**
     * Set Credit Approval Reply Message
     */
    private void setReplyMessage(String msg) {
        this.replyMessage = msg;
    }

    /**
     * Get Credit Approval Reply Message
     * @return :
     */
    public String getReplyMessage() {
        return replyMessage;
    }

    /**
     * Set outReplyCode(응답코드)
     * @param replyCode : outReplyCode
     */
    private void setReplyCode(String replyCode) {
        this.replyCode = replyCode;
    }

    /**
     * Get outReplyCode
     * @return : replyCode
     */
    public String getReplyCode() {
        return replyCode;
    }

    /**
     * Set outJanAmt(잔액)
     * @param janAmt : outJanAmt
     */
    private void setJanAmt(String janAmt) {
        this.janAmt = janAmt;
    }

    /**
     * Get outJanAmt
     * @return : janAmt
     */
    public String getJanAmt() {
        return janAmt;
    }

    /**
     * Set outAccepterCode(매입사코드)
     * @param accepterCode : outAccepterCode
     */
    private void setAccepterCode(String accepterCode) {
        this.accepterCode = accepterCode;
    }

    /**
     * Get outAccepterCode(매입사 코드)
     * @return : accepterCode
     */
    public String getAccepterCode() {
        return accepterCode;
    }

    /**
     * Set outAccepterName(매입사 명)
     * @param accepterName :outAccepterName
     */
    private void setAccepterName(String accepterName) {
        this.accepterName = accepterName;
    }

    /**
     * Get outAccepterName(매입사 명)
     * @return : accepterName
     */
    public String getAccepterName() {
        return accepterName;
    }

    /**
     * Set outAuthNo(승인번호)
     * @param authNo : outAuthNo
     */
    private void setAuthNo(String authNo) {
        this.authNo = authNo;
    }

    /**
     * Get outAuthNo(승인번호)
     * @return : authNo
     */
    public String getAuthNo() {
        return authNo;
    }

    /**
     * Set outIssuerCode(발급사코드)
     * @param issuerCode : outIssuerCode
     */
    private void setIssuerCode(String issuerCode) {
        this.issuerCode = issuerCode;
    }

    /**
     * Get outIssuerCode(발급사코드)
     * @return : issuerCode
     */
    public String getIssuerCode() {
        return issuerCode;
    }

    /**
     * Set outIssuerName(발급사명)
     * @param issuerName : outIssuerName
     */
    private void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    /**
     * Get outIssuerName(발급사명)
     * @return : issuerName
     */
    public String getIssuerName() {
        return issuerName;
    }

    /**
     * Set outMerchantRegNo(가맹점번호)
     * @param merchantRegNo :outMerchantRegNo
     */
    private void setMerchantRegNo(String merchantRegNo) {
        this.merchantRegNo = merchantRegNo;
    }

    /**
     * Get outMerchantRegNo(가맹점번호)
     * @return : merchantRegNo
     */
    public String getMerchantRegNo() {
        return merchantRegNo;
    }

    /**
     * Set outDisplayMsg(거래 메시지)
     * @param displayMsg : outDisplayMsg
     */
    private void setDisplayMsg(String displayMsg) {
        this.displayMsg = displayMsg;
    }

    /**
     * Get outDisplayMsg(거래 메시지)
     * @return : displayMsg
     */
    public String getDisplayMsg() {
        return displayMsg;
    }
}
