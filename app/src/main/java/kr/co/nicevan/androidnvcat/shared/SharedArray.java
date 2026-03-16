package kr.co.nicevan.androidnvcat.shared;

import static kr.co.nicevan.androidnvcat.MainActivity.mSharedManager;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.LogDebug;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bNoTimer;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.isBizdown;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.CountDownTimer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;


import com.devmel.communication.IUart;
import com.posbank.device.screader.kis.protocol.ScrProtocolCom;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.co.nicevan.androidnvcat.DifferentDisplay;
import kr.co.nicevan.androidnvcat.R;
import kr.co.nicevan.androidnvcat.UsbService;
import kr.co.nicevan.androidnvcat.nm2000.ZOACardReader;
import okpos.co.kr.payroid.libReader;
import okpos.co.kr.payroid.libUart;

public class SharedArray {
    public static char[] key_info = new char[64];
    public static char[] key_down = new char[287];
    public static char[] encdata = new char[127];
    public static char[] icdata = new char[512];        //OSM20260312 : 사이즈 변경 (257 -> 512)
    public static char[] RECVBuf = new char[4096];
    public static char[] CSN = new char[16];
    public static char[] HWNUM = new char[16];
    public static char[] RND_P1 = new char[8];
    public static char[] RND_P2 = new char[8];
    public static char[] RND_R1 = new char[8];
    public static char[] RND_R2 = new char[8];
    public static char[] cENC_READER = new char[32];
    public static char[] cDEC_READER = new char[16];
    public static char[] Bseed12 = new char[16];
    public static char[] ENC_TEMP = new char[16];
    public static char[] MSK = new char[16];
    public static char[] ASK = new char[16];
    public static char[] cENC_POS_temp = new char[16];
    public static char[] RND_FORM2 = new char[16];
    public static char[] ReaderSN = new char[10];
    public static char[] Paygb = new char[1];
    public static char[] writeBuffer = null;
    public static char[] CardBrand = new char[1]; //LJY20200713 : 동반위 JUST TOUCH
    public static char[] KeyDownCnt = new char[2]; //LJY20200918 : 키다운로드 카운트
    public static char[] CardCvm = new char[1]; //LJY20230713 : 은련PIN 체크
    public static char[] cReaderBinVer = new char[2]; //LJY20250904 : 8BIN/통합결제 적용
    public static char[] cSupportedList = new char[11]; //OSM20260312 : EMV Contactless 지원가능목록 변수

    public static char[] cMediagb = new char[1];

    public static int[] Roundkey = new int[32];
    public static int[] ret = new int[1];
    public static byte[] sendBuff = null;
    public static byte[] recvBuff = new byte[3000];
    public static byte[] sendBuffBody = null;   //OSM20260312 : 전문 바디 부 변수 추가

    public static byte[] temp = null;
    public static byte[] bEncPin = null;
    public static byte[] handlertemp = null;
    public static byte[] signBuff = null;
    public static byte[] DccmsgBuff = new byte[100]; //20200312 : DCC 개발
    public static String sTxtnum;  //OSM20241011 : 임시 전문관리번호 변수 추가
    public static UsbService usbService;

    public static libUart mUart;
    public static libReader mReader;
    public static ScrProtocolCom scr = null;

    public static Dialog dialog;

    public static char func_code;
    public static String sReaderApprtp; //LJY20250904 : 8BIN/통합결제 적용
    public static boolean isMultipad;
    public static boolean isSign; //LJY20201006 : OKPOS 서명 연동
    public static boolean isGetReader; //LJY20221004 : 리더기 정보 가져오기
    public static boolean isReaderCheck;    //OSM20250123 : 리더기 헬스체크


    public static int istep;
    public static int slen;
    public static int length_recv;
    public static int status;
    public static int icdataLen = 0;        //OSM20260312 : EMV 가변 저장 변수

    public static long tstart = 0;
    public static long tend = 0;
    public static long tstarttit = 0; //LJY20230911 : TITENG 리더기 연동 시 타임아웃 체크를 위한 start/end
    public static long tendtit = 0;
    public static long supported = 0; //OSM20260312 : EMV 지원가능목록 long형 변수
    public static boolean bTitchk; //LJY20230911 : TITENG 리더기 데이터 처리를 위한 구분 값

    public static boolean isrun;
    public static boolean bRESTART = false; //OSM20250902 : RESTART 플래그 값 추가

    public static boolean bFirst = false;

    public static String cENC_POS;

    public static DBHelper dbHelper;

    public static String portName = null;
    public static Class<?> portClass = null;
    public static Thread thread;
    public static IUart device;

    public static String space = "                                                                                                                                                      "; //20200306 : DCC
    public static String cashic_dttm; //LJY20200327 : 현금IC
    public static String stracctidx; //LJY20200327 : 현금IC
    public static String strpindata; //LJY20200327 : 현금IC
    public static String sNm2000SendData; //LJY20230726

    //LJY20230726
    public static ZOACardReader reader;
    public static String recent_dev_mac, recent_dev_name;

    public static DifferentDisplay presentation; //LJY20221202 : 듀얼 스크린 사용 위한 객체
    private static java.lang.ref.WeakReference<Dialog> sTimerOwnerDialog;       //OSM20260205 : 타이머 Owner 객체 선언 (FALLBACK 시, 전역 타이머 제거되는 이슈 조치)


    //LJY20230911 : TITENG 리더기 연동 시 구분 값
    public static boolean lb_dead; //상태 체크 확인
    public static boolean lb_insert; //카드 인식 여부
    public static boolean lb_sspay = false; //삼성페이 여부
    public static boolean lb_cardin; //카드 완전히 삽입 여부
    public static boolean lb_poweron; //POWERON 여부
    public static boolean m_Exit; //종료여부 여부

    public static String tvaid = "", tvpan = "", tvemv = "", tvtlv = "", tvuplancode = "";

    //OSM20250929 : 팝업타이머 객체 선언 (OSM20251121 : MERGE 완료)
    private static CountDownTimer sPopupTimer;

    public static void Memset() {
        LogDebug(bLogUse, "debugjy", "[NVCAT] Memset");
        if (key_info != null) {
            Arrays.fill(key_info, (char) 0x20);
            Arrays.fill(key_info, (char) 0xFF);
            Arrays.fill(key_info, (char) 0x00);
        }
        if (key_down != null) {
            Arrays.fill(key_down, (char) 0x20);
            Arrays.fill(key_down, (char) 0xFF);
            Arrays.fill(key_down, (char) 0x00);
        }
        if (encdata != null) {
            Arrays.fill(encdata, (char) 0x20);
            Arrays.fill(encdata, (char) 0xFF);
            Arrays.fill(encdata, (char) 0x00);
        }
        if (icdata != null) {
            Arrays.fill(icdata, (char) 0x20);
            Arrays.fill(icdata, (char) 0xFF);
            Arrays.fill(icdata, (char) 0x00);
        }
        if (RECVBuf != null) {
            Arrays.fill(RECVBuf, (char) 0x20);
            Arrays.fill(RECVBuf, (char) 0xFF);
            Arrays.fill(RECVBuf, (char) 0x00);
        }
        if (CSN != null) {
            Arrays.fill(CSN, (char) 0x20);
            Arrays.fill(CSN, (char) 0xFF);
            Arrays.fill(CSN, (char) 0x00);
        }
        if (HWNUM != null) {
            Arrays.fill(HWNUM, (char) 0x20);
            Arrays.fill(HWNUM, (char) 0xFF);
            Arrays.fill(HWNUM, (char) 0x00);
        }
        if (RND_P1 != null) {
            Arrays.fill(RND_P1, (char) 0x20);
            Arrays.fill(RND_P1, (char) 0xFF);
            Arrays.fill(RND_P1, (char) 0x00);
        }
        if (RND_P2 != null) {
            Arrays.fill(RND_P2, (char) 0x20);
            Arrays.fill(RND_P2, (char) 0xFF);
            Arrays.fill(RND_P2, (char) 0x00);
        }
        if (RND_R1 != null) {
            Arrays.fill(RND_R1, (char) 0x20);
            Arrays.fill(RND_R1, (char) 0xFF);
            Arrays.fill(RND_R1, (char) 0x00);
        }
        if (RND_R2 != null) {
            Arrays.fill(RND_R2, (char) 0x20);
            Arrays.fill(RND_R2, (char) 0xFF);
            Arrays.fill(RND_R2, (char) 0x00);
        }
        if (cENC_READER != null) {
            Arrays.fill(cENC_READER, (char) 0x20);
            Arrays.fill(cENC_READER, (char) 0xFF);
            Arrays.fill(cENC_READER, (char) 0x00);
        }
        if (cDEC_READER != null) {
            Arrays.fill(cDEC_READER, (char) 0x20);
            Arrays.fill(cDEC_READER, (char) 0xFF);
            Arrays.fill(cDEC_READER, (char) 0x00);
        }
        if (Bseed12 != null) {
            Arrays.fill(Bseed12, (char) 0x20);
            Arrays.fill(Bseed12, (char) 0xFF);
            Arrays.fill(Bseed12, (char) 0x00);
        }
        if (ENC_TEMP != null) {
            Arrays.fill(ENC_TEMP, (char) 0x20);
            Arrays.fill(ENC_TEMP, (char) 0xFF);
            Arrays.fill(ENC_TEMP, (char) 0x00);
        }
        if (MSK != null) {
            Arrays.fill(MSK, (char) 0x20);
            Arrays.fill(MSK, (char) 0xFF);
            Arrays.fill(MSK, (char) 0x00);
        }
        if (ASK != null) {
            Arrays.fill(ASK, (char) 0x20);
            Arrays.fill(ASK, (char) 0xFF);
            Arrays.fill(ASK, (char) 0x00);
        }
        if (cENC_POS_temp != null) {
            Arrays.fill(cENC_POS_temp, (char) 0x20);
            Arrays.fill(cENC_POS_temp, (char) 0xFF);
            Arrays.fill(cENC_POS_temp, (char) 0x00);
        }
        if (RND_FORM2 != null) {
            Arrays.fill(RND_FORM2, (char) 0x20);
            Arrays.fill(RND_FORM2, (char) 0xFF);
            Arrays.fill(RND_FORM2, (char) 0x00);
        }
        if (ReaderSN != null) {
            Arrays.fill(ReaderSN, (char) 0x20);
            Arrays.fill(ReaderSN, (char) 0xFF);
            Arrays.fill(ReaderSN, (char) 0x00);
        }
        if (Paygb != null) {
            Arrays.fill(Paygb, (char) 0x20);
            Arrays.fill(Paygb, (char) 0xFF);
            Arrays.fill(Paygb, (char) 0x00);
        }
        if (writeBuffer != null) {
            Arrays.fill(writeBuffer, (char) 0x20);
            Arrays.fill(writeBuffer, (char) 0xFF);
            Arrays.fill(writeBuffer, (char) 0x00);
        }
        if (Roundkey != null) {
            Arrays.fill(Roundkey, (int) 0x20);
            Arrays.fill(Roundkey, (int) 0xFF);
            Arrays.fill(Roundkey, (int) 0x00);
        }
        if (sendBuff != null) {
            Arrays.fill(sendBuff, (byte) 0x20);
            Arrays.fill(sendBuff, (byte) 0xFF);
            Arrays.fill(sendBuff, (byte) 0x00);
        }
        if (recvBuff != null) {
            Arrays.fill(recvBuff, (byte) 0x20);
            Arrays.fill(recvBuff, (byte) 0xFF);
            Arrays.fill(recvBuff, (byte) 0x00);
        }
        if (sendBuffBody != null) {
            Arrays.fill(sendBuffBody, (byte) 0x20);
            Arrays.fill(sendBuffBody, (byte) 0xFF);
            Arrays.fill(sendBuffBody, (byte) 0x00);
        }
        if (temp != null) {
            Arrays.fill(temp, (byte) 0x20);
            Arrays.fill(temp, (byte) 0xFF);
            Arrays.fill(temp, (byte) 0x00);
        }
        if (bEncPin != null) {
            Arrays.fill(bEncPin, (byte) 0x20);
            Arrays.fill(bEncPin, (byte) 0xFF);
            Arrays.fill(bEncPin, (byte) 0x00);
        }
        if (handlertemp != null) {
            Arrays.fill(handlertemp, (byte) 0x20);
            Arrays.fill(handlertemp, (byte) 0xFF);
            Arrays.fill(handlertemp, (byte) 0x00);
        }
        if (signBuff != null) {
            Arrays.fill(signBuff, (byte) 0x20);
            Arrays.fill(signBuff, (byte) 0xFF);
            Arrays.fill(signBuff, (byte) 0x00);
        }
        if (DccmsgBuff != null) { //20200312 : DCC 개발
            Arrays.fill(DccmsgBuff, (byte) 0x20);
            Arrays.fill(DccmsgBuff, (byte) 0xFF);
            Arrays.fill(DccmsgBuff, (byte) 0x00);
        }

        System.gc(); //가비지 컬렉션
        //Runtime.getRuntime().gc();
        try { //LJY20250527 : 버퍼 초기화 시간 (임시)
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStatusBar(Dialog dialog) { //OSM20230911 : 결제 팝업 시 하단 네비게이션 바 제거 (다이얼로그)
        View decorView = dialog.getWindow().getDecorView();
        int uiOption = decorView.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(uiOption);
    }

    public static void deleteStatusBar(Window wWindow) { //OSM20230911 : 결제 팝업 시 하단 네비게이션 바 제거 (윈도우)
        View decorView = wWindow.getDecorView();
        int uiOption = decorView.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(uiOption);
    }

    //OSM20250929 : ParseInt 별도 함수 생성 (OSM20251121 : MERGE 완료)
    public static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch(Exception e) { return def; }
    }


    //OSM20260312 : HexCompat 메서드 추가
    public static long strtolHexCompat(char[] src) {
        int i = 0;
        int len = src.length;

        //앞 공백 skip
        while (i < len && Character.isWhitespace(src[i])) {
            i++;
        }

        //optional 0x / 0X
        if (i + 1 < len && src[i] == '0' && (src[i + 1] == 'x' || src[i + 1] == 'X')) {
            i += 2;
        }

        long result = 0;
        boolean found = false;

        //hex parsing (유효한 동안만)
        while (i < len) {
            char c = src[i];
            int digit;

            if (c >= '0' && c <= '9') digit = c - '0';
            else if (c >= 'A' && c <= 'F') digit = c - 'A' + 10;
            else if (c >= 'a' && c <= 'f') digit = c - 'a' + 10;
            else break; //여기서 멈춤 (strtol 핵심)

            result = (result << 4) + digit;
            found = true;
            i++;
        }

        //유효한 숫자 하나도 없으면 0
        return found ? result : 0;
    }

    //OSM20250929 : 타이머 시작/갱신 유틸 함수 (OSM20251121 : MERGE 완료)
    public static void startPopupCountdown(final Dialog dialog, int totalSeconds) {
        // 기존 타이머 정리
        cancelPopupCountdown();

        if (dialog == null || totalSeconds <= 0) return;

        sTimerOwnerDialog = new java.lang.ref.WeakReference<>(dialog);      //OSM20260205 : 타이머 Owner 객체

        final WeakReference<TextView> tvRef = new WeakReference<>((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel));

        // 타이머 텍스트뷰가 없으면 조용히 패스
        final TextView tv = tvRef.get();
        if (tv != null) {
            tv.setVisibility(View.VISIBLE);   // 보장
            //OSM20250929 : 텍스트 색상 검정으로 지정 (OSM20251121 : MERGE 완료)
            tv.setTextColor(Color.parseColor("#000000"));
        }

        sPopupTimer = new CountDownTimer(totalSeconds * 1000L, 1000L) {
            @Override public void onTick(long millisUntilFinished) {
                TextView t = tvRef.get();
                if (t != null) {
                    int sec = (int) Math.ceil(millisUntilFinished / 1000.0);

                    t.setText("남은 시간 : " + sec + "초");
                }
            }

            @Override public void onFinish() {
                TextView t = tvRef.get();
                if (t != null) {
                    t.setText("시간 초과");
                }

                PopupClose();

                //OSM20250929 : 타임아웃일 때 리더기로 EOT 전송 (OSM20251121 : MERGE 완료)
                byte[] EOT = new byte[1];
                EOT[0] = 0x04;
            }
        }.start();

        //이 dialog가 "현재 타이머 소유자"일 때만 cancel 허용 (FALLBACK 타이머 적용 안되는 이슈에 대한 조치)
        dialog.setOnDismissListener(d -> {
            Dialog owner = (sTimerOwnerDialog != null) ? sTimerOwnerDialog.get() : null;
            if (owner == dialog) {
                cancelPopupCountdown();
            }
        });
    }

    public static void cancelPopupCountdown() {
        if (sPopupTimer != null) {
            try {
                sPopupTimer.cancel();
            } catch (Exception ignore) {}

            try { sPopupTimer.cancel(); } catch (Exception ignore) {}
            sPopupTimer = null;
        }
    }


    public static void PopupOpen(Context context, String text) {
        dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_popup_dialog);
        dialog.setCancelable(false);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.bg_dialog);

        deleteStatusBar(dialog); //OSM20230911 : 결제 팝업 시 하단 네비게이션 바 제거

        ((TextView) dialog.findViewById(R.id.tvpopup)).setText(text);
        ((Button) dialog.findViewById(R.id.btrooting)).setVisibility(View.GONE);
        ((ImageView) dialog.findViewById(R.id.iv_card)).setVisibility(View.GONE);
        ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setVisibility(View.GONE);
        ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
        ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setVisibility(View.GONE);
        if (SharedManager.getInstance(context).getPreferences().getBoolean("Msgbox", false) == false) {
            dialog.show();
        }
    }

    public static void PopupOpenEOT(Context context, String text) {
        dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_popup_dialog);
        dialog.setCancelable(false);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.bg_dialog);

        deleteStatusBar(dialog); //OSM20230911 : 결제 팝업 시 하단 네비게이션 바 제거

        ((TextView) dialog.findViewById(R.id.tvpopup)).setText("신용카드 결제");
        ((Button) dialog.findViewById(R.id.btrooting)).setText("요청취소");
        ((Button) dialog.findViewById(R.id.btrooting)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 요청취소 클릭");
                byte[] EOT = new byte[1];
                EOT[0] = 0x04;

                m_Exit = true; //LJY20230911 : TITENG 리더기 요청 취소 시 TRUE

                if(SharedManager.getInstance(context).getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                {
                    scr.sendEot();
                }
                else
                if(SharedManager.getInstance(context).getPreferences().getInt("Readertype", 0) == 2 || SharedManager.getInstance(context).getPreferences().getInt("Readertype", 0) == 7) //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가 //OKPOS
                {
                    //LJY20201005 : OKPOS 서명 / 은련 PIN 연동
                    if(isMultipad || isSign)
                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), EOT, EOT.length);
                    else
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), EOT, EOT.length);
                }
                else {
                    if(func_code != 'S') //LJY20230911 : TITENG 리더기 상태 체크 시 EOT 전송 안하도록 로직 추가
                        usbService.write(EOT);
                }

                if(isMultipad || isSign || func_code == 0xD3) //LJY20201005 : OKPOS 서명 연동 //LJY20200713 : 바코드리딩
                {
                    RECVBuf[0] = 0x04;
                    RECVBuf[4] = 0xCD;
                }

                if(isMultipad || isSign || func_code == 0x71) //OSM20241017 (요청 취소 메시지 팝업)
                {
                    RECVBuf[0] = 0x04;
                    RECVBuf[4] = 0xCD;
                }


                PopupClose();
//                if(isMultipad || isSign) { //LJY20201005 : OKPOS 서명 연동
//                    btnEnable();
//
//                    if(SharedManager.bStart == false) {
//                        isMultipad = false;
//                        //LJY20220427 : 서명패드 요청 후 취소 로직 추가
//                        if(!(mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false)))
//                        isSign = false;
//                    }
//                    isrun = false;
//                    status = 0;
//                    if(context.getClass().getSimpleName().equals("DetailDealResult"))
//                        btnBack.setEnabled(true);
//                }
            }
        });

        //LJY20220516 : 고객식별번호
        if(func_code == 0x45)
        {
            ((TextView) dialog.findViewById(R.id.tvpopup)).setText("서명패드 연동");
            ((ImageView) dialog.findViewById(R.id.iv_card)).setVisibility(View.GONE);
            ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText("고객식별번호 입력 중입니다.");
            ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
            ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setVisibility(View.GONE);
        }
        else
            //LJY20201005 : OKPOS 은련 PIN 연동
            if(isMultipad)
            {
                ((TextView) dialog.findViewById(R.id.tvpopup)).setText("서명패드 연동");
                ((ImageView) dialog.findViewById(R.id.iv_card)).setVisibility(View.GONE);
                ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText("PIN 입력 중입니다.");
                ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
            }
            else
                //LJY20201005 : OKPOS 서명 연동
                if(isSign)
                {
                    ((TextView) dialog.findViewById(R.id.tvpopup)).setText("서명패드 연동");
                    ((ImageView) dialog.findViewById(R.id.iv_card)).setVisibility(View.GONE);
                    ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText("서명 해주세요");
                    ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
                }
                else
                if(func_code == 0xD3) //LJY20200713 : 바코드리딩
                {
                    ((TextView) dialog.findViewById(R.id.tvpopup)).setText("서명패드 연동"); //LJY20221202 : 팝업 타이틀바 추가
                    ((ImageView) dialog.findViewById(R.id.iv_card)).setVisibility(View.GONE);
                    ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText("바코드리딩 해주세요");
                    ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
                }
                else
                if(func_code == 0x6C || func_code == 0x9C || ((mSharedManager.getPreferences().getInt("Readertype", 0) == 5 || mSharedManager.getPreferences().getInt("Readertype", 0) == 6 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) && (func_code == 'A' || func_code == 'R' || func_code == 'S' || func_code == 0xCF))) //LJY20251111 : TTM 추가 //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가 //LJY20250904 : 8BIN/통합결제 적용 //LJY20250904 //LJY20230911 : TITENG 리더기 사용하면서 MS CLEAR/POWERON/상태체크 시
                {
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 5 || mSharedManager.getPreferences().getInt("Readertype", 0) == 6 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20251111 : TTM 추가 //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가 //LJY20230912 : 이미지 추가
                        ((ImageView) dialog.findViewById(R.id.iv_card)).setImageResource(R.drawable.payment_processing_img_iccard_tit);
                    else
                        ((ImageView) dialog.findViewById(R.id.iv_card)).setImageResource(R.drawable.payment_processing_img_iccard);

                    int temp = 0;
                    ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText("신용카드를 그림과 같이\nIC카드 리더기에 꽂아주세요");
                    ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setText("결제가 완료될 때까지\n카드를 빼지 마세요!");
                    ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setText(mSharedManager.getPreferences().getString("Timeout", "30") + "초이내로 결제하지 않으면 자동 취소됩니다"); //LJY20230911 : 설정 타임아웃 시간 팝업 변경

                    ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setVisibility(View.VISIBLE);

                    if(text.equals("CHKCARDIN") || text.equals("CHKCARDBIN")) { //LJY20251117 : CHKCARDBIN API UI 변경 //LJY20250904 : 해당 API 시 문구 안보이게
                        ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
                        ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setVisibility(View.GONE);
                    }
//            ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setVisibility(View.GONE);
                }

                else if(func_code == 0x6E || func_code == 0x9E || ((mSharedManager.getPreferences().getInt("Readertype", 0) == 5 || mSharedManager.getPreferences().getInt("Readertype", 0) == 6 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) && (func_code == 'A' || func_code == 'R' || func_code == 'S' || func_code == 'e' || func_code == 'E'|| func_code == 0xCF))) //LJY20251111 : TTM 추가 //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가 //LJY20250904 : 8BIN/통합결제 적용 //LJY20250904 //LJY20230911 : TITENG 리더기 사용하면서 MS CLEAR/POWERON/상태체크/카드제거시 시 //LJY20230713 : 기타 팝업 추가
                {
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 5 || mSharedManager.getPreferences().getInt("Readertype", 0) == 6 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) { //LJY20251111 : TTM 추가 //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가 //LJY20230912 : 이미지 추가
                        ((ImageView) dialog.findViewById(R.id.iv_card)).setImageResource(R.drawable.payment_processing_img_fallback_tit);
                        ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText("신용카드를 그림과 같이 제거해주세요");
                    } else {
                        ((ImageView) dialog.findViewById(R.id.iv_card)).setImageResource(R.drawable.payment_processing_img_fallback);
                        ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText("신용카드 마그네틱을\n그림과 같이 리더기에 긁어주세요");
                    }
                    ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
                    ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setText(mSharedManager.getPreferences().getString("Timeout", "30") + "초이내로 결제하지 않으면 자동 취소됩니다"); //LJY20230911 : 설정 타임아웃 시간 팝업 변경

//            ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setVisibility(View.GONE);
                }
                else //LJY20230713 : 기타 팝업 추가
                {
                    ((TextView) dialog.findViewById(R.id.tvpopup)).setText("리더기/서명패드 연동");
                    ((ImageView) dialog.findViewById(R.id.iv_card)).setVisibility(View.GONE);
                    ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setText(text);
                    ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
                }

        if (SharedManager.getInstance(context).getPreferences().getBoolean("Msgbox", false) == false)
        {
            //OSM20250929 : 취소요청 리스너 키 등록 (OSM20251121 : MERGE 완료)
            if(mSharedManager.getPreferences().getBoolean("Reqstop", false) == true) {
                dialog.setOnKeyListener((d, keyCode, event) -> {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 요청취소 이벤트 시작");

                    // 저장된 Stopcode 값 불러오기
                    String stopcode = mSharedManager.getPreferences().getString("Stopcode", "25");
                    int stopKeyCode;
                    try {
                        stopKeyCode = Integer.parseInt(stopcode);  // 문자열을 int로 변환
                    } catch (NumberFormatException e) {
                        stopKeyCode = KeyEvent.KEYCODE_ALT_LEFT;   // fallback 기본 ALT
                    }

                    // 실제 KeyEvent와 저장된 stopKeyCode가 같으면 취소
                    if (keyCode == stopKeyCode) {
                        doCancelFromPopup(context);
                        return true;
                    }

                    return false;
                });
            }

            if (SharedManager.getInstance(context).getPreferences().getBoolean("DualScreenuse", false)) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 듀얼(외부) 디스플레이 사용");
                //OSM20260205 : 외부 Display들에 전부 표시
                try {
                    OverlayPopupManager.showOnAllExternalDisplaysByGb(context, 2, text);
                } catch (Exception ignored) {}
            }

            dialog.show();

            //OSM20250929 : Timeout 읽어서 카운트다운 시작 (OSM20251121 : MERGE 완료)
            int timeoutSec = parseIntSafe(SharedManager.getInstance(context).getPreferences().getString("Timeout","30"), 30);

            if(!(isSign || isMultipad) && bNoTimer == false)    //타이머 팝업 적용 예외 케이스
                startPopupCountdown(dialog, timeoutSec);
        }

//        //LJY20221202 : 듀얼 스크린 사용 시 팝업
//        if (SharedManager.getInstance(context).getPreferences().getBoolean("DualScreenuse", false)) {
//            if (presentationDisplays != null && presentationDisplays.length > 0) {
//                for (int i = 0; i < presentationDisplays.length; i++) {
//                    if (presentationDisplays[i].getDisplayId() == 1) {
//                        presentation = (DifferentDisplay) new DifferentDisplay(context, presentationDisplays[presentationDisplays.length - 1], 1, text); //LJY20230131 : 캐스팅 추가
//                        presentation.show();
//                    }
//                }
//            }
//        }
    }

    //OSM20250929 : 리더기 팝업 요청취소 이벤트 (OSM20251121 : MERGE 완료)
    public static void doCancelFromPopup(Context context) {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 요청취소(키/알림/버튼 공통 루틴)");

        byte[] EOT = new byte[]{ 0x04 };
        m_Exit = true; // TITENG 리더기 요청 취소 시 TRUE

        int rtype = SharedManager.getInstance(context).getPreferences().getInt("Readertype", 0);

        if (rtype == 3) { // POSBANK
            scr.sendEot();
        } else if (rtype == 2) { // OKPOS
            // OKPOS 서명 / 은련 PIN 연동
            if (isMultipad || isSign)
                mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), EOT, EOT.length);
            else
                mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), EOT, EOT.length);
        } else {
            if (func_code != 'S') // TITENG 리더기 상태 체크 시 EOT 전송 안 함
                usbService.write(EOT);
        }

        // OKPOS 서명/바코드/요청취소 메시지 팝업 등의 응답 세팅
        if (isMultipad || isSign || func_code == 0xD3) {
            RECVBuf[0] = 0x04;
            RECVBuf[4] = 0xCD;
        }
        if (isMultipad || isSign || func_code == 0x71) {
            RECVBuf[0] = 0x04;
            RECVBuf[4] = 0xCD;
        }

        PopupClose(); // 기존 닫기 루틴 그대로 사용
    }


    //OSM20250929 : 외부 취소 브로드캐스트 리시버 (OSM20251121 : MERGE 완료)
    public static class CancelReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if ("CANCEL_EOT".equals(action)) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                byte[] EOT = new byte[1];
                EOT[0] = 0x04;

                if (SharedManager.getInstance(context).getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    scr.sendEot();
                else if (SharedManager.getInstance(context).getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                {
                    if (isMultipad || isSign)
                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), EOT, EOT.length);
                    else
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), EOT, EOT.length);
                } else
                    usbService.write(EOT);

                SharedManager.LogDebug(true, "debugjy", "[NVCAT] 외부 명령어로 취소됨 (CANCEL_EOT)");
            }
        }
    }
    public static void PopupOpenWithClose(Context context, String text) {
        dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_popup_dialog);

        if (context instanceof Activity) {
            dialog.setOwnerActivity((Activity) context);
        }

        dialog.setCancelable(false);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.bg_dialog);

        deleteStatusBar(dialog); //OSM20230911 : 결제 팝업 시 하단 네비게이션 바 제거

        ((TextView) dialog.findViewById(R.id.tvpopup)).setText(text);
        ((ImageView) dialog.findViewById(R.id.iv_card)).setVisibility(View.GONE);
        ((TextView) dialog.findViewById(R.id.tv_guide_dock_card)).setVisibility(View.GONE);
        ((TextView) dialog.findViewById(R.id.tv_guide_payment)).setVisibility(View.GONE);
        ((TextView) dialog.findViewById(R.id.tv_guide_auto_cancel)).setVisibility(View.GONE);
        ((Button) dialog.findViewById(R.id.btrooting)).setText("닫기");
        ((Button) dialog.findViewById(R.id.btrooting)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupClose(); //LJY20221202 : PopupClose 호출로 변경

                if(isBizdown == false || func_code == 0x31 || func_code == 0xA0 || func_code == 0xA1) { //LJY20221202 : PopupOpenWithClose에 있는 기능을 여기서 호출
                }
            }
        });
        if (SharedManager.getInstance(context).getPreferences().getBoolean("Msgbox", false) == false)
            dialog.show();

//        //LJY20221202 : 듀얼 스크린 사용 시 팝업
//        if (SharedManager.getInstance(context).getPreferences().getBoolean("DualScreenuse", false)) {
//            if (presentationDisplays != null && presentationDisplays.length > 0) {
//                for (int i = 0; i < presentationDisplays.length; i++) {
//                    if (presentationDisplays[i].getDisplayId() == 1) {
//                        presentation = (DifferentDisplay) new DifferentDisplay(context, presentationDisplays[presentationDisplays.length - 1], 3, text);
//                        presentation.show();
//                    }
//                }
//            }
//        }

        //OSM20260205 : 외부 디스플레이 표시
        if (SharedManager.getInstance(context).getPreferences().getBoolean("DualScreenuse", false)) {
            OverlayPopupManager.showOnAllExternalDisplaysByGb(context, 3, "");
        }
    }


    public static void PopupClose() {
        if(!((mSharedManager.getPreferences().getInt("Readertype", 0) == 5 || mSharedManager.getPreferences().getInt("Readertype", 0) == 6 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) && (func_code == 'A' || func_code == 'R' || func_code == 'S' || func_code == 0xCF || ((func_code == 'e' || func_code == 'E') && lb_sspay == true)))) { //LJY20260109 : OKPOS TDR //LJY20251111 : TTM 추가 //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가 //LJY20230911 : TIT리더기 사용하면서 MS CLEAR/POWER ON/상태체크/카드제거 시 Popup Close 되지 않도록 변경

            try { cancelPopupCountdown(); } catch (Exception ignore) {} //OSM20250929 : 팝업 타이머 예외처리 (OSM20251121 : MERGE 완료)
            //OSM20260205 : 외부 디스플레이 오버레이 팝업도 같이 종료
            try { OverlayPopupManager.dismissAll(); } catch (Exception ignore) {}

            if (dialog != null && dialog.isShowing()) {
                try { dialog.setOnDismissListener(null); } catch (Exception ignore) {}
                deleteStatusBar(dialog);
                dialog.dismiss();
            }

            if (presentation != null && presentation.isShowing()) //LJY20221202 : 듀얼 스크린 켜져 있는 경우 팝업 종료
                presentation.dismiss();

            try { bNoTimer = false; } catch (Exception ignore) {}   //OSM20260205 : 팝업 타이머 예외 구분자 초기화

        }
    }


    public static long Get_RandomKey(char[] KSN, int nMAX) { //랜덤값생성
        char[] rndstr = new char[8];

        for (int i = 0; i < nMAX; i++) {
            int ntemp = ((int) (Math.random() * 10) + 1) % 9;
            int ntemp2 = ((int) (Math.random() * 10) + 1) % 2;

            if (ntemp2 == 0) {
                rndstr[i] = (char) (ntemp + 48);
            } else {
                rndstr[i] = (char) (ntemp + 65);
            }
        }

        System.arraycopy(rndstr, 0, KSN, 0, nMAX);

        return 0;
    }

    public static char xor_sum(char[] odata, int len) { //LRC값
        int i;
        char sum;
        for (sum = 0, i = 0; i < len; i++) {
            sum ^= odata[i];
        }
        return sum;
    }

    public static byte xor_sum(byte[] odata, int len) { //LJY20230713 : byte lrc 계산 추가 //LRC값
        int i;
        byte sum;
        for (sum = 0, i = 0; i < len; i++) {
            sum ^= odata[i];
        }
        return sum;
    }

    public static char xor_sum(char[] odata, int start, int len) { //LJY20230911 : LRC값 계산 시 시작 시점 선택 (char)
        int i;
        char sum;
        for (sum = 0, i = start; i < len; i++) {
            sum ^= odata[i];
        }
        return sum;
    }

    public static byte xor_sum(byte[] odata, int start, int len) { //LJY20230911 : LRC값 계산 시 시작 시점 선택 (byte)
        int i;
        byte sum;
        for (sum = 0, i = start; i < len; i++) {
            sum ^= odata[i];
        }
        return sum;
    }

    public static void hexToBinary(int[] bits, int hex) { //LJY20230911 : 헥사를 바이너리로 변경하여 BIT 저장
        for (int i = 0; i < 32; ++i) {
            bits[i] = (hex >> i) & 1;
        }
    }

    public static int calculate_interval_tit(int dwtimer) { //LJY20230911 : TITENG 타임아웃
        long tspan;
        tendtit = System.currentTimeMillis();
        tspan = tendtit - tstarttit;

        if (tspan >= dwtimer * 1000)
            return 1;
        return 0;
    }

    public static String byteArrayToHexString(char[] bytes) { //HEX STRING
        StringBuilder sb = new StringBuilder();
        for (char b : bytes) {
            sb.append(String.format("%02X", b & 0xff));
        }
        return sb.toString();
    }

    public static int calculate_interval(int dwtimer) { //타임아웃
        long tspan;
        tend = System.currentTimeMillis();
        tspan = tend - tstart;

        if (tspan >= dwtimer * 1000)
            return 1;
        return 0;
    }

    public static void initSerial() {
        status = 0;
        istep = 0;
        slen = 0;
        length_recv = 0;
        bTitchk = false; //LJY20230911 : TITENG 리더기 데이터 처리를 위한 구분 값 초기화
    }

    //OSM20260312 : 전문 길이 가변처리 공동 메서드 정의
    public static byte[] buildLen4Packet(String body) {
        try {
            byte[] bodyBytes = body.getBytes("EUC-KR");

            // 전문 길이 4자리 + HASH 16자리 = 20자리 추가
            int packetLen = bodyBytes.length + 20;

            String lenStr = String.format("%04d", packetLen);

            return (lenStr + body).getBytes("EUC-KR");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
