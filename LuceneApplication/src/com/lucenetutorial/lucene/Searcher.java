package com.lucenetutorial.lucene;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;



public class Searcher 
{
	 IndexSearcher indexSearcher;
	 Directory indexDirectory;
	 IndexReader indexReader;
	 QueryParser queryParser;
	 Query query;
	 
	 
	 public Searcher(String indexDirectoryPath, String fieldName) throws IOException 
	 {
		 Path indexPath = Paths.get(indexDirectoryPath);
		 indexDirectory = FSDirectory.open(indexPath);
		 indexReader = DirectoryReader.open(indexDirectory);
		 indexSearcher = new IndexSearcher(indexReader);
		 queryParser = new QueryParser(fieldName, new StandardAnalyzer());
	 }
	 
	 
	 public TopDocs search(String query2) throws IOException, ParseException 
	 {
		 query = queryParser.parse(query2);
		 return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
		 
	 }
	 
	 public TopDocs search(Builder query3) throws IOException, ParseException 
	 {
		 return indexSearcher.search(query3.build(), LuceneConstants.MAX_SEARCH);
		 
	 }
	 
	 public TopDocs search(Query query) throws IOException, ParseException 
	 {
	      return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	 }
	 
	 
	 public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException 
	 {
		 return indexSearcher.doc(scoreDoc.doc);
	 }
	 
	 
	 public void close() throws IOException 
	 {
		 indexReader.close();
		 indexDirectory.close();
	 }
}
