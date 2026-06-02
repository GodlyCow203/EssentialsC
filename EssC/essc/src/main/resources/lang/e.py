import json
from pathlib import Path

INSERT_AFTER = '  "enchant.invalid_level"'

NEW_TRANSLATIONS = {
    "en_us": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>The maximum level for <color:#FFFFFF><enchantment></color> is <color:#FFFFFF><level></color>.</color>"
    },
    "zh_cn": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> 的最大等级是 <color:#FFFFFF><level></color>。</color>"
    },
    "vi_vn": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Cấp độ tối đa cho <color:#FFFFFF><enchantment></color> là <color:#FFFFFF><level></color>.</color>"
    },
    "ur_pk": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> کے لیے زیادہ سے زیادہ لیول <color:#FFFFFF><level></color> ہے۔</color>"
    },
    "uk_ua": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Максимальний рівень для <color:#FFFFFF><enchantment></color> — <color:#FFFFFF><level></color>.</color>"
    },
    "tr_tr": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> için maksimum seviye <color:#FFFFFF><level></color>'dir.</color>"
    },
    "th_th": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>เลเวลสูงสุดสำหรับ <color:#FFFFFF><enchantment></color> คือ <color:#FFFFFF><level></color></color>"
    },
    "te_in": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> కోసం గరిష్ట స్థాయి <color:#FFFFFF><level></color>.</color>"
    },
    "ta_in": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> க்கான அதிகபட்ச நிலை <color:#FFFFFF><level></color>.</color>"
    },
    "sv_se": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Maximal nivå för <color:#FFFFFF><enchantment></color> är <color:#FFFFFF><level></color>.</color>"
    },
    "ru_ru": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Максимальный уровень для <color:#FFFFFF><enchantment></color> — <color:#FFFFFF><level></color>.</color>"
    },
    "pt_br": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>O nível máximo para <color:#FFFFFF><enchantment></color> é <color:#FFFFFF><level></color>.</color>"
    },
    "pl_pl": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Maksymalny poziom dla <color:#FFFFFF><enchantment></color> to <color:#FFFFFF><level></color>.</color>"
    },
    "nl_nl": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Het maximale niveau voor <color:#FFFFFF><enchantment></color> is <color:#FFFFFF><level></color>.</color>"
    },
    "mr_in": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> साठी कमाल पातळी <color:#FFFFFF><level></color> आहे.</color>"
    },
    "ko_kr": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color>의 최대 레벨은 <color:#FFFFFF><level></color>입니다.</color>"
    },
    "ja_jp": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> の最大レベルは <color:#FFFFFF><level></color> です。</color>"
    },
    "it_it": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Il livello massimo per <color:#FFFFFF><enchantment></color> è <color:#FFFFFF><level></color>.</color>"
    },
    "id_id": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Level maksimal untuk <color:#FFFFFF><enchantment></color> adalah <color:#FFFFFF><level></color>.</color>"
    },
    "hi_in": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> के लिए अधिकतम स्तर <color:#FFFFFF><level></color> है।</color>"
    },
    "gu_in": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color> માટે મહત્તમ સ્તર <color:#FFFFFF><level></color> છે.</color>"
    },
    "fr_fr": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Le niveau maximum pour <color:#FFFFFF><enchantment></color> est <color:#FFFFFF><level></color>.</color>"
    },
    "fil_ph": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Ang maximum level para sa <color:#FFFFFF><enchantment></color> ay <color:#FFFFFF><level></color>.</color>"
    },
    "es_es": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>El nivel máximo para <color:#FFFFFF><enchantment></color> es <color:#FFFFFF><level></color>.</color>"
    },
    "en_gb": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>The maximum level for <color:#FFFFFF><enchantment></color> is <color:#FFFFFF><level></color>.</color>"
    },
    "de_de": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Das maximale Level für <color:#FFFFFF><enchantment></color> ist <color:#FFFFFF><level></color>.</color>"
    },
    "de_ch": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>S maximale Level für <color:#FFFFFF><enchantment></color> isch <color:#FFFFFF><level></color>.</color>"
    },
    "bn_bd": {
        "enchant.level_too_high": "<prefix> <color:#FF0000><color:#FFFFFF><enchantment></color>-এর সর্বোচ্চ লেভেল হলো <color:#FFFFFF><level></color>।</color>"
    },
    "ar_sa": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>المستوى الأقصى لـ <color:#FFFFFF><enchantment></color> هو <color:#FFFFFF><level></color>.</color>"
    },
    "ar_eg": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>أقصى مستوى لـ <color:#FFFFFF><enchantment></color> هو <color:#FFFFFF><level></color>.</color>"
    },
    "lb_lu": {
        "enchant.level_too_high": "<prefix> <color:#FF0000>Dat maximalt Level fir <color:#FFFFFF><enchantment></color> ass <color:#FFFFFF><level></color>.</color>"
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

        if f'"{key}"' in existing_content:
            print(f"[EXISTS] {key} already exists in {file_path.name}")
            continue

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