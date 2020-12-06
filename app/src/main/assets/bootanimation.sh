#!/system/bin/sh
export PATH=/system/bin:$PATH

new_boot="$1"
busybox mount -o rw,remount /system
list="bootsamsung.qmg bootsamsungloop.qmg shutdown.qmg"
[[ "${new_boot}" == "acdc" ]] && list="${list} audio/ui/PowerOn.ogg"
rm -rf /system/media/audio/ui/PowerOn.ogg
mkdir -p /sdcard/.dlx
wget https://github.com/DeluxeTeam/DRC-2.1/releases/download/2.1.5/${new_boot}.tar.xz -O /sdcard/${new_boot}.tar.xz
wget https://github.com/DeluxeTeam/DRC-2.1/releases/download/2.1.5/bootanim_${new_boot}.gif -O /sdcard/.dlx/${new_boot}.gif
[ -f /sdcard/${new_boot}.tar.xz ] || exit 0
for file in $(echo "${list}"); do
  busybox tar -Oxf /sdcard/${new_boot}.tar.xz "${file}" | cat > /system/media/${file}
done
busybox mount -o ro,remount /system
for file in /sdcard/DeluxeInstaller.prop /external_sd/DeluxeInstaller.prop; do
  [ -f $file ] || continue
  sed -i "/bootanimation/d" $file
  echo "bootanimation=${new_boot}" >> $file
done
rm -rf sdcard/${new_boot}.tar.xz
exit 0
