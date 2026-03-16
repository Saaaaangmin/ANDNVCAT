package kr.co.nicevan.androidnvcat.shared;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.nicevan.androidnvcat.R;

public class DetailDealListAdapter extends BaseAdapter {
    LayoutInflater inflater = null;
    private ArrayList<DealItem> m_oData = null;
    private int nListCnt = 0;

    public DetailDealListAdapter(ArrayList<DealItem> _oData) {
        m_oData = _oData;
        nListCnt = m_oData.size();
    }

    @Override
    public int getCount() {
        Log.i("TAG", "getCount");
        return nListCnt;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final Context context = parent.getContext();
            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.listview_dealresult_detail, parent, false);
        }

        TextView tvtv1 = (TextView) convertView.findViewById(R.id.dtv1);
        TextView tvtv2 = (TextView) convertView.findViewById(R.id.dtv2);
        TextView tvtv3 = (TextView) convertView.findViewById(R.id.dtv3);
        TextView tvtv4 = (TextView) convertView.findViewById(R.id.dtv4);
        TextView tvtv5 = (TextView) convertView.findViewById(R.id.dtv5);

        TextView tvtv6 = (TextView) convertView.findViewById(R.id.dtv6);
        TextView tvtv7 = (TextView) convertView.findViewById(R.id.dtv7);
        TextView tvtv8 = (TextView) convertView.findViewById(R.id.dtv8);
        TextView tvtv9 = (TextView) convertView.findViewById(R.id.dtv9);
        TextView tvtv10 = (TextView) convertView.findViewById(R.id.dtv10);

        TextView tvtv11 = (TextView) convertView.findViewById(R.id.dtv11);
        TextView tvtv12 = (TextView) convertView.findViewById(R.id.dtv12);
        TextView tvtv13 = (TextView) convertView.findViewById(R.id.dtv13);
        TextView tvtv14 = (TextView) convertView.findViewById(R.id.dtv14);
        TextView tvtv15 = (TextView) convertView.findViewById(R.id.dtv15);

        TextView tvtv16 = (TextView) convertView.findViewById(R.id.dtv16);
        TextView tvtv17 = (TextView) convertView.findViewById(R.id.dtv17);
        TextView tvtv18 = (TextView) convertView.findViewById(R.id.dtv18);
        TextView tvtv19 = (TextView) convertView.findViewById(R.id.dtv19);
        TextView tvtv20 = (TextView) convertView.findViewById(R.id.dtv20);

        TextView tvtv21 = (TextView) convertView.findViewById(R.id.dtv21);
        TextView tvtv22 = (TextView) convertView.findViewById(R.id.dtv22);
        TextView tvtv23 = (TextView) convertView.findViewById(R.id.dtv23); //20200129 : 포인트거래
        TextView tvtv24 = (TextView) convertView.findViewById(R.id.dtv24); //OSM20250814 : 매입사코드 추가
        TextView tvtv25 = (TextView) convertView.findViewById(R.id.dtv25); //OSM20250814 : 발급사코드 추가

        tvtv1.setText(m_oData.get(position).str_id);
        tvtv2.setText(m_oData.get(position).str_date);
        tvtv3.setText(m_oData.get(position).str_dealtp);
        tvtv4.setText(m_oData.get(position).str_dealgb);
        tvtv5.setText(m_oData.get(position).str_cardno);

        tvtv6.setText(m_oData.get(position).str_money + " 원");
        tvtv7.setText(m_oData.get(position).str_tax + " 원");
        tvtv8.setText(m_oData.get(position).str_bongsa + " 원");
        tvtv9.setText(m_oData.get(position).str_halbu + " 개월");
        tvtv10.setText(m_oData.get(position).str_apprno.replaceAll(" ", ""));

        tvtv11.setText(m_oData.get(position).str_apprdate);
        tvtv12.setText(m_oData.get(position).str_tid);
        tvtv13.setText(m_oData.get(position).str_bgnm.replaceAll(" ", ""));
        tvtv14.setText(m_oData.get(position).str_minm.replaceAll(" ", ""));
        tvtv15.setText(m_oData.get(position).str_storeno.replaceAll(" ", ""));

        tvtv16.setText(m_oData.get(position).str_recvmsg.replaceAll(" ", ""));
        tvtv17.setText(m_oData.get(position).str_recvcode);
        tvtv18.setText(m_oData.get(position).str_bal);
        tvtv19.setText(m_oData.get(position).str_wcc);
        tvtv20.setText(m_oData.get(position).str_cardgb);

        tvtv21.setText(m_oData.get(position).str_msgno);
        tvtv22.setText(m_oData.get(position).str_dealno);
        tvtv23.setText(m_oData.get(position).str_msgtxt); //20200129 : 포인트거래
        tvtv24.setText(m_oData.get(position).str_micode); //OSM20250814 : 매입사코드 추가
        tvtv25.setText(m_oData.get(position).str_bgcode); //OSM20250814 : 발급사코드 추가

        return convertView;
    }
}
