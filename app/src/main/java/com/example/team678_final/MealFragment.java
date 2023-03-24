package com.example.team678_final;

import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MealFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private Context context;
    private Intent intent;
    private MealInfoDB infoHelper;
    private MealMenuDB menuHelper;
    private SQLiteDatabase db;
    Cursor cursor;


    AutoCompleteTextView tv_auto_complete;
    private List<String> searchWord = new ArrayList();

    Button btn_search;
    String foodName;
    int code = -1;
    int num = 1;

    TextView tv_meal_name;
    TextView tv_meal_total_calorie;
    TextView tv_meal_servings;
    TextView tv_meal_how_many;

    Button btn_meal_num_plus;
    Button btn_meal_num_minus;

    Button btn_meal_morning_insert;
    Button btn_meal_lunch_insert;
    Button btn_meal_dinner_insert;

    CircularProgressBar progress_circular_meal;
    TextView tv_total_calories;
    TextView tv_remaining_calories;

    Button btn_meal_detail;

    public static MealFragment newInstance(int number) {
        Bundle bundle = new Bundle();
        bundle.putInt("number", number);

        MealFragment dietFragment = new MealFragment();
        dietFragment.setArguments(bundle);
        return dietFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();

        if (getArguments() != null) {
            int num = getArguments().getInt("number");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(inflater.getContext()).inflate(R.layout.fragment_meal, container, false);

        infoHelper = new MealInfoDB(context);
        menuHelper = new MealMenuDB(context);

        searchWord = infoHelper.setSearchWord();
        tv_auto_complete = view.findViewById(R.id.tv_auto_complete);

        tv_auto_complete.setAdapter(new ArrayAdapter(context,
                android.R.layout.simple_dropdown_item_1line, searchWord));

        tv_meal_name = view.findViewById(R.id.tv_meal_name);
        tv_meal_total_calorie = view.findViewById(R.id.tv_meal_total_calorie);
        tv_meal_servings = view.findViewById(R.id.tv_meal_servings);
        tv_meal_how_many = view.findViewById(R.id.tv_meal_how_many);
        btn_search = view.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(this);

        btn_meal_num_plus = view.findViewById(R.id.btn_meal_num_plus);
        btn_meal_num_plus.setOnClickListener(this);
        btn_meal_num_minus = view.findViewById(R.id.btn_meal_num_minus);
        btn_meal_num_minus.setOnClickListener(this);

        btn_meal_morning_insert = view.findViewById(R.id.btn_meal_morning_insert);
        btn_meal_morning_insert.setOnLongClickListener(this);
        btn_meal_lunch_insert = view.findViewById(R.id.btn_meal_lunch_insert);
        btn_meal_lunch_insert.setOnLongClickListener(this);
        btn_meal_dinner_insert = view.findViewById(R.id.btn_meal_dinner_insert);
        btn_meal_dinner_insert.setOnLongClickListener(this);

        progress_circular_meal = view.findViewById(R.id.progress_circular_meal);
        tv_total_calories = view.findViewById(R.id.tv_total_calories);
        tv_remaining_calories = view.findViewById(R.id.tv_remaining_calories);

        btn_meal_detail = view.findViewById(R.id.btn_meal_detail);
        btn_meal_detail.setOnLongClickListener(this);

        updateCalories();

        // status, bottomNav bar 색상 변경
        onMultiWindowModeChanged(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // status, bottomNav bar 색상 변경
        onMultiWindowModeChanged(true);
        updateCalories();
        resetTextView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_search:

                foodName = tv_auto_complete.getText().toString();

                if(foodName.length() == 0) {
                    code = -1;
                }

                db = infoHelper.getReadableDatabase();

                cursor = db.rawQuery("select * from mealInfoTable where name = '"
                        + foodName +"'", null);

                boolean chk = true;

                while(cursor.moveToNext()) {
                    code = cursor.getInt(0);
                    StringBuffer name = new StringBuffer(cursor.getString(1));
                    if (name.length() > 5) name.insert(5, "\n");
                    tv_meal_name.setText(name);
                    tv_meal_total_calorie.setText(cursor.getInt(2) + " kcal");
                    tv_meal_servings.setText(cursor.getString(3) + " g");
                    num = 1;
                    tv_meal_how_many.setText(num + "인분");
                    chk = false;
                }

                if(chk)
                    Toast.makeText(context, "입력한 음식이 없습니다",
                            Toast.LENGTH_SHORT).show();

                // 음식 검색 후 키보드 내리기
                InputMethodManager mInputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(tv_auto_complete.getWindowToken(), 0);
                break;

            // 음식 수량 증가
            // setCalorieText() 메소드 사용했습니다.
            case R.id.btn_meal_num_plus:
                if(!tv_meal_name.getText().toString().equals("음식이름")) {
                    if(num > 8) Toast.makeText(context, "9보다 클 수 없습니다", Toast.LENGTH_SHORT).show();
                    else {
                        num = Integer.parseInt(tv_meal_how_many.getText().toString().substring(0,
                                tv_meal_how_many.getText().toString().indexOf("인")));
                        num += 1;
                        tv_meal_how_many.setText(num + "인분");
                        setCalorieText(num, -1);
                    }
                }
                break;

            // 음식 수량 감소
            case R.id.btn_meal_num_minus:
                if(!tv_meal_name.getText().toString().equals("음식이름")) {
                    if(num < 2) Toast.makeText(context, "1보다 작을 수 없습니다", Toast.LENGTH_SHORT).show();
                    else {
                        num = Integer.parseInt(tv_meal_how_many.getText().toString().substring(0,
                                tv_meal_how_many.getText().toString().indexOf("인")));
                        num -= 1;
                        tv_meal_how_many.setText(num + "인분");
                        setCalorieText(num, 1);
                    }
                }
                break;
        }
    }
    // gram 수, calorie 수 변화량 표시
    public void setCalorieText(int num, int n) {
        String[] tmp;
        tmp = tv_meal_servings.getText().toString().split(" ");
        tv_meal_servings.setText((Integer.parseInt(tmp[0]) / (num + n) * num) + " g");
        tmp = tv_meal_total_calorie.getText().toString().split(" ");
        tv_meal_total_calorie.setText((Integer.parseInt(tmp[0]) / (num + n) * num) + " kcal");
    }

    // 식단 추가 메소드(LongClick)
    @Override
    public boolean onLongClick(View view) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        switch (view.getId()) {
            case R.id.btn_meal_morning_insert:
                vibrator.vibrate(50); // 0.05초간 진동
                insertMenu("아침");
                break;

            case R.id.btn_meal_lunch_insert:
                vibrator.vibrate(50); // 0.05초간 진동
                insertMenu("점심");
                break;

            case R.id.btn_meal_dinner_insert:
                vibrator.vibrate(50); // 0.05초간 진동
                insertMenu("저녁");
                break;

            case R.id.btn_meal_detail:
                vibrator.vibrate(50); // 0.05초간 진동
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(),"datePicker");
        }
        return false;
    }

    public void insertMenu(String time) {
        if(code == -1) Toast.makeText(context, "음식이 선택되지 않았습니다", Toast.LENGTH_SHORT).show();
        else {
            db = menuHelper.getWritableDatabase();

            String date = new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

            String sql = "insert into mealMenuTable values('" +
                    date + "', " + num + ", " + code + ", '" + time +"');";
            db.execSQL(sql);

            // ****************************************************이거 해야합니다****************************************************
            updateCalories();
            Toast.makeText(context, "'" +time +"' 식단이 삽입되었습니다.", Toast.LENGTH_SHORT).show();

            resetTextView();
        }
    }

    public void updateCalories() {
        db = menuHelper.getReadableDatabase();
        String sql = "select * from mealMenuTable where date like '" + new SimpleDateFormat("yy-MM-dd HH:mm:ss").
                format(new Date(System.currentTimeMillis())).substring(0, 8) + "%';";
        cursor = db.rawQuery(sql, null);
        Log.e("test", sql);

        int tempCalories = 0;

        while(cursor.moveToNext()) {
            Log.e("test", cursor.getInt(2) + "");
            db = infoHelper.getReadableDatabase();
            Cursor infoCursor = db.rawQuery("select * from mealInfoTable where code = " + cursor.getInt(2) + ";", null);
            while(infoCursor.moveToNext()) {
                tempCalories += infoCursor.getInt(2) * cursor.getInt(1);
            }
        }

        progress_circular_meal.setProgressWithAnimation(tempCalories > 2000 ? 2000 : tempCalories, 1000l);
        tv_total_calories.setText(tempCalories + "");
        tv_remaining_calories.setText(tempCalories > 2000 ? (0 + "") : ((2000 - tempCalories) + ""));
    }

    public void resetTextView() {
        tv_auto_complete.setText("");
        tv_meal_name.setText("음식이름");
        tv_meal_total_calorie.setText("0 kcal");
        tv_meal_servings.setText("0 g");
        num = 1;
        code = -1;
        tv_meal_how_many.setText("1인분");
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
        getActivity().getWindow().setStatusBarColor(Color.parseColor("#FF9C47"));
        getActivity().getWindow().setNavigationBarColor(Color.parseColor("#FF9C47"));
    }
}