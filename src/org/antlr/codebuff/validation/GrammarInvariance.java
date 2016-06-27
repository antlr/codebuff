package org.antlr.codebuff.validation;

import org.antlr.codebuff.Formatter;
import org.antlr.v4.runtime.misc.Triple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.antlr.codebuff.Dbg.normalizedLevenshteinDistance;
import static org.antlr.codebuff.Tool.JAVA8_DESCR;
import static org.antlr.codebuff.Tool.JAVA8_GUAVA_DESCR;
import static org.antlr.codebuff.Tool.JAVA_DESCR;
import static org.antlr.codebuff.Tool.JAVA_GUAVA_DESCR;
import static org.antlr.codebuff.Tool.SQLITE_CLEAN_DESCR;
import static org.antlr.codebuff.Tool.TSQL_CLEAN_DESCR;

public class GrammarInvariance {
	public static void main(String[] args) throws Exception {
		// we need to get all of the results in order so that we can compare
		LeaveOneOutValidator.FORCE_SINGLE_THREADED = true;
		float sql_median;
		float java_st_median;
		float java_guava_median;
		{
			// SQL
			LeaveOneOutValidator sqliteValidator =
				new LeaveOneOutValidator(SQLITE_CLEAN_DESCR.corpusDir, SQLITE_CLEAN_DESCR);
			LeaveOneOutValidator tsqlValidator =
				new LeaveOneOutValidator(TSQL_CLEAN_DESCR.corpusDir, TSQL_CLEAN_DESCR);
			Triple<List<Formatter>, List<Float>, List<Float>> sqliteResults =
				sqliteValidator.validateDocuments(false, null);
			Triple<List<Formatter>, List<Float>, List<Float>> tsqlResults =
				tsqlValidator.validateDocuments(false, null);
			List<Formatter> sqliteFormatters = sqliteResults.a;
			List<Formatter> tsqlFormatters = tsqlResults.a;

			List<Float> distances = new ArrayList<>();
			for (int i = 0; i<sqliteFormatters.size(); i++) {
				Formatter sqlite = sqliteFormatters.get(i);
				Formatter tsql = tsqlFormatters.get(i);
				float editDistance = normalizedLevenshteinDistance(sqlite.getOutput(), tsql.getOutput());
				distances.add(editDistance);
//				System.out.println(sqlite.testDoc.fileName+" edit distance "+editDistance);
			}

			{
				Collections.sort(distances);
				int n = distances.size();
				float min = distances.get(0);
				float quart = distances.get((int)(0.27*n));
				float median = distances.get(n/2);
				float quart3 = distances.get((int)(0.75*n));
				float max = distances.get(distances.size()-1);
				String display = "("+min+","+median+","+max+")";
				sql_median = median;
			}
		}

		{
			// JAVA
			List<Float> distances = new ArrayList<>();
			LeaveOneOutValidator javaValidator = new LeaveOneOutValidator(JAVA_DESCR.corpusDir, JAVA_DESCR);
			LeaveOneOutValidator java8Validator = new LeaveOneOutValidator(JAVA8_DESCR.corpusDir, JAVA8_DESCR);
			Triple<List<Formatter>, List<Float>, List<Float>> javaResults = javaValidator.validateDocuments(false, null);
			Triple<List<Formatter>, List<Float>, List<Float>> java8Results = java8Validator.validateDocuments(false, null);
			List<Formatter> javaFormatters = javaResults.a;
			List<Formatter> java8Formatters = java8Results.a;

			for (int i = 0; i<javaFormatters.size(); i++) {
				Formatter java = javaFormatters.get(i);
				Formatter java8 = java8Formatters.get(i);
				float editDistance = normalizedLevenshteinDistance(java.getOutput(), java8.getOutput());
				distances.add(editDistance);
//				System.out.println(java.testDoc.fileName+" edit distance "+editDistance);
			}

			{
				Collections.sort(distances);
				int n = distances.size();
				float min = distances.get(0);
				float quart = distances.get((int) (0.27*n));
				float median = distances.get(n/2);
				float quart3 = distances.get((int) (0.75*n));
				float max = distances.get(distances.size()-1);
				String display = "("+min+","+median+","+max+")";
				java_st_median = median;
			}
		}

		{
			// JAVA GUAVA
			List<Float> distances = new ArrayList<>();
			LeaveOneOutValidator java_guavaValidator = new LeaveOneOutValidator(JAVA_GUAVA_DESCR.corpusDir, JAVA_GUAVA_DESCR);
			LeaveOneOutValidator java8_guavaValidator = new LeaveOneOutValidator(JAVA8_GUAVA_DESCR.corpusDir, JAVA8_GUAVA_DESCR);
			Triple<List<Formatter>, List<Float>, List<Float>> java_guavaResults = java_guavaValidator.validateDocuments(false, null);
			Triple<List<Formatter>, List<Float>, List<Float>> java8_guavaResults = java8_guavaValidator.validateDocuments(false, null);
			List<Formatter> java_guavaFormatters = java_guavaResults.a;
			List<Formatter> java8_guavaFormatters = java8_guavaResults.a;

			for (int i = 0; i<java_guavaFormatters.size(); i++) {
				Formatter java_guava = java_guavaFormatters.get(i);
				Formatter java8_guava = java8_guavaFormatters.get(i);
				float editDistance = normalizedLevenshteinDistance(java_guava.getOutput(), java8_guava.getOutput());
				distances.add(editDistance);
//				System.out.println(java_guava.testDoc.fileName+" edit distance "+editDistance);
			}

			{
				Collections.sort(distances);
				int n = distances.size();
				float min = distances.get(0);
				float quart = distances.get((int) (0.27*n));
				float median = distances.get(n/2);
				float quart3 = distances.get((int) (0.75*n));
				float max = distances.get(distances.size()-1);
				String display = "("+min+","+median+","+max+")";
				java_guava_median = median;
			}
		}
		System.out.println("clean SQLite vs TSQL edit distance info median="+sql_median);
		System.out.println("Java vs Java8 edit distance info median="+java_st_median);
		System.out.println("Java vs Java8 guava edit distance info median="+java_guava_median);
	}
}
