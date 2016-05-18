#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.15 'Wed May 18 15:45:53 PDT 2016'
#
import matplotlib
import matplotlib.pyplot as plt
fig = plt.figure()
ax = plt.subplot(111)
N = 11
sizes = range(0,N)
tsql_noisy = [0.1827957,0.12292359,0.035439137,0.031007752,0.031007752,0.04037267,0.025188917,0.02293578,0.023728814,0.022727273,0.02020202]
ax.plot(sizes, tsql_noisy, label="tsql_noisy", marker='o')
sqlite_noisy = [0.17948718,0.12468828,0.046875,0.029985007,0.050691243,0.0375817,0.03137255,0.028037382,0.025974026,0.025974026,0.025974026]
ax.plot(sizes, sqlite_noisy, label="sqlite_noisy", marker='o')
java = [0.05303761,0.018867925,0.013100437,0.010752688,0.011130137,0.012048192,0.011463845,0.011130137,0.012658228,0.012607626,0.013100437]
ax.plot(sizes, java, label="java", marker='o')
sqlite = [0.10973085,0.12468828,0.046875,0.029985007,0.029007634,0.036585364,0.036923077,0.035276074,0.030927835,0.029023746,0.028455285]
ax.plot(sizes, sqlite, label="sqlite", marker='o')
java8 = [0.047131147,0.018348623,0.012048192,0.009417809,0.009259259,0.009345794,0.010784314,0.009427121,0.0091743115,0.010989011,0.009417809]
ax.plot(sizes, java8, label="java8", marker='o')
quorum = [0.041800644,0.023255814,0.01631964,0.015075377,0.013040494,0.011494253,0.012861736,0.011494253,0.012861736,0.013333334,0.012861736]
ax.plot(sizes, quorum, label="quorum", marker='o')
antlr = [0.16694611,0.04863722,0.025252525,0.03448276,0.022988506,0.03539823,0.044247787,0.028735632,0.022988506,0.023362696,0.023745691]
ax.plot(sizes, antlr, label="antlr", marker='o')
tsql = [0.09504132,0.12292359,0.035439137,0.031007752,0.022421524,0.02739726,0.02739726,0.018376723,0.02008032,0.023655914,0.018691588]
ax.plot(sizes, tsql, label="tsql", marker='o')

ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)
xa = ax.get_xaxis()
xa.set_major_locator(matplotlib.ticker.MaxNLocator(integer=True))
ax.set_xlabel("Formatting Stage; stage 0 is first formatting pass")
ax.set_ylabel("Median Leave-one-out Validation Error Rate")
ax.set_title("11-Stage Formatting Stability\nStage $n$ trained on $n-1$")
plt.legend()
plt.tight_layout()
fig.savefig('images/stability.pdf', format='pdf')
plt.show()
