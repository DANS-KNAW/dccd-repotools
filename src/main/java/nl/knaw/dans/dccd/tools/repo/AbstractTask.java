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
package nl.knaw.dans.dccd.tools.repo;

import java.io.IOException;

import nl.knaw.dans.dccd.tools.exceptions.FatalRuntimeException;
import nl.knaw.dans.dccd.tools.util.Reporter;

public abstract class AbstractTask implements Task
{

	private static String	REPORT_FILE;

	private int				errorCount;

	@Override
	public int getErrorCount()
	{
		return errorCount;
	}

	public void increaseErrors()
	{
		errorCount++;
	}

	public void report(String category, String... msgs) throws FatalRuntimeException
	{
		String cat = category == null ? "undefined" : category;
		StringBuilder report = new StringBuilder(cat);
		for (String msg : msgs)
		{
			report.append(";");
			report.append(msg);
		}

		try
		{
			Reporter.appendReport(getReportFile(), report.toString());
		}
		catch (IOException e)
		{
			throw new FatalRuntimeException("Unable to write report", e);
		}
	}

	@Override
	public String getReportFile()
	{
		if (REPORT_FILE == null)
		{
			REPORT_FILE = this.getClass().getSimpleName() + ".csv";
		}
		return REPORT_FILE;
	}

	@Override
	public String getTaskName()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean needsAuthentication()
	{
		return false;
	}
}
