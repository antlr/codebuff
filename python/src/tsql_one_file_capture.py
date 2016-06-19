#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 18 16:50:27 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
tsql_self = [0.1358309, 0.024554646, 0.03950051, 0.0011862396, 0.00591716, 0.0539629, 0.018469658, 0.016433854, 0.09162329, 0.09557091, 0.108938545, 0.08414985, 0.014715719, 0.023696683, 0.04780115, 0.04946654, 0.10274898, 0.04357251, 0.040605478, 0.018026926, 0.11490126, 0.19121814, 0.0025445293, 0.05945946, 0.066287875, 0.02233677, 0.013705336, 0.02145046, 0.0015898251, 0.14969242, 0.05381255, 0.010190519, 0.05529499, 0.016555695, 0.22154634, 0.12573099]
tsql_corpus = [0.12180829, 0.04191033, 0.05988787, 0.0011862396, 0.00591716, 0.09780776, 0.19739696, 0.028211448, 0.060983993, 0.12239453, 0.37918994, 0.08299712, 0.06827048, 0.048183255, 0.04206501, 0.12027158, 0.273096, 0.063057326, 0.046131667, 0.018559366, 0.13105924, 0.11024551, 0.013422819, 0.13280116, 0.16266094, 0.04639175, 0.06626506, 0.01629328, 0.011923688, 0.17771702, 0.08141382, 0.03809524, 0.058913544, 0.0395174, 0.17325458, 0.12248213]
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
