#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Fri Jun 17 15:30:29 PDT 2016'
#
import numpy as np
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 3
sizes = range(1,N+1)
sqlite = [0.39566395,0.19639066,0.1870229]
ax.plot(range(1,len(sqlite)+1), sqlite, label="sqlite", marker='o')
antlr = [0.23529412,0.11906425,0.22540188]
ax.plot(range(1,len(antlr)+1), antlr, label="antlr", marker='o')
java_st = [0.1372315,0.07272727,0.06632213]
ax.plot(range(1,len(java_st)+1), java_st, label="java_st", marker='o')
java8_st = [0.2593828,0.06481481,0.0754717]
ax.plot(range(1,len(java8_st)+1), java8_st, label="java8_st", marker='o')
tsql = [0.23404256,0.24701196,0.13541667]
ax.plot(range(1,len(tsql)+1), tsql, label="tsql", marker='o')

ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)
ax.set_xlabel("Number n of training files in sample subset corpus", fontsize=14)
ax.set_ylabel("Median Error rate for 5 trials", fontsize=14)
ax.set_title("Effect of Corpus size on Median Leave-one-out Validation Error Rate")
plt.legend()
plt.tight_layout()
fig.savefig('images/subset_validator.pdf', format='pdf')
plt.show()
