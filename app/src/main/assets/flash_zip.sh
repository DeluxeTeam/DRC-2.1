#!/system/bin/sh
export PATH=/system/bin:$PATH

[ -f /sdcard/dlxtmpzip.zip ] || exit 0
echo -e "boot-recovery
--update-package=/sdcard/0/Download/dlxtmpzip.zip
reboot system" > /cache/recovery/command
reboot recovery
