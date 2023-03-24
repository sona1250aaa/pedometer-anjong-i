package com.example.team678_final;

import static android.hardware.Sensor.TYPE_STEP_COUNTER;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PedometerFragment extends Fragment implements View.OnLongClickListener {
    private BroadcastReceiver receiver;
    private Context context;
    private Intent intent;

    int steps_in_one_set = 100;

    CircularProgressBar progress_circular;
    TextView tv_quotient_steps;
    TextView tv_remainder_steps;
    TextView tv_total_steps;
    TextView tv_weekly_date;
    TextView battery;
    Button btn_start_stop;
    BarChart barChart;

    PedometerDB db;
    ArrayList<PedometerInfo> countList;

    public static PedometerFragment newInstance(int number) {
        Bundle bundle = new Bundle();
        bundle.putInt("number", number);

        PedometerFragment pedometerFragment = new PedometerFragment();
        pedometerFragment.setArguments(bundle);
        return pedometerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this.getActivity();

        if (getArguments() != null) {
            int num = getArguments().getInt("number");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pedometer, container, false);

        //최초 실행 시에만 동작하는 코드
        if(!PreferenceManager.getBoolean(context, "first_start")) {
            Log.e("first", "start");
            //최초 실행 여부를 저장하는 코드
            PreferenceManager.setBoolean(context, "first_start", true);
            //여기부터 최초 실행 시 동작시킬 코드를 삽입하면 됨
            PreferenceManager.setBoolean(context, "is_start", false);
            PreferenceManager.setInt(context, "current_steps", 0);
            PreferenceManager.setBoolean(context, "first_start_foreground", true);
            PreferenceManager.setString(context, "today_date",
                    new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis())));
            for (int i = 0; i < 7; i++) {
                new PedometerDB(context).getWritableDatabase().execSQL("insert into pedometerTable values('" +
                        new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis() - 86400 * 1000 * i))
                        + "', " + (i == 0 ? 0 : 3000) + ");");
                Log.e("date", new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis() - 86400 * 1000 * i)));
            }
        }

        //센서 사용권한을 묻는 코드
        if(ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] { Manifest.permission.ACTIVITY_RECOGNITION }, TYPE_STEP_COUNTER);
        }

        progress_circular = view.findViewById(R.id.progress_circular_pedometer);
        tv_quotient_steps = view.findViewById(R.id.tv_quotient_steps);
        tv_remainder_steps = view.findViewById(R.id.tv_remainder_steps);
        tv_total_steps = view.findViewById(R.id.tv_total_steps);
        tv_weekly_date = view.findViewById(R.id.tv_weekly_date);
        btn_start_stop = view.findViewById(R.id.btn_start_stop);
        barChart = view.findViewById(R.id.barchart);

        //현재 만보기 실행/중지 여부에 따라 버튼의 텍스트를 변경
        if(PreferenceManager.getBoolean(context, "is_start")) {
            btn_start_stop.setText("기록\n중지");
        } else {
            btn_start_stop.setText("기록\n시작");
        }

        //앱 재실행 시 화면 새로고침
        checkDateChange(context);
        updateDaily(new PedometerDB(context).getCount(
                new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis()))
        ));
        updateWeekly();

        //브로드캐스트가 전송되면 현재 걸음수를 반영해 프로그레스바, 텍스트뷰의 내용 갱신
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int current_steps = PreferenceManager.getInt(context, "current_steps");
                updateDaily(current_steps);
                updateWeekly();
            }
        };

        //foregroundservice에서 센서 이벤트가 발생할 때마다 보내는 브로드캐스트를 수신하도록 리시버 등록
        LocalBroadcastManager.getInstance(context).registerReceiver(
                receiver, new IntentFilter("ForegroundServiceFilter"));

        intent = new Intent(context, PedometerForegroundService.class);

        //버튼 클릭 리스너, 오동작 방지를 위해 길게 누를 때만 응답하도록 설정
        btn_start_stop.setOnLongClickListener(this);

        // status, bottomNav bar 색상 변경
        onMultiWindowModeChanged(true);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        // status, bottomNav bar 색상 변경
        onMultiWindowModeChanged(true);

        Log.e("steptest", "" + PreferenceManager.getInt(context, "current_steps") + "");
        LocalBroadcastManager.getInstance(context).registerReceiver(
                receiver, new IntentFilter("ForegroundServiceFilter"));
        checkDateChange(context);
        updateDaily(new PedometerDB(context).getCount(
                new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis()))
        ));
        updateWeekly();
    }

    @Override
    public void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    @Override
    public boolean onLongClick(View view){
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50); // 0.05초간 진동
        if(PreferenceManager.getBoolean(context, "is_start")) {
            //실행여부를 중지로 변경하고, foregroundservice를 중지, 버튼 텍스트를 중지로 변경
            PreferenceManager.setBoolean(context, "is_start", false);
            btn_start_stop.setText("기록\n시작");
            context.stopService(intent);
            //만보기가 중지 상태일 때
        } else {
            //실행여부를 실행으로 변경하고, foregroundservice를 실행, 만보기를 실행으로 변경
            PreferenceManager.setBoolean(context, "is_start", true);
            btn_start_stop.setText("기록\n중지");
            context.startForegroundService(intent);
        }
        return false;
    }

    public void updateDaily(int current_steps){
        progress_circular.setProgressWithAnimation((current_steps % steps_in_one_set), 1000l);
        tv_quotient_steps.setText("" + steps_in_one_set + "걸음씩 " + (current_steps / steps_in_one_set) + "번 누적");
        tv_remainder_steps.setText((current_steps % steps_in_one_set) + "");
        tv_total_steps.setText(current_steps + "");
    }

    public void updateWeekly(){
        String weeklyDate = "주간통계 (" +
                new SimpleDateFormat("MM").format(new Date(System.currentTimeMillis() - 86400 * 1000 * 6)) + "/" +
                new SimpleDateFormat("dd").format(new Date(System.currentTimeMillis() - 86400 * 1000 * 6)) + " ~ " +
                new SimpleDateFormat("MM").format(new Date(System.currentTimeMillis())) + "/" +
                new SimpleDateFormat("dd").format(new Date(System.currentTimeMillis())) + ")";
        tv_weekly_date.setText(weeklyDate);

        db = new PedometerDB(context);
        countList = db.getCountList();
        Collections.reverse(countList);

        barChart.setTouchEnabled(false);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < countList.size(); i++) {
            entries.add(new BarEntry(i, Integer.parseInt(countList.get(i).getCount())));
        }
        final ArrayList<String> xAxisLabel = new ArrayList<>();
        for (int i = 0; i < countList.size(); i++) {
            String date = countList.get(i).getDate().substring(3, 5) + "/" + countList.get(i).getDate().substring(6, 8);
            xAxisLabel.add(date);
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setTextColor(Color.rgb(0,0,0));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); // x축 Grid 제거
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xAxisLabel.get((int) value);
            }
        });

        YAxis yAxis;
        yAxis = barChart.getAxisLeft();
        yAxis.setTextColor(Color.rgb(255,255,255));
        yAxis.setDrawGridLines(false); // y축 왼쪽 Grid 삭제
        yAxis.setAxisMaximum(10000f); // y축 최대값
        yAxis.setAxisMinimum(0f); // y축 최소값
        YAxis axisRight =barChart.getAxisRight();
        yAxis.setDrawAxisLine(false);
        axisRight.setDrawLabels(false); // 오른쪽 label 삭제
        axisRight.setDrawGridLines(false); // y축 오른쪽 Grid 삭제
        axisRight.setDrawAxisLine(false);

        BarDataSet set = new BarDataSet(entries, "걸음");
        set.setColor(ContextCompat.getColor(context, R.color.BarChartGradientColor)); // 바 Color
        set.setGradientColor(ContextCompat.getColor(context, R.color.BarChartColor), ContextCompat.getColor(context, R.color.BarChartGradientColor)); // 145 57 203
        BarData data = new BarData(set);
        data.setBarWidth(0.45f); // set custom bar width
        data.setValueTextSize(15f); // 데이터 텍스트 크기
        data.setValueTextColor(ContextCompat.getColor(context, R.color.BarChartGradientColor)); // 데이터 텍스트 컬러

        CustomBarChartRender barChartRender = new CustomBarChartRender(barChart,barChart.getAnimator(), barChart.getViewPortHandler());
        barChartRender.setRadius(50);
        barChart.setRenderer(barChartRender); // 그래프 모서리 radius 설정부분
        barChart.setBackgroundColor(Color.rgb(255, 255, 255));
        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);

        barChart.invalidate();
    }

    public void checkDateChange(Context context) {
        String today = new SimpleDateFormat("yy-MM-dd").format(new Date(System.currentTimeMillis()));
        if(!today.equals(PreferenceManager.getString(context, "today_date"))) {
            PreferenceManager.setString(context, "today_date", today);
            PreferenceManager.setBoolean(context, "start_step_reset", true);
            PreferenceManager.setInt(context, "current_steps", 0);
            new PedometerDB(context).getWritableDatabase().execSQL("insert into pedometerTable values('" +
                    today + "', 0);");
        }
    }

    @Override
    public void onMultiWindowModeChanged(boolean hasFocus) {
        super.onMultiWindowModeChanged(hasFocus);
        if (hasFocus) {
            applyColors();
        }
    }

    // Apply the title/navigation bar color
    private void applyColors() {
        getActivity().getWindow().setStatusBarColor(Color.parseColor("#F37062"));
        getActivity().getWindow().setNavigationBarColor(Color.parseColor("#F37062"));
    }
}