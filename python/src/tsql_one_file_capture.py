#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Mon May 09 17:59:40 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_self = [0.113645874, 0.024554646, 0.03567788, 0.0011862396, 0.00591716, 0.05059022, 0.018469658, 0.019535376, 0.05310169, 0.09073832, 0.17946927, 0.054755043, 0.015394913, 0.017741935, 0.017509727, 0.04946654, 0.10410095, 0.05394191, 0.0427679, 0.01463006, 0.10048623, 0.13468014, 0.005942275, 0.05945946, 0.055397727, 0.02233677, 0.013222331, 0.021472393, 0.06518283, 0.12317835, 0.063145354, 0.057155516, 0.15473609, 0.015835883, 0.06627181, 0.06853377]
tsql_corpus = [0.1525743, 0.06481481, 0.040010195, 0.0011862396, 0.00591716, 0.09780776, 0.21940929, 0.043558605, 0.084177405, 0.1311599, 0.39315644, 0.12910663, 0.06350268, 0.051612902, 0.11401869, 0.103782736, 0.2478594, 0.08355148, 0.040365208, 0.05529776, 0.16693678, 0.13732564, 0.01595298, 0.14903475, 0.12594697, 0.03264605, 0.025527736, 0.012269938, 0.060413353, 0.20402499, 0.07446386, 0.092569, 0.046161693, 0.015211167, 0.10340963, 0.12981878]
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
