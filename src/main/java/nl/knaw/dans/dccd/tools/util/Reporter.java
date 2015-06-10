/*******************************************************************************
 * Copyright 2015 DANS - Data Archiving and Networked Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package nl.knaw.dans.dccd.tools.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reporter
{

	private static final Logger							logger		= LoggerFactory.getLogger(Reporter.class);

	private static final Map<String, RandomAccessFile>	OPEN_FILES	= new HashMap<String, RandomAccessFile>();

	private static File									BASE_DIR;

	private Reporter()
	{

	}

	public static void setBaseDir(String baseDir, DateTime dateTime)
	{
		String lastDir = new File(baseDir).getName() + "_" + dateTime.toString("yyyy-MM-dd_HH.mm.ss");
		File parent = new File(baseDir).getParentFile();

		BASE_DIR = new File(parent, lastDir);
		BASE_DIR.mkdirs();
	}

	private static File getBaseDir()
	{
		if (BASE_DIR == null)
		{
			setBaseDir("reports/report", new DateTime());
		}
		return BASE_DIR;
	}

	private static RandomAccessFile getRAF(String reportFile) throws IOException
	{
		RandomAccessFile raf = OPEN_FILES.get(reportFile);
		if (raf == null)
		{
			File newRaf = new File(getBaseDir(), reportFile);
			raf = new RandomAccessFile(newRaf, "rw");
			raf.seek(raf.length());
			OPEN_FILES.put(reportFile, raf);
		}
		return raf;
	}

	public static void appendReport(String reportFile, String report) throws IOException
	{
		RandomAccessFile raf = getRAF(reportFile);

		raf.writeBytes(report);
		raf.writeBytes("\n");
	}

	public static void closeFile(String reportFile) throws IOException
	{
		RandomAccessFile raf = OPEN_FILES.get(reportFile);
		if (raf != null)
		{
			raf.close();
			OPEN_FILES.remove(reportFile);
			logger.info("Closed report file: " + reportFile);
		}
	}

	public static void closeAllFiles()
	{
		for (String reportFile : OPEN_FILES.keySet())
		{
			try
			{
				OPEN_FILES.get(reportFile).close();
				logger.info("Closed report file: " + reportFile);
			}
			catch (IOException e)
			{
				logger.error("Could not close file: " + reportFile, e);
			}
		}
		OPEN_FILES.clear();
	}

}
