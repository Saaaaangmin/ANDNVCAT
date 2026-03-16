package kr.co.nicevan.androidnvcat.shared;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import kr.co.nicevan.androidnvcat.R;

import static kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad;
import static kr.co.nicevan.androidnvcat.shared.SharedArray.isSign;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bNoTimer;

public class OverlayPopupManager {

    private static final java.util.Map<Integer, View> overlayRoots = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Integer, WindowManager> windowManagers = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Integer, android.os.CountDownTimer> overlayTimers = new java.util.concurrent.ConcurrentHashMap<>();

    private OverlayPopupManager() {}

    // 외부 디스플레이 overlay show
    public static void showOnDisplayByGb(Context context, Display display, int gb, String text) {
        if (context == null || display == null) return;

        Context appCtx = context.getApplicationContext();
        Context displayCtx = appCtx.createDisplayContext(display);
        WindowManager wm = (WindowManager) displayCtx.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return;

        int displayId = display.getDisplayId();

        // 중복 제거
        dismiss(displayId);

        // fullscreen overlay root
        android.widget.FrameLayout root = new android.widget.FrameLayout(displayCtx);

        root.setBackgroundColor(0x33000000);
        root.setClickable(true);

        View content = bindPopupEOT(displayCtx, display, displayId, gb, text);

        // content 사이즈/중앙 배치
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        display.getRealMetrics(dm);

        int w = dm.widthPixels;
        int h = dm.heightPixels;

        // DifferentDisplay 비율 그대로 매핑
        float wRate, hRate;
        if (gb == 5) {          // 키패드
            wRate = 0.7f; hRate = 0.35f;
        } else if (gb == 4) {   // 서명
            wRate = 0.8f; hRate = 0.6f;
        } else if (gb == 1) {   // 팝업만(간단)
            wRate = 0.9f; hRate = 0.09f; // DifferentDisplay: 0.9, 0.9*0.1
        } else if (gb == 3) {   // 메시지+닫기
            wRate = 0.8f; hRate = 0.16f; // DifferentDisplay: 0.8, 0.8*0.2
        } else {                // gb == 2 (팝업+EOT)
            if (kr.co.nicevan.androidnvcat.shared.SharedArray.func_code == 0x45
                    || kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad
                    || kr.co.nicevan.androidnvcat.shared.SharedArray.isSign
                    || kr.co.nicevan.androidnvcat.shared.SharedArray.func_code == 0xD3
                    || kr.co.nicevan.androidnvcat.shared.SharedArray.func_code == 0x71
                    || kr.co.nicevan.androidnvcat.shared.SharedArray.func_code == 0x93) {
                wRate = 0.6f; hRate = 0.6f;
            } else {
                wRate = 0.7f; hRate = 0.7f;
            }
        }

        android.widget.FrameLayout.LayoutParams clp =
                new android.widget.FrameLayout.LayoutParams((int)(w * wRate), (int)(h * hRate));
        clp.gravity = android.view.Gravity.CENTER;
        root.addView(content, clp);

        // window type/flags
        int type;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                flags,
                android.graphics.PixelFormat.TRANSLUCENT
        );
        lp.gravity = android.view.Gravity.CENTER;

        try {
            wm.addView(root, lp);
            overlayRoots.put(displayId, root);
            windowManagers.put(displayId, wm);

            SharedManager.LogDebug(kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse,
                    "debugjy", "[NVCAT] overlay 활성화 성공. gb=" + gb + ", displayId=" + displayId);

        } catch (Exception e) {
            SharedManager.LogDebug(kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse,
                    "debugjy", "[NVCAT] overlay 활성화 실패. gb=" + gb + ", displayId=" + displayId + ", err=" + e);
        }
    }


    //bindPopupEOT 하나로 gb==1/2/3/4/5 전부 처리
    private static View bindPopupEOT(Context ctx, Display display, int displayId, int gb, String text) {

        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(ctx);

        // gb == 5 : 키패드
        if (gb == 5) {
            View v = inflater.inflate(R.layout.activity_different_key_pad_number, null, false);
            View card = v.findViewById(R.id.popup_card);
            if (card != null) {
                card.setBackgroundResource(R.drawable.bg_dialog);
            }

            // DifferentDisplay와 동일: EditText inputType(0)
            try {
                android.widget.EditText et = v.findViewById(R.id.etcashinputnum_dual);
                if (et != null) et.setInputType(0);
            } catch (Exception ignored) {}

            // performClick 브릿지
            v.findViewById(R.id.ButtonOK_dual).setOnClickListener(view -> kr.co.nicevan.androidnvcat.KeyPadNumber.buttonOK.performClick());
            v.findViewById(R.id.ButtonExit_dual).setOnClickListener(view -> kr.co.nicevan.androidnvcat.KeyPadNumber.buttonExit.performClick());

            v.findViewById(R.id.ButtonClear_dual).setOnClickListener(view -> {
                try {
                    android.widget.EditText et = v.findViewById(R.id.etcashinputnum_dual);
                    if (et != null) et.setText("");
                } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.buttonClear.performClick();
            });

            v.findViewById(R.id.ButtonBackSpace_dual).setOnClickListener(view -> {
                try {
                    android.widget.EditText et = v.findViewById(R.id.etcashinputnum_dual);
                    if (et != null) {
                        String s = et.getText().toString();
                        int len = et.getText().length();
                        if (len < 1) et.setText("");
                        else et.setText(s.substring(0, len - 1));
                    }
                } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.buttonBackspace.performClick();
            });

            v.findViewById(R.id.Button1_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("1"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button1.performClick();
            });
            v.findViewById(R.id.Button2_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("2"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button2.performClick();
            });
            v.findViewById(R.id.Button3_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("3"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button3.performClick();
            });
            v.findViewById(R.id.Button4_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("4"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button4.performClick();
            });
            v.findViewById(R.id.Button5_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("5"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button5.performClick();
            });
            v.findViewById(R.id.Button6_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("6"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button6.performClick();
            });
            v.findViewById(R.id.Button7_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("7"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button7.performClick();
            });
            v.findViewById(R.id.Button8_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("8"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button8.performClick();
            });
            v.findViewById(R.id.Button9_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("9"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button9.performClick();
            });
            v.findViewById(R.id.Button0_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("0"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button0.performClick();
            });
            v.findViewById(R.id.Button010_dual).setOnClickListener(view -> {
                try { ((android.widget.EditText)v.findViewById(R.id.etcashinputnum_dual)).append("010"); } catch (Exception ignored) {}
                kr.co.nicevan.androidnvcat.KeyPadNumber.button010.performClick();
            });

            return v;
        }

        // gb == 4 : 서명패드
        if (gb == 4) {
            View v = inflater.inflate(R.layout.activity_different_sign_pad, null, false);
            View card = v.findViewById(R.id.popup_card);
            if (card != null) {
                card.setBackgroundResource(R.drawable.bg_dialog);
            }

            android.widget.ImageButton cancelBtn = v.findViewById(R.id.cancel_button_dual);
            android.widget.Button clearBtn = v.findViewById(R.id.clear_button_dual);
            android.widget.Button saveBtn = v.findViewById(R.id.save_button_dual);
            com.github.gcacace.signaturepad.views.SignaturePad pad = v.findViewById(R.id.signature_pad_dual);

            if (saveBtn != null) saveBtn.setEnabled(false);
            if (clearBtn != null) clearBtn.setEnabled(false);

            if (cancelBtn != null) {
                cancelBtn.setOnClickListener(view -> {
                    // DifferentDisplay: mCancelButton.performClick()
                    try { kr.co.nicevan.androidnvcat.SignPad.mCancelButton.performClick(); } catch (Exception ignored) {}
                    dismiss(displayId);
                });
            }

            if (pad != null) {
                pad.setOnSignedListener(new com.github.gcacace.signaturepad.views.SignaturePad.OnSignedListener() {
                    @Override public void onStartSigning() {}
                    @Override public void onSigned() {
                        if (saveBtn != null) saveBtn.setEnabled(true);
                        if (clearBtn != null) clearBtn.setEnabled(true);
                        if (cancelBtn != null) cancelBtn.setEnabled(true);
                    }
                    @Override public void onClear() {
                        if (saveBtn != null) saveBtn.setEnabled(false);
                        if (clearBtn != null) clearBtn.setEnabled(false);
                        if (cancelBtn != null) cancelBtn.setEnabled(true);
                    }
                });
            }

            if (clearBtn != null) {
                clearBtn.setOnClickListener(view -> {
                    try { if (pad != null) pad.clear(); } catch (Exception ignored) {}
                });
            }

            if (saveBtn != null) {
                saveBtn.setOnClickListener(view -> {
                    try {
                        android.graphics.Bitmap signatureBitmap = pad.getSignatureBitmap();

                        android.graphics.Bitmap largeMono = android.graphics.Bitmap.createScaledBitmap(signatureBitmap, 128, 64, true);
                        android.graphics.Bitmap smallMono = kr.co.nicevan.androidnvcat.SignPad.createBlackAndWhite(largeMono);

                        java.nio.ByteBuffer saveBuffer = bmpSaveBuffer(smallMono); // 아래 static helper
                        kr.co.nicevan.androidnvcat.SignPad.in = saveBuffer.array();
                        kr.co.nicevan.androidnvcat.SignPad.iTSuse = 1;

                        kr.co.nicevan.androidnvcat.SignPad.mSaveButton.performClick();
                    } catch (Exception ignored) {}
                });
            }

            return v;
        }

        // gb == 1/2/3(팝업류는 activity_popup_dialog 사용)
        View v = inflater.inflate(R.layout.activity_popup_dialog, null, false);
        View card = v.findViewById(R.id.popup_card);
        if (card != null) {
            card.setBackgroundResource(R.drawable.bg_dialog);
        }

        android.widget.TextView tvTitle = v.findViewById(R.id.tvpopup);
        android.widget.TextView tvDock  = v.findViewById(R.id.tv_guide_dock_card);
        android.widget.TextView tvPay   = v.findViewById(R.id.tv_guide_payment);
        android.widget.TextView tvTimer = v.findViewById(R.id.tv_guide_auto_cancel);
        android.widget.ImageView iv     = v.findViewById(R.id.iv_card);
        android.widget.Button bt        = v.findViewById(R.id.btrooting);

        //gb == 1 : 팝업만
        if (gb == 1) {
            if (tvTitle != null) tvTitle.setText(text != null ? text : "");
            if (bt != null) bt.setVisibility(View.GONE);
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setVisibility(View.GONE);
            if (tvPay != null) tvPay.setVisibility(View.GONE);
            if (tvTimer != null) tvTimer.setVisibility(View.GONE);
            return v;
        }

        //gb == 3 : 메시지 + 닫기
        if (gb == 3) {
            if (tvTitle != null) tvTitle.setText(text != null ? text : "");
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setVisibility(View.GONE);
            if (tvPay != null) tvPay.setVisibility(View.GONE);
            if (tvTimer != null) tvTimer.setVisibility(View.GONE);

            if (bt != null) {
                bt.setText("닫기");
                bt.setOnClickListener(view -> {
                    try {
                        // DifferentDisplay: PopupClose() + moveTaskToBack 조건부
                        kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose();

                        // overlay context가 Activity면 moveTaskToBack 가능
                        if (ctx instanceof android.app.Activity) {
                            android.app.Activity a = (android.app.Activity) ctx;
                            try { a.moveTaskToBack(true); } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {}
                    dismiss(displayId);
                });
            }
            return v;
        }

        //gb == 2 : 팝업 + EOT
        if (tvTitle != null) tvTitle.setText("신용카드 결제");
        if (bt != null) {
            bt.setText("요청취소");
            bt.setOnClickListener(view -> {
                try {
                    byte[] EOT = new byte[1];
                    EOT[0] = 0x04;

                    int rt = kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("Readertype", 0);

                    if (rt == 3) { // POSBANK
                        try { kr.co.nicevan.androidnvcat.shared.SharedArray.scr.sendEot(); } catch (Exception ignored) {}
                    } else if (rt == 2 || rt == 7) { // OKPOS (+7 포함)
                        try {
                            if (kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad
                                    || kr.co.nicevan.androidnvcat.shared.SharedArray.isSign) {
                                kr.co.nicevan.androidnvcat.shared.SharedArray.mUart.DataSend(
                                        kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("sPortnum", 0),
                                        EOT, EOT.length
                                );
                            } else {
                                kr.co.nicevan.androidnvcat.shared.SharedArray.mUart.DataSend(
                                        kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("Portnum", 0),
                                        EOT, EOT.length
                                );
                            }
                        } catch (Exception ignored) {}
                    } else {
                        try { if (kr.co.nicevan.androidnvcat.shared.SharedArray.usbService != null) kr.co.nicevan.androidnvcat.shared.SharedArray.usbService.write(EOT); } catch (Exception ignored) {}
                    }

                    // DifferentDisplay: 특정 조건이면 RECVBuf 세팅 후 PopupClose
                    if (kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad
                            || kr.co.nicevan.androidnvcat.shared.SharedArray.isSign
                            || kr.co.nicevan.androidnvcat.shared.SharedArray.func_code == 0xD3
                            || kr.co.nicevan.androidnvcat.shared.SharedArray.func_code == 0x71
                            || kr.co.nicevan.androidnvcat.shared.SharedArray.func_code == 0x93) {
                        try {
                            kr.co.nicevan.androidnvcat.shared.SharedArray.RECVBuf[0] = 0x04;
                            kr.co.nicevan.androidnvcat.shared.SharedArray.RECVBuf[4] = 0xCD;
                        } catch (Exception ignored) {}
                    }

                    kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose();
                } catch (Exception ignored) {}

                dismiss(displayId);
            });
        }

        //func_code 분기
        int func = kr.co.nicevan.androidnvcat.shared.SharedArray.func_code;

        if (func == 0x45) { // 고객식별번호
            if (tvTitle != null) tvTitle.setText("서명패드 연동");
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setText("고객식별번호 입력 중입니다.");
            if (tvPay != null) tvPay.setVisibility(View.GONE);
            if (tvTimer != null) tvTimer.setVisibility(View.GONE);

        } else if (kr.co.nicevan.androidnvcat.shared.SharedArray.isMultipad) { // PIN
            if (tvTitle != null) tvTitle.setText("서명패드 연동");
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setText("PIN 입력 중입니다.");
            if (tvPay != null) tvPay.setVisibility(View.GONE);

        } else if (kr.co.nicevan.androidnvcat.shared.SharedArray.isSign) { // 서명
            if (tvTitle != null) tvTitle.setText("서명패드 연동");
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setText("서명 해주세요");
            if (tvPay != null) tvPay.setVisibility(View.GONE);

        } else if (func == 0xD3) { // 바코드
            if (tvTitle != null) tvTitle.setText("서명패드 연동");
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setText("바코드리딩 해주세요");
            if (tvPay != null) tvPay.setVisibility(View.GONE);
            if (tvTimer != null) tvTimer.setVisibility(View.GONE);

        } else if (func == 0x71) { // RF 잔액조회
            if (tvTitle != null) tvTitle.setText("서명패드 연동");
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setText("RF 리딩 해주세요");
            if (tvTimer != null) tvTimer.setVisibility(View.GONE);

        } else if (func == 0x93) { // Mifare 사원증ID
            if (tvTitle != null) tvTitle.setText("서명패드 연동");
            if (iv != null) iv.setVisibility(View.GONE);
            if (tvDock != null) tvDock.setText("사원증 리딩 해주세요");
            if (tvTimer != null) tvTimer.setVisibility(View.GONE);

        } else if (func == 0x6C || func == 0x9C
                || ((kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("Readertype", 0) == 5
                || kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("Readertype", 0) == 6
                || kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("Readertype", 0) == 7)
                && (func == 'A' || func == 'R' || func == 'S' || func == 0xCF))) {

            // IC 삽입 안내
            if (iv != null) {
                int rt = kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("Readertype", 0);
                if (rt == 5 || rt == 6 || rt == 7) iv.setImageResource(R.drawable.payment_processing_img_iccard_tit);
                else iv.setImageResource(R.drawable.payment_processing_img_iccard);
            }
            if (tvDock != null) tvDock.setText("신용카드를 그림과 같이\nIC카드 리더기에 꽂아주세요");
            if (tvPay != null) tvPay.setText("결제가 완료될 때까지\n카드를 빼지 마세요!");

            // PopupOpenEOT: 기본 문구(타이머로 교체될 수 있음)
            if (tvTimer != null) {
                tvTimer.setVisibility(View.VISIBLE);
                tvTimer.setText(kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getString("Timeout", "30")
                        + "초이내로 결제하지 않으면 자동 취소됩니다");
            }

            // CHKCARDIN/CHKCARDBIN 시 숨김(원본 동일)
            if ("CHKCARDIN".equals(text) || "CHKCARDBIN".equals(text)) {
                if (tvPay != null) tvPay.setVisibility(View.GONE);
                if (tvTimer != null) tvTimer.setVisibility(View.GONE);
            }

        } else {
            if (iv != null) iv.setImageResource(R.drawable.payment_processing_img_fallback);

            int rt = kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getInt("Readertype", 0);
            if (rt == 5 || rt == 6 || rt == 7) {
                if (iv != null) iv.setImageResource(R.drawable.payment_processing_img_fallback_tit);
                if (tvDock != null) tvDock.setText("신용카드를 그림과 같이 제거해주세요");
            } else {
                if (tvDock != null) tvDock.setText("신용카드 마그네틱을\n그림과 같이 리더기에 긁어주세요");
            }

            if (tvPay != null) tvPay.setVisibility(View.GONE);

            if (tvTimer != null) {
                tvTimer.setVisibility(View.VISIBLE);
                tvTimer.setText(kr.co.nicevan.androidnvcat.MainActivity.mSharedManager.getPreferences().getString("Timeout", "30")
                        + "초이내로 결제하지 않으면 자동 취소됩니다");
            }
        }

        // Msgbox=false이면 countdown 적용(0x6E/0x9E도 포함)
        boolean msgbox = kr.co.nicevan.androidnvcat.shared.SharedManager.getInstance(ctx).getPreferences().getBoolean("Msgbox", false);
        if (!msgbox) {
            int timeoutSec = kr.co.nicevan.androidnvcat.shared.SharedArray.parseIntSafe(
                    kr.co.nicevan.androidnvcat.shared.SharedManager.getInstance(ctx).getPreferences().getString("Timeout", "30"), 30);

            if (tvTimer != null && tvTimer.getVisibility() == View.VISIBLE && !(isSign || isMultipad) && bNoTimer == false) {
                startOverlayCountdown(displayId, v, timeoutSec);
            }
        }

        return v;
    }

    // overlay countdown 타이머 메서드
    private static void startOverlayCountdown(int displayId, View contentRoot, int totalSeconds) {
        stopOverlayCountdown(displayId);
        if (contentRoot == null || totalSeconds <= 0) return;

        final java.lang.ref.WeakReference<android.widget.TextView> tvRef =
                new java.lang.ref.WeakReference<>(contentRoot.findViewById(R.id.tv_guide_auto_cancel));

        android.widget.TextView tv = tvRef.get();
        if (tv != null) {
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(android.graphics.Color.parseColor("#000000"));
            tv.setText("남은 시간 : " + totalSeconds + "초");
        }

        android.os.CountDownTimer t = new android.os.CountDownTimer(totalSeconds * 1000L, 1000L) {
            @Override public void onTick(long millisUntilFinished) {
                android.widget.TextView tx = tvRef.get();
                if (tx != null) {
                    int sec = (int) Math.ceil(millisUntilFinished / 1000.0);
                    tx.setText("남은 시간 : " + sec + "초");
                }
            }

            @Override public void onFinish() {
                android.widget.TextView tx = tvRef.get();
                if (tx != null) tx.setText("시간 초과");

                try { kr.co.nicevan.androidnvcat.shared.SharedArray.PopupClose(); } catch (Exception ignored) {}
                dismiss(displayId);
            }
        }.start();

        overlayTimers.put(displayId, t);
    }

    private static void stopOverlayCountdown(int displayId) {
        android.os.CountDownTimer t = overlayTimers.remove(displayId);
        if (t != null) {
            try { t.cancel(); } catch (Exception ignored) {}
        }
    }

    // dismiss
    public static void dismiss(int displayId) {
        try {
            stopOverlayCountdown(displayId);

            View root = overlayRoots.remove(displayId);
            WindowManager wm = windowManagers.remove(displayId);

            if (root != null && wm != null) {
                wm.removeViewImmediate(root);
            }
        } catch (Exception ignored) {}
    }

    public static void dismissAll() {
        try {
            for (Integer id : new java.util.ArrayList<>(overlayRoots.keySet())) {
                dismiss(id);
            }
        } catch (Exception ignored) {}
    }



    //디스플레이 getID 하는 로직 공통 메서드 정의
    public static void showOnAllExternalDisplaysByGb(Context context, int gb, String text) {
        if (context == null) return;

        Context appCtx = context.getApplicationContext();
        DisplayManager dm =
                (DisplayManager) appCtx.getSystemService(Context.DISPLAY_SERVICE);
        if (dm == null) return;

        Display[] displays = dm.getDisplays();
        if (displays == null) return;

        for (Display d : displays) {
            if (d == null) continue;
            if (d.getDisplayId() == Display.DEFAULT_DISPLAY) continue;

            showOnDisplayByGb(appCtx, d, gb, text);
        }
    }


    //서명 BMP 저장 버퍼 메서드
    private static java.nio.ByteBuffer bmpSaveBuffer(android.graphics.Bitmap mBitmap) throws Exception {
        int mWidth = mBitmap.getWidth();
        int mHeight = mBitmap.getHeight();

        int[] mPixel = new int[mWidth * mHeight];
        int imgSize = mPixel.length / 8;
        int imgOffset = 0x3E;
        int fileSize = imgSize + imgOffset;

        mBitmap.getPixels(mPixel, 0, mWidth, 0, 0, mWidth, mHeight);

        java.nio.ByteBuffer mBuffer = java.nio.ByteBuffer.allocate(fileSize);

        // header
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

        // palette
        mBuffer.put(putInt(0));         // black
        mBuffer.put(putInt(0x00FFFFFF)); // white

        int height = mHeight;
        int width = mWidth;

        while (height > 0) {
            int startPosition = (height - 1) * width;
            int endPosition = height * width;
            int[] iTemp = new int[8];

            for (int i = startPosition; i < endPosition;) {
                if ((i + 7) <= endPosition) {
                    for (int j = 7; j >= 0; j--) iTemp[j] = mPixel[i++];
                } else {
                    int mLength = endPosition - i;
                    for (int j = 7; j >= (8 - mLength); j--) iTemp[j] = mPixel[i++];
                    for (int j = (7 - mLength); j >= 0; j--) iTemp[j] = 0x00FFFFFF;
                }
                mBuffer.put(putIntToBit(iTemp));
            }
            height--;
        }

        return mBuffer;
    }

    private static byte[] putInt(int value) {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0x000000FF);
        b[1] = (byte) ((value & 0x0000FF00) >> 8);
        b[2] = (byte) ((value & 0x00FF0000) >> 16);
        b[3] = (byte) ((value & 0xFF000000) >> 24);
        return b;
    }

    private static byte[] putShort(short value) {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0x00FF);
        b[1] = (byte) ((value & 0xFF00) >> 8);
        return b;
    }

    private static byte putIntToBit(int[] value) {
        byte bReturn = (byte) 0xFF;
        byte[] b = new byte[3];

        for (int i = 0; i < value.length; i++) {
            b[0] = (byte) (value[i] & 0x000000FF);
            b[1] = (byte) ((value[i] & 0x0000FF00) >> 8);
            b[2] = (byte) ((value[i] & 0x00FF0000) >> 16);

            if ((b[0] != (byte) 0xFF) || (b[1] != (byte) 0xFF) || (b[2] != (byte) 0xFF)) {
                bReturn -= (byte) Math.pow(2, i);
            }
        }
        return bReturn;
    }
}
