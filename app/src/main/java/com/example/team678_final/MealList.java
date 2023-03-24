package com.example.team678_final;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*public class MealList extends AppCompatActivity {
    //date로 달력에서 선택한 날짜가 넘어올 것
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_list);
        //이 문장으로 date에 날짜를 넘겨주니 지우지 말 것
        date = PreferenceManager.getString(this, "meal_list_date");
        Log.e("test", date);

        TextView test = findViewById(R.id.test);
        test.setText(date);
    }
}*/

public class MealList extends AppCompatActivity {

    //MealLst
    private MealInfoDB infoHelper;
    private MealMenuDB menuHelper;
    private SQLiteDatabase db;
    Cursor cursor;
    ArrayList<Object> mealInfo;
    ListView lst_listview, lst_listview2, lst_listview3, lst_listview4;
    String date;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meal_lst);

        //MealLst
        lst_listview = findViewById(R.id.lst_listview);
        lst_listview2 = findViewById(R.id.lst_listview2);
        lst_listview3 = findViewById(R.id.lst_listview3);
        //lst_listview4 = findViewById(R.id.lst_listview4);

        button = findViewById(R.id.return_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

                //액티비티 전환 애니메이션 설정하는 부분
                overridePendingTransition(R.anim.none, R.anim.vertical_exit);
            }
        });

        //이 문장으로 date에 날짜를 넘겨주니 지우지 말 것
        date = PreferenceManager.getString(this, "meal_list_date");

        // 리스트뷰 화면에 나타내기
        displaylist_bindlist(date);

        // status, bottomNav bar 색상 변경
        onMultiWindowModeChanged(true);
    }
    // end onCreate()...

    // 액티비티 이동하면 실행
    @Override
    public void onResume() {
        super.onResume();
        if(PreferenceManager.getBoolean(this, "insert_check_lst")) {
            // status, bottomNav bar 색상 변경
            onMultiWindowModeChanged(true);

            displaylist_bindlist(date);

            PreferenceManager.removeKey(this, "insert_check_lst");
            Log.e("test", PreferenceManager.getBoolean(this, "insert_check_lst") + "");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }*/

    // 각 time 별로 식단 리스트 출력 & 리스트 삭제 호출
    public void displaylist_bindlist(String date) {
        MealLst_ListViewAdapter adapter1 ,adapter2, adapter3, adapter4;
        adapter1 = new MealLst_ListViewAdapter();
        adapter2 = new MealLst_ListViewAdapter();
        adapter3 = new MealLst_ListViewAdapter();
        //adapter4 = new MealLst_ListViewAdapter();

        displayList(lst_listview, adapter1, date, "아침");
        bindList(lst_listview, adapter1);
        displayList(lst_listview2, adapter2, date, "점심");
        bindList(lst_listview2, adapter2);
        displayList(lst_listview3, adapter3, date, "저녁");
        bindList(lst_listview3, adapter3);
        /*displayList(lst_listview4, adapter4, date, "간식");
        bindList(lst_listview4, adapter4);*/
    }

    // 식단 리스트 출력 메소드
    public void displayList(ListView listView, MealLst_ListViewAdapter adapter, String date, String time){

        menuHelper = new MealMenuDB(this);
        infoHelper = new MealInfoDB(this);

        Log.e("displaydate", date);

        db = menuHelper.getReadableDatabase();
        String sql = "select * from mealMenuTable where date like '" + date + "%' and " +
                "time like '" +time +"';";

        Log.e("sql", sql);

        cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            db = infoHelper.getReadableDatabase();

            String mealStr = cursor.getString(0).substring(9, 11);

            Log.e("mealStr", mealStr);

            mealInfo = MealInfoDB.getmealInfo(db, cursor.getInt(2));

            adapter.addItemToList(mealInfo.get(0).toString(),
                    cursor.getInt(2),
                    ((int)mealInfo.get(1) * cursor.getInt(1)),
                    mealStr, cursor.getString(0)
            );
        }
        listView.setAdapter(adapter);
    }

    // 리스트 삭제
    private void bindList(ListView listView, MealLst_ListViewAdapter listViewAdapter) {

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a_parent, View a_view, final int a_position, long a_id) {
                // MealLst_ListViewAdapterData 에 저장된 db 정보 가져오기
                MealLst_ListViewAdapterData item = (MealLst_ListViewAdapterData) listViewAdapter.getItem(a_position);
                // Popup menu 생성
                PopupMenu popup = new PopupMenu(MealList.this, a_view);
                popup.getMenuInflater().inflate(R.menu.menu_listview, popup.getMenu());
                // Popup menu click event 처리
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    private static final String TAG = "Team678";

                    @Override
                    public boolean onMenuItemClick(MenuItem a_item) {
                        switch (a_item.getItemId()) {
                            case R.id.delete:
                                Log.d(TAG, "입력한 시간 : " +item.getDate());
                                Log.d(TAG, "인덱스 위치 : " +listViewAdapter.getItemId(a_position));
                                Log.d(TAG, "사이즈 크기 : " +listViewAdapter.getCount());
                                // db 삭제
                                new MealMenuDB(MealList.this).deleteMenu(item.getDate());
                                // 리스트 아이템 삭제
                                listViewAdapter.removeItem(a_position);
                                listViewAdapter.notifyDataSetChanged();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

                // Popup 보이기
                popup.show();

                return true;
            }
        });
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
        getWindow().setStatusBarColor(Color.parseColor("#FF9C47"));
        getWindow().setNavigationBarColor(Color.parseColor("#FF9C47"));
    }
}