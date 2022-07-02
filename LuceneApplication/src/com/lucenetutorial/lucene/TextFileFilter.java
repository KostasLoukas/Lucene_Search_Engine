package com.lucenetutorial.lucene;
import java.io.File;
import java.io.FileFilter;


//This class gathers all the .txt files
public class TextFileFilter implements FileFilter
{
	@Override
	public boolean accept(File pathname)
	{
		return pathname.getName().toLowerCase().endsWith(".txt");
	}
}
