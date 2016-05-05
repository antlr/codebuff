#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.4 'Thu May 05 10:12:25 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_noisy_self = [0.037144937, 0.08732246, 0.17150092, 0.021666666, 0.011173184, 0.16908212, 0.010526316, 0.036614485, 0.08644318, 0.13799888, 0.034077555, 0.059856344, 0.011502516, 0.07496252, 0.13157895, 0.18058455, 0.0256917, 0.122231685, 0.054867685, 0.06028614, 0.10114504, 0.09068777, 0.13006757, 0.05913504, 0.05014245, 0.028301887, 0.029579658, 0.01033295, 0.16213922, 0.12260711, 0.0730473, 0.07984791, 0.034464333, 0.022121385, 0.19818431, 0.02259887]
sqlite_noisy_corpus = [0.1940522, 0.14781694, 0.16436464, 0.022033898, 0.083798885, 0.17552336, 0.32845187, 0.11476202, 0.16563186, 0.24813649, 0.38876733, 0.2651279, 0.0625, 0.15967016, 0.32736155, 0.24634655, 0.10263158, 0.17155756, 0.060451567, 0.18658115, 0.110687025, 0.49189836, 0.11570946, 0.19533762, 0.26145554, 0.15306123, 0.089590885, 0.012629162, 0.19410746, 0.47204593, 0.10056967, 0.16967681, 0.17315802, 0.079394735, 0.53591865, 0.3107556]
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

ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File $f$ with Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/sqlite_noisy_one_file_capture.pdf", format='pdf')
plt.show()
