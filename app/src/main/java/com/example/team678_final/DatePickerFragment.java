package com.example.team678_final;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(),this,year,month,day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String date = (year % 100) + "-" + (month + 1) + "-" + (day < 10 ? ("0" + day) : day);
        PreferenceManager.setString(getActivity(), "meal_list_date", date);
        startActivity(new Intent(getActivity(), MealList.class));

        //액티비티 전환 애니메이션 설정하는 부분
        getActivity().overridePendingTransition(R.anim.vertical_enter, R.anim.none);
    }
}