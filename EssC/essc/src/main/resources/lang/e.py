import json
from pathlib import Path

INSERT_AFTER = '  "kits.error.unknown"'

NEW_TRANSLATIONS = {
    "en_us": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Previous Page</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Next Page</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>No Kits Available</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>You have no accessible kits.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Items: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Ready in: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Click to claim!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Claims: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "zh_cn": {
        "kit.gui.title": "<color:#404040>礼包 <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>上一页</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>下一页</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>无可用礼包</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>你没有可获取的礼包。</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>物品: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>冷却: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>准备就绪: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>点击领取！</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>领取次数: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "vi_vn": {
        "kit.gui.title": "<color:#404040>Bộ vật phẩm <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Trang trước</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Trang sau</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Không có bộ vật phẩm</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Bạn không có bộ vật phẩm nào.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Vật phẩm: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Hồi chiêu: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Sẵn sàng trong: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Nhấp để nhận!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Lượt nhận: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "ur_pk": {
        "kit.gui.title": "<color:#404040>کٹس <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>پچھلا صفحہ</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>اگلا صفحہ</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>کوئی کٹ دستیاب نہیں</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>آپ کے پاس کوئی کٹ نہیں ہے۔</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>اشیاء: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>کولڈاؤن: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>تیار ہو گی: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>حاصل کرنے کے لیے کلک کریں!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>کلیکشنز: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "uk_ua": {
        "kit.gui.title": "<color:#404040>Набори <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Попередня сторінка</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Наступна сторінка</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Немає наборів</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Ви не маєте доступу до наборів.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Предмети: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Відкат: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Готово через: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Натисніть, щоб отримати!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Отримано: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "tr_tr": {
        "kit.gui.title": "<color:#404040>Kitler <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Önceki Sayfa</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Sonraki Sayfa</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Kullanılabilir Kit Yok</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Erişilebilir kitin yok.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Eşyalar: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Bekleme Süresi: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Hazır olma: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Almak için tıkla!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Alım: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "th_th": {
        "kit.gui.title": "<color:#404040>ชุดไอเทม <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>หน้าก่อนหน้า</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>หน้าถัดไป</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>ไม่มีชุดไอเทม</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>คุณไม่มีสิทธิ์เข้าถึงชุดไอเทม</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>ไอเทม: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>คูลดาวน์: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>พร้อมใน: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>คลิกเพื่อรับ!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>ได้รับ: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "te_in": {
        "kit.gui.title": "<color:#404040>కిట్‌లు <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>మునుపటి పేజీ</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>తదుపరి పేజీ</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>కిట్‌లు అందుబాటులో లేవు</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>మీకు ఎటువంటి కిట్‌లు లేవు.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>ఐటమ్‌లు: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>కూల్‌డౌన్: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>సిద్ధమవుతుంది: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>క్లెయిమ్ చేయడానికి క్లిక్ చేయండి!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>క్లెయిమ్‌లు: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "ta_in": {
        "kit.gui.title": "<color:#404040>கிட்கள் <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>முந்தைய பக்கம்</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>அடுத்த பக்கம்</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>கிட்கள் இல்லை</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>உங்களிடம் எந்த கிட்களும் இல்லை.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>பொருட்கள்: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>கூலிங் டவுன்: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>தயாராக: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>பெற கிளிக் செய்யவும்!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>பெறப்பட்டது: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "sv_se": {
        "kit.gui.title": "<color:#404040>Kit <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Föregående sida</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Nästa sida</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Inga Kit tillgängliga</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Du har inga tillgängliga kit.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Föremål: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Nedkylning: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Klar om: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Klicka för att hämta!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Hämtningar: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "ru_ru": {
        "kit.gui.title": "<color:#404040>Наборы <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Предыдущая страница</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Следующая страница</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Нет доступных наборов</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>У вас нет доступных наборов.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Предметы: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Откат: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Готов через: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Нажмите, чтобы забрать!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Получено: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "pt_br": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Página Anterior</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Próxima Página</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Sem Kits Disponíveis</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Você não tem kits acessíveis.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Itens: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Recarga: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Pronto em: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Clique para resgatar!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Resgates: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "pl_pl": {
        "kit.gui.title": "<color:#404040>Zestawy <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Poprzednia strona</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Następna strona</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Brak dostępnych zestawów</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Nie masz dostępnych zestawów.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Przedmioty: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Czas odnowienia: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Gotowe za: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Kliknij, aby odebrać!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Odebrane: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "nl_nl": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Vorige pagina</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Volgende pagina</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Geen kits beschikbaar</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Je hebt geen toegankelijke kits.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Items: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Klaar in: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Klik om te claimen!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Claims: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "mr_in": {
        "kit.gui.title": "<color:#404040>किट <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>मागील पान</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>पुढील पान</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>कोणतेही किट उपलब्ध नाहीत</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>तुमच्याकडे कोणतेही किट नाहीत.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>वस्तू: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>कूलडाउन: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>तयार होईल: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>दावा करण्यासाठी क्लिक करा!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>दावे: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "ko_kr": {
        "kit.gui.title": "<color:#404040>키트 <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>이전 페이지</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>다음 페이지</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>사용 가능한 키트 없음</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>사용 가능한 키트가 없습니다.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>아이템: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>쿨타임: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>준비 완료: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>클릭하여 수령!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>수령 횟수: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "ja_jp": {
        "kit.gui.title": "<color:#404040>キット <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>前のページ</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>次のページ</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>利用可能なキットなし</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>利用できるキットはありません。</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>アイテム: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>クールダウン: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>準備完了まで: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>クリックして受け取る!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>受取回数: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "it_it": {
        "kit.gui.title": "<color:#404040>Kit <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Pagina Precedente</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Pagina Successiva</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Nessun Kit Disponibile</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Non hai kit accessibili.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Oggetti: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Pronto tra: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Clicca per reclamare!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Reclami: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "id_id": {
        "kit.gui.title": "<color:#404040>Kit <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Halaman Sebelumnya</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Halaman Berikutnya</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Tidak Ada Kit Tersedia</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Kamu tidak memiliki kit yang tersedia.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Item: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Siap dalam: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Klik untuk klaim!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Klaim: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "hi_in": {
        "kit.gui.title": "<color:#404040>किट <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>पिछला पेज</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>अगला पेज</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>कोई किट उपलब्ध नहीं</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>आपके पास कोई किट उपलब्ध नहीं है।</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>आइटम: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>कूलडाउन: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>तैयार होगा: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>दावा करने के लिए क्लिक करें!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>दावे: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "gu_in": {
        "kit.gui.title": "<color:#404040>કિટ <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>પાછળનું પેજ</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>આગળનું પેજ</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>કોઈ કિટ ઉપલબ્ધ નથી</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>તમારી પાસે કોઈ કિટ નથી.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>આઇટમ્સ: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>કૂલડાઉન: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>તૈયાર થશે: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>દાવો કરવા માટે ક્લિક કરો!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>દાવો: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "fr_fr": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Page précédente</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Page suivante</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Aucun kit disponible</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Vous n'avez aucun kit accessible.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Objets : <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown : <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Prêt dans : <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Cliquez pour réclamer !</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Réclamations : <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "fil_ph": {
        "kit.gui.title": "<color:#404040>Mga Kit <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Nakaraang Pahina</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Susunod na Pahina</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Walang Available na Kit</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Wala kang ma-a-access na kit.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Mga Item: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Handa sa loob ng: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>I-click para makuha!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Claims: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "es_es": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Página Anterior</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Página Siguiente</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>No hay kits disponibles</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>No tienes kits accesibles.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Objetos: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Recarga: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Listo en: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>¡Haz clic para reclamar!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Reclamaciones: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "en_gb": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Previous Page</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Next Page</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>No Kits Available</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>You have no accessible kits.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Items: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Ready in: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Click to claim!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Claims: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "de_de": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Vorherige Seite</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Nächste Seite</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Keine Kits verfügbar</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Du hast keine zugänglichen Kits.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Gegenstände: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Abklingzeit: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Bereit in: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Klicken zum Beanspruchen!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Beanspruchungen: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "de_ch": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Vorherigi Site</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Nächsti Site</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Kei Kits verfügbar</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Du hesch kei zugänglechi Kits.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Gegeständ: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Abchülig: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Bereit in: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Chlick zum beanspruche!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Beanspruchige: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "bn_bd": {
        "kit.gui.title": "<color:#404040>কিট <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>পূর্ববর্তী পৃষ্ঠা</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>পরবর্তী পৃষ্ঠা</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>কোন কিট নেই</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>আপনার কাছে কোন কিট নেই।</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>আইটেম: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>কুলডাউন: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>প্রস্তুত হবে: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>ক্লেম করতে ক্লিক করুন!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>ক্লেম: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "ar_sa": {
        "kit.gui.title": "<color:#404040>مجموعات <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>الصفحة السابقة</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>الصفحة التالية</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>لا توجد مجموعات متاحة</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>ليس لديك مجموعات متاحة.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>عناصر: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>فترة الانتظار: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>جاهزة في: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>انقر للمطالبة!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>المطالبات: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "ar_eg": {
        "kit.gui.title": "<color:#404040>كتات <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>الصفحة اللي فاتت</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>الصفحة الجاية</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>مافيش كتات متاحة</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>مافيش كتات تقدر تاخدها.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>حاجات: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>وقت الانتظار: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>جاهزة في: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>دوس عشان تاخدها!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>مرات الأخد: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
    },
    "lb_lu": {
        "kit.gui.title": "<color:#404040>Kits <color:#AAAAAA>(<color:#FFFFFF><page></color>/<color:#FFFFFF><total></color>)</color>",
        "kit.gui.item.nav.prev.name": "<color:#FFF200>Virecht Säit</color>",
        "kit.gui.item.nav.next.name": "<color:#FFF200>Nächst Säit</color>",
        "kit.gui.item.empty.name": "<color:#FF0000>Keng Kits verfügbar</color>",
        "kit.gui.item.empty.lore1": "<color:#AAAAAA>Du hues keng Kits disponibel.</color>",
        "kit.gui.item.kit.name.available": "<color:#57F527><kit></color>",
        "kit.gui.item.kit.name.cooldown": "<color:#FFF200><kit></color>",
        "kit.gui.item.kit.name.used": "<color:#FF0000><kit></color>",
        "kit.gui.item.kit.lore.items": "<color:#AAAAAA>Elementer: <color:#FFFFFF><count></color></color>",
        "kit.gui.item.kit.lore.cooldown": "<color:#AAAAAA>Cooldown: <color:#FFFFFF><time></color></color>",
        "kit.gui.item.kit.lore.ready_in": "<color:#AAAAAA>Bereet an: <color:#FFF200><time></color></color>",
        "kit.gui.item.kit.lore.click_to_claim": "<color:#57F527>Klick fir ze kréien!</color>",
        "kit.gui.item.kit.lore.claims": "<color:#AAAAAA>Claims: <color:#FFFFFF><count></color>/<color:#FFFFFF><max></color></color>"
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