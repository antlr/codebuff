#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.12 'Wed May 11 16:16:45 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_noisy_self = [0.076227985, 0.05387378, 0.10865562, 0.02, 0.011173184, 0.17552336, 0.010526316, 0.049538866, 0.051127978, 0.10293189, 0.032902468, 0.045490824, 0.023005033, 0.03748126, 0.12247839, 0.1565762, 0.023715414, 0.08519702, 0.062393785, 0.026450388, 0.07442748, 0.0897748, 0.07516892, 0.089143865, 0.05014245, 0.033962265, 0.031136481, 0.03258427, 0.14559068, 0.100729264, 0.029482948, 0.5631157, 0.14831023, 0.021554168, 0.23334879, 0.04203233]
tsql_noisy_corpus = [0.12396122, 0.16675012, 0.12062615, 0.07026144, 0.10614525, 0.17713365, 0.3368201, 0.06404959, 0.050600886, 0.09902567, 0.0693302, 0.2832231, 0.06661942, 0.11994003, 0.257329, 0.21085595, 0.09354414, 0.117023535, 0.06457878, 0.056191415, 0.099236645, 0.08180202, 0.1402027, 0.17886855, 0.24456522, 0.08695652, 0.094076656, 0.06447964, 0.20056497, 0.16180493, 0.082068205, 0.21203667, 0.11060646, 0.096409336, 0.27174827, 0.14890087]
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
