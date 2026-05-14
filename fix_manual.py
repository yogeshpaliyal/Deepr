import re

file1 = "app/src/main/java/com/yogeshpaliyal/deepr/server/LocalServerRepositoryImpl.kt"
with open(file1, 'r') as f:
    content = f.read()

content = re.sub(r'<<<<<<< HEAD.*?=======\n(.*?)\n>>>>>>> feature-localized-web-interface', r'\1', content, flags=re.DOTALL)

with open(file1, 'w') as f:
    f.write(content)

file2 = "app/src/main/res/values-it/strings.xml"
with open(file2, 'r') as f:
    content = f.read()

content = re.sub(r'<<<<<<< HEAD\n(.*?)=======\n(.*?)\n>>>>>>> feature-localized-web-interface', r'\1\n\2', content, flags=re.DOTALL)
with open(file2, 'w') as f:
    f.write(content)
