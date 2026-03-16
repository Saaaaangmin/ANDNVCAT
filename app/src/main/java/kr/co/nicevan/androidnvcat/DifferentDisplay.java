package kr.co.nicevan.androidnvcat;

import static kr.co.nicevan.androidnvcat.KeyPadNumber.button0;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button010;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button1;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button2;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button3;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button4;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button5;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button6;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button7;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button8;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.button9;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.buttonBackspace;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.buttonClear;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.buttonExit;
import static kr.co.nicevan.androidnvcat.KeyPadNumber.buttonOK;
import static kr.co.nicevan.androidnvcat.MainActivity.mSharedManager;
import static kr.co.nicevan.androidnvcat.SignPad.createBlackAndWhite;
import static kr.co.nicevan.androidnvcat.SignPad.iTSuse;
import static kr.co.nicevan.androidnvcat.SignPad.in;
import static kr.co.nicevan.androidnvcat.SignPad.mCancelButton;
import static kr.co.nicevan.androidnvcat.SignPad.mSaveButton;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.RECVBuf;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.dialog;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isSign;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.mUart;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.presentation;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.scr;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.usbService;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.isBizdown;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.IOException;
import java.nio.ByteBuffer;

import kr.co.nicevan.androidnvcat.shared.SharedManager;

public class DifferentDisplay extends Presentation {

    int display_gb;
    Context context;
    String sendstr = "";

    public DifferentDisplay(Context outerContext, Display display, int gb, String str) {
        super(outerContext, display, gb);

        display_gb = gb;
        sendstr = str;
        context = outerContext;
    }

//    private void PopupSetting() {
//        // 팝업이 올라오면 배경 블러처리
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//        layoutParams.dimAmount = 0.2f;
//        getWindow().setAttributes(layoutParams);
//
//        if (presentation != null && presentation.isShowing()) {
//            Display dp = ((WindowManager) presentation.getOwnerActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); //디스플레이 화면 사이즈 구하기
//            getWindow().getAttributes().width = (int) (dp.getWidth() * 0.5); //가로 전체에 80프로
//            getWindow().getAttributes().height = (int) (dp.getHeight() * 0.5); //세로 전체에 50프로
//            presentation.getOwnerActivity().setFinishOnTouchOutside(false); //액티비티 바깥화면이 클릭되어도 종료되지 않게 설정하기
//        }
//
//    }

    Button btBtrooting_dual, bt_Clear_Button_dual, bt_Save_Button_dual;
    TextView tvPopup_dual, tv_Guide_dock_card_dual, tv_Guide_payment_dual, tv_Guide_auto_cancel_dual;
    ImageView iv_Card_dual;
    ImageButton ib_Cancel_Button_dual;
    SignaturePad sp_Signature_Pad_dual;
    LinearLayout ll_Popup_daul;

    EditText etCashNum_dual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.2f;
        getWindow().setAttributes(layoutParams);

//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
//
//        Presentation의 기존 LayoutParams를 가져온 후 수정
//        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//        layoutParams.dimAmount = 0.2f;
//        getWindow().setAttributes(layoutParams);

        if (display_gb == 5) { //키패드
            setContentView(R.layout.activity_different_key_pad_number);

            Display dp = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); //디스플레이 화면 사이즈 구하기
            getWindow().getAttributes().width = (int) (dp.getWidth() * 0.7); //가로 전체에 70프로
            getWindow().getAttributes().height = (int) (dp.getHeight() * 0.35); //세로 전체에 35프로

            etCashNum_dual = findViewById(R.id.etcashinputnum_dual);
            etCashNum_dual.setInputType(0);

            findViewById(R.id.ButtonOK_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonOK.performClick();
                }
            });

            findViewById(R.id.ButtonExit_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonExit.performClick();
                }
            });

            findViewById(R.id.ButtonClear_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.setText("");
                    buttonClear.performClick();
                }
            });

            findViewById(R.id.ButtonBackSpace_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String mCashNum = etCashNum_dual.getText().toString();
                    int mCashNumLen = etCashNum_dual.getText().length();
                    if (mCashNumLen < 1)
                        etCashNum_dual.setText("");
                    else
                        etCashNum_dual.setText(mCashNum.substring(0, mCashNumLen - 1));

                    buttonBackspace.performClick();
                }
            });

            findViewById(R.id.Button1_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("1");
                    button1.performClick();
                }
            });

            findViewById(R.id.Button2_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("2");
                    button2.performClick();
                }
            });

            findViewById(R.id.Button3_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("3");
                    button3.performClick();
                }
            });

            findViewById(R.id.Button4_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("4");
                    button4.performClick();
                }
            });

            findViewById(R.id.Button5_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("5");
                    button5.performClick();
                }
            });

            findViewById(R.id.Button6_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("6");
                    button6.performClick();
                }
            });

            findViewById(R.id.Button7_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("7");
                    button7.performClick();
                }
            });

            findViewById(R.id.Button8_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("8");
                    button8.performClick();
                }
            });

            findViewById(R.id.Button9_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("9");
                    button9.performClick();
                }
            });

            findViewById(R.id.Button0_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("0");
                    button0.performClick();
                }
            });

            findViewById(R.id.Button010_dual).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCashNum_dual.append("010");
                    button010.performClick();
                }
            });
        } else if (display_gb == 4) {
            setContentView(R.layout.activity_different_sign_pad);

            Display dp = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); //디스플레이 화면 사이즈 구하기
            getWindow().getAttributes().width = (int) (dp.getWidth() * 0.8); //가로 전체에 80프로
            getWindow().getAttributes().height = (int) (dp.getHeight() * 0.6); //세로 전체에 40프로

            ib_Cancel_Button_dual = findViewById(R.id.cancel_button_dual);
            ib_Cancel_Button_dual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCancelButton.performClick();
                }
            });

            sp_Signature_Pad_dual = findViewById(R.id.signature_pad_dual);
            sp_Signature_Pad_dual.setOnSignedListener(new SignaturePad.OnSignedListener() {
                @Override
                public void onStartSigning() {
                }

                @Override
                public void onSigned() {
                    bt_Save_Button_dual.setEnabled(true);
                    bt_Clear_Button_dual.setEnabled(true);
                    ib_Cancel_Button_dual.setEnabled(true);
                }

                @Override
                public void onClear() {
                    bt_Save_Button_dual.setEnabled(false);
                    bt_Clear_Button_dual.setEnabled(false);
                    ib_Cancel_Button_dual.setEnabled(true);
                }
            });

            bt_Clear_Button_dual = findViewById(R.id.clear_button_dual);
            bt_Clear_Button_dual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sp_Signature_Pad_dual.clear();
                }
            });

            bt_Save_Button_dual = findViewById(R.id.save_button_dual);
            bt_Save_Button_dual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bitmap signatureBitmap = sp_Signature_Pad_dual.getSignatureBitmap();

                    Bitmap largeMono = Bitmap.createScaledBitmap(signatureBitmap, 128, 64, true); //128x64 단색 비트맵
                    Bitmap smallMono = createBlackAndWhite(largeMono);

                    ByteBuffer saveBuffer = null;
                    try {
                        saveBuffer = bmpSaveBuffer(smallMono); //BMP 버퍼 저장
                    } catch (Exception e) {

                    }

                    in = saveBuffer.array();

                    iTSuse = 1;
                    mSaveButton.performClick();
                }
            });
        } else {
            setContentView(R.layout.activity_different_display);

            btBtrooting_dual = findViewById(R.id.btrooting_dual);
            tvPopup_dual = findViewById(R.id.tvpopup_dual);
            tv_Guide_dock_card_dual = findViewById(R.id.tv_guide_dock_card_dual);
            tv_Guide_payment_dual = findViewById(R.id.tv_guide_payment_dual);
            tv_Guide_auto_cancel_dual = findViewById(R.id.tv_guide_auto_cancel_dual);
            iv_Card_dual = findViewById(R.id.iv_card_dual);
            ll_Popup_daul = findViewById(R.id.ll_popup_dual);
        }

        Display dp = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); //디스플레이 화면 사이즈 구하기

        if (display_gb == 1) { //팝업만
            getWindow().getAttributes().width = (int) (dp.getWidth() * 0.9); //가로 전체에 70프로
            getWindow().getAttributes().height = (int) (dp.getHeight() * 0.9 * 0.1); //세로 전체에 35프로

            tvPopup_dual.setText(sendstr);
            btBtrooting_dual.setVisibility(View.GONE);
            iv_Card_dual.setVisibility(View.GONE);
            tv_Guide_dock_card_dual.setVisibility(View.GONE);
            tv_Guide_payment_dual.setVisibility(View.GONE);
            tv_Guide_auto_cancel_dual.setVisibility(View.GONE);
            ll_Popup_daul.setVisibility(View.GONE);
        } else if (display_gb == 2) { //팝업+EOT

            tvPopup_dual.setText("신용카드 결제");
            btBtrooting_dual.setText("요청취소");
            btBtrooting_dual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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


                    if (isMultipad || isSign || func_code == 0xD3 || func_code == 0x71 || func_code == 0x93) //OSM20250508 : Mifare 사원증ID 조회  //OSM20230719 : RF 잔액조회    //LJY20201005 : OKPOS 서명 연동 //LJY20200713 : 바코드리딩
                    {
                        RECVBuf[0] = 0x04;
                        RECVBuf[4] = 0xCD;
                    }


                    PopupClose();
                }
            });

            if (func_code == 0x45) {
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.6); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.6); //세로 전체에 35프로

                tvPopup_dual.setText("서명패드 연동");
                iv_Card_dual.setVisibility(View.GONE);
                tv_Guide_dock_card_dual.setText("고객식별번호 입력 중입니다.");
                tv_Guide_payment_dual.setVisibility(View.GONE);
                tv_Guide_auto_cancel_dual.setVisibility(View.GONE);
            } else if (isMultipad) {
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.6); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.6); //세로 전체에 35프로

                tvPopup_dual.setText("서명패드 연동");
                iv_Card_dual.setVisibility(View.GONE);
                tv_Guide_dock_card_dual.setText("PIN 입력 중입니다.");
                tv_Guide_payment_dual.setVisibility(View.GONE);
            } else if (isSign) {
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.6); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.6); //세로 전체에 35프로

                tvPopup_dual.setText("서명패드 연동");
                iv_Card_dual.setVisibility(View.GONE);
                tv_Guide_dock_card_dual.setText("서명 해주세요");
                tv_Guide_payment_dual.setVisibility(View.GONE);
            } else if (func_code == 0xD3) //LJY20200713 : 바코드리딩
            {
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.6); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.6); //세로 전체에 35프로

                tvPopup_dual.setText("서명패드 연동");
                iv_Card_dual.setVisibility(View.GONE);
                tv_Guide_dock_card_dual.setText("바코드리딩 해주세요");
                tv_Guide_auto_cancel_dual.setVisibility(View.GONE);
                ll_Popup_daul.setWeightSum(3);
            } else if (func_code == 0x71) //OSM20230718 : RF잔액 조회
            {
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.6); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.6); //세로 전체에 35프로

                tvPopup_dual.setText("서명패드 연동");
                iv_Card_dual.setVisibility(View.GONE);
                tv_Guide_dock_card_dual.setText("RF 리딩 해주세요");
                tv_Guide_auto_cancel_dual.setVisibility(View.GONE);
                ll_Popup_daul.setWeightSum(3);
            }

            else if (func_code == 0x93) //OSM20250508 : Mifare 사원증ID 조회
            {
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.6); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.6); //세로 전체에 35프로

                tvPopup_dual.setText("서명패드 연동");
                iv_Card_dual.setVisibility(View.GONE);
                tv_Guide_dock_card_dual.setText("사원증 리딩 해주세요");
                tv_Guide_auto_cancel_dual.setVisibility(View.GONE);
                ll_Popup_daul.setWeightSum(3);
            }

            else if (func_code == 0x6C || func_code == 0x9C) { //LJY20250904 : 8BIN/통합결제 적용
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.7); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.7); //세로 전체에 35프로

                iv_Card_dual.setImageResource(R.drawable.payment_processing_img_iccard);
                tv_Guide_dock_card_dual.setText("신용카드를 그림과 같이\nIC카드 리더기에 꽂아주세요");
                tv_Guide_payment_dual.setText("결제가 완료될 때까지\n카드를 빼지 마세요!");
                tv_Guide_auto_cancel_dual.setText(mSharedManager.getPreferences().getString("Timeout", "30") + "초이내로 결제하지 않으면 자동 취소됩니다"); //LJY20230911 : 설정 타임아웃 시간 팝업 변경

            } else {
                getWindow().getAttributes().width = (int) (dp.getWidth() * 0.7); //가로 전체에 70프로
                getWindow().getAttributes().height = (int) (dp.getHeight() * 0.7); //세로 전체에 35프로

                iv_Card_dual.setImageResource(R.drawable.payment_processing_img_fallback);
                tv_Guide_dock_card_dual.setText("신용카드 마그네틱을\n그림과 같이 리더기에 긁어주세요");
                tv_Guide_payment_dual.setVisibility(View.GONE);
                tv_Guide_auto_cancel_dual.setText(mSharedManager.getPreferences().getString("Timeout", "30") + "초이내로 결제하지 않으면 자동 취소됩니다"); //LJY20230911 : 설정 타임아웃 시간 팝업 변경

            }
        } else if (display_gb == 3) {
            getWindow().getAttributes().width = (int) (dp.getWidth() * 0.8); //가로 전체에 70프로
            getWindow().getAttributes().height = (int) (dp.getHeight() * 0.8 * 0.2); //세로 전체에 35프로

            ll_Popup_daul.setVisibility(View.GONE);
            tvPopup_dual.setText(sendstr);
            iv_Card_dual.setVisibility(View.GONE);
            tv_Guide_dock_card_dual.setVisibility(View.GONE);
            tv_Guide_payment_dual.setVisibility(View.GONE);
            tv_Guide_auto_cancel_dual.setVisibility(View.GONE);
            btBtrooting_dual.setText("닫기");
            btBtrooting_dual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (presentation != null && presentation.isShowing()) {
                        PopupClose();

                        if (isBizdown == false || func_code == 0x31 || func_code == 0xA0 || func_code == 0xA1) {//LJY20221004 : RESTART 실패시 예외처리 추가
                            if (presentation.getOwnerActivity() != null)
                                presentation.getOwnerActivity().moveTaskToBack(true);

                            if (dialog.getOwnerActivity() != null)
                                dialog.getOwnerActivity().moveTaskToBack(true);
                        }
                    }
                }
            });
        }
    }


    public ByteBuffer bmpSaveBuffer(Bitmap mBitmap) throws Exception {
        // 이미지 사이즈
        int mWidth = mBitmap.getWidth();
        int mHeight = mBitmap.getHeight();

        // 픽셀 갯수
        int[] mPixel = new int[mWidth * mHeight];

        // 이미지 크기 (1비트로 구성된 이미지이므로 8을 나누어 줘야 함)
        int imgSize = mPixel.length / 8;

        // 비트맵 데이터를 찾을 수 있는 시작 주소
        int imgOffset = 0x3E;

        // 비트맵 이미지의 최종 사이즈
        int fileSize = imgSize + imgOffset;

        // 받아온 Bitmap으로부터 픽셀 정보를 int 배열로 받아옴
        // 픽셀 한 개당 32비트의 데이터로 가져오게 됨 (FF FF FF FF)
        // 순서대로 (Alpha Red Green Blue)임
        mBitmap.getPixels(mPixel, 0, mWidth, 0, 0, mWidth, mHeight);

        // 파일 정보를 담아둘 바이트 버퍼를 생성한다
        ByteBuffer mBuffer = ByteBuffer.allocate(fileSize);

        try {
            // 헤더 시작
            mBuffer.put((byte) 0x42);
            mBuffer.put((byte) 0x4D);
            mBuffer.put(putInt(fileSize));
            mBuffer.put(putShort((short) 0));
            mBuffer.put(putShort((short) 0));
            mBuffer.put(putInt(imgOffset));
            mBuffer.put(putInt(0x28));
            mBuffer.put(putInt(mWidth));
            mBuffer.put(putInt(mHeight));
            mBuffer.put(putShort((short) 1));
            mBuffer.put(putShort((short) 1));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(imgSize));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(0));
            mBuffer.put(putInt(0));
            // 여기서 사용할 색상 선택
            // 1비트로 표현되므로 두 가지 색상만 선택 가능
            mBuffer.put(putInt(0)); // 검정색
            mBuffer.put(putInt(0x00FFFFFF)); // 흰색

            // 여기부터 비트맵 데이터
            int height = mHeight;
            int width = mWidth;
            int startPosition = 0;
            int endPosition = 0;

            while (height > 0) {
                startPosition = (height - 1) * width;
                endPosition = height * width;
                int[] iTemp = new int[8];

                for (int i = startPosition; i < endPosition; ) {
                    if ((i + 7) <= endPosition) {
                        for (int j = 7; j >= 0; j--) {
                            iTemp[j] = mPixel[i++];
                        }
                    } else {
                        int mLength = endPosition - i;
                        for (int j = 7; j >= (8 - mLength); j++) {
                            iTemp[j] = mPixel[i++];
                        }
                        for (int j = (7 - mLength); j >= 0; j++) {
                            iTemp[j] = 0x00FFFFFF;
                        }
                    }

                    mBuffer.put(putIntToBit(iTemp));

                }
                height--;
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            throw e1;
        } finally {

        }

        return mBuffer;
    }

    private byte[] putInt(int value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0x000000FF);
        b[1] = (byte) ((value & 0x0000FF00) >> 8);
        b[2] = (byte) ((value & 0x00FF0000) >> 16);
        b[3] = (byte) ((value & 0xFF000000) >> 24);

        return b;
    }

    private byte[] putShort(short value) throws IOException {
        byte[] b = new byte[2];

        b[0] = (byte) (value & 0x00FF);
        b[1] = (byte) ((value & 0xFF00) >> 8);

        return b;
    }

    private byte putIntToBit(int[] value) throws IOException {
        byte bReturn = (byte) 0xFF;
        byte[] b = new byte[3];

        for (int i = 0; i < value.length; i++) {
            // 알파값은 버리고 RGB값만 사용함
            b[0] = (byte) (value[i] & 0x000000FF);
            b[1] = (byte) ((value[i] & 0x0000FF00) >> 8);
            b[2] = (byte) ((value[i] & 0x00FF0000) >> 16);

            if ((b[0] != (byte) 0xFF) || (b[1] != (byte) 0xFF) || (b[2] != (byte) 0xFF)) {
                // 셋중 하나라도 FF가 아니면 검정색처리
                bReturn -= Math.pow(2, i);
            }
        }
        return (byte) bReturn;
    }
}