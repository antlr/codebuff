#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.4 'Thu May 05 10:06:10 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["C.g4", "Clojure.g4", "Java.g4", "Java8.g4", "JSON.g4", "MASM.g4", "Smalltalk.g4", "SQLite.g4", "T.g4", "VisualBasic6.g4", "XMLLexer.g4", "XMLParser.g4"]
N = len(labels)

featureIndexes = range(0,N)
antlr_self = [0.032107882, 0.11701346, 0.040242016, 0.022305975, 0.029411765, 0.031887088, 0.0151011, 0.097505875, 0.05, 0.096088246, 0.061298076, 0.014465703]
antlr_corpus = [0.06858336, 0.1283673, 0.29567, 0.05208625, 0.17884405, 0.13199164, 0.17451772, 0.3909937, 0.37931034, 0.1368976, 0.12259615, 0.0658418]
antlr_diff = np.abs(np.subtract(antlr_self, antlr_corpus))

all = zip(antlr_self, antlr_corpus, antlr_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
antlr_self, antlr_corpus, antlr_diff, labels = zip(*all)

ax.plot(featureIndexes, antlr_self, label="antlr_self")
#ax.plot(featureIndexes, antlr_corpus, label="antlr_corpus")
ax.plot(featureIndexes, antlr_diff, label="antlr_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File $f$ with Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/antlr_one_file_capture.pdf", format='pdf')
plt.show()
