import json
from pathlib import Path

INSERT_AFTER = '  "back.success"'  # Change this to an existing key in your target files

NEW_TRANSLATIONS = {
    "en_us": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Teleported to your unsafe death location.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Teleported to your unsafe location.</color>"
    },
    "zh_cn": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>已传送至你不安全的死亡点。</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>已传送至你不安全的位置。</color>"
    },
    "vi_vn": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Đã dịch chuyển đến vị trí tử vong không an toàn của bạn.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Đã dịch chuyển đến vị trí không an toàn của bạn.</color>"
    },
    "ur_pk": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>آپ کی غیر محفوظ موت کے مقام پر ٹیلی پورٹ کیا گیا۔</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>آپ کی غیر محفوظ جگہ پر ٹیلی پورٹ کیا گیا۔</color>"
    },
    "uk_ua": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Телепортовано до вашої небезпечної точки смерті.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Телепортовано до вашого небезпечного місця.</color>"
    },
    "tr_tr": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Güvensiz ölüm noktana ışınlandın.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Güvensiz konumuna ışınlandın.</color>"
    },
    "th_th": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>วาร์ปไปยังจุดตายที่ไม่ปลอดภัยของคุณแล้ว</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>วาร์ปไปยังตำแหน่งที่ไม่ปลอดภัยของคุณแล้ว</color>"
    },
    "te_in": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>మీ అసురక్షిత మరణం జరిగిన ప్రదేశానికి టెలిపోర్ట్ చేయబడ్డారు.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>మీ అసురక్షిత ప్రదేశానికి టెలిపోర్ట్ చేయబడ్డారు.</color>"
    },
    "ta_in": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>உங்கள் பாதுகாப்பற்ற இறப்பு இடத்திற்கு டெலிபோர்ட் செய்யப்பட்டது.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>உங்கள் பாதுகாப்பற்ற இடத்திற்கு டெலிபோர்ட் செய்யப்பட்டது.</color>"
    },
    "sv_se": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Teleporterad till din osäkra dödspunkt.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Teleporterad till din osäkra plats.</color>"
    },
    "ru_ru": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Телепортирован в ваше небезопасное место смерти.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Телепортирован в ваше небезопасное место.</color>"
    },
    "pt_br": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Teletransportado para seu local de morte inseguro.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Teletransportado para seu local inseguro.</color>"
    },
    "pl_pl": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Przeteleportowano do niebezpiecznego punktu śmierci.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Przeteleportowano do niebezpiecznego miejsca.</color>"
    },
    "nl_nl": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Geteleporteerd naar je onveilige sterfpunt.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Geteleporteerd naar je onveilige locatie.</color>"
    },
    "mr_in": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>तुमच्या असुरक्षित मृत्यूच्या ठिकाणी टेलिपोर्ट केले.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>तुमच्या असुरक्षित ठिकाणी टेलिपोर्ट केले.</color>"
    },
    "ko_kr": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>안전하지 않은 사망 지점으로 텔레포트했습니다.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>안전하지 않은 위치로 텔레포트했습니다.</color>"
    },
    "ja_jp": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>安全でない死亡地点にテレポートしました。</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>安全でない場所にテレポートしました。</color>"
    },
    "it_it": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Teletrasportato nel tuo punto di morte non sicuro.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Teletrasportato nella tua posizione non sicura.</color>"
    },
    "id_id": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Diteleportasikan ke titik kematian Anda yang tidak aman.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Diteleportasikan ke lokasi Anda yang tidak aman.</color>"
    },
    "hi_in": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>आपकी असुरक्षित मृत्यु के स्थान पर टेलीपोर्ट किया गया।</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>आपके असुरक्षित स्थान पर टेलीपोर्ट किया गया।</color>"
    },
    "gu_in": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>તમારા અસુરક્ષિત મૃત્યુના સ્થાન પર ટેલિપોર્ટ કરવામાં આવ્યું.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>તમારા અસુરક્ષિત સ્થાન પર ટેલિપોર્ટ કરવામાં આવ્યું.</color>"
    },
    "fr_fr": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Téléporté vers votre point de mort dangereux.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Téléporté vers votre endroit dangereux.</color>"
    },
    "fil_ph": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Na-teleport sa iyong hindi ligtas na lugar ng pagkamatay.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Na-teleport sa iyong hindi ligtas na lokasyon.</color>"
    },
    "es_es": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Teletransportado a tu punto de muerte no seguro.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Teletransportado a tu ubicación no segura.</color>"
    },
    "en_gb": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Teleported to your unsafe death location.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Teleported to your unsafe location.</color>"
    },
    "de_de": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Zu deinem unsicheren Todespunkt teleportiert.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Zu deinem unsicheren Ort teleportiert.</color>"
    },
    "de_ch": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Zu deinem unsicheren Todespunkt teleportiert.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Zu deinem unsicheren Ort teleportiert.</color>"
    },
    "bn_bd": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>আপনার অনিরাপদ মৃত্যুর স্থানে টেলিপোর্ট করা হয়েছে।</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>আপনার অনিরাপদ স্থানে টেলিপোর্ট করা হয়েছে।</color>"
    },
    "ar_sa": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>تم الانتقال إلى موقع موتك غير الآمن.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>تم الانتقال إلى موقعك غير الآمن.</color>"
    },
    "ar_eg": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>تم الانتقال إلى موقع موتك غير الآمن.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>تم الانتقال إلى موقعك غير الآمن.</color>"
    },
    "lb_lu": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Op dengen onséchere Doudespunkt teleportéiert.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Op deng onsécher Plaz teleportéiert.</color>"
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

        escaped_value = value.replace('"', '\"')

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