#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.4 'Wed May 04 18:58:46 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_self = [0.12704061, 0.036144577, 0.030835882, 0.0023724793, 0.00591716, 0.060708262, 0.018469658, 0.043558605, 0.06490314, 0.09269192, 0.16550279, 0.08242075, 0.014715719, 0.017741935, 0.04863813, 0.011639185, 0.057683643, 0.064424545, 0.04228736, 0.012503223, 0.13290113, 0.14502165, 0.0016977929, 0.05945946, 0.05871212, 0.01718213, 0.021600394, 0.012269938, 0.07790143, 0.12664816, 0.22142981, 0.06468764, 0.047946952, 0.029512327, 0.2308308, 0.09785832]
sqlite_corpus = [0.18857263, 0.13547759, 0.072704405, 0.011764706, 0.10695187, 0.09780776, 0.21940929, 0.051863436, 0.104140036, 0.15102246, 0.2730447, 0.25821325, 0.16933097, 0.077044025, 0.060311284, 0.28806984, 0.15998198, 0.15111563, 0.053099472, 0.07740397, 0.17666127, 0.22294372, 0.06838294, 0.2172915, 0.2159091, 0.119205296, 0.16329226, 0.030120483, 0.098569155, 0.24878557, 0.17052948, 0.09215773, 0.09742413, 0.11935323, 0.32015368, 0.22487593]
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

ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File $f$ with Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/sqlite_one_file_capture.pdf", format='pdf')
plt.show()
