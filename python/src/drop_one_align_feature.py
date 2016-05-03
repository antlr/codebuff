#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.2 'Tue May 03 13:15:34 PDT 2016'
#
import numpy as np
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 18
featureIndexes = range(0,N)
java = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.051282052, 0.04761905, 0.04761905, 0.04, 0.051282052, 0.13513513, 0.04347826, 0.054054055, 0.05172414, 0.051282052, 0.04761905, 0.051282052, 0.04, 0.04347826, 0.04761905, 0.05140187, 0.03797468, 0.03846154]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.041666668, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.054054055, 0.055555556, 0.057692308, 0.048387095, 0.054054055, 0.13461539, 0.054054055, 0.038961038, 0.054054055, 0.054054055, 0.057692308, 0.054054055, 0.057692308, 0.057692308, 0.055555556, 0.048387095, 0.057142857, 0.055555556]
ax.plot(featureIndexes, tsql, label="tsql")

labels = ['curated', ' LT(-1)', 'LT(-1) right ancestor', ' LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', ' parent', 'parent child index', ' parent^2', 'parent^2 child index', ' parent^3', 'parent^3 child index', ' parent^4', 'parent^4 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index']
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.set_xlabel("Alignment Feature")
ax.set_ylabel("Median Error rate")
ax.set_title("Effect of Dropping One Feature on Alignment Decision\nMedian Leave-one-out Validation Error Rate")
plt.legend()
plt.tight_layout()
fig.savefig("images/drop_one_align_feature.pdf", format='pdf')
plt.show()
