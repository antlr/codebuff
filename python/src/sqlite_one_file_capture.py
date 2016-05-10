#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Mon May 09 17:59:29 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_self = [0.08560067, 0.033540968, 0.0333843, 0.0023724793, 0.00591716, 0.060100168, 0.018469658, 0.043558605, 0.06932835, 0.0884177, 0.16550279, 0.054755043, 0.014715719, 0.017741935, 0.017509727, 0.007759457, 0.057683643, 0.0615855, 0.04228736, 0.012503223, 0.13290113, 0.14959115, 0.0016977929, 0.05945946, 0.056818184, 0.01718213, 0.021600394, 0.012269938, 0.07790143, 0.12109646, 0.22132555, 0.06468764, 0.047946952, 0.035450783, 0.062109813, 0.08371484]
sqlite_corpus = [0.12264546, 0.10477583, 0.03797146, 0.0023724793, 0.00591716, 0.09780776, 0.23553719, 0.040390708, 0.079948865, 0.10476031, 0.28421786, 0.19020173, 0.07514832, 0.042741936, 0.060311284, 0.16876818, 0.15998198, 0.10204082, 0.045891397, 0.057231244, 0.15397082, 0.15848966, 0.025811823, 0.15135135, 0.16145833, 0.03264605, 0.06859383, 0.012269938, 0.073131956, 0.23629424, 0.14138205, 0.055383254, 0.05559806, 0.05615781, 0.12787125, 0.18912685]
sqlite_diff = np.abs(np.subtract(sqlite_self, sqlite_corpus))

all = zip(sqlite_self, sqlite_corpus, sqlite_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
sqlite_self, sqlite_corpus, sqlite_diff, labels = zip(*all)

ax.plot(featureIndexes, sqlite_self, label="sqlite_self")
#ax.plot(featureIndexes, sqlite_corpus, label="sqlite_corpus")
ax.plot(featureIndexes, sqlite_diff, label="sqlite_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(sqlite_self),np.median(sqlite_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File sqlite $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/sqlite_one_file_capture.pdf", format='pdf')
plt.show()
