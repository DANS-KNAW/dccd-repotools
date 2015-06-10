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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.dccd.application.services.ServiceException;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.tools.exceptions.FatalException;
import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;
import nl.knaw.dans.dccd.tools.project.ProjectProcessListener;
import nl.knaw.dans.dccd.tools.project.ProjectProcessor;
import nl.knaw.dans.dccd.tools.util.Dialogue;

public abstract class AbstractProjectProcessingTask extends AbstractSidSetTask implements ProjectProcessListener
{
	static final Logger	logger	= LoggerFactory.getLogger(AbstractProjectProcessingTask.class);

	private int	projectsNotFoundCount;
	private int	projectsProcessedCount;

	public AbstractProjectProcessingTask(String... idFilenames)
	{
		super(idFilenames);
	}

	public AbstractProjectProcessingTask(IdConverter idConverter, String... idFilenames)
	{
		super(idConverter, idFilenames);
	}

	abstract public ProjectProcessor createProcessor(DccdUser user) throws NoSearchEngineListeningException;

	@Override
	public void run() throws FatalTaskException
	{
		boolean confirmed = Dialogue.confirm("Process all projects listed in " + getIdFilenamesToString() + "?");
		if (!confirmed)
		{
			logger.info("Aborting " + getTaskName());
			return;
		}
	
		DccdUser user = Application.authenticate();
	
		Set<String> sidSet;
		try
		{
			sidSet = loadSids();
		}
		catch (IOException e)
		{
			throw new FatalTaskException("Cannot read file.", e, this);
		}
		catch (FatalException e)
		{
			throw new FatalTaskException("Cannot convert.", e, this);
		}
		confirmed = Dialogue.confirm("There are " + sidSet.size() + " project(s) that will be processed." + "\nDo you want to continue?");
		if (!confirmed)
		{
			logger.info("Aborting " + getTaskName());
			return;
		}
	
		logger.info("A total of " + sidSet.size() + " project(s) will be processed.");
	
		// for Processing the project, could be some interface!
		ProjectProcessor projectProcessor;
		try
		{
			projectProcessor = createProcessor(user);//new EmptyValuesFixer(user, this);
		}
		catch (NoSearchEngineListeningException e)
		{
			throw new FatalTaskException(e, this);
		}
	
		for (String storeId : sidSet)
		{
			try
			{
				projectProcessor.process(storeId);
			}
			catch (RepositoryException e)
			{
				throw new FatalTaskException(e, this);
			}
			catch (ServiceException e)
			{
				throw new FatalTaskException(e, this);
			}
		}
	
		logger.info("\n\tProcessed " + projectsProcessedCount + " projects of " + sidSet.size() + " projects in " + getIdFilenamesToString() + "."
				+ "\n\tA total of " + getOriginalIdNotFoundCount() + " originalIds was not found." + "\n\tA total of " + projectsNotFoundCount
				+ " projects were not found on the system.");
	
	}

	@Override
	public void onProjectNotFound(String storeId)
	{
		projectsNotFoundCount++;
		report("project not found: ", "\"" + storeId + "\"");
	}

	@Override
	public void onProjectProcessed(Project project, String message)
	{
		projectsProcessedCount++;
		report("Project " + project.getStoreId() + " processed: " + message);
	}

}
