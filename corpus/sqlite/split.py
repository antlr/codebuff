import sys

filename = sys.argv[1]
f = open(filename)
text = f.read()
f.close()
for line in text.split("\n"):
    print line