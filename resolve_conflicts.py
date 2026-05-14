import sys, re

files = [
    "app/src/main/java/com/yogeshpaliyal/deepr/server/LocalServerRepositoryImpl.kt",
    "app/src/main/res/values-de/strings.xml",
    "app/src/main/res/values-es/strings.xml",
    "app/src/main/res/values-et/strings.xml",
    "app/src/main/res/values-fr/strings.xml",
    "app/src/main/res/values-hi/strings.xml",
    "app/src/main/res/values-it/strings.xml",
    "app/src/main/res/values-ja/strings.xml",
    "app/src/main/res/values-pl/strings.xml",
    "app/src/main/res/values-ru/strings.xml",
    "app/src/main/res/values-ur/strings.xml",
    "app/src/main/res/values/strings.xml"
]

for file in files:
    try:
        with open(file, 'r') as f:
            content = f.read()
            
        if "LocalServerRepositoryImpl.kt" in file:
            content = re.sub(r'<<<<<<< HEAD.*?=======\n(.*?)\n>>>>>>> feature-localized-web-interface', r'\1', content, flags=re.DOTALL)
        else:
            def replacer(match):
                head_content = match.group(1).replace('</resources>', '')
                feature_content = match.group(2).replace('</resources>', '')
                return head_content + feature_content
                
            content = re.sub(r'<<<<<<< HEAD\n(.*?)=======\n(.*?)\n>>>>>>> feature-localized-web-interface', replacer, content, flags=re.DOTALL)
            
            # Remove all </resources> and append exactly one at the end
            content = content.replace('</resources>', '')
            content = content.strip() + '\n</resources>\n'
            
        with open(file, 'w') as f:
            f.write(content)
        print(f"Resolved {file}")
    except Exception as e:
        print(f"Error processing {file}: {e}")
