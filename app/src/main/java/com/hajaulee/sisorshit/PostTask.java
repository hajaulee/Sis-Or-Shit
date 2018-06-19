package com.hajaulee.sisorshit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.hajaulee.sisorshit.PostTaskAction.LOGIN_DKSIS_WITH_CAPTCHA;
import static com.hajaulee.sisorshit.PostTaskAction.LOGIN_SIS;
import static com.hajaulee.sisorshit.PostTaskAction.LOGIN_SIS_GET_MARK_TABLE;
import static com.hajaulee.sisorshit.PostTaskAction.UPDATE_MARK_LIST;

@SuppressWarnings("deprecation")
public class PostTask extends AsyncTask<String, String, String> {
    private Activity sender = null;
    private Context context = null;
    private String captchaLink;
    private PostTaskAction task;
    private HttpResponse response;
    private HttpEntity entity;
    private static HttpClient httpclient;
    private String responseString = "";
    protected String acc;
    protected String pass;
    private boolean connected = false;
    private PostTask startPostTask;

    public void setCancel(boolean cancel) {
        isCancel = cancel;
        if (startPostTask != null)
            startPostTask.setCancel(true);
    }

    private boolean isCancel = false;

    public PostTask(Activity msender, PostTaskAction mtask) {
        this.sender = msender;
        this.task = mtask;
    }

    public PostTask(Context msender, PostTaskAction mtask) {
        this.context = msender;
        this.task = mtask;
    }

    @Override
    protected String doInBackground(String... data) {
        if (isCancel)
            return null;
        HttpParams httpParams = new BasicHttpParams();

        // It's always good to set how long they should try to connect. In this
        // this example, five seconds.
        HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
        HttpConnectionParams.setSoTimeout(httpParams, 8000);
        // Create a new HttpClient and Post Header
        httpclient = new DefaultHttpClient(httpParams);
        HttpPost httppost = null;
        Log.d("Start Task", task.name());
        try {
            Log.d("Task", task + " Pass :" + 1);
            // add data
            List<NameValuePair> nameValuePairs;
            // nameValuePairs.add(new BasicNameValuePair("data", data[0]));
            if (task.equals(LOGIN_SIS)) {
                ProgressBar bar = (ProgressBar) sender.findViewById(R.id.progressBar);
                bar.setProgress(0);
                setProgressSmoothly(bar, 25);
                httppost = new HttpPost("http://sis.hust.edu.vn/");
                nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", data[0]));
                nameValuePairs.add(new BasicNameValuePair("ctl00$cLogIn1$tb_cLogIn_User", data[1]));
                nameValuePairs.add(new BasicNameValuePair("ctl00$cLogIn1$tb_cLogIn_Pass", data[2]));
                acc = data[1];
                pass = data[2];
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                Log.d("Task", task + " Pass :" + 2);
                setProgressSmoothly(bar, 90);
                response = httpclient.execute(httppost);
                entity = response.getEntity();
                responseString = EntityUtils.toString(entity, "UTF-8");
            } else if (task.equals(LOGIN_SIS_GET_MARK_TABLE) || task.equals(UPDATE_MARK_LIST)) {
                httppost = new HttpPost("http://sis.hust.edu.vn/");
                nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", data[0]));
                nameValuePairs.add(new BasicNameValuePair("ctl00$cLogIn1$tb_cLogIn_User", data[1]));
                nameValuePairs.add(new BasicNameValuePair("ctl00$cLogIn1$tb_cLogIn_Pass", data[2]));
                acc = data[1];
                pass = data[2];
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                Log.d("Task", task + " Pass :" + 2);
                response = httpclient.execute(httppost);
                entity = response.getEntity();
                responseString = EntityUtils.toString(entity, "UTF-8");
                int vLogout = responseString.indexOf("vLogout");
                if (vLogout == -1) {
                    //Toast.makeText(sender!=null?sender:context, "Có lỗi xảy ra!", Toast.LENGTH_LONG).show();
                    Log.e(UpdateMarkService.TAG, "Mark list load fail");
//                    Log.e(UpdateMarkService.TAG, acc + pass);
                    return "";
                }
                httppost = new HttpPost("http://sis.hust.edu.vn/ModuleGradeBook/StudentCourseMarks.aspx");
                response = httpclient.execute(httppost);
                entity = response.getEntity();
                responseString = EntityUtils.toString(entity, "UTF-8");
            } else if (task.equals(LOGIN_DKSIS_WITH_CAPTCHA)) {
                boolean flag = true;
                acc = data[1];
                pass = data[2];
                Log.d("Hello", "Log for fun");
                httppost = new HttpPost("http://dk-sis.hust.edu.vn/Users/Login.aspx");
                httppost.addHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
                printToScreen("Khởi tạo kết nối");
                while (flag && !isCancel) {
                    Log.d("Start", "tack 2");
                    response = MainActivity.registerHttpClient.execute(httppost);
                    Log.d("Task", task + " Pass :" + 2);
                    entity = response.getEntity();
                    responseString = EntityUtils.toString(entity, "UTF-8");
                    String subjectTrtabId = "ctl00_ctl00_ASPxSplitter1_Content_ContentSplitter_MainContent_ASPxCallbackPanel1_gvRegisteredList_DXDataRow";
                    int idPos = responseString.indexOf(subjectTrtabId);
                    // printToScreen("Loading captcha");
                    toOutputFile("pre_dk-sis.html", responseString);
                    String captchaLink = getLinkCaptcha(responseString);
                    Bitmap capImage = getSmallBitmapFromNet(captchaLink);
                    // printToScreen("Captcha loaded");
                    if (capImage != null || idPos != -1) {
                        if (idPos == -1) {
                            Log.d("Task", task + " Pass :" + 3);
                            //imCaptcha = capImage;
                            String captchaText = StaticFunc.captchaToText(capImage, MainActivity.digit);
                            Log.d("Trying with Captcha::", "Captcha::" + captchaText);
                            showLoginProgress("Captcha:" + captchaText);
                            // Show Image function is deleted here
                            int vstaPos = responseString.indexOf("id=\"__VIEWSTATE\"");
                            Log.d("vspos", "" + vstaPos);
                            String viewState = responseString.substring(responseString.indexOf("value", vstaPos) + 7,
                                    responseString.indexOf("\"", vstaPos + 100));
                            nameValuePairs = new ArrayList<NameValuePair>(15);
                            nameValuePairs.add(
                                    new BasicNameValuePair("tbUserName$State", "{\"rawValue\":\"" + acc + "\"}"));
                            nameValuePairs.add(new BasicNameValuePair("tbUserName", acc));
                            nameValuePairs.add(new BasicNameValuePair("tbPassword$State",
                                    "{&quot;rawValue&quot;:&quot;" + pass + "&quot;}"));
                            nameValuePairs.add(new BasicNameValuePair("tbPassword", pass));
                            nameValuePairs.add(new BasicNameValuePair("ccCaptcha$TB$State",
                                    "{&quot;validationState&quot;:&quot;&quot;}"));
                            nameValuePairs.add(new BasicNameValuePair("ccCaptcha$TB", captchaText));
                            // This is most important __VIEWSTATE
                            nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", viewState));
                            Log.d("state", viewState);
                            toOutputFile("state.txt", viewState);
                            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                            response = MainActivity.registerHttpClient.execute(httppost);
                            entity = response.getEntity();
                            responseString = EntityUtils.toString(entity, "UTF-8");
                        }
                        idPos = responseString.indexOf(subjectTrtabId);
                        int lbStatusPos = responseString.indexOf("id=\"lbStatus");
                        int ccCaptcha_TB_EC_Pos = responseString.indexOf("id=\"ccCaptcha_TB_EC");

                        String lbStatus = "";
                        String ccCaptcha_TB_EC = "Error";
                        if (lbStatusPos != -1) {
                            lbStatus = Html
                                    .fromHtml(responseString.substring(responseString.indexOf(">", lbStatusPos) + 1,
                                            responseString.indexOf("<", lbStatusPos)))
                                    .toString();
                            if (lbStatus.indexOf("Captcha") == -1) {
                                printToScreen(lbStatus);
                                sender.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sender.findViewById(R.id.start).setEnabled(true);
                                        //sender.findViewById(R.id.fab).setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                        if (ccCaptcha_TB_EC_Pos != -1) {
                            ccCaptcha_TB_EC = responseString.substring(ccCaptcha_TB_EC_Pos, ccCaptcha_TB_EC_Pos + 178);
                            if (ccCaptcha_TB_EC.indexOf("hidden") != -1) {
                                flag = false;
                                connected = true;
                                // printToScreen("Cap correct");
                            }
                        }
                        if (idPos != -1) {
                            toOutputFile("logged-dk-sis.html", responseString);
                            Log.d("LOg", "logged");
                            printToScreen("Logged");
                            DkSisState newDkSisState = getDkSisState();
                            int tinVaMon = updateRegisteredSubjectList(responseString, idPos, subjectTrtabId);
                            printToScreen("Registered: TC " + (tinVaMon % 100) + "   Số môn: " + (tinVaMon / 1000));
                            // Start send class register request
                            httppost = new HttpPost("http://dk-sis.hust.edu.vn/default.aspx");
                            httppost.addHeader("User-Agent",
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
                            nameValuePairs = new ArrayList<NameValuePair>();
                            int count = 0;
                            for (String a : MainActivity.classList) {
                                try {
                                    nameValuePairs = setDataRegisterRequest(a, newDkSisState);
                                } catch (Exception e) {
                                    Log.d("Pairs Value error", e.toString());
                                }
                                toOutputFile("postdata" + (count) + ".txt", nameValuePairs.toString());
                                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                response = MainActivity.registerHttpClient.execute(httppost);
                                entity = response.getEntity();
                                responseString = EntityUtils.toString(entity, "UTF-8");
                                toOutputFile("Result" + (count++) + ".txt", responseString);
                                try {
                                    int messPos = responseString.indexOf(
                                            "ctl00_ctl00_ASPxSplitter1_Content_ContentSplitter_MainContent_ASPxCallbackPanel1_lbKQ");
                                    final int xstart = responseString.indexOf(">", messPos);
                                    final int xend = responseString.indexOf("<", xstart);
//									sender.runOnUiThread(new Runnable() {
//
//										@Override
//										public void run() {
//											// TODO Auto-generated method stub
//											TextView cc = (TextView) sender.findViewById(R.id.lbkq);
//											String xcx = cc.getText().toString() + "\n" + Html
//													.fromHtml(responseString.substring(xstart + 1, xend)).toString();
//											cc.setText(xcx);
//										}
//
//									});
                                    printToScreen(Html.fromHtml(responseString.substring(xstart + 1, xend)).toString());
                                } catch (Exception e) {
                                }

                                if (responseString.length() > 5000) {
                                    idPos = responseString.indexOf(subjectTrtabId);
                                    tinVaMon = updateRegisteredSubjectList(responseString, idPos, subjectTrtabId);

                                }
                                printToScreen("Đã đăng kí: TC " + (tinVaMon % 100) + "   Số môn: " + (tinVaMon / 1000));
                                responseString = responseString.replace("\\", "");
                            }
                            try {

                                nameValuePairs = setDataRegisterRequest("c0:#$%SendtoServer%$#", newDkSisState);
                            } catch (Exception e) {
                                Log.d("Pairs Value error", e.toString());
                            }
                            Log.d("Pass", "Replace");
                            toOutputFile("postdata_last.txt", nameValuePairs.toString());
                            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                            response = MainActivity.registerHttpClient.execute(httppost);
                            entity = response.getEntity();
                            responseString = EntityUtils.toString(entity, "UTF-8");
                            toOutputFile("Result_last.txt", responseString);
                            if (responseString.length() > 5000) {
                                idPos = responseString.indexOf(subjectTrtabId);
                                tinVaMon = updateRegisteredSubjectList(responseString, idPos, subjectTrtabId);

                            }
                            printToScreen("Đã đăng kí: TC " + (tinVaMon % 100) + "   Số môn: " + (tinVaMon / 1000));

                            try {
                                int messPos = responseString.indexOf(
                                        "ctl00_ctl00_ASPxSplitter1_Content_ContentSplitter_MainContent_ASPxCallbackPanel1_lbKQ");
                                int start = responseString.indexOf(">", messPos);
                                int end = responseString.indexOf("<", start);
                                printToScreen(Html.fromHtml(responseString.substring(start + 1, end)).toString());
                            } catch (Exception e) {
                            }
                            // Sign out account
                            // httpclient.execute(new
                            // HttpGet("http://dk-sis.hust.edu.vn/Users/Logout.aspx"));
                            flag = false;
                        }
                        toOutputFile("dk-sis.html", responseString);
                    } else {
                        Log.d("Task", task + " Error :" + 3);
                    }
                }
            }

            Log.d("Task", task + " Pass :" + 6);
            return "";
        } catch (ClientProtocolException e) {
            Log.d("e1", e.toString());
        } catch (IOException e) {
            Log.d("e2", e.toString());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPostExecute(String ecec) {
        if (isCancel)
            return;
        Log.d("End Task", task.name());
        if (task.equals(LOGIN_DKSIS_WITH_CAPTCHA)) {
            if (!connected && !isCancel) {
                Snackbar.make(sender.getCurrentFocus(), "Máy chủ lỗi, đang đăng nhập lại!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //doInBackground("",acc,pass);
                startPostTask = new PostTask(sender, LOGIN_DKSIS_WITH_CAPTCHA);
                startPostTask.execute("ctl00$cLogIn1$bt_cLogIn", acc, pass);
            }
        } else if (task.equals(LOGIN_SIS_GET_MARK_TABLE) || task.equals(UPDATE_MARK_LIST)) {
            if (responseString.length() < 1069 && task.equals(LOGIN_SIS_GET_MARK_TABLE)) {
                sender.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(sender.findViewById(R.id.fab), "Lỗi tải bảng điểm, kiểm tra lại kết nối Internet!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                return;
            }
            //sendNotification("Cập nhật bảng điểm", new Date().toString());
            MainActivity.spin.clear();
            int currentSubjectCount = MainActivity.bangDiemAll.size();
            MainActivity.bangDiem.clear();
            MainActivity.ketQuaHocTap.clear();
            MainActivity.spin.add("Tất cả");
            HashSet<String> hocky = new HashSet<>();
            for (int i = 0; i < 200; i++) {
                int subjectRowPos = responseString.indexOf("MainContent_gvCourseMarks_DXDataRow" + i);
                if (subjectRowPos == -1)
                    break;
                int start = responseString.indexOf("\">", subjectRowPos);
                start = responseString.indexOf("\">", start + 1);
                int end = responseString.indexOf("<", start);
                String row = Html.fromHtml(responseString.substring(start + 2, end)).toString();
                for (int a = 0; a < 7; a++) {
                    start = responseString.indexOf("\">", end);
                    end = responseString.indexOf("<", start);
                    row += "__" + Html.fromHtml(responseString.substring(start + 2, end)).toString();
                }
                Log.d("cc", row);
                MainActivity.bangDiem.add(row);
                hocky.add("Học kỳ: " + row.substring(0, row.indexOf("_")));
            }
            List l = new ArrayList(hocky);
            Collections.sort(l);
            MainActivity.spin.addAll(l);
            for (int i = 0; i < 30; i++) {
                int kihocRowPos = responseString.indexOf("MainContent_gvResults_DXDataRow" + i);
                if (kihocRowPos == -1)
                    break;
                int start = responseString.indexOf("\">", kihocRowPos);
                start = responseString.indexOf("\">", start + 1);
                int end = responseString.indexOf("<", start);
                String row = Html.fromHtml(responseString.substring(start + 2, end)).toString();
                //MainActivity.spin.add("Học kì: " + row);
                for (int a = 0; a < 6; a++) {
                    start = responseString.indexOf("\">", end);
                    end = responseString.indexOf("<", start);
                    row += "__" + Html.fromHtml(responseString.substring(start + 2, end)).toString();
                }
                MainActivity.ketQuaHocTap.add(row);
            }
            MainActivity.spin.add("Điểm A/A+");
            MainActivity.spin.add("Điểm B/B+");
            MainActivity.spin.add("Điểm C/C+");
            MainActivity.spin.add("Điểm D/D+");
            MainActivity.spin.add("Điểm F");
            MainActivity.bangDiemAll = (ArrayList<String>) MainActivity.bangDiem.clone();
            int newSubjectCount = MainActivity.bangDiemAll.size();

            boolean allowAlwaysShowNotify = UpdateMarkService.allowAlwaysShow(context);
            if (newSubjectCount > currentSubjectCount || allowAlwaysShowNotify) {
                WidgetUtils.notifyNewestSubject(sender != null ? sender : context);
            }
            Log.d(UpdateMarkService.TAG, "AlwaysShow: " + allowAlwaysShowNotify);
            if (newSubjectCount >= currentSubjectCount)
                saveMarkList();
            if (task.equals(LOGIN_SIS_GET_MARK_TABLE)) {
                sender.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(sender.findViewById(R.id.fab), "Đã cập nhật lại dữ liệu!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        MainActivity.bangDiemAdapter.notifyDataSetChanged();
                        MainActivity.spinAdapter.notifyDataSetChanged();
                        ((MainActivity) sender).setCurrentSemesterSelected();
                        ((MainActivity) sender).showChartOfCPA();
                        ((MainActivity) sender).showChartOfPoint();
                    }
                });
            }
        } else if (task.equals(LOGIN_SIS)) {
            int vLogout = responseString.indexOf("vLogout");
            ProgressBar bar = (ProgressBar) sender.findViewById(R.id.progressBar);
            if (vLogout != -1) {
                // When logged
                //Save acc and pass word for fingerprint login
                LoginScreen loginScreen = (LoginScreen) sender;
                CheckBox checkBox = (CheckBox) loginScreen.findViewById(R.id.checkBox);
                if (checkBox.isChecked())
                    setFileContentToDataDir("acc4fing", acc + "," + pass);
                setProgressSmoothly(bar, 99);
                String userName = Html
                        .fromHtml(responseString.substring(responseString.indexOf("<strong>", vLogout - 269) + 8,
                                responseString.indexOf("</strong>", vLogout - 269)))
                        .toString();
                if (acc.equals("20151277")) {
                    Toast.makeText(sender, "Chào mừng ông chủ quay lại.", Toast.LENGTH_LONG).show();
                    Toast.makeText(sender, "お帰りなさい、ご主人様！", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(sender, "Xin chào " + userName + " どの", Toast.LENGTH_LONG).show();
                setFileContentToDataDir("username.jav", userName);
                saveAccAndPass();
                Intent i = new Intent(sender, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sender.startActivity(i);
                sender.finish();
            } else {
                // When cannot login
                bar.clearAnimation();
                bar.setVisibility(View.INVISIBLE);
                Animation tran = AnimationUtils.loadAnimation(sender, R.anim.anim_tran);
                Button login = (Button) sender.findViewById(R.id.login);
                login.setVisibility(View.VISIBLE);
                login.startAnimation(tran);
                if (responseString.indexOf("MainContent_UserName") != -1)
                    Toast.makeText(sender, "Tài khoản hoặc mật khẩu không đúng!", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(sender, "Lỗi mạng!", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void saveMarkList() {
        File file = new File((sender != null ? sender : context).getApplicationInfo().dataDir + "/marktable.jav");
        File file1 = new File((sender != null ? sender : context).getApplicationInfo().dataDir + "/hocki.jav");
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(file));
            BufferedWriter br1 = new BufferedWriter(new FileWriter(file1));
            String s = "";
            for (String a : MainActivity.bangDiem)
                s += a + "===";
            br.write(s);
            br.close();
            s = "";
            for (String a : MainActivity.ketQuaHocTap)
                s += a + "===";
            br1.write(s);
            br1.close();
        } catch (Exception e) {

        }
    }

    public void showLoginProgress(String explain) {
        String mms = ((TextView) sender.findViewById(R.id.outScreen)).getText().toString();
        int c = mms.length() - mms.indexOf(".");
        StringBuffer toPrint = new StringBuffer("Đang đăng nhập");
        c = c > mms.length() ? 0 : (c + 1) % 6;
        for (int i = 0; i < c; i++)
            toPrint.append('.');
        printToScreen(toPrint.toString() + explain);

    }

    public String getLinkCaptcha(String webContent) {
        int captchaStart = webContent.indexOf("/DXB.axd?DXCache=");
        if (captchaStart == -1) {
            Log.d("Server error", "ahihi");
            return null;
        }
        int captchaEnd = responseString.indexOf('"', captchaStart);
        captchaLink = "http://dk-sis.hust.edu.vn" + responseString.substring(captchaStart, captchaEnd);
        return captchaLink;
    }

    public void setProgressSmoothly(ProgressBar bar, int progress) {
        ProgressBarAnimation anim = new ProgressBarAnimation(bar, bar.getProgress(), progress);
        anim.setDuration(1000);
        try {
            bar.startAnimation(anim);
        } catch (Exception e) {
            Log.d("Error Animation", e.toString());
        }
    }

    public void printToScreen(final String mss) {
        sender.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) sender.findViewById(R.id.outScreen)).setText(mss);
            }
        });
    }

    public ArrayList<NameValuePair> setDataRegisterRequest(String className, DkSisState newDkSisState) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(20);

        int listCallBackStatePos;
        int lend;
        int timeTableCallBackStatePos;
        int end;
        listCallBackStatePos = responseString.indexOf(
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1$gvRegisteredList");
        listCallBackStatePos = responseString.indexOf("({", listCallBackStatePos);
        lend = responseString.indexOf(");", listCallBackStatePos);

        timeTableCallBackStatePos = responseString.indexOf(
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1$gvTimeTable");
        timeTableCallBackStatePos = responseString.indexOf("{", timeTableCallBackStatePos);
        end = responseString.indexOf(");", timeTableCallBackStatePos);

        nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", ""));
        nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", newDkSisState.getvst()));
        nameValuePairs.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", newDkSisState.getvstg()));
        nameValuePairs.add(new BasicNameValuePair("ctl00$ctl00$ASPxSplitter1",
                "{\"state\":[{\"s\":114,\"st\":\"px\",\"c\":0,\"spt\":0,\"spl\":0},{\"s\":50,\"st\":\"%\",\"c\":0,\"spt\":0,\"spl\":0},{\"st\":\"px\",\"s\":50,\"c\":0,\"spt\":0,\"spl\":0}]}"));
        nameValuePairs.add(new BasicNameValuePair("ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter",
                "{\"state\":[{\"st\":\"px\",\"s\":250,\"c\":0,\"spt\":0,\"spl\":0},{\"s\":100,\"st\":\"%\",\"c\":0,\"spt\":824,\"spl\":0}]}"));
        nameValuePairs.add(new BasicNameValuePair(
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1$tbDirectClassRegister",
                ""));
        nameValuePairs.add(new BasicNameValuePair(
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1$gvRegisteredList",
                responseString.substring(listCallBackStatePos + 1, lend)));
        nameValuePairs.add(new BasicNameValuePair(
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1$gvRegisteredList$header11$SelectAllCheckBox",
                "I"));
        nameValuePairs.add(new BasicNameValuePair(
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1$gvTimeTable",
                responseString.substring(timeTableCallBackStatePos, end)));
        nameValuePairs.add(new BasicNameValuePair(
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1$pcYesNo",
                "{\"windowsState\":\"0:0:12000:363:221:0:-10000:-10000:1:0:0:0\"}"));
        nameValuePairs.add(new BasicNameValuePair("__CALLBACKID",
                "ctl00$ctl00$ASPxSplitter1$Content$ContentSplitter$MainContent$ASPxCallbackPanel1"));
        nameValuePairs.add(new BasicNameValuePair("__CALLBACKPARAM", "c0:" + className));
        nameValuePairs.add(new BasicNameValuePair("__EVENTVALIDATION", newDkSisState.geteve()));

        return nameValuePairs;

    }

    public void toOutputFile(String fname, String data) {
        try {
            FileOutputStream is = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + fname);
            is.write(data.getBytes());
            is.close();
        } catch (Exception e) {
            Log.d("Error write: " + fname, e.toString());
        }
    }

    public Bitmap getBitmpFromLink(String url) {
        String urldisplay = url;
        Bitmap mIcon11 = null;
        try {
            InputStream in = new URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    public int updateRegisteredSubjectList(String src, int idPos, String subjectTrtabId) {
        idPos -= 100;
        int tinVaMon = 0;
        MainActivity.arrList.clear();
        for (int i = 0; i < 20; i++) {
            String str = "";
            if ((idPos = src.indexOf(subjectTrtabId + i, idPos + 1)) != -1) {
                int start = src.indexOf("\">", idPos);
                start = src.indexOf("\">", start + 1);
                int end = src.indexOf("<", start);
                str = src.substring(start + 2, end);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                str += "__" + src.substring(start + 2, end);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                str += "__" + src.substring(start + 2, end);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                str += "__" + Html.fromHtml(src.substring(start + 2, end)).toString();
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                start = src.indexOf("\">", end);
                end = src.indexOf("<", start);
                try {
                    tinVaMon += Integer.parseInt(src.substring(start + 2, end));
                } catch (Exception e) {
                }
                Log.d("Update list class", "str");
                tinVaMon += 1000;
                str += "__" + src.substring(start + 2, end);
                MainActivity.arrList.add(str);
                sender.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.adapter.notifyDataSetChanged();
                    }
                });
            } else
                break;

        }
        return tinVaMon;
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getSmallBitmapFromNet(String urlString) {
        HttpGet httpRequest;
        try {
            httpRequest = new HttpGet(new URL(urlString).toURI());
            HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
            InputStream is = bufHttpEntity.getContent();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public void saveAccAndPass() {
        String acc_pass = acc + "_jav_number_one_" + pass;
        try {
            FileOutputStream loginedData = new FileOutputStream(
                    sender.getApplicationInfo().dataDir + "/tokuda.jav.tmp");
            ObjectOutputStream oos = new ObjectOutputStream(loginedData);
            oos.writeObject(acc_pass);
            oos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public DkSisState getDkSisState() {
        int vstaPos = responseString.indexOf("id=\"__VIEWSTATE\"");
        String viewState = "";
        if (vstaPos != -1)
            viewState = responseString.substring(responseString.indexOf("value", vstaPos) + 7,
                    responseString.indexOf("\"", vstaPos + 100));

        int vstagenPos = responseString.indexOf("__VIEWSTATEGENERATOR");
        vstagenPos = responseString.indexOf("value", vstagenPos) + 7;
        String vstagen = responseString.substring(vstagenPos, responseString.indexOf("\"", vstagenPos));
        int eventvalidPos = responseString.indexOf("__EVENTVALIDATION");
        eventvalidPos = responseString.indexOf("value", eventvalidPos) + 7;
        String eventvalid = responseString.substring(eventvalidPos, responseString.indexOf("\"", eventvalidPos));
        return new DkSisState(viewState, vstagen, eventvalid);
    }

    public boolean setFileContentToDataDir(String fileName, String content) {
        File file = new File(sender.getApplicationInfo().dataDir + "/" + fileName);
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(file));
            br.write(content);
            br.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCancelled() {
        Log.d("onCancelled", startPostTask.hashCode() + "");
        startPostTask.cancel(true);
        startPostTask = null;
        super.onCancelled();
    }
}

class DkSisState {
    public String viewstate;
    public String viewstategen;
    public String eventvalid;

    public DkSisState(String viewstate, String viewstategen, String eventvalid) {
        this.viewstate = viewstate;
        this.viewstategen = viewstategen;
        this.eventvalid = eventvalid;
    }

    public String getvst() {
        return this.viewstate;
    }

    public String getvstg() {
        return this.viewstategen;
    }

    public String geteve() {
        return this.eventvalid;
    }

}