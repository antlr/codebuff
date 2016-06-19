#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 18 16:50:10 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_noisy_self = [0.042000484, 0.089952655, 0.10313076, 0.02, 0.011173184, 0.17874396, 0.010526316, 0.03900239, 0.053552605, 0.1551179, 0.071680374, 0.045490824, 0.00790798, 0.036731634, 0.13157895, 0.151357, 0.03359684, 0.08329699, 0.06020879, 0.06575026, 0.07061069, 0.0982958, 0.13344595, 0.082965575, 0.04957265, 0.009433962, 0.029060716, 0.01033295, 0.112651646, 0.111212395, 0.050165016, 0.0769962, 0.033333335, 0.031764038, 0.30019632, 0.024079321]
sqlite_noisy_corpus = [0.14073072, 0.22530864, 0.11832412, 0.092651755, 0.061452515, 0.17874396, 0.3195652, 0.055469558, 0.05386886, 0.09396665, 0.059859157, 0.2923674, 0.038102087, 0.12968516, 0.3110749, 0.2453027, 0.10737813, 0.07784562, 0.059480455, 0.16615689, 0.122137405, 0.069689594, 0.11402027, 0.21098725, 0.19670443, 0.050943397, 0.06953814, 0.012629162, 0.168447, 0.17319964, 0.06160616, 0.14624882, 0.08068395, 0.058990356, 0.33832902, 0.06853147]
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
