#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.15 'Wed May 18 15:35:43 PDT 2016'
#
import matplotlib
import matplotlib.pyplot as plt
fig = plt.figure()
ax = plt.subplot(111)
N = 3
sizes = range(0,N)
tsql_noisy = [0.1827957,0.12292359,0.035439137]
ax.plot(sizes, tsql_noisy, label="tsql_noisy", marker='o')
sqlite_noisy = [0.17948718,0.12468828,0.055226825]
ax.plot(sizes, sqlite_noisy, label="sqlite_noisy", marker='o')
java = [0.05303761,0.018867925,0.013100437]
ax.plot(sizes, java, label="java", marker='o')
sqlite = [0.10973085,0.12468828,0.049295776]
ax.plot(sizes, sqlite, label="sqlite", marker='o')
java8 = [0.047131147,0.018348623,0.012048192]
ax.plot(sizes, java8, label="java8", marker='o')
quorum = [0.041800644,0.023255814,0.01631964]
ax.plot(sizes, quorum, label="quorum", marker='o')
antlr = [0.16694611,0.04863722,0.025252525]
ax.plot(sizes, antlr, label="antlr", marker='o')
tsql = [0.09504132,0.12292359,0.035439137]
ax.plot(sizes, tsql, label="tsql", marker='o')

ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)
xa = ax.get_xaxis()
xa.set_major_locator(matplotlib.ticker.MaxNLocator(integer=True))
ax.set_xlabel("Formatting Stage; stage 0 is first formatting pass")
ax.set_ylabel("Median Leave-one-out Validation Error Rate")
ax.set_title("3-Stage Formatting Stability\nStage $n$ is formatted output of stage $n-1$")
plt.legend()
plt.tight_layout()
fig.savefig('images/subset_validator.pdf', format='pdf')
plt.show()
