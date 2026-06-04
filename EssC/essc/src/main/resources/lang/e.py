import json
from pathlib import Path

INSERT_AFTER = '  "home.list.entry"'

NEW_TRANSLATIONS = {
    "en_us": {
        "home.list.header": "<prefix> <color:#FFFFFF>Your Homes <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Your Homes</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>in <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Your default home <color:#FFFFFF><name></color> does not exist. Use <color:#FFF200>/homes</color> to see your homes.</color>"
    },
    "zh_cn": {
        "home.list.header": "<prefix> <color:#FFFFFF>你的家 <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>你的家</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>位于 <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>你的默认家 <color:#FFFFFF><name></color> 不存在。使用 <color:#FFF200>/homes</color> 查看你的家。</color>"
    },
    "vi_vn": {
        "home.list.header": "<prefix> <color:#FFFFFF>Nhà của bạn <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Nhà của bạn</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>tại <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Nhà mặc định <color:#FFFFFF><name></color> của bạn không tồn tại. Sử dụng <color:#FFF200>/homes</color> để xem các nhà của bạn.</color>"
    },
    "ur_pk": {
        "home.list.header": "<prefix> <color:#FFFFFF>آپ کے گھر <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>آپ کے گھر</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>بمقام <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>آپ کا ڈیفالٹ گھر <color:#FFFFFF><name></color> موجود نہیں ہے۔ اپنے گھر دیکھنے کے لیے <color:#FFF200>/homes</color> استعمال کریں۔</color>"
    },
    "uk_ua": {
        "home.list.header": "<prefix> <color:#FFFFFF>Ваші будинки <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Ваші будинки</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>у <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Ваш домашній будинок за замовчуванням <color:#FFFFFF><name></color> не існує. Використовуйте <color:#FFF200>/homes</color>, щоб переглянути ваші будинки.</color>"
    },
    "tr_tr": {
        "home.list.header": "<prefix> <color:#FFFFFF>Evleriniz <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Evleriniz</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>konumunda <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Varsayılan eviniz <color:#FFFFFF><name></color> mevcut değil. Evlerinizi görmek için <color:#FFF200>/homes</color> komutunu kullanın.</color>"
    },
    "th_th": {
        "home.list.header": "<prefix> <color:#FFFFFF>บ้านของคุณ <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>บ้านของคุณ</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>ใน <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>บ้านเริ่มต้น <color:#FFFFFF><name></color> ของคุณไม่มีอยู่จริง ใช้ <color:#FFF200>/homes</color> เพื่อดูบ้านของคุณ</color>"
    },
    "te_in": {
        "home.list.header": "<prefix> <color:#FFFFFF>మీ ఇళ్లు <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>మీ ఇళ్లు</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>ఇక్కడ <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>మీ డిఫాల్ట్ ఇల్లు <color:#FFFFFF><name></color> ఉనికిలో లేదు. మీ ఇళ్లను చూడటానికి <color:#FFF200>/homes</color> ఉపయోగించండి.</color>"
    },
    "ta_in": {
        "home.list.header": "<prefix> <color:#FFFFFF>உங்கள் வீடுகள் <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>உங்கள் வீடுகள்</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>இங்கு <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>உங்கள் இயல்புநிலை வீடு <color:#FFFFFF><name></color> இல்லை. உங்கள் வீடுகளைப் பார்க்க <color:#FFF200>/homes</color> பயன்படுத்தவும்.</color>"
    },
    "sv_se": {
        "home.list.header": "<prefix> <color:#FFFFFF>Dina hem <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Dina hem</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>i <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Ditt förvalda hem <color:#FFFFFF><name></color> finns inte. Använd <color:#FFF200>/homes</color> för att se dina hem.</color>"
    },
    "ru_ru": {
        "home.list.header": "<prefix> <color:#FFFFFF>Ваши дома <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Ваши дома</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>в <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Ваш дом по умолчанию <color:#FFFFFF><name></color> не существует. Используйте <color:#FFF200>/homes</color>, чтобы посмотреть свои дома.</color>"
    },
    "pt_br": {
        "home.list.header": "<prefix> <color:#FFFFFF>Seus lares <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Seus lares</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>em <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Seu lar padrão <color:#FFFFFF><name></color> não existe. Use <color:#FFF200>/homes</color> para ver seus lares.</color>"
    },
    "pl_pl": {
        "home.list.header": "<prefix> <color:#FFFFFF>Twoje domy <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Twoje domy</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>w <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Twój dom domyślny <color:#FFFFFF><name></color> nie istnieje. Użyj <color:#FFF200>/homes</color>, aby zobaczyć swoje domy.</color>"
    },
    "nl_nl": {
        "home.list.header": "<prefix> <color:#FFFFFF>Jouw huizen <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Jouw huizen</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>in <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Jouw standaard huis <color:#FFFFFF><name></color> bestaat niet. Gebruik <color:#FFF200>/homes</color> om jouw huizen te zien.</color>"
    },
    "mr_in": {
        "home.list.header": "<prefix> <color:#FFFFFF>तुमची घरे <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>तुमची घरे</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>येथे <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>तुमचे डीफॉल्ट घर <color:#FFFFFF><name></color> अस्तित्वात नाही. तुमची घरे पाहण्यासाठी <color:#FFF200>/homes</color> वापरा.</color>"
    },
    "ko_kr": {
        "home.list.header": "<prefix> <color:#FFFFFF>당신의 집 <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>당신의 집</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>위치 <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>기본 집 <color:#FFFFFF><name></color>이(가) 존재하지 않습니다. <color:#FFF200>/homes</color>를 사용하여 집 목록을 확인하세요.</color>"
    },
    "ja_jp": {
        "home.list.header": "<prefix> <color:#FFFFFF>あなたのホーム <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>あなたのホーム</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA><color:#FFFFFF><world></color> にある <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>デフォルトホーム <color:#FFFFFF><name></color> は存在しません。<color:#FFF200>/homes</color> を使用してホームを確認してください。</color>"
    },
    "it_it": {
        "home.list.header": "<prefix> <color:#FFFFFF>Le tue case <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Le tue case</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>nel <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>La tua casa predefinita <color:#FFFFFF><name></color> non esiste. Usa <color:#FFF200>/homes</color> per vedere le tue case.</color>"
    },
    "id_id": {
        "home.list.header": "<prefix> <color:#FFFFFF>Rumah Anda <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Rumah Anda</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>di <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Rumah default Anda <color:#FFFFFF><name></color> tidak ada. Gunakan <color:#FFF200>/homes</color> untuk melihat rumah Anda.</color>"
    },
    "hi_in": {
        "home.list.header": "<prefix> <color:#FFFFFF>आपके घर <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>आपके घर</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>में <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>आपका डिफ़ॉल्ट घर <color:#FFFFFF><name></color> मौजूद नहीं है। अपने घर देखने के लिए <color:#FFF200>/homes</color> का उपयोग करें।</color>"
    },
    "gu_in": {
        "home.list.header": "<prefix> <color:#FFFFFF>તમારા ઘરો <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>તમારા ઘરો</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>માં <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>તમારું ડિફોલ્ટ ઘર <color:#FFFFFF><name></color> અસ્તિત્વમાં નથી. તમારા ઘરો જોવા માટે <color:#FFF200>/homes</color> નો ઉપયોગ કરો.</color>"
    },
    "fr_fr": {
        "home.list.header": "<prefix> <color:#FFFFFF>Vos maisons <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Vos maisons</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>dans <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Votre maison par défaut <color:#FFFFFF><name></color> n'existe pas. Utilisez <color:#FFF200>/homes</color> pour voir vos maisons.</color>"
    },
    "fil_ph": {
        "home.list.header": "<prefix> <color:#FFFFFF>Iyong mga bahay <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Iyong mga bahay</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>sa <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Ang iyong default na bahay <color:#FFFFFF><name></color> ay hindi umiiral. Gamitin ang <color:#FFF200>/homes</color> para makita ang iyong mga bahay.</color>"
    },
    "es_es": {
        "home.list.header": "<prefix> <color:#FFFFFF>Tus casas <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Tus casas</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>en <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Tu casa predeterminada <color:#FFFFFF><name></color> no existe. Usa <color:#FFF200>/homes</color> para ver tus casas.</color>"
    },
    "en_gb": {
        "home.list.header": "<prefix> <color:#FFFFFF>Your Homes <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Your Homes</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>in <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Your default home <color:#FFFFFF><name></color> does not exist. Use <color:#FFF200>/homes</color> to see your homes.</color>"
    },
    "de_de": {
        "home.list.header": "<prefix> <color:#FFFFFF>Deine Zuhause <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Deine Zuhause</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>in <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Dein Standard-Zuhause <color:#FFFFFF><name></color> existiert nicht. Nutze <color:#FFF200>/homes</color>, um deine Zuhause zu sehen.</color>"
    },
    "de_ch": {
        "home.list.header": "<prefix> <color:#FFFFFF>Deine Zuhause <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Deine Zuhause</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>in <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Dein Standard-Zuhause <color:#FFFFFF><name></color> existiert nicht. Nutze <color:#FFF200>/homes</color>, um deine Zuhause zu sehen.</color>"
    },
    "bn_bd": {
        "home.list.header": "<prefix> <color:#FFFFFF>আপনার ঘর <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>আপনার ঘর</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA><color:#FFFFFF><world></color>-এ <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>আপনার ডিফল্ট ঘর <color:#FFFFFF><name></color> বিদ্যমান নেই। আপনার ঘরগুলো দেখতে <color:#FFF200>/homes</color> ব্যবহার করুন।</color>"
    },
    "ar_sa": {
        "home.list.header": "<prefix> <color:#FFFFFF>منازلك <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>منازلك</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>في <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>منزلك الافتراضي <color:#FFFFFF><name></color> غير موجود. استخدم <color:#FFF200>/homes</color> لعرض منازلك.</color>"
    },
    "ar_eg": {
        "home.list.header": "<prefix> <color:#FFFFFF>منازلك <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>منازلك</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>في <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>منزلك الافتراضي <color:#FFFFFF><name></color> غير موجود. استخدم <color:#FFF200>/homes</color> لعرض منازلك.</color>"
    },
    "lb_lu": {
        "home.list.header": "<prefix> <color:#FFFFFF>Är Heemechten <color:#AAAAAA>(<color:#FFF200><used></color>/<color:#FFF200><limit></color>):</color></color>",
        "home.list.entries": "<color:#AAAAAA>  <color:#FFFFFF><homes></color></color>",
        "homes.list.header": "<prefix> <color:#FFF200><b>Är Heemechten</b></color> <color:#AAAAAA>[<color:#FFF200><used></color>/<color:#FFF200><limit></color>]</color>",
        "homes.list.separator": "<color:#666666>  ═══════════════════════</color>",
        "homes.list.entry": "<color:#AAAAAA>  <color:#FFF200>●</color> <click:run_command:/home <name>><color:#FFFFFF><name></color></click> <color:#AAAAAA>an <color:#FFFFFF><world></color> <color:#666666>(<color:#AAAAAA><x></color>, <color:#AAAAAA><y></color>, <color:#AAAAAA><z></color>)</color></color>",
        "homes.list.footer": "<color:#666666>  ═══════════════════════</color>",
        "home.teleport.default_not_found": "<prefix> <color:#FF0000>Ärt Standard-Heem <color:#FFFFFF><name></color> existéiert net. Benotzt <color:#FFF200>/homes</color> fir är Heemechten ze gesinn.</color>"
    }
}

current_folder = Path(".")

for lang_code, translations in NEW_TRANSLATIONS.items():

    file_path = current_folder / f"{lang_code}.json"

    if not file_path.exists():
        print(f"[SKIPPED] {file_path.name} not found")
        continue

    with open(file_path, "r", encoding="utf-8") as file:
        lines = file.readlines()

    insert_index = None

    for i, line in enumerate(lines):
        if INSERT_AFTER in line:
            insert_index = i + 1
            break

    if insert_index is None:
        print(f"[SKIPPED] Key not found in {file_path.name}")
        continue

    existing_content = "".join(lines)

    new_lines = []

    for key, value in translations.items():

        # Check if key already exists - if so, remove the old line
        if f'"{key}"' in existing_content:
            print(f"[OVERWRITE] {key} already exists in {file_path.name}, replacing...")
            # Find and remove the old line containing this key
            lines = [line for line in lines if f'"{key}"' not in line]
            # After removal, we need to re-find insert position
            for j, line in enumerate(lines):
                if INSERT_AFTER in line:
                    insert_index = j + 1
                    break

        escaped_value = value.replace('"', '\\"')

        new_lines.append(
            f'  "{key}": "{escaped_value}",\n'
        )

    if not new_lines:
        print(f"[NO CHANGES] {file_path.name}")
        continue

    lines[insert_index:insert_index] = new_lines

    with open(file_path, "w", encoding="utf-8") as file:
        file.writelines(lines)

    print(f"[UPDATED] {file_path.name}")

print("\nDone.")