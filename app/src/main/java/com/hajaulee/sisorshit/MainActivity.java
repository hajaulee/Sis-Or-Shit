package com.hajaulee.sisorshit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.hajaulee.sisorshit.PostTaskAction.LOGIN_DKSIS_WITH_CAPTCHA;
import static com.hajaulee.sisorshit.PostTaskAction.LOGIN_SIS_GET_MARK_TABLE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String acc;
    public static String pass;
    private int currentLayoutId = R.id.nav_register;
    private int[] LAYOUTS_OF_APP = {R.id.register_layout, R.id.point_list_layout, R.id.chart_layout};
    @SuppressWarnings("unchecked")
    public static ArrayList<int[][]>[] digit = (ArrayList<int[][]>[]) new ArrayList[10];
    public static ArrayList<String> arrList = new ArrayList<String>();
    public static ArrayAdapter<String> adapter;
    public static ArrayList<String> classList = new ArrayList<String>();
    public static ArrayList<String> bangDiem = new ArrayList<String>();
    public static ArrayList<String> bangDiemAll = new ArrayList<String>();
    public static ArrayList<String> ketQuaHocTap = new ArrayList<String>();
    public static ArrayAdapter<String> bangDiemAdapter;
    public static ArrayList<String> spin = new ArrayList<String>();
    public static ArrayAdapter<String> spinAdapter;
    public static String USERNAME;
    public static HttpClient registerHttpClient;
    FloatingActionButton fab;
    Toolbar toolbar;
    Spinner dropdown;
    private PostTask startPostTask;

    public ImageView getCaptchaView() {
        return captchaView;
    }

    private ImageView captchaView;
    private static MainActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HttpParams httpParams = new BasicHttpParams();
        instance = this;
        // It's always good to set how long they should try to connect. In this
        // this example, five seconds.
        HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
        HttpConnectionParams.setSoTimeout(httpParams, 8000);
        registerHttpClient = new DefaultHttpClient(httpParams);

        //Check for user logged, if just not redirect to LoginScreen Layout
        checkUserLogged();
        USERNAME = getFileContentFromDataDir("username.jav");

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.register);
        setSupportActionBar(toolbar);
        //Load các thành phần của chương trình như Buttons, ListView, Spinner
        loadButtonsAndListview();
        captchaView = findViewById(R.id.imageView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation rotate = AnimationUtils.loadAnimation(MainActivity.this, R.anim.quay);
                view.startAnimation(rotate);

                if (currentLayoutId == R.id.nav_listmark) {
                    refreshMarkList(view, dropdown);
                    Snackbar.make(view, "Đang tải lại danh sách điểm.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                if (currentLayoutId == R.id.nav_register) {
                    startRegisterSubject();
                    Snackbar.make(view, "Bắt đầu đăng ký.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (!drawer.isDrawerOpen(GravityCompat.START)) {
                        hideKeyBoard();
                    } else {
                        if (currentLayoutId == R.id.nav_register) {
                            toolbar.setTitle(R.string.register);
                        } else if (currentLayoutId == R.id.nav_listmark) {
                            toolbar.setTitle(R.string.point_list);
                        } else if (currentLayoutId == R.id.nav_chart) {
                            toolbar.setTitle(R.string.chart);
                        }
                    }
                    invalidateOptionsMenu();
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView userNameView = (TextView) header.findViewById(R.id.user_name_view);
        TextView userEmailView = (TextView) header.findViewById(R.id.user_email_view);
        if ("20151277".equals(acc)) {
            userNameView.setText("Ông chủ đẹp trai");
        } else {
            userNameView.setText(USERNAME);
            userEmailView.setText(acc + "@student.hust.edu.vn");
        }
        if (WidgetUtils.OPEN_MARK_LIST.equals(getIntent().getStringExtra(WidgetUtils.ACTION))) {
            showMarkListLayout();
        }
        loadMarkList();
        startService(new Intent(this, UpdateMarkService.class));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_stop) {
            startPostTask.setCancel(true);
            startPostTask = null;
            findViewById(R.id.classCode).setEnabled(true);
            findViewById(R.id.add).setEnabled(true);
            findViewById(R.id.start).setVisibility(View.VISIBLE);
            captchaView.setVisibility(View.INVISIBLE);
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            return true;
        }else if (id == R.id.action_showNotify){
            boolean allow = UpdateMarkService.allowAlwaysShow(this);
            if (UpdateMarkService.allowAlwaysShow(this, allow^=true)){
                Toast.makeText(this, "Luôn hiện thông báo", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Chỉ hiện khi có điểm môn mới", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMarkListLayout() {
        showLayout(R.id.point_list_layout, R.drawable.ic_refresh);
        if (bangDiemAll.isEmpty()) {
            loadMarkList();
        }
        setCurrentSemesterSelected();
    }

    private void showChartLayout() {
        showLayout(R.id.chart_layout, -1);
        if (bangDiemAll.isEmpty()) {
            Snackbar.make(fab, "Đang tải lại danh sách điểm.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            loadMarkList();
        }
        showChartOfPoint();
        showChartOfCPA();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        currentLayoutId = id;
        if (id == R.id.nav_register) {
            showLayout(R.id.register_layout, R.drawable.ic_play_arrow);
        } else if (id == R.id.nav_listmark) {
            showMarkListLayout();
        } else if (id == R.id.nav_chart) {
            showChartLayout();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String getFileContentFromDataDir(String fileName) {
        File file = new File(getApplicationInfo().dataDir + "/" + fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str = br.readLine();
            br.close();
            return str;
        } catch (Exception e) {
            return "";
        }
    }

    public void setCurrentSemesterSelected() {
        int countOfOption = dropdown.getAdapter().getCount();
        dropdown.setSelection(countOfOption - 6);
    }

    @SuppressWarnings("unchecked")
    public void loadMarkList() {
        Snackbar.make(fab, "Đang tải lại danh sách điểm.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        File file = new File(getApplicationInfo().dataDir + "/marktable.jav");
        File file1 = new File(getApplicationInfo().dataDir + "/hocki.jav");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedReader br1 = new BufferedReader(new FileReader(file1));
            String[] markArray = br.readLine().split("===");
            String[] markArray1 = br1.readLine().split("===");
            bangDiem.clear();
            spin.clear();
            spin.add("Tất cả");
            ketQuaHocTap.clear();
            HashSet<String> hocky = new HashSet<>();
            for (String a : markArray) {
                bangDiem.add(a);
                hocky.add("Học kỳ: " + a.substring(0, a.indexOf("_")));
            }
            List l = new ArrayList(hocky);
            Collections.sort(l);
            spin.addAll(l);

            for (String a : markArray1) {
                ketQuaHocTap.add(a);
//                spin.add("Học kì: " + a.substring(0, a.indexOf("_")));
            }
            spin.add("Điểm A/A+");
            spin.add("Điểm B/B+");
            spin.add("Điểm C/C+");
            spin.add("Điểm D/D+");
            spin.add("Điểm F");
            bangDiemAll = (ArrayList<String>) bangDiem.clone();
            spinAdapter.notifyDataSetChanged();
            bangDiemAdapter.notifyDataSetChanged();
            br.close();
            br1.close();
            Log.d("Had data", "cc");
        } catch (Exception e) {
            Log.d("Not data", "cc");
            refreshMarkList(null, dropdown);
        }
    }

    public void cleanFile(String... files) {
        for (String file : files) {
            try {
                File f = new File(getApplicationInfo().dataDir + "/" + file);
                f.delete();
            } catch (Exception e) {
                Log.d("File delete error", e.toString());
            }
        }
    }

    public void logout() {
        spin.clear();
        bangDiem.clear();
        bangDiemAll.clear();
        ketQuaHocTap.clear();
        classList.clear();
        cleanFile("tokuda.jav.tmp", "hocki.jav", "marktable.jav");

//        mTitle = getString(R.string.title_section3);
        Intent i = new Intent(this, LoginScreen.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(i);
        this.finish();
    }

    public void addClassToList() {
        EditText ou = (EditText) findViewById(R.id.class_queue);
        String classListString = "";
        for (String a : classList)
            if (a.charAt(0) == '-')
                classListString += "<font color=\"red\">" + a.substring(1) + "</font> ";
            else
                classListString += "<font color=\"blue\">" + a + "</font> ";
        classListString = classListString.trim();
        ou.setText(Html.fromHtml(classListString));
    }

    public void checkUserLogged() {
        checkUserLogged(true);
    }

    public void checkUserLogged(boolean openLoginScreen) {
        try {
            FileInputStream loginData = new FileInputStream(getApplicationInfo().dataDir + "/tokuda.jav.tmp");
            ObjectInputStream ios = new ObjectInputStream(loginData);
            String acc_pass = (String) ios.readObject();
            String[] ahihi = acc_pass.split("_jav_number_one_");
            acc = ahihi[0];
            pass = ahihi[1];
            ios.close();

            try {
                loadTrainedDataFromAssets("data.trained");
            } catch (StreamCorruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } catch (Exception e) {
            if (openLoginScreen) {
                Intent i = new Intent(this, LoginScreen.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(i);
                this.finish();
            }
        }
    }


    @SuppressWarnings("unchecked")
    public ArrayList<int[][]>[] loadTrainedDataFromAssets(String fileName)
            throws StreamCorruptedException, IOException, ClassNotFoundException {
        InputStream fin = getAssets().open(fileName);
        ObjectInputStream ois = new ObjectInputStream(fin);
        digit = (ArrayList<int[][]>[]) ois.readObject();
        ois.close();
        return digit;
    }

    public static double diemChuDoiRaSo(String diemchu) {
        switch (diemchu) {
            case "A":
            case "A+":
                return 4;
            case "B+":
                return 3.5;
            case "B":
                return 3;
            case "C+":
                return 2.5;
            case "C":
                return 2;
            case "D+":
                return 1.5;
            case "D":
                return 1;
        }

        return 0;
    }

    public void loadButtonsAndListview() {
        Button start = (Button) findViewById(R.id.start);
        Button add = (Button) findViewById(R.id.add);
        Button reload = (Button) findViewById(R.id.reload);
        ListView ls = (ListView) findViewById(R.id.listView);
        //get the spinner from the xml.
        dropdown = (Spinner) findViewById(R.id.spinner);
        spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spin);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(spinAdapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (selectedItemView == null)
                    position = 0;
                bangDiem.clear();
                TextView thanhTich = (TextView) findViewById(R.id.thanhtich);
                if (position == 0) {
                    thanhTich.setText("");
                    bangDiem.addAll(bangDiemAll);
                } else {
                    String text = ((TextView) selectedItemView).getText().toString();

                    if (!text.contains("20")) {
                        int mon = 0;
                        int tin = 0;
                        text = text.substring(text.lastIndexOf(' ') + 1);
                        String[] rank = text.split("/");
                        for (String a : bangDiemAll)
                            if (a.substring(a.length() - 3).contains(rank[0]) || a.substring(a.length() - 3).contains(rank[rank.length - 1])) {
                                bangDiem.add(a);
                                mon++;
                                tin += Integer.parseInt(a.split("__")[3]);
                            }
                        thanhTich.setText(Html.fromHtml("<b>Tổng số: " + mon + " môn, " + tin + " tín</b>"));
                    } else {
                        text = text.substring(text.lastIndexOf(' ') + 1);
                        double currentCPA = 0;
                        int tongTc = 0;
                        for (String a : bangDiemAll) {
                            String[] infoMon = a.split("__");
                            int tc = Integer.parseInt(infoMon[3]);
                            tongTc += tc;
                            currentCPA += tc * diemChuDoiRaSo(infoMon[7]);

                            if (a.contains(text))
                                bangDiem.add(a);
                            Log.d(text, a);
                        }
                        currentCPA = Math.floor(currentCPA / tongTc * 100) / 100;
                        for (String a : ketQuaHocTap)
                            if (a.contains(text)) {
                                String[] jav = a.split("__");
                                thanhTich.setText(Html.fromHtml("<b><font color=\"red\" >CPA:</font> " + jav[2] +
                                        "&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"green\" >GPA:</font> " + jav[1] + "</b><br>TC qua: " + jav[3] +
                                        "&nbsp;&nbsp;TC tích lũy: " + jav[4] + "&nbsp;&nbsp;TC nợ ĐK: " + jav[5] + "&nbsp;&nbsp;TC ĐK: " + jav[6]));
                                break;
                            } else {
                                double currentGPA = 0;
                                int soTC = 0;
                                for (String mon : bangDiem) {
                                    String[] info = mon.split("__");
                                    int tc = Integer.parseInt(info[3]);
                                    soTC += tc;
                                    currentGPA += tc * diemChuDoiRaSo(info[7]);
                                }
                                currentGPA /= soTC;
                                thanhTich.setText(Html.fromHtml("<b><font color=\"red\" >CPA:</font> " +
                                        currentCPA +
                                        "&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"green\" >GPA:</font> " +
                                        (Math.floor(currentGPA * 100) / 100) +
                                        "</b><br>Kết quả trên dựa vào tính toán"));
                            }
                    }
                }
                bangDiemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        // 1. Tạo ArrayList object
        if (arrList.isEmpty())
            arrList.add("Danh sách môn đăng kí thành công");
        // 2. Gán Data Source (ArrayList object) vào ArrayAdapter
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrList) {
            @Override
            public View getView(int pos, View v, ViewGroup p) {
                View view = super.getView(pos, v, p);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                String text = tv.getText().toString();
                String[] xahihi = text.split("__");
                if (xahihi.length >= 5) {
                    tv.setText(Html.fromHtml("<b><font color=\"blue\">" + xahihi[0] + "</font>&nbsp;&nbsp;"
                            + "<font color=\"#C0BE41\">" + xahihi[2] + "</font>&nbsp;&nbsp;" + xahihi[1] + "</b><br>"
                            + "<font size=\"1\" color=\"#9B9B75\">TC " + xahihi[4] + "&nbsp;&nbsp;" + xahihi[3]
                            + "</font>"));
                }
                return view;
            }
        };
        // 3. gán Adapter vào ListView

        ls.setAdapter(adapter);
        addClassToList();
        bangDiemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bangDiem) {
            @NonNull
            @Override
            public View getView(int pos, View v, ViewGroup p) {
                View view = super.getView(pos, v, p);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                String text = tv.getText().toString();
                String[] xahihi = text.split("__");
                if (xahihi.length >= 8) {
                    tv.setText(Html.fromHtml("<b><font color=\"blue\">" + xahihi[1] + "</font>&nbsp;&nbsp;"
                            + "&nbsp;&nbsp;" + xahihi[2] + "</b><br>"
                            + "<font size=\"1\" color=\"#9B9B75\">GK: "
                            + xahihi[5] + "&nbsp;&nbsp;&nbsp;&nbsp;"
                            + ((xahihi[5].length() == 1) ? "&nbsp;&nbsp;&thinsp;" : ((xahihi[5].length() == 2) ? "&thinsp;" : ""))
                            + "CK: " + xahihi[6]
                            + "</font>" + "&nbsp;&nbsp;&nbsp;&nbsp;"
                            + ((xahihi[6].length() == 1) ? "&nbsp;&nbsp;&thinsp;" : ((xahihi[6].length() == 2) ? "&thinsp;" : ""))
                            + ((xahihi[7].equals("A+")) ? "<b><font color=\"red\">" :
                            (xahihi[7].equals("A") ? "<b><font color=\"green\">" :
                                    xahihi[7].equals("F") ? "<b><font color=\"balck\">" : "<b><font color=\"blue\">"))
                            + xahihi[7] + "</font></b>"
                    ));
                }
                return view;
            }
        };
        ListView bangdiemListView = ((ListView) findViewById(R.id.bangDiem));
        bangdiemListView.setAdapter(bangDiemAdapter);
        bangdiemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] infoOfSubject = bangDiem.get(i).split("__");
                Toast.makeText(MainActivity.this,
                        infoOfSubject[2] + ": " + infoOfSubject[3] + " tín chỉ.", Toast.LENGTH_LONG).show();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                EditText in = (EditText) findViewById(R.id.classCode);
                String classCode = in.getText().toString();

                if (classList.indexOf(classCode) == -1) {
                    in.setText("");
                    if (classList.indexOf("-" + classCode) != -1)
                        classList.remove("-" + classCode);
                    else if (classList.indexOf(classCode.substring(1)) != -1)
                        classList.remove(classCode.substring(1));
                    else
                        classList.add(classCode);
                    addClassToList();
                } else {
                    Animation tran = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_tran);
                    v.startAnimation(tran);
                }
            }
        });
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startRegisterSubject();
            }
        });
        reload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                refreshMarkList(v, dropdown);
            }
        });
    }

    public void startRegisterSubject() {
        findViewById(R.id.classCode).setEnabled(false);
        findViewById(R.id.add).setEnabled(false);
        captchaView.setVisibility(View.VISIBLE);
        findViewById(R.id.start).setVisibility(View.INVISIBLE);
        findViewById(R.id.fab).setVisibility(View.INVISIBLE);
        startPostTask = new PostTask(MainActivity.this, LOGIN_DKSIS_WITH_CAPTCHA);
        startPostTask.execute("ctl00$cLogIn1$bt_cLogIn", acc, pass);
    }

    public void refreshMarkList(View v, Spinner dropdown) {
        dropdown.setSelection(0);
        PostTask x = new PostTask(MainActivity.this, LOGIN_SIS_GET_MARK_TABLE);
        x.execute("ctl00$cLogIn1$bt_cLogIn", acc, pass);
    }

    public void hideKeyBoard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public void showLayout(int layoutId, int floatButtonResource) {
        for (int id : LAYOUTS_OF_APP) {
            findViewById(id).setVisibility(View.INVISIBLE);
        }
        findViewById(layoutId).setVisibility(View.VISIBLE);
        if (floatButtonResource == -1) {
            fab.setVisibility(View.INVISIBLE);
        } else {
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(floatButtonResource);
        }
    }

    public int getTinChi(String diemChu) {
        int mon = 0, tin = 0;
        int len = diemChu.length();
        for (String a : bangDiemAll) {
            if (a.charAt(a.length() - len) == diemChu.charAt(0)) {
                tin += Integer.parseInt(a.split("__")[3]);
            }
        }
        return tin;
    }

    public void showChartOfPoint() {
        BarChart barChart = (BarChart) findViewById(R.id.bar_chart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(getTinChi("A+"), 0));
        entries.add(new BarEntry(getTinChi("A"), 1));
        entries.add(new BarEntry(getTinChi("B+"), 2));
        entries.add(new BarEntry(getTinChi("B"), 3));
        entries.add(new BarEntry(getTinChi("C+"), 4));
        entries.add(new BarEntry(getTinChi("C"), 5));
        entries.add(new BarEntry(getTinChi("D+"), 6));
        entries.add(new BarEntry(getTinChi("D"), 7));
        entries.add(new BarEntry(getTinChi("F"), 8));
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("A+");
        labels.add("A");
        labels.add("B+");
        labels.add("B");
        labels.add("C+");
        labels.add("C");
        labels.add("D+");
        labels.add("D");
        labels.add("F");

        BarDataSet barDataSet = new BarDataSet(entries, "Tín chỉ");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData data = new BarData(labels, barDataSet);
        barChart.setData(data);
        barChart.setDescription("");
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0);
        barChart.animateY(2000);
    }

    public void showChartOfCPA() {
        LineChart lineChart = (LineChart) findViewById(R.id.line_chart);
        ArrayList<Entry> lineEntriesCPA = new ArrayList<>();
        ArrayList<Entry> lineEntriesGPA = new ArrayList<>();
        ArrayList<String> lineLabels = new ArrayList<>();
        for (int i = 0; i < ketQuaHocTap.size(); i++) {
            String[] thanhTich1Ky = ketQuaHocTap.get(i).split("__");
            lineEntriesCPA.add(new Entry(Float.parseFloat(thanhTich1Ky[2]), i));
            lineEntriesGPA.add(new Entry(Float.parseFloat(thanhTich1Ky[1]), i));
            lineLabels.add(thanhTich1Ky[0]);
        }
        LineDataSet dataSetCPA = new LineDataSet(lineEntriesCPA, "CPA");
        LineDataSet dataSetGPA = new LineDataSet(lineEntriesGPA, "GPA");
        dataSetCPA.setDrawFilled(true);
        dataSetGPA.setDrawFilled(true);
        dataSetCPA.setColor(Color.RED);
        dataSetGPA.setColor(Color.BLUE);
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(dataSetCPA);
        dataSets.add(dataSetGPA);
        LimitLine xs = new LimitLine(3.6f, "Xuất sắc");
        xs.enableDashedLine(3.6f, 3.2f, 2.0f);
        xs.setTextSize(9f);
        xs.setLineColor(Color.BLUE);
        LimitLine g = new LimitLine(3.2f, "Giỏi");
        LimitLine k = new LimitLine(2.5f, "Khá");
        LimitLine tb = new LimitLine(2f, "Trung bình");
        g.setTextSize(9f);
        k.setTextSize(9f);
        tb.setTextSize(9f);
        xs.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        g.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        k.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        tb.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);

        lineChart.getAxisLeft().addLimitLine(xs);
        lineChart.getAxisLeft().addLimitLine(g);
        lineChart.getAxisLeft().addLimitLine(k);
        lineChart.getAxisLeft().addLimitLine(tb);

        LineData lineData = new LineData(lineLabels, (List) dataSets);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setDescription("");
        lineChart.setData(lineData);
        lineChart.animateY(2000);
    }

    public static MainActivity getInstance() {
        return instance;
    }
}
