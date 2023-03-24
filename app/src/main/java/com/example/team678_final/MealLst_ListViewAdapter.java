package com.example.team678_final;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MealLst_ListViewAdapter extends BaseAdapter {

    public ArrayList<MealLst_ListViewAdapterData> list = new ArrayList<>();
    private ArrayList<MealLst_ListViewAdapterData> arrayList = list; //백업리스트
    @Override
    public int getCount() {
        return arrayList.size(); //그냥 배열의 크기를 반환하면 됨
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i); //배열에 아이템을 현재 위치값을 넣어 가져옴
    }

    @Override
    public long getItemId(int i) {
        return i; //그냥 위치값을 반환해도 되지만 원한다면 아이템의 num 을 반환해도 된다.
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // ItemView의 각 요소를 바로 엑세스 할 수 있도록 저장해두고 사용하기 위한 객체 ViewHolder
        ViewHolder holder;

        //리스트뷰에 아이템이 인플레이트 되어있는지 확인한후
        //아이템이 없다면 아래처럼 아이템 레이아웃을 인플레이트 하고 view객체에 담는다.
        if(view == null){
            holder = new ViewHolder();
            //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.meal_lst_item,viewGroup,false);
            //이제 아이템에 존재하는 텍스트뷰 객체들을 view객체에서 찾아 가져온다
            TextView lst_item_meal_name = view.findViewById(R.id.lst_item_meal_name);
            //TextView lst_item_meal_code = view.findViewById(R.id.lst_item_meal_code);
            TextView lst_item_meal_total_calorie = view.findViewById(R.id.lst_item_meal_total_calorie);
            //TextView lst_item_meal = view.findViewById(R.id.lst_item_meal);
            TextView lst_item_date = view.findViewById(R.id.lst_item_date);

            holder.lst_item_meal_name = lst_item_meal_name;
            //holder.lst_item_meal_code = lst_item_meal_code;
            holder.lst_item_meal_total_calorie = lst_item_meal_total_calorie;
            //holder.lst_item_meal = lst_item_meal;
            holder.lst_item_date = lst_item_date;

            view.setTag(holder);
        }
        else{
            // ViewHolder를 getTag로 불러와서 재사용
            // 이렇게하면 xml 리소스에 findeViewById로 직접 접근하지 않아도 됨.
            holder = (ViewHolder) view.getTag();
        }


        //현재 포지션에 해당하는 아이템에 글자를 적용하기 위해 list배열에서 객체를 가져온다.
        MealLst_ListViewAdapterData listdata = arrayList.get(i);

        //가져온 객체안에 있는 글자들을 각 뷰에 적용한다
        holder.lst_item_meal_name.setText(listdata.getName());

        /*int tmp = listdata.getCode();
        switch ((tmp + "").length()) {
            case 1:
                holder.lst_item_meal_code.setText("000" + tmp);
                break;
            case 2:
                holder.lst_item_meal_code.setText("00" + tmp);
                break;
            case 3:
                holder.lst_item_meal_code.setText("0" + tmp);
                break;
            case 4:
                holder.lst_item_meal_code.setText(tmp + "");
                break;
        }*/

        //원래 int형이라 String으로 형 변환
        holder.lst_item_meal_total_calorie.setText(listdata.getCalorie() +"kcal");
        //holder.lst_item_meal.setText(listdata.getMeal());
        holder.lst_item_date.setText(listdata.getDate().substring(9, 14));

        return view;
    }

    //ArrayList로 선언된 list 변수에 목록을 채워주기 위함 다른방시으로 구현해도 됨
    public void addItemToList(String name, int code, int calorie, String meal, String date){
        MealLst_ListViewAdapterData listdata = new MealLst_ListViewAdapterData();

        // 7글자 이상 자동 줄바꿈 처리
        if(name.length() > 7){
            String name1 = name.substring(0, 7);
            String name2 = name.substring(7);
            name = name1 +"\n" + name2;
            Log.e("name1", name1 +"\n" + name2);
            listdata.setName(name);
        } else{
            listdata.setName(name);
        }
        //listdata.setCode(code);
        listdata.setCalorie(calorie);
        listdata.setMeal(meal);
        listdata.setDate(date);

        //값들의 조립이 완성된 listdata객체 한개를 list배열에 추가
        list.add(listdata);
    }
    //리스트 아이템 삭제
    public void removeItem(int position){
        if(list.size() < 1){ }
        else{
            list.remove(position);
        }
    }

    static class ViewHolder{
        TextView lst_item_meal_name, lst_item_meal_total_calorie, lst_item_date;
    }
}
