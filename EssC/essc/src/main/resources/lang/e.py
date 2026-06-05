import json
from pathlib import Path

INSERT_AFTER = '  "home.list.entry"'

NEW_TRANSLATIONS = {
    "en_us": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Usage: /playerlist [page]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Online Players [<online><gray>/</gray><max>] - Page [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>No players online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Invalid page number.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Prev<#FFF200>]</color></click> <color:#FFFFFF>Page <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Next <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "zh_cn": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>用法: /playerlist [页码]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= 在线玩家 [<online><gray>/</gray><max>] - 第 [<page><gray>/</gray><total_pages>] 页 =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>当前没有玩家在线</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>无效的页码。</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>上一页<#FFF200>]</color></click> <color:#FFFFFF>第 <prev_page>/<total_pages> 页</color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>下一页 <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "vi_vn": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Cách dùng: /playerlist [trang]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Người chơi trực tuyến [<online><gray>/</gray><max>] - Trang [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Không có người chơi trực tuyến</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Số trang không hợp lệ.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Trước<#FFF200>]</color></click> <color:#FFFFFF>Trang <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Tiếp <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "ur_pk": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>استعمال: /playerlist [صفحہ]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= آن لائن کھلاڑی [<online><gray>/</gray><max>] - صفحہ [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>کوئی کھلاڑی آن لائن نہیں ہے</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>غلط صفحہ نمبر۔</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>پیچھے<#FFF200>]</color></click> <color:#FFFFFF>صفحہ <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>آگے <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "uk_ua": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Використання: /playerlist [сторінка]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Гравці онлайн [<online><gray>/</gray><max>] - Сторінка [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Гравців немає онлайн</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Неправильний номер сторінки.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Назад<#FFF200>]</color></click> <color:#FFFFFF>Сторінка <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Вперед <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "tr_tr": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Kullanım: /playerlist [sayfa]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Çevrimiçi Oyuncular [<online><gray>/</gray><max>] - Sayfa [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Çevrimiçi oyuncu yok</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Geçersiz sayfa numarası.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Önceki<#FFF200>]</color></click> <color:#FFFFFF>Sayfa <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Sonraki <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "th_th": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>วิธีใช้: /playerlist [หน้า]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= ผู้เล่นออนไลน์ [<online><gray>/</gray><max>] - หน้า [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>ไม่มีผู้เล่นออนไลน์</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>หมายเลขหน้าไม่ถูกต้อง</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>ย้อนกลับ<#FFF200>]</color></click> <color:#FFFFFF>หน้า <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>ถัดไป <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "te_in": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>విధానం: /playerlist [పేజీ]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= ఆన్‌లైన్ ఆటగాళ్లు [<online><gray>/</gray><max>] - పేజీ [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>ఆన్‌లైన్‌లో ఎవరూ లేరు</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>చెల్లని పేజీ సంఖ్య.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>గత<#FFF200>]</color></click> <color:#FFFFFF>పేజీ <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>తదుపరి <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "ta_in": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>பயன்பாடு: /playerlist [பக்கம்]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= ஆன்லைன் வீரர்கள் [<online><gray>/</gray><max>] - பக்கம் [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>ஆன்லைனில் வீரர்கள் இல்லை</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>தவறான பக்க எண்.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>முந்தைய<#FFF200>]</color></click> <color:#FFFFFF>பக்கம் <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>அடுத்தது <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "sv_se": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Användning: /playerlist [sida]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Spelare online [<online><gray>/</gray><max>] - Sida [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Inga spelare online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Ogiltigt sidnummer.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Föregående<#FFF200>]</color></click> <color:#FFFFFF>Sida <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Nästa <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "ru_ru": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Использование: /playerlist [страница]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Игроки онлайн [<online><gray>/</gray><max>] - Страница [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Нет игроков онлайн</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Неверный номер страницы.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Назад<#FFF200>]</color></click> <color:#FFFFFF>Страница <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Вперед <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "pt_br": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Uso: /playerlist [página]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Jogadores online [<online><gray>/</gray><max>] - Página [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Nenhum jogador online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Número de página inválido.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Anterior<#FFF200>]</color></click> <color:#FFFFFF>Página <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Próximo <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "pl_pl": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Użycie: /playerlist [strona]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Gracze online [<online><gray>/</gray><max>] - Strona [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Brak graczy online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Nieprawidłowy numer strony.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Poprzednia<#FFF200>]</color></click> <color:#FFFFFF>Strona <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Następna <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "nl_nl": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Gebruik: /playerlist [pagina]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Spelers online [<online><gray>/</gray><max>] - Pagina [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Geen spelers online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Ongeldig paginanummer.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Vorige<#FFF200>]</color></click> <color:#FFFFFF>Pagina <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Volgende <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "mr_in": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>वापर: /playerlist [पृष्ठ]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= ऑनलाइन खेळाडू [<online><gray>/</gray><max>] - पृष्ठ [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>कोणीही खेळाडू ऑनलाइन नाही</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>अवैध पृष्ठ क्रमांक.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>मागील<#FFF200>]</color></click> <color:#FFFFFF>पृष्ठ <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>पुढील <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "ko_kr": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>사용법: /playerlist [페이지]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= 접속 중인 플레이어 [<online><gray>/</gray><max>] - 페이지 [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>접속 중인 플레이어가 없습니다</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>잘못된 페이지 번호입니다.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>이전<#FFF200>]</color></click> <color:#FFFFFF>페이지 <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>다음 <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "ja_jp": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>使用法: /playerlist [ページ]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= オンラインプレイヤー [<online><gray>/</gray><max>] - ページ [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>オンラインのプレイヤーはいません</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>無効なページ番号です。</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>前へ<#FFF200>]</color></click> <color:#FFFFFF>ページ <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>次へ <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "it_it": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Uso: /playerlist [pagina]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Giocatori online [<online><gray>/</gray><max>] - Pagina [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Nessun giocatore online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Numero di pagina non valido.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Indietro<#FFF200>]</color></click> <color:#FFFFFF>Pagina <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Avanti <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "id_id": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Penggunaan: /playerlist [halaman]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Pemain online [<online><gray>/</gray><max>] - Halaman [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Tidak ada pemain online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Nomor halaman tidak valid.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Sebelumnya<#FFF200>]</color></click> <color:#FFFFFF>Halaman <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Selanjutnya <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "hi_in": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>उपयोग: /playerlist [पृष्ठ]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= ऑनलाइन खिलाड़ी [<online><gray>/</gray><max>] - पृष्ठ [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>कोई खिलाड़ी ऑनलाइन नहीं है</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>अमान्य पृष्ठ संख्या।</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>पिछला<#FFF200>]</color></click> <color:#FFFFFF>पृष्ठ <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>अगला <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "gu_in": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>ઉપયોગ: /playerlist [પૃષ્ઠ]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= ઓનલાઇન ખેલાડીઓ [<online><gray>/</gray><max>] - પૃષ્ઠ [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>કોઈ ખેલાડી ઓનલાઇન નથી</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>અમાન્ય પૃષ્ઠ નંબર.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>પાછળ<#FFF200>]</color></click> <color:#FFFFFF>પૃષ્ઠ <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>આગળ <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "fr_fr": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Utilisation: /playerlist [page]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Joueurs en ligne [<online><gray>/</gray><max>] - Page [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Aucun joueur en ligne</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Numéro de page invalide.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Précédent<#FFF200>]</color></click> <color:#FFFFFF>Page <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Suivant <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "fil_ph": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Paggamit: /playerlist [pahina]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Mga manlalaro online [<online><gray>/</gray><max>] - Pahina [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Walang manlalaro online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Maling numero ng pahina.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Nakaraan<#FFF200>]</color></click> <color:#FFFFFF>Pahina <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Susunod <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "es_es": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Uso: /playerlist [página]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Jugadores en línea [<online><gray>/</gray><max>] - Página [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>No hay jugadores en línea</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Número de página no válido.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Anterior<#FFF200>]</color></click> <color:#FFFFFF>Página <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Siguiente <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "en_gb": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Usage: /playerlist [page]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Online Players [<online><gray>/</gray><max>] - Page [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>No players online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Invalid page number.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Prev<#FFF200>]</color></click> <color:#FFFFFF>Page <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Next <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "de_de": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Nutzung: /playerlist [Seite]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Online-Spieler [<online><gray>/</gray><max>] - Seite [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Keine Spieler online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Ungültige Seitennummer.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Zurück<#FFF200>]</color></click> <color:#FFFFFF>Seite <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Vor<dark_gray> →<#FFF200>]</color></click></color>"
    },
    "de_ch": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Nutzung: /playerlist [Seite]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Online-Spieler [<online><gray>/</gray><max>] - Seite [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Keine Spieler online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Ungültige Seitennummer.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Zurück<#FFF200>]</color></click> <color:#FFFFFF>Seite <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Vor<dark_gray> →<#FFF200>]</color></click></color>"
    },
    "bn_bd": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>ব্যবহার: /playerlist [পৃষ্ঠা]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= অনলাইন খেলোয়াড় [<online><gray>/</gray><max>] - পৃষ্ঠা [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>কোন খেলোয়াড় অনলাইনে নেই</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>অবৈধ পৃষ্ঠা নম্বর।</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>পূর্ববর্তী<#FFF200>]</color></click> <color:#FFFFFF>পৃষ্ঠা <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>পরবর্তী <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "ar_sa": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>الاستخدام: /playerlist [صفحة]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= اللاعبون المتصلون [<online><gray>/</gray><max>] - صفحة [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>لا يوجد لاعبون متصلون</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>رقم صفحة غير صالح.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>السابق<#FFF200>]</color></click> <color:#FFFFFF>صفحة <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>التالي <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "ar_eg": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>الاستخدام: /playerlist [صفحة]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= اللاعبون المتصلون [<online><gray>/</gray><max>] - صفحة [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>لا يوجد لاعبون متصلون</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>رقم صفحة غير صالح.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>السابق<#FFF200>]</color></click> <color:#FFFFFF>صفحة <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>التالي <dark_gray>→<#FFF200>]</color></click></color>"
    },
    "lb_lu": {
        "command.usage.playerlist": "<prefix> <color:#FF0000>Benotzung: /playerlist [Säit]</color>",
        "playerlist.header": "<prefix> <color:#FFF200>= Spiller online [<online><gray>/</gray><max>] - Säit [<page><gray>/</gray><total_pages>] =</color>",
        "playerlist.empty": "<prefix> <color:#AAAAAA>Keng Spiller online</color>",
        "playerlist.footer": "<prefix> <color:#FFF200><bold>============================= </bold></color>",
        "playerlist.invalid_page": "<prefix> <color:#FF0000>Ongëlteg Säitennummer.</color>",
        "playerlist.navigation": "<prefix> <color:#AAAAAA><click:run_command:/playerlist <prev_page>><color:#FFF200>[<dark_gray>← <gray>Zeréck<#FFF200>]</color></click> <color:#FFFFFF>Säit <prev_page>/<total_pages></color> <click:run_command:/playerlist <next_page>><color:#FFF200>[<gray>Weider <dark_gray>→<#FFF200>]</color></click></color>"
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