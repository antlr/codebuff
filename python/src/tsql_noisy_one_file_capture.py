#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.14 'Sat May 14 16:12:34 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_noisy_self = [0.076227985, 0.050177395, 0.113950275, 0.02, 0.011173184, 0.17552336, 0.010526316, 0.04139029, 0.050495468, 0.09078134, 0.05640423, 0.045490824, 0.00790798, 0.03748126, 0.13157895, 0.1565762, 0.032279316, 0.09180114, 0.058752123, 0.026165765, 0.07442748, 0.105903834, 0.1258446, 0.089143865, 0.05014245, 0.01509434, 0.031136481, 0.03258427, 0.112651646, 0.09708295, 0.07238724, 0.09553232, 0.03537234, 0.020797882, 0.28449047, 0.04287046]
tsql_noisy_corpus = [0.13265073, 0.15465544, 0.12131676, 0.022033898, 0.061452515, 0.18196458, 0.31681034, 0.06453278, 0.06029939, 0.16860062, 0.059929494, 0.2625387, 0.05362117, 0.13868067, 0.257329, 0.26409185, 0.09486166, 0.115821026, 0.06457878, 0.058658116, 0.122137405, 0.056603774, 0.17483108, 0.17901748, 0.22862454, 0.035849057, 0.088455774, 0.030998852, 0.19876733, 0.17411122, 0.15128152, 0.17702703, 0.14312367, 0.08527273, 0.30501518, 0.08877053]
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
