#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Fri May 06 18:38:19 PDT 2016'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 8
featureIndexes = range(0,N)
java = [0.04255319, 0.05462185, 0.04255319, 0.042071197, 0.04675964, 0.04, 0.04255319, 0.04255319]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.08, 0.09378186, 0.07979626, 0.08787129, 0.09633028, 0.08571429, 0.08791209, 0.08516484]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.037549406, 0.04255319, 0.037815128, 0.037549406, 0.036465637, 0.039719626, 0.037549406, 0.04]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.23849206, 0.2212693, 0.23670635, 0.2359127, 0.22302158, 0.23021583, 0.23888889, 0.23869048]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.08450704, 0.08203125, 0.0859375, 0.084745765, 0.094915256, 0.077922076, 0.08450704, 0.088607594]
ax.plot(featureIndexes, tsql, label="tsql")

labels = ['curated', ' LT(-1)', 'Strt line', 'LT(-1) right ancestor', ' LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'Big list', 'List elem.', 'LT(1) left ancestor']
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
