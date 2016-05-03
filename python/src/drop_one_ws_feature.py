#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.2 'Tue May 03 12:48:54 PDT 2016'
#
import numpy as np
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 8
featureIndexes = range(0,N)
java = [0.054545455, 0.056737587, 0.054347824, 0.054347824, 0.058132343, 0.04, 0.054545455, 0.05473098]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.09677419, 0.099585064, 0.09128631, 0.0950764, 0.10696095, 0.082417585, 0.10074627, 0.09405941]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.054545455, 0.056737587, 0.054347824, 0.054347824, 0.058132343, 0.04, 0.054545455, 0.05473098]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.28987992, 0.27787307, 0.31389365, 0.28987992, 0.2915952, 0.23021583, 0.2915952, 0.28987992]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.08713693, 0.08984375, 0.09293681, 0.09071274, 0.097402595, 0.08184143, 0.08713693, 0.08983051]
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
