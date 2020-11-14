
/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */


package com.deluxelabs.drc.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;

import com.deluxelabs.drc.prefssupport.PrefAttrsInfo;




/**** to do -> observers for settings system, secure and global keys to get real time behaviour in multiwindows roms */


public class GrxObserver extends ContentObserver {

        final Context mContext;
        final GrxObserver.OnObservedSettingsKeyChange mCallBack;

        public interface OnObservedSettingsKeyChange{
            void observedSettingsKeyChanged();
        }

        public GrxObserver(android.os.Handler handler, Context context, GrxObserver.OnObservedSettingsKeyChange callback) {
            super(handler);
            mContext= context;
            mCallBack = callback;
        }



        public void addObservedSettingsKey(PrefAttrsInfo.SETTINGS_PREF_TYPE type, String key){
            Uri uri;
            switch (type){
                case SYSTEM:
                    uri=Settings.System.getUriFor(key);
                    break;
                case SECURE:
                    uri=Settings.Secure.getUriFor(key);
                    break;
                case GLOBAL:
                    uri=Settings.Global.getUriFor(key);
                    break;
                 default:
                     return;
            }

            mContext.getContentResolver().registerContentObserver(uri,false,this);
        }


        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            String key = uri.getEncodedPath();
            assert key != null;
            key = key.replace(Common.SETTINGS_PREF_SYSTEM_CLEAN_fROM_PATH,"");
            key = key.replace(Common.SETTINGS_PREF_SECURE_CLEAN_fROM_PATH,"");
            key = key.replace(Common.SETTINGS_PREF_GLOBAL_CLEAN_fROM_PATH,"");

            if(mCallBack!=null) mCallBack.observedSettingsKeyChanged();
        }


        private static class UriInfo{

            public UriInfo(){

            }

        }


    }
