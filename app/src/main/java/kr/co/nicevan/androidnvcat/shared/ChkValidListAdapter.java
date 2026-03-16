package kr.co.nicevan.androidnvcat.shared;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.nicevan.androidnvcat.R;

public class ChkValidListAdapter extends BaseAdapter {
    LayoutInflater inflater = null;
    private ArrayList<ChkValidItem> m_oData = null;
    private int nListCnt = 0;

    public ChkValidListAdapter(ArrayList<ChkValidItem> _oData) {
        m_oData = _oData;
        nListCnt = m_oData.size();
    }

    @Override
    public int getCount() {
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
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        TextView oTextTitle = (TextView) convertView.findViewById(R.id.text_id);
        TextView oTextDate = (TextView) convertView.findViewById(R.id.textdate);
        TextView oTextResult = (TextView) convertView.findViewById(R.id.textresult);
        TextView oTextReason = (TextView) convertView.findViewById(R.id.textreason);

        oTextTitle.setText(m_oData.get(position).str_id);
        oTextDate.setText(m_oData.get(position).strdate);
        oTextResult.setText(m_oData.get(position).strresult);
        oTextReason.setText(m_oData.get(position).strreason);

        return convertView;
    }
}
