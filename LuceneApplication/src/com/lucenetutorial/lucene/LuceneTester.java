package com.lucenetutorial.lucene;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;



public class LuceneTester 
{
	 static String indexDir = "C:\\Users\\kosta\\Desktop\\eclipse-workspace\\LuceneApplication\\Index";  //Where the index is going to be saved at
	 static String dataDir = "C:\\Users\\kosta\\Desktop\\eclipse-workspace\\LuceneApplication\\Data";   //Where the documents are saved
	 Indexer indexer;  //Create an indexer
	 Searcher searcher;  //Create a searcher
	private int length;
	 
	 
	 public static void main(String[] args) throws IOException 
	 {
		 LuceneTester tester = new LuceneTester();
		 Scanner input = new Scanner(System.in);
		 int ans, searchans, categoryans, k;
		 String path;
		 String text, docText, filename, searchExpression, category="", searchPhrase[];
		 
		 
		 
		 do
		 {
			 System.out.println("\nWelcome!\nWhat do you want to do?\n(type in the desired choice's number and hit enter)");
			 System.out.println("1. Insert a document\n2. Delete a document\n3. Index stored documents\n4. Search for documents\n5. Exit");
			 
			 ans = input.nextInt();
			 input.nextLine();  //To consume the '\n' character
			 
			 if (ans==1)  //Insert Document
			 {
				 System.out.println("Please insert the absolute path of the document's location you want to insert:");
				 path = input.nextLine();
				 
				 
				 
				 docText = Files.readString(Paths.get(path));
				 
				 
				 if (docText.matches("<PLACES>[\\s\\S]*</PLACES>[\\s]*<PEOPLE>[\\s\\S]*</PEOPLE>[\\s]*<TITLE>[\\s\\S]*</TITLE>[\\s]*<BODY>[\\s\\S]*</BODY>[\\s]*"))
				 {
					 System.out.println("Document stored to engine and will be indexed sortly!");
					 Files.copy(Paths.get(path), Paths.get("Data\\" + path.substring(path.lastIndexOf("\\"))), StandardCopyOption.REPLACE_EXISTING);
					 
					 
					 //Emptying the index before creating it again with the newly inserted file
					 File index = new File(indexDir);
					 File[] allContents = index.listFiles();
					 if (allContents != null) 
					 {
					     for (File file : allContents) 
					     {
					         file.delete();
					     }
					 }
					 System.out.println("Re-Indexing all available documents...");
					 tester.createIndex();   //Create the index
					 System.out.println("Re-Indexing Complete!");
					 
				 }
				 else
				 {
					 System.out.println("ERROR: DOCUMENT DOES NOT MATCH THE APPROPRIATE FORMAT!\n\nCORRECT FORMAT IS:\n<PLACES>places here</PLACES>\n<PEOPLE>people here</PEOPLE>\n<TITLE>title here</TITLE>\n<BODY>document body here</BODY>");
					 System.out.println("\nGoing back to the main menu...");
					 continue;
				 }
				 	 
			 }
			 else if(ans==2)  //Delete document
			 {
				 System.out.println("Please insert the document's file name that you want to delete:");
				 filename = input.nextLine();
				 
				 File file = new File("Data\\" + filename);
				 if(file.delete())
				 {
					 System.out.println("Deleted file:" + filename + " from repository.");
					 
					//Emptying the index before creating it again to exclude the deleted file from the index
					 File index = new File(indexDir);
					 File[] allContents = index.listFiles();
					 if (allContents != null) 
					 {
					     for (File file1 : allContents) 
					     {
					         file1.delete();
					     }
					 }
					 System.out.println("Re-Indexing all available documents...");
					 tester.createIndex();   //Create the index
					 System.out.println("Re-Indexing Complete!");
				 }
				 else
				 {
					 System.out.println("FILE DOES NOT EXIST OR WRONG FILE NAME INSERTED!");
					 continue;
				 }
			 }
			 else if (ans==3)  //Index all available documents
			 {
				 //Emptying the index before creating it again to avoid duplicate indexing of documents
				 File index = new File(indexDir);
				 File[] allContents = index.listFiles();
				 if (allContents != null) 
				 {
				     for (File file : allContents) 
				     {
				         file.delete();
				     }
				 }
				 
				 
				 tester.createIndex();   //Create the index
				 System.out.println("Indexing Complete!");
			 }
			 else if (ans==4)  //Search for documents
			 {
				 System.out.println("Now choose which search method you want to use:\n1. Search with Phrase Query\n2. Search with Boolean model\n3. Search with Vector Space model\n4. Back");
				 searchans= input.nextInt();
				 input.nextLine();
				 
				 if (searchans == 4)
				 {
					 continue;
				 }
				 
				 System.out.println("Select search category:\n1. Places\n2. People\n3. Titles\n4. Document contexts (body)\n5. Back");
				 categoryans = input.nextInt();
				 input.nextLine();
					 
				 switch (categoryans)
				 {
					 case 1: category="PLACES";
					 break;
					 	
					 case 2: category="PEOPLE";
					 break;
					 	
					 case 3: category="TITLE";
					 	break;
					 	
					 case 4: category="BODY";
					 break;
					 
					 case 5: continue;
					 	
					 default: System.out.println("NOT GIVEN CHOICE SELECTED!\nPLEASE SELECT ONE OF THE AVAILABLE CHOICES NEXT TIME!");
					 continue;
				 }
				 
				 System.out.println("Select the K-top (max) number of results you want to be shown (0=back, default max=31): ");
				 k = input.nextInt();
				 input.nextLine();
				 
				 if(k==0)
				 {
					 continue;
				 }
				 else if (k<0)
				 {
					 System.out.println("INVALID (NEGATIVE) K NUMBER SELECTED!");
					 continue;
				 }
				 
				 
				 
				 if (searchans==1)  //Phrase Search
				 {
					 System.out.print("Search for phrase:");
					 searchExpression = input.nextLine();
					 
					 try 
					 {
						 searchPhrase = searchExpression.split("[\\s]+", 0);
						 tester.searchPhraseQuery(searchPhrase, category, k); 
					 }
					 catch (IOException e) 
					 {
						 e.printStackTrace();
					 }
					 catch (ParseException e) 
					 {
						 e.printStackTrace();
					 }
				 }
				 else if (searchans==2)  //Boolean Search
				 {
					 System.out.print("Search for (usage: ... AND/OR/NOT ... AND/OR/NOT ...):");  //Valid are ... AND ... AND ..., ... OR ... OR ..., ... NOT ... NOT ...
					 searchExpression = input.nextLine();
					 
					 try 
					 {
						 tester.searchBooleanQuery(searchExpression, category, k);
					 }
					 catch (IOException e) 
					 {
						 e.printStackTrace();
					 }
					 catch (ParseException e) 
					 {
						 e.printStackTrace();
					 }
				 }
				 else if (searchans==3)  //Vector Space Search
				 {
					 System.out.print("Search for:");
					 searchExpression = input.nextLine();
					 
					 try 
					 { 
						 tester.searchVectorSpace(searchExpression, category, k);
					 }
					 catch (IOException e) 
					 {
						 e.printStackTrace();
					 }
					 catch (ParseException e) 
					 {
						 e.printStackTrace();
					 }
				 }
				 else if (ans==4)  //Go back to the main menu
				 {
					 continue;
				 }
				 else
				 {
					 System.out.println("INVALID OPTION SELECTED!");
				 }
			 }
			 
		 }while(ans!=5);
		 
		 
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 private void createIndex() throws IOException 
	 {
		 indexer = new Indexer(indexDir);
		 int numIndexed;
		 long startTime = System.currentTimeMillis();
		 numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
		 long endTime = System.currentTimeMillis();
		 indexer.close();
		 System.out.println(numIndexed+" File(s) indexed, time taken: " + (endTime-startTime)+" ms");
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 //The Phrase Search Function
	 private void searchPhraseQuery(String[] phrases, String fieldName, int k) throws IOException, ParseException
	 {
		 	  
		 
		 	  String nextpath;
		 	  
		      searcher = new Searcher(indexDir, fieldName);
		      long startTime = System.currentTimeMillis();
		      String Allphrases ="", searchPhrase="", clearText="";
		      String[] clearTextsplitted = new String[phrases.length];
		      
		      
		      //Preprocess the query according to the field
		      for (int i = 0 ; i<phrases.length ; i++)
		      {
		    	  searchPhrase += phrases[i] + " ";
		      }
		      
		      
		      
		      if (fieldName == "PLACES")
		      {
		    	  clearText = searchPhrase.toLowerCase();
		      }
		      else if (fieldName == "PEOPLE")
		      {
		    	  clearText = searchPhrase.toLowerCase();
		      }
		      else if (fieldName == "TITLE")
		      {
		    	  searchPhrase = searchPhrase.toLowerCase();
		    	  
		    	  try 
		 		 {
		 			 
		 			 Analyzer analyzer = new StandardAnalyzer();
		 			 
		 			 
		 			 TokenStream tokenStream = analyzer.tokenStream("TITLE", new StringReader(searchPhrase));
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
		    	 
		    	  
		      }
		      else if (fieldName == "BODY")
		      {
		    	  searchPhrase = searchPhrase.toLowerCase();
		    	  
		    	  try 
		 		 {
		 			 
		 			 Analyzer analyzer = new StandardAnalyzer();
		 			 PorterStemmer stemmer = new PorterStemmer();
		 			 
		 			 TokenStream tokenStream = analyzer.tokenStream("BODY", new StringReader(searchPhrase));
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
		      }
		      
		      clearTextsplitted = clearText.split("[\\s]+");
		      
		      PhraseQuery query = new PhraseQuery(0, fieldName, clearTextsplitted);

		      //Do the search and print the results
		      TopDocs hits = searcher.search(query);
		      long endTime = System.currentTimeMillis();

		      System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime) + "ms");
		      
			  String[] paths = new String[LuceneConstants.MAX_SEARCH];
		      
		      int i = 0, w = 0;
		      for(ScoreDoc scoreDoc : hits.scoreDocs)
		      {
		    	  if (w==k)
		    	  {
		    		  break;
		    	  }
		    	  
		         Document doc = searcher.getDocument(scoreDoc);
		         nextpath = doc.get(LuceneConstants.FILE_PATH);
		         
		         System.out.print("#" + i + "\nFile: " + doc.getField(LuceneConstants.FILE_NAME).stringValue() + "\nScore: " + scoreDoc.score);
		         
		        		 
		         String str = doc.getField("BODY").stringValue();
		         
		         System.out.println("\nDocument's " + i + " shard: " + str);  //printing the documents indexed body
		         
		         paths[i] = nextpath;
		         
		         i++;
		         w++;
		      }
		      
		      Scanner input = new Scanner(System.in);
		      int resNumber;
		      
		      //Ask the user which of the documents' context to show
		      System.out.println("Type the result's # to show that article's contents (-1=back to menu): ");
		      resNumber = input.nextInt();
		      input.nextLine();
		      
		      
		      if (resNumber == -1)
		      {
		    	  return;
		      }
		      
		      
		      Scanner inputStream;
		      String docContext;
		      
		      try {
		    	  inputStream = new Scanner(new File(paths[resNumber]));
		      }
		      catch (FileNotFoundException e)
		      {
		    	  throw e;
		      }
		      inputStream.close();
		      
		      Path p = Paths.get(paths[resNumber]);
		      docContext = Files.readString(p);
		      System.out.println(docContext);
		      
		      
		      searcher.close();
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 //The Boolean Search Function
	 private void searchBooleanQuery(String searchPhrase, String fieldName, int k) throws IOException, ParseException
	 {
		      searcher = new Searcher(indexDir, fieldName);
		      long startTime = System.currentTimeMillis();
		      String clearText = "", nextpath;
		      String[] paths = new String[LuceneConstants.MAX_SEARCH];
		      boolean AND=false, OR=false, NOT=false;
		      
		      
		      //See what type of search the user used and keep only the words without the boolean phrases (AND/OR/NOT)
		      if (searchPhrase.contains("AND"))
		      {
		    	  AND = true;
		    	  searchPhrase = searchPhrase.replaceAll("AND", "");
		    	  
		      }
		      else if(searchPhrase.contains("OR"))
		      {
		    	  OR = true;
		    	  searchPhrase = searchPhrase.replaceAll("OR", "");
		    	  
		      }
		      else if(searchPhrase.contains("NOT"))
		      {
		    	  NOT = true;
		    	  searchPhrase = searchPhrase.replaceAll("NOT", "");
		    	  
		      }
		      else
		      {
		    	  System.out.println("WRONG SYNTAX USED IN BOOLEAN QUERY!\nSEARCH TERMINATED.");
		    	  return;
		      }
		      
		      
		      //Preprocess the query according to the field
		      if (fieldName == "PLACES")
		      {
		    	  clearText = searchPhrase.toLowerCase();
		      }
		      else if (fieldName == "PEOPLE")
		      {
		    	  clearText = searchPhrase.toLowerCase();
		      }
		      else if (fieldName == "TITLE")
		      {
		    	  searchPhrase = searchPhrase.toLowerCase();
		    	  
		    	  try 
		 		 {
		 			 
		 			 Analyzer analyzer = new StandardAnalyzer(); // Filters stop words
		 			 
		 			 
		 			 TokenStream tokenStream = analyzer.tokenStream("TITLE", new StringReader(searchPhrase));
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
		      }
		      else if (fieldName == "BODY")
		      {
		    	  searchPhrase = searchPhrase.toLowerCase();
		    	  
		    	  try 
		 		 {
		 			 
		 			 Analyzer analyzer = new StandardAnalyzer(); // Filters stop words and stems them
		 			 PorterStemmer stemmer = new PorterStemmer();
		 			 
		 			 TokenStream tokenStream = analyzer.tokenStream("BODY", new StringReader(searchPhrase));
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
		      }
		      
		      String[] phrases = clearText.split("[\\s]+");
		      
		      TermQuery[] termQuery = new TermQuery[phrases.length];
		      BooleanClause[] booleanClause = new BooleanClause[phrases.length];
		      BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		      
		      for(int i = 0 ; i<phrases.length ; i++)
		      {
		    	  if (AND == true)
		    	  {
			    	  termQuery[i] = new TermQuery(new Term(fieldName, phrases[i]));
			    	  booleanClause[i] = new BooleanClause(termQuery[i], BooleanClause.Occur.MUST);
			    	  booleanQuery.add(booleanClause[i]);
		    	  }
		    	  else if (OR == true)
		    	  {
		    		  termQuery[i] = new TermQuery(new Term(fieldName, phrases[i]));
			    	  booleanClause[i] = new BooleanClause(termQuery[i], BooleanClause.Occur.SHOULD);
			    	  booleanQuery.add(booleanClause[i]);
		    	  }
		    	  
		      }
		      
		      if (NOT == true)
		      {
		    	  //The first term of the NOT query must exist as the MUST_NOT words do not contribute to the score
		    	  termQuery[0] = new TermQuery(new Term(fieldName, phrases[0]));
		    	  booleanClause[0] = new BooleanClause(termQuery[0], BooleanClause.Occur.MUST);
		    	  booleanQuery.add(booleanClause[0]);
		    	  
		    	  for(int i = 1 ; i<phrases.length ; i++)
			      {
		    		  termQuery[i] = new TermQuery(new Term(fieldName, phrases[i]));
			    	  booleanClause[i] = new BooleanClause(termQuery[i], BooleanClause.Occur.MUST_NOT);
			    	  booleanQuery.add(booleanClause[i]);
			      }
		      }
		      
		      
		      
		      
		      //Do the search and print the results
		      TopDocs hits = searcher.search(booleanQuery);
		      long endTime = System.currentTimeMillis();
		      
		      int i = 0, w = 0;
		      for(ScoreDoc scoreDoc : hits.scoreDocs)
		      {
		    	  if (w==k)
		    	  {
		    		  break;
		    	  }
		    	  
		         Document doc = searcher.getDocument(scoreDoc);
		         nextpath = doc.get(LuceneConstants.FILE_PATH);
		         
		         System.out.print("#" + i + "\nFile: " + doc.getField(LuceneConstants.FILE_NAME).stringValue() + "\nScore: " + scoreDoc.score);
		         
		        		 
		         String str = doc.getField("BODY").stringValue();
				      
		         System.out.println("\nDocument's " + i + " shard: " + str);  //printing the documents indexed body
		         
		         paths[i] = nextpath;
		         
		         i++;
		         w++;
		      }
		      
		      
		      //Ask the user which of the documents' context to show
		      Scanner input = new Scanner(System.in);
		      int resNumber;
		      
		      
		      System.out.println("\nType the result's # to show that article's contents (-1=back to menu): ");
		      resNumber = input.nextInt();
		      input.nextLine();
		      
		      if (resNumber == -1)
		      {
		    	  return;
		      }
		      
		      Scanner inputStream;
		      String docContext;
		      
		      try 
		      {
		    	  inputStream = new Scanner(new File(paths[resNumber]));
		      }
		      catch (FileNotFoundException e)
		      {
		    	  throw e;
		      }
		      inputStream.close();
		      
		      Path p = Paths.get(paths[resNumber]);
		      docContext = Files.readString(p);
		      System.out.println(docContext);
		      
		      
		      searcher.close();
	 }
	
	 
	 
	 
	 
	 
	 
	 
	 
	 

	 //The Vector Space Search Function
	 private void searchVectorSpace(String searchPhrase, String fieldName, int k) throws IOException, ParseException
	 {
		 	  
		 		
		 	  String nextpath, clearText = "";
		 	  int i = 0, w = 0;
		 	  
		      searcher = new Searcher(indexDir, fieldName);
		      long startTime = System.currentTimeMillis();
		      
		      //Preprocess the query according to the field
		      if (fieldName == "PLACES")
		      {
		    	  clearText = searchPhrase.toLowerCase();
		      }
		      else if (fieldName == "PEOPLE")
		      {
		    	  clearText = searchPhrase.toLowerCase();
		      }
		      else if (fieldName == "TITLE")
		      {
		    	  searchPhrase = searchPhrase.toLowerCase();
		    	  
		    	  try 
		 		 {
		 			 
		 			 Analyzer analyzer = new StandardAnalyzer(); // Filters stop words
		 			 
		 			 
		 			 TokenStream tokenStream = analyzer.tokenStream("TITLE", new StringReader(searchPhrase));
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
		      }
		      else if (fieldName == "BODY")
		      {
		    	  searchPhrase = searchPhrase.toLowerCase();
		    	  
		    	  try 
		 		 {
		 			 
		 			 Analyzer analyzer = new StandardAnalyzer(); // Filters stop words and stems them
		 			 PorterStemmer stemmer = new PorterStemmer();
		 			 
		 			 TokenStream tokenStream = analyzer.tokenStream("BODY", new StringReader(searchPhrase));
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
		      }
		      

		      //Do the search and print the results
		      TopDocs hits = searcher.search(clearText);
		      long endTime = System.currentTimeMillis();

		      System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime) + "ms");
		      
			  String[] paths = new String[LuceneConstants.MAX_SEARCH];
		      
		      
			  i = 0;
			  w = 0;
		      for(ScoreDoc scoreDoc : hits.scoreDocs)
		      {
		    	  if (w==k)
		    	  {
		    		  break;
		    	  }
		    	  
		         Document doc = searcher.getDocument(scoreDoc);
		         nextpath = doc.get(LuceneConstants.FILE_PATH);
		         
		         System.out.print("#" + i + "\nFile: " + doc.getField(LuceneConstants.FILE_NAME).stringValue() + "\nScore: " + scoreDoc.score);
		         
		        		 
		         String str = doc.getField("BODY").stringValue();
				      
		         System.out.println("\nDocument's " + i + " shard: " + str);  //printing the documents indexed body
		         
		         paths[i] = nextpath;
		         
		         i++;
		         w++;
		      }
		      
		      Scanner input = new Scanner(System.in);
		      int resNumber;
		      
		      
		      //Ask the user which of the documents' context to show
		      System.out.println("\nType the result's # to show that article's contents (-1=back to menu): ");
		      resNumber = input.nextInt();
		      input.nextLine();
		      
		      if (resNumber == -1)
		      {
		    	  return;
		      }
		      
		      Scanner inputStream;
		      String docContext;
		      
		      try 
		      {
		    	  inputStream = new Scanner(new File(paths[resNumber]));
		      }
		      catch (FileNotFoundException e)
		      {
		    	  throw e;
		      }
		      inputStream.close();
		      
		      Path p = Paths.get(paths[resNumber]);
		      docContext = Files.readString(p);
		      System.out.println(docContext);
		      
		      
		      
		      
		      searcher.close();
	 }
	 
	 
}
