package kr.co.nicevan.androidnvcat;


import static android.app.Activity.RESULT_OK;
import static com.felhr.utils.HexData.stringTobytes;
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
import static kr.co.nicevan.androidnvcat.shared.SharedArray.bEncPin;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.bTitchk;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.cMediagb;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.calculate_interval;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.dbHelper;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.dialog;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.encdata;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.icdata;
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
import static kr.co.nicevan.androidnvcat.shared.SharedArray.signBuff;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.slen;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.space;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.status;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.temp;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.tstart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.usbService;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.writeBuffer;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.xor_sum;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.BarcodeToTrack2;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.IsBarcodeSign;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bRelease;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.iresult;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.posbank.device.common.Utils;
import com.posbank.device.screader.kis.protocol.ScrProtocolCom;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import kr.co.nicevan.androidnvcat.shared.SharedManager;
import kr.co.nicevan.pos.PosClient;
import kr.co.nicevan.signenc.SignEnc;

import okpos.co.kr.payroid.libUart;


/**
 * A simple {@link Fragment} subclass.
 */
public class TwoFragment extends Fragment {

    CheckBox cbCup;
    CheckBox cbDcc;
    CheckBox cbPoint;
    CheckBox cbMem;
    Button btReqcard;
    Button btnEtclear;
    EditText etMoney;
    EditText etHalbu;
    EditText etTax;
    EditText etBongsa;
    EditText etBal;
    EditText etMinm;
    EditText etApprno;
    EditText etApprdate;
    EditText etRecvmsg;
    EditText etTxt, etDevicegb, etApprtp; //20200129 : 포인트
    EditText etDcc1, etDcc2, etDcc3; //20200306 : DCC
    String mCatid, mMoney, mHalbu, mTax, mBongsa, mHwnum, mServerip, mServerport, mTimeout, EncPin, mTxt, mDevicegb, mApprtp, mFiller2, mDcc1, mDcc2, mDcc3, mBaseamount; //20200129 : 포인트
    private handler_thread handlerThread;
    private SharedManager mSharedManager;
    private String mFiller;
    EditText etFiller2, etP1, etP2, etP3; //20200131 : 멤버쉽거래
    TextView tvTax, tvBongsa, tvFiller2, tvHalbu, tvApprCardTitle; //LJY20221004 : tvApprCardTitle 추가 //20200131 : 멤버쉽거래

    public TwoFragment() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 신용결제 탭입니다.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSharedManager = SharedManager.getInstance(getActivity());
        View view = inflater.inflate(R.layout.fragment_two, container, false);

        //mFiller = "NVC" + mSharedManager.getPreferences().getString("READERSN", "          ") + SharedManager.ROMVER + "                               ";
        //LJY20220816 : READERSN 예외 처리
        String sReaderSn = mSharedManager.getPreferences().getString("READERSN", "          ");
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] READERSN : " + sReaderSn);
        if(sReaderSn.length() != 10) {
            mFiller = "NVC" + "          " + SharedManager.ROMVER + "                               ";
        }
        else
            mFiller = "NVC" + mSharedManager.getPreferences().getString("READERSN", "          ") + SharedManager.ROMVER + "                               "; //20200318 : 리더기일련번호

        cbCup = (CheckBox) view.findViewById(R.id.cbcup);
        cbDcc = (CheckBox) view.findViewById(R.id.cbdcc);
        cbPoint = (CheckBox) view.findViewById(R.id.cbpoint);
        cbMem = (CheckBox) view.findViewById(R.id.cbmem);
        btReqcard = (Button) view.findViewById(R.id.btnreqcard);
        btnEtclear = (Button) view.findViewById(R.id.btnetclear);
        etMoney = (EditText) view.findViewById(R.id.etmoney);
        etHalbu = (EditText) view.findViewById(R.id.ethalbu);
        etTax = (EditText) view.findViewById(R.id.ettax);
        etBongsa = (EditText) view.findViewById(R.id.etbongsa);
        etBal = (EditText) view.findViewById(R.id.etbal);
        etMinm = (EditText) view.findViewById(R.id.etminm);
        etApprno = (EditText) view.findViewById(R.id.etapprno);
        etApprdate = (EditText) view.findViewById(R.id.etapprdate);
        etRecvmsg = (EditText) view.findViewById(R.id.etrecvmsg);
        etTxt = (EditText) view.findViewById(R.id.ettxt); //20200129
        etDevicegb = (EditText) view.findViewById(R.id.etdevicegb); //20200129
        etApprtp = (EditText) view.findViewById(R.id.etapprtp); //20200129
        tvTax = (TextView)view.findViewById(R.id.tvtax); //20200131 : 멤버쉽거래
        tvBongsa = (TextView)view.findViewById(R.id.tvbongsa); //20200131 : 멤버쉽거래
        etFiller2 = (EditText)view.findViewById(R.id.etfiller2); //20200131 : 멤버쉽거래
        tvFiller2 = (TextView)view.findViewById(R.id.tvfiller2); //20200131 : 멤버쉽거래
        etP1 = (EditText)view.findViewById(R.id.etp1); //20200131 : 멤버쉽거래
        etP2 = (EditText)view.findViewById(R.id.etp2); //20200131 : 멤버쉽거래
        etP3 = (EditText)view.findViewById(R.id.etp3); //20200131 : 멤버쉽거래
        tvHalbu = (TextView)view.findViewById(R.id.tvhalbu); //20200131 : 멤버쉽거래
        etDcc1 = (EditText)view.findViewById(R.id.etdcc1);
        etDcc2 = (EditText)view.findViewById(R.id.etdcc2);
        etDcc3 = (EditText)view.findViewById(R.id.etdcc3);
        tvApprCardTitle = (TextView)view.findViewById(R.id.tv_card_appr_title); //LJY20221004 : tvApprCardTitle 추가

        mServerip = mSharedManager.getPreferences().getString("Serverip", ""); //LJY20221004 : tvApprCardTitle 컬러 변경
        if(mServerip.length() != 0 && mServerip.equals("211.33.136.19")) {
            tvApprCardTitle.setText("신용 거래 승인 (테스트)");
            tvApprCardTitle.setTextColor(Color.RED);
        } else {
            tvApprCardTitle.setText("신용 거래 승인 (운영)");
            tvApprCardTitle.setTextColor(Color.YELLOW);
        }

        cbCup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbCup.isChecked()) //체크됨
                {
                    tvTax.setText("부가세");
                    tvBongsa.setText("봉사료");
                    tvHalbu.setText("할부");

                    cbPoint.setChecked(false);
                    cbMem.setChecked(false);
                    cbDcc.setChecked(false); //20200306 : DCC
                }
            }
        });

        cbPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbPoint.isChecked()) //체크됨
                {
                    tvTax.setText("부가세");
                    tvBongsa.setText("봉사료");
                    tvHalbu.setText("할부");

                    cbCup.setChecked(false);
                    cbMem.setChecked(false);
                    cbDcc.setChecked(false); //20200306 : DCC
                }
            }
        });

        cbMem.setOnClickListener(new View.OnClickListener() { //20200131 : 멤버쉽거래
            @Override
            public void onClick(View view) {
                if(cbMem.isChecked()) //체크됨
                {
                    tvTax.setText("적립구분");
                    tvBongsa.setText("포인트구분");
                    tvHalbu.setText("비밀번호");

                    cbCup.setChecked(false);
                    cbPoint.setChecked(false);
                    cbDcc.setChecked(false); //20200306 : DCC
                }
            }
        });

        cbDcc.setOnClickListener(new View.OnClickListener() { //20200306 : DCC
            @Override
            public void onClick(View view) {
                if(cbDcc.isChecked()) //체크됨
                {
                    tvTax.setText("부가세");
                    tvBongsa.setText("봉사료");
                    tvHalbu.setText("할부");

                    cbCup.setChecked(false);
                    cbPoint.setChecked(false);
                    cbMem.setChecked(false);
                }
            }
        });

        btnEtclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 초기화 버튼 클릭되었습니다.");
                cbCup.setChecked(false);
                etMoney.setText("");
                etHalbu.setText("");
                etTax.setText("");
                etBongsa.setText("");
                etBal.setText("");
                etMinm.setText("");
                etApprno.setText("");
                etApprdate.setText("");
                etRecvmsg.setText("");
                cbPoint.setChecked(false);
                cbMem.setChecked(false); //20200131 : 멤버십거래
                cbDcc.setChecked(false); //20200306 : DCC
                etDcc1.setText("");
                etDcc2.setText("");
                etDcc3.setText("");
                etTxt.setText(""); //20200129
                etDevicegb.setText(""); //20200129
                etApprtp.setText(""); //20200129
                etFiller2.setText(""); //20200131 : 멤버쉽거래
                etP1.setText(""); //20200131 : 멤버쉽거래
                etP2.setText(""); //20200131 : 멤버쉽거래
                etP3.setText(""); //20200131 : 멤버쉽거래
            }
        });

        btReqcard.setOnClickListener(new View.OnClickListener() {
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

                mCatid = mSharedManager.getPreferences().getString("Catid", "");
                if (mCatid.length() != 10) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID 길이 10자리가 아닙니다.");
                    Toast.makeText(getContext(), "CATID 길이 10자리가 아닙니다.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] CATID : " + mCatid);

                if (etMoney.getText().length() == 0 || etMoney.getText().length() > 12) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 금액을 입력해주세요.");
                    Toast.makeText(getContext(), "금액을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    return;
                } else {
                    mMoney = String.format("%012d", Long.parseLong(etMoney.getText().toString()));
                }
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 금액 : " + mMoney);

                if(cbMem.isChecked()) //20200131 : 멤버쉽거래
                {
                    if(etHalbu.getText().length() > 16)
                        mHalbu = "                ";
                    else
                        mHalbu = String.format("%-16s", etHalbu.getText().toString());
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 비밀번호 : " + mHalbu);
                }
                else {
                    if (etHalbu.getText().length() == 0 || etHalbu.getText().length() > 2) {
                        mHalbu = "00";
                    } else {
                        mHalbu = String.format("%02d", Long.parseLong(etHalbu.getText().toString()));
                    }
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 할부 : " + mHalbu);
                }

                if(cbMem.isChecked()) //20200131 : 멤버쉽거래
                {
                    if(etTax.getText().length() != 2)
                        mTax = "  ";
                    else
                        mTax = etTax.getText().toString();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 적립구분 : " + mTax);
                }
                else {
                    if (etTax.getText().length() == 0) {
                        mTax = "000000000000";
                    } else if (etTax.getText().length() > 12) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 부가세를 잘못 입력했습니다.");
                        Toast.makeText(getContext(), "부가세를 잘못 입력했습니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    } else {
                        mTax = String.format("%012d", Long.parseLong(etTax.getText().toString()));
                    }
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 부가세 : " + mTax);
                }

                if(cbMem.isChecked()) //20200131 : 멤버쉽거래
                {
                    if(etBongsa.getText().length() != 2)
                        mBongsa = "  ";
                    else
                        mBongsa = etBongsa.getText().toString();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 포인트구분 : " + mBongsa);
                }
                else {
                    if (etBongsa.getText().length() == 0) {
                        mBongsa = "000000000000";
                    } else if (etBongsa.getText().length() > 12) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 봉사료를 잘못 입력했습니다.");
                        Toast.makeText(getContext(), "봉사료를 잘못 입력했습니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    } else {
                        mBongsa = String.format("%012d", Long.parseLong(etBongsa.getText().toString()));
                    }
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 봉사료 : " + mBongsa);
                }

                mHwnum = mSharedManager.getPreferences().getString("HWNUM", "################"); //LJY20220816 : 디폴트값 설정
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

                if (cbPoint.isChecked() || cbMem.isChecked()) //20200131 : 멤버쉽거래// 20200129 : 포인트거래
                {
                    if(etTxt.getText().length() != 3)
                        mTxt = "HPS";
                    else
                        mTxt = etTxt.getText().toString();

                    if(etDevicegb.getText().length() != 2)
                        mDevicegb = "H1";
                    else
                        mDevicegb = etDevicegb.getText().toString();

                    if(etApprtp.getText().length() != 2) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 거래유형을 확인해주세요.");
                        Toast.makeText(getContext(), "거래유형을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }
                    mApprtp = etApprtp.getText().toString();
                }
                else if(cbDcc.isChecked()) //20200306 : DCC
                {
                    mTxt = "DCC";
                    mDevicegb = "H1";
                    mApprtp = "10";

                    if(etDcc1.getText().length() != 3)
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 통화코드를 확인해주세요.");
                        Toast.makeText(getContext(), "통화코드를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }
                    mDcc1 = etDcc1.getText().toString(); //베이스통화코드

                    if(etDcc2.getText().length() != 1)
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 거래금액소수점을 확인해주세요.");
                        Toast.makeText(getContext(), "거래금액소수점을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }
                    mDcc2 = etDcc2.getText().toString(); //베이스통화코드

                    mBaseamount = String.format("%014d", Long.parseLong(etMoney.getText().toString()));
                }

                if(cbMem.isChecked()) //20200131 : 멤버쉽거래
                {
                    if(etFiller2.getText().length() > 32)
                        mFiller2 = "                                ";
                    else
                        mFiller2 = String.format("%-32s", etFiller2.getText().toString());
                }


                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 카드리딩 요청입니다.");

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

                if ((mSharedManager.getPreferences().getInt("Readertype", 0) == 3) && (isrun == false)) //POSBANK
                {
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
//                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
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

                    writeBuffer = new char[44];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x39; //Length(2)
                    String sendstr = mTimeout; //Card 대기시간(2)
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                    String strDate = sdfDate.format(new Date());
                    sendstr = sendstr + strDate; //거래일시(14)
                    sendstr = sendstr + mMoney; //거래금액(12)
                    sendstr = sendstr + mCatid; //TID(10)
                    sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                    temp = new byte[44];
                    for (int i = 0; i < 44; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    scr.sendMsg(temp, temp.length);
                    PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");

                    scr.clearRxBuffer();
                    handlerThread = new handler_thread(handler);
                    handlerThread.start();
                } else if ((mSharedManager.getPreferences().getInt("Readertype", 0) == 2) && (isrun == false)) //OKPOS
                {
                    mUart = new libUart();
                    isrun = true;

                    Arrays.fill(RECVBuf, (char) 0x00);
                    Arrays.fill(encdata, (char) 0x00);
                    Arrays.fill(icdata, (char) 0x00);

                    initSerial();
//                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                    btnDisable();

                    writeBuffer = new char[44];
                    writeBuffer[0] = 0x02; //Header ID
                    writeBuffer[1] = func_code; //Command ID
                    writeBuffer[2] = 0x00;
                    writeBuffer[3] = 0x39; //Length(2)
                    String sendstr = mTimeout; //Card 대기시간(2)
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                    String strDate = sdfDate.format(new Date());
                    sendstr = sendstr + strDate; //거래일시(14)
                    sendstr = sendstr + mMoney; //거래금액(12)
                    sendstr = sendstr + mCatid; //TID(10)
                    sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                    temp = new byte[44];
                    for (int i = 0; i < 44; i++) {
                        temp[i] = (byte) writeBuffer[i];
                    }
                    PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");

                    //LJY20201217 : 포트번호/통신속도 가변
                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                    }
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

                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                            isrun = true;

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();

                            Arrays.fill(RECVBuf, (char) 0x00);
                            Arrays.fill(encdata, (char) 0x00);
                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
//                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                            btnDisable();

                            writeBuffer = new char[44];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = func_code; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x39; //Length(2)
                            String sendstr = mTimeout; //Card 대기시간(2)
                            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                            String strDate = sdfDate.format(new Date());
                            sendstr = sendstr + strDate; //거래일시(14)
                            sendstr = sendstr + mMoney; //거래금액(12)
                            sendstr = sendstr + mCatid; //TID(10)
                            sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                            writeBuffer[43] = xor_sum(writeBuffer, 43);

                            temp = new byte[44];
                            for (int i = 0; i < 44; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");
                            usbService.write(temp);
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");  //OSM20250902 : 중복요청 리턴코드 수정
                            Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        return;
                    }
                }
            }
        });

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { //서명패드 결과
            if (resultCode == RESULT_OK) { //SignPad의 RESULT_OK
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 OK 클릭");
                PopupClose();

                if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 서명 결제");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    PopupOpen(getContext(), "VAN 승인 중입니다.");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    PosClient posClient = new PosClient();
                    //LJY20250904 : 서명 일부분 짤리는 부분 수정 (전문 길이 수정)
                    temp = new byte[4096];
                    if (cbCup.isChecked() == true) //은련
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련승인");
                        sendBuff = ("1521CUP" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                    } else {
                        if (cbPoint.isChecked()) { //20200129 : 포인트거래
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 포인트승인");
                            sendBuff = ("1521" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                        else if(cbDcc.isChecked()) { //20200306 : DCC
                            if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK TAX or DCA");
                                System.arraycopy("1521".getBytes(), 0, sendBuff, 0, 4);
                                System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 368, 2);
                                System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK DCC");
                                signBuff = data.getByteArrayExtra("SIGN");
                                sendBuff = ("1521" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 신용승인");
                            sendBuff = ("1521HPS" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(data.getByteArrayExtra("SIGN"), 2, temp, sendBuff.length, data.getByteArrayExtra("SIGN").length - 2);

                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0)
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113

                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    PopupClose();
                    InsertRecv(recvBuff);
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 서명 결제");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    PopupOpen(getContext(), "VAN 승인 중입니다.");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    PosClient posClient = new PosClient();
                    //LJY20250904 : 서명 일부분 짤리는 부분 수정 (전문 길이 수정)
                    //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                    temp = new byte[4096];
                    if (cbCup.isChecked()) //은련
                    {
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련승인");
                            sendBuff = ("1521PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                        else if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련승인(동반위)");
                            sendBuff = ("1778CUP" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련승인");
                            sendBuff = ("1778CUP" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련승인");
                            sendBuff = ("1521CUP" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                        }
                    } else {
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            mTxt = "PRO";
                            if (cbPoint.isChecked()) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 포인트승인");
                                sendBuff = ("1521" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) {
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO TAX or DCA");
                                    System.arraycopy("1521PRO".getBytes(), 0, sendBuff, 0, 7);
                                    System.arraycopy("L37", 0, sendBuff, 55, 3);
                                    System.arraycopy(icdata, 0, sendBuff, 58, 125);
//                                    System.arraycopy(icdata, 0, sendBuff, 368, 2);
                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO DCC");
                                    signBuff = data.getByteArrayExtra("SIGN");
                                    sendBuff = ("1521" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 신용승인");
                                sendBuff = ("1521" + mTxt + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        } else if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            if (cbPoint.isChecked()) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 포인트승인(동반위)");
                                sendBuff = ("1778" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF TAX or DCA(동반위)");
                                    System.arraycopy("1778".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(encdata, 0, sendBuff, 56, 127);
                                    System.arraycopy( ("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF DCC(동반위)");
                                    signBuff = data.getByteArrayExtra("SIGN");
                                    sendBuff = ("1778" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 신용승인(동반위)");
                                sendBuff = ("1778HPS" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 포인트승인");
                                sendBuff = ("1778" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC TAX or DCA");
                                    System.arraycopy("1778".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(encdata, 0, sendBuff, 56, 127);
//                                    System.arraycopy(icdata, 0, sendBuff, 368, 2);
                                    System.arraycopy( ("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC DCC");
                                    signBuff = data.getByteArrayExtra("SIGN");
                                    sendBuff = ("1778" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 신용승인");
                                sendBuff = ("1778HPS" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }else {
                            if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 포인트승인");
                                sendBuff = ("1521" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS TAX or DCA");
                                    System.arraycopy("1521".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(encdata, 0, sendBuff, 56, 127);
//                                    System.arraycopy(icdata, 0, sendBuff, 368, 2);
                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS DCC");
                                    signBuff = data.getByteArrayExtra("SIGN");
                                    sendBuff = ("1521" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 신용승인");
                                sendBuff = ("1521HPS" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y10801" + mCatid + "                       ").getBytes();
                            }
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(data.getByteArrayExtra("SIGN"), 2, temp, sendBuff.length, data.getByteArrayExtra("SIGN").length - 2);
                    if (Paygb[0] == 'I' || (Paygb[0] == 'R' && CardBrand[0] == 'K') || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) //LJY20230818 //LJY20200713 : 동반위 JUST TOUCH
                        System.arraycopy(new String(icdata).getBytes(), 0, temp, sendBuff.length + data.getByteArrayExtra("SIGN").length - 2, new String(icdata).length());

                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0)
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113

                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    PopupClose();
                    InsertRecv(recvBuff);
                }
            } else {//SignPad의 RESULT_CANCEL
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 취소 클릭");
                PopupClose();

                if (mSharedManager.getPreferences().getBoolean("Nosign", false)) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 오류시 결제됩니다.");

                    if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 오류시 FALLBACK 결제");

                        String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                        PopupOpen(getContext(), "VAN 승인 중입니다.");

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        PosClient posClient = new PosClient();
                        if (cbCup.isChecked() == true) //은련
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련승인");
                            sendBuff = ("0437CUP" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                        } else {
                            if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 포인트승인");
                                sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - TAX or DCA");
                                    System.arraycopy("0437".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 368, 2);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - DCC");
                                    sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 신용승인");
                                sendBuff = ("0437HPS" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                            }
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
                            recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113

                            iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                        }
                        PopupClose();
                        InsertRecv(recvBuff);
                    } else if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 오류시 IC 결제");

                        String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                        PopupOpen(getContext(), "VAN 승인 중입니다.");

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        PosClient posClient = new PosClient();
                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (cbCup.isChecked() == true) //은련
                        {
                            if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련승인");
                                sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else
                            if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련승인(동반위)");
                                sendBuff = ("0694CUP" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            }
                            else
                            if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련승인");
                                sendBuff = ("0694CUP" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련승인");
                                sendBuff = ("0437CUP" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            }
                        } else {
                            if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                mTxt = "PRO";
                                if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 포인트승인");
                                    sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                                else if(cbDcc.isChecked()) { //20200306 : DCC
                                    if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - TAX or DCA");
                                        System.arraycopy("0437PRO".getBytes(), 0, sendBuff, 0, 7);
                                        System.arraycopy("L".getBytes(), 0, sendBuff, 55, 1);
                                        System.arraycopy(space.getBytes(), 0, sendBuff, 56, 127);
                                        System.arraycopy("37".getBytes(), 0, sendBuff, 56, 2);
                                        System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 58, 127-2);
//                                        System.arraycopy(new String(icdata), 0, sendBuff, 421, 257);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - DCC");
                                        sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                    }
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 신용승인");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            } else
                            if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 포인트승인(동반위)");
                                    sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                                else if(cbDcc.isChecked()) { //20200306 : DCC
                                    if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - TAX or DCA(동반위)");
                                        System.arraycopy("0694".getBytes(), 0, sendBuff, 0, 4);
                                        System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                        System.arraycopy(new String(icdata), 0, sendBuff, 421, 257);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - DCC(동반위)");
                                        sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                    }
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 신용승인(동반위)");
                                    sendBuff = ("0694HPS" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            }
                            else
                            if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 포인트승인");
                                    sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                                else if(cbDcc.isChecked()) { //20200306 : DCC
                                    if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - TAX or DCA");
                                        System.arraycopy("0694".getBytes(), 0, sendBuff, 0, 4);
                                        System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                        System.arraycopy(new String(icdata), 0, sendBuff, 421, 257);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - DCC");
                                        sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                    }
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 신용승인");
                                    sendBuff = ("0694HPS" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            }else {
                                if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 포인트승인");
                                    sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                                else if(cbDcc.isChecked()) { //20200306 : DCC
                                    if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - TAX or DCA");
                                        System.arraycopy("0437".getBytes(), 0, sendBuff, 0, 4);
                                        System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
//                                        System.arraycopy(new String(icdata), 0, sendBuff, 421, 257);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - DCC");
                                        sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                    }
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 신용승인");
                                    sendBuff = ("0437HPS" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            }
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
                            recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113

                            iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                        }
                        PopupClose();
                        InsertRecv(recvBuff);
                    }
                } else {
                    btnEtclear.performClick();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명 취소 하셨습니다.");
                    Toast.makeText(getContext(), "서명 취소 하셨습니다.", Toast.LENGTH_LONG).show();
                    btnEnable();
                    return ;
                }
            }
        } else if (requestCode == 2) //은련 PIN
        {
            if (resultCode == RESULT_OK) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 은련PIN OK 버튼 클릭");
                PopupClose();

                bEncPin = new byte[16];
                SignEnc nicesign = new SignEnc();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN : " + data.getStringExtra("RESULT"));

                if (data.getStringExtra("RESULT").length() > 0) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN LENGTH OK!!");
                    int ret = nicesign.MakePinBlock("0000000000000000".getBytes(), data.getStringExtra("RESULT").getBytes(), bEncPin);

                    if (ret > 0) {
                        EncPin = new String(bEncPin);
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] ENC PIN : " + EncPin);

                        if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용
                            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 서명");

                                //LJY20220427 : 서명 연동
                                if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                                {
                                    if (usbService != null) { // if UsbService was correctly binded, Send data
                                        if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                            isrun = true;

                                            handlerThread = new handler_thread(handler);
                                            handlerThread.start();

                                            Arrays.fill(RECVBuf, (char) 0x00);
                                            //Arrays.fill(encdata, (char) 0x00);
                                            //Arrays.fill(icdata, (char) 0x00);

                                            initSerial();
                                            //isMultipad = true;
                                            btnDisable();
                                            isSign = true;

                                            writeBuffer = new char[53];
                                            writeBuffer[0] = 0x02; //Header ID
                                            writeBuffer[1] = 0x42; //Command ID
                                            writeBuffer[2] = 0x00;
                                            writeBuffer[3] = 0x48; //Length(2)
                                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                                            temp = new byte[53];
                                            for (int i = 0; i < 53; i++) {
                                                temp[i] = (byte) writeBuffer[i];
                                            }
                                            PopupOpenEOT(getContext(), "서명 해주세요.");
                                            usbService.write(temp);
                                        }
                                        else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");        //OSM20250902 : 중복요청 리턴코드 수정
                                            Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                        btnEnable();
                                        PopupClose();
                                        return;
                                    }
                                }
                                else
                                    //LJY20201005 : OKPOS 서명 연동
                                    if ((mSharedManager.getPreferences().getInt("Readertype", 0) == 2) && (isrun == false)) //OKPOS
                                    {
                                        mUart = new libUart();
                                        isrun = true;

                                        Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        isSign = true;
                                        btnDisable();

                                        writeBuffer = new char[53];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = 0x42; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x48; //Length(2)
                                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                                        temp = new byte[53];
                                        for (int i = 0; i < 53; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        PopupOpenEOT(getContext(), "서명 해주세요.");

                                        //LJY20201217 : 포트번호/통신속도 가변
                                        mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                            mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                        }
                                        mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();
                                    }
                                    else {
                                        Intent intent = new Intent(getContext(), SignPad.class);
                                        startActivityForResult(intent, 1);
                                    }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 노서명");

                                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                                PopupOpen(getContext(), "VAN 승인 중입니다.");

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                PosClient posClient = new PosClient();
                                //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                                if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련승인");
                                    sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                                else if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련승인(동반위)");
                                    sendBuff = ("0694CUP" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                                else
                                if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련승인");
                                    sendBuff = ("0694CUP" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련승인");
                                    sendBuff = ("0437CUP" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
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
                                PopupClose();
                                InsertRecv(recvBuff);
                            }
                        } else if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 서명");

                                //LJY20220427 : 서명 연동
                                if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                                {
                                    if (usbService != null) { // if UsbService was correctly binded, Send data
                                        if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                            isrun = true;

                                            handlerThread = new handler_thread(handler);
                                            handlerThread.start();

                                            Arrays.fill(RECVBuf, (char) 0x00);
                                            //Arrays.fill(encdata, (char) 0x00);
                                            //Arrays.fill(icdata, (char) 0x00);

                                            initSerial();
                                            //isMultipad = true;
                                            btnDisable();
                                            isSign = true;

                                            writeBuffer = new char[53];
                                            writeBuffer[0] = 0x02; //Header ID
                                            writeBuffer[1] = 0x42; //Command ID
                                            writeBuffer[2] = 0x00;
                                            writeBuffer[3] = 0x48; //Length(2)
                                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                                            temp = new byte[53];
                                            for (int i = 0; i < 53; i++) {
                                                temp[i] = (byte) writeBuffer[i];
                                            }
                                            PopupOpenEOT(getContext(), "서명 해주세요.");
                                            usbService.write(temp);
                                        }
                                        else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");        //OSM20250902 : 중복요청 리턴코드 수정
                                            Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                        btnEnable();
                                        PopupClose();
                                        return;
                                    }
                                }
                                else
                                    //LJY20201005 : OKPOS 서명 연동
                                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                                    {
                                        mUart = new libUart();
                                        isrun = true;

                                        Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        isSign = true;
                                        btnDisable();

                                        writeBuffer = new char[53];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = 0x42; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x48; //Length(2)
                                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                                        temp = new byte[53];
                                        for (int i = 0; i < 53; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        PopupOpenEOT(getContext(), "서명 해주세요.");

                                        //LJY20201217 : 포트번호/통신속도 가변
                                        mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                            mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                        }
                                        mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();
                                    }
                                    else {
                                        Intent intent = new Intent(getContext(), SignPad.class);
                                        startActivityForResult(intent, 1);
                                    }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 노서명");

                                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                                PopupOpen(getContext(), "VAN 승인 중입니다.");

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                PosClient posClient = new PosClient();
                                sendBuff = ("0437CUP" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
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
                                PopupClose();
                                InsertRecv(recvBuff);
                            }
                        }
                    } else {
                        btnEtclear.performClick();
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN 암호화 실패");
                        Toast.makeText(getContext(), "PIN 암호화 실패", Toast.LENGTH_LONG).show();
                        btnEnable();
                    }
                } else {
                    btnEtclear.performClick();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 입력된 PIN 데이터가 없습니다.");
                    Toast.makeText(getContext(), "입력된 PIN 데이터가 없습니다.", Toast.LENGTH_LONG).show();
                    btnEnable();
                }
            } else {
                btnEtclear.performClick();
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN 입력 취소하셨습니다.");
                Toast.makeText(getContext(), "PIN 입력 취소하셨습니다.", Toast.LENGTH_LONG).show();
                btnEnable();
                return ;
            }
        }
    }

    private void CUPfunc() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 은련PIN OK 버튼 클릭");
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PIN : " + EncPin);
        PopupClose();

        if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용

            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) {
                //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                if (Paygb[0] == 'B' && cMediagb[0] == 'B' && !IsBarcodeSign(new String(icdata))) {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련 노서명");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    PopupOpen(getContext(), "VAN 승인 중입니다.");

                    PosClient posClient = new PosClient();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련승인");
                    sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();

                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));

                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    PopupClose();
                    InsertRecv(recvBuff);
                }
                else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 서명");
                    //LJY20220427 : 서명 연동
                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false)) {
                        if (usbService != null) { // if UsbService was correctly binded, Send data
                            if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                isrun = true;

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();

                                Arrays.fill(RECVBuf, (char) 0x00);
                                //Arrays.fill(encdata, (char) 0x00);
                                //Arrays.fill(icdata, (char) 0x00);

                                initSerial();
                                //isMultipad = true;
                                btnDisable();
                                isSign = true;

                                writeBuffer = new char[53];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = 0x42; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x48; //Length(2)
                                String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                writeBuffer[52] = xor_sum(writeBuffer, 52);

                                temp = new byte[53];
                                for (int i = 0; i < 53; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(getContext(), "서명 해주세요.");
                                usbService.write(temp);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");        //OSM20250829 : 중복요청 리턴코드 수정
                                Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                            Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                            btnEnable();
                            PopupClose();
                            return;
                        }
                    } else
                        //LJY20201005 : OKPOS 서명 연동
                        if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                        {
                            mUart = new libUart();
                            isrun = true;

                            Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            isSign = true;
                            btnDisable();

                            writeBuffer = new char[53];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x42; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x48; //Length(2)
                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                            temp = new byte[53];
                            for (int i = 0; i < 53; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(getContext(), "서명 해주세요.");

                            //LJY20201217 : 포트번호/통신속도 가변
                            mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                            }
                            mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();
                        } else {
                            Intent intent = new Intent(getContext(), SignPad.class);
                            startActivityForResult(intent, 1);
                        }
                }
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련 노서명");

                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                PopupOpen(getContext(), "VAN 승인 중입니다.");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                PosClient posClient = new PosClient();
                //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련승인");
                    sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                } else
                if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련승인(동반위)");
                    sendBuff = ("0694CUP" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                }
                else
                if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련승인");
                    sendBuff = ("0694CUP" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                } else {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련승인");
                    sendBuff = ("0437CUP" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                }
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
                PopupClose();
                InsertRecv(recvBuff);
            }
        } else if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
            if (mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 서명");

                //LJY20220427 : 서명 연동
                if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                {
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                            isrun = true;

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();

                            Arrays.fill(RECVBuf, (char) 0x00);
                            //Arrays.fill(encdata, (char) 0x00);
                            //Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            //isMultipad = true;
                            btnDisable();
                            isSign = true;

                            writeBuffer = new char[53];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x42; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x48; //Length(2)
                            String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                            writeBuffer[52] = xor_sum(writeBuffer, 52);

                            temp = new byte[53];
                            for (int i = 0; i < 53; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(getContext(), "서명 해주세요.");
                            usbService.write(temp);
                        }

                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250829 : 중복요청 리턴코드 수정
                            Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                        btnEnable();
                        PopupClose();
                        return;
                    }
                }
                else
                    //LJY20201005 : OKPOS 서명 연동
                    if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                    {
                        mUart = new libUart();
                        isrun = true;

                        Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                        initSerial();
                        isSign = true;
                        btnDisable();

                        writeBuffer = new char[53];
                        writeBuffer[0] = 0x02; //Header ID
                        writeBuffer[1] = 0x42; //Command ID
                        writeBuffer[2] = 0x00;
                        writeBuffer[3] = 0x48; //Length(2)
                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                        temp = new byte[53];
                        for (int i = 0; i < 53; i++) {
                            temp[i] = (byte) writeBuffer[i];
                        }
                        PopupOpenEOT(getContext(), "서명 해주세요.");

                        //LJY20201217 : 포트번호/통신속도 가변
                        mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                        if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                            mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                            mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                        }
                        mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                        mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                        handlerThread = new handler_thread(handler);
                        handlerThread.start();
                    }
                    else {
                        Intent intent = new Intent(getContext(), SignPad.class);
                        startActivityForResult(intent, 1);
                    }
            } else {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련 노서명");

                String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                PopupOpen(getContext(), "VAN 승인 중입니다.");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                PosClient posClient = new PosClient();
                sendBuff = ("0437CUP" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
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
                PopupClose();
                InsertRecv(recvBuff);
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

            if (bRelease == false)
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 시리얼데이터 : [" + new String(RECVBuf) + "]");

            if (isMultipad) {
                isMultipad = false;
                if(RECVBuf[0] == 0x04 || (RECVBuf[0] == 0x00 && RECVBuf[4] == 0xCD)) //LJY20220520 : CD추가 //EOT 수신
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN EOT 수신");
                    Toast.makeText(getContext(), "암호화 PIN EOT 수신", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    PopupClose();
                    return;
                }
                else
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 수신 정상");
                    EncPin = new String(RECVBuf, 4, 16);

                    if(String.format("%02X", RECVBuf[4] & 0xff).equals("9F"))
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 멀티패드 아닙니다.");
                        Toast.makeText(getContext(), "멀티패드 아닙니다.", Toast.LENGTH_SHORT).show();
                        btnEnable();
                        PopupClose();
                        return;
                    }

                    CUPfunc();
                    return;
                }
            }
            else if (isSign) { //LJY20201005 : OKPOS 서명 연동
                isSign = false;

                //LJY20220427 : 서명 연동 취소시 예외 처리
                if(RECVBuf[0] == 0x04 || (RECVBuf[0] == 0x00 && RECVBuf[4] == 0xCD)) //LJY20220520 : CD추가 //EOT 수신
                {
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 서명패드 EOT 수신");
                    Toast.makeText(getContext(), "서명패드 EOT 수신", Toast.LENGTH_SHORT).show();
                    btnEnable();
                    PopupClose();
                    return;
                }
//                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 0x42 : " + new String(RECVBuf));
                String SignData = "";
                for(int kk=2; kk<RECVBuf.length-2; kk++) {
                    SignData = SignData + String.format("%02X", RECVBuf[kk] & 0xff);
                }
                SignData = SignData.substring(0, 4) + SignData.substring(4, Integer.parseInt(SignData.substring(0, 4))*2+4);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SignData : " + SignData);

                if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                    String SignLen = String.format("%04d", Integer.parseInt(SignData.substring(0, 4)) + 34);
                    String TotalLen = String.format("%04d", 475 + Integer.parseInt(SignData.substring(0, 4)));

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 서명 결제");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    PopupOpen(getContext(), "VAN 승인 중입니다.");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    PosClient posClient = new PosClient();
                    temp = new byte[4096];
                    if (cbCup.isChecked() == true) //은련
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 은련승인");
                        sendBuff = (TotalLen + "CUP" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                    } else {
                        if (cbPoint.isChecked()) { //20200129 : 포인트거래
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 포인트승인");
                            sendBuff = (TotalLen + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        }
                        else if(cbDcc.isChecked()) { //20200306 : DCC
                            if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK TAX or DCA");
                                System.arraycopy(TotalLen.getBytes(), 0, sendBuff, 0, 4);
                                System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 368, 2);
                                System.arraycopy(("Y" + SignLen + "1" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK DCC");
                                signBuff = stringTobytes(SignData);
                                sendBuff = (TotalLen + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 신용승인");
                            sendBuff = (TotalLen + "HPS" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(stringTobytes(SignData), 2, temp, sendBuff.length, stringTobytes(SignData).length - 2);

                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    PopupClose();
                    InsertRecv(recvBuff);
                }
                else {
                    String SignLen = String.format("%04d", Integer.parseInt(SignData.substring(0, 4)) + 34);
                    String TotalLen = String.format("%04d", 475 + 257 + Integer.parseInt(SignData.substring(0, 4)));
                    String TotalLenSwipe = String.format("%04d", 475 + Integer.parseInt(SignData.substring(0, 4)));

                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 서명 결제");

                    String strDate = new SimpleDateFormat("MMddHHmmss").format(new Date());
                    PopupOpen(getContext(), "VAN 승인 중입니다.");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    PosClient posClient = new PosClient();
                    temp = new byte[4096];
                    if (cbCup.isChecked()) //은련
                    {
                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 은련승인");
                            sendBuff = (TotalLenSwipe + "PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        } else
                        if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 은련승인(동반위)");
                            sendBuff = (TotalLen + "CUP" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 은련승인");
                            sendBuff = (TotalLen + "CUP" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 은련승인");
                            sendBuff = (TotalLenSwipe + "CUP" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                   " + EncPin + "                                                  " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                        }
                    } else {
                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            mTxt = "PRO";
                            if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 포인트승인");
                                sendBuff = (TotalLenSwipe + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO TAX or DCA");
                                    System.arraycopy(TotalLenSwipe.getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(mTxt.getBytes(), 0, sendBuff, 4, 7);
                                    System.arraycopy("L".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(space.getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy("37".getBytes(), 0, sendBuff, 56, 2);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 58, 127-2);
//                                    System.arraycopy(icdata, 0, sendBuff, 368, 2);
                                    System.arraycopy(("Y" + SignLen + "1" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO DCC");
                                    signBuff = stringTobytes(SignData);
                                    sendBuff = (TotalLenSwipe + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO 신용승인");
                                sendBuff = (TotalLenSwipe + "HPS" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        } else
                        if(Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            if (cbPoint.isChecked()) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 포인트승인(동반위)");
                                sendBuff = (TotalLen + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF TAX or DCA(동반위)");
                                    System.arraycopy(TotalLen.getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(encdata, 0, sendBuff, 56, 127);
                                    System.arraycopy( ("Y" + SignLen + "1" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF DCC(동반위)");
                                    signBuff = stringTobytes(SignData);
                                    sendBuff = (TotalLen + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF 신용승인(동반위)");
                                sendBuff = (TotalLen + "HPS" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        }
                        else
                        if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 포인트승인");
                                sendBuff = (TotalLen + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC TAX or DCA");
                                    System.arraycopy(TotalLen.getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(encdata, 0, sendBuff, 56, 127);
//                                    System.arraycopy(icdata, 0, sendBuff, 368, 2);
                                    System.arraycopy( ("Y" + SignLen + "1" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC DCC");
                                    signBuff = stringTobytes(SignData);
                                    sendBuff = (TotalLen + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 신용승인");
                                sendBuff = (TotalLen + "HPS" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        }else {
                            if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 포인트승인");
                                sendBuff = (TotalLenSwipe + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                            else if(cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS TAX or DCA");
                                    System.arraycopy(TotalLenSwipe.getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(encdata, 0, sendBuff, 56, 127);
//                                    System.arraycopy(icdata, 0, sendBuff, 368, 2);
                                    System.arraycopy(("Y" + SignLen + "1" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS DCC");
                                    signBuff = stringTobytes(SignData);
                                    sendBuff = (TotalLenSwipe + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                                }
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS 신용승인");
                                sendBuff = (TotalLenSwipe + "HPS" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "Y" + SignLen + "1" + mCatid + "                       ").getBytes();
                            }
                        }
                    }
                    System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                    System.arraycopy(stringTobytes(SignData), 2, temp, sendBuff.length, stringTobytes(SignData).length - 2);
                    if (Paygb[0] == 'I' || (Paygb[0] == 'R' && CardBrand[0] == 'K') || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) //LJY20230818 //LJY20200713 : 동반위 JUST TOUCH
                        System.arraycopy(new String(icdata).getBytes(), 0, temp, sendBuff.length + stringTobytes(SignData).length - 2, new String(icdata).length());

                    if (bRelease)
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(temp)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(temp)).substring(183, temp.length - 183));
                    else
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(temp));
                    mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                    if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                        System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                        recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                    } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                        recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                    }
                    else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                        iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                    }
                    PopupClose();
                    InsertRecv(recvBuff);
                }
            }
            else if (func_code == 0x6E || func_code == 0x9E) { //LJY20250904 : 8BIN/통합결제 적용
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 에러코드 : " + errcode);

                if (errcode.equals("00")) { //FALLBACK 카드리딩 정상
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

                    if (cbCup.isChecked()) //은련PIN
                    {
                        if(mSharedManager.getPreferences().getInt("Readertype", 0) == 1) //멀티패드
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 은련");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 요청입니다.");

                            if(!SharedManager.isBizdown)
                            {
                                //LJY20200812 : 가맹점다운로드 예외처리
//                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                                Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                                btnEnable();
//                                return;
                            }
                            if (SharedManager.isStatus == false) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                btnEnable();
                                return;
                            }

                            if (usbService != null) { // if UsbService was correctly binded, Send data
                                if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                    isrun = true;

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();

                                    Arrays.fill(RECVBuf, (char) 0x00);
//                                Arrays.fill(encdata, (char) 0x00);
//                                Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
                                    isMultipad = true;
                                    btnDisable();

                                    writeBuffer = new char[37];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = 0x43; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x32; //Length(2)
                                    writeBuffer[4] = 0x20;
                                    writeBuffer[5] = 0x20;
                                    writeBuffer[6] = 0x20;
                                    writeBuffer[7] = 0x20;
                                    writeBuffer[8] = 0x20;
                                    writeBuffer[9] = 0x20;
                                    writeBuffer[10] = 0x20;
                                    writeBuffer[11] = 0x20;
                                    writeBuffer[12] = 0x20;
                                    writeBuffer[13] = 0x20;
                                    writeBuffer[14] = 0x20;
                                    writeBuffer[15] = 0x20;
                                    writeBuffer[16] = 0x20;
                                    writeBuffer[17] = 0x20;
                                    writeBuffer[18] = 0x20;
                                    writeBuffer[19] = 0x20;
                                    writeBuffer[20] = 0x30;
                                    writeBuffer[21] = 0x30;
                                    writeBuffer[22] = 0x30;
                                    writeBuffer[23] = 0x30;
                                    writeBuffer[24] = 0x30;
                                    writeBuffer[25] = 0x30;
                                    writeBuffer[26] = 0x30;
                                    writeBuffer[27] = 0x30;
                                    writeBuffer[28] = 0x30;
                                    writeBuffer[29] = 0x30;
                                    writeBuffer[30] = 0x30;
                                    writeBuffer[31] = 0x30;
                                    writeBuffer[32] = 0x30;
                                    writeBuffer[33] = 0x30;
                                    writeBuffer[34] = 0x30;
                                    writeBuffer[35] = 0x30;
                                    writeBuffer[36] = 0x73;

                                    temp = new byte[37];
                                    for (int i = 0; i < 37; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(getContext(), "암호화 PIN 입력해주세요.");
                                    usbService.write(temp);
                                }
                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                    Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                btnEnable();
                                PopupClose();
                                return;
                            }
                        }
                        else if(mSharedManager.getPreferences().getInt("Readertype", 0) == 2) { //LJY20201005 : OKPOS 은련 PIN 연동
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 은련");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 요청입니다.");

                            mUart = new libUart();
                            isrun = true;

                            Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            isMultipad = true;
                            btnDisable();

                            writeBuffer = new char[37];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x43; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x32; //Length(2)
                            writeBuffer[4] = 0x20;
                            writeBuffer[5] = 0x20;
                            writeBuffer[6] = 0x20;
                            writeBuffer[7] = 0x20;
                            writeBuffer[8] = 0x20;
                            writeBuffer[9] = 0x20;
                            writeBuffer[10] = 0x20;
                            writeBuffer[11] = 0x20;
                            writeBuffer[12] = 0x20;
                            writeBuffer[13] = 0x20;
                            writeBuffer[14] = 0x20;
                            writeBuffer[15] = 0x20;
                            writeBuffer[16] = 0x20;
                            writeBuffer[17] = 0x20;
                            writeBuffer[18] = 0x20;
                            writeBuffer[19] = 0x20;
                            writeBuffer[20] = 0x30;
                            writeBuffer[21] = 0x30;
                            writeBuffer[22] = 0x30;
                            writeBuffer[23] = 0x30;
                            writeBuffer[24] = 0x30;
                            writeBuffer[25] = 0x30;
                            writeBuffer[26] = 0x30;
                            writeBuffer[27] = 0x30;
                            writeBuffer[28] = 0x30;
                            writeBuffer[29] = 0x30;
                            writeBuffer[30] = 0x30;
                            writeBuffer[31] = 0x30;
                            writeBuffer[32] = 0x30;
                            writeBuffer[33] = 0x30;
                            writeBuffer[34] = 0x30;
                            writeBuffer[35] = 0x30;
                            writeBuffer[36] = 0x73;

                            temp = new byte[37];
                            for (int i = 0; i < 37; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(getContext(), "암호화 PIN 입력해주세요.");

                            //LJY20201217 : 포트번호/통신속도 가변
                            mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                            }
                            mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();
                        }
                        else
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 은련 (PIN 사용 안됨)");
                            Toast.makeText(getContext(), "FALLBACK - 은련 (PIN 사용 안됨)", Toast.LENGTH_LONG).show();
                            btnEnable();
                            PopupClose();
                            return ; //TTA요청 : 은련터치 막아야 됨
                        }
                        return;
                    }

                    if ((mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) && (cbPoint.isChecked() && mApprtp.equals("10"))) { //20200129 : 포인트거래
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 서명");

                        //LJY20220427 : 서명 연동
                        if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                        {
                            if (usbService != null) { // if UsbService was correctly binded, Send data
                                if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                    isrun = true;

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();

                                    Arrays.fill(RECVBuf, (char) 0x00);
                                    //Arrays.fill(encdata, (char) 0x00);
                                    //Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
                                    //isMultipad = true;
                                    btnDisable();
                                    isSign = true;

                                    writeBuffer = new char[53];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = 0x42; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x48; //Length(2)
                                    String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                    writeBuffer[52] = xor_sum(writeBuffer, 52);

                                    temp = new byte[53];
                                    for (int i = 0; i < 53; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(getContext(), "서명 해주세요.");
                                    usbService.write(temp);
                                }

                                else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                    Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                btnEnable();
                                PopupClose();
                                return;
                            }
                        }
                        else
                            //LJY20201005 : OKPOS 서명 연동
                            if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                            {
                                mUart = new libUart();
                                isrun = true;

                                Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                                initSerial();
                                isSign = true;
                                btnDisable();

                                writeBuffer = new char[53];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = 0x42; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x48; //Length(2)
                                String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                writeBuffer[52] = xor_sum(writeBuffer, 52);

                                temp = new byte[53];
                                for (int i = 0; i < 53; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(getContext(), "서명 해주세요.");

                                //LJY20201217 : 포트번호/통신속도 가변
                                mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                    mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                    mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                }
                                mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();
                            }
                            else {
                                Intent intent = new Intent(getContext(), SignPad.class);
                                startActivityForResult(intent, 1);
                            }
                    }
                    else if ((mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) && (cbPoint.isChecked() == false && cbMem.isChecked() == false)) { //20200129 : 포인트거래
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 서명");
                        if (cbDcc.isChecked() && signBuff != null && (mTxt.equals("TAX") || mTxt.equals("DCA"))) { //20200313 : DCC 개발
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - TAX or DCA");
                            PopupOpen(getContext(), "VAN 승인 중입니다.");

                            PosClient posClient = new PosClient();
                            temp = new byte[4096];
                            System.arraycopy("1521".getBytes(), 0, sendBuff, 0, 4); //LJY20250904 : 서명 일부분 짤리는 부분 수정 (전문 길이 수정)
                            System.arraycopy("F".getBytes(), 0, sendBuff, 55, 1);
                            System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                            System.arraycopy(new String(icdata, 0, 2).getBytes(), 0, sendBuff, 368, 2);
                            System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);

                            System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                            System.arraycopy(signBuff, 2, temp, sendBuff.length, signBuff.length - 2);

                            if (bRelease)
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                            else
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                            mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                            if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                                recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                            } else if(mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                                recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                            }
                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                                iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                            }
                            PopupClose();
                            InsertRecv(recvBuff);
                        }
                        else {
                            //LJY20220427 : 서명 연동
                            if( mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false) )
                            {
                                if (usbService != null) { // if UsbService was correctly binded, Send data
                                    if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                        isrun = true;

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();

                                        Arrays.fill(RECVBuf, (char) 0x00);
                                        //Arrays.fill(encdata, (char) 0x00);
                                        //Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        //isMultipad = true;
                                        btnDisable();
                                        isSign = true;

                                        writeBuffer = new char[53];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = 0x42; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x48; //Length(2)
                                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                                        temp = new byte[53];
                                        for (int i = 0; i < 53; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        PopupOpenEOT(getContext(), "서명 해주세요.");
                                        usbService.write(temp);
                                    }

                                    else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                        Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                    Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                    btnEnable();
                                    PopupClose();
                                    return;
                                }
                            }
                            else
                                //LJY20201005 : OKPOS 서명 연동
                                if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                                {
                                    mUart = new libUart();
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
//                                Arrays.fill(encdata, (char) 0x00);
//                                Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
                                    isSign = true;
                                    btnDisable();

                                    writeBuffer = new char[53];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = 0x42; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x48; //Length(2)
                                    String sendstr = "19F316BA33A57729" + "                " + "                "; //서명문구(48)
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                    writeBuffer[52] = xor_sum(writeBuffer, 52);

                                    temp = new byte[53];
                                    for (int i = 0; i < 53; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(getContext(), "서명 해주세요.");

                                    //LJY20201217 : 포트번호/통신속도 가변
                                    mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                        mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                    }
                                    mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                    mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                }
                                else {
                                    Intent intent = new Intent(getContext(), SignPad.class);
                                    startActivityForResult(intent, 1);
                                }
                        }
                    } else {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - 노서명");
                        PopupOpen(getContext(), "VAN 승인 중입니다.");

                        PosClient posClient = new PosClient();
                        if(cbMem.isChecked()) //20200131 : 멤버쉽거래
                            sendBuff = ("0343" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "F" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + "            " + "      " + mFiller2 + mFiller + " ").getBytes();
                        else
                        if (cbPoint.isChecked()) //20200129 : 포인트거래
                            sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                        else if (cbDcc.isChecked()) { //20200306 : DCC
                            if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - TAX or DCA");
                                System.arraycopy("0437".getBytes(), 0, sendBuff, 0, 4);
                                System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                System.arraycopy(new String(icdata, 0, 2).getBytes(), 0, sendBuff, 368, 2);
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK - DCC");
                                sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();
                            }
                        }
                        else
                            sendBuff = ("0437HPS" + mCatid + strDate + "020010H1          " + mCatid + "F" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + new String(icdata, 0, 2) + mFiller + "N").getBytes();

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
                        PopupClose();
                        InsertRecv(recvBuff);
                    }
                } else {
                    Toast.makeText(getContext(), "[NVCAT] FALLBACK 에러코드 : " + errcode, Toast.LENGTH_SHORT).show();
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] FALLBACK 에러코드 : " + errcode);
                    btnEnable();
                    return;
                }
            } else if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용
                String errcode = String.format("%02X", RECVBuf[4] & 0xff);
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 에러코드 : " + errcode);

                if (errcode.equals("00")) { //IC 카드 리딩 정상
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

                    if(func_code == 0x6C)   System.arraycopy(RECVBuf, 180, icdata, 0, 257);
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

                    if (cbCup.isChecked() == true && (CardBrand[0] == 'C' && CardCvm[0] == '1')) //LJY20230713 : 은련PIN 체크 //은련PIN
                    {
                        if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1) //멀티패드
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 은련");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 요청입니다.");

                            if (!SharedManager.isBizdown) {
                                //LJY20200812 : 가맹점다운로드 예외처리
//                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                                Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                                btnEnable();
//                                return;
                            }
                            if (SharedManager.isStatus == false) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                btnEnable();
                                return;
                            }

                            if (usbService != null) { // if UsbService was correctly binded, Send data
                                if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                    isrun = true;

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();

                                    Arrays.fill(RECVBuf, (char) 0x00);
//                                Arrays.fill(encdata, (char) 0x00);
//                                Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
                                    isMultipad = true;
                                    btnDisable();

                                    writeBuffer = new char[37];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = 0x43; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x32; //Length(2)
                                    writeBuffer[4] = 0x20;
                                    writeBuffer[5] = 0x20;
                                    writeBuffer[6] = 0x20;
                                    writeBuffer[7] = 0x20;
                                    writeBuffer[8] = 0x20;
                                    writeBuffer[9] = 0x20;
                                    writeBuffer[10] = 0x20;
                                    writeBuffer[11] = 0x20;
                                    writeBuffer[12] = 0x20;
                                    writeBuffer[13] = 0x20;
                                    writeBuffer[14] = 0x20;
                                    writeBuffer[15] = 0x20;
                                    writeBuffer[16] = 0x20;
                                    writeBuffer[17] = 0x20;
                                    writeBuffer[18] = 0x20;
                                    writeBuffer[19] = 0x20;
                                    writeBuffer[20] = 0x30;
                                    writeBuffer[21] = 0x30;
                                    writeBuffer[22] = 0x30;
                                    writeBuffer[23] = 0x30;
                                    writeBuffer[24] = 0x30;
                                    writeBuffer[25] = 0x30;
                                    writeBuffer[26] = 0x30;
                                    writeBuffer[27] = 0x30;
                                    writeBuffer[28] = 0x30;
                                    writeBuffer[29] = 0x30;
                                    writeBuffer[30] = 0x30;
                                    writeBuffer[31] = 0x30;
                                    writeBuffer[32] = 0x30;
                                    writeBuffer[33] = 0x30;
                                    writeBuffer[34] = 0x30;
                                    writeBuffer[35] = 0x30;
                                    writeBuffer[36] = 0x73;

                                    temp = new byte[37];
                                    for (int i = 0; i < 37; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(getContext(), "암호화 PIN 입력해주세요.");
                                    usbService.write(temp);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                    Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                btnEnable();
                                PopupClose();
                                return;
                            }
                        } else if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) { //LJY20201005 : OKPOS 은련 PIN 연동
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 은련");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 암호화 PIN 요청입니다.");

                            mUart = new libUart();
                            isrun = true;

                            Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                            initSerial();
                            isMultipad = true;
                            btnDisable();

                            writeBuffer = new char[37];
                            writeBuffer[0] = 0x02; //Header ID
                            writeBuffer[1] = 0x43; //Command ID
                            writeBuffer[2] = 0x00;
                            writeBuffer[3] = 0x32; //Length(2)
                            writeBuffer[4] = 0x20;
                            writeBuffer[5] = 0x20;
                            writeBuffer[6] = 0x20;
                            writeBuffer[7] = 0x20;
                            writeBuffer[8] = 0x20;
                            writeBuffer[9] = 0x20;
                            writeBuffer[10] = 0x20;
                            writeBuffer[11] = 0x20;
                            writeBuffer[12] = 0x20;
                            writeBuffer[13] = 0x20;
                            writeBuffer[14] = 0x20;
                            writeBuffer[15] = 0x20;
                            writeBuffer[16] = 0x20;
                            writeBuffer[17] = 0x20;
                            writeBuffer[18] = 0x20;
                            writeBuffer[19] = 0x20;
                            writeBuffer[20] = 0x30;
                            writeBuffer[21] = 0x30;
                            writeBuffer[22] = 0x30;
                            writeBuffer[23] = 0x30;
                            writeBuffer[24] = 0x30;
                            writeBuffer[25] = 0x30;
                            writeBuffer[26] = 0x30;
                            writeBuffer[27] = 0x30;
                            writeBuffer[28] = 0x30;
                            writeBuffer[29] = 0x30;
                            writeBuffer[30] = 0x30;
                            writeBuffer[31] = 0x30;
                            writeBuffer[32] = 0x30;
                            writeBuffer[33] = 0x30;
                            writeBuffer[34] = 0x30;
                            writeBuffer[35] = 0x30;
                            writeBuffer[36] = 0x73;

                            temp = new byte[37];
                            for (int i = 0; i < 37; i++) {
                                temp[i] = (byte) writeBuffer[i];
                            }
                            PopupOpenEOT(getContext(), "암호화 PIN 입력해주세요.");

                            //LJY20201217 : 포트번호/통신속도 가변
                            mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                            }
                            mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                            mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                            handlerThread = new handler_thread(handler);
                            handlerThread.start();
                        } else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 은련 (PIN 사용 안됨)");
                            Toast.makeText(getContext(), "IC - 은련 (PIN 사용 안됨)", Toast.LENGTH_LONG).show();
                            btnEnable();
                            PopupClose();
                            return; //TTA요청 : 은련터치 막아야 됨
                        }
                        return;
                    }

                    if ((mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) && (cbPoint.isChecked() && mApprtp.equals("10"))) { //20200129 : 포인트거래
                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B' && !IsBarcodeSign(new String(icdata))) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 노서명");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAY구분 : " + Paygb[0]);
                            PopupOpen(getContext(), "VAN 승인 중입니다.");

                            PosClient posClient = new PosClient();
                            if (cbDcc.isChecked() && (mTxt.equals("TAX") || mTxt.equals("DCA"))) {
                            } else
                                sendBuff = null;

                            mTxt = "PRO";
                            if (cbMem.isChecked()) //20200131 : 멤버쉽거래
                            {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 멤버쉽승인");
                                sendBuff = ("0343" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "L37" + BarcodeToTrack2(new String(icdata)) + mMoney.substring(3, 12) + mHalbu + "            " + "      " + mFiller2 + mFiller + " ").getBytes();
                            } else if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 포인트승인");
                                sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else if (cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - TAX or DCA");
                                    System.arraycopy("0437PRO".getBytes(), 0, sendBuff, 0, 7);
                                    System.arraycopy("L".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(space.getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy("37".getBytes(), 0, sendBuff, 56, 2);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 58, 127 - 2);
//                                    System.arraycopy(icdata, 0, sendBuff, 421, icdata.length);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - DCC");
                                    sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 신용승인");
                                sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            }

                            if (bRelease)
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                            else
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                            mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                            if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                                recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                            } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113

                                recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                            } else {
                                byte[] recvcode = new byte[4];
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113

                                iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가

                                if (iresult == 1) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화 정상"); //OSM20250113
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "result code : " + String.valueOf(iresult));
                                    SharedManager.LogBinHex("RecvBuff", recvBuff);
                                    System.arraycopy(recvBuff, 55, recvcode, 0, recvBuff.length);
                                    SharedManager.LogDebug(bLogUse, "debugjy", "recv code : " + String.valueOf(recvcode));
                                }
                            }
                            PopupClose();
                            InsertRecv(recvBuff);
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 서명");

                            //LJY20220427 : 서명 연동
                            if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false)) {
                                if (usbService != null) { // if UsbService was correctly binded, Send data
                                    if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                        isrun = true;

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();

                                        Arrays.fill(RECVBuf, (char) 0x00);
                                        //Arrays.fill(encdata, (char) 0x00);
                                        //Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        //isMultipad = true;
                                        btnDisable();
                                        isSign = true;

                                        writeBuffer = new char[53];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = 0x42; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x48; //Length(2)
                                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                                        temp = new byte[53];
                                        for (int i = 0; i < 53; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        PopupOpenEOT(getContext(), "서명 해주세요.");
                                        usbService.write(temp);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                        Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                    Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                    btnEnable();
                                    PopupClose();
                                    return;
                                }
                            } else
                                //LJY20201005 : OKPOS 서명 연동
                                if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                                {
                                    mUart = new libUart();
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
                                    isSign = true;
                                    btnDisable();

                                    writeBuffer = new char[53];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = 0x42; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x48; //Length(2)
                                    String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                    writeBuffer[52] = xor_sum(writeBuffer, 52);

                                    temp = new byte[53];
                                    for (int i = 0; i < 53; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(getContext(), "서명 해주세요.");

                                    //LJY20201217 : 포트번호/통신속도 가변
                                    mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                        mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                    }
                                    mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                    mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else {
                                    Intent intent = new Intent(getContext(), SignPad.class);
                                    startActivityForResult(intent, 1);
                                }
                        }
                    } else if ((mSharedManager.getPreferences().getBoolean("Nocvm", false) == false || (mSharedManager.getPreferences().getBoolean("Nocvm", false) == true && Long.parseLong(etMoney.getText().toString()) > 50000)) && (cbPoint.isChecked() == false && cbMem.isChecked() == false)) { //20200129 : 포인트거래
                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B' && !IsBarcodeSign(new String(icdata))) {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 노서명");
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAY구분 : " + Paygb[0]);
                            PopupOpen(getContext(), "VAN 승인 중입니다.");

                            PosClient posClient = new PosClient();
                            if (cbDcc.isChecked() && (mTxt.equals("TAX") || mTxt.equals("DCA"))) {
                            } else
                                sendBuff = null;

                            mTxt = "PRO";
                            if (cbMem.isChecked()) //20200131 : 멤버쉽거래
                            {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 멤버쉽승인");
                                sendBuff = ("0343" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "L37" + BarcodeToTrack2(new String(icdata)) + mMoney.substring(3, 12) + mHalbu + "            " + "      " + mFiller2 + mFiller + " ").getBytes();
                            } else if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 포인트승인");
                                sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else if (cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - TAX or DCA");
                                    System.arraycopy("0437PRO".getBytes(), 0, sendBuff, 0, 7);
                                    System.arraycopy("L".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(space.getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy("37".getBytes(), 0, sendBuff, 56, 2);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 58, 127 - 2);
//                                    System.arraycopy(icdata, 0, sendBuff, 421, icdata.length);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - DCC");
                                    sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 신용승인");
                                sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            }

                            if (bRelease)
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                            else
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                            mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                            if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                                recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                            } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113

                                recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                            } else {
                                byte[] recvcode = new byte[4];
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113

                                iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가

                                if (iresult == 1) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화 정상"); //OSM20250113
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "result code : " + String.valueOf(iresult));
                                    SharedManager.LogBinHex("RecvBuff", recvBuff);
                                    System.arraycopy(recvBuff, 55, recvcode, 0, recvBuff.length);
                                    SharedManager.LogDebug(bLogUse, "debugjy", "recv code : " + String.valueOf(recvcode));
                                }
                            }
                            PopupClose();
                            InsertRecv(recvBuff);
                        }
                        else {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 서명");
                            if (cbDcc.isChecked() && signBuff != null && (mTxt.equals("TAX") || mTxt.equals("DCA"))) { //20200313 : DCC 개발
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAY구분 : " + Paygb[0]);
                                PopupOpen(getContext(), "VAN 승인 중입니다.");

                                PosClient posClient = new PosClient();
                                temp = new byte[4096];
                                //LJY20250904 : 서명 일부분 짤리는 부분 수정 (전문 길이 수정)
                                //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                                if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - TAX or DCA");
                                    System.arraycopy("1521PRO".getBytes(), 0, sendBuff, 0, 7);
                                    System.arraycopy("L".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(space.getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy("37".getBytes(), 0, sendBuff, 56, 2);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 58, 127 - 2);
//                                System.arraycopy(new String(icdata, 0, 2).getBytes(), 0, sendBuff, 368, 2);
                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else if (Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - TAX or DCA(동반위)");
                                    System.arraycopy("1778".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy("K".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - TAX or DCA");
                                    System.arraycopy("1778".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy("I".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
//                                System.arraycopy(new String(icdata, 0, 2).getBytes(), 0, sendBuff, 368, 2);
                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - TAX or DCA");
                                    System.arraycopy("1521".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy("A".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
//                                System.arraycopy(new String(icdata, 0, 2).getBytes(), 0, sendBuff, 368, 2);
                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                }

                                System.arraycopy(sendBuff, 0, temp, 0, sendBuff.length);
                                System.arraycopy(signBuff, 2, temp, sendBuff.length, signBuff.length - 2);

                                if (Paygb[0] == 'I' || (Paygb[0] == 'R' && CardBrand[0] == 'K') || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) //LJY20230818 //LJY20200713 : 동반위 JUST TOUCH
                                    System.arraycopy(new String(icdata).getBytes(), 0, temp, sendBuff.length + signBuff.length - 2, new String(icdata).length());

                                if (bRelease)
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "*******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                                else
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                                mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                                if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                                    System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, temp, 0, 4);
                                    recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), temp);
                                } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113
                                    recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), temp);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113
                                    iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), temp, recvBuff);   //OSM20250113 : DES암복호화 함수 추가
                                }
                                PopupClose();
                                InsertRecv(recvBuff);
                            }

                            //LJY20220427 : 서명 연동
                            else if (mSharedManager.getPreferences().getInt("Readertype", 0) == 1 && mSharedManager.getPreferences().getBoolean("Signuse", false)) {
                                if (usbService != null) { // if UsbService was correctly binded, Send data
                                    if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                        isrun = true;

                                        handlerThread = new handler_thread(handler);
                                        handlerThread.start();

                                        Arrays.fill(RECVBuf, (char) 0x00);
                                        //Arrays.fill(encdata, (char) 0x00);
                                        //Arrays.fill(icdata, (char) 0x00);

                                        initSerial();
                                        //isMultipad = true;
                                        btnDisable();
                                        isSign = true;

                                        writeBuffer = new char[53];
                                        writeBuffer[0] = 0x02; //Header ID
                                        writeBuffer[1] = 0x42; //Command ID
                                        writeBuffer[2] = 0x00;
                                        writeBuffer[3] = 0x48; //Length(2)
                                        String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                        System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                        writeBuffer[52] = xor_sum(writeBuffer, 52);

                                        temp = new byte[53];
                                        for (int i = 0; i < 53; i++) {
                                            temp[i] = (byte) writeBuffer[i];
                                        }
                                        PopupOpenEOT(getContext(), "서명 해주세요.");
                                        usbService.write(temp);
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                        Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                    Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_LONG).show();
                                    btnEnable();
                                    PopupClose();
                                    return;
                                }
                            }
                            //LJY20201005 : OKPOS 서명 연동
                            else if (mSharedManager.getPreferences().getInt("Readertype", 0) == 2) //OKPOS
                            {
                                mUart = new libUart();
                                isrun = true;

                                Arrays.fill(RECVBuf, (char) 0x00);
//                            Arrays.fill(encdata, (char) 0x00);
//                            Arrays.fill(icdata, (char) 0x00);

                                initSerial();
                                isSign = true;
                                btnDisable();

                                writeBuffer = new char[53];
                                writeBuffer[0] = 0x02; //Header ID
                                writeBuffer[1] = 0x42; //Command ID
                                writeBuffer[2] = 0x00;
                                writeBuffer[3] = 0x48; //Length(2)
                                String sendstr = "19F316BA33A57729" + "  Please Sign!  " + "                "; //서명문구(48)
                                System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 48);
                                writeBuffer[52] = xor_sum(writeBuffer, 52);

                                temp = new byte[53];
                                for (int i = 0; i < 53; i++) {
                                    temp[i] = (byte) writeBuffer[i];
                                }
                                PopupOpenEOT(getContext(), "서명 해주세요.");

                                //LJY20201217 : 포트번호/통신속도 가변
                                mUart.Init(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                if (mUart.IsOpen(mSharedManager.getPreferences().getInt("sPortnum", 0)) == false) {
                                    mUart.Open(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")), 8, 0, 1, true);
                                    mUart.SetBaudrate(mSharedManager.getPreferences().getInt("sPortnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("sBaudrateStr", "115200")));
                                }
                                mUart.QueueClear(mSharedManager.getPreferences().getInt("sPortnum", 0));
                                mUart.DataSend(mSharedManager.getPreferences().getInt("sPortnum", 0), temp, temp.length);

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();
                            } else {
                                Intent intent = new Intent(getContext(), SignPad.class);
                                startActivityForResult(intent, 1);
                            }
                            return;
                        }
                    } else { //노서명
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 노서명");
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAY구분 : " + Paygb[0]);
                        PopupOpen(getContext(), "VAN 승인 중입니다.");

                        PosClient posClient = new PosClient();
                        if (cbDcc.isChecked() && (mTxt.equals("TAX") || mTxt.equals("DCA"))) {
                        } else
                            sendBuff = null;

                        //LJY20250904 : 통합결제 바코드 리딩 시 로직 추가 (매체구분 "B" && 결제구분 "B")
                        if (Paygb[0] == 'B' && cMediagb[0] == 'B') {
                            mTxt = "PRO";
                            if (cbMem.isChecked()) //20200131 : 멤버쉽거래
                            {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 멤버쉽승인");
                                sendBuff = ("0343" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "L37" + BarcodeToTrack2(new String(icdata)) + mMoney.substring(3, 12) + mHalbu + "            " + "      " + mFiller2 + mFiller + " ").getBytes();
                            } else if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 포인트승인");
                                sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else if (cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - TAX or DCA");
                                    System.arraycopy("0437PRO".getBytes(), 0, sendBuff, 0, 7);
                                    System.arraycopy("L".getBytes(), 0, sendBuff, 55, 1);
                                    System.arraycopy(space.getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy("37".getBytes(), 0, sendBuff, 56, 2);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 58, 127-2);
//                                    System.arraycopy(icdata, 0, sendBuff, 421, icdata.length);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - DCC");
                                    sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] PAYPRO - 신용승인");
                                sendBuff = ("0437PRO" + mCatid + strDate + "020010H1          " + mCatid + "L37" + BarcodeToTrack2(new String(icdata)) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            }
                        } else
                        if (Paygb[0] == 'R' && CardBrand[0] == 'K') { //LJY20200713 : 동반위 JUST TOUCH
                            if (cbMem.isChecked()) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 멤버쉽승인(동반위)");
                                sendBuff = ("0343" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "K" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + "            " + "      " + mFiller2 + mFiller + " ").getBytes();
                            } else if (cbPoint.isChecked()) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 포인트승인(동반위)");
                                sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            } else if (cbDcc.isChecked()) {
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - TAX or DCA(동반위)");
                                    System.arraycopy("0694".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 421, 257);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - DCC(동반위)");
                                    sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] RF - 신용승인(동반위)");
                                sendBuff = ("0694HPS" + mCatid + strDate + "020010H1          " + mCatid + "K" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            }
                        } else if (Paygb[0] == 'I' || (Paygb[0] == 'R' && Integer.parseInt(new String(icdata, 0, 4)) > 0)) { //LJY20230818
                            if (cbMem.isChecked()) //20200131 : 멤버쉽거래
                            {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 멤버쉽승인");
                                sendBuff = ("0343" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "I" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + "            " + "      " + mFiller2 + mFiller + " ").getBytes();
                            } else if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 포인트승인");
                                sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            } else if (cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - TAX or DCA");
                                    System.arraycopy("0694".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(new String(encdata).getBytes(), 0, sendBuff, 56, 127);
                                    System.arraycopy(new String(icdata).getBytes(), 0, sendBuff, 421, 257);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - DCC");
                                    sendBuff = ("0694" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC - 신용승인");
                                sendBuff = ("0694HPS" + mCatid + strDate + "020010H1          " + mCatid + "I" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N" + new String(icdata)).getBytes();
                            }
                        } else {
                            if (cbMem.isChecked()) //20200131 : 멤버쉽거래
                            {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 멤버쉽승인");
                                sendBuff = ("0343" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + mTax + mBongsa + new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "A" + new String(encdata, 0, 127) + mMoney.substring(3, 12) + mHalbu + "            " + "      " + mFiller2 + mFiller + " ").getBytes();
                            } else if (cbPoint.isChecked()) { //20200129 : 포인트거래
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 포인트승인");
                                sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            } else if (cbDcc.isChecked()) { //20200306 : DCC
                                if (mTxt.equals("TAX") || mTxt.equals("DCA")) {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - TAX or DCA");
                                    System.arraycopy("0437".getBytes(), 0, sendBuff, 0, 4);
                                    System.arraycopy(encdata, 0, sendBuff, 56, 127);
//                                    System.arraycopy(icdata, 0, sendBuff, 421, icdata.length);
//                                    System.arraycopy(("Y10801" + mCatid + "                       ").getBytes(), 0, sendBuff, 420, 39);
                                } else {
                                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - DCC");
                                    sendBuff = ("0437" + mTxt + mCatid + strDate + "0200" + mApprtp + mDevicegb + "          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                      " + mDcc1 + mBaseamount + mDcc2 + space.substring(0, 61) + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                                }
                            } else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] MS - 신용승인");
                                sendBuff = ("0437HPS" + mCatid + strDate + "020010H1          " + mCatid + "A" + new String(encdata) + mHalbu + mBongsa + mTax + mMoney + "        " + "      " + "                                                                                                     " + mHwnum + SharedManager.SWNUM + "  " + mFiller + "N").getBytes();
                            }
                        }
                        if (bRelease)
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + (new String(sendBuff)).substring(0, 56) + "******************************************************************************************************************************" + (new String(sendBuff)).substring(183, sendBuff.length - 183));
                        else
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT->VAN] SendData : " + new String(sendBuff));
                        mSharedManager.getPreferences().edit().putString("Txtnum", mCatid + strDate).commit();
                        if (mSharedManager.getPreferences().getBoolean("Vpnuse", false)) { //LJY20230111 : 전용회선 사용 시
                            System.arraycopy(String.format("%04d", sendBuff.length).getBytes(), 0, sendBuff, 0, 4);
                            recvBuff = posClient.service_line(mServerip, Integer.parseInt(mServerport), sendBuff);
                        } else if (mSharedManager.getPreferences().getInt("Enctype", 0) == 0)
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] SEED 암복호화"); //OSM20250113

                            recvBuff = posClient.service(mServerip, Integer.parseInt(mServerport), sendBuff);
                        } else {
                            byte[] recvcode = new byte[4];
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화"); //OSM20250113

                            iresult = posClient.service_DES(mServerip, Integer.parseInt(mServerport), sendBuff, recvBuff);   //OSM20250113 : DES암복호화 함수 추가

                            if(iresult == 1) {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DES 암복호화 정상"); //OSM20250113
                            }

                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "result code : " + String.valueOf(iresult));
                                SharedManager.LogBinHex("RecvBuff", recvBuff);
                                System.arraycopy(recvBuff, 55, recvcode, 0, recvBuff.length);
                                SharedManager.LogDebug(bLogUse, "debugjy", "recv code : " + String.valueOf(recvcode));
                            }
                        }
                        PopupClose();
                        InsertRecv(recvBuff);
                    }
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

                    if((mSharedManager.getPreferences().getInt("Readertype", 0) == 3) && (isrun == false)) //POSBANK
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
                    if((mSharedManager.getPreferences().getInt("Readertype", 0) == 2) && (isrun == false)) //OKPOS
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

                        //LJY20201217 : 포트번호/통신속도 가변
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
                            if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                isrun = true;

                                handlerThread = new handler_thread(handler);
                                handlerThread.start();

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
                                usbService.write(temp);
                            }

                            else {
                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
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
                    //LJY20201005 : OKPOS 서명 연동
                    //LJY20201217 : 포트번호/통신속도 가변
                    if (mUart.IsRxData(mSharedManager.getPreferences().getInt("sPortnum", 0)) || mUart.IsRxData(mSharedManager.getPreferences().getInt("Portnum", 0)) == true) {
                        //카드리더UART 문자 꺼내기
                        //LJY20201005 : OKPOS 서명 연동
                        //LJY20201217 : 포트번호/통신속도 가변
                        if(mUart.IsRxData(mSharedManager.getPreferences().getInt("sPortnum", 0)))
                            cData = mUart.GetCh(mSharedManager.getPreferences().getInt("sPortnum", 0));
                        else
                            cData = mUart.GetCh(mSharedManager.getPreferences().getInt("Portnum", 0));

                        if (istep == 0 && cData == 0x06) //ACK수신
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] ACK 수신");
                            slen = 0;
                            istep = 0;
                        } else if (istep == 0 && cData == 0x04) //EOT수신
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] EOT 수신");
                            slen = 0;
                            istep = 0;
                        }
                        else if (istep == 0 && cData == 0x02) //STX수신
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] STX 수신");
                            slen = 0;
                            if (bTitchk) //LJY20230911 : TITENG 리더기 추가
                                istep = 21;
                            else
                                istep = 15;
                            RECVBuf[slen++] = (char) cData;
                        }
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
//                        else if (istep == 15 && func_code == 0x42 && (cData == 0x0E || cData == 0x0F)) //COMMAND수신
//                        {
//                            slen = 0;
//                            istep = 0;
//                        }
                        else if (istep == 15) //COMMAND수신
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] COMMAND 수신");
                            istep = 20;
                            RECVBuf[slen++] = (char)cData;
                        } else if (istep == 20) //길이수신
                        {
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 길이 수신");
                            RECVBuf[slen++] = (char)cData;
                            if (slen == 4) {
                                istep = 25;
                                length_recv = Integer.parseInt(String.format("%02X", RECVBuf[2] & 0xff) + String.format("%02X", RECVBuf[3] & 0xff));
                            }
                        }
                        else if (istep == 21) { //LJY20230911 : TITENG 리더기 추가 //길이수신
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] TITENG 길이 수신");
                            RECVBuf[slen++] = (char) cData;
                            if (slen == 3) {
                                istep = 26;
                                length_recv = RECVBuf[1] * 16 + RECVBuf[2];
                            }
                        }
                        else if (istep == 25) { //데이터수신
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 데이터 수신");
                            RECVBuf[slen++] = (char)cData;
                            if (length_recv == slen - 4) {
                                istep = 30;
                            }
                        }
                        else if (istep == 26) { //LJY20230911 : TITENG 리더기 추가 //데이터수신
                            RECVBuf[slen++] = (char) cData;
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] TITENG 데이터 수신");
                            if (length_recv + 1 == slen - 3) //ETX(1)
                                istep = 30;
                        }

                        else if (istep == 30) { //데이터수신완료
                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 데이터 수신 완료");
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
            if (new String(recvBuff, "EUC-KR").equals("-1") == true || iresult == -1) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] InsertRecv : -1");    //OSM20250113

                Toast.makeText(getContext(), "-1", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-2") == true || iresult == -2) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] InsertRecv : -2");    //OSM20250113

                Toast.makeText(getContext(), "-2", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-3") == true || iresult == -3) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] InsertRecv : -3");    //OSM20250113

                Toast.makeText(getContext(), "-3", Toast.LENGTH_SHORT).show();
            } else if (new String(recvBuff, "EUC-KR").equals("-4") == true || iresult == -4) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] InsertRecv : -4");    //OSM20250113

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
                if(cbMem.isChecked()) //20200131 : 멤버쉽거래
                    recv.str_Msggb = "0330";
                else
                if(cbPoint.isChecked()) //20200129 : 포인트거래
                    recv.str_Msggb = "0310";
                else
                    recv.str_Msggb = new String(recvBuff, 27, 4, "EUC-KR");
                if (cbCup.isChecked())
                    recv.str_Dealgb = "UP";
                else
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
                if(cbDcc.isChecked()) //20200306 : DCC
                {
                    //환율조회 DB저장 안함
                    String str_dccdealno = new String(recvBuff, 360, 18, "EUC-KR");
                    String str_dccofficecd = new String(recvBuff, 439, 1, "EUC-KR");
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DCC 나이스 일련번호 : [" + str_dccdealno + "]");
                    SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DCC 업체 구분 코드 : [" + str_dccofficecd + "]");

                    if(mTxt.equals("DCC") && str_dccdealno.equals("                  ") && (str_dccofficecd.equals("0") || str_dccofficecd.equals(" ")))
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] NOT DCC");
                        recvBuff[271] = '2';
                        etDcc3.setText("2");

                        recv.str_Wcc = new String(recvBuff, 59, 1, "EUC-KR");
                        recv.str_Carddata = new String(recvBuff, 62, 6, "EUC-KR") + "**********";
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

                        etMoney.setText(recv.str_Money);
                        etHalbu.setText(recv.str_Halbu);
                        etTax.setText(recv.str_Tax);
                        etBongsa.setText(recv.str_Bongsa);
                        etBal.setText(recv.str_P3);
                        etMinm.setText(recv.str_Minm);
                        etApprno.setText(recv.str_Apprno);
                        etApprdate.setText(recv.str_Apprdate);
                        etRecvmsg.setText((recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4).replaceAll(" ", ""));
                    }
                    else if(mTxt.equals("DCC"))
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DCC 환율조회");

                        String str_signkey = "";
                        if(new String(sendBuff, 257, 3, "EUC-KR").equals("410")) //현지통화코드
                        {
                            str_signkey = str_signkey + "KRW : " + new String(sendBuff, 264, 10, "EUC-KR") + "\n";
                        }
                        else if (new String(sendBuff, 257, 3, "EUC-KR").equals("840"))
                        {
                            str_signkey = str_signkey + "USD : " + new String(sendBuff, 265, 7, "EUC-KR") + "." + new String(sendBuff, 272, 2, "EUC-KR") + "\n";
                        }
                        str_signkey = str_signkey + new String(recvBuff, 381, 3, "EUC-KR") + " : ";

                        int tempdotint =  Integer.parseInt(new String(recvBuff, 398, 1, "EUC-KR"));

                        if(tempdotint == 0)
                        {
                            str_signkey = str_signkey + new String(recvBuff, 388, 10, "EUC-KR") + "\n";
                        }
                        else
                        {
                            str_signkey = str_signkey + new String(recvBuff, 389, 9 - tempdotint, "EUC-KR") + "." + new String(recvBuff, 398-tempdotint, tempdotint, "EUC-KR") + "\n";
                        }

                        tempdotint =  Integer.parseInt(new String(recvBuff, 413, 1, "EUC-KR"));

                        if(new String(sendBuff, 257, 3, "EUC-KR").equals("410")) //현지통화코드
                        {
                            str_signkey = str_signkey + "( 1KRW = ";
                        }
                        else if (new String(sendBuff, 257, 3, "EUC-KR").equals("840"))
                        {
                            str_signkey = str_signkey + "( 1USD = ";
                        }

                        if(tempdotint == 0)
                        {
                            str_signkey = str_signkey + new String(recvBuff, 399, 14, "EUC-KR") + " ";
                        }
                        else
                        {
                            str_signkey = str_signkey + new String(recvBuff, 399, 14 - tempdotint, "EUC-KR") + "." + new String(recvBuff, 399+14-tempdotint, tempdotint, "EUC-KR") + " ";
                        }
                        str_signkey = str_signkey + new String(recvBuff, 381, 3, "EUC-KR") + " )\n" + "            " + "                              ";

                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] str_signkey : " + str_signkey);

//                        PopupOpen(getContext(), str_signkey);

                        recvBuff[271] = '1';
                        etDcc3.setText("1");

//                        String tempsend = new String(sendBuff, 0, sendBuff.length, "EUC-KR");
//                        String str_dcctemp = new String(sendBuff, 257, 18, "EUC-KR");

                        dialog = new Dialog(getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.activity_popup_dialog);
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        ((TextView) dialog.findViewById(R.id.tvpopup)).setText(str_signkey);
                        ((Button) dialog.findViewById(R.id.btrooting)).setText(str_signkey.substring(0, 3));
                        ((Button) dialog.findViewById(R.id.btrooting)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) { //원화승인
                                mTxt = "TAX";
                                System.arraycopy(mTxt.getBytes(), 0, sendBuff, 4, 3);
                                System.arraycopy(recvBuff, 360, sendBuff, 275, 18);
                                if (dialog.isShowing())
                                    dialog.dismiss();

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 카드리딩 요청입니다.");

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

                                if ((mSharedManager.getPreferences().getInt("Readertype", 0) == 3) && (isrun == false)) //POSBANK
                                {
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
                                    Arrays.fill(encdata, (char) 0x00);
                                    Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
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

                                    writeBuffer = new char[44];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = func_code; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x39; //Length(2)
                                    String sendstr = mTimeout; //Card 대기시간(2)
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String strDate = sdfDate.format(new Date());
                                    sendstr = sendstr + strDate; //거래일시(14)
                                    sendstr = sendstr + mMoney; //거래금액(12)
                                    sendstr = sendstr + mCatid; //TID(10)
                                    sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                                    temp = new byte[44];
                                    for (int i = 0; i < 44; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    scr.sendMsg(temp, temp.length);
                                    PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");

                                    scr.clearRxBuffer();
                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else if ((mSharedManager.getPreferences().getInt("Readertype", 0) == 2) && (isrun == false))//OKPOS
                                {
                                    mUart = new libUart();
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
                                    Arrays.fill(encdata, (char) 0x00);
                                    Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                    btnDisable();

                                    writeBuffer = new char[44];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = func_code; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x39; //Length(2)
                                    String sendstr = mTimeout; //Card 대기시간(2)
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String strDate = sdfDate.format(new Date());
                                    sendstr = sendstr + strDate; //거래일시(14)
                                    sendstr = sendstr + mMoney; //거래금액(12)
                                    sendstr = sendstr + mCatid; //TID(10)
                                    sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                                    temp = new byte[44];
                                    for (int i = 0; i < 44; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");

                                    //LJY20201217 : 포트번호/통신속도 가변
                                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                                    }
                                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else {
                                    if (!SharedManager.isBizdown) {
                                        //LJY20200812 : 가맹점다운로드 예외처리
//                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                                        btnEnable();
//                                        return;
                                    }
                                    if (SharedManager.isStatus == false) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }

                                    if (usbService != null) { // if UsbService was correctly binded, Send data
                                        if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                            isrun = true;

                                            handlerThread = new handler_thread(handler);
                                            handlerThread.start();

                                            Arrays.fill(RECVBuf, (char) 0x00);
                                            Arrays.fill(encdata, (char) 0x00);
                                            Arrays.fill(icdata, (char) 0x00);

                                            initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                            btnDisable();

                                            writeBuffer = new char[44];
                                            writeBuffer[0] = 0x02; //Header ID
                                            writeBuffer[1] = func_code; //Command ID
                                            writeBuffer[2] = 0x00;
                                            writeBuffer[3] = 0x39; //Length(2)
                                            String sendstr = mTimeout; //Card 대기시간(2)
                                            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                            String strDate = sdfDate.format(new Date());
                                            sendstr = sendstr + strDate; //거래일시(14)
                                            sendstr = sendstr + mMoney; //거래금액(12)
                                            sendstr = sendstr + mCatid; //TID(10)
                                            sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                            writeBuffer[43] = xor_sum(writeBuffer, 43);

                                            temp = new byte[44];
                                            for (int i = 0; i < 44; i++) {
                                                temp[i] = (byte) writeBuffer[i];
                                            }
                                            PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");
                                            usbService.write(temp);
                                        }

                                        else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");    //OSM20250902 : 중복요청 리턴코드 수정
                                            Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }
                                }
                            }
                        });
                        ((Button) dialog.findViewById(R.id.btdcc)).setVisibility(View.VISIBLE);
                        ((Button) dialog.findViewById(R.id.btdcc)).setText(new String(recvBuff, 381, 3, "EUC-KR"));
                        ((Button) dialog.findViewById(R.id.btdcc)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTxt = "DCA";
                                System.arraycopy(mTxt.getBytes(), 0, sendBuff, 4, 3);
                                System.arraycopy(recvBuff, 360, sendBuff, 275, 54);
                                if (dialog.isShowing())
                                    dialog.dismiss();

                                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] IC 카드리딩 요청입니다.");

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

                                if ((mSharedManager.getPreferences().getInt("Readertype", 0) == 3) && (isrun == false)) //POSBANK
                                {
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
                                    Arrays.fill(encdata, (char) 0x00);
                                    Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
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

                                    writeBuffer = new char[44];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = func_code; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x39; //Length(2)
                                    String sendstr = mTimeout; //Card 대기시간(2)
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String strDate = sdfDate.format(new Date());
                                    sendstr = sendstr + strDate; //거래일시(14)
                                    sendstr = sendstr + mMoney; //거래금액(12)
                                    sendstr = sendstr + mCatid; //TID(10)
                                    sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                                    temp = new byte[44];
                                    for (int i = 0; i < 44; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    scr.sendMsg(temp, temp.length);
                                    PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");

                                    scr.clearRxBuffer();
                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else if ((mSharedManager.getPreferences().getInt("Readertype", 0) == 2) && (isrun == false)) //OKPOS
                                {
                                    mUart = new libUart();
                                    isrun = true;

                                    Arrays.fill(RECVBuf, (char) 0x00);
                                    Arrays.fill(encdata, (char) 0x00);
                                    Arrays.fill(icdata, (char) 0x00);

                                    initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                    btnDisable();

                                    writeBuffer = new char[44];
                                    writeBuffer[0] = 0x02; //Header ID
                                    writeBuffer[1] = func_code; //Command ID
                                    writeBuffer[2] = 0x00;
                                    writeBuffer[3] = 0x39; //Length(2)
                                    String sendstr = mTimeout; //Card 대기시간(2)
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String strDate = sdfDate.format(new Date());
                                    sendstr = sendstr + strDate; //거래일시(14)
                                    sendstr = sendstr + mMoney; //거래금액(12)
                                    sendstr = sendstr + mCatid; //TID(10)
                                    sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                                    System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                    writeBuffer[43] = xor_sum(writeBuffer, 43);

                                    temp = new byte[44];
                                    for (int i = 0; i < 44; i++) {
                                        temp[i] = (byte) writeBuffer[i];
                                    }
                                    PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");

                                    //LJY20201217 : 포트번호/통신속도 가변
                                    mUart.Init(mSharedManager.getPreferences().getInt("Portnum", 0));
                                    if (mUart.IsOpen(mSharedManager.getPreferences().getInt("Portnum", 0)) == false) {
                                        mUart.Open(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")), 8, 0, 1, true);
                                        mUart.SetBaudrate(mSharedManager.getPreferences().getInt("Portnum", 0), Integer.parseInt(mSharedManager.getPreferences().getString("BaudrateStr", "115200")));
                                    }
                                    mUart.QueueClear(mSharedManager.getPreferences().getInt("Portnum", 0));
                                    mUart.DataSend(mSharedManager.getPreferences().getInt("Portnum", 0), temp, temp.length);

                                    handlerThread = new handler_thread(handler);
                                    handlerThread.start();
                                } else {
                                    if (!SharedManager.isBizdown) {
                                        //LJY20200812 : 가맹점다운로드 예외처리
//                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 가맹점다운로드 해주시길 바랍니다.");
//                                        Toast.makeText(getContext(), "가맹점다운로드 해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
//                                        btnEnable();
//                                        return;
                                    }
                                    if (SharedManager.isStatus == false) {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 리더기 연결 상태 체크해주시길 바랍니다.");
                                        Toast.makeText(getContext(), "리더기 연결 상태 체크해주시길 바랍니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }

                                    if (usbService != null) { // if UsbService was correctly binded, Send data
                                        if (isrun == false) {       //OSM20240605 : 중복 호출 방지 추가
                                            isrun = true;

                                            handlerThread = new handler_thread(handler);
                                            handlerThread.start();

                                            Arrays.fill(RECVBuf, (char) 0x00);
                                            Arrays.fill(encdata, (char) 0x00);
                                            Arrays.fill(icdata, (char) 0x00);

                                            initSerial();
//                                    func_code = 0x6C;     //LJY20250904 : 8BIN/통합결제 적용
                                            btnDisable();

                                            writeBuffer = new char[44];
                                            writeBuffer[0] = 0x02; //Header ID
                                            writeBuffer[1] = func_code; //Command ID
                                            writeBuffer[2] = 0x00;
                                            writeBuffer[3] = 0x39; //Length(2)
                                            String sendstr = mTimeout; //Card 대기시간(2)
                                            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
                                            String strDate = sdfDate.format(new Date());
                                            sendstr = sendstr + strDate; //거래일시(14)
                                            sendstr = sendstr + mMoney; //거래금액(12)
                                            sendstr = sendstr + mCatid; //TID(10)
                                            sendstr = sendstr + sReaderApprtp; //거래종류(1)        //LJY20250904 : 8BIN/통합결제 적용
                                            System.arraycopy(sendstr.toCharArray(), 0, writeBuffer, 4, 39);
                                            writeBuffer[43] = xor_sum(writeBuffer, 43);

                                            temp = new byte[44];
                                            for (int i = 0; i < 44; i++) {
                                                temp[i] = (byte) writeBuffer[i];
                                            }
                                            PopupOpenEOT(getContext(), "IC 카드리딩 해주세요.");
                                            usbService.write(temp);
                                        }

                                        else {
                                            SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] -17 : API가 이미 진행중입니다.");        //OSM20250902 : 중복요청 리턴코드 수정
                                            Toast.makeText(getContext(), "API가 이미 진행중입니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] USB 서비스 불가능합니다.");
                                        Toast.makeText(getContext(), "USB 서비스 불가능합니다.", Toast.LENGTH_SHORT).show();
                                        btnEnable();
                                        return;
                                    }
                                }
                            }
                        });
                        dialog.show();
                        return;
                    }
                    else
                    {
                        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] DCC 환율조회 후 승인");

                        recvBuff[271] = '1';
                        etDcc3.setText("1");

                        recv.str_Wcc = new String(recvBuff, 59, 1, "EUC-KR");
                        recv.str_Carddata = new String(recvBuff, 62, 6, "EUC-KR") + "**********";
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

                        etMoney.setText(recv.str_Money);
                        etHalbu.setText(recv.str_Halbu);
                        etTax.setText(recv.str_Tax);
                        etBongsa.setText(recv.str_Bongsa);
                        etBal.setText(recv.str_P3);
                        etMinm.setText(recv.str_Minm);
                        etApprno.setText(recv.str_Apprno);
                        etApprdate.setText(recv.str_Apprdate);
                        etRecvmsg.setText((recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4).replaceAll(" ", ""));
                    }
                }
                else
                if(cbMem.isChecked()) //20200131 : 멤버쉽거래
                {
                    recv.str_Tax = new String(recvBuff, 59, 2, "EUC-KR"); //적립구분(2)
                    recv.str_Bongsa = new String(recvBuff, 61, 2, "EUC-KR"); //포인트구분(2)
                    recv.str_Apprdate = new String(recvBuff, 65, 12, "EUC-KR"); //쇼핑몰거래일시(12)
                    recv.str_Wcc = new String(recvBuff, 77, 1, "EUC-KR"); //WCC(1)
                    recv.str_Carddata = new String(recvBuff, 80, 6, "EUC-KR") + "**********"; //카드BIN(6)
                    recv.str_Money = new String(recvBuff, 118, 9, "EUC-KR"); //거래금액(9)
                    recv.str_Halbu = new String(recvBuff, 127, 16, "EUC-KR"); //비밀번호(16)
                    recv.str_Apprno = new String(recvBuff, 143, 15, "EUC-KR"); //승인번호(15)
                    recv.str_Bgcode = new String(recvBuff, 158, 2, "EUC-KR"); //발급사코드(2)
                    recv.str_Bgnm = new String(recvBuff, 160, 20, "EUC-KR"); //발급사명(20)
                    recv.str_Storeno = new String(recvBuff, 180, 15, "EUC-KR"); //가맹점번호(15)
                    recv.str_P1 = new String(recvBuff, 195, 9, "EUC-KR"); //발생포인트(9)
                    recv.str_P2 = new String(recvBuff, 204, 9, "EUC-KR"); //가용포인트(9)
                    recv.str_P3 = new String(recvBuff, 213, 9, "EUC-KR"); //누적포인트(9)
                    recv.str_Msg1 = new String(recvBuff, 222, 40, "EUC-KR"); //알림메시지1(40)
                    recv.str_Msg2 = new String(recvBuff, 262, 24, "EUC-KR"); //알림메시지2(24)
                    recv.str_Msg3 = new String(recvBuff, 286, 24, "EUC-KR"); //알림메시지3(24)
                    recv.str_Msg4 = "";
                    recv.str_CBStore = new String(recvBuff, 310, 9, "EUC-KR"); //신판실승인금액(9)
                    recv.str_CBApprno = new String(recvBuff, 319, 131, "EUC-KR"); //Filler(131)
                    recv.str_Bizno = "";
                    recv.str_Perno = "";
                    recv.str_Pinno = "";
                    recv.str_Micode = "";
                    recv.str_Minm = "";
                    recv.str_Dealno = "";
                    recv.str_Dccyn = "";
                    recv.str_RealApprmoney = "";
                    recv.str_DealCardno = "";

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dbHelper.insertDeal(simpleDateFormat.format(new Date()), recv.str_Dealgb, recv.str_Msggb, recv.str_Carddata, recv.str_Money, recv.str_Tax, recv.str_Bongsa, recv.str_Halbu, recv.str_Apprno, recv.str_Apprdate, recv.str_Tid, recv.str_Bgnm, recv.str_Minm, recv.str_Storeno, recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4, recv.str_Recvcode, recv.str_P3, recv.str_Wcc, recv.str_RealApprmoney, recv.str_Msgno, recv.str_DealCardno, recv.str_Msgtxt, recv.str_Micode, recv.str_Bgcode); //OSM20250814 : 매입사/발급사 코드 추가  //20200129 : 포인트거래

                    etMoney.setText(recv.str_Money);
                    etHalbu.setText(recv.str_Halbu);
                    etTax.setText(recv.str_Tax);
                    etBongsa.setText(recv.str_Bongsa);
                    etBal.setText(recv.str_P3);
                    etMinm.setText(recv.str_Minm);
                    etApprno.setText(recv.str_Apprno);
                    etApprdate.setText(recv.str_Apprdate);
                    etRecvmsg.setText((recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4).replaceAll(" ", ""));
                    etP1.setText(recv.str_P1);
                    etP2.setText(recv.str_P2);
                    etP3.setText(recv.str_P3);
                }
                else {
                    recv.str_Wcc = new String(recvBuff, 59, 1, "EUC-KR");
                    recv.str_Carddata = new String(recvBuff, 62, 6, "EUC-KR") + "**********";
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

                    etMoney.setText(recv.str_Money);
                    etHalbu.setText(recv.str_Halbu);
                    etTax.setText(recv.str_Tax);
                    etBongsa.setText(recv.str_Bongsa);
                    etBal.setText(recv.str_P3);
                    etMinm.setText(recv.str_Minm);
                    etApprno.setText(recv.str_Apprno);
                    etApprdate.setText(recv.str_Apprdate);
                    etRecvmsg.setText((recv.str_Msg1 + recv.str_Msg2 + recv.str_Msg3 + recv.str_Msg4).replaceAll(" ", ""));

                    Arrays.fill(recvBuff, (byte)0x00);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Arrays.fill(recvBuff, (byte)0x00);
        btnEnable();
    }
}