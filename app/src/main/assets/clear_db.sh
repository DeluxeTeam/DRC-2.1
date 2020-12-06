#!/system/bin/sh
export PATH=/system/bin:$PATH

for db in system secure global; do
    list=$(settings list $db | grep -E "navbar|dlx|kg_|b_|ls_|qp_|qs_|sb_|ts_|vbutton" | cut -d= -f1)
    echo $list | tr -d "\"" | tr " " "\\n" | while read line; do settings delete $db $line; done
    for key in icon_blacklist heads_up_notifications_enabled navigationbar_color; do
        settings delete $db $key
    done;
done;
exit 0