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

import java.util.List;

import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;
import nl.knaw.dans.dccd.tools.util.Printer;
import nl.knaw.dans.dccd.tools.util.Reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TaskRunner
{
	private static final Logger	logger	= LoggerFactory.getLogger(TaskRunner.class);

	private List<Task>			tasks;
	private String				reportDir;

	public TaskRunner()
	{

	}

	public List<Task> getTasks()
	{
		return tasks;
	}

	public void setTasks(List<Task> tasks)
	{
		this.tasks = tasks;
	}

	public String getReportDir()
	{
		if (reportDir == null)
		{
			reportDir = "reports/taskreports";
		}
		return reportDir;
	}

	public void setReportDir(String reportDir)
	{
		this.reportDir = reportDir;
	}

	public void execute()
	{
		if (tasks == null || tasks.isEmpty())
		{
			logger.info("No tasks defined in applicationContext.xml");
			return;
		}

		Reporter.setBaseDir(getReportDir(), Application.getStartDate());

		for (Task task : tasks)
		{
			if (task.needsAuthentication())
			{
				// Note Could do that!
				// Application.authenticate();
				break;
			}
		}

		int taskCount = 0;
		int errorCount = 0;
		try
		{
			for (Task task : tasks)
			{
				logger.info(Printer.format("Executing task " + task.getClass().getName()));
				task.run();
				errorCount += task.getErrorCount();
				taskCount++;
				logger.info("Finished task " + task.getClass().getName() + " with " + task.getErrorCount() + " exception(s)");
				Reporter.closeFile(task.getReportFile());
			}
			logger.info(Printer.format("Finished all tasks with " + errorCount + " exceptions"));
		}
		catch (FatalTaskException e)
		{
			logger.error("Fatal exception while executing " + e.getThrower().getClass().getSimpleName() + ": ", e);
			logger.error("Aborting further tasks. Finished " + taskCount + " task(s) with " + errorCount + " exception(s)");
		}
		catch (Throwable t)
		{
			logger.error("Exception while running tasks. Aborting further tasks. Finished " + taskCount + " task(s) with " + errorCount + " exception(s)", t);
		}
		finally
		{
			Reporter.closeAllFiles();
		}
	}

}
