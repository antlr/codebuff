#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.12 'Wed May 11 13:23:10 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_self = [0.11490163, 0.028297363, 0.03567788, 0.0011862396, 0.00591716, 0.0539629, 0.018469658, 0.021119324, 0.0544011, 0.09765721, 0.1801676, 0.05648415, 0.02899528, 0.024193548, 0.017509727, 0.04946654, 0.10410095, 0.058091287, 0.04781355, 0.015919052, 0.10372771, 0.13612314, 0.007640068, 0.05945946, 0.054450758, 0.02233677, 0.013705336, 0.02145046, 0.06518283, 0.12456627, 0.056393962, 0.059813913, 0.1581853, 0.017275508, 0.068192735, 0.06853377]
tsql_corpus = [0.15403935, 0.06481481, 0.038735982, 0.0011862396, 0.00591716, 0.09780776, 0.22105263, 0.045406546, 0.086242504, 0.13560174, 0.39385474, 0.129683, 0.07687121, 0.05483871, 0.1119403, 0.12415131, 0.24740875, 0.084948815, 0.036761172, 0.055942252, 0.16855754, 0.13852814, 0.015899582, 0.15057915, 0.12642045, 0.029209621, 0.02404318, 0.012269938, 0.060413353, 0.20471895, 0.074861, 0.10339054, 0.04820199, 0.016091542, 0.19375093, 0.12981878]
tsql_diff = np.abs(np.subtract(tsql_self, tsql_corpus))

all = zip(tsql_self, tsql_corpus, tsql_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
tsql_self, tsql_corpus, tsql_diff, labels = zip(*all)

ax.plot(featureIndexes, tsql_self, label="tsql_self")
#ax.plot(featureIndexes, tsql_corpus, label="tsql_corpus")
ax.plot(featureIndexes, tsql_diff, label="tsql_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(tsql_self),np.median(tsql_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File tsql $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/tsql_one_file_capture.pdf", format='pdf')
plt.show()
