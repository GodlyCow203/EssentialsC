import json
from pathlib import Path

INSERT_AFTER = '  "trash.error.blacklisted"'

NEW_TRANSLATIONS = {
    "en_us": {
        "trash.confirm.title": "<#FF4444><b>Confirm Disposal</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Confirm</b>",
        "trash.confirm.confirm.lore": "<#888888>Click to permanently delete items.",
        "trash.confirm.cancel.name": "<#FF4444><b>Cancel</b>",
        "trash.confirm.cancel.lore": "<#888888>Click to take your items back."
    },
    "zh_cn": {
        "trash.confirm.title": "<#FF4444><b>确认删除</b>",
        "trash.confirm.confirm.name": "<#57F527><b>确认</b>",
        "trash.confirm.confirm.lore": "<#888888>点击以永久删除物品。",
        "trash.confirm.cancel.name": "<#FF4444><b>取消</b>",
        "trash.confirm.cancel.lore": "<#888888>点击以找回您的物品。"
    },
    "vi_vn": {
        "trash.confirm.title": "<#FF4444><b>Xác nhận hủy bỏ</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Xác nhận</b>",
        "trash.confirm.confirm.lore": "<#888888>Nhấp để xóa vĩnh viễn các mục.",
        "trash.confirm.cancel.name": "<#FF4444><b>Hủy bỏ</b>",
        "trash.confirm.cancel.lore": "<#888888>Nhấp để lấy lại các mục của bạn."
    },
    "ur_pk": {
        "trash.confirm.title": "<#FF4444><b>تلف کرنے کی تصدیق کریں</b>",
        "trash.confirm.confirm.name": "<#57F527><b>تصدیق کریں</b>",
        "trash.confirm.confirm.lore": "<#888888>آئٹمز کو مستقل طور پر حذف کرنے کے لیے کلک کریں۔",
        "trash.confirm.cancel.name": "<#FF4444><b>منسوخ کریں</b>",
        "trash.confirm.cancel.lore": "<#888888>اپنے آئٹمز واپس لینے کے لیے کلک کریں۔"
    },
    "uk_ua": {
        "trash.confirm.title": "<#FF4444><b>Підтвердити видалення</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Підтвердити</b>",
        "trash.confirm.confirm.lore": "<#888888>Натисніть, щоб видалити предмети назавжди.",
        "trash.confirm.cancel.name": "<#FF4444><b>Скасувати</b>",
        "trash.confirm.cancel.lore": "<#888888>Натисніть, щоб повернути предмети назад."
    },
    "tr_tr": {
        "trash.confirm.title": "<#FF4444><b>İmha İşlemini Onayla</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Onayla</b>",
        "trash.confirm.confirm.lore": "<#888888>Öğeleri kalıcı olarak silmek için tıklayın.",
        "trash.confirm.cancel.name": "<#FF4444><b>İptal</b>",
        "trash.confirm.cancel.lore": "<#888888>Öğelerinizi geri almak için tıklayın."
    },
    "th_th": {
        "trash.confirm.title": "<#FF4444><b>ยืนยันการทำลาย</b>",
        "trash.confirm.confirm.name": "<#57F527><b>ยืนยัน</b>",
        "trash.confirm.confirm.lore": "<#888888>คลิกเพื่อลบรายการอย่างถาวร",
        "trash.confirm.cancel.name": "<#FF4444><b>ยกเลิก</b>",
        "trash.confirm.cancel.lore": "<#888888>คลิกเพื่อรับรายการของคุณกลับคืน"
    },
    "te_in": {
        "trash.confirm.title": "<#FF4444><b>తొలగింపును నిర్ధారించండి</b>",
        "trash.confirm.confirm.name": "<#57F527><b>నిర్ధారించు</b>",
        "trash.confirm.confirm.lore": "<#888888>అంశాలను శాశ్వతంగా తొలగించడానికి క్లిక్ చేయండి.",
        "trash.confirm.cancel.name": "<#FF4444><b>రద్దు చేయి</b>",
        "trash.confirm.cancel.lore": "<#888888>మీ అంశాలను తిరిగి పొందడానికి క్లిక్ చేయండి."
    },
    "ta_in": {
        "trash.confirm.title": "<#FF4444><b>நீக்குதலை உறுதிப்படுத்தவும்</b>",
        "trash.confirm.confirm.name": "<#57F527><b>உறுதிப்படுத்தவும்</b>",
        "trash.confirm.confirm.lore": "<#888888>பொருட்களை நிரந்தரமாக நீக்க கிளிக் செய்யவும்.",
        "trash.confirm.cancel.name": "<#FF4444><b>ரத்துசெய்</b>",
        "trash.confirm.cancel.lore": "<#888888>உங்கள் பொருட்களைத் திரும்பப் பெற கிளிக் செய்யவும்."
    },
    "sv_se": {
        "trash.confirm.title": "<#FF4444><b>Bekräfta borttagning</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Bekräfta</b>",
        "trash.confirm.confirm.lore": "<#888888>Klicka för att radera föremål permanent.",
        "trash.confirm.cancel.name": "<#FF4444><b>Avbryt</b>",
        "trash.confirm.cancel.lore": "<#888888>Klicka för att få tillbaka dina föremål."
    },
    "ru_ru": {
        "trash.confirm.title": "<#FF4444><b>Подтвердить удаление</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Подтвердить</b>",
        "trash.confirm.confirm.lore": "<#888888>Нажмите, чтобы безвозвратно удалить предметы.",
        "trash.confirm.cancel.name": "<#FF4444><b>Отмена</b>",
        "trash.confirm.cancel.lore": "<#888888>Нажмите, чтобы вернуть предметы назад."
    },
    "pt_br": {
        "trash.confirm.title": "<#FF4444><b>Confirmar Descarte</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Confirmar</b>",
        "trash.confirm.confirm.lore": "<#888888>Clique para excluir itens permanentemente.",
        "trash.confirm.cancel.name": "<#FF4444><b>Cancelar</b>",
        "trash.confirm.cancel.lore": "<#888888>Clique para recuperar seus itens."
    },
    "pl_pl": {
        "trash.confirm.title": "<#FF4444><b>Potwierdź usunięcie</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Potwierdź</b>",
        "trash.confirm.confirm.lore": "<#888888>Kliknij, aby trwale usunąć przedmioty.",
        "trash.confirm.cancel.name": "<#FF4444><b>Anuluj</b>",
        "trash.confirm.cancel.lore": "<#888888>Kliknij, aby odzyskać swoje przedmioty."
    },
    "nl_nl": {
        "trash.confirm.title": "<#FF4444><b>Bevestig verwijdering</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Bevestigen</b>",
        "trash.confirm.confirm.lore": "<#888888>Klik om items permanent te verwijderen.",
        "trash.confirm.cancel.name": "<#FF4444><b>Annuleren</b>",
        "trash.confirm.cancel.lore": "<#888888>Klik om uw items terug te nemen."
    },
    "mr_in": {
        "trash.confirm.title": "<#FF4444><b>हटवण्याची पुष्टी करा</b>",
        "trash.confirm.confirm.name": "<#57F527><b>पुष्टी करा</b>",
        "trash.confirm.confirm.lore": "<#888888>आयटम कायमचे हटवण्यासाठी क्लिक करा.",
        "trash.confirm.cancel.name": "<#FF4444><b>रद्द करा</b>",
        "trash.confirm.cancel.lore": "<#888888>तुमचे आयटम परत मिळवण्यासाठी क्लिक करा."
    },
    "ko_kr": {
        "trash.confirm.title": "<#FF4444><b>삭제 확인</b>",
        "trash.confirm.confirm.name": "<#57F527><b>확인</b>",
        "trash.confirm.confirm.lore": "<#888888>클릭하여 아이템을 영구적으로 삭제합니다.",
        "trash.confirm.cancel.name": "<#FF4444><b>취소</b>",
        "trash.confirm.cancel.lore": "<#888888>클릭하여 아이템을 되돌립니다."
    },
    "ja_jp": {
        "trash.confirm.title": "<#FF4444><b>破棄の確認</b>",
        "trash.confirm.confirm.name": "<#57F527><b>確認</b>",
        "trash.confirm.confirm.lore": "<#888888>クリックしてアイテムを完全に削除します。",
        "trash.confirm.cancel.name": "<#FF4444><b>キャンセル</b>",
        "trash.confirm.cancel.lore": "<#888888>クリックしてアイテムを取り戻します。"
    },
    "it_it": {
        "trash.confirm.title": "<#FF4444><b>Conferma smaltimento</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Conferma</b>",
        "trash.confirm.confirm.lore": "<#888888>Clicca per eliminare definitivamente gli oggetti.",
        "trash.confirm.cancel.name": "<#FF4444><b>Annulla</b>",
        "trash.confirm.cancel.lore": "<#888888>Clicca per recuperare i tuoi oggetti."
    },
    "id_id": {
        "trash.confirm.title": "<#FF4444><b>Konfirmasi Pembuangan</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Konfirmasi</b>",
        "trash.confirm.confirm.lore": "<#888888>Klik untuk menghapus item secara permanen.",
        "trash.confirm.cancel.name": "<#FF4444><b>Batal</b>",
        "trash.confirm.cancel.lore": "<#888888>Klik untuk mengambil kembali item Anda."
    },
    "hi_in": {
        "trash.confirm.title": "<#FF4444><b>निपटान की पुष्टि करें</b>",
        "trash.confirm.confirm.name": "<#57F527><b>पुष्टि करें</b>",
        "trash.confirm.confirm.lore": "<#888888>आइटम को स्थायी रूप से हटाने के लिए क्लिक करें।",
        "trash.confirm.cancel.name": "<#FF4444><b>रद्द करें</b>",
        "trash.confirm.cancel.lore": "<#888888>अपने आइटम वापस लेने के लिए क्लिक करें।"
    },
    "gu_in": {
        "trash.confirm.title": "<#FF4444><b>નિકાલની પુષ્ટિ કરો</b>",
        "trash.confirm.confirm.name": "<#57F527><b>પુષ્ટિ કરો</b>",
        "trash.confirm.confirm.lore": "<#888888>આઇટમ્સ કાયમી ધોરણે કાઢી નાખવા માટે ક્લિક કરો.",
        "trash.confirm.cancel.name": "<#FF4444><b>રદ કરો</b>",
        "trash.confirm.cancel.lore": "<#888888>તમારી આઇટમ્સ પાછી મેળવવા માટે ક્લિક કરો."
    },
    "fr_fr": {
        "trash.confirm.title": "<#FF4444><b>Confirmer la suppression</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Confirmer</b>",
        "trash.confirm.confirm.lore": "<#888888>Cliquez pour supprimer définitivement les objets.",
        "trash.confirm.cancel.name": "<#FF4444><b>Annuler</b>",
        "trash.confirm.cancel.lore": "<#888888>Cliquez pour récupérer vos objets."
    },
    "fil_ph": {
        "trash.confirm.title": "<#FF4444><b>Kumpirmahin ang Pagtatapon</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Kumpirmahin</b>",
        "trash.confirm.confirm.lore": "<#888888>I-click para permanenteng burahin ang mga item.",
        "trash.confirm.cancel.name": "<#FF4444><b>Kanselahin</b>",
        "trash.confirm.cancel.lore": "<#888888>I-click para bawiin ang iyong mga item."
    },
    "es_es": {
        "trash.confirm.title": "<#FF4444><b>Confirmar eliminación</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Confirmar</b>",
        "trash.confirm.confirm.lore": "<#888888>Haz clic para eliminar los objetos permanentemente.",
        "trash.confirm.cancel.name": "<#FF4444><b>Cancelar</b>",
        "trash.confirm.cancel.lore": "<#888888>Haz clic para recuperar tus objetos."
    },
    "en_gb": {
        "trash.confirm.title": "<#FF4444><b>Confirm Disposal</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Confirm</b>",
        "trash.confirm.confirm.lore": "<#888888>Click to permanently delete items.",
        "trash.confirm.cancel.name": "<#FF4444><b>Cancel</b>",
        "trash.confirm.cancel.lore": "<#888888>Click to take your items back."
    },
    "de_de": {
        "trash.confirm.title": "<#FF4444><b>Löschen bestätigen</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Bestätigen</b>",
        "trash.confirm.confirm.lore": "<#888888>Klicke, um Gegenstände dauerhaft zu löschen.",
        "trash.confirm.cancel.name": "<#FF4444><b>Abbrechen</b>",
        "trash.confirm.cancel.lore": "<#888888>Klicke, um deine Gegenstände zurückzunehmen."
    },
    "de_ch": {
        "trash.confirm.title": "<#FF4444><b>Löschen bestätigen</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Bestätigen</b>",
        "trash.confirm.confirm.lore": "<#888888>Klicke, um Gegenstände dauerhaft zu löschen.",
        "trash.confirm.cancel.name": "<#FF4444><b>Abbrechen</b>",
        "trash.confirm.cancel.lore": "<#888888>Klicke, um deine Gegenstände zurückzunehmen."
    },
    "bn_bd": {
        "trash.confirm.title": "<#FF4444><b>মুছে ফেলার বিষয়টি নিশ্চিত করুন</b>",
        "trash.confirm.confirm.name": "<#57F527><b>নিশ্চিত করুন</b>",
        "trash.confirm.confirm.lore": "<#888888>আইটেমগুলি স্থায়ীভাবে মুছতে ক্লিক করুন।",
        "trash.confirm.cancel.name": "<#FF4444><b>বাতিল করুন</b>",
        "trash.confirm.cancel.lore": "<#888888>আপনার আইটেমগুলি ফিরে পেতে ক্লিক করুন।"
    },
    "ar_sa": {
        "trash.confirm.title": "<#FF4444><b>تأكيد التخلص</b>",
        "trash.confirm.confirm.name": "<#57F527><b>تأكيد</b>",
        "trash.confirm.confirm.lore": "<#888888>انقر لحذف العناصر بشكل دائم.",
        "trash.confirm.cancel.name": "<#FF4444><b>إلغاء</b>",
        "trash.confirm.cancel.lore": "<#888888>انقر لاستعادة عناصرك."
    },
    "ar_eg": {
        "trash.confirm.title": "<#FF4444><b>تأكيد التخلص</b>",
        "trash.confirm.confirm.name": "<#57F527><b>تأكيد</b>",
        "trash.confirm.confirm.lore": "<#888888>انقر لحذف العناصر بشكل دائم.",
        "trash.confirm.cancel.name": "<#FF4444><b>إلغاء</b>",
        "trash.confirm.cancel.lore": "<#888888>انقر لاستعادة عناصرك."
    },
    "lb_lu": {
        "trash.confirm.title": "<#FF4444><b>Läschen bestätegen</b>",
        "trash.confirm.confirm.name": "<#57F527><b>Bestätegen</b>",
        "trash.confirm.confirm.lore": "<#888888>Klickt hei fir d'Elementer definitiv ze läschen.",
        "trash.confirm.cancel.name": "<#FF4444><b>Ofbriechen</b>",
        "trash.confirm.cancel.lore": "<#888888>Klickt hei fir är Elementer zréckzehuelen."
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