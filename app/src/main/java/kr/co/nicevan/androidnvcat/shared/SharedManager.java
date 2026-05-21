package kr.co.nicevan.androidnvcat.shared;

import static android.content.Context.MODE_PRIVATE;

import static com.posbank.device.common.ReturnValue.RTN_COMM_OK;
import static kr.co.nicevan.androidnvcat.MainActivity.btnEnable;
import static kr.co.nicevan.androidnvcat.MainActivity.mSharedManager;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpenEOT;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RECVBuf;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.Roundkey;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.bFirst;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.dialog;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.encdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.icdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.initSerial;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isSign;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isrun;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.key_down;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.mUart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.recvBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.scr;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.sendBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.space;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.temp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.usbService;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.xor_sum;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.posbank.device.screader.kis.protocol.ScrProtocolCom;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import kr.co.nicevan.androidnvcat.DetailDealResult;
import kr.co.nicevan.androidnvcat.MainActivity;
import kr.co.nicevan.pos.PosClient;
import okpos.co.kr.payroid.libUart;

public class SharedManager {
    public static String SWNUM = "####ANDNVCAT1005"; //OSM20251223 : 버전 변경
    public static String ROMVER = "AVKN04"; //LJY20230911 : 버전 변경
    public static String APKHASH = "";
    public static int iresult = 0;

    public static boolean bStart = false;
    public static boolean isStatus = false;
    public static boolean isBizdown = false;
    public static boolean bRooting = false;
    public static boolean bApkchk = false;
    public static boolean bRelease = true;   //OSM20260430 : 릴리즈용 수정
    public static boolean bLogUse = true;
    public static boolean bSetenv = false;    //OSM20240523 : setenv 구분자 값 추가
    public static boolean bCount = false;     //OSM20240617 : 설정 저장 여부 확인을 위한 구분자 값 추가
    public static boolean bNoTimer = false;   //OSM20260205 : 팝업 타이머 제외 구분자 추가

    private static SharedManager mInstance = null;
    private SharedPreferences mPreferences;

    public SharedPreferences getPreferences() {
        return mPreferences;
    }
    public SharedPreferences.Editor getEditer() {
        return mPreferences.edit();
    }

    private SharedManager(Context context) {
        mPreferences = context.getSharedPreferences("pref", MODE_PRIVATE);
    }

    public static SharedManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedManager(context);
        }
        return mInstance;
    }

    public static void LogDebug(boolean bUse, String sTag, String sValue) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss SSS"); //LJY20220520 : msec 추가
        if (bUse) {
            Log.d(sTag, "[" + simpleDateFormat.format(new Date()) + "] " + sValue);
        }
    }

    public static void LogBinHex(String sTag, byte[] bSendbyte) { //LJY20230911 : HEX 로그 함수 추가
        StringBuffer sb = new StringBuffer(bSendbyte.length * 2);
        String hexNumber = "";
        for (int x = 0; x < bSendbyte.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & bSendbyte[x]);
            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] " + sTag + " Bin : [" + new String(bSendbyte) + "]");
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] " + sTag + " Hex : [" + sb.toString() + "]");
    }

    public static String byteBuffer2String(ByteBuffer buf, Charset charset) {
        byte[] bytes;
        if (buf.hasArray()) {
            bytes = buf.array();
        } else {
            buf.rewind();
            bytes = new byte[buf.remaining()];
        }
        return new String(bytes, charset);
    }

    //JDK20230220 : 안드로이드 디바이스 UUID 생성
    public static String GetDevicesUUID(Context mContext) {
        LogDebug(bLogUse, "debugjy", "[NVCAT] GetDevicesUUID");

        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_generater : " + "permission error");
            return "Fail to Get UUID";
        }


        tmDevice = "" + tm.getDeviceId();
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_tmDevice : " + tmDevice);
        tmSerial = "" + tm.getSimSerialNumber();
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_tmSerial : " + tmSerial);
        androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_androidId : " + androidId);


        int deHashCode = tmDevice.hashCode();
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_tmDevice hash : " + deHashCode);
        int seHashCode = tmSerial.hashCode();
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_tmSerial hash : " + seHashCode);
        int idHashCode = androidId.hashCode();
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_androidId hash : " + idHashCode);


        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_generater : " + deviceId);
        String deviceId2 = deviceId.replace("-", "");
        LogDebug(bLogUse, "debugjy", "[NVCAT] UUID_generater : " + deviceId2);

        return deviceId2;
    }

    public static String BarcodeToTrack2(String input) { //LJY20250904 : 바코드 데이터를 TRACK2 필드에 넣을 데이터로 변환
        int iDatalen = Integer.parseInt(input.substring(0, 4));

        String sData = input.substring(4, 4 + iDatalen);

        if (!sData.contains("=") && !sData.substring(0, 7).equals("hQVDUFY")) { //LJY20250918 : BC QR 예외 추가
            sData += "=";
            iDatalen += 1;
        }

        byte[] bResult = null;
        if(iDatalen <= 125)
            bResult = new byte[125]; //LJY20250918 : 사이즈 변경
        else
            bResult = new byte[iDatalen];

        byte[] bDatabytes = sData.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bDatabytes, 0, bResult, 0, bDatabytes.length);

        Arrays.fill(bResult, bDatabytes.length, bResult.length, (byte) ' ');

        return new String(bResult);
    }

    public static boolean IsBarcodeSign(String input) { //LJY20250904 : PAYPRO 서명 필요 유무 체크
        int iDatalen = Integer.parseInt(input.substring(0, 4));

        String sData = input.substring(4, 4 + iDatalen);
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 바코드 : " + sData);

        byte[] bDatabytes = sData.getBytes(StandardCharsets.US_ASCII);

        String sBarcode = new String(bDatabytes);
        if (sBarcode.length() == 24 && sBarcode.substring(0, 6).equals("281006")) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 카카오페이 거래입니다.");
            return true;
        } else if(sBarcode.length() == 24 && (sBarcode.substring(0, 8).equals("70550001") || sBarcode.substring(0, 8).equals("70550002") || sBarcode.substring(0, 8).equals("70550003"))) {
            if(sBarcode.substring(0, 8).equals("70550001"))       SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 토스페이 카드 거래입니다.");
            else if(sBarcode.substring(0, 8).equals("70550002"))  SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 토스페이 머니 거래입니다.");
            else                                                  SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 토스페이 계좌 거래입니다.");
            return true;
        } else if (sBarcode.length() == 22 && sBarcode.substring(0, 2).equals("17")) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO SSGPAY 거래입니다.");
            return true;
        } else if (sBarcode.length() == 22 && sBarcode.substring(0, 4).equals("8710")) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO LPAY 거래입니다.");
            return true;
        } else if ((sBarcode.length() > 16 && sBarcode.length() < 25) && (Integer.parseInt(sBarcode.substring(0, 2)) > 19 && Integer.parseInt(sBarcode.substring(0, 2)) < 34)) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 알리페이 거래입니다.");
            return false;
        } else if ((sBarcode.length() > 15 && sBarcode.length() < 22) && (Integer.parseInt(sBarcode.substring(0, 2)) > 9 && Integer.parseInt(sBarcode.substring(0, 2)) < 16)) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 위챗페이 거래입니다.");
            return false;
        } else if (sBarcode.length() == 24 && sBarcode.substring(0, 6).equals("800088")) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 제로페이 바코드 거래입니다.");
            return false;
        } else if (sBarcode.length() == 25 && sBarcode.substring(0, 2).equals("3-")) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 제로페이 QR 거래입니다.");
            return false;
        } else if (sBarcode.length() == 21) {
            if (sBarcode.substring(0, 6).equals("941083") || sBarcode.substring(0, 8).equals("94204023") || sBarcode.substring(0, 8).equals("94204024") || sBarcode.substring(0, 8).equals("94204025")) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 서울페이 거래입니다.");
                return false;
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 앱카드 거래입니다.");
                return true;
            }
        } else if (sBarcode.length() >= 100 && sBarcode.substring(0, 7).equals("hQVDUFY")) {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 비씨은련QR 거래입니다.");
            return true;
        }

        return true;
    }

    public static byte[] SendRecv(String serverIp, String serverPort, byte[] sendBuff) {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] @serverIp : " + serverIp);
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] @serverPort : " + serverPort);
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] @sendBuff : " + new String(sendBuff));
        if (bRelease)
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 55) + "*******************************************************************************************************************************");
        else
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));

        PosClient posClient = new PosClient();

        byte[] recvBuff = null;

        if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) {
            // LJY20230111 : 전용회선 사용 시
            System.arraycopy(
                    String.format("%04d", sendBuff.length).getBytes(),
                    0,
                    sendBuff,
                    0,
                    4
            );
            recvBuff = posClient.service_line(serverIp, Integer.parseInt(serverPort), sendBuff);

        } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
            // OSM20250113 : SEED 암복호화
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화");
            recvBuff = posClient.service(serverIp, Integer.parseInt(serverPort), sendBuff);
            SharedManager.LogDebug(bLogUse, "debugjy", "@recvBuff : " + new String(recvBuff));

        } else {
            // OSM20250113 : DES 암복호화
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화");
            posClient.service_DES(serverIp, Integer.parseInt(serverPort), sendBuff, recvBuff);
        }

        return recvBuff;
    }

    public static void Sleep(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte toBcd(int value) {
        if (value < 0 || value > 99) {
            throw new IllegalArgumentException("value must be 0~99: " + value);
        }
        int tens = value / 10;     // 십의 자리
        int ones = value % 10;     // 일의 자리
        return (byte) ((tens << 4) | ones);
    }

    public static byte[] MakeReqData_NICE(char cFuncCode, String sSenddata, String sLogMsg) {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] " + sLogMsg);

        if(cFuncCode != 0x42) {
            Arrays.fill(RECVBuf, (char) 0x00);
            Arrays.fill(encdata, (char) 0x00);
            Arrays.fill(icdata, (char) 0x00);
        }

        initSerial();

        int iDataLen = (sSenddata != null) ? sSenddata.length() : 0;
        int iBuffersize = 1 + 1 + 2 + iDataLen + 1; //STX(1)+CMD(1)+LEN(2)+DATA(N)+LRC(1)

        byte[] bReqData = new byte[iBuffersize];

        int slen = 0;
        bReqData[slen++] = 0x02;                    // STX
        bReqData[slen++] = (byte) cFuncCode;  // CMD
        bReqData[slen++] = (byte) toBcd((iDataLen / 100));
        bReqData[slen++] = (byte) toBcd((iDataLen % 100));  //LEN

        if (iDataLen > 0) {
            System.arraycopy(sSenddata.getBytes(), 0, bReqData, slen, iDataLen);
            slen += iDataLen;
        }

        bReqData[slen] = xor_sum(bReqData, slen);
        slen++;

        return bReqData;
    }



    public static int PrintVanRecvdata(Context context, byte[] recvBuff) {
        try {
            if (new String(recvBuff, "EUC-KR").equals("-1")) {
                Toast.makeText(context, "-1:서버연결실패", Toast.LENGTH_SHORT).show();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -1:서버연결실패");
                btnEnable();

            } else if (new String(recvBuff, "EUC-KR").equals("-2")) {
                Toast.makeText(context, "-2:서버 전문 송신 실패", Toast.LENGTH_SHORT).show();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -2:서버 전문 송신 실패");
                btnEnable();
            } else if (new String(recvBuff, "EUC-KR").equals("-3")) {
                Toast.makeText(context, "-3:서버 전문 수신 실패", Toast.LENGTH_SHORT).show();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -3:서버 전문 수신 실패");
                btnEnable();
            } else if (new String(recvBuff, "EUC-KR").equals("-4")) {
                Toast.makeText(context, "-4:서버 키교환 실패", Toast.LENGTH_SHORT).show();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -4:서버 키교환 실패");
                btnEnable();
            } else if (new String(recvBuff, "EUC-KR").equals("-5")) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -5:서버 전문 암복호화 실패");
                Toast.makeText(context, "-5:서버 전문 암복호화 실패", Toast.LENGTH_SHORT).show();
                btnEnable();
            } else {
                if (bRelease)
                    SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + (new String(recvBuff, "EUC-KR")).substring(0, 59) + "**************************************************");
                else
                    SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + new String(recvBuff, "EUC-KR"));

                return 1;
            }
            return -1;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void SerialDataSendOnly(int iReaderType, byte[] bSenddata) {
        if (iReaderType == 3) //POSBANK
            scr.sendEot();
        else if (iReaderType == 2) //OKPOS
        {
            if (isMultipad || isSign)
                mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), bSenddata, bSenddata.length);
            else
                mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), bSenddata, bSenddata.length);
        } else
            usbService.write(bSenddata);
    }

    public static int SerialDataSend(Context context, int iPopupType, String sPopupMsg, int iReaderType, byte[] bSenddata) {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] @sPopupMsg : " + sPopupMsg);
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] @iPopupType : " + iPopupType);
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] @iReaderType : " + iReaderType);

        if (iReaderType == 3) //POSBANK
        {
            isrun = true;

            scr = new ScrProtocolCom(context, "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0) + 1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

            int readState = scr.checkSerialPortOpened();
            if (readState != RTN_COMM_OK) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                Toast.makeText(context, "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                btnEnable();
                return -16;
            }

            scr.clearTxBuffer();
            scr.sendMsg(bSenddata, bSenddata.length);
            scr.clearRxBuffer();
        } else if (iReaderType == 2 || iReaderType == 7) //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가 //OKPOS
        {
            isrun = true;

            mUart = new libUart();
            mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
            }
            mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
            mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), bSenddata, bSenddata.length);
        } else {
            if (usbService != null) {
                if(isrun == false) {        //OSM20240605 : 중복 호출 방지 추가
                    isrun = true;
                    usbService.write(bSenddata);
                }
                else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");
                    if(iReaderType != 6)    //LJY20251111 : TTM 추가
                        Toast.makeText(context, "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return -17;
                }
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                if(iReaderType != 6)    //LJY20251111 : TTM 추가
                    Toast.makeText(context, "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                btnEnable();
                return -15;
            }
        }
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] " + sPopupMsg);

        if(iPopupType == 99) {
            //팝업 스킵
        } else if(iPopupType == 1)
            PopupOpenEOT(context, sPopupMsg);
        else
            PopupOpen(context, sPopupMsg);

        return 1;
    }

    public static void SetFinish(Context context, int iReturnCode, String sReuslt, boolean bToast, boolean bBtnEnable) {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] " + sReuslt);
        if(bToast || SharedManager.getInstance(context).getPreferences().getBoolean("Toastuse", false) == true) //OSM20250929 : 토스트메시지 사용하는 경우
            Toast.makeText(context, sReuslt, Toast.LENGTH_SHORT).show();
        if(bBtnEnable)  btnEnable();
    }

    public static String extractString(char[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 0x00) {
                return new String(buffer, 0, i);
            }
        }
        return new String(buffer);
    }

    public static int decodeHexChars(char[] asciiHex, int off, int len, char[] out, int outOff) {
        if ((len & 1) != 0) throw new IllegalArgumentException("hex length must be even");
        int chars = len / 2;
        for (int i = 0; i < chars; i++) {
            int hi = Character.digit(asciiHex[off + 2 * i], 16);
            int lo = Character.digit(asciiHex[off + 2 * i + 1], 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Invalid hex at index " + (off + 2 * i));
            }
            out[outOff + i] = (char) ((hi << 4) | lo);
        }
        return chars;
    }

    public static void memset(byte[] bBuffer) {
        if(bBuffer != null)
        {
            Arrays.fill(bBuffer, (byte) 0x20);
            Arrays.fill(bBuffer, (byte) 0xFF);
            Arrays.fill(bBuffer, (byte) 0x00);
        }
    }

    public static void memset(char[] bBuffer) {
        if(bBuffer != null)
        {
            Arrays.fill(bBuffer, (char) 0x20);
            Arrays.fill(bBuffer, (char) 0xFF);
            Arrays.fill(bBuffer, (char) 0x00);
        }
    }

    public static byte[] MakeReqVanSendData(int iPosType, String sTxt, String sTxtNumber, String sDealgb, String sDealtp, String sDevicegb, String sCatid, String sWCC, String sCarddata, String sHalbu, String sBongsa, String sTax, String sMoney, String sApprno, String sApprdate, String sApprCarid, String sMyunse, String sFiller, String sIcdata, String sSingdata, String sMPAddInfoYN, String sMPBizCode, String sMPAddInfo, String sMPFiller2) {
        byte[] bSenddata = null;
        String sSpace = "                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      ";

        if(sTxt.length() < 1 || sTxt.length() > 3)                  sTxt = "HPS";

        if(sCatid.length() < 1 || sCatid.length() > 10)             sCatid = mSharedManager.getPreferences().getString("Catid", "          ");
        else                                                        sCatid = String.format("%-10s", sCatid);

        if(sTxtNumber.length() < 1 || sTxtNumber.length() > 20) {
            sTxtNumber = sCatid + new SimpleDateFormat("MMddHHmmss").format(new Date());
        }

        if(sDealgb.length() < 1 || sDealgb.length() > 4)            sDealgb = "    ";

        if(sDealtp.length() < 1 || sDealtp.length() > 2)            sDealtp = "  ";

        if(sDevicegb.length() < 1 || sDevicegb.length() > 2)        sDevicegb = "H1";

        if(sWCC.length() < 1 || sWCC.length() > 1)                  sWCC = " ";

        if(sCarddata.length() > 127)                                sCarddata = sSpace.substring(0, 127);
        else                                                        sCarddata = sCarddata + sSpace.substring(0, 127-sCarddata.length());

        if(sHalbu.length() < 1 || sHalbu.length() > 127)            sHalbu = "00";

        if(sBongsa.length() < 1 || sBongsa.length() > 12)           sBongsa = "000000000000";

        if(sTax.length() < 1 || sTax.length() > 12)                 sTax = "000000000000";

        if(sMoney.length() < 1 || sMoney.length() > 12)             sMoney = "000000000000";

        if(sApprno.length() < 1 || sApprno.length() > 8)            sApprno = sSpace.substring(0, 8);
        else                                                        sApprno = sApprno + sSpace.substring(0, 8-sApprno.length());

        if(sApprdate.length() < 1 || sApprdate.length() > 6)        sApprdate = sSpace.substring(0, 6);
        else                                                        sApprdate = sApprdate + sSpace.substring(0, 6-sApprdate.length());

        if(sApprCarid.length() < 1 || sApprCarid.length() > 30)     sApprCarid = sSpace.substring(0, 30);
        else                                                        sApprCarid = sApprCarid + sSpace.substring(0, 30-sApprCarid.length());

        String sHwnum = mSharedManager.getPreferences().getString("HWNUM", "################");
        String sSwnum = SharedManager.SWNUM;

        if(sMyunse.length() < 1 || sMyunse.length() > 20)           sMyunse = sSpace.substring(0, 20);

        String sTotalLen = "";
        String sMPTotalLen = "";
        String sSignLen = "";
        if(sSingdata.length() > 0) {
            sSignLen = String.format("%04d", 34 + Integer.parseInt(sSingdata.substring(0, 4)));
            sTotalLen = String.format("%04d", 437 + sIcdata.length() + 4 + 34 + Integer.parseInt(sSingdata.substring(0, 4)));
            sMPTotalLen = String.format("%04d", 437 + 380 + sIcdata.length() + 4 + 34 + Integer.parseInt(sSingdata.substring(0, 4)));
        }
        else {
            sTotalLen = String.format("%04d", 437 + sIcdata.length());
            sMPTotalLen = String.format("%04d", 437 + 380 + sIcdata.length());
        }

        if(iPosType == 1) { //머니플러스
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 머니플러스 전문입니다.");
            if (sSingdata.length() == 0) {
                bSenddata = (sMPTotalLen + sTxt + sTxtNumber + sDealgb + sDealtp + sDevicegb + "          " + sCatid + sWCC + sCarddata + sHalbu + sBongsa + sTax + sMoney + "        " + "      " + "                                                   " + sApprCarid + sMyunse + sHwnum + sSwnum + "  " + sFiller + "N" + "NVC" + "          " + SharedManager.ROMVER + sMPAddInfoYN + sMPBizCode + sMPAddInfo + sMPFiller2).getBytes();
            }
        }
        else {
            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 일반 전문입니다.");

            if(sIcdata.length() == 0 && sSingdata.length() == 0)
                bSenddata = (sTotalLen + sTxt + sTxtNumber + sDealgb + sDealtp + sDevicegb + "          " + sCatid + sWCC + sCarddata + sHalbu + sBongsa + sTax + sMoney + "        " + "      " + "                                                   " + sApprCarid + sMyunse + sHwnum + sSwnum + "  " + sFiller + "N").getBytes();
        }

        return bSenddata;
    }
}





