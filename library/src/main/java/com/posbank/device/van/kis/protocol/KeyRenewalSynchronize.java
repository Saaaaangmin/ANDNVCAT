package com.posbank.device.van.kis.protocol;


import com.posbank.device.screader.kis.protocol.ScrProtocolCom;
import com.posbank.device.van.kis.model.KisInstance;

import java.nio.ByteBuffer;

import static com.posbank.device.common.ReturnValue.RTN_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_NOT_CONNECT;
import static com.posbank.device.common.ReturnValue.RTN_SCR_ERR_EXCHANGE_KEY;
import static com.posbank.device.common.ReturnValue.RTN_SCR_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_SCR_SUCCESS;
import static com.posbank.device.common.ReturnValue.RTN_SUCCESS;
import static com.posbank.device.common.Utils.ArrayCopyAsSize;
import static com.posbank.device.common.Utils.LeadingZerosString;
import static com.posbank.device.screader.kis.model.ScrConstant.KeyRenewal_First;
import static com.posbank.device.screader.kis.model.ScrConstant.KeyRenewal_Renewal;
import static com.posbank.device.van.kis.model.VanConstant.DEVELOPER_NAME;
import static com.posbank.device.van.kis.model.VanConstant.GROUPCODE_NORMAL;
import static com.posbank.device.van.kis.model.VanConstant.KEY_GUBUN_0200;
import static com.posbank.device.van.kis.model.VanConstant.KEY_GUBUN_0400;
import static com.posbank.device.van.kis.model.VanConstant.KIS_DEAL_CODE_KEY_DOWN_REQ;
import static com.posbank.device.van.kis.model.VanConstant.KIS_RTN_SUCCESS;
import static com.posbank.device.van.kis.model.VanConstant.SIGN_NO;
import static com.posbank.device.van.kis.model.VanConstant.SPEC_TYPE_NACF;
import static com.posbank.device.van.kis.model.VanConstant.VAN_ID_KIS;
import static kisvan.Kisvan.Init;
import static kisvan.Kisvan.KIS_Approval;
import static kisvan.Kisvan.inCatId;
import static kisvan.Kisvan.inDIKSerialNo;
import static kisvan.Kisvan.inDeveloperName;
import static kisvan.Kisvan.inGroupCode;
import static kisvan.Kisvan.inIC_DeviceId;
import static kisvan.Kisvan.inIC_KeyDownGubun;
import static kisvan.Kisvan.inIC_RandomNo;
import static kisvan.Kisvan.inPMKSerialNo;
import static kisvan.Kisvan.inServerIP;
import static kisvan.Kisvan.inServerPort;
import static kisvan.Kisvan.inSpecType;
import static kisvan.Kisvan.inTranCode;
import static kisvan.Kisvan.inVanId;
import static kisvan.Kisvan.inYNSign;
import static kisvan.Kisvan.outIC_EncKeyData;


public class KeyRenewalSynchronize {
    // ScrProtocolCom
    private ScrProtocolCom scr;

    //
    private String tcpAddress;
    private String tcpPort;
    private String terminalID;

    /*
     * Host 암호화 키 다운로드 Response
     */
    private ByteBuffer bBOutIC_EncKeyData;

    private byte[] baVanID = new byte[2];
    private byte[] baTerminalID = new byte[10];
    private byte[] baDIKnSerialNo = new byte[16];
    private byte[] baPMKSerialNo = new byte[6];
    private byte[] baEncryptedValue = new byte[256];
    private byte[] baPMKValidity = new byte[6];
    private byte[] baTPL = new byte[3];

    /**
     * Constructor
     * @param _vanInstance : KIS VAN Instance
     * @param _scr : SCR Protocol
     */
    public KeyRenewalSynchronize(KisInstance _vanInstance, ScrProtocolCom _scr) {
        this.tcpAddress = _vanInstance.getTcpAddress();
        this.tcpPort    = _vanInstance.getTcpPort();
        this.terminalID = _vanInstance.getTerminalID();
        this.scr = _scr;
    }

    /**
     * Execute Key Synchronize
     * @param renewalKeyGuBoon : 키 갱신종류(1:최초(분배) / 2:갱신)
     * @return Success/Fail
     */
    public int runKeyRenewal(String renewalKeyGuBoon) {
        // POS TID(CAT-ID)
        String sPosTID = LeadingZerosString(terminalID, 10);

        /*
         * 키 정보 요청
         */
        int scrResultCode = scr.synchronizeEncryptKey(renewalKeyGuBoon, sPosTID);
        if (scrResultCode != RTN_SCR_SUCCESS && scrResultCode != RTN_SCR_ERR_EXCHANGE_KEY) {
            if (scrResultCode != RTN_SCR_FAIL) {
                scr.setReturnCodeMsg(scrResultCode);
            }
            return RTN_FAIL;
        }

        // TID, AN(10)
        String sScrTID = new String(scr.rxF.mTerminalID);
        // Device ID, AN(10)
        String sDeviceID = new String(scr.rxF.mDeviceID);
        // DIKn 일련번호, AN(16)
        String sDIKnSerialNo = new String(scr.rxF.mDIKnSerialNo);
        // PMK 일련번호, AN(6)
        String sPMKSerialNo = new String(scr.rxF.mPMKSerialNo);
        // Random, AN(32)
        String sRandom = new String(scr.rxF.mRandom);

        /*
         * TID(CAT-ID)가 다르면 최초 KEY 다운로드를 진행
         */
        if (scrResultCode == RTN_SCR_ERR_EXCHANGE_KEY) {
            // 키 다운로드(최초(분배))
            int rcDownload = keyDownloadHost(KeyRenewal_First,
                    sPosTID, sDeviceID, sDIKnSerialNo, sPMKSerialNo, sRandom);
            if (rcDownload == RTN_SUCCESS) {
                /*
                 * outIC_EncKeyData
                 */
                // VAN ID(2)
                ArrayCopyAsSize(baVanID, bBOutIC_EncKeyData, 2);
                // TID(10)
                ArrayCopyAsSize(baTerminalID, bBOutIC_EncKeyData, 10);
                // DIKn 일련번호(16)
                ArrayCopyAsSize(baDIKnSerialNo, bBOutIC_EncKeyData, 16);
                // PMK 일련번호(6)
                ArrayCopyAsSize(baPMKSerialNo, bBOutIC_EncKeyData, 6);
                // 암호값(256)
                ArrayCopyAsSize(baEncryptedValue, bBOutIC_EncKeyData, 256);
                // PMK 유효기간(6)
                ArrayCopyAsSize(baPMKValidity, bBOutIC_EncKeyData, 6);
                // TPL(3)
                ArrayCopyAsSize(baTPL, bBOutIC_EncKeyData, 3);

                // SafeCard 암호화 Key 정보 다운로드 요청(최초(분배))
                scrResultCode = scr.downloadEncryptKey(KeyRenewal_First, baVanID, baTerminalID,
                        baDIKnSerialNo, baPMKSerialNo, baEncryptedValue, baPMKValidity, baTPL);
                if (scrResultCode != RTN_SCR_SUCCESS) {
                    if (scrResultCode != RTN_SCR_FAIL) {
                        scr.setReturnCodeMsg(scrResultCode);
                    }
                    return RTN_FAIL;
                }

                return RTN_SUCCESS;
            } else {
                return RTN_NOT_CONNECT;
            }
        }

        /*
         * TID 가 같지만 POS 가 부팅되거나 일 최초 거래 등에 의해 KEY 갱신을 요청한다.
         */
        // 키 갱신 다운로드
        int rcDownload = keyDownloadHost(KeyRenewal_Renewal,
                sScrTID, sDeviceID, sDIKnSerialNo, sPMKSerialNo, sRandom);
        if (rcDownload == RTN_SUCCESS) {
            /*
             * outIC_EncKeyData --> SCR
             */
            // VAN ID(2)
            ArrayCopyAsSize(baVanID, bBOutIC_EncKeyData, 2);
            // TID(10)
            ArrayCopyAsSize(baTerminalID, bBOutIC_EncKeyData, 10);
            // DIKn 일련번호(16)
            ArrayCopyAsSize(baDIKnSerialNo, bBOutIC_EncKeyData, 16);
            // PMK 일련번호(6)
            ArrayCopyAsSize(baPMKSerialNo, bBOutIC_EncKeyData, 6);
            // 암호값(256)
            ArrayCopyAsSize(baEncryptedValue, bBOutIC_EncKeyData, 256);
            // PMK 유효기간(6)
            ArrayCopyAsSize(baPMKValidity, bBOutIC_EncKeyData, 6);
            // TPL(3)
            ArrayCopyAsSize(baTPL, bBOutIC_EncKeyData, 3);

            // SafeCard 암호화 Key 정보 다운로드 요청(갱신)
            scrResultCode = scr.downloadEncryptKey(KeyRenewal_Renewal, baVanID, baTerminalID,
                    baDIKnSerialNo, baPMKSerialNo, baEncryptedValue, baPMKValidity, baTPL);
            if (scrResultCode != RTN_SCR_SUCCESS) {
                if (scrResultCode != RTN_SCR_FAIL) {
                    scr.setReturnCodeMsg(scrResultCode);
                }
                return RTN_FAIL;
            }

            return RTN_SUCCESS;
        }
        else {
            return RTN_NOT_CONNECT;
        }
    }

    /**
     * IC 보안 암호키 다운로드 요청(Host)
     * @param _KeyGuBoon : 전문구분
     * @param _TID : 단말기번호
     * @param _DeviceID : Device ID
     * @param _DIKnSerialNo : DIKn 일련번호
     * @param _PMKSerialNo : PMK 일련번호
     * @param _Random : Random Number
     * @return : return
     */
    private int keyDownloadHost(String _KeyGuBoon,
                                  String _TID,
                                  String _DeviceID,
                                  String _DIKnSerialNo,
                                  String _PMKSerialNo,
                                  String _Random) {
        /*
         * 상세 전문구분
         */
        String sKeyDownGuBun;
        if (_KeyGuBoon.equals(KeyRenewal_First)) {
            sKeyDownGuBun = KEY_GUBUN_0200;     // PMK 분배 - 최초
        } else {
            sKeyDownGuBun = KEY_GUBUN_0400;     // PMK 갱신 - 유효기간 만료 시
        }

        // 초기화
        Init();

        // KIS 서버 주소
        inServerIP = tcpAddress;
        // KIS 서버 포트
        inServerPort = Integer.parseInt(tcpPort);
        // 그룹코드
        inGroupCode = GROUPCODE_NORMAL;
        // 스펙구분
        inSpecType = SPEC_TYPE_NACF;
        // 전문구분코드
        inTranCode = KIS_DEAL_CODE_KEY_DOWN_REQ;
        // 단말기번호
        inCatId = _TID;
        // 전자서명 여부
        inYNSign = SIGN_NO;
        // 개발업체 구분
        inDeveloperName = DEVELOPER_NAME;
        // 상세전문구분
        inIC_KeyDownGubun = sKeyDownGuBun;
        // 기관코드
        inVanId = VAN_ID_KIS;
        // 디바이스 아이디
        inIC_DeviceId = _DeviceID;
        // DIKn 일련번호
        inDIKSerialNo = _DIKnSerialNo;
        // PMK 일련번호
        inPMKSerialNo = _PMKSerialNo;
        // 랜덤번호
        inIC_RandomNo = _Random;

        // 암호키 다운로드 요청...
        int result = KIS_Approval();
        if (result == KIS_RTN_SUCCESS) {
            bBOutIC_EncKeyData = ByteBuffer.wrap(outIC_EncKeyData.getBytes());
            return RTN_SUCCESS;
        }
        else {
            return RTN_NOT_CONNECT;
        }
    }
}
