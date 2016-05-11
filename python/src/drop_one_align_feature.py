#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.12 'Wed May 11 13:15:55 PDT 2016'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 18
featureIndexes = range(0,N)
java = [0.0, 0.0, 0.0, 0.043165468, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java, label="java")
sqlite = [0.2413793, 0.24691358, 0.25, 0.30769232, 0.2413793, 0.23076923, 0.26086956, 0.2413793, 0.2413793, 0.23529412, 0.23529412, 0.23529412, 0.2413793, 0.2413793, 0.23076923, 0.23913044, 0.2413793, 0.2413793]
ax.plot(featureIndexes, sqlite, label="sqlite")
java8 = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java8, label="java8")
antlr = [0.016200295, 0.016200295, 0.016200295, 0.041666668, 0.016200295, 0.023564065, 0.040257648, 0.016200295, 0.016200295, 0.016200295, 0.025, 0.016200295, 0.016200295, 0.016200295, 0.016200295, 0.016200295, 0.016200295, 0.016200295]
ax.plot(featureIndexes, antlr, label="antlr")
tsql = [0.17460318, 0.17391305, 0.22077923, 0.23569024, 0.17460318, 0.18855219, 0.17857143, 0.1875, 0.17460318, 0.17460318, 0.1875, 0.17391305, 0.17142858, 0.17460318, 0.17460318, 0.17460318, 0.17460318, 0.17391305]
ax.plot(featureIndexes, tsql, label="tsql")

labels = ['curated', 'LT(-1) right ancestor', ' LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', ' parent', 'parent child index', ' parent^2', 'parent^2 child index', ' parent^3', ' parent^4', 'parent^4 child index', ' parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index', 'LT(-1) right ancestor', 'LT(1)', 'Strt line', 'Big list', 'List elem.', 'token child index', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^4', 'parent^4 child index', 'parent^5', 'parent^5 child index']
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
