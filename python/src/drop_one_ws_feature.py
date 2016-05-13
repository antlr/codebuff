#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.13 'Fri May 13 12:32:18 PDT 2016'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 8
featureIndexes = range(0,N)
java = [0.05252101, 0.06, 0.05252101, 0.050041016, 0.05882353, 0.048039217, 0.05252101, 0.05252101]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.09365244, 0.09885536, 0.08324662, 0.09428571, 0.09714286, 0.08974359, 0.09428571, 0.09428571]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.04628331, 0.047685836, 0.045769766, 0.04628331, 0.04628331, 0.047685836, 0.04628331, 0.04628331]
ax.plot(featureIndexes, java8, label="java8")
quorum = [0.027692307, 0.029860651, 0.027027028, 0.026755853, 0.038424592, 0.028037382, 0.027692307, 0.029835392]
ax.plot(featureIndexes, quorum, label="quorum")
antlr = [0.20212767, 0.18502203, 0.20575744, 0.20212767, 0.19309174, 0.21909109, 0.20212767, 0.20212767]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.088607594, 0.08450704, 0.088607594, 0.096045196, 0.09113924, 0.08101266, 0.09367089, 0.09765625]
ax.plot(featureIndexes, tsql, label="tsql")

labels = ['curated', ' LT(-1)', 'Strt line', 'LT(-1) right ancestor', ' LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor']
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.set_xlabel("Inject Whitespace Feature")
ax.set_ylabel("Median Error rate")
ax.set_title("Effect of Dropping One Feature on Whitespace Decision\nMedian Leave-one-out Validation Error Rate")
plt.legend()
plt.tight_layout()
fig.savefig("images/drop_one_ws_feature.pdf", format='pdf')
plt.show()
