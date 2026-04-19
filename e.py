import re
import os

# Pattern for one-line methods with a return
METHOD_PATTERN = re.compile(
    r'\b(public|private|protected)\s+[\w<>\[\]]+\s+\w+\s*\([^)]*\)\s*\{\s*return\s+[^;]+;\s*\}'
)

def scan_file(filepath):
    results = []
    with open(filepath, "r", encoding="utf-8") as f:
        for i, line in enumerate(f, start=1):
            if METHOD_PATTERN.search(line):
                results.append((i, line.strip()))
    return results


def scan_directory(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                path = os.path.join(root, file)
                matches = scan_file(path)

                if matches:
                    print(f"\n📄 {path}")
                    for line_num, content in matches:
                        print(f"  Line {line_num}: {content}")


if __name__ == "__main__":
    # Change this to your project directory
    target_dir = "."

    scan_directory(target_dir)
