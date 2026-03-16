package kr.co.nicevan.androidnvcat;


import static android.app.Activity.RESULT_OK;
import static com.posbank.device.common.AscII.CH_ACK;
import static com.posbank.device.common.AscII.CH_NAK;
import static com.posbank.device.common.ReturnValue.RTN_COMM_OK;
import static com.posbank.device.common.ReturnValue.RTN_CONTINUE;
import static kr.co.nicevan.androidnvcat.MainActivity.CompareKsn;
import static kr.co.nicevan.androidnvcat.MainActivity.btnDisable;
import static kr.co.nicevan.androidnvcat.MainActivity.btnEnable;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.CardBrand;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.CardCvm;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.Paygb;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupOpenEOT;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RECVBuf;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cMediagb;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.calculate_interval;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.dbHelper;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.encdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.icdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.icdataLen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.initSerial;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isReaderCheck;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isSign;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isrun;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.istep;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.length_recv;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.mUart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.recvBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.sReaderApprtp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.scr;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.sendBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.slen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.status;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.temp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.tstart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.usbService;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.writeBuffer;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.xor_sum;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.BarcodeToTrack2;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.SetFinish;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRelease;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.iresult;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.posbank.device.common.Utils;
import com.posbank.device.screader.kis.protocol.ScrProtocolCom;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import kr.co.nicevan.androidnvcat.shared.SharedManager;
import kr.co.nicevan.pos.PosClient;
import okpos.co.kr.payroid.libUart;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThreeFragment extends Fragment {
    CheckBox cbKeyin;
    EditText etCashMoney;
    EditText etCashTax;
    EditText etCashBongsa;
    EditText etCashApprno;
    EditText etCashApprdate;
    EditText etCashRecvmsg;
    Button btReqCash;
    Button btnCashClear;
    ArrayList<String> alcashtp = new ArrayList<>();
    String mCashMoney, mCashTax, mCashBongsa, mHwnum, mServerip, mServerport, mCatid, mTimeout, mCashHalbu;
    Spinner spCashtp;
    ArrayAdapter spinnerAdapter;
    TextView tvApprCashTitle; //LJY20221004 : tvApprCashTitle 추가

    private handler_thread handlerThread;
    private SharedManager mSharedManager;
    private String mFiller;

    public ThreeFragment() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금결제 탭입니다.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSharedManager = SharedManager.getInstance(getActivity());
        View view = inflater.inflate(R.layout.fragment_three, container, false);

        //mFiller = "NVC" + mSharedManager.getPreferences().getString("READERSN", "          ") + SharedManager.ROMVER + "                               ";
        //LJY20220816 : READERSN 예외 처리
        String sReaderSn = mSharedManager.getPreferences().getString("READERSN", "          ");
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] READERSN : " + sReaderSn);
        if(sReaderSn.length() != 10) {
            mFiller = "NVC" + "          " + SharedManager.ROMVER + "                               ";
        }
        else
            mFiller = "NVC" + mSharedManager.getPreferences().getString("READERSN", "          ") + SharedManager.ROMVER + "                               "; //20200318 : 리더기일련번호

        alcashtp.add("01:소비자");
        alcashtp.add("02:사업자");
        alcashtp.add("03:자진발급");

        spCashtp = (Spinner) view.findViewById(R.id.spcashtp);
        spinnerAdapter = new ArrayAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, alcashtp);
        spCashtp.setAdapter(spinnerAdapter);
        spCashtp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cbKeyin = (CheckBox) view.findViewById(R.id.cbkeyin);
        etCashMoney = (EditText) view.findViewById(R.id.etcashmoney);
        etCashTax = (EditText) view.findViewById(R.id.etcashtax);
        etCashBongsa = (EditText) view.findViewById(R.id.etcashbongsa);
        etCashApprno = (EditText) view.findViewById(R.id.etcashapprno);
        etCashApprdate = (EditText) view.findViewById(R.id.etcashapprdate);
        etCashRecvmsg = (EditText) view.findViewById(R.id.etcashrecvmsg);
        tvApprCashTitle = (TextView) view.findViewById(R.id.tv_cash_appr_title); //LJY20221004 : tvApprCashTitle 추가

        mServerip = mSharedManager.getPreferences().getString("Serverip", ""); //LJY20221004 : tvApprCardTitle 컬러 변경
        if(mServerip.length() != 0 && mServerip.equals("211.33.136.19")) {
            tvApprCashTitle.setText("현금영수증 거래 승인 (테스트)");
            tvApprCashTitle.setTextColor(Color.RED);
        } else {
            tvApprCashTitle.setText("현금영수증 거래 승인 (운영)");
            tvApprCashTitle.setTextColor(Color.YELLOW);
        }

        btnCashClear = (Button) view.findViewById(R.id.btncashclear);
        btnCashClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 초기화 버튼 클릭되었습니다.");
                etCashMoney.setText("");
                etCashTax.setText("");
                etCashBongsa.setText("");
                etCashApprno.setText("");
                etCashApprdate.setText("");
                etCashRecvmsg.setText("");
            }
        });

        btReqCash = (Button) view.findViewById(R.id.btnreqcash); //현금영수증 결제 버튼
        btReqCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 결제 버튼 클릭되었습니다.");

                if(!SharedManager.isBizdown)
                {
                    //LJY20200812 : 가맹점다운로드 예외처리
//                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                    Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                    btnEnable();
//                    return;
                }
                if (SharedManager.isStatus == false) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                    Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }

                if (spCashtp.getSelectedItemPosition() == 0) mCashHalbu = "01";
                else if (spCashtp.getSelectedItemPosition() == 1) mCashHalbu = "02";
                else if (spCashtp.getSelectedItemPosition() == 2) mCashHalbu = "03";
                else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 거래 구분 선택해주세요.");
                    Toast.makeText(getContext(), "거래 구분 선택해주세요.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 거래구분 : " + mCashHalbu);

                if (etCashMoney.getText().length() == 0 || etCashMoney.getText().length() > 12) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 금액을 입력해주세요.");
                    Toast.makeText(getContext(), "금액을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                } else {
                    mCashMoney = String.format("%012d", Long.parseLong(etCashMoney.getText().toString()));
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 금액 : " + mCashMoney);

                if (etCashTax.getText().length() > 12) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 부가세를 잘못 입력했습니다.");
                    Toast.makeText(getContext(), "부가세를 잘못 입력했습니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                } else if (etCashTax.getText().length() == 0) {
                    mCashTax = "000000000000";
                } else {
                    mCashTax = String.format("%012d", Long.parseLong(etCashTax.getText().toString()));
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 부가세 : " + mCashTax);

                if (etCashBongsa.getText().length() > 12) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 봉사료를 잘못 입력했습니다.");
                    Toast.makeText(getContext(), "봉사료를 잘못 입력했습니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                } else if (etCashBongsa.getText().length() == 0) {
                    mCashBongsa = "000000000000";
                } else {
                    mCashBongsa = String.format("%012d", Long.parseLong(etCashBongsa.getText().toString()));
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 봉사료 : " + mCashBongsa);

                mCatid = mSharedManager.getPreferences().getString("Catid", "");
                if (mCatid.length() != 10) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이가 10이 아닙니다.");
                    Toast.makeText(getContext(), "CATID 길이가 10이 아닙니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID : " + mCatid);

                mHwnum = mSharedManager.getPreferences().getString("HWNUM", "################"); //LJY20220816 : Default 값 설정
                if (mHwnum.length() != 16) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] HW식별번호가 잘못 되었습니다.");
                    Toast.makeText(getContext(), "HW식별번호가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] HW식별번호 : " + mHwnum);

                mServerip = mSharedManager.getPreferences().getString("Serverip", "");
                if (mServerip.length() == 0 || mServerip.length() > 16) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 IP가 잘못 되었습니다.");
                    Toast.makeText(getContext(), "서버 IP가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버IP : " + mServerip);

                mServerport = mSharedManager.getPreferences().getString("Serverport", "");
                if (mServerport.length() == 0 || mServerport.length() > 6) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버 PORT가 잘못 되었습니다.");
                    Toast.makeText(getContext(), "서버 PORT가 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서버PORT : " + mServerport);

                mTimeout = mSharedManager.getPreferences().getString("Timeout", "30");
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기타임아웃 : " + mTimeout);

                if (mCashHalbu.equals("03")) //자진발급
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 자진발급 중입니다.");
                    PopupOpen(getContext(), "자진발급 중입니다.");

                    String cashnum = "37" + "0100001234" + "=";
                    mCashHalbu = "01";
                    String space = "                                                                                                                                                      ";

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                    PosClient posClient = new PosClient();
                    sendBuff = ("0437HPS" + mCatid + strDate + "020021H1          " + mCatid + "@" + cashnum + space.substring(0, 127 - cashnum.length()) + mCashHalbu + mCashBongsa + mCashTax + mCashMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();

                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);

                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();

                    return;
                } else if (cbKeyin.isChecked()) //키인결제
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Keyin 결제 중입니다.");
                    Intent i = new Intent(getActivity(), KeyPadNumber.class);
                    startActivityForResult(i, 3);
                } else //카드결제
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 카드 결제 중입니다.");
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금영수증 카드 리딩해주세요.");

                    String sReaderBinVer = mSharedManager.getPreferences().getString("READERBINVER", "  "); //LJY20250904 : 8BIN/통합결제 적용
                    if(sReaderBinVer.equals("02")) { //통합결제
                        func_code = 0x9C;
                        sReaderApprtp = "4";
                    } else if(sReaderBinVer.equals("01")) { //8BIN
                        func_code = 0x9C;
                        sReaderApprtp = "0";
                    } else { //6BIN
                        func_code = 0x6C;
                        sReaderApprtp = "0";
                    }

                    if((mSharedManager.getPreferences().getInt("Readertype", 0) == 3) && (isrun == false)) //OSM20240605 : 중복 호출 방지 추가		//POSBANK
                    {
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
//                        func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
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

                        writeBuffer = new char[44];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x39; //Length(2)
                        String sendstr = mTimeout; //Card 대기시간(2)
                        String strDate = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
                        sendstr = sendstr + strDate; //거래일시(14)
                        sendstr = sendstr + mCashMoney; //거래금액(12)
                        sendstr = sendstr + mCatid; //TID(10)
                        sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                        writeBuffer[43] = xor_sum(writeBuffer, 43);

                        temp = new byte[44];
                        for (int i = 0; i < 44; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        scr.sendMsg(temp, temp.length);
                        PopupOpenEOT(getContext(), "현금영수증 카드 리딩해주세요.");

                        scr.clearRxBuffer();
                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else
                    if((mSharedManager.getPreferences().getInt("Readertype", 0) == 2) && (isrun == false)) //OSM20240605 : 중복 호출 방지 추가 //OKPOS
                    {
                        mUart = new libUart();
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
//                        func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                        btnDisable();

                        writeBuffer = new char[44];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x39; //Length(2)
                        String sendstr = mTimeout; //Card 대기시간(2)
                        String strDate = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
                        sendstr = sendstr + strDate; //거래일시(14)
                        sendstr = sendstr + mCashMoney; //거래금액(12)
                        sendstr = sendstr + mCatid; //TID(10)
                        sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                        writeBuffer[43] = xor_sum(writeBuffer, 43);

                        temp = new byte[44];
                        for (int i = 0; i < 44; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpenEOT(getContext(), "현금영수증 카드 리딩해주세요.");

                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                        }
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
                            return;
                        }

                        if (usbService != null) { // if UsbService was correctly binded, Send data
                            if (isrun == false) {        //OSM20240605 : 중복 호출 방지 추가
                                isrun = true;

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();

                                Arrays.fill(RECVBuf, (char) 0x00);
                                Arrays.fill(encdata, (char) 0x00);
                                Arrays.fill(icdata, (char) 0x00);

                                initSerial();
//                        func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                btnDisable();

                                writeBuffer = new char[44];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = func_code; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x39; //Length(2)
                                String sendstr = mTimeout; //Card 대기시간(2)
                                String strDate = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
                                sendstr = sendstr + strDate; //거래일시(14)
                                sendstr = sendstr + mCashMoney; //거래금액(12)
                                sendstr = sendstr + mCatid; //TID(10)
                                sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                                System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                writeBuffer[43] = xor_sum(writeBuffer, 43);

                                temp = new byte[44];
                                for (int i = 0; i < 44; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(getContext(), "현금영수증 카드 리딩해주세요.");
                                usbService.write(temp);
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                            btnEnable();
                            return;
                        }
                    }
                }
            }
        });

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            if (resultCode == RESULT_OK) //식별번호입력완료
            {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 현금PIN OK 버튼 클릭");
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN : " + data.getStringExtra("RESULT"));

                if (data.getStringExtra("RESULT").length() > 0) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN LENGTH OK!!");
                    PopupOpen(getContext(), "현금영수증 키인 VAN 승인 중입니다.");

                    String cashnum = "37" + data.getStringExtra("RESULT") + "=";
                    String space = "                                                                                                                                                      ";
                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());

                    PosClient posClient = new PosClient();
                    sendBuff = ("0437HPS" + mCatid + strDate + "020021H1          " + mCatid + "@" + cashnum + space.substring(0, 127 - cashnum.length()) + mCashHalbu + mCashBongsa + mCashTax + mCashMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                    }else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                    return;
                } else {
                    btnCashClear.performClick();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 입력된 식별번호가 없습니다.");
                    Toast.makeText(getContext(), "입력된 식별번호가 없습니다.", Toast.LENGTH_LONG).show();
                    btnEnable();
                    return;
                }
            } else //취소
            {
                btnCashClear.performClick();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 식별번호 입력 취소 하셨습니다.");
                Toast.makeText(getContext(), "식별번호 입력 취소 하셨습니다.", Toast.LENGTH_LONG).show();
                btnEnable();
                return;
            }
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isrun = false;
            PopupClose();

            if (status == 2) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 시리얼 통신 타임아웃");
                Toast.makeText(getContext(), "시리얼 통신 타임아웃", Toast.LENGTH_LONG).show();
                btnEnable();


                //OSM20250902 : 타임아웃일 때 리더기로 EOT 전송
                byte[] EOT = new byte[1];
                EOT[0] = 0x04;

                if (SharedManager.getInstance(getContext()).getPreferences().getInt("Readertype", 0) == 3) //POSBANK
                    scr.sendEot();
                else if (SharedManager.getInstance(getContext()).getPreferences().getInt("Readertype", 0) == 2) //OKPOS
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

            if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 에러코드 : " + errcode);

                if (errcode.equals("00")) {//FALLBACK 카드리딩 정상
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 수신 정상");

                    System.arraycopy(RECVBuf, 9, encdata, 0, 104);
                    System.arraycopy(RECVBuf, 123, encdata, 104, 23);
                    System.arraycopy(RECVBuf, 5, icdata, 0, 2);

                    if(CompareKsn() == -1) {    //LJY20260109 : KSN 체크
                        Toast.makeText(getContext(), "중복 거래 방지", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PopupOpen(getContext(), "VAN 승인 중입니다.");

                    PosClient posClient = new PosClient();
                    sendBuff = ("0437HPS" + mCatid + strDate + "020021H1          " + mCatid + "F" + new String(encdata) + mCashHalbu + mCashBongsa + mCashTax + mCashMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
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

                    InsertRecv(recvBuff);
                    PopupClose();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 에러코드 : " + errcode);
                    Toast.makeText(getContext(), "FALLBACK 에러코드 : " + errcode, Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }
            } else if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용 //IC카드리딩
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 에러코드 : " + errcode);

                if (errcode.equals("00")) { //IC카드리딩 정상
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 수신 정상");

                    System.arraycopy(RECVBuf, 12, encdata, 0, 104);
                    System.arraycopy(RECVBuf, 126, encdata, 104, 23);
                    System.arraycopy(RECVBuf, 6, Paygb, 0, 1); //결제구분 : "I":IC, "M":MSR
                    System.arraycopy(RECVBuf, 8, CardBrand, 0, 1); //LJY20200713 : 동반위 JUST TOUCH
                    System.arraycopy(RECVBuf, 9, CardCvm, 0, 1); //LJY20230713 : 은련PIN 체크
                    //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                    System.arraycopy(RECVBuf, 5, cMediagb, 0, 1); //매체구분 : Mobile – “M”, Plastic – “P”, 바코드/QR – “B”

                    if(CompareKsn() == -1) {    //LJY20260109 : KSN 체크
                        Toast.makeText(getContext(), "중복 거래 방지", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }

                    if(func_code == 0x6C)
                        System.arraycopy(RECVBuf, 180, icdata, 0, 257);
                    else {
                        char[] cFillerLen = new char[4];
                        System.arraycopy(RECVBuf, 244, cFillerLen, 0, 4);
                        int iFillerLen = Integer.parseInt(new String(cFillerLen).trim());

                        System.arraycopy(RECVBuf, 244+4+iFillerLen, icdata, 0, 257);
                    }

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PopupOpen(getContext(), "VAN 승인 중입니다.");

                    PosClient posClient = new PosClient();
                    //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                    if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 현금승인");
                        sendBuff = ("0437PRO" + mCatid + strDate + "020021H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mCashHalbu + mCashBongsa + mCashTax + mCashMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                    }
                    else if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 현금승인(동반위)");
                        sendBuff = ("0694HPS" + mCatid + strDate + "020021H1          " + mCatid + "K" + new String(encdata) + mCashHalbu + mCashBongsa + mCashTax + mCashMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                    }
                    else
                    if (Paygb[0] == 'I') {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 현금승인");
                        sendBuff = ("0694HPS" + mCatid + strDate + "020021H1          " + mCatid + "I" + new String(encdata) + mCashHalbu + mCashBongsa + mCashTax + mCashMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 현금승인");
                        sendBuff = ("0437HPS" + mCatid + strDate + "020021H1          " + mCatid + "A" + new String(encdata) + mCashHalbu + mCashBongsa + mCashTax + mCashMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                    }
                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0)
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    InsertRecv(recvBuff);
                    PopupClose();
                } else if (errcode.equals("CF") == true) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC FALLBACK 코드 수신");

                    if (mSharedManager.getPreferences().getBoolean("Retry", false)) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 재시도 사용 안함");
                        Toast.makeText(getContext(), "FALLBACK 재시도 사용 안함", Toast.LENGTH_LONG).show();
                        btnEnable();
                        return;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 카드리딩 요청입니다.");

                    String sReaderBinVer = mSharedManager.getPreferences().getString("READERBINVER", "  "); //LJY20250904 : 8BIN/통합결제 적용
                    if(sReaderBinVer.equals("02") || sReaderBinVer.equals("01")) //통합결제 //8BIN
                        func_code = 0x9E;
                    else
                        func_code = 0x6E;

                    if((mSharedManager.getPreferences().getInt("Readertype", 0) == 3) && (isrun == false)) //OSM20240605 : 중복 호출 방지 추가	//POSBANK
                    {
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
//                        func_code = 0x6E;     //LJY20250904 : 8BIN/통합결제 적용
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

                        writeBuffer = new char[7];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x02; //Length(2)
                        String sendstr = mTimeout; //Card 대기시간(2)
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 2);
                        writeBuffer[6] = xor_sum(writeBuffer, 6);

                        temp = new byte[7];
                        for (int i = 0; i < 7; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        scr.sendMsg(temp, temp.length);
                        PopupOpenEOT(getContext(), "FALLBACK 카드리딩 해주세요.");

                        scr.clearRxBuffer();
                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else
                    if((mSharedManager.getPreferences().getInt("Readertype", 0) == 2) && (isrun == false)) //OSM20240605 : 중복 호출 방지 추가	//OKPOS
                    {
                        mUart = new libUart();
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
                        Arrays.fill(encdata, (char) 0x00);
                        Arrays.fill(icdata, (char) 0x00);

                        initSerial();
//                        func_code = 0x6E;     //LJY20250904 : 8BIN/통합결제 적용
                        btnDisable();

                        writeBuffer = new char[7];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = func_code; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x02; //Length(2)
                        String sendstr = mTimeout; //Card 대기시간(2)
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 2);
                        writeBuffer[6] = xor_sum(writeBuffer, 6);

                        temp = new byte[7];
                        for (int i = 0; i < 7; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpenEOT(getContext(), "FALLBACK 카드리딩 해주세요.");

                        //LJY20201217 : 리더기 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                        }
                        mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                        mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else {
                        if (!SharedManager.isBizdown) {
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
                            return;
                        }

                        if (usbService != null) { // if UsbService was correctly binded, Send data
                            if (isrun == false) {        //OSM20240605 : 중복 호출 방지 추가
                                isrun = true;

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();

                                Arrays.fill(RECVBuf, (char) 0x00);
                                Arrays.fill(encdata, (char) 0x00);
                                Arrays.fill(icdata, (char) 0x00);

                                initSerial();
//                        func_code = 0x6E;     //LJY20250904 : 8BIN/통합결제 적용

                                btnEnable();

                                writeBuffer = new char[7];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = func_code; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x02; //Length(2)
                                String sendstr = mTimeout; //Card 대기시간(2)
                                System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 2);
                                writeBuffer[6] = xor_sum(writeBuffer, 6);

                                temp = new byte[7];
                                for (int i = 0; i < 7; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(getContext(), "FALLBACK 카드리딩 해주세요.");
                                usbService.write(temp);
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                            btnEnable();
                            return;
                        }
                    }
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 에러코드 : " + errcode);
                    Toast.makeText(getContext(), "IC 에러코드 : " + errcode, Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
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
//            FiveFragment.saveLogFile();   //OSM20241223 : 로그 저장

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

//                    FiveFragment.saveLogFile(); //OSM20241223 : 로그 저장

                    //응답전문수신
                    if (scr.respMsg.rxCommandID == CH_NAK) { //NAK 수신
                        status = 2;
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    } else if (scr.respMsg.rxCommandID == CH_ACK) { // ACK수신 - 응답전문 수신 대기.
                        scr.clearRxBuffer();
                        startTimeTick = Utils.GetStartTimeTick();
                        while (Utils.CheckTickTimeOut(startTimeTick, 60000)) {
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
            if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
            {
                while (isrun) {

//                   FiveFragment.saveLogFile();//OSM20241223 : 로그 저장

                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 5 && (func_code == 'A' || func_code == 'R' || func_code == 'S' || func_code == 'e' || func_code == 'E') || isReaderCheck) //OSM20250123 : 리더기 Health Check    //LJY20231006 : TITENG 리더기 연동 시 타임아웃 시간 변경
                        itimeover = calculate_interval(3);
                    else {
                        int timeoutValue = 0; // 기본값
                        try {
                            if (mTimeout != null && !mTimeout.trim().isEmpty())
                                timeoutValue = Integer.parseInt(mTimeout.trim());
                        } catch (NumberFormatException e) {
                            // 로그로 기록만 하고 기본값 유지
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Invalid mTimeout value : " + mTimeout);
                        }
                        itimeover = calculate_interval(timeoutValue + 10);  //OSM20250902 : 타임아웃 시간 변경 (리더기 요청 시간 + 10초)
                        mSharedManager.getPreferences().edit().putInt("NewTimeout", timeoutValue);
                    }

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
                            istep = 15;
                            RECVBuf[slen++] = (char)cData;
                        }
                        //LJY20220520 : 서명시 좌표 정리
                        else if(istep == 0 && isSign && cData == 0x0F) //좌표시작
                        {
                            slen = 0;
                            istep = 10;
                        }
                        else if(istep == 10 && slen < 2 && isSign) //좌표입력
                        {
                            RECVBuf[slen++] = (char)cData;
                            istep = 10;
                        }
                        else if(istep == 10 && slen == 2 && isSign && cData == 0x0E) //좌표종료
                        {
                            slen = 0;
                            istep = 0;
                        }
                        else if (istep == 15) //COMMAND수신
                        {
                            istep = 20;
                            RECVBuf[slen++] = (char)cData;
                        } else if (istep == 20) //길이수신
                        {
                            RECVBuf[slen++] = (char)cData;
                            if (slen == 4) {
                                istep = 25;
                                length_recv = Integer.parseInt(String.format("%02X", RECVBuf[2] & 0xff) + String.format("%02X", RECVBuf[3] & 0xff));
                            }
                        } else if (istep == 25) { //데이터수신
                            RECVBuf[slen++] = (char)cData;
                            if (length_recv == slen - 4) {
                                istep = 30;
                            }
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

                    //                   FiveFragment.saveLogFile(); //OSM20241223 : 로그 저장

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                    }

                    if(mSharedManager.getPreferences().getInt("Readertype", 0) == 5 && (func_code == 'A' || func_code == 'R' || func_code == 'S' || func_code == 'e' || func_code == 'E') || isReaderCheck) //OSM20250123 : 리더기 Health Check    //LJY20231006 : TITENG 리더기 연동 시 타임아웃 시간 변경
                        itimeover = calculate_interval(3);
                    else {
                        int timeoutValue = 0; // 기본값
                        try {
                            if (mTimeout != null && !mTimeout.trim().isEmpty())
                                timeoutValue = Integer.parseInt(mTimeout.trim());
                        } catch (NumberFormatException e) {
                            // 로그로 기록만 하고 기본값 유지
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] Invalid mTimeout value : " + mTimeout);
                        }
                        itimeover = calculate_interval(timeoutValue + 10);  //OSM20250902 : 타임아웃 시간 변경 (리더기 요청 시간 + 10초)
                        mSharedManager.getPreferences().edit().putInt("NewTimeout", timeoutValue);
                    }

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

    private void InsertRecv(byte[] recvBuff) {
        try {
            if (new String(recvBuff, "EUC-KR").equals("-1") || iresult == -1) {
                Toast.makeText(getContext(), "-1", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-2") == true || iresult == -2) {
                Toast.makeText(getContext(), "-2", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-3") == true || iresult == -3) {
                Toast.makeText(getContext(), "-3", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-4") == true || iresult == -4) {
                Toast.makeText(getContext(), "-4", Toast.LENGTH_SHORT).show();
            } else {
                if (bRelease)
                    SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + (new String(recvBuff, "EUC-KR")).substring(0, 68) + "**********" + (new String(recvBuff, "EUC-KR")).substring(78, recvBuff.length - 78));
                else
                    SharedManager.LogDebug(bLogUse, "debugjy", "[VAN->NVCAT] Recvdata : " + new String(recvBuff, "EUC-KR"));
                RecvFormat recv = new RecvFormat();
                recv.str_Msglen = new String(recvBuff, 0, 4, "EUC-KR");
                recv.str_Msgtxt = new String(recvBuff, 4, 3, "EUC-KR");
                recv.str_Msgno = new String(recvBuff, 7, 20, "EUC-KR");
                recv.str_Msggb = new String(recvBuff, 27, 4, "EUC-KR");
                recv.str_Dealgb = new String(recvBuff, 31, 2, "EUC-KR");
                recv.str_Devicegb = new String(recvBuff, 33, 2, "EUC-KR");
                recv.str_Deviceno = new String(recvBuff, 35, 10, "EUC-KR");
                recv.str_Tid = new String(recvBuff, 45, 10, "EUC-KR");
                recv.str_Recvcode = new String(recvBuff, 55, 4, "EUC-KR");
                if (recv.str_Recvcode.equals("0000")) {
                    Toast.makeText(getContext(), "결제완료", Toast.LENGTH_SHORT).show();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 결제완료");
                } else {
                    Toast.makeText(getContext(), "결제실패", Toast.LENGTH_SHORT).show();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 결제실패");
                }
                recv.str_Wcc = new String(recvBuff, 59, 1, "EUC-KR");
                recv.str_Carddata = new String(recvBuff, 60, 6, "EUC-KR") + "**********";
                recv.str_Halbu = new String(recvBuff, 99, 2, "EUC-KR");
                recv.str_Bongsa = new String(recvBuff, 101, 12, "EUC-KR");
                recv.str_Tax = new String(recvBuff, 113, 12, "EUC-KR");
                recv.str_Money = new String(recvBuff, 125, 12, "EUC-KR");
                recv.str_Bizno = new String(recvBuff, 137, 10, "EUC-KR");
                recv.str_Perno = new String(recvBuff, 147, 13, "EUC-KR");
                recv.str_Pinno = new String(recvBuff, 160, 16, "EUC-KR");
                recv.str_Bgcode = new String(recvBuff, 176, 2, "EUC-KR");
                recv.str_Bgnm = new String(recvBuff, 178, 20, "EUC-KR");
                recv.str_Micode = new String(recvBuff, 198, 2, "EUC-KR");
                recv.str_Minm = new String(recvBuff, 200, 20, "EUC-KR");
                recv.str_Storeno = new String(recvBuff, 220, 15, "EUC-KR");
                recv.str_Apprdate = new String(recvBuff, 235, 12, "EUC-KR");
                recv.str_Apprno = new String(recvBuff, 247, 12, "EUC-KR");
                recv.str_Dealno = new String(recvBuff, 259, 12, "EUC-KR");
                recv.str_Dccyn = new String(recvBuff, 271, 1, "EUC-KR");
                recv.str_Msg1 = new String(recvBuff, 272, 40, "EUC-KR");
                recv.str_Msg2 = new String(recvBuff, 312, 24, "EUC-KR");
                recv.str_Msg3 = new String(recvBuff, 336, 24, "EUC-KR");
                recv.str_Msg4 = new String(recvBuff, 360, 24, "EUC-KR");
                recv.str_P1 = new String(recvBuff, 384, 9, "EUC-KR");
                recv.str_P2 = new String(recvBuff, 393, 9, "EUC-KR");
                recv.str_P3 = new String(recvBuff, 402, 9, "EUC-KR");
                recv.str_CBStore = new String(recvBuff, 411, 15, "EUC-KR");
                recv.str_CBApprno = new String(recvBuff, 426, 12, "EUC-KR");
                recv.str_RealApprmoney = new String(recvBuff, 438, 21, "EUC-KR");
                recv.str_DealCardno = new String(recvBuff, 459, 20, "EUC-KR");

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dbHelper.insertDeal(simpleDateFormat.format(new Date()), recv.str_Dealgb, recv.str_Msggb, recv.str_Carddata, recv.str_Money, recv.str_Tax, recv.str_Bongsa, recv.str_Halbu, recv.str_Apprno, recv.str_Apprdate, recv.str_Tid, recv.str_Bgnm, recv.str_Minm, recv.str_Storeno, recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4, recv.str_Recvcode, recv.str_P3, recv.str_Wcc, recv.str_RealApprmoney.substring(9, 10), recv.str_Msgno, recv.str_DealCardno, recv.str_Msgtxt, recv.str_Micode, recv.str_Bgcode); //OSM20250814 : 매입사/발급사 코드 추가  //20200129 : 포인트거래

                etCashMoney.setText(recv.str_Money);
                etCashTax.setText(recv.str_Tax);
                etCashBongsa.setText(recv.str_Bongsa);
                etCashApprno.setText(recv.str_Apprno);
                etCashApprdate.setText(recv.str_Apprdate);
                etCashRecvmsg.setText((recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4).replaceAll(" ", ""));

                Arrays.fill(recvBuff, (byte)0x00);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Arrays.fill(recvBuff, (byte)0x00);
        btnEnable();
    }
}