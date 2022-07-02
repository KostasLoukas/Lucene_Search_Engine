package com.lucenetutorial.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.Object;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.BytesTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Indexer 
{
	 private IndexWriter writer;
	 
	 
	 public Indexer(String indexDirectoryPath) throws IOException 
	 {
		 //This directory will contain the indexes
		 Path indexPath = Paths.get(indexDirectoryPath);
		 
		 if(!Files.exists(indexPath)) 
		 {
			 Files.createDirectory(indexPath);
		 }
		 
		 //Path indexPath = Files.createTempDirectory(indexDirectoryPath);
		 Directory indexDirectory = FSDirectory.open(indexPath);
		 
		 //Create the indexer
		 IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		 writer = new IndexWriter(indexDirectory, config);
	 }
	 
	 
	 public void close() throws CorruptIndexException, IOException 
	 {
		 writer.close();
	 }
	 
	 
	 private Document getDocument(File file) throws IOException
	 {
		 
		 Document document = new Document();
		 
		 
		 		 
		 int lio1;
		 int lio2;
		 String docContext, currentText, stemmedText="", clearText="";
		 Field contentField;
		 Path fileName = Path.of(file.toString());
	     
		 
		 
		 docContext = Files.readString(fileName);   //Read the whole document
		 
		 
		 //Pre-process document's PLACE and then index it
		 lio1 = docContext.indexOf("<PLACES>");
		 lio2 = docContext.indexOf("</PLACES>");
		 currentText = docContext.substring(lio1+8, lio2);
		 currentText = currentText.toLowerCase();
		 contentField = new Field("PLACES", currentText, TextField.TYPE_STORED);
		 document.add(contentField);
		 
		 
		 
		 //Pre-process document's PEOPLE and then index it
		 lio1 = docContext.indexOf("<PEOPLE>");
		 lio2 = docContext.indexOf("</PEOPLE>");
		 currentText = docContext.substring(lio1+8, lio2);
		 currentText = currentText.toLowerCase();
		 contentField = new Field("PEOPLE", currentText, TextField.TYPE_STORED);
		 document.add(contentField);
		 
		 
		 
		 //Pre-process document's TITLE and then index it
		 lio1 = docContext.indexOf("<TITLE>");
		 lio2 = docContext.indexOf("</TITLE>");
		 currentText = docContext.substring(lio1+7, lio2);
		 currentText = currentText.toLowerCase();
		 try 
		 {
			 
			 Analyzer analyzer = new StandardAnalyzer(); // Filters stop words
			 
			 
			 TokenStream tokenStream = analyzer.tokenStream("TITLE", new StringReader(currentText));
			 CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
			 tokenStream.reset();
			 
			 final CharArraySet stopset = new CharArraySet(LuceneConstants.stopwords, true);
			 tokenStream = new StopFilter(tokenStream, stopset);
			 
			  while(tokenStream.incrementToken()) 
			  {
			     String text = term.toString();
			    	
				  clearText = clearText + text + " ";
			  }
		

			  tokenStream.close();
			  analyzer.close();
		 } 
		 catch (IOException e) 
		 {
			    e.printStackTrace();
		 }
		 contentField = new Field("TITLE", clearText, TextField.TYPE_STORED);
		 document.add(contentField);
		 
		 
		 
		 //Pre-process document's BODY and then index it
		 lio1 = docContext.indexOf("<BODY>");
		 lio2 = docContext.indexOf("</BODY>");
		 currentText = docContext.substring(lio1+6, lio2);
		 currentText = currentText.toLowerCase();
		 try 
		 {
			 
			 Analyzer analyzer = new StandardAnalyzer(); // Filters stop words
			 PorterStemmer stemmer = new PorterStemmer();
			 
			 TokenStream tokenStream = analyzer.tokenStream("BODY", new StringReader(currentText));
			 CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
			 tokenStream.reset();
			 
			 final CharArraySet stopset = new CharArraySet(LuceneConstants.stopwords, true);
			 tokenStream = new StopFilter(tokenStream, stopset);
			 
			    while(tokenStream.incrementToken()) 
			    {
			    	String text = term.toString();
			    	for (int j=0 ; j<text.length() ; j++)
					 {
						 stemmer.add(text.charAt(j));
					 }
					 stemmer.stem();
					 clearText = clearText + stemmer.toString() + " ";
			    }
			    

			    tokenStream.close();
			    analyzer.close();
		 } 
		 catch (IOException e) 
		 {
			    e.printStackTrace();
		 }
		 contentField = new Field("BODY", clearText, TextField.TYPE_STORED);
		 document.add(contentField);
		 
		 
		 
		 //Index file name
		 Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), StringField.TYPE_STORED);
		 //Index file path
		 Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), StringField.TYPE_STORED);
		 
		 
		 document.add(fileNameField);
		 document.add(filePathField);
		
		 
		 return document;
	 }
	 
	 
	 
	 
	 
	 private void indexFile(File file) throws IOException
	 {
		 //System.out.println("Indexing " + file.getCanonicalPath());
		 Document document = getDocument(file);
		 
		 writer.addDocument(document);
	 }
	 
	 
	 
	 
	 public int createIndex(String dataDirPath, FileFilter filter) throws IOException
	 {
		 //Get all files in the data directory
		 File[] files = new File(dataDirPath).listFiles();
		 
		 for (File file : files)
		 {
			 if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file))
			 {
				 indexFile(file);
			 }
		 }
		 
		 return writer.numRamDocs();
	 }
}
