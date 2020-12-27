#!/system/bin/sh
export PATH=/system/bin:$PATH

[ -d /storage/emulated/0/bug_report ] && rm -rf /storage/emulated/0/bug_report
mkdir -p /storage/emulated/0/bug_report
file="/sdcard/bug_report/info"
echo -e "DeluxeROM® - Bug report\n\n" > $file

[ -f /system/lib/libdlx.so ] && lib=1 || lib=0
[ -f /system/priv-app/DRC/DRC.apk ] && drc=1 || drc=0
[ -f /system/vendor/overlay/framework-res__auto_generated_rro.apk ] && overlay=1 || overlay=0
[ -f /system/xbin/busybox ] && busybox=1 || busybox=0
[ -d /system/priv-app/AppLock ] && applock=1 || applock=0
version="$(getprop ro.deluxerom.version)"
build_date="$(getprop ro.deluxerom.date)"
device="$(getprop ro.deluxerom.device)"
csc="$(cat /system/omc/sales_code.dat)"
kernel="$(uname -a)"
selinux="$(getenforce)"
magisk="$(magisk -c)"
timeout 25s logcat *:V > /sdcard/bug_report/logcat

echo "version=${version}" >> $file
echo "device=${device}" >> $file
echo "build=${build_date}" >> $file
echo "kernel=${kernel}" >> $file
echo "selinux=${selinux}" >> $file
echo "csc=${csc}" >> $file
echo "lib=${lib}" >> $file
echo "drc=${drc}" >> $file
echo "overlay=${overlay}" >> $file
echo "busybox=${busybox}" >> $file
echo "magisk=${magisk}" >> $file
echo "applock=${applock}" >> $file
echo "ui_hash=$(md5sum /system/priv-app/SystemUI/SystemUI.apk)" >> $file
echo "drc_hash=$(md5sum /system/priv-app/DRC/DRC.apk)" >> $file

[ -d /system/omc/${csc} ] && cat /system/omc/${csc}/cscfeature.xml > /storage/emulated/0/bug_report/csc
cat /system/etc/floating_feature.xml > /storage/emulated/0/bug_report/floating
cat /system/build.prop > /storage/emulated/0/bug_report/sysprop
[ -f /storage/emulated/0/deluxe_installer.log ] && cat /storage/emulated/0/deluxe_installer.log > /storage/emulated/0/bug_report/aroma
[ -f /storage/emulated/0/deluxe_recovery.log ] && cat /storage/emulated/0/deluxe_recovery.log > /storage/emulated/0/bug_report/recovery
[ -f /storage/emulated/0/DeluxeInstaller.prop ] && cat /storage/emulated/0/DeluxeInstaller.prop > /storage/emulated/0/bug_report/prop

cd /storage/emulated/0/bug_report
tar -czvf bug_report.tar.gz *
mv -f bug_report.tar.gz ../bug_report.tar.gz || cp -rf bug_report.tar.gz ../bug_report.tar.gz
cd ../
rm -rf bug_report
exit 0
