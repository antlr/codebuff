#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 18 16:50:22 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_noisy_self = [0.076227985, 0.050177395, 0.11878453, 0.02, 0.011173184, 0.17552336, 0.010526316, 0.028654816, 0.050495468, 0.09415402, 0.05640423, 0.045490824, 0.00790798, 0.03748126, 0.13157895, 0.1565762, 0.032279316, 0.07828173, 0.059966013, 0.026165765, 0.07442748, 0.10529519, 0.12922297, 0.089143865, 0.05014245, 0.01509434, 0.022739017, 0.03258427, 0.112651646, 0.09617138, 0.07216722, 0.09363118, 0.03537234, 0.012289658, 0.28449047, 0.04287046]
tsql_noisy_corpus = [0.08400097, 0.105207786, 0.12039595, 0.016949153, 0.06703911, 0.18035427, 0.31681034, 0.06707472, 0.05935062, 0.1066142, 0.055229142, 0.10428016, 0.023005033, 0.13868067, 0.257329, 0.2526096, 0.09486166, 0.1001306, 0.06457878, 0.058658116, 0.114503816, 0.057212416, 0.1714527, 0.18038237, 0.09441341, 0.035849057, 0.08885616, 0.030998852, 0.28156027, 0.18732908, 0.09658966, 0.25, 0.14528553, 0.078667864, 0.3151883, 0.08043956]
tsql_noisy_diff = np.abs(np.subtract(tsql_noisy_self, tsql_noisy_corpus))

all = zip(tsql_noisy_self, tsql_noisy_corpus, tsql_noisy_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
tsql_noisy_self, tsql_noisy_corpus, tsql_noisy_diff, labels = zip(*all)

ax.plot(featureIndexes, tsql_noisy_self, label="tsql_noisy_self")
#ax.plot(featureIndexes, tsql_noisy_corpus, label="tsql_noisy_corpus")
ax.plot(featureIndexes, tsql_noisy_diff, label="tsql_noisy_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(tsql_noisy_self),np.median(tsql_noisy_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File tsql_noisy $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/tsql_noisy_one_file_capture.pdf", format='pdf')
plt.show()
