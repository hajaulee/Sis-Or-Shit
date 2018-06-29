package com.hajaulee.sisorshit;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

/**
 * Created by HaJaU on 07-11-17.
 */

public class WidgetUtils {
    private Activity sender;
    public static final String ACTION = "ACTION";
    public static final String OPEN_MARK_LIST = "OPEN_MARK_LIST";
    static private final String CHANNEL_1 = "CHANNEL_1";


    public static void sendNotification(Context caller, String title, String message) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(caller, CHANNEL_1);
        Intent intent = new Intent(caller, MainActivity.class);
        intent.putExtra(ACTION, OPEN_MARK_LIST);
        PendingIntent contentIntent =
                PendingIntent.getActivity(caller, 0, intent, FLAG_ONE_SHOT);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.mark_updated)
//                .setLargeIcon(BitmapFactory.decodeResource(caller.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo("INFO")
//                .setStyle(
                .setContentIntent(contentIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            b.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }else{
            b.setPriority(Notification.PRIORITY_MAX);
        }
        NotificationManager nm = (NotificationManager) caller.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = b.build();

        if (nm != null) {
            nm.notify(1, notification);
        }
    }

    public static void notifyNewestSubject(Context caller, int oldCount, int newCount) {
        int newSubjectCount = MainActivity.bangDiemAll.size();
        if (newSubjectCount == 0) {
            sendNotification(caller, "Error: " + oldCount + "x" + newCount, "Not found");
//            MainActivity.getInstance().loadMarkList();
            return;
        }
        String[] newestSubject = MainActivity.bangDiemAll.get(newSubjectCount - 1).split("__");
        Subject subject = Subject.createSubject(newestSubject);
        if (subject != null) {
            sendNotification(caller,
                    "Cập nhật bảng điểm: " +  oldCount + " + " + (newCount - oldCount)+ " môn" ,
                    "GK: " + subject.getMidTermScore() +
                            "  CK: " + subject.getFinalExamScore() +
                            "  Loại: " + subject.getRank() +
                            "  Môn: " + subject.getCourseName());
        }

    }

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
                MainActivity.bangDiem.addAll(MainActivity.bangDiemAll);
            } else {
                String text = ((TextView) selectedItemView).getText().toString();
                if (!text.contains("20")) {
                    int mon = 0;
                    int tin = 0;
                    text = text.substring(text.lastIndexOf(' ') + 1);
                    String[] rank = text.split("/");
                    for (String a : MainActivity.bangDiemAll)
                        if (a.substring(a.length() - 3).contains(rank[0]) || a.substring(a.length() - 3).contains(rank[rank.length - 1])) {
                            MainActivity.bangDiem.add(a);
                            mon++;
                            tin += Integer.parseInt(a.split("__")[3]);
                        }
                    thanhTich.setText(Html.fromHtml("<b>Tổng số: " + mon + " môn, " + tin + " tín</b>"));
                } else {
                    text = text.substring(text.lastIndexOf(' ') + 1);
                    for (String a : MainActivity.bangDiemAll) {
                        if (a.contains(text))
                            MainActivity.bangDiem.add(a);
                        Log.d(text, a);
                    }
                    for (String a : MainActivity.ketQuaHocTap)
                        if (a.contains(text)) {
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
