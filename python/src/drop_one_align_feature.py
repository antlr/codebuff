#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.8 'Tue May 10 19:00:59 PDT 2016'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 21
featureIndexes = range(0,N)
java = [0.0, 0.0, 0.0, 0.0, 0.0, 0.007936508, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.25, 0.25, 0.25, 0.2777778, 0.25, 0.31034482, 0.25, 0.25925925, 0.25, 0.25, 0.25, 0.24905661, 0.26037735, 0.25, 0.26923078, 0.25283018, 0.2413793, 0.25, 0.25283018, 0.25, 0.25]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.0, 0.0, 0.0, 0.0, 0.0, 0.013888889, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.037037037, 0.016200295, 0.037037037, 0.037037037, 0.037037037, 0.0694864, 0.037037037, 0.041666668, 0.041666668, 0.037037037, 0.037037037, 0.037037037, 0.040257648, 0.037037037, 0.030864198, 0.037037037, 0.037037037, 0.037037037, 0.040257648, 0.037037037, 0.037037037]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.19480519, 0.1875, 0.18994413, 0.23232323, 0.19480519, 0.22222222, 0.19480519, 0.18855219, 0.1875, 0.19553073, 0.19480519, 0.19480519, 0.19553073, 0.19480519, 0.19553073, 0.19480519, 0.17391305, 0.19480519, 0.19480519, 0.19480519, 0.19565217]
ax.plot(featureIndexes, tsql, label="tsql")

labels = ['curated', ' LT(-1)', 'LT(-1) right ancestor', ' LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', ' parent', 'parent child index', ' parent^2', 'parent^2 child index', ' parent^3', 'parent^3 child index', ' parent^4', 'parent^4 child index', ' parent^5', 'parent^5 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index']
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
