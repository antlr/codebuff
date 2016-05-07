#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Fri May 06 17:43:46 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["backupset_queries.sql", "buffer_pool_usage_by_db.sql", "compare_db_powershell.sql", "compare_tables.sql", "create_columnlist.sql", "database_execute_permissions.sql", "daysCTE.sql", "dmart_bits.sql", "dmart_bits_IAPPBO510.sql", "dmart_bits_PSQLRPT24.sql", "DriveSpace.sql", "enum_permissions.sql", "ex_CTEExample.sql", "ex_GROUPBY.sql", "ex_SUMbyColumn.sql", "filegroup_location_per_object.sql", "Generate_Weekly_Perfmon.sql", "index_bits.sql", "ipmonitor_notes.sql", "IPMonVerificationMaster.sql", "OrphanedUserCleanup_bits.sql", "ProgressQueries.sql", "project_status.sql", "RealECOrdersBy30days.sql", "role_details.sql", "sel_DeadLinkedServers.sql", "server_correlate.sql", "sprocswithservernames_bits.sql", "SQLErrorLogs_queries.sql", "SQLFilesAudit.sql", "SQLQuery23.sql", "SQLSpaceStats_bits.sql", "SQLStatsQueries.sql", "t_component_bits.sql", "table_info.sql", "vwTableInfo.sql"]
N = len(labels)

featureIndexes = range(0,N)
sqlite_noisy_self = [0.037144937, 0.10205155, 0.12914364, 0.021666666, 0.011173184, 0.16908212, 0.010526316, 0.036614485, 0.05154965, 0.10221098, 0.034077555, 0.059856344, 0.011502516, 0.032983508, 0.12247839, 0.20668058, 0.0256917, 0.1719733, 0.054867685, 0.12866305, 0.10114504, 0.05386488, 0.06925676, 0.082965575, 0.05014245, 0.028301887, 0.029579658, 0.01033295, 0.16213922, 0.120328166, 0.051485147, 0.07984791, 0.034464333, 0.025902817, 0.19818431, 0.02259887]
sqlite_noisy_corpus = [0.23702665, 0.14781694, 0.14755985, 0.022033898, 0.061452515, 0.17552336, 0.27952754, 0.10051483, 0.16054396, 0.23482695, 0.38876733, 0.28790787, 0.09267965, 0.14992504, 0.5162867, 0.26617953, 0.09881423, 0.16476448, 0.063122116, 0.17508633, 0.14122137, 0.29144448, 0.11824324, 0.16059603, 0.2689815, 0.15136054, 0.15399423, 0.058370043, 0.19844021, 0.45841092, 0.094169416, 0.14876425, 0.17315802, 0.061826434, 0.53785, 0.31135282]
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
