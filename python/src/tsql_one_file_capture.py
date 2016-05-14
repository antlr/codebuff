#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.14 'Sat May 14 16:12:39 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_self = [0.1358309, 0.024554646, 0.03567788, 0.0011862396, 0.00591716, 0.0539629, 0.018469658, 0.015839493, 0.09705969, 0.10122021, 0.11033519, 0.08414985, 0.014715719, 0.024193548, 0.04863813, 0.04946654, 0.10274898, 0.05896484, 0.03940413, 0.015919052, 0.10372771, 0.19480519, 0.0025445293, 0.05945946, 0.066287875, 0.02233677, 0.013705336, 0.02145046, 0.06518283, 0.16099931, 0.05401112, 0.056269385, 0.04565162, 0.016555695, 0.22154634, 0.13278419]
tsql_corpus = [0.14231896, 0.06335282, 0.04332314, 0.0011862396, 0.00591716, 0.09780776, 0.19739696, 0.031414993, 0.06264136, 0.12286289, 0.3784916, 0.129683, 0.041132838, 0.052419353, 0.1119403, 0.14354995, 0.24740875, 0.07300216, 0.041086018, 0.055942252, 0.13614263, 0.09235209, 0.006756757, 0.16412213, 0.14259173, 0.04639175, 0.017569546, 0.012269938, 0.00953895, 0.19361554, 0.08101668, 0.024637043, 0.05228258, 0.017182745, 0.17979583, 0.14332785]
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
