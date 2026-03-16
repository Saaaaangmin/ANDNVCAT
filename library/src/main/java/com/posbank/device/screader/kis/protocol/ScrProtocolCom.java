package com.posbank.device.screader.kis.protocol;


import android.content.Context;
import android.util.Log;

import com.posbank.device.screader.kis.model.ScrRequestFields;
import com.posbank.device.screader.kis.model.ScrResponseFields;
import com.posbank.device.serialDevice.SerialCom;
import com.posbank.device.serialDevice.SerialPortService;

import java.util.Arrays;

import static com.posbank.device.common.AscII.CH_ACK;
import static com.posbank.device.common.AscII.CH_EOT;
import static com.posbank.device.common.AscII.CH_NAK;
import static com.posbank.device.common.ReturnValue.RTN_CANCEL;
import static com.posbank.device.common.ReturnValue.RTN_COMM_OK;
import static com.posbank.device.common.ReturnValue.RTN_CONTINUE;
import static com.posbank.device.common.ReturnValue.RTN_INVALID_DATA;
import static com.posbank.device.common.ReturnValue.RTN_INVALID_PARAM;
import static com.posbank.device.common.ReturnValue.RTN_NOT_CONNECT;
import static com.posbank.device.common.ReturnValue.RTN_READ_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_READ_NAK;
import static com.posbank.device.common.ReturnValue.RTN_SCR_DEVICE_ERROR;
import static com.posbank.device.common.ReturnValue.RTN_SCR_ERR_EXCHANGE_KEY;
import static com.posbank.device.common.ReturnValue.RTN_SCR_ERR_FIRST_IC;
import static com.posbank.device.common.ReturnValue.RTN_SCR_ERR_INTEGRITY;
import static com.posbank.device.common.ReturnValue.RTN_SCR_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_SCR_FALLBACK_OCCUR;
import static com.posbank.device.common.ReturnValue.RTN_SCR_MUTUAL_AUTH_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_SCR_SUCCESS;
import static com.posbank.device.common.ReturnValue.RTN_SEND_FAIL;
import static com.posbank.device.common.ReturnValue.RTN_TIMEOUT;
import static com.posbank.device.common.Utils.ArrayCopyAsSize;
import static com.posbank.device.common.Utils.CheckTickTimeOut;
import static com.posbank.device.common.Utils.GetCurrentDateTime;
import static com.posbank.device.common.Utils.GetStartTimeTick;
import static com.posbank.device.common.Utils.LeadingZerosString;
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
import static com.posbank.device.screader.kis.model.ScrConstant.CMD_Self_Integrity_xA1;
import static com.posbank.device.screader.kis.model.ScrConstant.EncryptCardNumber;
import static com.posbank.device.screader.kis.model.ScrConstant.NotEncryptCardNumber;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_APDU_ERROR_x8C;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_APP_INTEGRITY_FAIL_xFC;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_AUTH_ERROR_xFA;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_AUTH_NOT_PERFORMED_xFB;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_CANCEL_xCD;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_DECLINE_xCE;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_DIFF_SAFECARD_KEY_xE6;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_ERR_DEVICE_IFM_xE2;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_ERR_DEVICE_INIT_xEC;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_ERR_DEVICE_MSR_xE1;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_FAILURE_xFF;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_FALLBACK_xCF;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_FIRST_IC_INSERT_xE8;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_FIRST_NOT_FALLBACK_xE9;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_INVALID_CONDITION_x8D;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_INVALID_DATA_xF8;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_INVALID_KEY_PMK_xE4;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_INVALID_PARAM_x95;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_KEY_INTEGRITY_FAIL_xFD;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_NOT_ACCEPT_xF5;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_NO_CARD_xF2;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_NO_ID_xD2;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_NO_SIGNATURE_xD0;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_NO_SafeMSR_KEY_xD1;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_REVERSAL_xCC;
import static com.posbank.device.screader.kis.model.ScrConstant.RC_SUCCESS_x00;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_x00;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_x8C;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_x8D;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_x95;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xCC;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xCD;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xCE;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xCF;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xD0;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xD1;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xD2;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xE1;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xE2;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xE4;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xE6;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xE8;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xE9;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xEC;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xF2;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xF5;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xF8;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xFA;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xFB;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xFC;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xFD;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xFF;
import static com.posbank.device.screader.kis.model.ScrConstant.RESP_CODE_MSG_xXX;
import static com.posbank.device.screader.kis.model.ScrConstant.TIMEOUT_1SEC;
import static com.posbank.device.screader.kis.model.ScrConstant.TIMEOUT_2SEC;
import static com.posbank.device.screader.kis.model.ScrConstant.TIMEOUT_3SEC;


public class ScrProtocolCom {
    private static final String TAG = "ScrProtocolCom";

    /*
     * Serial 통신 설정
     */
    private Context context;
    private String serialPortNumber;
    private String serialPortSpeed;

    /*
     * Serial 통신 객체
     */
    private SerialPortService serialService;
    private SerialCom serialCom = null;

    /*
     * 요청 전문 Maker 객체
     */
    private RequestMsgMaker makeMsg = null;
    // 요청전문 필드
    public ScrRequestFields txF = null;

    /*
     * 응답 전문 수신 객체
     */
    public ResponseMsgContain respMsg = null;
    // 응답전문 필드
    public ScrResponseFields rxF = null;

    /*
     * Serial TX Buffer
     */
    private byte[] txMsgData;
    private int txMsgDataLen;

    /*
     * Serial RX Buffer
     */
    public byte[] rxMsgData;
    public int rxMsgDataLen;

    /*
     * 프로토콜 처리 실패 시 메시지
     */
    private byte responseCode = 0;      // 응답코드
    private String responseMsg = "";    // 응답코드 메시지

    /**
     * Constructor
     * @param _Context : Context
     * @param _SerialPortNumber : Serial Port Number
     * @param _SerialPortSpeed  : Serial Port Baud-rate Speed
     */
    public ScrProtocolCom(Context _Context, String _SerialPortNumber, String _SerialPortSpeed) {
        this.context = _Context;
        this.serialPortNumber = _SerialPortNumber;
        this.serialPortSpeed  = _SerialPortSpeed;

        this.txMsgData = new byte[1024];
        this.txMsgDataLen = 0;
        this.rxMsgData = new byte[2048];
        this.rxMsgDataLen = 0;

        this.makeMsg = new RequestMsgMaker();
        this.txF = makeMsg.txFields;
        this.respMsg = new ResponseMsgContain();
        this.rxF = respMsg.rxFields;

        this.serialService = new SerialPortService();
        this.serialService.loadSerialPortDeviceMaps(context);
        this.serialCom = new SerialCom(serialService.getSerialPortDevice(context, serialPortNumber, serialPortSpeed));
        this.serialCom.open();
    }

    //==============================================================================================//
    // 송신버퍼 초기화
    //==============================================================================================//
    public void clearTxBuffer() {
        for(int i=0; i<txMsgData.length; i++) {
            txMsgData[i] = (byte)0x00;
        }
        this.txMsgDataLen = 0;
    }

    //==============================================================================================//
    // 수신버퍼 초기화.
    //==============================================================================================//
    public void clearRxBuffer() {
        for(int i=0; i<rxMsgData.length; i++) {
            rxMsgData[i] = (byte)0x00;
        }
        this.rxMsgDataLen = 0;
    }

    //==============================================================================================//
    // 통신버퍼 안전한 삭제.
    //==============================================================================================//
    public void clearBuffer() {
        secureErase(txMsgData);
        txMsgDataLen = 0;
        secureErase(rxMsgData);
        rxMsgDataLen = 0;
    }

    //==============================================================================================//
    // 메모리 안전한 삭제
    //==============================================================================================//
    private void secureErase(byte[] srcBuff) {
        if (null == srcBuff) return;

        int iCapacity = srcBuff.length;
        byte clearVal;
        for(int i = 0; i < 3; i++) {
            if (i == 1) clearVal = (byte)(0xFF);
            else clearVal = (byte)(0x00);

            for(int j = 0; j < iCapacity; j++){
                srcBuff[j] = clearVal;
            }
        }
    }

    //==============================================================================================
    //
    //==============================================================================================
    public void DEBUG_FIELD_HEX(String title, byte[] src, int len) {
//        if (BuildConfig.DEBUG) {
            String sHex = "";
            for (int i=0; i<len; i++)
                sHex += String.format("%02X ", src[i]);
            String sTitle = String.format("[" + title + "]" + "(%d) ", len);
            Log.d(TAG, sTitle + sHex);
//        }
    }

    //==============================================================================================
    // Serial Port Check Opened
    //==============================================================================================
    public int checkSerialPortOpened() {
        if (null != serialCom) {
            if (!serialCom.isOpened()) {
                if (serialCom.open()) return RTN_COMM_OK;
                return RTN_NOT_CONNECT;
            }
            return RTN_COMM_OK;
        } else {
            serialCom = new SerialCom(serialService.getSerialPortDevice(context, serialPortNumber, serialPortSpeed));
            if (serialCom.open()) return RTN_COMM_OK;
            return RTN_NOT_CONNECT;
        }
    }

    //==============================================================================================
    // Serial Port Destruction
    //==============================================================================================
    public void serialPortDestruction() {
        if (null != serialCom) {
            // Serial Port Close
            serialCom.close();
            serialCom = null;
        }
    }

    /**
     * Sanitize SCR Protocol Buffer
     */
    public void sanitizeScrBuffer() {
        txF.sanitizeScrRequestFields();
        rxF.sanitizeScrResponseFields();
    }

    /**
     * ACK 제어문자 송신
     */
    private void sendAck() {
        clearTxBuffer();

        txMsgData[0] = CH_ACK;
        txMsgDataLen++;

        DEBUG_FIELD_HEX("SendACK", txMsgData, txMsgDataLen);
        serialCom.send(txMsgData, txMsgDataLen);
    }

//    /**
//     * NAK 제어문자 송신
//     */
//    private void sendNak() {
//        clearTxBuffer();
//
//        txMsgData[0] = CH_NAK;
//        txMsgDataLen++;
//
//        DEBUG_FIELD_HEX("SendNAK", txMsgData, txMsgDataLen);
//        serialCom.send(txMsgData, txMsgDataLen);
//    }

    /**
     * EOT 제어문자 송신
     */
    public void sendEot() {
        clearTxBuffer();

        txMsgData[0] = CH_EOT;
        txMsgDataLen++;

        DEBUG_FIELD_HEX("SendEOT", txMsgData, txMsgDataLen);
        serialCom.send(txMsgData, txMsgDataLen);
    }

    //==============================================================================================
    // 전문 송신
    //==============================================================================================
    public int sendMsg(byte[] msg, int msgLen) {
        return serialCom.send(msg, msgLen);
    }

    //==============================================================================================
    // 전문 수신
    //==============================================================================================
    // @param readTimeOverTick : 시리얼 대기시간
    //==============================================================================================
    public int readMsg(long readTimeOverTick) {
        // Frame Check 후 남은 Data 검사필요.
        if (respMsg.remainMsg) {
            rxMsgDataLen = respMsg.remainMsgLen;
            respMsg.initRemainValue();
        } else {
            long startTimeTick = GetStartTimeTick();
            while (CheckTickTimeOut(startTimeTick, readTimeOverTick)) {
                //
                int rxCount = serialCom.receive(rxMsgData, rxMsgDataLen);
                if (rxCount > 0) {
                    rxMsgDataLen += rxCount;
                    // Check Available Data
                    if (serialCom.checkAvailable() == 0) {
                        break;
                    }
                }
            }
        }

        if (rxMsgDataLen > 0)
            DEBUG_FIELD_HEX("수신 DATA" , rxMsgData, rxMsgDataLen);

        if (rxMsgDataLen > 0) {
            // Check Frame
            int frameCheckResult = respMsg.frameCheck(rxMsgData, rxMsgDataLen);
            if (frameCheckResult == RTN_COMM_OK) {
                if (respMsg.remainMsg) {
                    System.arraycopy(rxMsgData, respMsg.remainIndex, rxMsgData, 0, respMsg.remainMsgLen);
                } else {
                    // 수신버퍼 Clear
                    for (int i=0; i<rxMsgDataLen; i++) {
                        rxMsgData[i] = 0;
                    }
                    rxMsgDataLen = 0;
                }
                return RTN_COMM_OK;
            }
            return frameCheckResult;
        } else {
            // Receive Continue...
            return RTN_CONTINUE;
        }
    }

    /**
     * 사용자 강제 종료 이벤트
     */
    private boolean isUserForceStop = false;

    /**
     * 사용자 강제 종료 이벤트 발생(동기화메소드)
     */
    public synchronized void userForceStop() {
        isUserForceStop = true;
    }

    /**
     * 사용자 강제 종료 이벤트 체크(동기화메소드)
     * @return :
     */
    private synchronized boolean getUserStopEvent() {
        return isUserForceStop;
    }

    /**
     * Get System Information 요청
     * 0x31 --> : Get System Information 요청
     *         <-- ACK/NAK 응답
     *         <-- 0x31 : Get System Information 응답
     * @return : 상태
     */
    public int getSystemInformation() {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x31_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("Get System Information 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK / NAK / 응답전문 대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            /*
             * 응답 분석
             */
            if (respMsg.rxCommandID == CH_NAK) {
                // NAK 수신
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답전문 다시 대기 ...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    // 응답 분석
                    if (respMsg.rxCommandID == CMD_Get_System_Information_x31) {
                        // Data Value Parse
                        // 단말기 일련번호, AN(10)
                        int copiedSize = ArrayCopyAsSize(rxF.mTerminalSerialNo, respMsg.rxDataValue, 10);
                        DEBUG_FIELD_HEX("단말기 일련번호", rxF.mTerminalSerialNo, copiedSize);
                        // DIKn 일련번호, AN(16)
                        copiedSize = ArrayCopyAsSize(rxF.mDIKnSerialNo, respMsg.rxDataValue, 16);
                        DEBUG_FIELD_HEX("DIKn 일련번호", rxF.mDIKnSerialNo, copiedSize);
                        // 모델명, AN(12)
                        copiedSize = ArrayCopyAsSize(rxF.mModelName, respMsg.rxDataValue, 12);
                        DEBUG_FIELD_HEX("모델명", rxF.mModelName, copiedSize);
                        // S/W 버전, AN(4)
                        copiedSize = ArrayCopyAsSize(rxF.mSWVersion, respMsg.rxDataValue, 4);
                        DEBUG_FIELD_HEX("S/W 버전", rxF.mSWVersion, copiedSize);
                        // DL 버전, AN(2)
                        copiedSize = ArrayCopyAsSize(rxF.mDownloadVersion, respMsg.rxDataValue, 2);
                        DEBUG_FIELD_HEX("DL 버전", rxF.mDownloadVersion, copiedSize);
                        // 리더기 일련번호(Option), AN(20)
                        copiedSize = ArrayCopyAsSize(rxF.mReaderSerialNo, respMsg.rxDataValue, 20);
                        DEBUG_FIELD_HEX("리더기 일련번호(Option)", rxF.mReaderSerialNo, copiedSize);
                        return RTN_SCR_SUCCESS;
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }

                return readState;
            }
            else if (respMsg.rxCommandID == CMD_Get_System_Information_x31) {
                // Data Value Parse
                // 단말기 일련번호, AN(10)
                int copiedSize = ArrayCopyAsSize(rxF.mTerminalSerialNo, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 일련번호", rxF.mTerminalSerialNo, copiedSize);
                // DIKn 일련번호, AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mDIKnSerialNo, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("DIKn 일련번호", rxF.mDIKnSerialNo, copiedSize);
                // 모델명, AN(12)
                copiedSize = ArrayCopyAsSize(rxF.mModelName, respMsg.rxDataValue, 12);
                DEBUG_FIELD_HEX("모델명", rxF.mModelName, copiedSize);
                // S/W 버전, AN(4)
                copiedSize = ArrayCopyAsSize(rxF.mSWVersion, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("S/W 버전", rxF.mSWVersion, copiedSize);
                // DL 버전, AN(2)
                copiedSize = ArrayCopyAsSize(rxF.mDownloadVersion, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("DL 버전", rxF.mDownloadVersion, copiedSize);
                // 리더기 일련번호(Option), AN(20)
                copiedSize = ArrayCopyAsSize(rxF.mReaderSerialNo, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("리더기 일련번호(Option)", rxF.mReaderSerialNo, copiedSize);
                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * Reset 요청
     * 0x32 -->
     *         <-- ACK/NAK 응답
     *         : ** 응답전문 없음
     * @param rebootWaitTime : Reboot Wait Time("0" ~ "9" /sec)
     * @return : 상태
     */
    public int resetReader(String rebootWaitTime) {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // Reboot Wait Time(0 ~ 9 /sec)
        int iRebootWaitTime;
        if (null != rebootWaitTime && rebootWaitTime.length() == 1) {
            txF.mRebootWaitTime = rebootWaitTime;
            iRebootWaitTime = Integer.parseInt(rebootWaitTime);
        } else {
            return RTN_INVALID_PARAM;
        }

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x32_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("Reset 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        long timeOverTick = iRebootWaitTime * 1000 + TIMEOUT_1SEC;
        while(CheckTickTimeOut(startTimeTick, timeOverTick)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_ACK) {
                return RTN_SCR_SUCCESS;
            }
            else if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * DIK Download 요청
     * 단말기 최초 상태(Key 저장이 안된 상태에서만 가능)
     * 최초 1회 DIK 주입만 가능하고 정상적으로 주입된 이후에는 동작하지 않는다.
     * 0x3D -->
     *         <-- ACK/NAK 응답
     *         <-- 0x3D
     * @param _DIKSerialNo : DIK 일련번호
     * @param _DIK : DIK Key Data
     * @return : 상태
     */
    public int insertDIK(String _DIKSerialNo, String _DIK) {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // DIK 일련번호, AN(16)
        if (null != _DIKSerialNo && !_DIKSerialNo.isEmpty() && _DIKSerialNo.length() == 16) {
            txF.mDIK_SerialNo = _DIKSerialNo;
        } else {
            return RTN_INVALID_PARAM;
        }

        // DIK, AN(32)
        if (null != _DIK && !_DIK.isEmpty() && _DIK.length() == 32) {
            txF.mDIK_KeyData = _DIK;
        } else {
            return RTN_INVALID_PARAM;
        }

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x3D_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("DIK Download 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답 전문 다시 대기 ...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    /*
                     * 응답 전문 분석
                     */
                    if (respMsg.rxCommandID == CMD_Insert_DIK_x3D) {
                        // Result Code, AN(1)
                        int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                        if (copiedSize == 1) {
                            // Result Code Check
                            setResponseCodeMsg();
                            if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                                return RTN_SCR_FAIL;
                            }
                        } else {
                            return RTN_INVALID_DATA;
                        }

                        /*
                         * 응답 코드 SUCCESS(0x00)
                         */
                        // Data Value Parse
                        // 단말기 일련번호, AN(10)
                        copiedSize = ArrayCopyAsSize(rxF.mTerminalSerialNo, respMsg.rxDataValue, 10);
                        DEBUG_FIELD_HEX("단말기 일련번호", rxF.mTerminalSerialNo, copiedSize);
                        return RTN_SCR_SUCCESS;
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }
                return readState;
            }
            // 응답 전문 수신
            else if (respMsg.rxCommandID == CMD_Insert_DIK_x3D) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Data Value Parse
                // 단말기 일련번호, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalSerialNo, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 일련번호", rxF.mTerminalSerialNo, copiedSize);
                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * POS 와 Reader 의 상호 인증
     * MSK(Message Session Key) 생성 요청
     * 0xA0("F1") -->
     *              <-- ACK/NAK 응답
     *              <-- 0xA0("F1") : MSK(Message Session Key) 생성 응답
     * @param _1stPosRandomNumber : 1st POS Random Number
     * @return : 상태
     */
    public int requestCreateMSK(byte[] _1stPosRandomNumber) {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 1st Pos Random Number
        txF.mRND_P1 = _1stPosRandomNumber;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.xA0F1_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("MSK(Message Session Key)생성 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK/응답 대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            /*
             * 응답 분석
             */
            // NAK 수신
            if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답 전문 다시 대기 ...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    /*
                     * 응답 전문 분석
                     */
                    if (respMsg.rxCommandID == CMD_Mutual_Authentication_xA0) {
                        // Result Code, AN(1)
                        int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                        if (copiedSize == 1) {
                            // Result Code Check
                            setResponseCodeMsg();
                            if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                                return RTN_SCR_FAIL;
                            }
                        } else {
                            return RTN_INVALID_DATA;
                        }

                        /*
                         * 응답 코드 SUCCESS(0x00)
                         */
                        // Data Value Parse
                        // 거래 구분, AN(2)
                        copiedSize = ArrayCopyAsSize(rxF.mGuBoonCode, respMsg.rxDataValue, 2);
                        DEBUG_FIELD_HEX("거래구분", rxF.mGuBoonCode, copiedSize);

                        // RND_R1, AN(8)
                        copiedSize = ArrayCopyAsSize(rxF.mRND_R1, respMsg.rxDataValue, 8);
                        DEBUG_FIELD_HEX("1st Reader Random Number", rxF.mRND_R1, copiedSize);
                        return RTN_SCR_SUCCESS;
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }
                return readState;
            }
            // 응답 전문 수신
            else if (respMsg.rxCommandID == CMD_Mutual_Authentication_xA0) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Data Value Parse
                // 거래 구분, AN(2)
                copiedSize = ArrayCopyAsSize(rxF.mGuBoonCode, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("거래구분", rxF.mGuBoonCode, copiedSize);

                // RND_R1, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mRND_R1, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("1st Reader Random Number", rxF.mRND_R1, copiedSize);
                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        } else {
            return readState;
        }
    }

    /**
     * 상호 인증 초기화 요청
     * 0xA0("F2") -->
     *               <-- ACK/NAK 응답
     *               <-- 0xA0("F2") : 상호 인증 초기화 응답
     * @param _2ndPosRandomNumber : 2nd POS Random Number
     * @return : 상태
     */
    public int initializeMutualAuth(byte[] _2ndPosRandomNumber) {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 2nd Pos Random Number
        txF.mRND_P2 = _2ndPosRandomNumber;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.xA0F2_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("상호 인증 초기화 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK/응답 대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_1SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            /*
             * 응답 분석
             */
            if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답 전문 대기...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    /*
                     * 응답 전문 분석
                     */
                    if (respMsg.rxCommandID == CMD_Mutual_Authentication_xA0) {
                        // Result Code, AN(1)
                        int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                        if (copiedSize == 1) {
                            // Result Code Check
                            setResponseCodeMsg();
                            if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                                return RTN_SCR_FAIL;
                            }
                        } else {
                            return RTN_INVALID_DATA;
                        }

                        /*
                         * 응답 코드 SUCCESS(0x00)
                         */
                        // Data Value Parse
                        // 거래 구분, AN(2)
                        copiedSize = ArrayCopyAsSize(rxF.mGuBoonCode, respMsg.rxDataValue, 2);
                        DEBUG_FIELD_HEX("거래구분", rxF.mGuBoonCode, copiedSize);
                        // RND_R2, AN(8)
                        copiedSize = ArrayCopyAsSize(rxF.mRND_R2, respMsg.rxDataValue, 8);
                        DEBUG_FIELD_HEX("2nd Reader Random Number", rxF.mRND_R2, copiedSize);
                        // Encrypted RND, AN(32)
                        copiedSize = ArrayCopyAsSize(rxF.mEncrypted_RND, respMsg.rxDataValue, 32);
                        DEBUG_FIELD_HEX("Encrypted RND", rxF.mEncrypted_RND, copiedSize);

                        return RTN_SCR_SUCCESS;
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }

                return readState;
            }
            else if (respMsg.rxCommandID == CMD_Mutual_Authentication_xA0) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Data Value Parse
                // 거래 구분, AN(2)
                copiedSize = ArrayCopyAsSize(rxF.mGuBoonCode, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("거래구분", rxF.mGuBoonCode, copiedSize);
                // RND_R2, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mRND_R2, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("2nd Reader Random Number", rxF.mRND_R2, copiedSize);
                // Encrypted RND, AN(32)
                copiedSize = ArrayCopyAsSize(rxF.mEncrypted_RND, respMsg.rxDataValue, 32);
                DEBUG_FIELD_HEX("Encrypted RND", rxF.mEncrypted_RND, copiedSize);

                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * 상호 인증 완료 요청
     * 0xA0("F3") -->
     *               <-- ACK/NAK 응답
     *               <-- 0xA0("F3") : 상호 인증 완료 응답
     * @param _EncryptedRnd : 암호화된 Random Number
     * @return : 상태
     */
    public int completeMutualAuth(byte[] _EncryptedRnd) {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 암호화된 Random Number
        txF.mEncrypted_RND = _EncryptedRnd;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.xA0F3_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("상호 인증 완료 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_1SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답 전문 대기...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    /*
                     * 응답 전문 분석
                     */
                    if (respMsg.rxCommandID == CMD_Mutual_Authentication_xA0) {
                        // Result Code, AN(1)
                        int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                        if (copiedSize == 1) {
                            // Result Code Check
                            setResponseCodeMsg();
                            if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                                return RTN_SCR_FAIL;
                            }
                        } else {
                            return RTN_INVALID_DATA;
                        }

                        /*
                         * 응답코드 SUCCESS(0x00)
                         */
                        // Data Value Parse
                        // 거래 구분, AN(2)
                        copiedSize = ArrayCopyAsSize(rxF.mGuBoonCode, respMsg.rxDataValue, 2);
                        DEBUG_FIELD_HEX("거래구분", rxF.mGuBoonCode, copiedSize);
                        // 거래 결과, AN(1)
                        copiedSize = ArrayCopyAsSize(rxF.mMutualAuth_Result, respMsg.rxDataValue, 1);
                        DEBUG_FIELD_HEX("거래결과", rxF.mMutualAuth_Result, copiedSize);

                        return RTN_SCR_SUCCESS;
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }

                return readState;
            }
            else if (respMsg.rxCommandID == CMD_Mutual_Authentication_xA0) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                // Result Code Check
                if (copiedSize == 1) {
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답코드 SUCCESS(0x00)
                 */
                // Data Value Parse
                // 거래 구분, AN(2)
                copiedSize = ArrayCopyAsSize(rxF.mGuBoonCode, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("거래구분", rxF.mGuBoonCode, copiedSize);
                // 거래 결과, AN(1)
                copiedSize = ArrayCopyAsSize(rxF.mMutualAuth_Result, respMsg.rxDataValue, 1);
                DEBUG_FIELD_HEX("거래결과", rxF.mMutualAuth_Result, copiedSize);

                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * Reader 무결성 검증 요청
     * 0xA1 -->
     *         <-- ACK/NAK
     *         <-- 0xA1 : Reader 무결성 검증 응답
     * @return : 상태
     */
    public int verifyIntegrity() {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.xA1_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("Reader 무결성 검증 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK/응답 대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_2SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            /*
             * 응답 분석
             */
            if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답 전문 대기...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    /*
                     * 응답 전문 분석
                     */
                    if (respMsg.rxCommandID == CMD_Self_Integrity_xA1) {
                        // Result Code, AN(1)
                        int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                        setResponseCodeMsg();
                        // Result Code Check
                        if (copiedSize == 1) {
                            if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                                return RTN_SCR_ERR_INTEGRITY;
                            }
                            /*
                             * 응답코드 SUCCESS(0x00)
                             */
                            return RTN_SCR_SUCCESS;
                        } else {
                            return RTN_INVALID_DATA;
                        }
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }

                return readState;
            }
            // 응답전문 분석
            else if (respMsg.rxCommandID == CMD_Self_Integrity_xA1) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                setResponseCodeMsg();
                // Result Code Check
                if (copiedSize == 1) {
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_ERR_INTEGRITY;
                    }
                    /*
                     * 응답콛 SUCCESS(0x00)
                     */
                    return RTN_SCR_SUCCESS;
                } else {
                    return RTN_INVALID_DATA;
                }
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * SafeCard 암호화 Key 정보 동기화 요청
     * 0x6A -->
     *         <-- ACK/NAK
     *         <-- 0x6A : Key 정보 동기화 응답
     * @param _KeyRenewalGuBoon : 키 갱신종류, 최초(분배) : 1, 갱신 : 2
     * @param _TerminalID : 가맹점 TID
     * @return : 상태
     */
    public int synchronizeEncryptKey(String _KeyRenewalGuBoon, String _TerminalID) {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 키 갱신종류, 최초(분배):"1", 갱신:"2"
        if ((null == _KeyRenewalGuBoon) || (_KeyRenewalGuBoon.isEmpty())) {
            return RTN_INVALID_PARAM;
        }
        txF.mKey_Renewal_Kind = _KeyRenewalGuBoon;

        // CAT-ID, 가맹점 TID
        if ((null == _TerminalID) || (_TerminalID.isEmpty())) {
            return RTN_INVALID_PARAM;
        }
        txF.mTerminalID = LeadingZerosString(_TerminalID, 10).getBytes();

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x6A_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("SafeCard 암호화 Key 정보 동기화 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            /*
             * 응답 분석
             */
            if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답 전문 대기...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    /*
                     * 응답 전문 분석
                     */
                    if (respMsg.rxCommandID == CMD_Encrypt_Key_Sync_x6A) {
                        // Result Code, AN(1)
                        int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                        if (copiedSize == 1) {
                            // Result Code Check
                            setResponseCodeMsg();
                            if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                                return RTN_SCR_FAIL;
                            }
                        } else {
                            return RTN_INVALID_DATA;
                        }

                        /*
                         * 응답코드 SUCCESS(0x00)
                         */
                        // Data Value Parse
                        // TID, AN(10)
                        copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                        DEBUG_FIELD_HEX("TID", rxF.mTerminalID, copiedSize);
                        // Device ID, AN(10)
                        copiedSize = ArrayCopyAsSize(rxF.mDeviceID, respMsg.rxDataValue, 10);
                        DEBUG_FIELD_HEX("Device ID", rxF.mDeviceID, copiedSize);
                        // DIKn 일련번호, AN(16)
                        copiedSize = ArrayCopyAsSize(rxF.mDIKnSerialNo, respMsg.rxDataValue, 16);
                        DEBUG_FIELD_HEX("DIKn 일련번호", rxF.mDIKnSerialNo, copiedSize);
                        // PMK 일련번호, AN(6)
                        copiedSize = ArrayCopyAsSize(rxF.mPMKSerialNo, respMsg.rxDataValue, 6);
                        DEBUG_FIELD_HEX("PMK 일련번호", rxF.mPMKSerialNo, copiedSize);
                        // Random, AN(32)
                        copiedSize = ArrayCopyAsSize(rxF.mRandom, respMsg.rxDataValue, 32);
                        DEBUG_FIELD_HEX("Random", rxF.mRandom, copiedSize);

                        // TID 일치?
                        if(compareTerminalID(txF.mTerminalID, rxF.mTerminalID)) {
                            return RTN_SCR_SUCCESS;
                        } else {
                            // 단말기 ID가 일치하지 않음
                            // DIK KEY 주입필요...
                            rxF.mResult_Code[0] = RC_NO_ID_xD2;
                            setResponseCodeMsg();
                            return RTN_SCR_ERR_EXCHANGE_KEY;
                        }
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }

                return readState;
            }
            // 응답전문 분석
            else if (respMsg.rxCommandID == CMD_Encrypt_Key_Sync_x6A) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답코드 SUCCESS(0x00)
                 */
                // Data Value Parse
                // TID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("TID", rxF.mTerminalID, copiedSize);
                // Device ID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mDeviceID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("Device ID", rxF.mDeviceID, copiedSize);
                // DIKn 일련번호, AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mDIKnSerialNo, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("DIKn 일련번호", rxF.mDIKnSerialNo, copiedSize);
                // PMK 일련번호, AN(6)
                copiedSize = ArrayCopyAsSize(rxF.mPMKSerialNo, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("PMK 일련번호", rxF.mPMKSerialNo, copiedSize);
                // Random, AN(32)
                copiedSize = ArrayCopyAsSize(rxF.mRandom, respMsg.rxDataValue, 32);
                DEBUG_FIELD_HEX("Random", rxF.mRandom, copiedSize);

                // TID 일치?
                if(compareTerminalID(txF.mTerminalID, rxF.mTerminalID)) {
                    return RTN_SCR_SUCCESS;
                } else {
                    // 단말기 ID가 일치하지 않음
                    // DIK KEY 주입필요...
                    rxF.mResult_Code[0] = RC_NO_ID_xD2;
                    setResponseCodeMsg();
                    return RTN_SCR_ERR_EXCHANGE_KEY;
                }
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * SafeCard 암호화 Key 정보 다운로드 요청
     * 0x6B -->
     *         <-- ACK/NAK
     *         <-- 0x6B : 암호화 Key 정보 다운로드 결과
     * @param _KeyRenewalGuBoon : 키 갱신종류
     * @param _VanID : 기관 코드(VAN ID)
     * @param _TerminalID : 가맹점 TID or CAT-ID
     * @param _DIKnSn : DIKn 일련번호
     * @param _PMKSn : PMK 일련번호, 갱신 시 Old PMF Index
     * @param _EncData : 암호값, 암호화 정보
     * @param _PMKValidity : PMK 유효기간
     * @param _TPL : 암호화 범위 설정
     * @return : 상태
     */
    public int downloadEncryptKey(String _KeyRenewalGuBoon, byte[] _VanID, byte[] _TerminalID,
                                  byte[] _DIKnSn, byte[] _PMKSn, byte[] _EncData,
                                  byte[] _PMKValidity, byte[] _TPL) {
        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 키 갱신종류, 최초(분배):"1", 갱신:"2"
        txF.mKey_Renewal_Kind = _KeyRenewalGuBoon;
        // VAN ID, 기관코드
        txF.mVanID = _VanID;
        // TID, 가맹점 TID or CAT-ID
        txF.mTerminalID = _TerminalID;
        // DIKn 일련번호
        txF.mDIKnSerialNo = _DIKnSn;
        // PMK 일련번호
        txF.mPMKSerialNo = _PMKSn;
        // 암호값, 암호화 정보
        txF.mEncryptedValue = _EncData;
        // PMK 유효기간
        txF.mPMKValidity = _PMKValidity;
        // TPL, 암호화 범위 설정
        txF.mTPL = _TPL;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x6B_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("SafeCard 암호화 Key 정보 다운로드 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            /*
             * 응답 분석
             */
            if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CH_ACK) {
                // ACK 수신
                // 응답 전문 대기...
                clearRxBuffer();
                startTimeTick = GetStartTimeTick();
                while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
                    readState = readMsg(100);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                clearBuffer();

                if (readState == RTN_COMM_OK) {
                    /*
                     * 응답 전문 분석
                     */
                    if (respMsg.rxCommandID == CMD_Encrypt_Key_Down_x6B) {
                        // Result Code, AN(1)
                        int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                        // Result Code Check
                        setResponseCodeMsg();
                        if (copiedSize == 1) {
                            if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                                return RTN_SCR_FAIL;
                            }
                            /*
                             * 응답코드 SUCCESS(0x00)
                             */
                            return RTN_SCR_SUCCESS;
                        } else {
                            return RTN_INVALID_DATA;
                        }
                    }
                    else {
                        return RTN_INVALID_DATA;
                    }
                }
                // 응답대기 시간 초과
                else if (readState == RTN_CONTINUE) {
                    return RTN_TIMEOUT;
                }

                return readState;
            }
            // 응답전문 분석
            else if (respMsg.rxCommandID == CMD_Encrypt_Key_Down_x6B) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                // Result Code Check
                setResponseCodeMsg();
                if (copiedSize == 1) {
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                    /*
                     * 응답코드 SUCCESS(0x00)
                     */
                    return RTN_SCR_SUCCESS;
                } else {
                    return RTN_INVALID_DATA;
                }
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * SafeCard 암호화 MS/IC 신용거래 요청
     * 0x6C    -->
     *            <-- ACK/NAK
     *            <-- 0x6C : SafeCard 암호화 MS/IC 신용거래 응답
     * ACK/NAK -->
     * @param _TerminalID : TID
     * @param _TransType : 거래 종류, 구매:"0", 취소:"1"
     * @param _TransAmount : 거래 금액
     * @param _CardWaitTime : 카드 대기 시간(sec)
     * @return : 상태
     */
    public int startCreditTransaction(String _TerminalID, String _TransType,
                                      int _TransAmount, int _CardWaitTime) {
        boolean isAck = false;

        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // Card 대기시간
        txF.mCardWait_Time = _CardWaitTime;
        // 거래 일시, YYYYMMDDHHMMSS
        txF.mTransDate = GetCurrentDateTime();
        // 거래 금액
        txF.mTransAmount = _TransAmount;
        // TID
        if ((null == _TerminalID) || (_TerminalID.isEmpty())) {
            return RTN_INVALID_PARAM;
        }
        txF.mTerminalID = LeadingZerosString(_TerminalID, 10).getBytes();
        // 거래종류, 구매:"0", 취소:"1"
        txF.mTransType = _TransType;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x6C_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("SafeCard 암호화 MS/IC 신용거래 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_ACK) {
                isAck = true;
            }
            else if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            // 응답전문 분석
            else if (respMsg.rxCommandID == CMD_Encrypt_MS_IC_Credit_x6C) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    //
                    if (responseCode != RC_SUCCESS_x00) {
                        // fallback 오류 발생
                        if (responseCode == RC_FALLBACK_xCF) {
                            return RTN_SCR_FALLBACK_OCCUR;
                        }
                        // IC 우선거래 발생
                        if (responseCode == RC_FIRST_IC_INSERT_xE8) {
                            return RTN_SCR_ERR_FIRST_IC;
                        }
                        // 상호 인증 실패 발생
                        if (responseCode == RC_AUTH_NOT_PERFORMED_xFB) {
                            return RTN_SCR_MUTUAL_AUTH_FAIL;
                        }
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // 카드구분자, AN(4)
                copiedSize = ArrayCopyAsSize(rxF.mCardProperty, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, copiedSize);
                // CVM, AN(1)
                copiedSize = ArrayCopyAsSize(rxF.mCVM, respMsg.rxDataValue, 1);
                DEBUG_FIELD_HEX("CVM", rxF.mCVM, copiedSize);
                // VAN ID, N(2)
                copiedSize = ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, copiedSize);
                // 암호화 정보, AN(96)
                copiedSize = ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, copiedSize);
                // 생성된 MAC, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, copiedSize);
                // 단말기 ID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, copiedSize);
                // KSN, AN(20)
                copiedSize = ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, copiedSize);
                // TPL, AN(3)
                copiedSize = ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, copiedSize);
                // 일련번호(리더기 식별번호), AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, copiedSize);
                // 유효기간, AN(6)
                copiedSize = ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, copiedSize);
                // fallback, N(3)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("fallback", rxF.mFallbackCode, copiedSize);
                // 카드 Bin, N(6)
                copiedSize = ArrayCopyAsSize(rxF.mCardBinNo, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNo, copiedSize);
                // Chip 데이터 길이 필드, N(4)
                copiedSize = ArrayCopyAsSize(rxF.mChipDataLength, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("Chip 데이터 길이", rxF.mChipDataLength, copiedSize);
                if (copiedSize == 4 ) {
                    rxF.miChipDataLength = Integer.parseInt(new String(rxF.mChipDataLength));
                    if (rxF.miChipDataLength > 0) {
                        // IC EMV Chip Data, var(ChipData 길이)
                        rxF.mICEmvChipData = new byte[rxF.miChipDataLength];
                        ArrayCopyAsSize(rxF.mICEmvChipData, respMsg.rxDataValue, rxF.miChipDataLength);
                        DEBUG_FIELD_HEX("IC Emv ChipData", rxF.mICEmvChipData, rxF.mICEmvChipData.length);
                    }
                }

                // TID 일치?
                if(compareTerminalID(txF.mTerminalID, rxF.mTerminalID)) {
                    return RTN_SCR_SUCCESS;
                } else {
                    // 단말기 ID가 일치하지 않음
                    // DIK KEY 주입필요...
                    rxF.mResult_Code[0] = RC_NO_ID_xD2;
                    setResponseCodeMsg();
                    return RTN_SCR_ERR_EXCHANGE_KEY;
                }
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        //  ACK Check
        if (!isAck) return RTN_READ_FAIL;
        //==========================================================================================
        startTimeTick = GetStartTimeTick();
        // Card Wait Time(Sec) 만큼 응답대기
        long timeOverTick = txF.mCardWait_Time*1000 + TIMEOUT_2SEC;
        while(CheckTickTimeOut(startTimeTick, timeOverTick)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
            // 사용자 강제 종료 이벤트 체크
            if (getUserStopEvent()) {
                // 초기화
                sendEot();
                return RTN_CANCEL;
            }
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CMD_Encrypt_MS_IC_Credit_x6C) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                // Result Code Check
                if (copiedSize == 1) {
                    setResponseCodeMsg();
                    //
                    if (responseCode != RC_SUCCESS_x00) {
                        // fallback 오류 발생
                        if (responseCode == RC_FALLBACK_xCF) {
                            return RTN_SCR_FALLBACK_OCCUR;
                        }
                        // IC 우선거래 발생
                        if (responseCode == RC_FIRST_IC_INSERT_xE8) {
                            return RTN_SCR_ERR_FIRST_IC;
                        }
                        // 상호 인증 실패 발생
                        if (responseCode == RC_AUTH_NOT_PERFORMED_xFB) {
                            return RTN_SCR_MUTUAL_AUTH_FAIL;
                        }
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // 카드구분자, AN(4)
                copiedSize = ArrayCopyAsSize(rxF.mCardProperty, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, copiedSize);
                // CVM, AN(1)
                copiedSize = ArrayCopyAsSize(rxF.mCVM, respMsg.rxDataValue, 1);
                DEBUG_FIELD_HEX("CVM", rxF.mCVM, copiedSize);
                // VAN ID, N(2)
                copiedSize = ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, copiedSize);
                // 암호화 정보, AN(96)
                copiedSize = ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, copiedSize);
                // 생성된 MAC, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, copiedSize);
                // 단말기 ID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, copiedSize);
                // KSN, AN(20)
                copiedSize = ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, copiedSize);
                // TPL, AN(3)
                copiedSize = ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, copiedSize);
                // 일련번호(리더기 식별번호), AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, copiedSize);
                // 유효기간, AN(6)
                copiedSize = ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, copiedSize);
                // fallback, N(3)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("fallback", rxF.mFallbackCode, copiedSize);
                // 카드 Bin, N(6)
                copiedSize = ArrayCopyAsSize(rxF.mCardBinNo, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNo, copiedSize);
                // Chip 데이터 길이 필드, N(4)
                copiedSize = ArrayCopyAsSize(rxF.mChipDataLength, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("Chip 데이터 길이", rxF.mChipDataLength, copiedSize);
                if (copiedSize == 4 ) {
                    rxF.miChipDataLength = Integer.parseInt(new String(rxF.mChipDataLength));
                    if (rxF.miChipDataLength > 0) {
                        // IC EMV Chip Data, var(ChipData 길이)
                        rxF.mICEmvChipData = new byte[rxF.miChipDataLength];
                        ArrayCopyAsSize(rxF.mICEmvChipData, respMsg.rxDataValue, rxF.miChipDataLength);
                        DEBUG_FIELD_HEX("IC Emv ChipData", rxF.mICEmvChipData, rxF.mICEmvChipData.length);
                    }
                }

                // TID 일치?
                if(compareTerminalID(txF.mTerminalID, rxF.mTerminalID)) {
                    return RTN_SCR_SUCCESS;
                } else {
                    // 단말기 ID가 일치하지 않음
                    // DIK KEY 주입필요...
                    rxF.mResult_Code[0] = RC_NO_ID_xD2;
                    setResponseCodeMsg();
                    return RTN_SCR_ERR_EXCHANGE_KEY;
                }
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * SafeCard 암호화 IC EMV 신용거래 완료 요청
     * 0x6D -->
     *         <-- ACK/NAK
     *         <-- 0x6D : IC EMV 완료 응답
     * @param _EmvLength : 전문길이
     * @param _ResponseCode : 응답코드
     * @param _ARD : Additional response data
     * @param _IAD : IAD
     * @param _IS : Issuer Script
     * @return : 상태
     */
    public int completeEmvTransaction(byte[] _EmvLength, byte[] _ResponseCode, byte[] _ARD,
                                  byte[] _IAD, byte[] _IS) {
        boolean isAck = false;

        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // 전문길이, ASC(4)
        txF.mEmvLength = _EmvLength;
        // 응답코드 + Additional response data + IAD + Issuer Script
        // 응답코드, ASC(2)
        txF.mResponseCode = _ResponseCode;
        // Additional response data, ASC(27)
        txF.mARD = _ARD;
        // IAD, ASC(34)
        txF.mIAD = _IAD;
        // Issuer Script, ASC(259)
        txF.mIS = _IS;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x6D_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("IC EMV 신용거래 완료 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_ACK) {
                isAck = true;
            } else if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            // 응답전문 분석
            else if (respMsg.rxCommandID == CMD_IC_EMV_Complete_x6D) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                // Result Code Check
                if (copiedSize == 1) {
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Data Value Parse
                // TVR, ANS(10)
                copiedSize = ArrayCopyAsSize(rxF.mTVR, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("TVR", rxF.mTVR, copiedSize);
                // AC, ANS(16)
                copiedSize = ArrayCopyAsSize(rxF.mAC, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("AC", rxF.mAC, copiedSize);
                // ISR, ANS(40)
                copiedSize = ArrayCopyAsSize(rxF.mISR, respMsg.rxDataValue, 40);
                DEBUG_FIELD_HEX("ISR", rxF.mISR, copiedSize);

                return RTN_SCR_SUCCESS;
            } else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        //  ACK Check
        if (!isAck) return RTN_READ_FAIL;
        //==========================================================================================
        startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CMD_IC_EMV_Complete_x6D) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                // Result Code Check
                if (copiedSize == 1) {
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Data Value Parse
                // TVR, ANS(10)
                copiedSize = ArrayCopyAsSize(rxF.mTVR, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("TVR", rxF.mTVR, copiedSize);
                // AC, ANS(16)
                copiedSize = ArrayCopyAsSize(rxF.mAC, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("AC", rxF.mAC, copiedSize);
                // ISR, ANS(40)
                copiedSize = ArrayCopyAsSize(rxF.mISR, respMsg.rxDataValue, 40);
                DEBUG_FIELD_HEX("ISR", rxF.mISR, copiedSize);

                return RTN_SCR_SUCCESS;
            } else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * SafeCard 암호화 MS Fallback 거래 요청
     * 0x6E    -->
     *            <-- ACK/NAK
     *            <-- 0x6E : MS Fallback 거래 응답
     * ACK/NAK -->
     * @param _Message : Display Message
     * @param _CardWaitTime : MS 카드 대기시간
     * @return : 상태
     */
    public int fallbackTransaction(String _Message, int _CardWaitTime) {
        boolean isAck = false;

        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // MS Card 대기시간
        txF.mCardWait_Time = _CardWaitTime;
        // Display Message
        txF.mDisplayMessage = _Message;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.x6E_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("SafeCard 암호화 MS Fallback 거래 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        //==========================================================================================
        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_ACK) {
                isAck = true;
            }
            else if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            // 응답전문 분석
            else if (respMsg.rxCommandID == CMD_MS_Fallback_Credit_x6E) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // Fallback 구분(사유)코드, AN(2)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackReasonCode, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("Fallback 구분", rxF.mFallbackReasonCode, copiedSize);
                // VAN ID, N(2)
                copiedSize = ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, copiedSize);
                // 암호화 정보, AN(96)
                copiedSize = ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, copiedSize);
                // 생성된 MAC, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, copiedSize);
                // 단말기 ID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, copiedSize);
                // KSN, AN(20)
                copiedSize = ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, copiedSize);
                // TPL, AN(3)
                copiedSize = ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, copiedSize);
                // 일련번호(리더기 식별번호), AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, copiedSize);
                // 유효기간, AN(6)
                copiedSize = ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, copiedSize);
                // fallback, N(3)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, copiedSize);
                // 카드 Bin, N(6)
                copiedSize = ArrayCopyAsSize(rxF.mCardBinNo, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNo, copiedSize);
                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        //  ACK Check
        if (!isAck) return RTN_READ_FAIL;
        //==========================================================================================
        startTimeTick = GetStartTimeTick();
        // Card Wait Time(Sec) 만큼 응답대기
        long timeOverTick = txF.mCardWait_Time*1000 + TIMEOUT_2SEC;
        while(CheckTickTimeOut(startTimeTick, timeOverTick)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
            // 사용자 강제 종료 이벤트 체크
            if (getUserStopEvent()) {
                // 초기화
                sendEot();
                return RTN_CANCEL;
            }
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CMD_MS_Fallback_Credit_x6E) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // Fallback 구분(사유)코드, AN(2)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackReasonCode, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("Fallback 구분", rxF.mFallbackReasonCode, copiedSize);
                // VAN ID, N(2)
                copiedSize = ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, copiedSize);
                // 암호화 정보, AN(96)
                copiedSize = ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, copiedSize);
                // 생성된 MAC, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, copiedSize);
                // 단말기 ID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, copiedSize);
                // KSN, AN(20)
                copiedSize = ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, copiedSize);
                // TPL, AN(3)
                copiedSize = ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, copiedSize);
                // 일련번호(리더기 식별번호), AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, copiedSize);
                // 유효기간, AN(6)
                copiedSize = ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, copiedSize);
                // fallback, N(3)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, copiedSize);
                // 카드 Bin, N(6)
                copiedSize = ArrayCopyAsSize(rxF.mCardBinNo, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNo, copiedSize);
                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * SafeCard 암호화 MS/IC 신용 & 포인트 거래 요청(*** MP1000 지원안함(0x9F(문서에 없음) 응답 옴) ***)
     * 0xBF    -->
     *            <-- ACK/NAK
     *            <-- 0xBF : MS/IC 신용&포인트 거래 응답
     * ACK/NAK -->
     * @param _TerminalID : TID
     * @param _TransType : 거래 종류
     * @param _TransAmount : 거래 금액
     * @param _CardWaitTime : 카드 대기 시간
     * @return : 상태
     */
    public int startCreditPointTransaction(String _TerminalID,
                                           String _TransType,
                                           int _TransAmount,
                                           int _CardWaitTime) {
        boolean isAck = false;

        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // Card 대기시간
        txF.mCardWait_Time = _CardWaitTime;
        // 거래 일시, YYYYMMDDHHMMSS
        txF.mTransDate = GetCurrentDateTime();
        // 거래 금액
        txF.mTransAmount = _TransAmount;
        // TID
        txF.mTerminalID = LeadingZerosString(_TerminalID, 10).getBytes();
        // 거래종류, 구매:"0", 취소:"1"
        txF.mTransType = _TransType;

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.xBF_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("SafeCard 암호화 MS/IC 신용&포인트 거래 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        //==========================================================================================
        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_ACK) {
                isAck = true;
            }
            else if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            else if (respMsg.rxCommandID == CMD_Encrypt_MS_IC_Credit_Point_xBF) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    //
                    if (responseCode != RC_SUCCESS_x00) {
                        // fallback 오류 발생
                        if (responseCode == RC_FALLBACK_xCF) {
                            return RTN_SCR_FALLBACK_OCCUR;
                        }
                        // IC 우선거래 발생
                        if (responseCode == RC_FIRST_IC_INSERT_xE8) {
                            return RTN_SCR_ERR_FIRST_IC;
                        }
                        // 상호 인증 실패 발생
                        if (responseCode == RC_AUTH_NOT_PERFORMED_xFB) {
                            return RTN_SCR_MUTUAL_AUTH_FAIL;
                        }
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // 카드구분자, AN(4)
                copiedSize = ArrayCopyAsSize(rxF.mCardProperty, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, copiedSize);
                // CVM, AN(1)
                copiedSize = ArrayCopyAsSize(rxF.mCVM, respMsg.rxDataValue, 1);
                DEBUG_FIELD_HEX("CVM", rxF.mCVM, copiedSize);
                // VAN ID, N(2)
                copiedSize = ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, copiedSize);
                // 암호화 정보, AN(96)
                copiedSize = ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, copiedSize);
                // 생성된 MAC, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, copiedSize);
                // 단말기 ID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, copiedSize);
                // KSN, AN(20)
                copiedSize = ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, copiedSize);
                // TPL, AN(3)
                copiedSize = ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, copiedSize);
                // 일련번호(리더기 식별번호), AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, copiedSize);
                // 유효기간, AN(6)
                copiedSize = ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, copiedSize);
                // fallback, N(3)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("fallback", rxF.mFallbackCode, copiedSize);
                // 카드 Bin, N(6)
                copiedSize = ArrayCopyAsSize(rxF.mCardBinNo, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNo, copiedSize);
                // Chip 데이터 길이 필드, N(4)
                copiedSize = ArrayCopyAsSize(rxF.mChipDataLength, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("Chip 데이터 길이", rxF.mChipDataLength, copiedSize);
                if (copiedSize == 4 ) {
                    rxF.miChipDataLength = Integer.parseInt(new String(rxF.mChipDataLength));
                    if (rxF.miChipDataLength > 0) {
                        // IC EMV Chip Data, var(ChipData 길이)
                        rxF.mICEmvChipData = new byte[rxF.miChipDataLength];
                        ArrayCopyAsSize(rxF.mICEmvChipData, respMsg.rxDataValue, rxF.miChipDataLength);
                        DEBUG_FIELD_HEX("IC Emv ChipData", rxF.mICEmvChipData, rxF.mICEmvChipData.length);
                    }
                }

                // TID 일치?
                if(compareTerminalID(txF.mTerminalID, rxF.mTerminalID)) {
                    return RTN_SCR_SUCCESS;
                } else {
                    // 단말기 ID가 일치하지 않음
                    // DIK KEY 주입필요...
                    rxF.mResult_Code[0] = RC_NO_ID_xD2;
                    setResponseCodeMsg();
                    return RTN_SCR_ERR_EXCHANGE_KEY;
                }
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        //  ACK Check
        if (!isAck) return RTN_READ_FAIL;
        //==========================================================================================
        startTimeTick = GetStartTimeTick();
        // Card Wait Time(Sec) 만큼 응답대기
        long timeOverTick = txF.mCardWait_Time*1000 + TIMEOUT_2SEC;
        while(CheckTickTimeOut(startTimeTick, timeOverTick)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
            // 사용자 강제 종료 이벤트 체크
            if (getUserStopEvent()) {
                // 초기화
                sendEot();
                return RTN_CANCEL;
            }
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CMD_Encrypt_MS_IC_Credit_Point_xBF) {
                // Result Code, AN(1)
                int copiedSize = ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                if (copiedSize == 1) {
                    // Result Code Check
                    setResponseCodeMsg();
                    //
                    if (responseCode != RC_SUCCESS_x00) {
                        // fallback 오류 발생
                        if (responseCode == RC_FALLBACK_xCF) {
                            return RTN_SCR_FALLBACK_OCCUR;
                        }
                        // IC 우선거래 발생
                        if (responseCode == RC_FIRST_IC_INSERT_xE8) {
                            return RTN_SCR_ERR_FIRST_IC;
                        }
                        // 상호 인증 실패 발생
                        if (responseCode == RC_AUTH_NOT_PERFORMED_xFB) {
                            return RTN_SCR_MUTUAL_AUTH_FAIL;
                        }
                        return RTN_SCR_FAIL;
                    }
                } else {
                    return RTN_INVALID_DATA;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // 카드구분자, AN(4)
                copiedSize = ArrayCopyAsSize(rxF.mCardProperty, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, copiedSize);
                // CVM, AN(1)
                copiedSize = ArrayCopyAsSize(rxF.mCVM, respMsg.rxDataValue, 1);
                DEBUG_FIELD_HEX("CVM", rxF.mCVM, copiedSize);
                // VAN ID, N(2)
                copiedSize = ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, copiedSize);
                // 암호화 정보, AN(96)
                copiedSize = ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, copiedSize);
                // 생성된 MAC, AN(8)
                copiedSize = ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, copiedSize);
                // 단말기 ID, AN(10)
                copiedSize = ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, copiedSize);
                // KSN, AN(20)
                copiedSize = ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, copiedSize);
                // TPL, AN(3)
                copiedSize = ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, copiedSize);
                // 일련번호(리더기 식별번호), AN(16)
                copiedSize = ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, copiedSize);
                // 유효기간, AN(6)
                copiedSize = ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, copiedSize);
                // fallback, N(3)
                copiedSize = ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("fallback", rxF.mFallbackCode, copiedSize);
                // 카드 Bin, N(6)
                copiedSize = ArrayCopyAsSize(rxF.mCardBinNo, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNo, copiedSize);
                // Chip 데이터 길이 필드, N(4)
                copiedSize = ArrayCopyAsSize(rxF.mChipDataLength, respMsg.rxDataValue, 4);
                DEBUG_FIELD_HEX("Chip 데이터 길이", rxF.mChipDataLength, copiedSize);
                if (copiedSize == 4 ) {
                    rxF.miChipDataLength = Integer.parseInt(new String(rxF.mChipDataLength));
                    if (rxF.miChipDataLength > 0) {
                        // IC EMV Chip Data, var(ChipData 길이)
                        rxF.mICEmvChipData = new byte[rxF.miChipDataLength];
                        ArrayCopyAsSize(rxF.mICEmvChipData, respMsg.rxDataValue, rxF.miChipDataLength);
                        DEBUG_FIELD_HEX("IC Emv ChipData", rxF.mICEmvChipData, rxF.mICEmvChipData.length);
                    }
                }

                // TID 일치?
                if(compareTerminalID(txF.mTerminalID, rxF.mTerminalID)) {
                    return RTN_SCR_SUCCESS;
                } else {
                    // 단말기 ID가 일치하지 않음
                    // DIK KEY 주입필요...
                    rxF.mResult_Code[0] = RC_NO_ID_xD2;
                    setResponseCodeMsg();
                    return RTN_SCR_ERR_EXCHANGE_KEY;
                }
            }
            else if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * SafeCard 암호화 MS Point Fallback 거래 요청
     * 0xBE    -->
     *            <-- ACK/NAK
     *            <-- 0xBE : MS Point Fallback 거래 응답
     * ACK/NAK -->
     * @param _IsEncrypt : 암호화 구분, "0" or 없음 : 암호화 하지 않음, "1" : SEED 암호화
     * @param _RandomKey : 카드번호 암호화 키, 암호화 구분 "0" : KEY 없음, 암호화 구분 "1" : 16Byte Random Key
     * @param _CardWaitTime : MS 카드 대기시간
     * @return : 상태
     */
    public int fallbackPointTransaction(String _IsEncrypt, byte[] _RandomKey, int _CardWaitTime) {
        boolean isAck = false;

        // Serial Port Check
        int readState = checkSerialPortOpened();
        if (readState != RTN_COMM_OK) return readState;

        // MS Card 대기시간
        txF.mCardWait_Time = _CardWaitTime;
        // 암호화 구분
        if (null != _IsEncrypt && !_IsEncrypt.isEmpty()) {
            txF.mIsEncrypt = _IsEncrypt;
        }
        // 카드번호 암호화 키
        if (txF.mIsEncrypt.equals(EncryptCardNumber)) {
            txF.mEncryptKeyForCardNo = _RandomKey;
        }

        // 요청전문 생성
        clearTxBuffer();
        txMsgDataLen = makeMsg.xBE_MakeMessage(txMsgData);
        if (txMsgDataLen < 5) return RTN_INVALID_DATA;

        //
        DEBUG_FIELD_HEX("SafeCard 암호화 MS Point Fallback 거래 요청전문", txMsgData, txMsgDataLen);

        // 요청전문 전송
        if (sendMsg(txMsgData, txMsgDataLen) != txMsgDataLen) {
            return RTN_SEND_FAIL;
        }

        //==========================================================================================
        // ACK/NAK 응답대기
        clearRxBuffer();
        long startTimeTick = GetStartTimeTick();
        while(CheckTickTimeOut(startTimeTick, TIMEOUT_3SEC)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CH_ACK) {
                isAck = true;
            }
            else if (respMsg.rxCommandID == CH_NAK) {
                return RTN_READ_NAK;
            }
            // 응답전문 분석
            else if (respMsg.rxCommandID == CMD_MS_Fallback_Credit_Point_xBE) {
                // Result Code, AN(1)
                ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                // Result Code Check
                setResponseCodeMsg();
                if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                    return RTN_SCR_FAIL;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // Fallback 구분(사유)코드, AN(2)
                ArrayCopyAsSize(rxF.mFallbackReasonCode, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("Fallback 구분", rxF.mFallbackReasonCode, rxF.mFallbackReasonCode.length);
                // VAN ID, N(2)
                ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, rxF.mVanID.length);
                // 암호화 정보, AN(96)
                ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, rxF.mEncryptedInfo.length);
                // 생성된 MAC, AN(8)
                ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, rxF.mCreatedMAC.length);
                // 단말기 ID, AN(10)
                ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, rxF.mTerminalID.length);
                // KSN, AN(20)
                ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, rxF.mKSN.length);
                // TPL, AN(3)
                ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, rxF.mTPL.length);
                // 일련번호(리더기 식별번호), AN(16)
                ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, rxF.mReaderAuthID.length);
                // 유효기간, AN(6)
                ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, rxF.mPMKValidity.length);
                // fallback, N(3)
                ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, rxF.mCardProperty.length);
                // 카드 BIN
                if (txF.mIsEncrypt.equals(NotEncryptCardNumber)) {
                    // 카드 Bin, N(19), 암호화 하지 않은 카드번호
                    ArrayCopyAsSize(rxF.mCardBinNoPlain, respMsg.rxDataValue, 19);
                    DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNoPlain, rxF.mCardBinNoPlain.length);
                } else {
                    // 카드 Bin, N(64), 암호화 된 카드번호
                    ArrayCopyAsSize(rxF.mCardBinNoEncrypt, respMsg.rxDataValue, 64);
                    DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNoEncrypt, rxF.mCardBinNoEncrypt.length);
                }
                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        //  ACK Check
        if (!isAck) return RTN_READ_FAIL;
        //==========================================================================================
        startTimeTick = GetStartTimeTick();
        // Card Wait Time(Sec) 만큼 응답대기
        long timeOverTick = txF.mCardWait_Time*1000 + TIMEOUT_2SEC;
        while(CheckTickTimeOut(startTimeTick, timeOverTick)) {
            readState = readMsg(100);
            if (readState != RTN_CONTINUE) break;
            // 사용자 강제 종료 이벤트 체크
            if (getUserStopEvent()) {
                // 초기화
                sendEot();
                return RTN_CANCEL;
            }
        }

        // 통신버퍼 Clear
        clearBuffer();

        if (readState == RTN_COMM_OK) {
            // 응답전문 분석
            if (respMsg.rxCommandID == CMD_MS_Fallback_Credit_Point_xBE) {
                // Result Code, AN(1)
                ArrayCopyAsSize(rxF.mResult_Code, respMsg.rxDataValue, 1);
                // Result Code Check
                setResponseCodeMsg();
                if (rxF.mResult_Code[0] != RC_SUCCESS_x00) {
                    return RTN_SCR_FAIL;
                }

                /*
                 * 응답 코드 SUCCESS(0x00)
                 */
                // Send ACK
                sendAck();

                // Data Value Parse
                // Fallback 구분(사유)코드, AN(2)
                ArrayCopyAsSize(rxF.mFallbackReasonCode, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("Fallback 구분", rxF.mFallbackReasonCode, rxF.mFallbackReasonCode.length);
                // VAN ID, N(2)
                ArrayCopyAsSize(rxF.mVanID, respMsg.rxDataValue, 2);
                DEBUG_FIELD_HEX("VAN ID", rxF.mVanID, rxF.mVanID.length);
                // 암호화 정보, AN(96)
                ArrayCopyAsSize(rxF.mEncryptedInfo, respMsg.rxDataValue, 96);
                DEBUG_FIELD_HEX("암호화 정보", rxF.mEncryptedInfo, rxF.mEncryptedInfo.length);
                // 생성된 MAC, AN(8)
                ArrayCopyAsSize(rxF.mCreatedMAC, respMsg.rxDataValue, 8);
                DEBUG_FIELD_HEX("생성된 MAC", rxF.mCreatedMAC, rxF.mCreatedMAC.length);
                // 단말기 ID, AN(10)
                ArrayCopyAsSize(rxF.mTerminalID, respMsg.rxDataValue, 10);
                DEBUG_FIELD_HEX("단말기 ID", rxF.mTerminalID, rxF.mTerminalID.length);
                // KSN, AN(20)
                ArrayCopyAsSize(rxF.mKSN, respMsg.rxDataValue, 20);
                DEBUG_FIELD_HEX("KSN", rxF.mKSN, rxF.mKSN.length);
                // TPL, AN(3)
                ArrayCopyAsSize(rxF.mTPL, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("TPL", rxF.mTPL, rxF.mTPL.length);
                // 일련번호(리더기 식별번호), AN(16)
                ArrayCopyAsSize(rxF.mReaderAuthID, respMsg.rxDataValue, 16);
                DEBUG_FIELD_HEX("리더기 식별번호", rxF.mReaderAuthID, rxF.mReaderAuthID.length);
                // 유효기간, AN(6)
                ArrayCopyAsSize(rxF.mPMKValidity, respMsg.rxDataValue, 6);
                DEBUG_FIELD_HEX("유효기간", rxF.mPMKValidity, rxF.mPMKValidity.length);
                // fallback, N(3)
                ArrayCopyAsSize(rxF.mFallbackCode, respMsg.rxDataValue, 3);
                DEBUG_FIELD_HEX("카드구분자", rxF.mCardProperty, rxF.mCardProperty.length);
                // 카드 BIN
                if (txF.mIsEncrypt.equals(NotEncryptCardNumber)) {
                    // 카드 Bin, N(19), 암호화 하지 않은 카드번호
                    ArrayCopyAsSize(rxF.mCardBinNoPlain, respMsg.rxDataValue, 19);
                    DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNoPlain, rxF.mCardBinNoPlain.length);
                } else {
                    // 카드 Bin, N(64), 암호화 된 카드번호
                    ArrayCopyAsSize(rxF.mCardBinNoEncrypt, respMsg.rxDataValue, 64);
                    DEBUG_FIELD_HEX("카드 Bin", rxF.mCardBinNoEncrypt, rxF.mCardBinNoEncrypt.length);
                }
                return RTN_SCR_SUCCESS;
            }
            else {
                return RTN_INVALID_DATA;
            }
        }
        // 응답대기 시간 초과
        else if (readState == RTN_CONTINUE) {
            return RTN_TIMEOUT;
        }

        return readState;
    }

    /**
     * 보안리더 응답코드 메시지 설정
     */
    private void setResponseCodeMsg() {
        // Reader 응답 결과코드
        responseCode = rxF.mResult_Code[0];
        responseMsg = "(x" + String.format("%02X", responseCode) + ")";
        switch (responseCode) {
            case RC_SUCCESS_x00:
                responseMsg = RESP_CODE_MSG_x00 + responseMsg; break;
            case RC_APDU_ERROR_x8C:
                responseMsg = RESP_CODE_MSG_x8C + responseMsg; break;
            case RC_INVALID_CONDITION_x8D:
                responseMsg = RESP_CODE_MSG_x8D + responseMsg; break;
            case RC_INVALID_PARAM_x95:
                responseMsg = RESP_CODE_MSG_x95 + responseMsg; break;
            case RC_REVERSAL_xCC:
                responseMsg = RESP_CODE_MSG_xCC + responseMsg; break;
            case RC_CANCEL_xCD:
                responseMsg = RESP_CODE_MSG_xCD + responseMsg; break;
            case RC_DECLINE_xCE:
                responseMsg = RESP_CODE_MSG_xCE + responseMsg; break;
            case RC_FALLBACK_xCF:
                responseMsg = RESP_CODE_MSG_xCF + responseMsg; break;
            case RC_NO_SIGNATURE_xD0:
                responseMsg = RESP_CODE_MSG_xD0 + responseMsg; break;
            case RC_NO_SafeMSR_KEY_xD1:
                responseMsg = RESP_CODE_MSG_xD1 + responseMsg; break;
            case RC_NO_ID_xD2:
                responseMsg = RESP_CODE_MSG_xD2 + responseMsg; break;
            case RC_ERR_DEVICE_MSR_xE1:
                responseMsg = RESP_CODE_MSG_xE1 + responseMsg; break;
            case RC_ERR_DEVICE_IFM_xE2:
                responseMsg = RESP_CODE_MSG_xE2 + responseMsg; break;
            case RC_INVALID_KEY_PMK_xE4:
                responseMsg = RESP_CODE_MSG_xE4 + responseMsg; break;
            case RC_DIFF_SAFECARD_KEY_xE6:
                responseMsg = RESP_CODE_MSG_xE6 + responseMsg; break;
            case RC_FIRST_IC_INSERT_xE8:
                responseMsg = RESP_CODE_MSG_xE8 + responseMsg; break;
            case RC_FIRST_NOT_FALLBACK_xE9:
                responseMsg = RESP_CODE_MSG_xE9 + responseMsg; break;
            case RC_ERR_DEVICE_INIT_xEC:
                responseMsg = RESP_CODE_MSG_xEC + responseMsg; break;
            case RC_NO_CARD_xF2:
                responseMsg = RESP_CODE_MSG_xF2 + responseMsg; break;
            case RC_NOT_ACCEPT_xF5:
                responseMsg = RESP_CODE_MSG_xF5 + responseMsg; break;
            case RC_INVALID_DATA_xF8:
                responseMsg = RESP_CODE_MSG_xF8 + responseMsg; break;
            case RC_AUTH_ERROR_xFA:
                responseMsg = RESP_CODE_MSG_xFA + responseMsg; break;
            case RC_AUTH_NOT_PERFORMED_xFB:
                responseMsg = RESP_CODE_MSG_xFB + responseMsg; break;
            case RC_APP_INTEGRITY_FAIL_xFC:
                responseMsg = RESP_CODE_MSG_xFC + responseMsg; break;
            case RC_KEY_INTEGRITY_FAIL_xFD:
                responseMsg = RESP_CODE_MSG_xFD + responseMsg; break;
            case RC_FAILURE_xFF:
                responseMsg = RESP_CODE_MSG_xFF + responseMsg; break;
            default:
                responseMsg = RESP_CODE_MSG_xXX + responseMsg; break;
        }
    }

    /**
     * 응답코드 메시지 설정
     */
    public void setReturnCodeMsg(int resultCode) {
        switch (resultCode) {
            case RTN_CANCEL:
                responseMsg = "거래 강제종료";
                break;
            case RTN_TIMEOUT:
                responseMsg = "대기시간 초과";
                break;
            case RTN_INVALID_PARAM:
                responseMsg = "파라메타 오류";
                break;
            case RTN_INVALID_DATA:
                responseMsg = "데이타 오류";
                break;
            case RTN_SEND_FAIL:
                responseMsg = "데이터 전송오류";
                break;
            case RTN_SCR_DEVICE_ERROR:
                responseMsg = "제조사 A/S요망";
                break;
            case RTN_SCR_MUTUAL_AUTH_FAIL:
                responseMsg = "상호인증 검증실패";
                break;
            default:
                break;
        }
    }

    /**
     * 응답코드 메시지 가져오기
     * @return          : 응답코드 메시지
     */
    public String getRespCodeMsg() {
        return responseMsg;
    }

    /**
     * 응답코드 가져오기
     * @return          : 응답코드
     */
    public byte getReaderResponseCode() {
        return responseCode;
    }

    /**
     * TID/CAT-ID 비교
     * @param _PosTID : POS TID(CAT-ID)
     * @param _ScrTID : 수신된 SCR TID(CAT-ID)
     * @return : true/false
     */
    private boolean compareTerminalID(byte[] _PosTID, byte[] _ScrTID) {
        if (null == _PosTID || _PosTID.length == 0) return false;
        if (null == _ScrTID || _ScrTID.length == 0) return false;

        //
        if(Arrays.equals(_PosTID, _ScrTID)) {
            return true;
        } else {
            //
            int iPosTID = 0;
            String sPosTID = new String(_PosTID).trim();
            if (!sPosTID.isEmpty()) {
                iPosTID = Integer.parseInt(sPosTID);
            }
            int iScrTID = 0;
            String sScrTID = new String(_ScrTID).trim();
            if (!sScrTID.isEmpty()) {
                iScrTID = Integer.parseInt(sScrTID);
            }
            return Integer.compare(iPosTID, iScrTID) == 0;
        }
    }


}
