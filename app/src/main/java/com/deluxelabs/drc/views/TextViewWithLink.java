package com.deluxelabs.drc.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.deluxelabs.drc.utils.Common;
import com.deluxelabs.drc.utils.GrxPrefsUtils;

/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */

@SuppressLint("AppCompatCustomView")
public class TextViewWithLink extends TextView implements View.OnClickListener{

    private String mUrl;

    public TextViewWithLink(Context context){
        super(context);
    }

    public TextViewWithLink(Context context, AttributeSet attrs){
        super(context, attrs);
        ini_params(attrs);
    }

    private void ini_params(AttributeSet attributeSet) {
        mUrl = attributeSet.getAttributeValue(null, Common.INFO_ATTR_ULR);
        boolean mAnimateText = attributeSet.getAttributeBooleanValue(null, Common.INFO_ATTR_ANIMATE_TEXT, false);
        if(mUrl!=null) {
            setClickable(true);
            setOnClickListener(this);
        }
        if(mAnimateText){
            setSingleLine();
            GrxPrefsUtils.animateTextviewMarqueeForever(this);
            setSelected(true);
        }
    }

    @Override
    public void onClick(View view){
        if(mUrl!=null){
            Intent myintent=new Intent(Intent.ACTION_VIEW);
            myintent.setData(Uri.parse(mUrl));
            getContext().startActivity(myintent);
        }
    }

}
