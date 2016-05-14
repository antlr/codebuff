#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.14 'Sat May 14 16:12:21 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_noisy_self = [0.4841343, 0.07627565, 0.10773481, 0.02, 0.011173184, 0.17874396, 0.010526316, 0.038737066, 0.05302551, 0.15537849, 0.072855465, 0.045490824, 0.00790798, 0.03898051, 0.13157895, 0.20668058, 0.03359684, 0.10793651, 0.055353243, 0.067047216, 0.07061069, 0.0982958, 0.13513513, 0.082965575, 0.04957265, 0.009433962, 0.029060716, 0.01033295, 0.112651646, 0.11394713, 0.071287125, 0.07747148, 0.035532996, 0.03195311, 0.24308406, 0.024079321]
sqlite_noisy_corpus = [0.14053689, 0.15307733, 0.12960406, 0.092651755, 0.061452515, 0.17874396, 0.3195652, 0.053085774, 0.052920092, 0.09471613, 0.060961314, 0.29099157, 0.0395399, 0.12968516, 0.2882736, 0.19311064, 0.10301837, 0.08460532, 0.059966013, 0.17651702, 0.099236645, 0.06908095, 0.11824324, 0.17833333, 0.19546157, 0.043396227, 0.06953814, 0.012629162, 0.17157713, 0.19234276, 0.07590759, 0.14591254, 0.08095111, 0.06200378, 0.3255976, 0.05077574]
sqlite_noisy_diff = np.abs(np.subtract(sqlite_noisy_self, sqlite_noisy_corpus))

all = zip(sqlite_noisy_self, sqlite_noisy_corpus, sqlite_noisy_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
sqlite_noisy_self, sqlite_noisy_corpus, sqlite_noisy_diff, labels = zip(*all)

ax.plot(featureIndexes, sqlite_noisy_self, label="sqlite_noisy_self")
#ax.plot(featureIndexes, sqlite_noisy_corpus, label="sqlite_noisy_corpus")
ax.plot(featureIndexes, sqlite_noisy_diff, label="sqlite_noisy_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(sqlite_noisy_self),np.median(sqlite_noisy_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File sqlite_noisy $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/sqlite_noisy_one_file_capture.pdf", format='pdf')
plt.show()
