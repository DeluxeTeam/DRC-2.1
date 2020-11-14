
/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */


package com.deluxelabs.drc.prefs_dlgs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;

import com.deluxelabs.drc.utils.Common;

import java.util.Calendar;
import java.util.Objects;

public class DlgFrGrxDatePicker extends DialogFragment {

    private String mkey;
    private DlgFrGrxDatePicker.OnGrxDateSetListener mCallBack;

    private OnGrxDateSetListener mCallback;

    public DlgFrGrxDatePicker(){}


    public static DlgFrGrxDatePicker newInstance(String key, String value){

        DlgFrGrxDatePicker ret = new DlgFrGrxDatePicker();
        Bundle bundle = new Bundle();
        bundle.putString("key",key);
        bundle.putString("val",value);
        ret.setArguments(bundle);
        return ret;
    }

    public void setOnGrxDateSetListener(OnGrxDateSetListener callback){
        mCallback=callback;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public interface OnGrxDateSetListener{
        void onGrxDateSet(String value, String key);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(mCallback==null) mCallback=(DlgFrGrxDatePicker.OnGrxDateSetListener) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
            mkey=getArguments().getString("key");
        String mValue = getArguments().getString("val");

        int year = 0;
        int month = 0;
        int day = 0;
        Calendar now = Calendar.getInstance();
        if(Objects.requireNonNull(mValue).isEmpty()){
            year = now.get(Calendar.YEAR);
            month = now.get(Calendar.MONTH);
            day = now.get(Calendar.DAY_OF_MONTH);
        }else{
            String[] arr= mValue.split("/");
            try{
                day = Integer.parseInt(arr[0]);
                month = Integer.parseInt(arr[1])-1;
                year = Integer.parseInt(arr[2]);
            }catch (NumberFormatException casque){
                System.out.println("Wrong date format (grxsettings)" + casque);
            }
        }
            DatePickerDialog dpd = new DatePickerDialog(getActivity(), (view, year1, monthOfYear, dayOfMonth) -> {
                if(mCallback!=null) mCallback.onGrxDateSet(dayOfMonth +"/"+ (monthOfYear + 1) +"/"+ year1,mkey);
                else {
                    Log.d("grxsettings", "null callback in datepicker");
                }
            }, year, month, day);
        return dpd;
    }

}
