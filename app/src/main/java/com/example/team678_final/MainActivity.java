package com.example.team678_final;

import static android.hardware.Sensor.TYPE_STEP_COUNTER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.pm.PackageManager;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Fragment> fragments;
    private ViewPager2Adapter viewPager2Adapter;
    private ViewPager2 viewPager2;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!PreferenceManager.getBoolean(this, "info_db_insert_completed")) {
            Toast.makeText(this, "식품정보 DB 갱신 시 약간 시간이 걸립니다.", Toast.LENGTH_LONG).show();
            JSONArray jsonArray;
            JSONObject jsonObject;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                this.getAssets().open("meal_info_db_data.json")));
                StringBuffer buffer = new StringBuffer();

                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line + "\n");
                    line = reader.readLine();
                }

                jsonArray = new JSONArray(buffer.toString());

                for(int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    String str = "insert or replace into mealInfoTable(name, calorie, gram) VALUES('" +
                            jsonObject.getString("식품명") + "', " +
                            (int) Float.parseFloat(jsonObject.getString("에너지(㎉)")) + ", " +
                            Integer.parseInt(jsonObject.getString("1회제공량")) + ");";

                    SQLiteDatabase db = new MealInfoDB(this).getWritableDatabase();
                    db.execSQL(str);

                    Log.e("test" + i, str);
                }

                PreferenceManager.setBoolean(this, "info_db_insert_completed", true);
                Toast.makeText(this, "완료되었습니다.", Toast.LENGTH_SHORT).show();

            } catch (IOException | JSONException e) { e.printStackTrace(); }
        }

        fragments = new ArrayList<>();
        fragments.add(PedometerFragment.newInstance(0));
        fragments.add(MealFragment.newInstance(1));

        viewPager2Adapter = new ViewPager2Adapter(this, fragments);

        viewPager2 = findViewById(R.id.viewPager2_container);
        viewPager2.setAdapter(viewPager2Adapter);

        // BottomNavigationView
        bottomNav = findViewById(R.id.bottom_nav);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            @Override
            public void onPageSelected(int position){
                super.onPageSelected(position);
                switch (position){
                    case 0:
                        bottomNav.getMenu().findItem(R.id.menu_pedometer).setChecked(true);
                        break;
                    case 1:
                        bottomNav.getMenu().findItem(R.id.menu_meal).setChecked(true);
                        break;
                }
            }
        });

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_pedometer:
                        viewPager2.setCurrentItem(0);
                        break;
                    case R.id.menu_meal:
                        viewPager2.setCurrentItem(1);
                }
                return true;
            }
        });
    }
}