#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff %s '%s'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 14
sizes = range(1,N+1)
java = [0.18353401,0.11111111,0.113604486,0.12857144,0.08180943,0.084033616,0.13432837,0.12184874,0.08092485,0.14470842,0.08943089,0.08983452,0.088913284,0.080684595]
ax.plot(sizes, java, label="java", marker='o')
sqlite = [0.29944134,0.2112676,0.17989418,0.15789473,0.18181819,0.18142548,0.16803278,0.17037037,0.19185059,0.171875,0.15298508,0.17032968,0.14261168,0.1388889]
ax.plot(sizes, sqlite, label="sqlite", marker='o')
java8 = [0.20027816,0.12716049,0.09756097,0.09887279,0.09512761,0.1252588,0.10238429,0.08,0.07550336,0.10144927,0.1097561,0.09095998,0.08421053,0.11086718]
ax.plot(sizes, java8, label="java8", marker='o')
antlr = [0.4893617,0.27771226,0.29787233,0.22496264,0.2779661,0.21269296,0.27272728,0.23253968,0.2086331,0.17270668,0.2323335,0.17021276,0.0,0.0]
ax.plot(sizes, antlr, label="antlr", marker='o')
tsql = [0.29523227,0.20942408,0.20192307,0.16402116,0.17580645,0.18045112,0.16317229,0.14329268,0.14697406,0.16889632,0.14929578,0.14851485,0.16356878,0.1576087]
ax.plot(sizes, tsql, label="tsql", marker='o')

ax.set_xlabel("Number n of training files in sample subset corpus")
ax.set_ylabel("Median Error rate for 30 trials")
ax.set_title("Effect of Corpus size on Median Leave-one-out Validation Error Rate")
plt.legend()
plt.show()
