#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Mon May 09 17:58:29 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["C.g4", "Clojure.g4", "Java.g4", "Java8.g4", "JSON.g4", "MASM.g4", "Smalltalk.g4", "SQLite.g4", "T.g4", "VisualBasic6.g4", "XMLLexer.g4", "XMLParser.g4"]
N = len(labels)

featureIndexes = range(0,N)
antlr_self = [0.028172765, 0.08945455, 0.034707565, 0.02126827, 0.02508179, 0.031887088, 0.02532274, 0.08664244, 0.05, 0.09073774, 0.058894232, 0.014465703]
antlr_corpus = [0.036551576, 0.10244233, 0.074480556, 0.031339988, 0.087241, 0.09940756, 0.130425, 0.14491574, 0.37931034, 0.13312672, 0.12920673, 0.025056947]
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

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(antlr_self),np.median(antlr_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File antlr $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/antlr_one_file_capture.pdf", format='pdf')
plt.show()
