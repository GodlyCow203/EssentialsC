# If anyone is wondering what this is,
# it helps me insert/add new translation keys much faster
#
# "tiu" stands for "Translation Keys Insertion Util" btw
#
# The only annoying part is that i have to ask AI to generate the translated
# keys in a very specific layout:

# "<lang>_<LANG>": {                                                                 # use this as an example prompt for the ai if needed
#     "random.translation.key": "<prefix> <color:#FFF200>text text text</color>",
#     "translated.key": "<prefix> <color:#FFF200>Hello World :)</color>"
# },

# etc.
#
# So, if you fork EssentialsC and your changes include new translation keys,
# consider using this to speed up the process

import json
from pathlib import Path

INSERT_AFTER = '  "back.success"'# insert message *behind

# New translation keys
NEW_TRANSLATIONS = {
    "en_us": {
        "dback.success_unsafe": "<prefix> <color:#FFF200>Teleported to your unsafe death location.</color>",
        "back.success_unsafe": "<prefix> <color:#FFF200>Teleported to your unsafe location.</color>"
    }
}

current_folder = Path(".")

for lang_code, translations in NEW_TRANSLATIONS.items():

    file_path = current_folder / f"{lang_code}.json"

    if not file_path.exists():
        print(f"[SKIPPED] {file_path.name} not found") # skip if the file cannot be found
        continue

    with open(file_path, "r", encoding="utf-8") as file:
        lines = file.readlines()

    insert_index = None

    for i, line in enumerate(lines):
        if INSERT_AFTER in line:
            insert_index = i + 1


            break

    if insert_index is None:
        print(f"[SKIPPED] Key not found in {file_path.name}") #log if INSERT_AFTER is not found

        continue

    existing_content = "".join(lines)

    new_lines = []

    for key, value in translations.items():

        # check if key exists, if yes, replace
        if f'"{key}"' in existing_content:
            print(f"[OVERWRITE] {key} already exists in {file_path.name},  replacing...") #log

            lines = [line for line in lines if f'"{key}"' not in line]
            for j, line in enumerate (lines):

                if INSERT_AFTER in line:
                    insert_index = j + 1
                    break


        escaped_value = value.replace('"', '\"')

        new_lines.append(

            f'  "{key}": "{escaped_value}",\n'
        )

    if not new_lines: # if no lines, changed, log
        print(f"[NO CHANGES] {file_path.name}")
        continue

    lines[insert_index:insert_index] = new_lines

    with open(file_path, "w", encoding="utf-8") as file:

        file.writelines(lines)

    print(f"[UPDATED] {file_path.name}") #log updated files

print("Done.")