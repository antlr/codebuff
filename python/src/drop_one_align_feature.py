#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.13 'Fri May 13 12:46:16 PDT 2016'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 19
featureIndexes = range(0,N)
java = [0.0, 0.0, 0.0, 0.042016808, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.25, 0.25, 0.2777778, 0.31034482, 0.25283018, 0.25925925, 0.25, 0.25, 0.25, 0.25, 0.26792452, 0.25, 0.26923078, 0.25283018, 0.2413793, 0.25, 0.25283018, 0.25, 0.25660378]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.0, 0.0, 0.0, 0.014814815, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java8, label="java8")
quorum = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.10526316, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, quorum, label="quorum")
antlr = [0.016200295, 0.016200295, 0.016200295, 0.041666668, 0.016200295, 0.02503682, 0.040257648, 0.016200295, 0.016200295, 0.016200295, 0.025, 0.016200295, 0.016200295, 0.016200295, 0.016200295, 0.016200295, 0.016200295, 0.016200295, 0.016200295]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.1875, 0.17460318, 0.22222222, 0.23569024, 0.1875, 0.18855219, 0.1875, 0.19865319, 0.1875, 0.1875, 0.1923077, 0.1875, 0.17391305, 0.1875, 0.17460318, 0.1875, 0.1875, 0.1875, 0.18855219]
ax.plot(featureIndexes, tsql, label="tsql")

labels = ['curated', 'LT(-1) right ancestor', ' LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', ' parent', 'parent child index', ' parent^2', 'parent^2 child index', ' parent^3', 'parent^3 child index', ' parent^4', 'parent^4 child index', ' parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index']
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
