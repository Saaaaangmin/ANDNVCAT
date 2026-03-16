package com.posbank.device.van.kis.model;

import android.content.Context;

import kisvan.Kisvan;


//==================================================================================================//
// VAN 결제용 필요 객체 생성
//==================================================================================================//
public class KisInstance {

    // KIS 객체생성
    private Kisvan kisvan = new Kisvan();

    // Context
    private Context context;

    // SCR COM-PORT 번호 및 baud rate 설정정보
    private String scrPort, scrSpeed;

    // TCP/IP Address & PORT
    private String tcpAddress, tcpPort;

    // TID
    private String terminalID;

    // 사업자번호
    private String posBusinessNo;

    // POS 시리얼번호
    private String posSerialNo;

    // APP 정보
    private String appCertification;
    private String appVersion;

    //==============================================================================================
    // Constructor
    //==============================================================================================
    public KisInstance(Context _context,
                       String _scrPort,
                       String _scrSpeed,
                       String _tcpAddress,
                       String _tcpPort,
                       String _terminalID,
                       String _posBusinessNo,
                       String _posSerialNo,
                       String _appCertification,
                       String _appVersion) {

        this.context    = _context;
        this.scrPort    = _scrPort;
        this.scrSpeed   = _scrSpeed;
        this.tcpAddress = _tcpAddress;
        this.tcpPort    = _tcpPort;
        this.terminalID = _terminalID;
        this.posBusinessNo = _posBusinessNo;
        this.posSerialNo   = _posSerialNo;
        this.appCertification = _appCertification;
        this.appVersion = _appVersion;
    }

    //==============================================================================================
    // Singleton 변수 getter
    //==============================================================================================
    public Kisvan getKisvan() {
        return kisvan;
    }

    public Context getContext() {
        return context;
    }

    public String getScrPort() {
        return scrPort;
    }

    public String getScrSpeed() {
        return scrSpeed;
    }

    public String getTcpAddress() {
        return tcpAddress;
    }

    public String getTcpPort() {
        return tcpPort;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public String getPosBusinessNo() {
        return posBusinessNo;
    }

    public String getPosSerialNo() {
        return posSerialNo;
    }

    public String getAppCertification() {
        return appCertification;
    }

    public String getAppVersion() {
        return appVersion;
    }

}
