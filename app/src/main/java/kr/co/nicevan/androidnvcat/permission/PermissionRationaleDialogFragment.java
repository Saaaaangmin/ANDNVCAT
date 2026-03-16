package kr.co.nicevan.androidnvcat.permission;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

public class PermissionRationaleDialogFragment extends DialogFragment implements DialogInterface.OnClickListener
{
    private static final String PERMISSION = "PERMISSION";
    private static final String REQUEST_CODE = "REQUEST_CODE";
    private static final String RATIONALE_MESSAGE = "RATIONALE_MESSAGE";
    private String mRationaleMessage;

    public static PermissionRationaleDialogFragment getInstance(final String message)
    {
        PermissionRationaleDialogFragment fragment = new PermissionRationaleDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString(RATIONALE_MESSAGE, message);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRationaleMessage = getArguments().getString(RATIONALE_MESSAGE);
        }
    }

    // Listener callback
    public interface PermissionDialogListener
    {
        void onRequestPermission();
        void onCancellingPermissionRationale();
    }

    @NonNull
    @Override
//    public Dialog onCreateDialog(final Bundle savedInstanceState) {
//        final AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).setTitle("권한이 필요합니다")
//                .setMessage(mRationaleMessage)
//                .setPositiveButton("요청", this)
//                .setNegativeButton("취소", this).create();
//        alertDialog.setCanceledOnTouchOutside(false);
//
//        return alertDialog;
//    }

    //OSM20250805 : SDK 28 -> 26 변경에 따른 context 변경
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Context context = getContext();
        if (context == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle("권한이 필요합니다")
                .setMessage(mRationaleMessage != null ? mRationaleMessage : "이 기능을 사용하려면 권한이 필요합니다.")
                .setPositiveButton("확인", this)
                .setNegativeButton("취소", this)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);

        return alertDialog;
    }

    @Override
//    public void onClick(final DialogInterface dialogInterface, final int position)
//    {
//        if (position == DialogInterface.BUTTON_POSITIVE)
//        {
//            Fragment fragment = getParentFragment();
//            if (fragment != null)
//           {
//                ((PermissionDialogListener) getParentFragment()).onRequestPermission();
//            }
//            else
//            {
//                ((PermissionDialogListener) requireActivity()).onRequestPermission();
//            }
//        }
//        else if (position == DialogInterface.BUTTON_NEGATIVE)
//        {
//            Fragment fragment = getParentFragment();
//            if (fragment != null)
//            {
//                ((PermissionDialogListener) getParentFragment()).onCancellingPermissionRationale();
//            }
//            else
//            {
//                ((PermissionDialogListener) requireActivity()).onCancellingPermissionRationale();
//            }
//        }
//    }

    //OSM20250805 : SDK 28 -> 26 변경에 따른 Activity 변경
    public void onClick(final DialogInterface dialogInterface, final int position)
    {
        Fragment fragment = getParentFragment();
        if (position == DialogInterface.BUTTON_POSITIVE)
        {
            if (fragment instanceof PermissionDialogListener) {
                ((PermissionDialogListener) fragment).onRequestPermission();
            } else if (getActivity() instanceof PermissionDialogListener) {
                ((PermissionDialogListener) getActivity()).onRequestPermission();
            }
        }
        else if (position == DialogInterface.BUTTON_NEGATIVE)
        {
            if (fragment instanceof PermissionDialogListener) {
                ((PermissionDialogListener) fragment).onCancellingPermissionRationale();
            } else if (getActivity() instanceof PermissionDialogListener) {
                ((PermissionDialogListener) getActivity()).onCancellingPermissionRationale();
            }
        }
    }
}

