#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.3 'Tue May 03 23:57:06 PDT 2016'
#
import numpy as np
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 18
featureIndexes = range(0,N)
java = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.051282052, 0.039215688, 0.05357143, 0.054054055, 0.051282052, 0.14473684, 0.043956045, 0.039215688, 0.051282052, 0.054054055, 0.04029304, 0.03448276, 0.0375, 0.04347826, 0.03846154, 0.02631579, 0.02631579, 0.04029304]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.041666668, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.058139537, 0.06382979, 0.058139537, 0.06521739, 0.058139537, 0.114285715, 0.05882353, 0.05263158, 0.06666667, 0.061452515, 0.058139537, 0.057142857, 0.0754717, 0.055555556, 0.058139537, 0.0754717, 0.069767445, 0.06382979]
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
