#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 18 17:27:26 PDT 2016'
#
import matplotlib
import matplotlib.pyplot as plt
fig = plt.figure()
ax = plt.subplot(111)
N = 6
sizes = range(0,N)
quorum = [0.041800644,0.8050103,0.8150558,0.80688065,0.8070041,0.8075207]
ax.plot(sizes, quorum, label="quorum", marker='o')

ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)
xa = ax.get_xaxis()
xa.set_major_locator(matplotlib.ticker.MaxNLocator(integer=True))
ax.set_xlabel("Formatting Stage; stage 0 is first formatting pass")
ax.set_ylabel("Median Leave-one-out Validation Error Rate")
ax.set_title("6-Stage Formatting Stability\nStage $n$ is formatted output of stage $n-1$")
plt.legend()
plt.tight_layout()
fig.savefig('images/stability.pdf', format='pdf')
plt.show()
