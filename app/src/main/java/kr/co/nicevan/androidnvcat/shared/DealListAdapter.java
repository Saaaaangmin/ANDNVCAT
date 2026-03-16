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

public class DealListAdapter extends BaseAdapter {
    LayoutInflater inflater = null;
    private ArrayList<DealItem> m_oData = null;
    private int nListCnt = 0;

    public DealListAdapter(ArrayList<DealItem> _oData) {
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
            convertView = inflater.inflate(R.layout.listview_dealresult, parent, false);
        }

        TextView tvtv1 = (TextView) convertView.findViewById(R.id.tv1);
        TextView tvtv2 = (TextView) convertView.findViewById(R.id.tv2);
        TextView tvtv3 = (TextView) convertView.findViewById(R.id.tv3);
        TextView tvtv4 = (TextView) convertView.findViewById(R.id.tv4);
        TextView tvtv5 = (TextView) convertView.findViewById(R.id.tv5);
        TextView tvtv6 = (TextView) convertView.findViewById(R.id.tv6);
        TextView tvtv10 = (TextView) convertView.findViewById(R.id.tv10);

        tvtv1.setText(m_oData.get(position).str_id);
        tvtv2.setText(m_oData.get(position).str_date);
        tvtv3.setText(m_oData.get(position).str_dealtp);
        tvtv4.setText(m_oData.get(position).str_dealgb);
        tvtv5.setText(m_oData.get(position).str_cardno);
        tvtv6.setText(m_oData.get(position).str_money.replaceAll("^0+", ""));
        tvtv10.setText(m_oData.get(position).str_apprno);

        return convertView;
    }
}
