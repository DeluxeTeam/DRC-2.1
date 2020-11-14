
/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */



package com.deluxelabs.drc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;



public class BPRulesUtils {


    public static boolean isBPEnabled(String BPRule){

        if(BPRule==null || BPRule.isEmpty()) return true;

        // Special case for the kernel. If is not deluxe, remove the prefs
        if (BPRule.equals("kernel") || BPRule.equals("kernelWarn")) {
            StringBuilder log = new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec("uname -a");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    log.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String kernel = log.toString();
            boolean isDeluxe = !kernel.isEmpty() && kernel.contains("Deluxe");
            if ( BPRule.equals("kernelWarn") ) { return !isDeluxe; }
            return isDeluxe;
        }


        String[] rule = BPRule.split(Pattern.quote("#"));
        if(rule==null || rule.length!=3  ) return true;

        boolean rule_isenabled = rule[0].toUpperCase().equals("SHOW");
        // String propValue = GrxPrefsUtils.getBPProperty(rule[1]);
        String[] conditions = rule[2].split(Pattern.quote(","));

        // process conditions

        StringBuilder mValuesToCheck= new StringBuilder(",");
        List<String> mValuesToContain = new ArrayList<>();

        for (String value : conditions) {
            if (value.startsWith("(") && value.endsWith(")")) {
                String substring = value.substring(1, value.length() - 1);
                if (substring.contains("NULL")) substring = substring.replace("NULL", "");
                mValuesToContain.add(substring);
            } else mValuesToCheck.append(value).append(",");
        }
        if(!mValuesToCheck.toString().endsWith(",")) mValuesToCheck.append(",");
        if(mValuesToCheck.toString().contains("NULL")) {
            mValuesToCheck = new StringBuilder(mValuesToCheck.toString().replace("NULL", ""));
        }else if(mValuesToCheck.toString().equals(",,")) mValuesToCheck = null;


        //process build prop property

        String value = getBPProperty(rule[1]);


        //String value = RootUtils.getProp("grx.prop");
        boolean matched = false;
        if(mValuesToCheck!=null) {
            String pattern = "," + value + ",";
            matched = mValuesToCheck.toString().contains(pattern);
        }

        if(matched)  return rule_isenabled;

        for (int i = 0; i<mValuesToContain.size();i++){
            String tmp = mValuesToContain.get(i);
            if(value.contains(tmp)){
                return rule_isenabled;
            }
        }
        return !rule_isenabled;


    }


    private static String getBPProperty(String property){
        Process p = null;
        String property_value = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", property).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line=br.readLine()) != null){
                property_value = line;
            }
            p.destroy();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return property_value;
    }


    public static boolean setBPProperty(String property, String value){
        Process p = null;
        try {
            p = new ProcessBuilder("/system/bin/setprop", property, value).redirectErrorStream(true).start();
            p.destroy();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return getBPProperty(property) == value;
    }

}
