#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Mon May 09 17:59:35 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_noisy_self = [0.073255815, 0.05244216, 0.10313076, 0.02, 0.011173184, 0.17230274, 0.010526316, 0.047052603, 0.049968373, 0.08751846, 0.03172738, 0.04309657, 0.008626888, 0.025487257, 0.12247839, 0.1565762, 0.023715414, 0.08240582, 0.05729546, 0.025501, 0.07061069, 0.08794887, 0.07347973, 0.089143865, 0.046723645, 0.033962265, 0.018162947, 0.032731377, 0.14559068, 0.11440292, 0.03872387, 0.56251436, 0.14414854, 0.020041596, 0.23192444, 0.04203233]
tsql_noisy_corpus = [0.109985866, 0.16608825, 0.12200737, 0.07037643, 0.10614525, 0.17552336, 0.3340336, 0.062386747, 0.05028463, 0.10427206, 0.06815511, 0.2840024, 0.07953773, 0.11694153, 0.257329, 0.21503131, 0.0942029, 0.1106176, 0.064336, 0.057424765, 0.122137405, 0.08343195, 0.14273648, 0.17985013, 0.26104242, 0.09090909, 0.093233086, 0.064772725, 0.20481928, 0.18277119, 0.06534653, 0.20692399, 0.11194229, 0.09463608, 0.20274852, 0.15860656]
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
