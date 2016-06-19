#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 18 16:50:16 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_self = [0.11657597, 0.014002897, 0.040142275, 0.0011862396, 0.00591716, 0.06322795, 0.018469658, 0.042454123, 0.04212904, 0.07337002, 0.036486488, 0.08184438, 0.014715719, 0.02764613, 0.04780115, 0.011639185, 0.043615106, 0.0497807, 0.04228736, 0.021525823, 0.12567325, 0.10623229, 0.008488964, 0.05945946, 0.056818184, 0.01718213, 0.01963672, 0.012269938, 0.0015898251, 0.14559124, 0.060365368, 0.018165708, 0.024222335, 0.041209284, 0.22858973, 0.061799552]
sqlite_corpus = [0.14064462, 0.06335282, 0.05606524, 0.0059101656, 0.00591716, 0.07419899, 0.27021697, 0.046288688, 0.059609152, 0.10405203, 0.32402235, 0.1371758, 0.05450875, 0.076619275, 0.04206501, 0.10087294, 0.20684993, 0.110826395, 0.05069678, 0.11356203, 0.14362657, 0.14140698, 0.021026073, 0.12509653, 0.12394761, 0.0395189, 0.050314464, 0.024242423, 0.0190779, 0.20027341, 0.110175975, 0.01683651, 0.031624585, 0.031317107, 0.07699696, 0.11760884]
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
