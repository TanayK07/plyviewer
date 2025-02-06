import os

def save_files_with_contents(output_file="file_contents.txt"):
    with open(output_file, "w", encoding="utf-8") as out:
        for filename in os.listdir():
            if os.path.isfile(filename):  # Ensure it's a file, not a folder
                out.write(f"=== FILE: {filename} ===\n")
                try:
                    with open(filename, "r", encoding="utf-8", errors="ignore") as f:
                        content = f.read()
                        out.write(content + "\n\n")
                except Exception as e:
                    out.write(f"[Error reading file: {e}]\n\n")
    print(f"✅ File contents saved in '{output_file}'")

# Run the function
save_files_with_contents()
