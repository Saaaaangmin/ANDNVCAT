package kr.co.nicevan.androidnvcat;

import static kr.co.nicevan.androidnvcat.shared.SharedArray.dbHelper;
import static kr.co.nicevan.androidnvcat.shared.SharedManager.bLogUse;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import kr.co.nicevan.androidnvcat.shared.ChkValidItem;
import kr.co.nicevan.androidnvcat.shared.ChkValidListAdapter;
import kr.co.nicevan.androidnvcat.shared.SharedManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FourFragment extends Fragment {

    private ListView mlistview = null;
    Button btnchkvalidresult;

    public FourFragment() {
        SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 무결성점검내역 탭입니다.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_four, container, false);

        btnchkvalidresult = view.findViewById(R.id.btnchkvalidresult);
        btnchkvalidresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedManager.LogDebug(bLogUse, "debugjy", "[NVCAT] 무결성점검 결과조회 버튼 클릭되었습니다.");

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM CHKVALIDTABLE", null); //CHKVALID테이블 조회

                ArrayList<ChkValidItem> arrChkvaliditem = new ArrayList<>(); //
                while (cursor.moveToNext()) {
                    ChkValidItem mchkvaliditem = new ChkValidItem();
                    mchkvaliditem.str_id = cursor.getString(0);
                    mchkvaliditem.strdate = cursor.getString(1);
                    mchkvaliditem.strresult = cursor.getString(2);
                    mchkvaliditem.strreason = cursor.getString(3);
                    arrChkvaliditem.add(mchkvaliditem);

                    mlistview = (ListView) view.findViewById(R.id.listView); //ListView - Adapter 생성 및 연결
                    ChkValidListAdapter mlistadapter = new ChkValidListAdapter(arrChkvaliditem);
                    mlistview.setAdapter(mlistadapter);
                    mlistview.setSelection(mlistadapter.getCount() - 1); //가장하단조회
                }
            }
        });

        return view;
    }
}
