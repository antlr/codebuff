#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.12 'Wed May 11 13:21:49 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["C.g4", "Clojure.g4", "Java.g4", "Java8.g4", "JSON.g4", "MASM.g4", "Smalltalk.g4", "SQLite.g4", "T.g4", "VisualBasic6.g4", "XMLLexer.g4", "XMLParser.g4"]
N = len(labels)

featureIndexes = range(0,N)
antlr_self = [0.028906908, 0.09132311, 0.036081992, 0.022040794, 0.029475983, 0.011341311, 0.025335321, 0.08369506, 0.05, 0.08363157, 0.03434022, 0.014465703]
antlr_corpus = [0.03472812, 0.09713124, 0.0759079, 0.03148903, 0.073224045, 0.05322379, 0.12447022, 0.124045104, 0.37931034, 0.12202528, 0.06855867, 0.026387624]
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
