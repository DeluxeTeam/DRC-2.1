package com.deluxelabs.drc.utils;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.deluxelabs.drc.R;
import com.root.RootUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static android.content.Context.MODE_PRIVATE;

/** Kernel controls, @abrahamgcc on 11/10/2020
 ** Using both busybox and native echo to handle some weird scenarios where value doesn't get saved
 **/

public class KernelUtils extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && !action.isEmpty() && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            new Handler().postDelayed(() -> dlxApplyValues(context, "boot"), 500);
        }
    }

    public static String getKernelName() {
        StringBuilder log = new StringBuilder();
        try {
            java.lang.Process process = Runtime.getRuntime().exec("uname -a");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log.toString();
    }

    public static void dlxApplyValues(Context context, String arg) {
        final String kernel = getKernelName();
        if (!kernel.isEmpty() && kernel.contains("Deluxe")) {
            if (!RootPrivilegedUtils.getIsDeviceRooted()) {
                Toast.makeText(context, R.string.grxs_app_not_rooted, Toast.LENGTH_LONG).show();
            } else {
                if (Common.sp == null) {
                    final String pm = context.getPackageName();
                    // Create SharedPreferences on boot_completed, else we'll get FC while getting prefs
                    try {
                        Common.sp = context.createPackageContext(pm, CONTEXT_IGNORE_SECURITY).getSharedPreferences(pm + "_preferences", MODE_PRIVATE);
                    } catch (PackageManager.NameNotFoundException ignored) {
                        Log.d("DLX", "CANNOT CREATE DRC SharedPreferences");
                        return;
                    }
                }
                switch (arg) {
                    case "boot":
                        bootCompleted(context);
                        break;
                    case "dt2w":
                        setDT2W(context);
                        break;
                    case "s2s":
                        setS2S();
                        break;
                    case "s2w":
                        setS2W();
                        break;
                    case "gesture_vibration":
                        setGestureVibration();
                        break;
                    case "governors":
                        setGovernors();
                        break;
                    case "schedulers":
                        setSchedulers();
                        break;
                    case "selinux":
                        setSELinux();
                        break;
                    case "crc":
                        setCRC();
                        break;
                    case "led":
                        setLed();
                        break;
                }
            }
        }
    }

    private static void bootCompleted(Context context) {
        Log.d("DLX", "SETTING KERNEL VALUES (ON-BOOT)");
        setSELinux();
        setDT2W(context);
        setS2S();
        setS2W();
        setGestureVibration();
        setGovernors();
        setSchedulers();
        setCRC();
        setLed();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private static void setLed() {
        final String led = Common.sp.getBoolean("dlx_kernel_led_ramp", true) ? "1" : "0";
        RootUtils.runCommand("busybox echo " + led + " > /sys/class/sec/led/led_fade");
        RootUtils.runCommand("echo " + led + " > /sys/class/sec/led/led_fade");

        final int in = Common.sp.getInt("dlx_kernel_led_in", 800);
        RootUtils.runCommand("busybox echo " + in + " > /sys/class/sec/led/led_fade_time_up");
        RootUtils.runCommand("echo " + in + " > /sys/class/sec/led/led_fade_time_up");

        final int out = Common.sp.getInt("dlx_kernel_led_out", 800);
        RootUtils.runCommand("busybox echo " + out + " > /sys/class/sec/led/led_fade_time_down");
        RootUtils.runCommand("echo " + out + " > /sys/class/sec/led/led_fade_time_down");
    }

    private static void setCRC() {
        final String crc = Common.sp.getBoolean("dlx_kernel_crc", false) ? "Y" : "N";
        RootUtils.runCommand("busybox echo " + crc + " > /sys/module/mmc_core/parameters/use_spi_crc");
        RootUtils.runCommand("echo " + crc + " > /sys/module/mmc_core/parameters/use_spi_crc");
    }

    private static void setDT2W(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        final int dt2w = Common.sp.getBoolean("dlx_kernel_dt2w", false) ? 1 : 0;
        RootUtils.runCommand("busybox echo " + dt2w + " > /sys/android_touch/doubletap2wake");
        RootUtils.runCommand("echo " + dt2w + " > /sys/android_touch/doubletap2wake");
        // Warning when aod is tape to show and dt2w is enabled
        if (dt2w == 1 && Settings.System.getInt(contentResolver, "aod_mode", 0) == 1 && Settings.System.getInt(contentResolver, "aod_tap_to_show_mode", 0) == 1) {
            Toast.makeText(context, R.string.aod_wake, Toast.LENGTH_LONG).show();
        }
    }

    private static void setS2S() {
        final String s2s = Common.sp.getString("dlx_kernel_s2s", "0");
        RootUtils.runCommand("busybox echo " + s2s + " > /sys/android_touch/sweep2sleep");
        RootUtils.runCommand("echo " + s2s + " > /sys/android_touch/sweep2sleep");
    }

    private static void setS2W() {
        final int s2w = dlxGenerateValue(Common.sp.getString("dlx_kernel_s2w", "0,"));
        RootUtils.runCommand("busybox echo " + s2w + " > /sys/android_touch/sweep2wake");
        RootUtils.runCommand("echo " + s2w + " > /sys/android_touch/sweep2wake");
    }

    private static void setGestureVibration() {
        final int vibr = Common.sp.getInt("dlx_kernel_gest_vibration", 0);
        RootUtils.runCommand("busybox echo " + vibr + " > /sys/android_touch/vib_strength");
        RootUtils.runCommand("echo " + vibr + " > /sys/android_touch/vib_strength");
    }

    private static void setGovernors() {
        String all = Common.sp.getString("dlx_kernel_governors", "big;interactive|little;interactive|");
        if ( all.isEmpty() || !all.contains("big") || !all.contains("little") ) { all = "big;interactive|little;interactive|"; }
        final String[] main = all.split(Pattern.quote("|"));
        String big = main[0].split(";")[1];
        String path = "/sys/devices/system/cpu/cpufreq/policy4/scaling_governor";
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo " + big + " > " + path + "; chmod 444 " + path + ";");
        String little = main[1].split(";")[1];
        path = "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor";
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo " + little + " > " + path + "; chmod 444 " + path + ";");
    }

    private static void setSchedulers() {
        String path = "/sys/block/sda/queue/scheduler";
        String sch = Common.sp.getString("dlx_kernel_schedulers", "internal;cfq|external;cfq|");
        if ( sch.isEmpty() || !sch.contains("internal") ) { sch = "internal;cfq|external;cfq|"; }
        final String[] main = sch.split(Pattern.quote("|"));
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo '" + main[0].split(";")[1] + "' > " + path + "; chmod 644 " + path + ";");
        RootUtils.runCommand("chmod 744 " + path + "; echo '" + main[0].split(";")[1] + "' > " + path + "; chmod 644 " + path + ";");
        path = path.replace("sda", "mmcblk0");
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo '" + main[1].split(";")[1] + "' > " + path + "; chmod 644 " + path + ";");
        RootUtils.runCommand("chmod 744 " + path + "; echo '" + main[1].split(";")[1] + "' > " + path + "; chmod 644 " + path + ";");
    }

    private static void setSELinux() {
        final String path = "/sys/fs/selinux/enforce";
        final int selinux = Common.sp.getBoolean("dlx_kernel_selinux", false) ? 1 : 0;
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo " + selinux + " > " + path + "; chmod 644 " + path + ";");
        RootUtils.runCommand("chmod 744 " + path + "; echo " + selinux + " > " + path + "; chmod 644 " + path + ";");
    }

    private static int dlxGenerateValue(String i) {
        int x = 0;
        if ( i == null || i.isEmpty() ) { i = "0,"; }
        for ( String d : i.split(Pattern.quote(",")) ) {
            x += Integer.parseInt(d);
        }
        return x;
    }

}
