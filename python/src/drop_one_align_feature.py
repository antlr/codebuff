#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.2 'Sat Apr 30 11:27:32 PDT 2016'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 18
featureIndexes = range(0,N)
java = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
ax.plot(featureIndexes, java, label="java")

labels = ['curated', ' LT(-1)', 'LT(-1) right ancestor', ' LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', ' parent', 'parent child index', ' parent^2', 'parent^2 child index', ' parent^3', 'parent^3 child index', ' parent^4', 'parent^4 child index', 'LT(-1)', 'LT(-1) right ancestor', 'LT(1)', 'Pair dif\n', 'Strt line', 'Big list', 'List elem.', 'LT(1) left ancestor', 'ancestor child index', 'parent', 'parent child index', 'parent^2', 'parent^2 child index', 'parent^3', 'parent^3 child index', 'parent^4', 'parent^4 child index']
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.set_xlabel("Inject Whitespace Feature")
ax.set_ylabel("Median Error rate")
ax.set_title("Effect of Dropping One Feature on Whitespace Decision\nMedian Leave-one-out Validation Error Rate")
plt.legend()
plt.tight_layout()
plt.show()
