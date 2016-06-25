#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 25 14:53:31 PDT 2016'
#
import numpy as np
import pylab
import matplotlib.pyplot as plt

java_st_err = [0.027777778, 0.0623608, 0.028478438, 0.05620438, 0.0583691, 0.05496625, 0.08366534, 0.183, 0.11627907, 0.025316456, 0.013661202, 0.053435113, 0.22580644, 0.15625, 0.018867925, 0.015873017, 0.10743801, 0.026402641, 0.023002421, 0.035958905, 0.06918239, 0.046296295, 0.028037382, 0.11784512, 0.072289154, 0.021052632, 0.14925373, 0.01843318, 0.07917059, 0.14925373, 0.036, 0.21081081, 0.009708738, 0.07159353, 0.12662722, 0.0375, 0.053097345, 0.09047619, 0.033088237, 0.05376344, 0.009433962, 0.05352798, 0.0, 0.0, 0.054545455, 0.053030305, 0.018348623, 0.07090464, 0.05263158, 0.056986302, 0.024449877, 0.054247696, 0.04506699, 0.024922118, 0.059734512, 0.12195122, 0.0415625, 0.047557004, 0.061060857]

language_data = [java_st_err]
labels = ["java_st\nn=59"]
fig = plt.figure()
ax = plt.subplot(111)
ax.boxplot(language_data,
           whis=[10, 90], # 10 and 90 % whiskers
           widths=.35,
           labels=labels,
           showfliers=False)
ax.set_xticklabels(labels, rotation=60, fontsize=18)
ax.tick_params(axis='both', which='major', labelsize=18)
plt.xticks(range(1,len(labels)+1), labels, rotation=60, fontsize=18)
pylab.ylim([0,.28])
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)
ax.set_xlabel("Grammar and corpus size", fontsize=20)
ax.set_ylabel("Misclassification Error Rate", fontsize=20)
# ax.set_title("Leave-one-out Validation Using Error Rate\nBetween Formatted and Original File")
plt.tight_layout()
fig.savefig('images/leave_one_out.pdf', format='pdf')
plt.show()
