#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.2 'Sat Apr 30 11:09:20 PDT 2016'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 7
featureIndexes = range(0,N)
java = [0.04, 0.04779412, 0.04, 0.04, 0.04411765, 0.04, 0.03875969]
ax.plot(featureIndexes, java, label="java")
antlr = [0.23021583, 0.26595744, 0.27579364, 0.23021583, 0.25179857, 0.2555746, 0.23021583]
ax.plot(featureIndexes, antlr, label="antlr")

labels = ['curated', ' LT(-1)', 'Strt line', 'LT(-1) right ancestor', ' LT(1)', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'List elem.', 'LT(1) left ancestor', 'LT(-1)', 'Strt line', 'LT(-1) right ancestor', 'LT(1)', 'List elem.', 'LT(1) left ancestor']
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.set_xlabel("Inject Whitespace Feature")
ax.set_ylabel("Median Error rate")
ax.set_title("Effect of Dropping One Feature on Whitespace Decision\nMedian Leave-one-out Validation Error Rate")
plt.legend()
plt.tight_layout()
plt.show()
