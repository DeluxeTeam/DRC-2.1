package com.deluxelabs.drc.views;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.deluxelabs.drc.R;
import com.deluxelabs.drc.utils.Common;


/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */


public class LinearLayoutWithLink extends LinearLayout implements View.OnClickListener{

    private String mUrl;

    public LinearLayoutWithLink(Context context){
        super(context);
    }

    public LinearLayoutWithLink(Context context, AttributeSet attrs){
        super(context, attrs);
        ini_params(attrs);
    }
    private void ini_params(AttributeSet attributeSet) {
        mUrl = attributeSet.getAttributeValue(null, Common.INFO_ATTR_ULR);
        if(mUrl!=null) {
            setClickable(true);
            setOnClickListener(this);
        }
    }
    @Override
    public void onClick(View view){
        if(mUrl!=null){
            Intent myintent=new Intent(Intent.ACTION_VIEW);
            myintent.setData(Uri.parse(mUrl));
            try {
                getContext().startActivity(myintent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), R.string.activity_not_found, Toast.LENGTH_LONG).show();
            }
        }
    }

}
