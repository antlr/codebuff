#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.14 'Sat May 14 16:12:28 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_self = [0.1157388, 0.038131554, 0.042547897, 0.0011862396, 0.00591716, 0.06322795, 0.018469658, 0.044878565, 0.04975907, 0.07500838, 0.036486488, 0.08414985, 0.014715719, 0.029032258, 0.04863813, 0.011639185, 0.057683643, 0.044763334, 0.03748198, 0.018883733, 0.11345219, 0.10630111, 0.008488964, 0.05945946, 0.05871212, 0.01718213, 0.01963672, 0.012269938, 0.06518283, 0.1204025, 0.060365368, 0.064244576, 0.0304414, 0.041209284, 0.22858973, 0.08276088]
sqlite_corpus = [0.14190038, 0.11939571, 0.042558614, 0.009422851, 0.00591716, 0.05059022, 0.24899599, 0.053326294, 0.06116629, 0.09302715, 0.1627095, 0.22939481, 0.07504938, 0.058064517, 0.06614786, 0.16197866, 0.14601171, 0.10475988, 0.05069678, 0.060582623, 0.1458671, 0.14694564, 0.008488964, 0.16447876, 0.19412878, 0.0395189, 0.08070678, 0.012269938, 0.0453125, 0.2297016, 0.10606061, 0.05344523, 0.026778882, 0.05708208, 0.13093337, 0.1261468]
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
