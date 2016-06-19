#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 18 16:50:03 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["C.g4", "Clojure.g4", "Java.g4", "Java8.g4", "JSON.g4", "MASM.g4", "Smalltalk.g4", "SQLite.g4", "T.g4", "VisualBasic6.g4", "XMLLexer.g4", "XMLParser.g4"]
N = len(labels)

featureIndexes = range(0,N)
antlr_self = [0.019541206, 0.094120495, 0.028079372, 0.006559583, 0.011904762, 0.008215752, 0.031296574, 0.08848672, 0.05, 0.08849395, 0.043825977, 0.006999533]
antlr_corpus = [0.027665675, 0.0892776, 0.05877948, 0.014419165, 0.0852459, 0.048490804, 0.11419325, 0.12763733, 0.37931034, 0.11046449, 0.0678183, 0.024601366]
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
