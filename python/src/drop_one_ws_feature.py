#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.3 'Tue May 03 23:16:10 PDT 2016'
#
import numpy as np
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 8
featureIndexes = range(0,N)
java = [0.05541237, 0.06451613, 0.05541237, 0.054545455, 0.05757576, 0.04, 0.05541237, 0.057843138]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.10169491, 0.11640212, 0.10152284, 0.107344635, 0.121212125, 0.08571429, 0.10897436, 0.10186757]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.042056076, 0.04699739, 0.042056076, 0.042056076, 0.042821158, 0.039719626, 0.042056076, 0.042821158]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.29331046, 0.27444252, 0.3053173, 0.29331046, 0.29331046, 0.27916196, 0.29331046, 0.29331046]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.08450704, 0.08203125, 0.0859375, 0.08857143, 0.09859155, 0.077922076, 0.08450704, 0.088607594]
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
