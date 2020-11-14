/* 
 * Grouxho - espdroids.com - 2018	

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 
 */



package android.preference;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import android.util.AttributeSet;


import com.deluxelabs.drc.GrxPreferenceScreen;
import com.deluxelabs.drc.R;

import com.deluxelabs.drc.prefs_dlgs.DlgFrGrxMultiAccess;
import com.deluxelabs.drc.utils.Common;
import com.deluxelabs.drc.utils.GrxPrefsUtils;

import java.util.regex.Pattern;


public class GrxMultiAccess extends GrxBasePreference implements DlgFrGrxMultiAccess.GrxMultiAccessListener {

    private boolean mShowShortCuts;
    private boolean mShowUsuApps;
    private boolean mShowActivities;
    private boolean mSaveCustomActionsIcons;
    int iconsValueTint =0;

    public GrxMultiAccess(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
    }


    public GrxMultiAccess(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        initAttributes(context,attrs);
    }

    private void initAttributes(Context context, AttributeSet attrs){
        setWidgetLayoutResource(R.layout.widget_icon_accent);
        initStringPrefsCommonAttributes(context,attrs,true, true);

        Resources res = context.getResources();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.grxPreferences);
        mSaveCustomActionsIcons = ta.getBoolean(R.styleable.grxPreferences_saveActionsicons, res.getBoolean(R.bool.grxb_saveActionsicons_default));
        mShowShortCuts = ta.getBoolean(R.styleable.grxPreferences_showShortcuts, res.getBoolean(R.bool.grxb_showShortcuts_default) );
        mShowUsuApps = ta.getBoolean(R.styleable.grxPreferences_showApplications, res.getBoolean(R.bool.grxb_showApplications_default) );
        mShowActivities=ta.getBoolean(R.styleable.grxPreferences_showActivities, res.getBoolean(R.bool.grxb_showActivities_default) );
        if(ta.hasValue(R.styleable.grxPreferences_iconsValueTint)) {
            try {
                iconsValueTint = ta.getInt(R.styleable.grxPreferences_iconsValueTint, 0);

            } catch (Exception ignored) {
            }
        }
        ta.recycle();
        setDefaultValue(myPrefAttrsInfo.getMyStringDefValue());
    }


    @Override
    public void showDialog(){
            GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
            if(prefsScreen!=null){
                DlgFrGrxMultiAccess dlg = (DlgFrGrxMultiAccess) prefsScreen.getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRMULTIACCESS);
                if(dlg!=null) return;
                dlg = DlgFrGrxMultiAccess.newInstance(this, Common.TAG_PREFSSCREEN_FRAGMENT, myPrefAttrsInfo.getMyKey(), myPrefAttrsInfo.getMyTitle(),mStringValue,
                        mShowShortCuts, mShowUsuApps,mShowActivities, myPrefAttrsInfo.getMyOptionsArrayId(), myPrefAttrsInfo.getMyValuesArrayId(), myPrefAttrsInfo.getMyIconsArrayId(),
                        iconsValueTint,mSaveCustomActionsIcons, myPrefAttrsInfo.getMySeparator(), myPrefAttrsInfo.getMyMaxItems());
                dlg.show(prefsScreen.getFragmentManager(),Common.TAG_DLGFRGRMULTIACCESS);
            }
    }


    @Override
    public void resetPreference(){
        if(mStringValue.isEmpty()) return;

         String[] uris = mStringValue.split(Pattern.quote(myPrefAttrsInfo.getMySeparator()));
        for (String s : uris) GrxPrefsUtils.deleteGrxIconFileFromUriString(s);

         mStringValue= myPrefAttrsInfo.getMyStringDefValue();
         configStringPreference(mStringValue);
         saveNewStringValue(mStringValue);
    }


    @Override
    public void configStringPreference(String value){
        int numitems=0;
        if(!mStringValue.isEmpty()){
            String[] arr = mStringValue.split(Pattern.quote(myPrefAttrsInfo.getMySeparator()));
            numitems=arr.length;
        }
        String mLabel;
        if(numitems==0) mLabel =myPrefAttrsInfo.getMySummary();
        else mLabel = getContext().getString( R.string.grxs_num_selected,numitems ) ;
        setSummary(myPrefAttrsInfo.getMySummary() + " " + mLabel);
    }

    public void GrxSetMultiAccess(String value){
        if(!mStringValue.equals(value)){
            mStringValue=value;
            configStringPreference(value);
            saveNewStringValue(value);
            }
    }

}
