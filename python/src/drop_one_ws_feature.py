#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.2 'Mon May 02 11:43:48 PDT 2016'
#
import numpy as np
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 7
featureIndexes = range(0,N)
java = [0.04, 0.04779412, 0.04, 0.04, 0.04411765, 0.04, 0.03875969]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.082417585, 0.083333336, 0.07979626, 0.078125, 0.10306644, 0.08787129, 0.07967033]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.04, 0.04779412, 0.04, 0.04, 0.04411765, 0.04, 0.03875969]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.23021583, 0.26595744, 0.27579364, 0.23021583, 0.25179857, 0.2555746, 0.23021583]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.08184143, 0.084745765, 0.08439898, 0.08361204, 0.08713693, 0.08680851, 0.08361204]
ax.plot(featureIndexes, tsql, label="tsql")

labels = ['curated', ' LT(-1)', 'Strt line', 'LT(-1) right ancestor', ' LT(1)', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'List elem.', 'LT(1) left ancestor']
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
