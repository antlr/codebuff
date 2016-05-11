#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.12 'Wed May 11 13:22:56 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_self = [0.08580996, 0.03875598, 0.034913354, 0.0023724793, 0.00591716, 0.06322795, 0.018469658, 0.045406546, 0.07090176, 0.08925578, 0.16620111, 0.05648415, 0.028320972, 0.029032258, 0.017509727, 0.007759457, 0.057683643, 0.06551649, 0.044209514, 0.013792215, 0.13614263, 0.14790764, 0.0033898305, 0.07181467, 0.05350379, 0.01718213, 0.021600394, 0.012269938, 0.07790143, 0.121790424, 0.27969947, 0.0691183, 0.05228258, 0.041209284, 0.061789658, 0.08371484]
sqlite_corpus = [0.12390121, 0.10526316, 0.054655872, 0.0023724793, 0.00591716, 0.09780776, 0.21940929, 0.04276663, 0.0833907, 0.108531676, 0.2988827, 0.16945244, 0.08939293, 0.048387095, 0.060311284, 0.19592628, 0.15998198, 0.105882354, 0.047573283, 0.05768239, 0.15559158, 0.16305916, 0.024267782, 0.14054054, 0.1780303, 0.029209621, 0.065004885, 0.012269938, 0.073131956, 0.23976405, 0.12370929, 0.057155516, 0.06579954, 0.060413353, 0.12742382, 0.18846788]
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
