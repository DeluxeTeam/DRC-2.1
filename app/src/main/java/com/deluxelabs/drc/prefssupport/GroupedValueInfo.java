package com.deluxelabs.drc.prefssupport;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.deluxelabs.drc.R;
import com.deluxelabs.drc.utils.Common;


import java.util.HashMap;
import java.util.Map;

public class GroupedValueInfo {

    final String mGroupedValueKey;
    String mGroupedValueSystemType = null;
    String mBroadCastAction=null;
    String mBroadCastExtraName=null;
    boolean mExistButtons = false;

    final String mValuesSeparator;
    final String mKeyValueSeparator;

    final Map<String, String> mKeysAlias = new HashMap<>();
    final Map<String, String> mKeysValues = new HashMap<>();
    final Map<String, PrefAttrsInfo.PREF_TYPE> mPrefTypes = new HashMap<>();
    final Map<String, Object> mPrefDefVals = new HashMap<>();

    final boolean mStillDebugging = false;

    String mGroupedValue="";

    final Context mContext;

    final boolean mSaveKeyNames;


    public GroupedValueInfo(String groupedKey, Context context){
        Resources resources = context.getResources();
        mValuesSeparator = resources.getString(R.string.grxs_groupedValuekey_valuesSeparator);
        mKeyValueSeparator = resources.getString(R.string.grxs_groupedValueKey_keyvalueSeparator);
        mSaveKeyNames = resources.getBoolean(R.bool.grxb_groupedValuekey_savekeysnames);
        mGroupedValueKey=groupedKey;
        mContext = context;
    }


    public void addPreferenceConfiguration(String prefkey, Object defval, PrefAttrsInfo.PREF_TYPE preftype, String alias, String systemtype, String broadcastaction,
                                           String broadcastExtraName){
        setBroadCastAction(broadcastaction);
        mBroadCastExtraName=broadcastExtraName;
        setGroupedValueSystemType(systemtype);
        addKeyAlias(prefkey, alias);
        if(preftype!= PrefAttrsInfo.PREF_TYPE.BUTTON ){
            mPrefTypes.put(prefkey, preftype);
            mPrefDefVals.put(prefkey,defval);
        }else {
            mExistButtons=true;
        }
        if (updateKeyValueInMap(prefkey) ) calculateGroupedValue();
    }

    public boolean updateKeyValueInMap(String prefKey) {
        // if(mKeysValues.get(prefKey) == null ) return false;
        if (mPrefDefVals.get(prefKey) == null) return false;
        if( mPrefTypes.get(prefKey) == null ) return false;

        switch (mPrefTypes.get(prefKey)) {
            case BOOL:
                boolean defvalue;
                if ( mPrefDefVals.get(prefKey) == null ) defvalue = false;
                else defvalue = (boolean) mPrefDefVals.get(prefKey);
                boolean currval = Common.sp.getBoolean(prefKey,defvalue);
                mKeysValues.put(prefKey, currval ? "1" : "0"  ) ;
                return true;

            case INT:
                int defval ;
                if(mPrefDefVals.get(prefKey) == null) defval = 0;
                else defval = (int) mPrefDefVals.get(prefKey);
                int currvali = Common.sp.getInt(prefKey,defval);
                mKeysValues.put(prefKey,String.valueOf(currvali));
                return true;

            case STRING:
                String defvals ;
                if(mPrefDefVals.get(prefKey) == null ) defvals="";
                else defvals = (String) mPrefDefVals.get(prefKey);
                String currvals = Common.sp.getString(prefKey,defvals);
                if(currvals ==null ) currvals = "";
                mKeysValues.put(prefKey, currvals);
                return true;
            default: break;
        }

        return  false;
    }



    private void setGroupedValueSystemType(String systemType){
        if ( (mGroupedValueSystemType==null && !TextUtils.isEmpty(systemType) ) )
                    mGroupedValueSystemType = systemType;
    }

    private void setBroadCastAction(String bc){
        if(mBroadCastAction==null && !TextUtils.isEmpty(bc))
                    mBroadCastAction = bc;
    }

    public void setExistButtons() {
        mExistButtons = true;
    }

    private void addKeyAlias(String key, String alias){
        if (TextUtils.isEmpty(alias) || TextUtils.isEmpty(key) ) return;
        mKeysAlias.put(key, alias);
    }


    public void updatePreferenceValue(String prefkey) {
        if (updateKeyValueInMap( prefkey) ) calculateGroupedValue();
        if(!mExistButtons) {
            notifyGroupedValueChanged();
        }
    }

    public void notifyGroupedValueChanged() {
        boolean error = false;

        if(TextUtils.isEmpty(mGroupedValueSystemType) || mContext.getResources().getBoolean(R.bool.grxb_demo_mode)) return;
        if(!mContext.getResources().getBoolean(R.bool.grxb_global_enable_settingsdb)) return;

        switch (mGroupedValueSystemType){
            case "secure":
                try{
                    Settings.Secure.putString(mContext.getContentResolver(), mGroupedValueKey, mGroupedValue);
                }catch (Exception e){
                    error = true;
                    Log.d("grxgrx ", e.toString());
                }
                break;
            case "global" :
                try{
                    Settings.Global.putString(mContext.getContentResolver(), mGroupedValueKey, mGroupedValue);
                }catch (Exception e){
                    error = true;
                    Log.d("grxgrx ", e.toString());
                }
                break;
            case "system":
                try{
                    Settings.System.putString(mContext.getContentResolver(), mGroupedValueKey, mGroupedValue);
                }catch (Exception e){
                    error = true;
                    Log.d("grxgrx ", e.toString());
                }
                break;
            default:
                break;
        }

        if(error) return;
        if(TextUtils.isEmpty(mBroadCastAction)) return;
        Intent intent = new Intent();
        intent.setAction(mBroadCastAction);
        if (mStillDebugging) Toast.makeText(mContext, "DEBUG - BC - " + mBroadCastAction + " + " + mBroadCastExtraName + " + " + mGroupedValueKey, Toast.LENGTH_SHORT).show();
        if (mBroadCastExtraName == null || mBroadCastExtraName.isEmpty()) {
            intent.putExtra(mGroupedValueKey, mGroupedValue);
        } else {
            intent.putExtra(mBroadCastExtraName, mGroupedValueKey);
        }
        mContext.sendBroadcast(intent);
    }

    private void calculateGroupedValue(){
        mGroupedValue = "";
        for(String key : mKeysValues.keySet()) {
            if(mSaveKeyNames) {
                String alias = mKeysAlias.get(key);
                if(TextUtils.isEmpty(alias)) alias = key;
                mGroupedValue+=alias;
                mGroupedValue+=mKeyValueSeparator;
            }
            String value = mKeysValues.get(key);
            if(value==null) value="";
            mGroupedValue+=value;
            mGroupedValue+=mValuesSeparator;
        }
        Common.sp.edit().putString(mGroupedValueKey,mGroupedValue).commit();
        if(mStillDebugging) Log.d("grxgrx grouped value = ", mGroupedValueKey+"="+mGroupedValue);
    }


    public void onGroupedValueButtonPressed(){
        calculateGroupedValue();
        notifyGroupedValueChanged();
    }

    public String getGroupedValueSystemType() {
        return mGroupedValueSystemType;
    }


    public String getGroupedValue(){
        return mGroupedValue;
    }

    public void recalculateGroupedValueForSync(){
        calculateGroupedValue();
        notifyGroupedValueChanged();
    }
}
