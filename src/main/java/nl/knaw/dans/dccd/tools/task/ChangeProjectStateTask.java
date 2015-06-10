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
package nl.knaw.dans.dccd.tools.task;

import java.io.IOException;
import java.util.Set;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.dataset.DatasetState;

import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.application.services.ServiceException;

import nl.knaw.dans.dccd.tools.project.ProjectStateChanger;
import nl.knaw.dans.dccd.tools.project.ProjectStateChangerListener;

import nl.knaw.dans.dccd.tools.repo.AbstractSidSetTask;
import nl.knaw.dans.dccd.tools.repo.Application;
import nl.knaw.dans.dccd.tools.repo.IdConverter;

import nl.knaw.dans.dccd.tools.exceptions.FatalException;
import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;
import nl.knaw.dans.dccd.tools.util.Dialogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes the state of the DCCD Projects, 
 * either to DRAFT or PUBLISHED (Archived)
 */
public class ChangeProjectStateTask extends AbstractSidSetTask implements ProjectStateChangerListener
{
	private static final Logger	logger	= LoggerFactory.getLogger(ChangeProjectStateTask.class);

	private final DatasetState	newState;

	private int					datasetsNotFoundCount;
	private int					datasetStateChangeCount;

	public ChangeProjectStateTask(DatasetState newState, String... idFilenames)
	{
		this(newState, null, idFilenames);
	}

	public ChangeProjectStateTask(DatasetState newState, IdConverter idConverter, String... idFilenames)
	{
		super(idConverter, idFilenames);
		this.newState = newState;
	}

	@Override
	public boolean needsAuthentication()
	{
		return true;
	}

	@Override
	public void run() throws FatalTaskException
	{
		boolean confirmed = Dialogue.confirm("Change state to " + newState + " of all projects listed in " + getIdFilenamesToString() + "?");
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
		confirmed = Dialogue.confirm("There are " + sidSet.size() + " project(s) that will be updated to state " + newState + "."
				+ "\nDo you want to continue?");
		if (!confirmed)
		{
			logger.info("Aborting " + getTaskName());
			return;
		}

		logger.info("A total of " + sidSet.size() + " project(s) will have the state changed to " + newState);

		ProjectStateChanger datasetStateChanger;
		try
		{
			datasetStateChanger = new ProjectStateChanger(user, this);
		}
		catch (NoSearchEngineListeningException e)
		{
			throw new FatalTaskException(e, this);
		}

		for (String storeId : sidSet)
		{
			try
			{
				datasetStateChanger.changeState(storeId, newState);
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

		logger.info("\n\tChanged " + datasetStateChangeCount + " projects of " + sidSet.size() + " projects in " + getIdFilenamesToString() + "."
				+ "\n\tA total of " + getOriginalIdNotFoundCount() + " originalIds was not found." + "\n\tA total of " + datasetsNotFoundCount
				+ " projects were not found on the system.");
	}

	@Override
	public void onProjectNotFound(String storeId)
	{
		datasetsNotFoundCount++;
		report("project not found", storeId);
	}

	@Override
	public void onProjectStateChanged(Project project, DatasetState oldState, DatasetState newState, String oldDobState, String newDobState)
	{
		datasetStateChangeCount++;
		report("datasetStateChanged", project.getStoreId(), oldState.toString(), newState.toString(), oldDobState, newDobState);
	}

}
