package com.hajaulee.sisorshit;

import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * Created by HaJaU on 07-11-17.
 */

public class WidgetUtils {
    private Activity sender;

    public WidgetUtils(Activity sender) {
        this.sender = sender;
    }

    AdapterView.OnItemSelectedListener onPointListSelectorSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            MainActivity.bangDiem.clear();
            TextView thanhTich = (TextView) sender.findViewById(R.id.thanhtich);
            if (position == 0) {
                thanhTich.setText("");
                for (String a : MainActivity.bangDiemAll)
                    MainActivity.bangDiem.add(a);
            } else {
                String text = ((TextView) selectedItemView).getText().toString();
                if (text.indexOf("20") == -1) {
                    int mon = 0;
                    int tin = 0;
                    text = text.substring(text.lastIndexOf(' ') + 1);
                    String[] rank = text.split("/");
                    for (String a : MainActivity.bangDiemAll)
                        if (a.substring(a.length() - 3).indexOf(rank[0]) != -1 || a.substring(a.length() - 3).indexOf(rank[rank.length - 1]) != -1) {
                            MainActivity.bangDiem.add(a);
                            mon++;
                            tin += Integer.parseInt(a.split("__")[3]);
                        }
                    thanhTich.setText(Html.fromHtml("<b>Tổng số: " + mon + " môn, " + tin + " tín</b>"));
                } else {
                    text = text.substring(text.lastIndexOf(' ') + 1);
                    for (String a : MainActivity.bangDiemAll) {
                        if (a.indexOf(text) != -1)
                            MainActivity.bangDiem.add(a);
                        Log.d(text, a);
                    }
                    for (String a : MainActivity.ketQuaHocTap)
                        if (a.indexOf(text) != -1) {
                            String[] jav = a.split("__");
                            thanhTich.setText(Html.fromHtml("<b><font color=\"red\" >CPA:</font> " + jav[2] +
                                    "&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"green\" >GPA:</font> " + jav[1] + "</b><br>TC qua: " + jav[3] +
                                    "&nbsp;&nbsp;TC tích lũy: " + jav[4] + "&nbsp;&nbsp;TC nợ ĐK: " + jav[5] + "&nbsp;&nbsp;TC ĐK: " + jav[6]));
                            break;
                        }
                }
            }
            MainActivity.bangDiemAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }

    };
}
