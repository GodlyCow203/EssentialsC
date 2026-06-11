import json
from pathlib import Path

INSERT_AFTER = '  "command.usage.smite"'  # Change this to an existing key in your target files

NEW_TRANSLATIONS = {
    "en_us": {
        "gamemode.changed": "<prefix> <color:#FFF200>Gamemode changed to <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Set <color:#FFFFFF><target></color>'s gamemode to <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Your gamemode was set to <color:#FFFFFF><mode></color> by <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Unknown gamemode: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Usage: /gm <survival|creative|adventure|spectator> [player]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Usage: /gms [player]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Usage: /gmc [player]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Usage: /gmsp [player]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Usage: /gma [player]</color>"
    },
    "zh_cn": {
        "gamemode.changed": "<prefix> <color:#FFF200>游戏模式已更改为 <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>已将 <color:#FFFFFF><target></color> 的游戏模式设置为 <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>你的游戏模式已被 <color:#FFFFFF><player></color> 设置为 <color:#FFFFFF><mode></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>未知游戏模式: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>用法: /gm <survival|creative|adventure|spectator> [玩家]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>用法: /gms [玩家]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>用法: /gmc [玩家]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>用法: /gmsp [玩家]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>用法: /gma [玩家]</color>"
    },
    "vi_vn": {
        "gamemode.changed": "<prefix> <color:#FFF200>Chế độ chơi đã đổi thành <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Đã đặt chế độ chơi của <color:#FFFFFF><target></color> thành <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Chế độ chơi của bạn đã được <color:#FFFFFF><player></color> đặt thành <color:#FFFFFF><mode></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Chế độ chơi không xác định: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Cách dùng: /gm <survival|creative|adventure|spectator> [người_chơi]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Cách dùng: /gms [người_chơi]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Cách dùng: /gmc [người_chơi]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Cách dùng: /gmsp [người_chơi]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Cách dùng: /gma [người_chơi]</color>"
    },
    "ur_pk": {
        "gamemode.changed": "<prefix> <color:#FFF200>گیم موڈ <color:#FFFFFF><mode></color> میں تبدیل ہو گیا</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> کا گیم موڈ <color:#FFFFFF><mode></color> پر سیٹ کیا گیا</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>آپ کا گیم موڈ <color:#FFFFFF><player></color> کی طرف سے <color:#FFFFFF><mode></color> پر سیٹ کیا گیا</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>نامعلوم گیم موڈ: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>استعمال: /gm <survival|creative|adventure|spectator> [کھلاڑی]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>استعمال: /gms [کھلاڑی]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>استعمال: /gmc [کھلاڑی]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>استعمال: /gmsp [کھلاڑی]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>استعمال: /gma [کھلاڑی]</color>"
    },
    "uk_ua": {
        "gamemode.changed": "<prefix> <color:#FFF200>Режим гри змінено на <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Встановлено режим гри <color:#FFFFFF><target></color> на <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Ваш режим гри було встановлено на <color:#FFFFFF><mode></color> гравцем <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Невідомий режим гри: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Використання: /gm <survival|creative|adventure|spectator> [гравець]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Використання: /gms [гравець]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Використання: /gmc [гравець]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Використання: /gmsp [гравець]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Використання: /gma [гравець]</color>"
    },
    "tr_tr": {
        "gamemode.changed": "<prefix> <color:#FFF200>Oyun modu <color:#FFFFFF><mode></color> olarak değiştirildi</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> oyuncusunun oyun modu <color:#FFFFFF><mode></color> yapıldı</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Oyun modun <color:#FFFFFF><player></color> tarafından <color:#FFFFFF><mode></color> yapıldı</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Bilinmeyen oyun modu: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Kullanım: /gm <survival|creative|adventure|spectator> [oyuncu]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Kullanım: /gms [oyuncu]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Kullanım: /gmc [oyuncu]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Kullanım: /gmsp [oyuncu]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Kullanım: /gma [oyuncu]</color>"
    },
    "th_th": {
        "gamemode.changed": "<prefix> <color:#FFF200>เปลี่ยนโหมดเกมเป็น <color:#FFFFFF><mode></color> แล้ว</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>ตั้งโหมดเกมของ <color:#FFFFFF><target></color> เป็น <color:#FFFFFF><mode></color> แล้ว</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>โหมดเกมของคุณถูกเปลี่ยนเป็น <color:#FFFFFF><mode></color> โดย <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>โหมดเกมไม่ถูกต้อง: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>วิธีใช้: /gm <survival|creative|adventure|spectator> [ผู้เล่น]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>วิธีใช้: /gms [ผู้เล่น]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>วิธีใช้: /gmc [ผู้เล่น]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>วิธีใช้: /gmsp [ผู้เล่น]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>วิธีใช้: /gma [ผู้เล่น]</color>"
    },
    "te_in": {
        "gamemode.changed": "<prefix> <color:#FFF200>గేమ్‌మోడ్ <color:#FFFFFF><mode></mode></color> కి మార్చబడింది</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> గేమ్‌మోడ్‌ను <color:#FFFFFF><mode></mode></color> కి సెట్ చేసారు</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>మీ గేమ్‌మోడ్ <color:#FFFFFF><player></color> ద్వారా <color:#FFFFFF><mode></mode></color> కి సెట్ చేయబడింది</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>తెలియని గేమ్‌మోడ్: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>విధానం: /gm <survival|creative|adventure|spectator> [ఆటగాడు]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>విధానం: /gms [ఆటగాడు]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>విధానం: /gmc [ఆటగాడు]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>విధానం: /gmsp [ఆటగాడు]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>విధానం: /gma [ఆటగాడు]</color>"
    },
    "ta_in": {
        "gamemode.changed": "<prefix> <color:#FFF200>கேம் மோட் <color:#FFFFFF><mode></mode></color> ஆக மாற்றப்பட்டது</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> இன் கேம் மோட் <color:#FFFFFF><mode></mode></color> ஆக அமைக்கப்பட்டது</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>உங்கள் கேம் மோட் <color:#FFFFFF><player></color> ஆல் <color:#FFFFFF><mode></mode></color> ஆக அமைக்கப்பட்டது</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>தெரியாத கேம் மோட்: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>பயன்பாடு: /gm <survival|creative|adventure|spectator> [வீரர்]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>பயன்பாடு: /gms [வீரர்]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>பயன்பாடு: /gmc [வீரர்]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>பயன்பாடு: /gmsp [வீரர்]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>பயன்பாடு: /gma [வீரர்]</color>"
    },
    "sv_se": {
        "gamemode.changed": "<prefix> <color:#FFF200>Spelläge ändrat till <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Satte <color:#FFFFFF><target></color>'s spelläge till <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Ditt spelläge sattes till <color:#FFFFFF><mode></color> av <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Okänt spelläge: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Användning: /gm <survival|creative|adventure|spectator> [spelare]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Användning: /gms [spelare]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Användning: /gmc [spelare]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Användning: /gmsp [spelare]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Användning: /gma [spelare]</color>"
    },
    "ru_ru": {
        "gamemode.changed": "<prefix> <color:#FFF200>Игровой режим изменен на <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Установлен игровой режим <color:#FFFFFF><target></color> на <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Ваш игровой режим был установлен на <color:#FFFFFF><mode></color> игроком <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Неизвестный игровой режим: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Использование: /gm <survival|creative|adventure|spectator> [игрок]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Использование: /gms [игрок]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Использование: /gmc [игрок]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Использование: /gmsp [игрок]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Использование: /gma [игрок]</color>"
    },
    "pt_br": {
        "gamemode.changed": "<prefix> <color:#FFF200>Modo de jogo alterado para <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Definido o modo de jogo de <color:#FFFFFF><target></color> para <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Seu modo de jogo foi definido para <color:#FFFFFF><mode></color> por <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Modo de jogo desconhecido: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Uso: /gm <survival|creative|adventure|spectator> [jogador]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Uso: /gms [jogador]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Uso: /gmc [jogador]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Uso: /gmsp [jogador]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Uso: /gma [jogador]</color>"
    },
    "pl_pl": {
        "gamemode.changed": "<prefix> <color:#FFF200>Tryb gry zmieniony na <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Ustawiono tryb gry gracza <color:#FFFFFF><target></color> na <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Twój tryb gry został ustawiony na <color:#FFFFFF><mode></color> przez <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Nieznany tryb gry: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Użycie: /gm <survival|creative|adventure|spectator> [gracz]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Użycie: /gms [gracz]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Użycie: /gmc [gracz]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Użycie: /gmsp [gracz]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Użycie: /gma [gracz]</color>"
    },
    "nl_nl": {
        "gamemode.changed": "<prefix> <color:#FFF200>Gamemode veranderd naar <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Gamemode van <color:#FFFFFF><target></color> veranderd naar <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Jouw gamemode is veranderd naar <color:#FFFFFF><mode></color> door <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Onbekende gamemode: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Gebruik: /gm <survival|creative|adventure|spectator> [speler]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Gebruik: /gms [speler]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Gebruik: /gmc [speler]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Gebruik: /gmsp [speler]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Gebruik: /gma [speler]</color>"
    },
    "mr_in": {
        "gamemode.changed": "<prefix> <color:#FFF200>गेममोड <color:#FFFFFF><mode></mode></color> वर बदलला</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> चा गेममोड <color:#FFFFFF><mode></mode></color> वर सेट केला</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>तुमचा गेममोड <color:#FFFFFF><player></color> द्वारे <color:#FFFFFF><mode></mode></color> वर सेट केला गेला</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>अवैध गेममोड: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>वापर: /gm <survival|creative|adventure|spectator> [खेळाडू]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>वापर: /gms [खेळाडू]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>वापर: /gmc [खेळाडू]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>वापर: /gmsp [खेळाडू]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>वापर: /gma [खेळाडू]</color>"
    },
    "ko_kr": {
        "gamemode.changed": "<prefix> <color:#FFF200>게임 모드가 <color:#FFFFFF><mode></color>(으)로 변경되었습니다</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color>의 게임 모드를 <color:#FFFFFF><mode></color>(으)로 설정했습니다</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>당신의 게임 모드가 <color:#FFFFFF><player></color>에 의해 <color:#FFFFFF><mode></color>(으)로 설정되었습니다</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>알 수 없는 게임 모드: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>사용법: /gm <survival|creative|adventure|spectator> [플레이어]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>사용법: /gms [플레이어]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>사용법: /gmc [플레이어]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>사용법: /gmsp [플레이어]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>사용법: /gma [플레이어]</color>"
    },
    "ja_jp": {
        "gamemode.changed": "<prefix> <color:#FFF200>ゲームモードが <color:#FFFFFF><mode></color> に変更されました</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> のゲームモードを <color:#FFFFFF><mode></color> に設定しました</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>あなたのゲームモードは <color:#FFFFFF><player></color> によって <color:#FFFFFF><mode></color> に設定されました</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>不明なゲームモード: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>使用法: /gm <survival|creative|adventure|spectator> [プレイヤー]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>使用法: /gms [プレイヤー]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>使用法: /gmc [プレイヤー]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>使用法: /gmsp [プレイヤー]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>使用法: /gma [プレイヤー]</color>"
    },
    "it_it": {
        "gamemode.changed": "<prefix> <color:#FFF200>Modalità di gioco cambiata in <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Impostata la modalità di gioco di <color:#FFFFFF><target></color> a <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>La tua modalità di gioco è stata impostata a <color:#FFFFFF><mode></color> da <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Modalità di gioco sconosciuta: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Uso: /gm <survival|creative|adventure|spectator> [giocatore]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Uso: /gms [giocatore]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Uso: /gmc [giocatore]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Uso: /gmsp [giocatore]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Uso: /gma [giocatore]</color>"
    },
    "id_id": {
        "gamemode.changed": "<prefix> <color:#FFF200>Mode permainan diubah ke <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Mode permainan <color:#FFFFFF><target></color> diatur ke <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Mode permainan Anda diatur ke <color:#FFFFFF><mode></color> oleh <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Mode permainan tidak diketahui: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Penggunaan: /gm <survival|creative|adventure|spectator> [pemain]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Penggunaan: /gms [pemain]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Penggunaan: /gmc [pemain]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Penggunaan: /gmsp [pemain]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Penggunaan: /gma [pemain]</color>"
    },
    "hi_in": {
        "gamemode.changed": "<prefix> <color:#FFF200>गेममोड <color:#FFFFFF><mode></mode></color> पर बदल दिया गया है</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> का गेममोड <color:#FFFFFF><mode></mode></color> पर सेट कर दिया गया है</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>आपका गेममोड <color:#FFFFFF><player></color> द्वारा <color:#FFFFFF><mode></mode></color> पर सेट किया गया है</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>अज्ञात गेममोड: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>उपयोग: /gm <survival|creative|adventure|spectator> [खिलाड़ी]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>उपयोग: /gms [खिलाड़ी]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>उपयोग: /gmc [खिलाड़ी]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>उपयोग: /gmsp [खिलाड़ी]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>उपयोग: /gma [खिलाड़ी]</color>"
    },
    "gu_in": {
        "gamemode.changed": "<prefix> <color:#FFF200>ગેમમોડ <color:#FFFFFF><mode></mode></color> માં બદલવામાં આવ્યો છે</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color> નો ગેમમોડ <color:#FFFFFF><mode></mode></color> માં સેટ કરવામાં આવ્યો છે</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>તમારો ગેમમોડ <color:#FFFFFF><player></color> દ્વારા <color:#FFFFFF><mode></mode></color> માં સેટ કરવામાં આવ્યો છે</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>અજ્ઞાત ગેમમોડ: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>ઉપયોગ: /gm <survival|creative|adventure|spectator> [ખેલાડી]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>ઉપયોગ: /gms [ખેલાડી]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>ઉપયોગ: /gmc [ખેલાડી]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>ઉપયોગ: /gmsp [ખેલાડી]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>ઉપયોગ: /gma [ખેલાડી]</color>"
    },
    "fr_fr": {
        "gamemode.changed": "<prefix> <color:#FFF200>Mode de jeu changé en <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Mode de jeu de <color:#FFFFFF><target></color> réglé sur <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Votre mode de jeu a été réglé sur <color:#FFFFFF><mode></mode></color> par <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Mode de jeu inconnu : <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Utilisation: /gm <survival|creative|adventure|spectator> [joueur]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Utilisation: /gms [joueur]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Utilisation: /gmc [joueur]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Utilisation: /gmsp [joueur]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Utilisation: /gma [joueur]</color>"
    },
    "fil_ph": {
        "gamemode.changed": "<prefix> <color:#FFF200>Ang gamemode ay binago sa <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Itinakda ang gamemode ni <color:#FFFFFF><target></color> sa <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Ang iyong gamemode ay itinakda sa <color:#FFFFFF><mode></color> ni <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Hindi kilalang gamemode: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Paggamit: /gm <survival|creative|adventure|spectator> [manlalaro]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Paggamit: /gms [manlalaro]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Paggamit: /gmc [manlalaro]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Paggamit: /gmsp [manlalaro]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Paggamit: /gma [manlalaro]</color>"
    },
    "es_es": {
        "gamemode.changed": "<prefix> <color:#FFF200>Modo de juego cambiado a <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Modo de juego de <color:#FFFFFF><target></color> establecido a <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Tu modo de juego fue establecido a <color:#FFFFFF><mode></color> por <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Modo de juego desconocido: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Uso: /gm <survival|creative|adventure|spectator> [jugador]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Uso: /gms [jugador]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Uso: /gmc [jugador]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Uso: /gmsp [jugador]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Uso: /gma [jugador]</color>"
    },
    "en_gb": {
        "gamemode.changed": "<prefix> <color:#FFF200>Gamemode changed to <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Set <color:#FFFFFF><target></color>'s gamemode to <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Your gamemode was set to <color:#FFFFFF><mode></color> by <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Unknown gamemode: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Usage: /gm <survival|creative|adventure|spectator> [player]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Usage: /gms [player]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Usage: /gmc [player]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Usage: /gmsp [player]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Usage: /gma [player]</color>"
    },
    "de_de": {
        "gamemode.changed": "<prefix> <color:#FFF200>Spielmodus geändert zu <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Spielmodus von <color:#FFFFFF><target></color> geändert zu <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Dein Spielmodus wurde von <color:#FFFFFF><player></color> zu <color:#FFFFFF><mode></color> geändert</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Unbekannter Spielmodus: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Nutzung: /gm <survival|creative|adventure|spectator> [spieler]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Nutzung: /gms [spieler]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Nutzung: /gmc [spieler]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Nutzung: /gmsp [spieler]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Nutzung: /gma [spieler]</color>"
    },
    "de_ch": {
        "gamemode.changed": "<prefix> <color:#FFF200>Spielmodus geändert zu <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Spielmodus von <color:#FFFFFF><target></color> geändert zu <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Dein Spielmodus wurde von <color:#FFFFFF><player></color> zu <color:#FFFFFF><mode></color> geändert</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Unbekannter Spielmodus: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Nutzung: /gm <survival|creative|adventure|spectator> [spieler]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Nutzung: /gms [spieler]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Nutzung: /gmc [spieler]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Nutzung: /gmsp [spieler]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Nutzung: /gma [spieler]</color>"
    },
    "bn_bd": {
        "gamemode.changed": "<prefix> <color:#FFF200>গেমমোড <color:#FFFFFF><mode></mode></color>-এ পরিবর্তন করা হয়েছে</color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200><color:#FFFFFF><target></color>-এর গেমমোড <color:#FFFFFF><mode></mode></color>-এ সেট করা হয়েছে</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>আপনার গেমমোড <color:#FFFFFF><player></color> দ্বারা <color:#FFFFFF><mode></mode></color>-এ সেট করা হয়েছে</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>অজানা গেমমোড: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>ব্যবহার: /gm <survival|creative|adventure|spectator> [খেলোয়াড়]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>ব্যবহার: /gms [খেলোয়াড়]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>ব্যবহার: /gmc [খেলোয়াড়]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>ব্যবহার: /gmsp [খেলোয়াড়]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>ব্যবহার: /gma [খেলোয়াড়]</color>"
    },
    "ar_sa": {
        "gamemode.changed": "<prefix> <color:#FFF200>تم تغيير نمط اللعبة إلى <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>تم تعيين نمط اللعبة لـ <color:#FFFFFF><target></color> إلى <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>تم تعيين نمط اللعبة الخاص بك إلى <color:#FFFFFF><mode></color> بواسطة <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>نمط لعبة غير معروف: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>الاستخدام: /gm <survival|creative|adventure|spectator> [لاعب]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>الاستخدام: /gms [لاعب]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>الاستخدام: /gmc [لاعب]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>الاستخدام: /gmsp [لاعب]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>الاستخدام: /gma [لاعب]</color>"
    },
    "ar_eg": {
        "gamemode.changed": "<prefix> <color:#FFF200>تم تغيير نمط اللعبة إلى <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>تم تعيين نمط اللعبة لـ <color:#FFFFFF><target></color> إلى <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>تم تعيين نمط اللعبة الخاص بك إلى <color:#FFFFFF><mode></color> بواسطة <color:#FFFFFF><player></color></color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>نمط لعبة غير معروف: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>الاستخدام: /gm <survival|creative|adventure|spectator> [لاعب]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>الاستخدام: /gms [لاعب]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>الاستخدام: /gmc [لاعب]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>الاستخدام: /gmsp [لاعب]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>الاستخدام: /gma [لاعب]</color>"
    },
    "lb_lu": {
        "gamemode.changed": "<prefix> <color:#FFF200>Spillmodus geännert op <color:#FFFFFF><mode></color></color>",
        "gamemode.changed.other": "<prefix> <color:#FFF200>Spillmodus vum <color:#FFFFFF><target></color> op <color:#FFFFFF><mode></color> gesat</color>",
        "gamemode.changed.by": "<prefix> <color:#FFF200>Äre Spillmodus gouf vum <color:#FFFFFF><player></color> op <color:#FFFFFF><mode></color> gesat</color>",
        "gamemode.invalid": "<prefix> <color:#FF0000>Onbekannte Spillmodus: <color:#FFFFFF><input></color></color>",
        "command.usage.gm": "<prefix> <color:#FF0000>Benotzung: /gm <survival|creative|adventure|spectator> [spiller]</color>",
        "command.usage.gms": "<prefix> <color:#FF0000>Benotzung: /gms [spiller]</color>",
        "command.usage.gmc": "<prefix> <color:#FF0000>Benotzung: /gmc [spiller]</color>",
        "command.usage.gmsp": "<prefix> <color:#FF0000>Benotzung: /gmsp [spiller]</color>",
        "command.usage.gma": "<prefix> <color:#FF0000>Benotzung: /gma [spiller]</color>"
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