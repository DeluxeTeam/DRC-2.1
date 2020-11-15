package com.deluxelabs.drc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.GrxBasePreference;
import android.preference.GrxPickImage;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;

import android.view.ContextThemeWrapper;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import android.preference.GrxCheckBoxPreference;
import android.preference.GrxDatePicker;
import android.preference.GrxPreferenceCategory;
import android.preference.GrxSwitchPreference;
import android.preference.GrxTimePicker;


import com.deluxelabs.drc.act.GrxImagePicker;
import com.deluxelabs.drc.prefs_dlgs.DlgFrGrxDatePicker;
import com.deluxelabs.drc.prefs_dlgs.DlgFrGrxTimePicker;
import com.deluxelabs.drc.prefssupport.CustomDependencyHelper;

import com.deluxelabs.drc.prefssupport.GroupedValueInfo;
import com.deluxelabs.drc.prefssupport.PrefAttrsInfo;
import com.deluxelabs.drc.utils.Common;
import com.deluxelabs.drc.utils.GrxObserver;
import com.deluxelabs.drc.utils.GrxPrefsUtils;
import com.deluxelabs.drc.utils.RootPrivilegedUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;



/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */

public class GrxPreferenceScreen extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        DlgFrGrxDatePicker.OnGrxDateSetListener,
        DlgFrGrxTimePicker.OnGrxTimeSetListener,
        GrxObserver.OnObservedSettingsKeyChange,
        SharedPreferences.OnSharedPreferenceChangeListener
{

    String mCurrentScreen;
    String mCurrentSubScreen;
    String mCurrentKey;
    int mDividerHeight;


    PreferenceScreen mGrxScreen;
    public Map<String, String> mScreensTree;
    private final ArrayList<String> mAuxScreenKeys = new ArrayList<>();
    public LinkedHashMap<String, Integer> mScreenPositions;
    HashSet<String> mGroupKeyList;

    Map<String, GroupedValueInfo> mGroupedValuesInfo;
    Map<String, String> mKeysInGroupedValues;

    boolean mSyncMode=false;
    int mAutoIndexForKey=0;

    List<String> mSettingsKeys=null;
    GrxObserver mGrxObserver=null;

    GrxSettingsActivity mGrxSettingsActivity =null;

    int mNumPrefs=0;

    final Map<String, List<CustomDependencyHelper>> CustomDependencies = new HashMap<>();

    PreferenceScreen mCurrentPreferenceScreen ;

    final List<Preference> mPrefsToRemove = new ArrayList<>();

    public boolean mIsDemoMode=false;

    public GrxPreferenceScreen(){

    }

    static GrxPreferenceScreen newInstance (String screen, String subscreen, String key, int dividerheight){

        GrxPreferenceScreen prefsfragment = new GrxPreferenceScreen();
        Bundle bundle = new Bundle();
        bundle.putString(Common.EXTRA_SCREEN,screen);
        bundle.putString(Common.EXTRA_SUB_SCREEN,subscreen);
        bundle.putString(Common.EXTRA_KEY, key);
        bundle.putInt(Common.EXTRA_DIV_HEIGHT,dividerheight);
        prefsfragment.setArguments(bundle);
        return prefsfragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGrxScreen=null;
        mScreensTree = new HashMap<>();
        mScreenPositions = new LinkedHashMap<>();
        mGroupKeyList = new HashSet<>();

        mGroupedValuesInfo = new HashMap<>();
        mKeysInGroupedValues = new HashMap<>();

        mSettingsKeys = new ArrayList<>();

        mIsDemoMode = getResources().getBoolean(R.bool.grxb_demo_mode);


        if(savedInstanceState==null){
            Bundle bundle = getArguments();
            mCurrentScreen = getArguments().getString(Common.EXTRA_SCREEN);
            mCurrentSubScreen = getArguments().getString(Common.EXTRA_SUB_SCREEN);
            mCurrentKey = getArguments().getString(Common.EXTRA_KEY);
            mDividerHeight = getArguments().getInt(Common.EXTRA_DIV_HEIGHT,getResources().getInteger(R.integer.grxi_default_list_divider_height));
        }else{
            Common.buildContextWrapper(getActivity());
            mCurrentScreen=savedInstanceState.getString(Common.EXTRA_SCREEN);
            mCurrentSubScreen=savedInstanceState.getString(Common.EXTRA_SUB_SCREEN);
            mCurrentKey=savedInstanceState.getString(Common.EXTRA_KEY);
            mDividerHeight=savedInstanceState.getInt(Common.EXTRA_DIV_HEIGHT);
        }


        if(mCurrentScreen!=null && !mCurrentScreen.isEmpty()){
            int i = getActivity().getResources().getIdentifier(mCurrentScreen, "xml", getActivity().getPackageName());

            mPrefsToRemove.clear();

            // Sometimes on reset preferences it throws InflateException, on that case let's just restart the app
            try {
                addPreferencesFromResource(i);
            } catch (InflateException ignored) {
                mGrxSettingsActivity.restartApp();
            }
            mGrxScreen = getPreferenceScreen();
            mNumPrefs=mGrxScreen.getPreferenceCount();
            String c = generateKeyForPreferenceScreen(mGrxScreen.getKey());
            getPreferenceScreen().setKey(c);
            mScreensTree.put(c,"");
            if(!Common.SyncUpMode && Common.GroupKeysList!=null) {
                Common.GroupKeysList.clear();
            }
            if(!Common.SyncUpMode && Common.BroadCastsList!=null) {
                Common.BroadCastsList.clear();
            }
            if(!Common.SyncUpMode && Common.CommonBroadCastList!=null) Common.CommonBroadCastList.clear();
            // Fix NullPointerException on screen density change
            try {
                initPreferenceScreen(mGrxScreen);
            } catch (NullPointerException ignored) {
                mGrxSettingsActivity.restartApp();
            }
            removePreferences();

            if(mCurrentSubScreen!=null && !TextUtils.isEmpty(mCurrentSubScreen)) {
                showScreen((PreferenceScreen) getPreferenceScreen().findPreference(mCurrentSubScreen));
            }

        }



        //update_all_custom_dependencies(); //gives fc here if the app is restarted by the system. F.example changin screen zoom on S7E

    }


    /** help preferences to preserve non standar color attributes */

    private void buildContextWrapper(){
        Common.mContextWrapper = null;
        String themename = Common.sp.getString(Common.S_APPOPT_USER_SELECTED_THEME_NAME, getString(R.string.grxs_default_theme));
        if(themename==null || themename.isEmpty()) return;
        int themeid = getResources().getIdentifier(themename,"style",  getActivity().getPackageName());
        Resources.Theme helpertheme = getResources().newTheme();
        helpertheme.applyStyle(themeid,true);
        Common.mContextWrapper = new ContextThemeWrapper(getActivity(), 0);
        Common.mContextWrapper.getTheme().setTo(helpertheme);
    }


    @Override
    public void onResume() {
        super.onResume();
        if(Common.sp!=null && !Common.SyncUpMode) {
            Common.sp.registerOnSharedPreferenceChangeListener(this);
        }
        refreshSettingsKeys();
/*            if(mGrxSettingsActivity !=null && Common.SyncUpMode){
                mGrxSettingsActivity.onPreferenceScreenSinchronized(mNumPrefs);
            }*/
    }

    @Override
    public  void onStop(){
        super.onStop();
        if(Common.sp!=null && !Common.SyncUpMode) {
            Common.sp.unregisterOnSharedPreferenceChangeListener(this);
        }

   /*     if(Common.SyncUpMode) {
            if(mGroupedValuesInfo!=null && mGroupedValuesInfo.size()>0) {
                for ( String key : mGroupedValuesInfo.keySet() ) {
                    if(key!=null && Common.GroupedValuesForRestoration !=null ){
                        Common.GroupedValuesForRestoration.put(key,mGroupedValuesInfo.get(key));
                    }
                }
            }
       }
*/
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Common.EXTRA_SCREEN,mCurrentScreen);
        outState.putString(Common.EXTRA_SUB_SCREEN,mCurrentSubScreen);
        outState.putString(Common.EXTRA_KEY,mCurrentKey);
        outState.putInt(Common.EXTRA_DIV_HEIGHT,mDividerHeight);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mGrxSettingsActivity = (GrxSettingsActivity) getActivity();

    }



    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){
        if (preference.getClass().getSimpleName().equals("PreferenceScreen")) {
            PreferenceScreen preferenceScreen1 = (PreferenceScreen) preference;
            if(preferenceScreen1.getIntent()==null) showScreen((PreferenceScreen) preference);
        }
        return false;
    }

    private void showScreen(PreferenceScreen preferenceScreen){
        if(preferenceScreen!=null){
            String p_actual= getPreferenceScreen().getKey();
            mScreenPositions.put(p_actual, getListPosition());
            PreferenceScreen p = preferenceScreen;
            if(p.getDialog()!=null) p.getDialog().dismiss();
            setPreferenceScreen(preferenceScreen);
            mGrxSettingsActivity.onBackKey(preferenceScreen.getTitle());
            setListPosition(mScreenPositions.get(getPreferenceScreen().getKey()));
            mCurrentSubScreen=getPreferenceScreen().getKey();
            mGrxSettingsActivity.onScreenChange(mCurrentSubScreen);
        }
        if (!(mCurrentKey==null || mCurrentKey.isEmpty())){
            Preference pref = getPreferenceScreen().findPreference(mCurrentKey);
            if(pref!=null) {
                if(pref.isEnabled()) getPreferenceScreen().onItemClick(null,null,pref.getOrder(),0);
            }
            mCurrentKey=null;
        }

    }

    private void setListPosition(final int pos){
        View rootView = getView();
        if(rootView!=null){
            final ListView list = (ListView) rootView.findViewById(android.R.id.list);
            if(list!=null) {
                list.clearFocus();
                list.post(() -> list.setSelection(pos));
            }
        }

    }

    private int getListPosition(){
        int pos=0;
        View rootView = getView();
        if(rootView!=null){
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            if(list!=null) pos=list.getFirstVisiblePosition();
        }
        return pos;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) { //grxgrx
        super.onActivityCreated(savedInstanceState);

        if(Common.SyncUpMode) {
            if(mGroupedValuesInfo!=null && mGroupedValuesInfo.size()>0) {
                for ( String key : mGroupedValuesInfo.keySet() ) {
                    if(key!=null && Common.GroupedValuesForRestoration !=null ){
                        Common.GroupedValuesForRestoration.put(key,mGroupedValuesInfo.get(key));
                    }
                }
            }
            mGrxSettingsActivity.onPreferenceScreenSinchronized(mNumPrefs);
            return;
        }
        //mGrxSettingsActivity = (GrxSettingsActivity) getActivity();
        View rootView = getView();
        assert rootView != null;
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDividerHeight(mDividerHeight);
        mGrxSettingsActivity.onListReady(list);
        String mScreenTemp = mScreensTree.get(getPreferenceScreen().getKey());
        if(mScreenTemp==null || mScreenTemp.isEmpty() ) mGrxSettingsActivity.onBackKey("");
        else {
            if(mCurrentSubScreen!=null && !mCurrentSubScreen.isEmpty()){
                mGrxSettingsActivity.onBackKey(getPreferenceScreen().getTitle());
            }
        }
        updateAllCustomDependencies();
    }


    public void forceHideFloatingWindow(){
        if(mGrxSettingsActivity!=null) mGrxSettingsActivity.hideFloatingWindow();
    }


    private void initPreferenceScreen(PreferenceScreen ps){
        int nprefs = ps.getPreferenceCount();
        String nps = ps.getKey();
        mAuxScreenKeys.add(nps);
        for(int i=0;i<nprefs;i++){
            initPreference(ps.getPreference(i), mAuxScreenKeys.get(mAuxScreenKeys.size()-1));
        }
        mAuxScreenKeys.remove(nps);
    }

    private void initPreferenceCatetgory(GrxPreferenceCategory cat){
        for(int i=0;i<cat.getPreferenceCount();i++){
            initPreference(cat.getPreference(i),mAuxScreenKeys.get(mAuxScreenKeys.size()-1));
        }
    }


    private void initPreference(Preference pref, String pant){
        switch (pref.getClass().getSimpleName()) {
            case "PreferenceScreen":
                PreferenceScreen pst = (PreferenceScreen) pref;
                pst.setWidgetLayoutResource(R.layout.widget_icon_accent);
                String c = generateKeyForPreferenceScreen(pst.getKey());
                pst.setKey(c);
                mScreensTree.put(c,pant);
                //mScreenPositions.put(c,mScreensTree.size()-1);
                mScreenPositions.put(c,0); // fix initial list positions in nested screens
                initPreferenceScreen(pst);
                break;
            case "GrxPreferenceCategory":
                GrxPreferenceCategory pc = (GrxPreferenceCategory) pref;
                pc.setOnPreferenceChangeListener(this);
                initPreferenceCatetgory(pc);
                break;
            default:
                if(pref instanceof GrxBasePreference || pref instanceof GrxSwitchPreference || pref instanceof GrxCheckBoxPreference)
                    pref.setOnPreferenceChangeListener(this);
                break;
        }
    }


    @Override
    public boolean onPreferenceChange(Preference pref,Object newvalue){
        PrefAttrsInfo prefAttrsInfo=null;

        String prefKey = pref.getKey();
        if(!TextUtils.isEmpty(prefKey)) {
            if(mKeysInGroupedValues.containsKey(prefKey)) {
                updateAllCustomDependencies(pref.getKey() , newvalue);
                return true;
            }
        }

        switch (pref.getClass().getSimpleName()) {
            case "GrxSwitchPreference":
            case "GrxFilePreference":
                GrxSwitchPreference swp = (GrxSwitchPreference) pref;
                swp.saveValueInSettings(!swp.isChecked());
                prefAttrsInfo = swp.getPrefAttrsInfo();
                break;
            case "GrxCheckBoxPreference":
                GrxCheckBoxPreference cbp = (GrxCheckBoxPreference) pref;
                cbp.saveValueInSettings(!cbp.isChecked());
                prefAttrsInfo = cbp.getPrefAttrsInfo();
                break;
            default:
                if (pref instanceof GrxBasePreference) prefAttrsInfo = ((GrxBasePreference) pref).getPrefAttrsInfo();
                break;
        }

        updateAllCustomDependencies(pref.getKey() , newvalue);

        if(prefAttrsInfo==null) return true;

        /*** process actions after preference changes **/

        String actions= prefAttrsInfo.getMyProcessActionOrder();
        if(actions==null) actions = getString(R.string.grxs_process_actions_order);
        String [] actions_to_process = actions.split(Pattern.quote("|"));
        for (String actionsToProcess : actions_to_process) {
            switch (actionsToProcess) {
                case "reboot":
                    if (prefAttrsInfo.isNeededReboot()) {
                        if (prefAttrsInfo.isNeededRebootDialog())
                            mGrxSettingsActivity.rebootDevice(true);
                        else mGrxSettingsActivity.rebootDevice(false);
                    }
                    break;
                case "kill":
                    // There is never a need to kill a package using root
                    // Apps needing reboot will be killed with Process.killProcess(Process.myPid()); while using DeluxeROM
                    // So we still assert no root is needed to change configurations
                    break;
                case "groupkey":
                    changeGroupKey(prefAttrsInfo.getMyGroupKey());

                    break;
                case "onclick":
                    String keytoclick = prefAttrsInfo.getmKeyToClick();
                    if (keytoclick != null) {
                        Preference preference = getPreferenceScreen().findPreference(keytoclick);
                        if (preference != null) {
                            if (preference.isEnabled()) {
                                String destRule = prefAttrsInfo.getMyOnClickRule().split(Pattern.quote("#"))[2];
                                performOnClickRule(preference, destRule);
                            }
                        }
                    }

                    break;

                case "broadcasts":
                    if (prefAttrsInfo.getMyCommonBcExtra() != null) {
                        GrxPrefsUtils.sendCommonBroadCastExtraDelayed(getActivity(), prefAttrsInfo.getMyCommonBcExtra(), prefAttrsInfo.getMyCommonBcExtraValue(), false);
                        if (mIsDemoMode) {
                            Toast.makeText(getActivity(), "Common BC send with extra : " + prefAttrsInfo.getMyCommonBcExtra() + " = " + prefAttrsInfo.getMyCommonBcExtraValue(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    sendPreferenceBroadcasts(prefAttrsInfo.getMyBroadCast1(), prefAttrsInfo.getMyBroadCast1Extra(),
                            prefAttrsInfo.getMyBroadCast2(), prefAttrsInfo.getMyBroadCast2Extra());

                    break;

                case "scripts":
                    if (!Common.IsRooted) {
                        if (mGrxSettingsActivity != null) mGrxSettingsActivity.showRootState();
                        break;
                    }
                    switch (prefAttrsInfo.getMyScriptType()) {
                        case ARRAY:
                            runArrayScript(prefAttrsInfo.getMyScriptArrayId(), prefAttrsInfo.getMyScriptToast(), prefAttrsInfo.isNeededConfirmationDialgotToRunScript());
                            break;
                        case FILE:
                            String arguments = prefAttrsInfo.getMyScriptFileArguments();
                            if (arguments == null || arguments.isEmpty() || !arguments.contains(" "))
                                runFileScript(prefAttrsInfo.getMyScriptFileName(), prefAttrsInfo.getMyScriptToast(), prefAttrsInfo.isNeededConfirmationDialgotToRunScript(), prefAttrsInfo.getMyScriptFileArguments());
                            else {
                                runFileScript(prefAttrsInfo.getMyScriptFileName(), prefAttrsInfo.getMyScriptToast(), prefAttrsInfo.isNeededConfirmationDialgotToRunScript(), arguments.split(Pattern.quote(" ")));
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;

            }
        }
        return true;
    }


    private void performOnClickRule(Preference pref, String destrule){

        switch (pref.getClass().getSimpleName()) {
            case "GrxSwitchPreference":
            case "GrxFilePreference":
                GrxSwitchPreference swp = (GrxSwitchPreference) pref;
                swp.performOnclickRule(destrule);

                break;
            case "GrxCheckBoxPreference":
                GrxCheckBoxPreference cbp = (GrxCheckBoxPreference) pref;
                cbp.performOnclickRule(destrule);

                break;

            default:
                if (pref instanceof GrxBasePreference) {
                    GrxBasePreference bp = (GrxBasePreference) pref;
                    bp.performOnclickRule(destrule);
                }
                break;
        }

    }


    private String generateKeyForPreferenceScreen(String clave){
        String tmp = clave;
        if((clave==null)||clave.isEmpty()) {
            tmp = mCurrentScreen + mAutoIndexForKey;
            mAutoIndexForKey++;
        }
        return tmp;
    }

    private void updateGroupKeyList(String gcr){
        if(gcr!=null && !gcr.isEmpty()){
            mGroupKeyList.add(gcr);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            int i= lv.getListPaddingLeft();
            int t = lv.getListPaddingTop();
            int d = lv.getListPaddingRight();
            int a = lv.getListPaddingBottom();
            a+=getResources().getDimensionPixelSize(R.dimen.grx_padding_lista_preferencias);
            lv.setPadding(i,t,d,a);
        }
        return v;
    }


    private void show_toast(String msg){
        if(msg==null) return;
        Toast.makeText(this.getActivity() ,msg,Toast.LENGTH_SHORT).show();
    }

    private void show_snack_msg(String msg){
        if(isAdded()) Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public boolean processBackPressed(){
        boolean vd = true;
        String pp=getPreferenceScreen().getKey();
        String tmp = mScreensTree.get(pp);
        mScreenPositions.put(getPreferenceScreen().getKey(), getListPosition());
        if(tmp!=null){
            PreferenceScreen pstemp = (PreferenceScreen) mGrxScreen.findPreference(tmp);
            if (pstemp!=null){
                String tmp1 = mScreensTree.get(getPreferenceScreen().getKey());
                boolean ni;
                ni= !pstemp.getKey().equals(mGrxScreen.getKey());
                mGrxSettingsActivity.onBackKey(pstemp.getTitle());
                setPreferenceScreen(pstemp);
                setListPosition(mScreenPositions.get(getPreferenceScreen().getKey()));
                mCurrentSubScreen=pstemp.getKey();
                mGrxSettingsActivity.onScreenChange(mCurrentSubScreen);
                vd=false;
            }
        }
        return vd;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        return true;
    }

    public interface onListReady{
        void onListReady(ListView listaprefs);
    }


    public interface onScreenChange{
        void  onScreenChange(String ultima_pantalla);
    }

    public interface onBackKey{
        void onBackKey(CharSequence Subtitle);
    }

    public void updateDividerHeight(int DividerHeight) {
        mDividerHeight=DividerHeight;
        View rootView = getView();
        if (rootView != null) {
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            if (list != null) {
                list.setDividerHeight(DividerHeight);

            }
        }
    }

    /*********** image picker support - this way is easy to re-create the state..********************************/

    public void startImagePicker(Intent intent, int reqcode){
        getActivity().startActivityForResult(intent,reqcode);
    }

    /************  broadcast and group keys support *******************/


    public void changeGroupKey(String groupkey){
        if(Common.SyncUpMode || !isAdded()) return;
        if(groupkey!=null && !groupkey.isEmpty()) {
            GrxPrefsUtils.changePreferenceGroupKeyValue(getActivity(),groupkey);
            if(mIsDemoMode && !Common.SyncUpMode) {
                String groupkeyvalue = String.valueOf(Settings.System.getInt(getActivity().getContentResolver(),groupkey,0));
                Toast.makeText(getActivity(), "GroupKey -> " + groupkey + " = " + groupkeyvalue,Toast.LENGTH_SHORT).show();

            }
        }
    }


    public void sendPreferenceBroadcasts(String bc1, String bc1extra, String bc2, String bc2extra){
        if(bc1!=null && !bc1.isEmpty()) {
            GrxPrefsUtils.sendPreferenceBroadCastWithExtra(getActivity(), bc1, bc1extra,false);
            if(mIsDemoMode && !Common.SyncUpMode) show_toast("broadcast1 = " + bc1);
        }
        if(bc2!=null && !bc2.isEmpty()) {
            GrxPrefsUtils.sendPreferenceBroadCastWithExtra(getActivity(), bc2,bc2extra, true);
            if(mIsDemoMode && !Common.SyncUpMode) show_toast("broadcast2 = " + bc2);
        }
    }

    /*************** Dialog Fragments Listeners and support  *******************/

    public void ShowGrxDatePickerDialog(String key, String value){
        DlgFrGrxDatePicker dlg = DlgFrGrxDatePicker.newInstance(key,value);
        dlg.setOnGrxDateSetListener(this);
        dlg.show(getFragmentManager(),Common.TAG_DLGFRGRDATEPICKER);
    }

    public void onGrxDateSet(String value, String key){
        GrxDatePicker grxDatePicker= (GrxDatePicker) findPreference(key);
        if(grxDatePicker!=null) grxDatePicker.setNewValue(value);
    }

    public void showGrxTimePickerDialog(String key, int value){
        DlgFrGrxTimePicker dlg = DlgFrGrxTimePicker.newInstance(key, value);
        dlg.setOnGrxTimeSetListener(this);
        dlg.show(getFragmentManager(),Common.TAG_DLGFRGRTIMEPICKER);
    }
    public void onGrxTimeSet(int value, String key){
        GrxTimePicker pref = (GrxTimePicker) findPreference(key);
        if(pref!=null) pref.setNewValue(value);
    }

    public Preference findAndGetCallBack(String key){
        return findPreference(key);
    }

    /********** SYNC UP SUPPORT  **************************************************/

    public void addGroupKeyForSyncUp(String groupkey){
        if(groupkey!=null && !groupkey.isEmpty()){
            //   if(isAdded()) show_toast(groupkey);
            Common.GroupKeysList.add(groupkey);
        }
    }

    public void addCommonBroadCastValuesForSyncUp(String extra, String extravalue){
        if(extra==null) return;
        if(Common.CommonBroadCastList==null) Common.CommonBroadCastList = new HashSet<>();
        String entry = extra+";"+extravalue;
        Common.CommonBroadCastList.add(entry);
    }

    public void addBroadCastToSendForSyncUp(String bc1, String bc1extra, String bc2, String bc2extra){
        String entry;
        if(bc1!=null && !bc1.isEmpty()){
            entry=bc1;
            if(bc1extra!=null && !bc1extra.isEmpty()) entry=entry+";"+bc1extra;
            //   if(isAdded()) show_toast(groupkey);
            Common.BroadCastsList.add(entry);
        }

        if(bc2!=null && !bc2.isEmpty()){
            entry=bc2;
            if(bc2extra!=null && !bc2extra.isEmpty()) entry=entry+";"+bc2extra;
            //   if(isAdded()) show_toast(groupkey);
            Common.BroadCastsList.add(entry);
        }
    }

    /***************** CUSTOM DEPENDENCIES *****************************/

    public interface CustomDependencyListener{
        void OnCustomDependencyChange(boolean state);
    }


    public void addCustomDependency(GrxPreferenceScreen.CustomDependencyListener dependencyListener, String rule, String separator){
        if(rule == null || rule.isEmpty()) return;
        List<CustomDependencyHelper> dependencylisteners;
        CustomDependencyHelper customDependencyHelper = new CustomDependencyHelper(dependencyListener, rule);
        String dependency_key = customDependencyHelper.get_custom_dependency_key();
        if(CustomDependencies.containsKey(dependency_key)){
            dependencylisteners = CustomDependencies.get(dependency_key);
        }else dependencylisteners = new ArrayList<>();
        dependencylisteners.add(customDependencyHelper);
        CustomDependencies.put(dependency_key,dependencylisteners);
    }


    private String getStringValueForDependencyKey(int type, String key){

        switch (type){
            case 0: return String.valueOf(Common.sp.getInt(key,-1));
            case 1:
            case 2: return Common.sp.getString(key,"");
            case 3: return Common.sp.getBoolean(key,false) ? "true" : "false";
        }
        return null;
    }

    public void updateAllCustomDependencies(){

        for (Map.Entry<String, List<CustomDependencyHelper>> entry : CustomDependencies.entrySet()) {
            String dependency_key = entry.getKey();
            List<CustomDependencyHelper> dependencyHelpers = entry.getValue();
            String dependency_key_value = getStringValueForDependencyKey(dependencyHelpers.get(0).get_dependency_type(), dependency_key);
            int num_listeners = dependencyHelpers.size();
            for(int i = 0; i< num_listeners;i++){
                CustomDependencyHelper customDependencyHelper = dependencyHelpers.get(i);
                customDependencyHelper.get_listener().OnCustomDependencyChange(customDependencyHelper.listener_should_be_enabled(dependency_key_value));
            }

        }

    }


    private String getStringValueForDependencyKey(int type, Object value){
        switch (type){
            case 0: return String.valueOf((int) value);
            case 1:
            case 2: String tmp = (String) value;
                if(tmp==null) tmp = "";
                return tmp;
            case 3: return (boolean) value ? "true" : "false";
        }
        return null;

    }

    public void updateAllCustomDependencies(String key, Object ob){
        if(key==null || key.isEmpty()) return;
        List<CustomDependencyHelper> dependencyHelpers = CustomDependencies.get(key);
        if(dependencyHelpers==null) return;

        int type = dependencyHelpers.get(0).get_dependency_type();
        String dependency_key_value = getStringValueForDependencyKey(type,ob);
        int num_listeners = dependencyHelpers.size();
        for(int i = 0; i< num_listeners;i++){
            CustomDependencyHelper customDependencyHelper = dependencyHelpers.get(i);
            customDependencyHelper.get_listener().OnCustomDependencyChange(customDependencyHelper.listener_should_be_enabled(dependency_key_value));
        }



    }

    /*************************************************************************/

    public void onImagePickerResult(Intent data, int requestcode){

        String key = data.getStringExtra(Common.TAG_DEST_FRAGMENT_NAME_EXTRA_KEY);
        if(key==null || key.isEmpty()) return;

        GrxPickImage grxPickImage = (GrxPickImage) getPreferenceScreen().findPreference(key);
        if(grxPickImage==null) return;

        switch (requestcode){
            case Common.REQ_CODE_GALLERY_IMAGE_PICKER_JUST_URI:
                grxPickImage.setNewImage(Objects.requireNonNull(data.getData()).toString());

                break;
            case Common.REQ_CODE_GALLERY_IMAGE_PICKER_CROP_CIRCULAR:
                String sFile = data.getStringExtra(GrxImagePicker.S_DIR_IMG);
                File file = new File(sFile);
                Uri uri = Uri.fromFile(file);
                grxPickImage.setNewImage(uri.toString());
                //       Uri uri = PublicFileProvider.getUriForFile(getActivity(),getString(R.string.grx_file_provider_authority),file);
                //     grxPickImage.setNewImage(uri.toString());
                break;
        }
    }


    public void addPreferenceToRemoveList(Preference pref){
        mPrefsToRemove.add(pref);
    }


    private void removePreferences(){
     /*   if(mPrefsToRemove.size()>0) for( Preference preference : mPrefsToRemove) {
            getPreferenceScreen().removePreference(preference);
        }
        mPrefsToRemove.clear();

      */

        if ( mPrefsToRemove.size() > 0 ) {
            for (Preference preference : mPrefsToRemove) {
                PreferenceGroup parent = getParent(getPreferenceScreen(), preference);
                assert parent != null;
                parent.removePreference(preference);
            }
        }
    }

        // Not needed if API is >=26
        private PreferenceGroup getParent(PreferenceGroup groupToSearchIn, Preference preference) {
            for (int i = 0; i < groupToSearchIn.getPreferenceCount(); ++i) {
                Preference child = groupToSearchIn.getPreference(i);

                if (child == preference)
                    return groupToSearchIn;

                if (child instanceof PreferenceGroup) {
                    PreferenceGroup childGroup = (PreferenceGroup)child;
                    PreferenceGroup result = getParent(childGroup, preference);
                    if (result != null)
                        return result;
                }
            }

            return null;
        }
    /*************************** Settings System, Secure and Global Support ****************/

    @Override
    public void observedSettingsKeyChanged(){
        //to do for next version - real time support (in multiwindow) for settings preferences
    }


    public void addSupportForSettingsKey(String key){
        if(key!=null && !key.isEmpty()) mSettingsKeys.add(key);
    }

    private void refreshSettingsKeys(){
        for(String key : mSettingsKeys){
            Preference preference = findPreference(key);
            if(preference!=null){
                switch (preference.getClass().getSimpleName()){
                    case "GrxSwitchPreference":
                    case "GrxFilePreference":
                        GrxSwitchPreference swp = (GrxSwitchPreference) preference;
                        swp.updateFromSettingsValue();
                        break;
                    case "GrxCheckBoxPreference":
                        GrxCheckBoxPreference cbp = (GrxCheckBoxPreference) preference;
                        cbp.updateFromSettingsValue();
                        break;
                    default:
                        if(preference instanceof GrxBasePreference) {
                            GrxBasePreference gbp = (GrxBasePreference) preference;
                            gbp.updateFromSettingsValue();
                        }
                        break;
                }
            }
        }
    }



    /**************    scripts **************************/

    private void runArrayScript(final int scriptarrayid, final String toasttext, boolean confirmation){
        if(confirmation){
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(getString(R.string.grxs_script_dialog_title));
            ab.setMessage(getString(R.string.grxs_script_dialog_msg));
            ab.setPositiveButton(R.string.grxs_ok, (dialogInterface, i) -> {
                boolean result = RootPrivilegedUtils.runScriptArray(getActivity(),scriptarrayid);
                if(result) show_toast(toasttext);
                else show_toast(getString(R.string.grxs_script_array_error));
            });
            ab.create().show();
        }else {
            boolean result = RootPrivilegedUtils.runScriptArray(getActivity(),scriptarrayid);
            if(result) show_toast(toasttext);
            else show_toast(getString(R.string.grxs_script_array_error));
        }
    }


    private void runFileScript(final String scriptfile, final String toasttext, boolean confirmation, final String... scriptargument){

        if(confirmation){
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(getString(R.string.grxs_script_dialog_title));
            ab.setMessage(getString(R.string.grxs_script_file_dialog_msg,scriptfile));
            ab.setPositiveButton(R.string.grxs_ok, (dialogInterface, i) -> {
                boolean result = RootPrivilegedUtils.runFileScript(getActivity(),scriptfile,scriptargument);
                if(result) show_toast(toasttext);
                else show_toast(getString(R.string.grxs_script_array_error));
            });
            ab.create().show();
        }else {
            boolean result = RootPrivilegedUtils.runFileScript(getActivity(),scriptfile,scriptargument);
            if(result) show_toast(toasttext);
            else show_toast(getString(R.string.grxs_script_array_error));
        }
    }


     /************** grouped value support ***********/

    public void addGroupedValueMember(String prefkey, Object defval, PrefAttrsInfo.PREF_TYPE preftype, String groupedValuekey,
                                      String alias, String systemtype, String broadcastaction, String broadcastExtraName){
        if(TextUtils.isEmpty(prefkey) || TextUtils.isEmpty(groupedValuekey)) return;
        mKeysInGroupedValues.put(prefkey,groupedValuekey);
        if(!mGroupedValuesInfo.containsKey(groupedValuekey)) {
            mGroupedValuesInfo.put(groupedValuekey,new GroupedValueInfo(groupedValuekey, getActivity()));
        }
        GroupedValueInfo groupedKeyInfo = mGroupedValuesInfo.get(groupedValuekey);
        groupedKeyInfo.addPreferenceConfiguration(prefkey, defval, preftype,alias,systemtype,broadcastaction, broadcastExtraName);
    }


    public void onGroupedValueButtonPressed(String groupedKey){
        GroupedValueInfo groupedValueInfo = mGroupedValuesInfo.get(groupedKey);
        if(groupedValueInfo!=null){
            groupedValueInfo.onGroupedValueButtonPressed();
        }
    }
    // grouped key


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefKey){
        // shared prefs changes are reported later than onchange pref. On the other way we could not persist in Settings db the value
        // so , i will use shared for this feature.
        if(Common.SyncUpMode) return;

        String groupedKey = mKeysInGroupedValues.get(prefKey);
        if(TextUtils.isEmpty(groupedKey)) return;
        mGroupedValuesInfo.get(groupedKey).updatePreferenceValue(prefKey);
    }

}
