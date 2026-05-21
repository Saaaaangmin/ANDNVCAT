package kr.co.nicevan.androidnvcat;

import static com.posbank.device.common.AscII.CH_ACK;
import static com.posbank.device.common.AscII.CH_NAK;
import static com.posbank.device.common.ReturnValue.RTN_COMM_OK;
import static com.posbank.device.common.ReturnValue.RTN_CONTINUE;
import static kr.co.nicevan.androidnvcat.MainActivity.btnDisable;
import static kr.co.nicevan.androidnvcat.MainActivity.btnEnable;
import static kr.co.nicevan.androidnvcat.MainActivity.on_scan_device;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.ASK;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.Bseed12;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.CSN;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.ENC_TEMP;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.Get_RandomKey;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.HWNUM;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.KeyDownCnt;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.MSK;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpenWithClose;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RECVBuf;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RND_FORM2;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RND_P1;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RND_P2;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RND_R1;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RND_R2;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.ReaderSN;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.Roundkey;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.bTitchk;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.byteArrayToHexString;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cDEC_READER;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cENC_POS;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cENC_POS_temp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cENC_READER;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cReaderBinVer;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cSupportedList;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.calculate_interval;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.dbHelper;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.encdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.icdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.initSerial;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isSign;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isrun;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.istep;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.key_down;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.key_info;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.length_recv;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.mUart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.recvBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.scr;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.sendBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.slen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.status;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.temp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.tstart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.usbService;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.writeBuffer;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.xor_sum;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.SetFinish;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bApkchk;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRelease;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRooting;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.isBizdown;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.isStatus;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bCount;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.iresult;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.posbank.device.common.Utils;
import com.posbank.device.screader.kis.protocol.ScrProtocolCom;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import kr.co.nicevan.androidnvcat.shared.LogSaver;
import kr.co.nicevan.androidnvcat.shared.SharedManager;
import kr.co.nicevan.androidnvcat.shared.seedx;
import kr.co.nicevan.pos.PosClient;
import kr.co.nicevan.pos.PosClientDown;
import okpos.co.kr.payroid.libUart;

/**
 * A simple {@link Fragment} subclass.
 */
public class OneFragment extends Fragment {

    public static Spinner spReadertp;
    public static Spinner spPortnum; //20200108LJY
    public static Spinner spBaudrate;
    public static Spinner spsPortnum; //LJY20201217 : 서명패드 정보 추가
    public static Spinner spsBaudrate; //LJY20201217 : 서명패드 정보 추가
    public static Spinner spPrintertp; //LJY20230726
    public static Spinner spEnctype;   //OSM20250113 : 암복호화 타입 추가
    public static Spinner spModetype;  //OSM20250929 : 모드 타입 추가 (OSM20251121 : MERGE 완료)
    public static Spinner spPostype;   //OSM20250811 : POS유형 추가

    //    public static Spinner spDeviceid; //20200228
    public static CheckBox cbMsgbox;
    public static CheckBox cbRetry;
    public static CheckBox cbNosign;
    public static CheckBox cbNocvm;
    public static CheckBox cbSetport;
    public static CheckBox cbSignuse, cbMinimalwindow, cbDualScreenuse, cbVpnuse, cbPrinteruse, cbBluetoothuse, cbTitejectuse, cbLockdisuse, cbBarriermode, cbToastuse, cbReqstop, cbPayprouse; //LJY20251204 : 통합결제 사용 옵션 처리 //OSM20250929 : 취소 요청 기능, LOCK 비활성화 (OSM20251121 : MERGE 완료) //LJY20231019 : TIT 카드 수동배출 추가 //LJY20230726 //LJY20230111 : 전용회선 사용 체크 박스//LJY20221202 : 듀얼 스크린 사용 체크 박스 //LJY20220427 : 서명 사용 여부 체크 박스
    public static EditText etTimeout, etTimeout2, etTimeout3;
    public static EditText etCatid;
    public static EditText etBizno;
    public static EditText etBizname;
    public static EditText etBizaddr;
    public static EditText etServerip;
    public static EditText etServerport;
    public static EditText etHwnum;
    public static EditText etStopcode;


    public static Button btSetenv;
    Button btGetenv;
    Button btBizDown; //20200110LJY
    public static Button btChkvalid;
    public static Button btInitkeydown;
    Button btUpdatekeydown;
    Button btRefresh;
    Button btBluetoothConnect; //LJY20230726
    ArrayList<String> alreadertp = new ArrayList<>();
    ArrayList<String> alportnum = new ArrayList<>(); //20200108LJY
    ArrayList<String> albaudrate = new ArrayList<>();
    ArrayList<String> alsportnum = new ArrayList<>(); //LJY20201217 : 서명패드 정보 추가
    ArrayList<String> alsbaudrate = new ArrayList<>(); //LJY20201217 : 서명패드 정보 추가
    ArrayList<String> alprintertp = new ArrayList<>(); //LJY20230726
    ArrayList<String> alenctp = new ArrayList<>(); //OSM20250113 : 암복호화 타입 추가
    //   ArrayList<String> almodetp = new ArrayList<>(); //OSM20250929 : 모드 타입 추가 (OSM20251121 : MERGE 완료)

    //   ArrayList<String> alpostp = new ArrayList<>(); //OSM20250811 : POS 유형 추가

    ArrayAdapter spinnerAdapter;
    LinearLayout sLLayout; //LJY20201217 : 서명패드 정보 레이아웃
    private String baudratestr = "";
    private String sbaudratestr = ""; //LJY20201217 : 서명패드 정보 추가


    private String mServerip = "", mServerport = "";
    private SharedManager mSharedManager;

    private boolean bFirst = false;
    private handler_thread handlerThread;

    public OneFragment() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 환경설정 탭입니다.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mSharedManager = SharedManager.getInstance(getActivity());
        View view = inflater.inflate(R.layout.fragment_one, container, false);

        sLLayout = (LinearLayout) view.findViewById(R.id.sllayout); //LJY20201217 : 서명패드 정보 레이아웃

        alreadertp.add("01:일반");
        alreadertp.add("02:멀티패드");
        alreadertp.add("03:OKPOS");
        alreadertp.add("04:POSBANK");
        alreadertp.add("05:JSOFT"); //LJY20220816 : 조은소프트웨어 추가 (시리얼통신시 특정 ID만 사용)
        alreadertp.add("06:TDR/TCP/NKR-1000"); //LJY20230911 : 리더기 타입 추가
        alreadertp.add("07:TTM"); //LJY20251103 : TTM 추가
        alreadertp.add("08:OKPOS_TDR/TCP/NKR-1000"); //LJY20251106 : OKPOS TDR/TCP/NKR-1000 추가

//        alreadertp.add("05:DEVICEID"); //20200228
        spReadertp = (Spinner) view.findViewById(R.id.readertype);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alreadertp);
        spReadertp.setAdapter(spinnerAdapter);
        spReadertp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //20200108LJY
                if(position == 3 || position == 2 || position == 7) //LJY20251111 : OKPOS_TDR/TCP/NKR-1000 사용 시 PORT 설정 UI 추가 //LJY20201217 : OKPOS 추가 //POSBANK시 사용
                {
                    spPortnum.setEnabled(true);
                    sLLayout.setVisibility(View.VISIBLE); //LJY20201217 : 서명패드 정보 레이아웃
                }
                else
                {
                    spPortnum.setEnabled(false);
                    sLLayout.setVisibility(View.GONE); //LJY20201217 : 서명패드 정보 레이아웃
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        alprintertp.add("01:NM2000"); //LJY20230726
        spPrintertp = (Spinner) view.findViewById(R.id.printertype);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alprintertp);
        spPrintertp.setAdapter(spinnerAdapter);
        spPrintertp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //20200108LJY
        alportnum.add("COM1");
        alportnum.add("COM2");
        alportnum.add("COM3");
        alportnum.add("COM4");
        alportnum.add("COM5");
        alportnum.add("COM6");
        alportnum.add("COM7");
        alportnum.add("COM8");
        alportnum.add("COM9");
        spPortnum = (Spinner) view.findViewById(R.id.portnum);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alportnum);
        spPortnum.setAdapter(spinnerAdapter);
        spPortnum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        albaudrate.add("9600");
        albaudrate.add("19200");
        albaudrate.add("38400");
        albaudrate.add("57600");
        albaudrate.add("115200");
        spBaudrate = (Spinner) view.findViewById(R.id.baudrate);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, albaudrate);
        spBaudrate.setAdapter(spinnerAdapter);
        spBaudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //LJY20201217 : 서명패드 정보 추가
        alsportnum.add("COM1");
        alsportnum.add("COM2");
        alsportnum.add("COM3");
        alsportnum.add("COM4");
        alsportnum.add("COM5");
        alsportnum.add("COM6");
        alsportnum.add("COM7");
        alsportnum.add("COM8");
        alsportnum.add("COM9");
        spsPortnum = (Spinner) view.findViewById(R.id.sportnum);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alsportnum);
        spsPortnum.setAdapter(spinnerAdapter);
        spsPortnum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        alsbaudrate.add("9600");
        alsbaudrate.add("19200");
        alsbaudrate.add("38400");
        alsbaudrate.add("57600");
        alsbaudrate.add("115200");
        spsBaudrate = (Spinner) view.findViewById(R.id.sbaudrate);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alsbaudrate);
        spsBaudrate.setAdapter(spinnerAdapter);
        spsBaudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        //OSM20250113 : 암복호화 타입 추가
        alenctp.add("01 : SEED");
        alenctp.add("02 : DES");
        spEnctype = (Spinner) view.findViewById(R.id.enctype);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alenctp);
        spEnctype.setAdapter(spinnerAdapter);
        spEnctype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



//        //OSM20250811 : POS 유형 추가
//        alpostp.add("01 : 일반");
//        alpostp.add("02 : 머니플러스");
//        spPostype = (Spinner) view.findViewById(R.id.postype);
//        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alpostp);
//        spPostype.setAdapter(spinnerAdapter);
//        spPostype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });



//        spDeviceid = (Spinner) view.findViewById(R.id.deviceid);
//        if(mSharedManager.getPreferences().getInt("Readertype", 0) == 4) //DEVICEID
//        {
//            String[] DeviceList = UartUsbOTG.list(getContext());
//            for (int i = 0; i < DeviceList.length; i++) {
//                Log.d("debugjy", "" + i + " : " + DeviceList[i]);
//            }
//
//            Log.d("debugjy", "Device ListView");
//            DeviceListAdapter Adapter = new DeviceListAdapter(getContext(), android.R.layout.simple_list_item_1, DeviceList);
//            ArrayAdapter<String> Adapter = new ArrayAdapter<String>(getContext());
////                ArrayAdapter<String> Adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, DeviceList);
//            spDeviceid.setAdapter(Adapter);
//
//            if (DeviceList != null) {
//                Log.d("debugjy", "DeviceList 있음");
//            } else {
//                Log.d("debugjy", "DeviceList 없음");
//            }
//
//            spDeviceid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//            });
//        }
//        else
//        {
//
//        }

        cbMsgbox = (CheckBox) view.findViewById(R.id.cbmsgbox);
        cbRetry = (CheckBox) view.findViewById(R.id.cbretry);
        cbNosign = (CheckBox) view.findViewById(R.id.cbnosign);
        cbNocvm = (CheckBox) view.findViewById(R.id.cbnocvm);
        //cbSetport = (CheckBox) view.findViewById(R.id.cbsetport);   //OSM20250123 : COM PORT 변경 체크박스 추가
        cbSignuse = (CheckBox) view.findViewById(R.id.cbsignuse); //LJY20220427 : 서명 사용 여부 체크 박스
        cbDualScreenuse = (CheckBox) view.findViewById(R.id.cbdualscreenuse); //LJY20221202 : 듀얼 스크린 사용 체크 박스
        cbMinimalwindow = (CheckBox) view.findViewById(R.id.cbminimalwindow); //OSM20240429 : 창 최소화 체크박스
        cbVpnuse = (CheckBox) view.findViewById(R.id.cbvpnuse); //LJY20230111 : 전용회선 사용 체크 박스
        cbPrinteruse = (CheckBox) view.findViewById(R.id.cbprinteruse); //LJY20230726
        cbPayprouse = (CheckBox) view.findViewById(R.id.cbpayprouse); //LJY20251204 : 통합결제 사용 옵션 처리
        cbPayprouse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 통합결제 사용 체크");
                    Toast.makeText(getContext(), "통합결제 사용합니다.\n신용결제 요청 시 신용+간편결제 사용 가능합니다.", Toast.LENGTH_LONG).show();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 통합결제 사용 체크 해제");
                    Toast.makeText(getContext(), "통합결제 비사용합니다.\n신용결제 요청 시 간편결제 사용은 불가능합니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        cbBluetoothuse = (CheckBox) view.findViewById(R.id.cbbluetoothuse); //LJY20230726
        cbTitejectuse = (CheckBox) view.findViewById(R.id.cbtitejectuse); //LJY20231019 : TIT 카드 수동배출 추가
        cbLockdisuse = (CheckBox) view.findViewById(R.id.cblockdisuse); //LJY20250904 : LOCK 비활성화
        cbReqstop = (CheckBox) view.findViewById(R.id.cbreqstop); //OSM20250929 : 취소 요청 체크 박스 (OSM20251121 : MERGE 완료)
        etTimeout = (EditText) view.findViewById(R.id.ettimeout);
        etTimeout2 = (EditText) view.findViewById(R.id.ettimeout2);
        etTimeout3 = (EditText) view.findViewById(R.id.ettimeout3);
        etCatid = (EditText) view.findViewById(R.id.etcatid);
        etBizno = (EditText) view.findViewById(R.id.etbizno);
        etBizname = (EditText) view.findViewById(R.id.etbizname);
        etBizaddr = (EditText) view.findViewById(R.id.etbizaddr);
        etServerip = (EditText) view.findViewById(R.id.etserverip);
        etServerport = (EditText) view.findViewById(R.id.etserverport);
        etHwnum = (EditText) view.findViewById(R.id.ethwnum);
        etStopcode = (EditText) view.findViewById(R.id.etstopcode); //OSM20250929 : 취소 명령어 에디트박스 (OSM20251121 : MERGE 완료)

        cbMsgbox.setChecked(mSharedManager.getPreferences().getBoolean("Msgbox", false));
        cbRetry.setChecked(mSharedManager.getPreferences().getBoolean("Retry", false));
        cbNocvm.setChecked(mSharedManager.getPreferences().getBoolean("Nocvm", true)); //LJY20220816 : 디폴트 설정
        //cbSetport.setChecked(mSharedManager.getPreferences().getBoolean("Setport", false)); //OSM20250123 : COM PORT 변경 체크박스 추가
        cbNosign.setChecked(mSharedManager.getPreferences().getBoolean("Nosign", false));
        cbSignuse.setChecked(mSharedManager.getPreferences().getBoolean("Signuse", false)); //LJY20220427 : 서명 사용 여부 체크 박스
        cbDualScreenuse.setChecked(mSharedManager.getPreferences().getBoolean("DualScreenuse", false)); //LJY20221202 : 듀얼 스크린 사용 체크 박스
        cbMinimalwindow.setChecked(mSharedManager.getPreferences().getBoolean("Minimalwindow", false)); //OSM20240429 : 창 최소화 체크박스
        cbVpnuse.setChecked(mSharedManager.getPreferences().getBoolean("Vpnuse", false)); //LJY20230111 : 전용회선 사용 체크 박스
        cbPrinteruse.setChecked(mSharedManager.getPreferences().getBoolean("Printeruse", false)); //LJY20230726
        cbPayprouse.setChecked(mSharedManager.getPreferences().getBoolean("Payprouse", true)); //LJY20251204 : 통합결제 사용 옵션 처리
        cbBluetoothuse.setChecked(mSharedManager.getPreferences().getBoolean("Bluetoothuse", false)); //LJY20230726
        cbTitejectuse.setChecked(mSharedManager.getPreferences().getBoolean("Titejectuse", false)); //LJY20231019 : TIT 카드 수동배출 추가
        cbLockdisuse.setChecked(mSharedManager.getPreferences().getBoolean("Lockdisuse", false)); //LJY20250904 : LOCK 비활성화
        cbReqstop.setChecked(mSharedManager.getPreferences().getBoolean("Reqstop", false)); //OSM20250929 : 취소 요청 추가 (OSM20251121 : MERGE 완료)
        spReadertp.setSelection(mSharedManager.getPreferences().getInt("Readertype", 0));
        spPrintertp.setSelection(mSharedManager.getPreferences().getInt("Printertype", 0)); //LJY20230726
        spPortnum.setSelection(mSharedManager.getPreferences().getInt("Portnum", 0)); //20200108LJY
        spBaudrate.setSelection(mSharedManager.getPreferences().getInt("Baudrate", 4)); //LJY20220816 : 0 > 4
        spsPortnum.setSelection(mSharedManager.getPreferences().getInt("sPortnum", 0)); //LJY20201217 : 서명패드 정보 추가
        spEnctype.setSelection(mSharedManager.getPreferences().getInt("Enctype", 0)); //OSM20250113 : 암복호화 타입 추가
//        spModetype.setSelection(mSharedManager.getPreferences().getInt("Modetype", 0)); //OSM20250929 : 모드 타입 추가 (OSM20251121 : MERGE 완료)

//        spPostype.setSelection(mSharedManager.getPreferences().getInt("Postype", 0)); //OSM20250811 : POS 유형 추가

        spsBaudrate.setSelection(mSharedManager.getPreferences().getInt("sBaudrate", 4)); //LJY20220816 : 0 > 4 //LJY20201217 : 서명패드 정보 추가
//        spDeviceid.setSelection(mSharedManager.getPreferences().getInt("Deviceid", 0));
        baudratestr = mSharedManager.getPreferences().getString("BaudrateStr", "115200");
        etTimeout.setText(mSharedManager.getPreferences().getString("Timeout", "30"));
        etTimeout2.setText(mSharedManager.getPreferences().getString("Timeout2", "10"));
        etTimeout3.setText(mSharedManager.getPreferences().getString("Timeout3", "10"));
        etStopcode.setText(mSharedManager.getPreferences().getString("Stopcode", "25"));   //OSM20250929 : 취소 명령어 요청 (OSM20251121 : MERGE 완료)

        etCatid.setText(mSharedManager.getPreferences().getString("Catid", ""));
        etBizno.setText(mSharedManager.getPreferences().getString("Bizno", ""));
        etBizname.setText(mSharedManager.getPreferences().getString("Bizname", ""));
        etBizaddr.setText(mSharedManager.getPreferences().getString("Bizaddr", ""));
        etServerip.setText(mSharedManager.getPreferences().getString("Serverip", "211.33.136.2")); //LJY20220816 : 디폴트 설정
        etServerport.setText(mSharedManager.getPreferences().getString("Serverport", "9701")); //LJY20220816 : 디폴트 설정

        //LJY20230726
        btBluetoothConnect = (Button) view.findViewById(R.id.btnbluetoothconnect);
        btBluetoothConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_scan_device(getActivity());
            }
        });

        btRefresh = (Button) view.findViewById(R.id.btrefresh);
        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 4) //DEVICEID
//                {
//                    String[] DeviceList = UartUsbOTG.list(getContext());
//                    for (int i = 0; i < DeviceList.length; i++) {
//                        Log.d("debugjy", "" + i + " : " + DeviceList[i]);
//                    }
//
//                    Log.d("debugjy", "Device ListView");
//                    DeviceListAdapter Adapter = new DeviceListAdapter(getContext(), android.R.layout.simple_list_item_1, DeviceList);
////                ArrayAdapter<String> Adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, DeviceList);
//                    spDeviceid.setAdapter(Adapter);
//
//                    if (DeviceList != null) {
//                        Log.d("debugjy", "DeviceList 있음");
//                    } else {
//                        Log.d("debugjy", "DeviceList 없음");
//                    }
//
//                    spDeviceid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                        @Override
//                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//                        }
//
//                        @Override
//                        public void onNothingSelected(AdapterView<?> adapterView) {
//
//                        }
//                    });
//                }
//                else
//                {
//
//                }
            }
        });

        btSetenv = (Button) view.findViewById(R.id.btnsetenv);
        btSetenv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mSharedManager.getPreferences().edit().putInt("MainVisibleInt", 1).commit(); //LJY20230731 //LJY20220913 : INT 형으로 변경
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 설정저장 버튼 클릭되었습니다.");
                SharedPreferences.Editor editor = mSharedManager.getPreferences().edit();
                editor.putBoolean("Msgbox", cbMsgbox.isChecked());
                editor.putBoolean("Retry", cbRetry.isChecked());
                editor.putBoolean("Nocvm", cbNocvm.isChecked());
                //editor.putBoolean("Setport", cbSetport.isChecked());    //OSM20250123 : COM PORT 변경 체크박스 추가
                editor.putBoolean("Nosign", cbNosign.isChecked());
                editor.putBoolean("Signuse", cbSignuse.isChecked()); //LJY20220427 : 서명 사용 여부 체크 박스
                editor.putBoolean("DualScreenuse", cbDualScreenuse.isChecked()); //LJY20221202 : 듀얼 스크린 사용 체크 박스
                editor.putBoolean("Minimalwindow", cbMinimalwindow.isChecked()); //OSM20240429 : 창 최소화 사용 체크박스
                editor.putBoolean("Vpnuse", cbVpnuse.isChecked()); //LJY20230111 : 전용회선 사용 체크 박스
                editor.putBoolean("Printeruse", cbPrinteruse.isChecked()); //LJY20230726
                editor.putBoolean("Payprouse", cbPayprouse.isChecked()); //LJY20251204 : 통합결제 사용 옵션 처리
                editor.putBoolean("Bluetoothuse", cbBluetoothuse.isChecked()); //LJY20230726
                editor.putBoolean("Titejectuse", cbTitejectuse.isChecked()); //LJY20231019 : TIT 카드 수동배출 추가
                editor.putBoolean("Lockdisuse", cbLockdisuse.isChecked()); //LJY20250904 : LOCK 비활성화
                editor.putBoolean("Reqstop", cbReqstop.isChecked()); //OSM20250929 : 취소 요청 기능 추가 (OSM20251121 : MERGE 완료)
                editor.putInt("Readertype", spReadertp.getSelectedItemPosition());
                editor.putInt("Printertype", spPrintertp.getSelectedItemPosition()); //LJY20230726
                editor.putInt("Portnum", spPortnum.getSelectedItemPosition()); //20200108LJY
                editor.putInt("Baudrate", spBaudrate.getSelectedItemPosition());
                editor.putInt("Enctype", spEnctype.getSelectedItemPosition());  //OSM20250113 : 암복호화 타입 추가
//                editor.putInt("Modetype", spModetype.getSelectedItemPosition());  //OSM20250929 : 모드 타입 추가 (OSM20251121 : MERGE 완료)

//                editor.putInt("Postype", spPostype.getSelectedItemPosition());  //OSM20250811 : POS 유형 추가

//                editor.putInt("Deviceid", spDeviceid.getSelectedItemPosition());
                editor.putString("BaudrateStr", spBaudrate.getSelectedItem().toString());
                //LJY20201217 : 서명패드 정보 추가
                editor.putInt("sPortnum", spsPortnum.getSelectedItemPosition());
                editor.putInt("sBaudrate", spsBaudrate.getSelectedItemPosition());
                editor.putString("sBaudrateStr", spsBaudrate.getSelectedItem().toString());
                editor.putString("Timeout", etTimeout.getText().toString());
                editor.putString("Timeout2", etTimeout2.getText().toString());
                editor.putString("Timeout3", etTimeout2.getText().toString());
                editor.putString("Stopcode", etStopcode.getText().toString());

                editor.putString("Catid", etCatid.getText().toString());
                editor.putString("Bizno", etBizno.getText().toString());
                editor.putString("Bizname", etBizname.getText().toString());
                editor.putString("Bizaddr", etBizaddr.getText().toString());
                editor.putString("Serverip", etServerip.getText().toString());
                editor.putString("Serverport", etServerport.getText().toString());
                editor.commit();

                //앱 재시작 //20200318 : 변경
                Context currentActivity = getContext();
                Intent intent = new Intent(currentActivity, MainActivity.class);
                Intent restartIntent = Intent.makeRestartActivityTask(intent.getComponent());
                currentActivity.startActivity(restartIntent);
                System.exit(0);
                //                Intent mStartActivity = new Intent(getContext(), MainActivity.class);
//                int mPendingIntentId = 123456;
//                PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager mgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);
//                System.exit(0);
            }
        });

        btGetenv = (Button) view.findViewById(R.id.btngetenv);
        btGetenv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 설정로드 버튼 클릭되었습니다.");
                cbMsgbox.setChecked(mSharedManager.getPreferences().getBoolean("Msgbox", false));
                cbRetry.setChecked(mSharedManager.getPreferences().getBoolean("Retry", false));
                cbNocvm.setChecked(mSharedManager.getPreferences().getBoolean("Nocvm", false));
                //cbSetport.setChecked(mSharedManager.getPreferences().getBoolean("Setport", false)); //OSM20250123 : COM PORT 변경 체크박스 추가
                cbNosign.setChecked(mSharedManager.getPreferences().getBoolean("Nosign", false));
                cbSignuse.setChecked(mSharedManager.getPreferences().getBoolean("Signuse", false)); //LJY20220427 : 서명 사용 여부 체크 박스
                cbDualScreenuse.setChecked(mSharedManager.getPreferences().getBoolean("DualScreenuse", false)); //LJY20221202 : 듀얼 스크린 사용 체크 박스
                cbMinimalwindow.setChecked(mSharedManager.getPreferences().getBoolean("Minimalwindow", false)); //OSM20240429 : 창 최소화 사용 체크박스
                cbVpnuse.setChecked(mSharedManager.getPreferences().getBoolean("Vpnuse", false)); //LJY20230111 : 전용회선 사용 체크 박스
                cbPrinteruse.setChecked(mSharedManager.getPreferences().getBoolean("Printeruse", false)); //LJY20230726
                cbPayprouse.setChecked(mSharedManager.getPreferences().getBoolean("Payprouse", true)); //LJY20251204 : 통합결제 사용 옵션 처리
                cbBluetoothuse.setChecked(mSharedManager.getPreferences().getBoolean("Bluetoothuse", false)); //LJY20230726
                cbTitejectuse.setChecked(mSharedManager.getPreferences().getBoolean("Titejectuse", false)); //LJY20231019 : TIT 카드 수동배출 추가
                cbLockdisuse.setChecked(mSharedManager.getPreferences().getBoolean("Lockdisuse", false)); //LJY20250904 : LOCK 비활성화
                cbReqstop.setChecked(mSharedManager.getPreferences().getBoolean("Reqstop", false)); //OSM20250929 : 취소 요청 기능 추가 (OSM20251121 : MERGE 완료)
                spReadertp.setSelection(mSharedManager.getPreferences().getInt("Readertype", 0));
                spPrintertp.setSelection(mSharedManager.getPreferences().getInt("Printertype", 0)); //LJY20230726
                spPortnum.setSelection(mSharedManager.getPreferences().getInt("Portnum", 0)); //20200108LJY
                spBaudrate.setSelection(mSharedManager.getPreferences().getInt("Baudrate", 0));
                spEnctype.setSelection(mSharedManager.getPreferences().getInt("Enctype", 0));   //OSM20250113 : 암복호화 타입 추가
//                spModetype.setSelection(mSharedManager.getPreferences().getInt("Modetype", 0));   //OSM20250929 : 모드 타입 추가 (OSM20251121 : MERGE 완료)

//                spPostype.setSelection(mSharedManager.getPreferences().getInt("Postype", 0));   //OSM20250811 : POS 유형 추가

//                spDeviceid.setSelection(mSharedManager.getPreferences().getInt("Deviceid", 0));
                baudratestr = mSharedManager.getPreferences().getString("BaudrateStr", "115200");
                //LJY20201217 : 서명패드 정보 추가
                spsPortnum.setSelection(mSharedManager.getPreferences().getInt("sPortnum", 0));
                spsBaudrate.setSelection(mSharedManager.getPreferences().getInt("sBaudrate", 0));
                sbaudratestr = mSharedManager.getPreferences().getString("sBaudrateStr", "115200");
                etTimeout.setText(mSharedManager.getPreferences().getString("Timeout", "30"));
                etTimeout2.setText(mSharedManager.getPreferences().getString("Timeout2", "10"));
                etStopcode.setText(mSharedManager.getPreferences().getString("Stopcode", "25"));       //OSM20250929 : 취소 명령어 연동 추가 (OSM20251121 : MERGE 완료)

                etCatid.setText(mSharedManager.getPreferences().getString("Catid", ""));
                etBizno.setText(mSharedManager.getPreferences().getString("Bizno", ""));
                etBizname.setText(mSharedManager.getPreferences().getString("Bizname", ""));
                etBizaddr.setText(mSharedManager.getPreferences().getString("Bizaddr", ""));
                etServerip.setText(mSharedManager.getPreferences().getString("Serverip", ""));
                etServerport.setText(mSharedManager.getPreferences().getString("Serverport", ""));
            }
        });

        //20200110LJY
        btBizDown = (Button) view.findViewById(R.id.btnbizdown);
        btBizDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 버튼 클릭되었습니다.");

                isBizdown = false;
                byte[] sendBuff = ("0109770G3" + mSharedManager.getPreferences().getString("Bizno", "          ") + mSharedManager.getPreferences().getString("Catid", "          ") + "11002091702027700000000                                       10                                                                    ").getBytes();
                byte[] recvBuff = new byte[512];

                PosClientDown posClient = new PosClientDown();
                recvBuff = posClient.service("211.33.136.9", 9002, sendBuff);
                try {
                    if (new String(recvBuff, "EUC-KR").equals("-1")) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 연결 실패");
                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 연결 실패");
                    } else if (new String(recvBuff, "EUC-KR").equals("-2")) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 전문 송신 실패");
                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 전문 송신 실패");
                    } else if (new String(recvBuff, "EUC-KR").equals("-3")) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 전문 수신 실패");
                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 전문 수신 실패");
                    } else if (new String(recvBuff, "EUC-KR").equals("-4")) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 전문 길이 오류");
                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 전문 길이 오류");
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] recvBuff : " + new String(recvBuff, "EUC-KR"));
                        if (!new String(recvBuff, 29, 4, "EUC-KR").equals("0000")) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 실패 (응답코드 : " + new String(recvBuff, 29, 4, "EUC-KR") + ")");
                            PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n" + new String(recvBuff, 33, 40, "EUC-KR"));
                        }
                        else
                        {
                            mSharedManager.getPreferences().edit().putString("Bizname", new String(recvBuff, 134, 34, "EUC-KR")).commit();
                            mSharedManager.getPreferences().edit().putString("Bizaddr", new String(recvBuff, 208, 50, "EUC-KR")).commit();
                            etBizname.setText(mSharedManager.getPreferences().getString("Bizname", ""));
                            etBizaddr.setText(mSharedManager.getPreferences().getString("Bizaddr", ""));
                            isBizdown = true;
                            PopupOpenWithClose(getContext(), "가맹점다운로드 성공!");
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    btnEnable(); //LJY20221202 : 가맹점다운로드 예외처리
                }
            }
        });

        btChkvalid = (Button) view.findViewById(R.id.btnchkvalid);
        btChkvalid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증및무결성점검 버튼 클릭되었습니다.");


//                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 중입니다."); //20200108LJY
//
//                isBizdown = false;
//                byte[] sendBuff = ("0109770G3" + mSharedManager.getPreferences().getString("Bizno", "          ") + mSharedManager.getPreferences().getString("Catid", "          ") + "11002091702027700000000                                       10                                                                    ").getBytes();
//                byte[] recvBuff = new byte[512];
//
//                PosClientDown posClient = new PosClientDown();
//                recvBuff = posClient.service("211.33.136.9", 9002, sendBuff);
//                try {
//                    if (new String(recvBuff, "EUC-KR").equals("-1")) {
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 연결 실패");
//                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 연결 실패");
//                        return;
//                    } else if (new String(recvBuff, "EUC-KR").equals("-2")) {
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 전문 송신 실패");
//                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 전문 송신 실패");
//                        btnEnable(); //LJY20200923 : 가맹점다운로드 예외처리
//                        return;
//                    } else if (new String(recvBuff, "EUC-KR").equals("-3")) {
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 전문 수신 실패");
//                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 전문 수신 실패");
//                        btnEnable(); //LJY20200923 : 가맹점다운로드 예외처리
//                        return;
//                    } else if (new String(recvBuff, "EUC-KR").equals("-4")) {
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 서버 전문 길이 오류");
//                        PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n서버 전문 길이 오류");
//                        btnEnable(); //LJY20200923 : 가맹점다운로드 예외처리
//                        return;
//                    } else {
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] recvBuff : " + new String(recvBuff, "EUC-KR"));
//                        if (!new String(recvBuff, 29, 4, "EUC-KR").equals("0000")) {
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 실패 (응답코드 : " + new String(recvBuff, 29, 4, "EUC-KR") + ")");
//                            PopupOpenWithClose(getContext(), "가맹점다운로드 실패!\n" + new String(recvBuff, 33, 40, "EUC-KR"));
//                            btnEnable(); //LJY20200923 : 가맹점다운로드 예외처리
//                            return;
//                        }
//                        else
//                        {
//                            mSharedManager.getPreferences().edit().putString("Bizname", new String(recvBuff, 134, 34, "EUC-KR")).commit();
//                            mSharedManager.getPreferences().edit().putString("Bizaddr", new String(recvBuff, 208, 50, "EUC-KR")).commit();
//                            etBizname.setText(mSharedManager.getPreferences().getString("Bizname", ""));
//                            etBizaddr.setText(mSharedManager.getPreferences().getString("Bizaddr", ""));
//                            isBizdown = true;
//                        }
//                    }
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                isBizdown = true; //LJY20230130 : 가맹점 다운로드 로직 삭제

                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Device 정보 가져오는 중입니다."); //20200108LJY

                if(!SharedManager.isBizdown)
                {
                    //LJY20200812 : 가맹점다운로드 예외처리
//                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                    Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                    btnEnable();
//                    return;
                }

//                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 4) //DEVICEID
//                {
//                    bFirst = false;
//                    isrun = true;
//
//                    Arrays.fill(RECVBuf, (char) 0x00);
//                    Arrays.fill(encdata, (char) 0x00);
//                    Arrays.fill(icdata, (char) 0x00);
//
//                    initSerial();
//                    func_code = 0x31;
//                    btnDisable();
//
//                    if(SharedArray.Deviceid_PortOpen(getContext()) != 1)
//                    {
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Deviceid_PortOpen 실패");
//                        Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! - Deviceid_PortOpen 실패", Toast.LENGTH_SHORT).show();
//                        InsertChkvalid("N", "상호인증 실패! Deviceid_PortOpen 실패!");
//                        btnEnable();
//                        return;
//                    }
//
//                    writeBuffer = new char[5];
//                    writeBuffer[0] = 0x02; //Header ID
//                    writeBuffer[1] = func_code; //Command ID
//                    writeBuffer[2] = 0x00;
//                    writeBuffer[3] = 0x00; //Length(2)
//                    writeBuffer[4] = xor_sum(writeBuffer, 4);
//
//                    temp = new byte[5];
//                    for (int i = 0; i < 5; i++) {
//                        temp[i] = (byte) writeBuffer[i];
//                    }
//
//                    if(Send(getContext(), temp) != 1)
//                    {
//                        disconnect(getContext());
//
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Deviceid_Send 실패");
//                        Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! - Deviceid_Send 실패", Toast.LENGTH_SHORT).show();
//                        InsertChkvalid("N", "상호인증 실패! Deviceid_Send 실패!");
//                        btnEnable();
//                        return;
//                    }
//
//                    PopupOpen(getContext(), "Device 정보 가져오는 중입니다.");
//
//                    handlerThread = new handler_thread(handler);
//                    handlerThread.start();
//                }
//                else
                //20200108LJY
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                {
                    bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x31;
                    btnDisable();

                    scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                    // Serial Port Check
                    int readState = scr.checkSerialPortOpened();
                    if (readState != RTN_COMM_OK) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    scr.clearTxBuffer();

                    writeBuffer = new char[5];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x00; //Length(2)
                    writeBuffer[4] = xor_sum(writeBuffer, 4);

                    temp = new byte[5];
                    for (int i = 0; i < 5; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    scr.sendMsg(temp, temp.length);
                    PopupOpen(getContext(), "Device 정보 가져오는 중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                    scr.clearRxBuffer();
                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                {
                    mUart = new libUart();
                    bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x31;
                    btnDisable();

                    writeBuffer = new char[5];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x00; //Length(2)
                    writeBuffer[4] = xor_sum(writeBuffer, 4);

                    temp = new byte[5];
                    for (int i = 0; i < 5; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    PopupOpen(getContext(), "Device 정보 가져오는 중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                    //LJY20201217 : 리더기 포트번호/통신속도 가변
                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                    }
                    //카드리더UART로 IC테스트 명령 전송
                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else {
                    if(!SharedManager.isBizdown)
                    {
                        //LJY20200812 : 가맹점다운로드 예외처리
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                        btnEnable();
//                        return;
                    }
                    if (SharedManager.isStatus == false) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        PopupOpenWithClose(getContext(), "리더기 연결 상태 체크해주시길 바랍니다. 0x31");
                        return;
                    }

                    if (usbService != null) // if UsbService was correctly binded, Send data
                    {
                        bFirst = false;
                        isrun = true;

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0x31;
                        btnDisable();

                        writeBuffer = new char[5];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x00; //Length(2)
                        writeBuffer[4] = xor_sum(writeBuffer, 4);

                        temp = new byte[5];
                        for (int i = 0; i < 5; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpen(getContext(), "Device 정보 가져오는 중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정
                        usbService.write(temp);

                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! - USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! USB 서비스 불가!"); //LJY20250904 : LOCK 비활성화
                        btnEnable();
                        PopupOpenWithClose(getContext(), "상호인증 실패! USB 서비스 불가! 0x31");
                    }
                }
            }
        });

        btInitkeydown = (Button) view.findViewById(R.id.btninitkey);
        btInitkeydown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키다운로드 버튼 클릭되었습니다.");
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키 1단계 다운로드 중입니다."); //20200108LJY

                if(!SharedManager.isBizdown)
                {
                    //LJY20200812 : 가맹점다운로드 예외처리
//                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                    Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                    btnEnable();
//                    return;
                }

                //20200108LJY
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                {
                    bFirst = true;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x6A;
                    btnDisable();

                    scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                    // Serial Port Check
                    int readState = scr.checkSerialPortOpened();
                    if (readState != RTN_COMM_OK) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    scr.clearTxBuffer();

                    writeBuffer = new char[16];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x11; //Length(2)
                    if (mSharedManager.getPreferences().getString("Catid", "").length() != 10) {
                        Toast.makeText(getContext(), "CATID 길이 10자리가 아닙니다.", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이 10자리가 아닙니다.");
                        btnEnable();
                        return;
                    }
                    String sendstr = "1" + mSharedManager.getPreferences().getString("Catid", "");
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 11);
                    writeBuffer[15] = xor_sum(writeBuffer, 15);

                    temp = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    scr.sendMsg(temp, temp.length);
                    PopupOpen(getContext(), "최초키 1단계 다운로드 중입니다.");

                    scr.clearRxBuffer();
                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                {
                    mUart = new libUart();
                    bFirst = true;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x6A;
                    btnDisable();

                    writeBuffer = new char[16];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x11; //Length(2)
                    if (mSharedManager.getPreferences().getString("Catid", "").length() != 10) {
                        Toast.makeText(getContext(), "CATID 길이 10자리가 아닙니다.", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이 10자리가 아닙니다.");
                        btnEnable();
                        return;
                    }
                    String sendstr = "1" + mSharedManager.getPreferences().getString("Catid", "");
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 11);
                    writeBuffer[15] = xor_sum(writeBuffer, 15);

                    temp = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    PopupOpen(getContext(), "최초키 1단계 다운로드 중입니다.");

                    //LJY20201217 : 리더기 포트번호/통신속도 가변
                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                    }
                    //카드리더UART로 IC테스트 명령 전송
                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else {
                    if(!SharedManager.isBizdown)
                    {
                        //LJY20200812 : 가맹점다운로드 예외처리
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                        btnEnable();
//                        return;
                    }
                    if (SharedManager.isStatus == false) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    if (usbService != null) {
                        bFirst = true;
                        isrun = true;

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0x6A;
                        btnDisable();

                        writeBuffer = new char[16];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x11; //Length(2)
                        if (mSharedManager.getPreferences().getString("Catid", "").length() != 10) {
                            Toast.makeText(getContext(), "CATID 길이 10자리가 아닙니다.", Toast.LENGTH_SHORT).show();
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이 10자리가 아닙니다.");
                            btnEnable();
                            return;
                        }
                        String sendstr = "1" + mSharedManager.getPreferences().getString("Catid", "");
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 11);
                        writeBuffer[15] = xor_sum(writeBuffer, 15);

                        temp = new byte[16];
                        for (int i = 0; i < 16; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpen(getContext(), "최초키 1단계 다운로드 중입니다.");
                        usbService.write(temp);
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                    }
                }
            }
        });

        btUpdatekeydown = (Button) view.findViewById(R.id.btnupdatekey);
        btUpdatekeydown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키다운로드 버튼 클릭되었습니다.");
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키 1단계 다운로드 중입니다."); //20200108LJY

                if(!SharedManager.isBizdown)
                {
                    //LJY20200812 : 가맹점다운로드 예외처리
//                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                    Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                    btnEnable();
//                    return;
                }

                //20200108LJY
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                {
                    bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x6A;
                    btnDisable();

                    scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                    // Serial Port Check
                    int readState = scr.checkSerialPortOpened();
                    if (readState != RTN_COMM_OK) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    scr.clearTxBuffer();

                    writeBuffer = new char[16];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x11; //Length(2)
                    if (mSharedManager.getPreferences().getString("Catid", "").length() != 10) {
                        Toast.makeText(getContext(), "CATID 길이 10자리가 아닙니다.", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이 10자리가 아닙니다.");
                        btnEnable();
                        return;
                    }
                    String sendstr = "2" + mSharedManager.getPreferences().getString("Catid", "");
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 11);
                    writeBuffer[15] = xor_sum(writeBuffer, 15);

                    temp = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    scr.sendMsg(temp, temp.length);
                    PopupOpen(getContext(), "갱신키 1단계 다운로드 중입니다.");

                    scr.clearRxBuffer();
                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                {
                    mUart = new libUart();
                    bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x6A;
                    btnDisable();

                    writeBuffer = new char[16];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x11; //Length(2)
                    if (mSharedManager.getPreferences().getString("Catid", "").length() != 10) {
                        Toast.makeText(getContext(), "CATID 길이 10자리가 아닙니다.", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이 10자리가 아닙니다.");
                        btnEnable();
                        return;
                    }
                    String sendstr = "2" + mSharedManager.getPreferences().getString("Catid", "");
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 11);
                    writeBuffer[15] = xor_sum(writeBuffer, 15);

                    temp = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    PopupOpen(getContext(), "갱신키 1단계 다운로드 중입니다.");

                    //LJY20201217 : 리더기 포트번호/통신속도 가변
                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                    }
                    //카드리더UART로 IC테스트 명령 전송
                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                } else {
                    if(!SharedManager.isBizdown)
                    {
                        //LJY20200812 : 가맹점다운로드 예외처리
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                        btnEnable();
//                        return;
                    }
                    if (SharedManager.isStatus == false) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    if (usbService != null) {
                        bFirst = false;
                        isrun = true;

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0x6A;
                        btnDisable();

                        writeBuffer = new char[16];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x11; //Length(2)
                        if (mSharedManager.getPreferences().getString("Catid", "").length() != 10) {
                            Toast.makeText(getContext(), "CATID 길이 10자리가 아닙니다.", Toast.LENGTH_SHORT).show();
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이 10자리가 아닙니다.");
                            btnEnable();
                            return;
                        }
                        String sendstr = "2" + mSharedManager.getPreferences().getString("Catid", "");
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 11);
                        writeBuffer[15] = xor_sum(writeBuffer, 15);

                        temp = new byte[16];
                        for (int i = 0; i < 16; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpen(getContext(), "갱신키 1단계 다운로드 중입니다.");
                        usbService.write(temp);
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                    }
                }
            }
        });

        return view;
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isrun = false;
            PopupClose();

            if (status == 2) {
                //20200108
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 시리얼 통신 타임아웃"); //20200108LJY
                Toast.makeText(getContext(), "시리얼 통신 타임아웃", Toast.LENGTH_SHORT).show();
                btnEnable();

                if (func_code == 0x31 || func_code == 0xA0 || func_code == 0xA1) {
                    ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 및 무결성점검 실패! - 시리얼 통신 타임아웃"); //LJY20250904 : LOCK 비활성화
                    PopupOpenWithClose(getContext(), "상호인증 및 무결성점검 실패! - 시리얼 통신 타임아웃");
                }

                //OSM20250902 : 타임아웃일 때 리더기로 EOT 전송
                byte[] EOT = new byte[1];
                EOT[0] = 0x04;

                if (SharedManager.getInstance(getContext()).getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    scr.sendEot();
                else if (SharedManager.getInstance(getContext()).getPreferences().getInt("Readertype", 0) == 2 || SharedManager.getInstance(getContext()).getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                {
                    if (isMultipad || isSign)
                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), EOT, EOT.length);
                    else
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), EOT, EOT.length);
                } else
                    usbService.write(EOT);

                //LJY20201217 : NVCAT 종료 중에도 호출 되도록 로직 변경
                isMultipad = false;
                isSign = false;
                return;
            }
            status = 0;


            if (bRelease == false) //20200108LJY : 디버깅로그
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 시리얼데이터 : [" + new String(RECVBuf) + "]");

            if (func_code == 0x6B) {
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0x6B 에러코드 : " + errcode);

                if (errcode.equals("00")) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 키다운로드 성공");
                    Toast.makeText(getContext(), "키다운로드 성공", Toast.LENGTH_SHORT).show();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 키다운로드 실패");
                    Toast.makeText(getContext(), "키다운로드 실패", Toast.LENGTH_SHORT).show();
                }
                btnEnable();
                return;
            } else if (func_code == 0x6A) {
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0x6A 에러코드 : " + errcode);

                if (errcode.equals("00")) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 키다운로드 1단계 성공");
                    Toast.makeText(getContext(), "키다운로드 1단계 성공", Toast.LENGTH_SHORT).show();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 키다운로드 1단계 실패");
                    Toast.makeText(getContext(), "키다운로드 1단계 실패", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }

                System.arraycopy(RECVBuf, 15, key_info, 0, 64); //key_info에 리더기 키정보 저장
                System.arraycopy(RECVBuf, 39, KeyDownCnt, 0, 2); //LJY20200918 : 키다운로드 카운트

                Arrays.fill(RECVBuf, (char) 0x00);
                Arrays.fill(encdata, (char) 0x00);
                Arrays.fill(icdata, (char) 0x00);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(writeBuffer[4] == '1') { //LJY20200918 : 키다운로드 카운트
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("최초키 다운로드 진행하시겠습니까?\n현재 키다운로드 횟수는 " + new String(KeyDownCnt) + "/19");
                    builder.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                                    mServerip = mSharedManager.getPreferences().getString("Serverip", "");
                                    if (mServerip.length() == 0 || mServerip.length() > 16) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 IP가 잘못 되었습니다.");
                                        Toast.makeText(getContext(), "서버 IP가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }

                                    mServerport = mSharedManager.getPreferences().getString("Serverport", "");
                                    if (mServerport.length() == 0 || mServerport.length() > 6) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 PORT가 잘못 되었습니다.");
                                        Toast.makeText(getContext(), "서버 PORT가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }

                                    PosClient posClient = new PosClient();

                                    if (bFirst == true)
                                        sendBuff = ("0256HPS" + mSharedManager.getPreferences().getString("Catid", "") + strDate + "0200KWH1          " + mSharedManager.getPreferences().getString("Catid", "") + new String(key_info) + "                                                                                                                                       ").getBytes();
                                    else
                                        sendBuff = ("0256HPS" + mSharedManager.getPreferences().getString("Catid", "") + strDate + "0200KXH1          " + mSharedManager.getPreferences().getString("Catid", "") + new String(key_info) + "                                                                                                                                       ").getBytes();
                                    if (bRelease)
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 55) + "*******************************************************************************************************************************");
                                    else
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                                    if(mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                                    }
                                    else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                                    }
                                    try {
                                        if (new String(recvBuff, "EUC-KR").equals("-1") || iresult == -1) {
                                            Toast.makeText(getContext(), "-1:서버연결실패", Toast.LENGTH_SHORT).show();
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -1:서버연결실패");
                                            btnEnable();
                                            return;
                                        } else if (new String(recvBuff, "EUC-KR").equals("-2") || iresult == -2) {
                                            Toast.makeText(getContext(), "-2:서버 전문 송신 실패", Toast.LENGTH_SHORT).show();
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -2:서버 전문 송신 실패");
                                            btnEnable();
                                            return;
                                        } else if (new String(recvBuff, "EUC-KR").equals("-3") || iresult == -3) {
                                            Toast.makeText(getContext(), "-3:서버 전문 수신 실패", Toast.LENGTH_SHORT).show();
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -3:서버 전문 수신 실패");
                                            btnEnable();
                                            return;
                                        } else if (new String(recvBuff, "EUC-KR").equals("-4") || iresult == -4) {
                                            Toast.makeText(getContext(), "-4:서버 키교환 실패", Toast.LENGTH_SHORT).show();
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -4:서버 키교환 실패");
                                            btnEnable();
                                            return;
                                        } else if (new String(recvBuff, "EUC-KR").equals("-5") || iresult == -5) {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -5:서버 전문 암복호화 실패");
                                            Toast.makeText(getContext(), "-5:서버 전문 암복호화 실패", Toast.LENGTH_SHORT).show();
                                            btnEnable();
                                            return;
                                        } else {
                                            if (bRelease)
                                                SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + (new String(recvBuff, "EUC-KR")).substring(0, 59) + "**************************************************");
                                            else
                                                SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + new String(recvBuff, "EUC-KR"));

                                            for (int k = 0; k < 287; k++) {
                                                key_down[k] = (char) recvBuff[k + 59];
                                            }
                                        }
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 키다운로드 2단계 중입니다.");
                                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                                    {
                                        //bFirst = false;
                                        isrun = true;

                                        Arrays.fill(RECVBuf, (char) 0x00);
                                        Arrays.fill(encdata, (char) 0x00);
                                        Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        func_code = 0x6B;
                                        btnDisable();

                                        scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0) + 1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                                        // Serial Port Check
                                        int readState = scr.checkSerialPortOpened();
                                        if (readState != RTN_COMM_OK) {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                            btnEnable();
                                            return;
                                        }

                                        scr.clearTxBuffer();

                                        writeBuffer = new char[305];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = func_code; //Command ID
                                        writeBuffer[2] = 0x03;
                                        writeBuffer[3] = 0x00; //Length(2)
                                        String sendstr = "";
                                        if (bFirst == true)
                                            sendstr = "102" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                                        else
                                            sendstr = "202" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 300);
                                        writeBuffer[304] = xor_sum(writeBuffer, 304);

                                        temp = new byte[305];
                                        for (int i = 0; i < 305; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        scr.sendMsg(temp, temp.length);
                                        if (bFirst == true) {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키다운로드 2단계 중입니다.");
                                            PopupOpen(getContext(), "최초키다운로드 2단계 중입니다.");
                                        } else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키다운로드 2단계 중입니다.");
                                            PopupOpen(getContext(), "갱신키다운로드 2단계 중입니다.");
                                        }

                                        scr.clearRxBuffer();
                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();
                                    } else if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                                    {
                                        mUart = new libUart();
                                        //bFirst = false;
                                        isrun = true;

                                        Arrays.fill(RECVBuf, (char) 0x00);
                                        Arrays.fill(encdata, (char) 0x00);
                                        Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        func_code = 0x6B;
                                        btnDisable();

                                        writeBuffer = new char[305];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = func_code; //Command ID
                                        writeBuffer[2] = 0x03;
                                        writeBuffer[3] = 0x00; //Length(2)
                                        String sendstr = "";
                                        if (bFirst == true)
                                            sendstr = "102" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                                        else
                                            sendstr = "202" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 300);
                                        writeBuffer[304] = xor_sum(writeBuffer, 304);

                                        temp = new byte[305];
                                        for (int i = 0; i < 305; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        if (bFirst == true) {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키다운로드 2단계 중입니다.");
                                            PopupOpen(getContext(), "최초키다운로드 2단계 중입니다.");
                                        } else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키다운로드 2단계 중입니다.");
                                            PopupOpen(getContext(), "갱신키다운로드 2단계 중입니다.");
                                        }

                                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                                        mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                                            mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                                        }
                                        //카드리더UART로 IC테스트 명령 전송
                                        mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();
                                    } else {
                                        if (!SharedManager.isBizdown) {
                                            //LJY20200812 : 가맹점다운로드 예외처리
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                        btnEnable();
//                        return;
                                        }
                                        if (SharedManager.isStatus == false) {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                            btnEnable();
                                            return;
                                        }

                                        if (usbService != null) {
                                            //bFirst = false;
                                            isrun = true;

                                            handlerThread = new handler_thread(handler);
                                            handlerThread.start();

                                            Arrays.fill(RECVBuf, (char) 0x00);
                                            Arrays.fill(encdata, (char) 0x00);
                                            Arrays.fill(icdata, (char) 0x00);

                                            initSerial();
                                            func_code = 0x6B;
                                            btnDisable();

                                            writeBuffer = new char[305];
                                            writeBuffer[0] = 0x02; //Header ID
                                            writeBuffer[1] = func_code; //Command ID
                                            writeBuffer[2] = 0x03;
                                            writeBuffer[3] = 0x00; //Length(2)
                                            String sendstr = "";
                                            if (bFirst == true)
                                                sendstr = "102" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                                            else
                                                sendstr = "202" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 300);
                                            writeBuffer[304] = xor_sum(writeBuffer, 304);

                                            temp = new byte[305];
                                            for (int i = 0; i < 305; i++) {
                                                temp[i] = (byte) writeBuffer[i];
                                            }
                                            if (bFirst == true) {
                                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키다운로드 2단계 중입니다.");
                                                PopupOpen(getContext(), "최초키다운로드 2단계 중입니다.");
                                            } else {
                                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키다운로드 2단계 중입니다.");
                                                PopupOpen(getContext(), "갱신키다운로드 2단계 중입니다.");
                                            }
                                            usbService.write(temp);
                                        } else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                            Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                                            btnEnable();
                                            return;
                                        }
                                    }
                                }
                            });
                    builder.setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Arrays.fill(key_info, (char) 0x00);

                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키 다운로드 사용자 취소");
                                    Toast.makeText(getContext(), "최초키 다운로드 사용자 취소", Toast.LENGTH_SHORT).show();
                                    btnEnable();
                                    return;
                                }
                            });
                    builder.show();

                    return ;
                }




                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                mServerip = mSharedManager.getPreferences().getString("Serverip", "");
                if (mServerip.length() == 0 || mServerip.length() > 16) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 IP가 잘못 되었습니다.");
                    Toast.makeText(getContext(), "서버 IP가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }

                mServerport = mSharedManager.getPreferences().getString("Serverport", "");
                if (mServerport.length() == 0 || mServerport.length() > 6) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 PORT가 잘못 되었습니다.");
                    Toast.makeText(getContext(), "서버 PORT가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }

                PosClient posClient = new PosClient();
                if (bFirst == true)
                    sendBuff = ("0256HPS" + mSharedManager.getPreferences().getString("Catid", "") + strDate + "0200KWH1          " + mSharedManager.getPreferences().getString("Catid", "") + new String(key_info) + "                                                                                                                                       ").getBytes();
                else
                    sendBuff = ("0256HPS" + mSharedManager.getPreferences().getString("Catid", "") + strDate + "0200KXH1          " + mSharedManager.getPreferences().getString("Catid", "") + new String(key_info) + "                                                                                                                                       ").getBytes();
                if (bRelease)
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 55) + "*******************************************************************************************************************************");
                else
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                if(mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                    System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                    recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                    recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                }
                else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                    iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                }  try {
                    if (new String(recvBuff, "EUC-KR").equals("-1") || iresult == -1) {
                        Toast.makeText(getContext(), "-1:서버연결실패", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -1:서버연결실패");
                        btnEnable();
                        return;
                    } else if (new String(recvBuff, "EUC-KR").equals("-2") || iresult == -2) {
                        Toast.makeText(getContext(), "-2:서버 전문 송신 실패", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -2:서버 전문 송신 실패");
                        btnEnable();
                        return;
                    } else if (new String(recvBuff, "EUC-KR").equals("-3") || iresult == -3) {
                        Toast.makeText(getContext(), "-3:서버 전문 수신 실패", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -3:서버 전문 수신 실패");
                        btnEnable();
                        return;
                    } else if (new String(recvBuff, "EUC-KR").equals("-4") || iresult == -4) {
                        Toast.makeText(getContext(), "-4:서버 키교환 실패", Toast.LENGTH_SHORT).show();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -4:서버 키교환 실패");
                        btnEnable();
                        return;
                    } else if (new String(recvBuff, "EUC-KR").equals("-5") || iresult == -5) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -5:서버 전문 암복호화 실패");
                        Toast.makeText(getContext(), "-5:서버 전문 암복호화 실패", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    } else {
                        if (bRelease)
                            SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + (new String(recvBuff, "EUC-KR")).substring(0, 59) + "**************************************************");
                        else
                            SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + new String(recvBuff, "EUC-KR"));

                        for (int k = 0; k < 287; k++) {
                            key_down[k] = (char) recvBuff[k + 59];
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 키다운로드 2단계 중입니다.");
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                {
                    //bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x6B;
                    btnDisable();

                    scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                    // Serial Port Check
                    int readState = scr.checkSerialPortOpened();
                    if (readState != RTN_COMM_OK) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    scr.clearTxBuffer();

                    writeBuffer = new char[305];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x03;
                    writeBuffer[3] = 0x00; //Length(2)
                    String sendstr = "";
                    if (bFirst == true)
                        sendstr = "102" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                    else
                        sendstr = "202" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 300);
                    writeBuffer[304] = xor_sum(writeBuffer, 304);

                    temp = new byte[305];
                    for (int i = 0; i < 305; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    scr.sendMsg(temp, temp.length);
                    if (bFirst == true) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키다운로드 2단계 중입니다.");
                        PopupOpen(getContext(), "최초키다운로드 2단계 중입니다.");
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키다운로드 2단계 중입니다.");
                        PopupOpen(getContext(), "갱신키다운로드 2단계 중입니다.");
                    }

                    scr.clearRxBuffer();
                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                {
                    mUart = new libUart();
                    //bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0x6B;
                    btnDisable();

                    writeBuffer = new char[305];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x03;
                    writeBuffer[3] = 0x00; //Length(2)
                    String sendstr = "";
                    if (bFirst == true)
                        sendstr = "102" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                    else
                        sendstr = "202" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 300);
                    writeBuffer[304] = xor_sum(writeBuffer, 304);

                    temp = new byte[305];
                    for (int i = 0; i < 305; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    if (bFirst == true) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키다운로드 2단계 중입니다.");
                        PopupOpen(getContext(), "최초키다운로드 2단계 중입니다.");
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키다운로드 2단계 중입니다.");
                        PopupOpen(getContext(), "갱신키다운로드 2단계 중입니다.");
                    }

                    //LJY20201217 : 리더기 포트번호/통신속도 가변
                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                    }
                    //카드리더UART로 IC테스트 명령 전송
                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                } else {
                    if(!SharedManager.isBizdown)
                    {
                        //LJY20200812 : 가맹점다운로드 예외처리
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                        btnEnable();
//                        return;
                    }
                    if (SharedManager.isStatus == false) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    if (usbService != null) {
                        //bFirst = false;
                        isrun = true;

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0x6B;
                        btnDisable();

                        writeBuffer = new char[305];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x03;
                        writeBuffer[3] = 0x00; //Length(2)
                        String sendstr = "";
                        if (bFirst == true)
                            sendstr = "102" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                        else
                            sendstr = "202" + mSharedManager.getPreferences().getString("Catid", "") + new String(key_down);
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 300);
                        writeBuffer[304] = xor_sum(writeBuffer, 304);

                        temp = new byte[305];
                        for (int i = 0; i < 305; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        if (bFirst == true) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 최초키다운로드 2단계 중입니다.");
                            PopupOpen(getContext(), "최초키다운로드 2단계 중입니다.");
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 갱신키다운로드 2단계 중입니다.");
                            PopupOpen(getContext(), "갱신키다운로드 2단계 중입니다.");
                        }
                        usbService.write(temp);
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return ;
                    }
                }
            } else if (func_code == 0xA1) {
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0xA1 에러코드 : " + errcode);

                if (!errcode.equals("00")) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증 실패! 에러코드 : " + errcode);
                    Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! 에러코드 : " + errcode, Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! 에러코드 : " + errcode); //LJY20250904 : LOCK 비활성화
//                    mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화
                    btnEnable();
                    PopupOpenWithClose(getContext(), "상호인증 실패! 에러코드 : " + errcode + "0xA1");
                    return;
                }

                if (RECVBuf[5] == 'O' || (RECVBuf[5] == 0x00 && RECVBuf[6] == 0x4F)) {
//                    mSharedManager.getPreferences().edit().putInt("MainVisibleInt", 2).commit(); //LJY20230731 //LJY20220913 : INT 형으로 변경

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증 및 무결성 점검 성공!");
                    Toast.makeText(getContext(), "상호인증 및 무결성 점검 성공!", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).InsertChkvalid("Y", "상호인증 및 무결성 점검 성공!"); //LJY20250904 : LOCK 비활성화
                    getActivity().moveTaskToBack(true); //액티비티 최소화
                    //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화
                    btnEnable();
                    return;
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 무결성 점검 실패!");
                    Toast.makeText(getContext(), "상호인증 및 무결성점검 실패!", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).InsertChkvalid("N", "무결성 점검 실패!"); //LJY20250904 : LOCK 비활성화
                    //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화
                    btnEnable();
                    PopupOpenWithClose(getContext(), "무결성 점검 실패! 0xA1");
                    return;
                }
            } else if (func_code == 0xA0) {
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0xA0 에러코드 : " + errcode);

                if (!errcode.equals("00")) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증 실패! 에러코드 : " + errcode);
                    Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! 에러코드 : " + errcode, Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! 에러코드 : " + errcode); //LJY20250904 : LOCK 비활성화
                    //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화

                    btnEnable();
                    PopupOpenWithClose(getContext(), "상호인증 실패! 에러코드 : " + errcode + "0xA0");
                    return;
                }

                if (RECVBuf[5] == 'F' && RECVBuf[6] == '2') { //상호인증 2단계 완료 후
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0xA0 F2 완료");

                    int k;
                    for (k = 0; k < 8 + 32; k++) { //리더기랜덤값2 저장, 리더기암호화랜덤값 저장
                        if (k < 8)
                            RND_R2[k] = RECVBuf[k + 7];
                        else
                            cENC_READER[k - 8] = RECVBuf[k + 7];
                    }

                    for (k = 0; k < 4; k++)
                        Bseed12[k] = (char) RND_R2[k];
                    for (k = 4; k < 8; k++)
                        Bseed12[k] = (char) RND_P2[k];
                    for (k = 8; k < 12; k++)
                        Bseed12[k] = (char) RND_R2[k - 4];
                    for (k = 12; k < 16; k++)
                        Bseed12[k] = (char) RND_P2[k - 12];

                    seedx.SeedEncRoundKey(Roundkey, MSK); //MSK로 ROUNDKEY 생성
                    seedx.encrypt(Bseed12, ASK, 16, Roundkey); //ASK 생성

                    Arrays.fill(Roundkey, 0x00);

                    int ii = 0, jj = 0;
                    char hexi = 0;
                    char[] str = new char[2];

                    for (ii = 0, jj = 0; ii < 32; ) {
                        str[0] = (char) cENC_READER[ii];
                        str[1] = (char) cENC_READER[ii + 1];

                        switch (str[0]) {
                            case '0':
                                hexi = 0;
                                break;
                            case '1':
                                hexi = 16;
                                break;
                            case '2':
                                hexi = 32;
                                break;
                            case '3':
                                hexi = 48;
                                break;
                            case '4':
                                hexi = 64;
                                break;
                            case '5':
                                hexi = 80;
                                break;
                            case '6':
                                hexi = 96;
                                break;
                            case '7':
                                hexi = 112;
                                break;
                            case '8':
                                hexi = 128;
                                break;
                            case '9':
                                hexi = 144;
                                break;
                            case 'A':
                            case 'a':
                                hexi = 160;
                                break;
                            case 'B':
                            case 'b':
                                hexi = 176;
                                break;
                            case 'C':
                            case 'c':
                                hexi = 192;
                                break;
                            case 'D':
                            case 'd':
                                hexi = 208;
                                break;
                            case 'E':
                            case 'e':
                                hexi = 224;
                                break;
                            case 'F':
                            case 'f':
                                hexi = 240;
                                break;
                        }

                        switch (str[1]) {
                            case '0':
                                hexi += 0;
                                break;
                            case '1':
                                hexi += 1;
                                break;
                            case '2':
                                hexi += 2;
                                break;
                            case '3':
                                hexi += 3;
                                break;
                            case '4':
                                hexi += 4;
                                break;
                            case '5':
                                hexi += 5;
                                break;
                            case '6':
                                hexi += 6;
                                break;
                            case '7':
                                hexi += 7;
                                break;
                            case '8':
                                hexi += 8;
                                break;
                            case '9':
                                hexi += 9;
                                break;
                            case 'A':
                            case 'a':
                                hexi += 10;
                                break;
                            case 'B':
                            case 'b':
                                hexi += 11;
                                break;
                            case 'C':
                            case 'c':
                                hexi += 12;
                                break;
                            case 'D':
                            case 'd':
                                hexi += 13;
                                break;
                            case 'E':
                            case 'e':
                                hexi += 14;
                                break;
                            case 'F':
                            case 'f':
                                hexi += 15;
                                break;
                        }
                        ENC_TEMP[jj++] = hexi;
                        ii += 2;

                        Arrays.fill(str, (char) 0x00);
                    }

                    seedx.SeedEncRoundKey(Roundkey, ASK); //ASK로 RoundKey 생성
                    seedx.decrypt(ENC_TEMP, cDEC_READER, 16, Roundkey);

                    Arrays.fill(Roundkey, 0x00);

                    try {
                        String t1 = new String(cDEC_READER, 0, 16);
                        String t2 = new String(RND_R2, 0, 8) + new String(RND_P2, 0, 8);

                        if (t1.equals(t2)) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 랜덤값 같습니다.");

                            System.arraycopy(RND_P2, 0, RND_FORM2, 0, 4);
                            System.arraycopy(RND_R2, 4, RND_FORM2, 4, 4);
                            System.arraycopy(RND_P2, 4, RND_FORM2, 8, 4);
                            System.arraycopy(RND_R2, 0, RND_FORM2, 12, 4);

                            seedx.SeedEncRoundKey(Roundkey, ASK);
                            seedx.encrypt(RND_FORM2, cENC_POS_temp, 16, Roundkey);

                            Arrays.fill(Roundkey, 0x20);
                            Arrays.fill(Roundkey, 0xFF);
                            Arrays.fill(Roundkey, 0x00);

                            cENC_POS = byteArrayToHexString(cENC_POS_temp);
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증 실패! 리더기 랜덤값 다릅니다.");
                            Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! 리더기 랜덤값 다릅니다.", Toast.LENGTH_SHORT).show();
                            ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! 리더기 랜덤값 다릅니다."); //LJY20250904 : LOCK 비활성화
                            btnEnable();
                            PopupOpenWithClose(getContext(), "상호인증 실패! 리더기 랜덤값 다릅니다. 0xA0 F3");
                            return;
                        }
                    } catch (Exception e) {

                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증 3단계 진행중입니다."); //20200108LJY

                    //20200108LJY
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    {
                        bFirst = false;
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0xA0;
                        btnDisable();

                        scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                        // Serial Port Check
                        int readState = scr.checkSerialPortOpened();
                        if (readState != RTN_COMM_OK) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            return;
                        }

                        scr.clearTxBuffer();

                        writeBuffer = new char[39];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x34; //Length(2)
                        String sendstr = "F3" + cENC_POS;
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 34);
                        writeBuffer[38] = xor_sum(writeBuffer, 38);

                        temp = new byte[39];
                        for (ii = 0; ii < 39; ii++) {
                            temp[ii] = (byte) writeBuffer[ii];
                        }
                        scr.sendMsg(temp, temp.length);
                        PopupOpen(getContext(), "상호인증 3단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                        scr.clearRxBuffer();
                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                    {
                        mUart = new libUart();
                        bFirst = false;
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0xA0;
                        btnDisable();

                        writeBuffer = new char[39];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x34; //Length(2)
                        String sendstr = "F3" + cENC_POS;
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 34);
                        writeBuffer[38] = xor_sum(writeBuffer, 38);

                        temp = new byte[39];
                        for (ii = 0; ii < 39; ii++) {
                            temp[ii] = (byte) writeBuffer[ii];
                        }
                        PopupOpen(getContext(), "상호인증 3단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                        }
                        //카드리더UART로 IC테스트 명령 전송
                        mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else {
                        if(!SharedManager.isBizdown)
                        {
                            //LJY20200812 : 가맹점다운로드 예외처리
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                            Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                            btnEnable();
//                            return;
                        }
                        if (SharedManager.isStatus == false) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            PopupOpenWithClose(getContext(), "리더기 연결 상태 체크해주시길 바랍니다. 0xA0 F3");
                            return;
                        }

                        if (usbService != null) {
                            bFirst = false;
                            isrun = true;

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();

                            Arrays.fill(RECVBuf, (char) 0x00);
                            Arrays.fill(encdata, (char) 0x00);
                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            func_code = 0xA0;
                            btnDisable();

                            writeBuffer = new char[39];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = func_code; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x34; //Length(2)
                            String sendstr = "F3" + cENC_POS;
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 34);
                            writeBuffer[38] = xor_sum(writeBuffer, 38);

                            temp = new byte[39];
                            for (ii = 0; ii < 39; ii++) {
                                temp[ii] = (byte) writeBuffer[ii];
                            }
                            PopupOpen(getContext(), "상호인증 3단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정
                            usbService.write(temp);
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! - USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                            ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! USB 서비스 불가!"); //LJY20250904 : LOCK 비활성화
                            //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화
                            btnEnable();
                            PopupOpenWithClose(getContext(), "상호인증 실패! USB 서비스 불가! 0xA0 F3");
                            return;
                        }
                    }
                } else if (RECVBuf[5] == 'F' && RECVBuf[6] == '1') {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0xA0 F1 완료");

                    int k;
                    for (k = 0; k < 8; k++)
                        RND_R1[k] = RECVBuf[k + 7];
                    for (k = 0; k < 4; k++)
                        Bseed12[k] = (char) RND_R1[k];
                    for (k = 4; k < 8; k++)
                        Bseed12[k] = (char) RND_P1[k];
                    for (k = 8; k < 12; k++)
                        Bseed12[k] = (char) RND_P1[k - 8];
                    for (k = 12; k < 16; k++)
                        Bseed12[k] = (char) RND_R1[k - 8];

                    seedx.SeedEncRoundKey(Roundkey, CSN); //CSN으로 라운드키 생성
                    seedx.encrypt(Bseed12, MSK, 16, Roundkey); //MSK 생성

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증 2단계 진행중입니다."); //20200108LJY

                    //20200108LJY
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    {
                        bFirst = false;
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0xA0;
                        btnDisable();

                        scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                        // Serial Port Check
                        int readState = scr.checkSerialPortOpened();
                        if (readState != RTN_COMM_OK) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            return;
                        }

                        scr.clearTxBuffer();

                        writeBuffer = new char[47];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x10; //Length(2)
                        Get_RandomKey(RND_P2, 8);
                        String sendstr = "F2" + new String(RND_P2, 0, RND_P2.length);
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 10);
                        writeBuffer[14] = xor_sum(writeBuffer, 14);

                        temp = new byte[15];
                        for (int ii = 0; ii < 15; ii++) {
                            temp[ii] = (byte) writeBuffer[ii];
                        }
                        scr.sendMsg(temp, temp.length);
                        PopupOpen(getContext(), "상호인증 2단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                        scr.clearRxBuffer();
                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                    {
                        mUart = new libUart();
                        bFirst = false;
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0xA0;
                        btnDisable();

                        writeBuffer = new char[47];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x10; //Length(2)
                        Get_RandomKey(RND_P2, 8);
                        String sendstr = "F2" + new String(RND_P2, 0, RND_P2.length);
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 10);
                        writeBuffer[14] = xor_sum(writeBuffer, 14);

                        temp = new byte[15];
                        for (int ii = 0; ii < 15; ii++) {
                            temp[ii] = (byte) writeBuffer[ii];
                        }
                        PopupOpen(getContext(), "상호인증 2단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                        }
                        //카드리더UART로 IC테스트 명령 전송
                        mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else {
                        if(!SharedManager.isBizdown)
                        {
                            //LJY20200812 : 가맹점다운로드 예외처리
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                            Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                            btnEnable();
//                            return;
                        }
                        if (SharedManager.isStatus == false) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            PopupOpenWithClose(getContext(), "리더기 연결 상태 체크해주시길 바랍니다. 0xA0 F2");
                            return;
                        }

                        if (usbService != null) {
                            bFirst = false;
                            isrun = true;

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();

                            Arrays.fill(RECVBuf, (char) 0x00);
                            Arrays.fill(encdata, (char) 0x00);
                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            func_code = 0xA0;
                            btnDisable();

                            writeBuffer = new char[47];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = func_code; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x10; //Length(2)
                            Get_RandomKey(RND_P2, 8);
                            String sendstr = "F2" + new String(RND_P2, 0, RND_P2.length);
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 10);
                            writeBuffer[14] = xor_sum(writeBuffer, 14);

                            temp = new byte[15];
                            for (int ii = 0; ii < 15; ii++) {
                                temp[ii] = (byte) writeBuffer[ii];
                            }
                            PopupOpen(getContext(), "상호인증 2단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정
                            usbService.write(temp);
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! - USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                            ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! USB 서비스 불가!"); //LJY20250904 : LOCK 비활성화
                            //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화

                            btnEnable();
                            PopupOpenWithClose(getContext(), "상호인증 실패! USB 서비스 불가! 0xA0 F2");
                            return;
                        }
                    }
                } else if (RECVBuf[5] == 'F' && RECVBuf[6] == '3') {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0xA0 F3 완료");

                    if (RECVBuf[7] == 0x30) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 카드리더 상호인증 성공! 에러코드 : " + String.format("%02X", RECVBuf[7] & 0xff));
                        Toast.makeText(getContext(), "카드리더 상호인증 성공!", Toast.LENGTH_SHORT).show(); //Toast 팝업 수정 (LJY20200922)
                        //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화

                        //Toast.makeText(getContext(), "카드리더 상호인증 성공! 에러코드 : " + String.format("%02X", RECVBuf[7] & 0xff), Toast.LENGTH_SHORT).show();
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 카드리더 상호인증 실패! 에러코드 : " + String.format("%02X", RECVBuf[7] & 0xff));
                        Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! -  에러코드 : " + String.format("%02X", RECVBuf[7] & 0xff), Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).InsertChkvalid("N", "카드리더 상호인증 실패! 에러코드 : " + String.format("%02X", RECVBuf[7] & 0xff)); //LJY20250904 : LOCK 비활성화
                        //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화

                        btnEnable();
                        PopupOpenWithClose(getContext(), "카드리더 상호인증 실패! 에러코드 : " + String.format("%02X", RECVBuf[7] & 0xff) + " 0xA0 F3");
                        return;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 무결성검증 진행중입니다."); //20200108LJY

                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    {
                        bFirst = false;
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0xA1;
                        btnDisable();

                        scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                        // Serial Port Check
                        int readState = scr.checkSerialPortOpened();
                        if (readState != RTN_COMM_OK) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            return;
                        }

                        scr.clearTxBuffer();

                        writeBuffer = new char[5];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x00; //Length(2)
                        writeBuffer[4] = xor_sum(writeBuffer, 4);

                        temp = new byte[5];
                        for (int i = 0; i < 5; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        scr.sendMsg(temp, temp.length);
                        PopupOpen(getContext(), "무결성검증 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                        scr.clearRxBuffer();
                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else
                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                    {
                        mUart = new libUart();
                        bFirst = false;
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0xA1;
                        btnDisable();

                        writeBuffer = new char[5];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x00; //Length(2)
                        writeBuffer[4] = xor_sum(writeBuffer, 4);

                        temp = new byte[5];
                        for (int i = 0; i < 5; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpen(getContext(), "무결성검증 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                        }
                        //카드리더UART로 IC테스트 명령 전송
                        mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else {
                        if(!SharedManager.isBizdown)
                        {
                            //LJY20200812 : 가맹점다운로드 예외처리
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                            Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                            btnEnable();
//                            return;
                        }
                        if (SharedManager.isStatus == false) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                            Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                            btnEnable();
                            PopupOpenWithClose(getContext(), "리더기 연결 상태 체크해주시길 바랍니다. 0xA1");
                            return;
                        }

                        if (usbService != null) {
                            bFirst = false;
                            isrun = true;

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();

                            Arrays.fill(RECVBuf, (char) 0x00);
                            Arrays.fill(encdata, (char) 0x00);
                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            func_code = 0xA1;
                            btnDisable();

                            writeBuffer = new char[5];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = func_code; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x00; //Length(2)
                            writeBuffer[4] = xor_sum(writeBuffer, 4);

                            temp = new byte[5];
                            for (int i = 0; i < 5; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpen(getContext(), "무결성검증 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정
                            usbService.write(temp);
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! - USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                            ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! USB 서비스 불가!"); //LJY20250904 : LOCK 비활성화
                            //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화

                            btnEnable();
                            PopupOpenWithClose(getContext(), "상호인증 실패! USB 서비스 불가! 0xA1");
                            return;
                        }
                    }
                }
            } else if (func_code == 0x31) {
                int k;
                for (k = 0; k < 10; k++)
                    ReaderSN[k] = RECVBuf[k + 4];
                for (k = 0; k < 16; k++)
                    CSN[k] = RECVBuf[k + 14];
                for (k = 0; k < 16; k++)
                    HWNUM[k] = RECVBuf[k + 30];
                etHwnum.setText(new String(HWNUM));

                //20200318 : 리더기일련번호
                mSharedManager.getPreferences().edit().putString("READERSN", new String(ReaderSN)).commit();

                //LJY20250904 : 리더기 버전 저장
                for (k = 0; k < 2; k++) cReaderBinVer[k] = RECVBuf[k + 30 + 16 + 6];
                mSharedManager.getPreferences().edit().putString("READERBINVER", new String(cReaderBinVer)).commit();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 버전 : " + new String(cReaderBinVer));
                if(mSharedManager.getPreferences().getBoolean("Payprouse", true) == false) { //LJY20251204 : 통합결제 사용 옵션 처리
                    mSharedManager.getPreferences().edit().putString("READERBINVER", "00").commit();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 버전 변경 (통합결제 미사용) : " + "00");
                }


                //OSM20260430 : 지원목록 로직 추가
                if (length_recv >= 60) {
                    try {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 지원목록 파싱 시작");

                        if (cSupportedList == null || cSupportedList.length < 10) {
                            cSupportedList = new char[10];
                        }
                        Arrays.fill(cSupportedList, (char) 0x00);

                        int offset = 30 + 16 + 6 + 2;
                        int need = offset + 10;

                        for (k = 0; k < 10; k++) cSupportedList[k] = RECVBuf[offset + k];
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 지원가능목록 : '" + new String(cSupportedList).replace("\u0000", "_") + "'");

                        String supportedStr = new String(cSupportedList).replace("\u0000", "").trim();
                        mSharedManager.getPreferences().edit().putString("SUPPORTEDLIST", supportedStr).commit();

                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 지원목록 저장 완료");
                    } catch (Throwable t) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 지원목록 파싱 예외: " + Log.getStackTraceString(t));
                    }
                }


                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 상호인증 1단계 진행중입니다."); //20200108LJY

                //20200108LJY
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                {
                    bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0xA0;
                    btnDisable();

                    scr = new ScrProtocolCom(getContext(), "COM" + (mSharedManager.getPreferences().getInt("Portnum", 0)+1), mSharedManager.getPreferences().getString("BaudrateStr", "115200"));

                    // Serial Port Check
                    int readState = scr.checkSerialPortOpened();
                    if (readState != RTN_COMM_OK) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    scr.clearTxBuffer();

                    writeBuffer = new char[15];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x10; //Length(2)
                    Get_RandomKey(RND_P1, 8);
                    String sendstr = "F1" + new String(RND_P1, 0, RND_P1.length);
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 10);
                    writeBuffer[14] = xor_sum(writeBuffer, 14);

                    temp = new byte[15];
                    for (int ii = 0; ii < 15; ii++) {
                        temp[ii] = (byte) writeBuffer[ii];
                    }
                    scr.sendMsg(temp, temp.length);
                    PopupOpen(getContext(), "상호인증 1단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                    scr.clearRxBuffer();
                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else
                if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
                {
                    mUart = new libUart();
                    bFirst = false;
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
                    func_code = 0xA0;
                    btnDisable();

                    writeBuffer = new char[15];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x10; //Length(2)
                    Get_RandomKey(RND_P1, 8);
                    String sendstr = "F1" + new String(RND_P1, 0, RND_P1.length);
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 10);
                    writeBuffer[14] = xor_sum(writeBuffer, 14);

                    temp = new byte[15];
                    for (int ii = 0; ii < 15; ii++) {
                        temp[ii] = (byte) writeBuffer[ii];
                    }
                    PopupOpen(getContext(), "상호인증 1단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정

                    //LJY20201217 : 리더기 포트번호/통신속도 가변
                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                    }
                    //카드리더UART로 IC테스트 명령 전송
                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                }
                else {
                    if(!SharedManager.isBizdown)
                    {
                        //LJY20200812 : 가맹점다운로드 예외처리
//                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                        btnEnable();
//                        return;
                    }
                    if (SharedManager.isStatus == false) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        PopupOpenWithClose(getContext(), "리더기 연결 상태 체크해주시길 바랍니다. 0x31");
                        return;
                    }

                    if (usbService != null) {
                        bFirst = false;
                        isrun = true;

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        func_code = 0xA0;
                        btnDisable();

                        writeBuffer = new char[15];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x10; //Length(2)
                        Get_RandomKey(RND_P1, 8);
                        String sendstr = "F1" + new String(RND_P1, 0, RND_P1.length);
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 10);
                        writeBuffer[14] = xor_sum(writeBuffer, 14);

                        temp = new byte[15];
                        for (int ii = 0; ii < 15; ii++) {
                            temp[ii] = (byte) writeBuffer[ii];
                        }
                        PopupOpen(getContext(), "상호인증 1단계 진행중입니다. (리더기 연결 중)"); //LJY20220908 : 문구 수정
                        usbService.write(temp);
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(getContext(), "상호인증 및 무결성점검 실패! - USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).InsertChkvalid("N", "상호인증 실패! USB 서비스 불가!"); //LJY20250904 : LOCK 비활성화
                        //mSharedManager.getPreferences().edit().putBoolean("bPermission", false).commit();   //OSM20250902 : permisson 구분자 초기화

                        btnEnable();
                        PopupOpenWithClose(getContext(), "상호인증 실패! USB 서비스 불가! 0xA0 F1");
                        return;
                    }
                }
            }
        }
    };

    private class handler_thread extends Thread implements Runnable {
        Handler mHandler;
        int itimeover;
        byte cData;

        handler_thread(Handler h) {
            mHandler = h;
        }

        public void run() {
            Message msg;

            tstart = System.currentTimeMillis();

            //20190108LJY
            if(mSharedManager.getPreferences().getInt("Readertype", 0) == 3) //POSBANK
            {
                int readState = 0;
                long startTimeTick = Utils.GetStartTimeTick();
                while (Utils.CheckTickTimeOut(startTimeTick, 3000)) { //ACK타임아웃 3초
                    readState = scr.readMsg(250);
                    if (readState != RTN_CONTINUE) break;
                }

                // 통신버퍼 Clear
                scr.clearBuffer();

                if (readState == RTN_COMM_OK) {
                    //응답전문수신
                    if (scr.respMsg.rxCommandID == CH_NAK) { //NAK 수신
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    } else if (scr.respMsg.rxCommandID == CH_ACK) { // ACK수신 - 응답전문 수신 대기.
                        scr.clearRxBuffer();
                        startTimeTick = Utils.GetStartTimeTick();
                        while (Utils.CheckTickTimeOut(startTimeTick, 5000)) {
                            readState = scr.readMsg(250);
                            if (readState != RTN_CONTINUE) break;
                        }

                        // 통신버퍼 Clear
                        scr.clearBuffer();

                        if (readState == RTN_COMM_OK) {
                            //응답전문수신
                            if (scr.respMsg.rxCommandID == (byte) func_code) {
                                for (int k = 0; k < scr.respMsg.rxDataValueLength; k++) {
                                    RECVBuf[k] = (char) scr.respMsg.rxDataValuebyte[k];
                                    scr.respMsg.rxDataValuebyte[k] = 0x00;
                                }

                                length_recv = Integer.parseInt(String.format("%02X", RECVBuf[2] & 0xff) + String.format("%02X", RECVBuf[3] & 0xff)); //JDK20230110 : 포스뱅크 바코드리딩 버그 개선

                                status = 1;
                                msg = mHandler.obtainMessage();
                                mHandler.sendMessage(msg);
                            } else { //RTN_INVALID_DATA
                                status = 2;
                                msg = mHandler.obtainMessage();
                                mHandler.sendMessage(msg);
                            }
                        } else if (readState == RTN_CONTINUE) { //RTN_TIMEOUT
                            status = 2;
                            msg = mHandler.obtainMessage();
                            mHandler.sendMessage(msg);
                        }
                        //return readState;
                    } else if (scr.respMsg.rxCommandID == (byte) func_code) { //응답전문수신
                        for (int k = 0; k < scr.respMsg.rxDataValueLength; k++) {
                            RECVBuf[k] = (char) scr.respMsg.rxDataValuebyte[k];
                            scr.respMsg.rxDataValuebyte[k] = 0x00;
                        }

                        status = 1;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    } else { //RTN_INVALID_DATA
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                } else if (readState == RTN_CONTINUE) { //RTN_TIMEOUT
                    status = 2;
                    msg = mHandler.obtainMessage();
                    mHandler.sendMessage(msg);
                }
                //return readState;
            }
            else
            if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
            {
                while (isrun) {
                    itimeover = calculate_interval(5);

                    if (itimeover == 1) {
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }

                    //카드리더UART 응답데이터 있을시
                    //LJY20201217 : 리더기 포트번호/통신속도 가변
                    if (mUart.IsRxData(mSharedManager.getPreferences().getInt("Portnum", 0)) == true) {
                        //카드리더UART 문자 꺼내기
                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                        cData = mUart.GetCh(mSharedManager.getPreferences().getInt("Portnum", 0));

                        if (istep == 0 && cData == 0x06) //ACK수신
                        {
                            slen = 0;
                            istep = 0;
                        } else if (istep == 0 && cData == 0x04) //EOT수신
                        {
                            slen = 0;
                            istep = 0;
                        } else if (istep == 0 && cData == 0x02) //STX수신
                        {
                            slen = 0;
                            if (bTitchk) //LJY20230911 : TITENG 리더기 추가
                                istep = 21;
                            else
                                istep = 15;
                            RECVBuf[slen++] = (char) cData;
                        }
                        //LJY20220520 : 서명시 좌표 정리
                        else if (istep == 0 && isSign && cData == 0x0F) //좌표시작
                        {
                            slen = 0;
                            istep = 10;
                        } else if (istep == 10 && slen < 2 && isSign) //좌표입력
                        {
                            RECVBuf[slen++] = (char) cData;
                            istep = 10;
                        } else if (istep == 10 && slen == 2 && isSign && cData == 0x0E) //좌표종료
                        {
                            slen = 0;
                            istep = 0;
                        } else if (istep == 15) //COMMAND수신
                        {
                            istep = 20;
                            RECVBuf[slen++] = (char) cData;
                        } else if (istep == 20) //길이수신
                        {
                            RECVBuf[slen++] = (char) cData;
                            if (slen == 4) {
                                istep = 25;
                                length_recv = Integer.parseInt(String.format("%02X", RECVBuf[2] & 0xff) + String.format("%02X", RECVBuf[3] & 0xff));
                            }
                        } else if (istep == 21) { //LJY20230911 : TITENG 리더기 추가 //길이수신
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] TITENG 길이 수신");
                            RECVBuf[slen++] = (char) cData;
                            if (slen == 3) {
                                istep = 26;
                                length_recv = RECVBuf[1] * 16 + RECVBuf[2];
                            }
                        } else if (istep == 25) { //데이터수신
                            RECVBuf[slen++] = (char) cData;
                            if (length_recv == slen - 4) {
                                istep = 30;
                            }
                        } else if (istep == 26) { //LJY20230911 : TITENG 리더기 추가 //데이터수신
                            RECVBuf[slen++] = (char) cData;
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] TITENG 데이터 수신");
                            if (length_recv + 1 == slen - 3) //ETX(1)
                                istep = 30;
                        } else if (istep == 30) { //데이터수신완료
                            istep = 0;
                            status = 1;
                        }


                        if (status == 1 && slen > 0) {
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] OKPOS");
//                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RECVBuf : " + new String(RECVBuf));
                            msg = mHandler.obtainMessage();
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }
            else {
                while (isrun) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                    }

                    itimeover = calculate_interval(5);

                    if (itimeover == 1) {
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }

                    //LJY20220520 : 서명/멀티패드 요청취소에 대한 예외처리
                    if((isSign || isMultipad) && RECVBuf[0] == 0x04)
                    {
                        slen = 1;
                        istep = 0;
                        status = 1;

                        if (status == 1 && slen > 0 && RECVBuf[0] == 0x04) {
//                            RECVBuf[0] = 0x00; //LJY20220520 : 초기화 처리 후 진행
                            msg = mHandler.obtainMessage();
                            mHandler.sendMessage(msg);
                        }
                    }
                    else
                    if (status == 1 && slen > 0) {
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    public static void performChkvalid() {
        if (!bRooting) {
            btChkvalid.performClick();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedManager.LogDebug(bLogUse, "debugjy", "OnResume");


        if (!bRooting && !bApkchk) {
            if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 3 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
            {
                if (isStatus == false) {
                    btChkvalid.performClick();
                }
            }
        }

        if (cbMinimalwindow.isChecked()) {        //OSM20240508 : 창 최소화 기능 추가
            class myThread extends Thread {
                public void run() {
                    SharedManager.LogDebug(bLogUse, "debugjy", "OnResume_RunStart");
                    long itime = Long.parseLong(etTimeout3.getText().toString());
                    long itime2 = Long.parseLong(etTimeout2.getText().toString());
                    String stime = String.valueOf(itime);
                    String stime2 = String.valueOf(itime2);

                    if (bCount == true) {       //OSM20240618 : 설정 저장 시에만 창 최소화 활성화
                        try {
                            itime2 = (itime2 * 1000);
                            Thread.sleep(itime2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Intent main_intent = new Intent(Intent.ACTION_MAIN);
                        main_intent.addCategory(Intent.CATEGORY_HOME);           //OSM20240508 : 홈 화면 이동
                        main_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(main_intent);

                        itime = itime2;
                        bCount = false;
                    }

                    else {                     //OSM20240709 : 설정저장 아닌 Cass
                        try {
                            itime = (itime * 1000);
                            Thread.sleep(itime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Intent main_intent = new Intent(Intent.ACTION_MAIN);
                        main_intent.addCategory(Intent.CATEGORY_HOME);           //OSM20240508 : 홈 화면 이동
                        main_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(main_intent);

                        return;
                    }
                }
            }
            myThread my_thread = new myThread();
            my_thread.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedManager.LogDebug(bLogUse, "debugjy", "OnPause");

    }

    public void InsertChkvalid(String ResultCode, String Reason) {
        if (ResultCode.equals("N")) {
            etHwnum.setText("");
            mSharedManager.getPreferences().edit().putString("HWNUM", etHwnum.getText().toString()).commit();
            mSharedManager.getPreferences().edit().putString("READERSN", "          ").commit();
        } else {
            if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2 || mSharedManager.getPreferences().getInt("Readertype", 0) == 3 || mSharedManager.getPreferences().getInt("Readertype", 0) == 7) //LJY20260109 : OKPOS TDR
            {
                isStatus = true;
            }
            mSharedManager.getPreferences().edit().putString("HWNUM", etHwnum.getText().toString()).commit();
            mSharedManager.getPreferences().edit().putString("READERSN", new String(ReaderSN)).commit();
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dbHelper.insert(simpleDateFormat.format(date), ResultCode, Reason);
    }
}