
/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */


package com.deluxelabs.drc.prefssupport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;



public class OnClickRuleHelper {


    private static boolean checkIfRuleMatchesForStringAndIntPrefs(String rule, String currentvalue){
        boolean matched = false;

        String[] conditions = rule.split(Pattern.quote(","));
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

        if(mValuesToCheck!=null) {
            String pattern = "," + currentvalue + ",";
            matched = mValuesToCheck.toString().contains(pattern);
        }

        if(!matched){
            for (int i = 0; i<mValuesToContain.size();i++){
                String tmp = mValuesToContain.get(i);
                if(currentvalue.contains(tmp)){
                    matched = true;
                    break;
                }
            }
        }
        return matched;
    }



    private static String getKeyToClick(String xmlrule, String currentvalue){
        String key;
        String rule;
         if(xmlrule!=null && !xmlrule.isEmpty()){
            String[] array = xmlrule.split(Pattern.quote("#"));
            if(array.length != 3) return null;
            key = array[1];
            rule=array[0];

            if(checkIfRuleMatchesForStringAndIntPrefs(rule,currentvalue)) return key;
            else return null;

        }
        return null;
    }

    public static String getKeyToClickFromRules(String xmlrules, String currentvalue){

        if(xmlrules==null || xmlrules.isEmpty()) return null;

        if(xmlrules.contains("##")){
            String[] rules = xmlrules.split(Pattern.quote("##"));
            for(String rule  : rules){
                String key = getKeyToClick(rule,currentvalue);
                if(key!=null) return key;
            }


         }else return getKeyToClick(xmlrules,currentvalue);

        return null;
    }

    public static boolean shouldPerformOnClickForBooleanPref(boolean currval, String rule){

        if(rule.toUpperCase().equals("ALWAYS")) return true;
        String currentval = (currval) ? "TRUE" : "FALSE";
        return rule.toUpperCase().equals(currentval);

    }

    public static boolean shouldPerformOnClickForIntPref(int value, String rule){

        if(rule.toUpperCase().equals("ALWAYS")) return true;

        else return checkIfRuleMatchesForStringAndIntPrefs(rule,String.valueOf(value));
    }

    public static boolean shouldPerformOnClickForStringPref(String value, String rule){
        if(rule.toUpperCase().equals("ALWAYS")) return true;
        else return checkIfRuleMatchesForStringAndIntPrefs(rule,value);

    }

}
