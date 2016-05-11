#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.12 'Wed May 11 13:22:24 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_noisy_self = [0.046370476, 0.108364016, 0.13098526, 0.021666666, 0.011173184, 0.17230274, 0.010526316, 0.03926771, 0.05249842, 0.102492034, 0.035252646, 0.060654428, 0.02588066, 0.044977512, 0.12247839, 0.20668058, 0.0256917, 0.1743346, 0.056809906, 0.1325111, 0.104961835, 0.05569081, 0.07094595, 0.082965575, 0.052991454, 0.028301887, 0.029579658, 0.01033295, 0.16213922, 0.121239744, 0.060066007, 0.08460076, 0.03980764, 0.03195311, 0.20037486, 0.02259887]
sqlite_noisy_corpus = [0.24305004, 0.15781167, 0.1424954, 0.022033898, 0.061452515, 0.17552336, 0.28515625, 0.10266439, 0.16691616, 0.23998, 0.38921282, 0.289288, 0.116498314, 0.15292354, 0.5179153, 0.26617953, 0.10536649, 0.16690826, 0.06506433, 0.1981253, 0.110687025, 0.2981409, 0.11824324, 0.15963607, 0.26258823, 0.14650767, 0.14637682, 0.058114037, 0.19410746, 0.45858267, 0.086468644, 0.1506654, 0.17465416, 0.0711344, 0.536683, 0.3031768]
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
