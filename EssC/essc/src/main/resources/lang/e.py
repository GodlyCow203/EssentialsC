import json
from pathlib import Path

INSERT_AFTER = '  "kit.gui.item.kit.lore.description"'  # Change this to an existing key in your target files

NEW_TRANSLATIONS = {
    "en_us": {
        "smite.success": "<prefix> <color:#FFF200>Struck <color:#FFFFFF><target></color> with lightning.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>You cannot smite yourself.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>That player is exempt from /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Usage: /smite <player></color>"
    },
    "zh_cn": {
        "smite.success": "<prefix> <color:#FFF200>已用闪电击中 <color:#FFFFFF><target></color>。</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>你不能惩戒你自己。</color>",
        "smite.exempt": "<prefix> <color:#FF0000>该玩家免疫 /smite。</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>用法: /smite <玩家></color>"
    },
    "vi_vn": {
        "smite.success": "<prefix> <color:#FFF200>Đã đánh <color:#FFFFFF><target></color> bằng sét.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Bạn không thể tự trừng phạt chính mình.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Người chơi đó được miễn trừ khỏi /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Cách dùng: /smite <người_chơi></color>"
    },
    "ur_pk": {
        "smite.success": "<prefix> <color:#FFF200>بجلی گرا کر <color:#FFFFFF><target></color> کو نشانہ بنایا۔</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>آپ خود کو نہیں مار سکتے۔</color>",
        "smite.exempt": "<prefix> <color:#FF0000>وہ کھلاڑی /smite سے مستثنیٰ ہے۔</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>استعمال: /smite <کھلاڑی></color>"
    },
    "uk_ua": {
        "smite.success": "<prefix> <color:#FFF200>Вразив <color:#FFFFFF><target></color> блискавкою.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Ви не можете вдарити себе блискавкою.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Цей гравець захищений від /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Використання: /smite <гравець></color>"
    },
    "tr_tr": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> kişisini şimşekle vurdun.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Kendini cezalandıramazsın.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Bu oyuncu /smite komutundan muaftır.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Kullanım: /smite <oyuncu></color>"
    },
    "th_th": {
        "smite.success": "<prefix> <color:#FFF200>สายฟ้าฟาดใส่ <color:#FFFFFF><target></color> แล้ว</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>คุณไม่สามารถลงโทษตัวเองได้</color>",
        "smite.exempt": "<prefix> <color:#FF0000>ผู้เล่นนี้ได้รับยกเว้นจากการใช้ /smite</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>วิธีใช้: /smite <ผู้เล่น></color>"
    },
    "te_in": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> ని మెరుపుతో కొట్టారు.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>మీరు మిమ్మల్ని మీరు కొట్టుకోలేరు.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>ఆ ఆటగాడు /smite నుండి మినహాయించబడ్డారు.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>విధానం: /smite <ఆటగాడు></color>"
    },
    "ta_in": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> மின்னல் தாக்கியது.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>நீங்கள் உங்களையே தண்டிக்க முடியாது.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>அந்த வீரர் /smite இலிருந்து விலக்கப்பட்டுள்ளார்.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>பயன்பாடு: /smite <வீரர்></color>"
    },
    "sv_se": {
        "smite.success": "<prefix> <color:#FFF200>Träffade <color:#FFFFFF><target></color> med blixten.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Du kan inte slå dig själv.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Den spelaren är undantagen från /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Användning: /smite <spelare></color>"
    },
    "ru_ru": {
        "smite.success": "<prefix> <color:#FFF200>Ударил <color:#FFFFFF><target></color> молнией.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Вы не можете ударить самого себя.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Этот игрок защищен от /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Использование: /smite <игрок></color>"
    },
    "pt_br": {
        "smite.success": "<prefix> <color:#FFF200>Atingiu <color:#FFFFFF><target></color> com um raio.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Você não pode castigar a si mesmo.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Esse jogador está isento do /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Uso: /smite <jogador></color>"
    },
    "pl_pl": {
        "smite.success": "<prefix> <color:#FFF200>Uderzono <color:#FFFFFF><target></color> piorunem.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Nie możesz uderzyć samego siebie.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Ten gracz jest zwolniony z /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Użycie: /smite <gracz></color>"
    },
    "nl_nl": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> geraakt door bliksem.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Je kunt jezelf niet slaan.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Die speler is vrijgesteld van /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Gebruik: /smite <speler></color>"
    },
    "mr_in": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> ला विजेने मारले.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>तुम्ही स्वतःला मारू शकत नाही.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>तो खेळाडू /smite पासून मुक्त आहे.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>वापर: /smite <खेळाडू></color>"
    },
    "ko_kr": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color>에게 벼락을 내렸습니다.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>자기 자신은 벼락을 내릴 수 없습니다.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>해당 플레이어는 /smite 대상에서 제외되었습니다.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>사용법: /smite <플레이어></color>"
    },
    "ja_jp": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> に落雷させました。</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>自分自身に落雷させることはできません。</color>",
        "smite.exempt": "<prefix> <color:#FF0000>そのプレイヤーは /smite の対象外です。</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>使用法: /smite <プレイヤー></color>"
    },
    "it_it": {
        "smite.success": "<prefix> <color:#FFF200>Colpito <color:#FFFFFF><target></color> con un fulmine.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Non puoi colpire te stesso.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Quel giocatore è esente da /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Uso: /smite <giocatore></color>"
    },
    "id_id": {
        "smite.success": "<prefix> <color:#FFF200>Menyambar <color:#FFFFFF><target></color> dengan petir.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Anda tidak bisa menyambar diri sendiri.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Pemain itu kebal dari /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Penggunaan: /smite <pemain></color>"
    },
    "hi_in": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> को बिजली से मारा।</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>आप खुद को नहीं मार सकते।</color>",
        "smite.exempt": "<prefix> <color:#FF0000>वह खिलाड़ी /smite से मुक्त है।</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>उपयोग: /smite <खिलाड़ी></color>"
    },
    "gu_in": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> પર વીજળી પાડી.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>તમે તમારી જાતને મારી શકતા નથી.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>તે ખેલાડી /smite થી મુક્ત છે.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>ઉપયોગ: /smite <ખેલાડી></color>"
    },
    "fr_fr": {
        "smite.success": "<prefix> <color:#FFF200>A frappé <color:#FFFFFF><target></color> avec la foudre.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Vous ne pouvez pas vous frapper vous-même.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Ce joueur est exempté de /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Utilisation: /smite <joueur></color>"
    },
    "fil_ph": {
        "smite.success": "<prefix> <color:#FFF200>Tinamaan ng kidlat si <color:#FFFFFF><target></color>.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Hindi mo pwedeng parusahan ang iyong sarili.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Ang manlalaro na iyon ay exempt sa /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Paggamit: /smite <manlalaro></color>"
    },
    "es_es": {
        "smite.success": "<prefix> <color:#FFF200>Golpeaste a <color:#FFFFFF><target></color> con un rayo.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>No puedes golpearte a ti mismo.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Ese jugador está exento de /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Uso: /smite <jugador></color>"
    },
    "en_gb": {
        "smite.success": "<prefix> <color:#FFF200>Struck <color:#FFFFFF><target></color> with lightning.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>You cannot smite yourself.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>That player is exempt from /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Usage: /smite <player></color>"
    },
    "de_de": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> vom Blitz getroffen.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Du kannst dich nicht selbst bestrafen.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Dieser Spieler ist von /smite ausgenommen.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Nutzung: /smite <spieler></color>"
    },
    "de_ch": {
        "smite.success": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> vom Blitz getroffen.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Du kannst dich nicht selbst bestrafen.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Dieser Spieler ist von /smite ausgenommen.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Nutzung: /smite <spieler></color>"
    },
    "bn_bd": {
        "smite.success": "<prefix> <color:#FFF200>বজ্রপাত দিয়ে <color:#FFFFFF><target></color>-কে আঘাত করা হয়েছে।</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>আপনি নিজেকে আঘাত করতে পারবেন না।</color>",
        "smite.exempt": "<prefix> <color:#FF0000>ঐ খেলোয়াড় /smite থেকে মুক্ত।</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>ব্যবহার: /smite <খেলোয়াড়></color>"
    },
    "ar_sa": {
        "smite.success": "<prefix> <color:#FFF200>تم ضرب <color:#FFFFFF><target></color> بالبرق.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>لا يمكنك ضرب نفسك.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>هذا اللاعب معفى من /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>الاستخدام: /smite <لاعب></color>"
    },
    "ar_eg": {
        "smite.success": "<prefix> <color:#FFF200>تم ضرب <color:#FFFFFF><target></color> بالبرق.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>لا يمكنك ضرب نفسك.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>هذا اللاعب معفى من /smite.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>الاستخدام: /smite <لاعب></color>"
    },
    "lb_lu": {
        "smite.success": "<prefix> <color:#FFF200>Huet <color:#FFFFFF><target></color> mam Blëtz getraff.</color>",
        "smite.cannot_smite_self": "<prefix> <color:#FF0000>Du kanns dech net selwer bestrofen.</color>",
        "smite.exempt": "<prefix> <color:#FF0000>Dee Spiller ass vun /smite ausgemaach.</color>",
        "command.usage.smite": "<prefix> <color:#FF0000>Benotzung: /smite <spiller></color>"
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