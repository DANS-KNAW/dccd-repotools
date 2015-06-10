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
package nl.knaw.dans.dccd.tools.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.dataset.DatasetState;

import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.ProjectAdministrativeMetadata;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.ServiceException;

/**
 * Change the state of datasets.
 */
public class ProjectStateChanger
{
	private static final Logger					logger	= LoggerFactory.getLogger(ProjectStateChanger.class);

	private final ProjectStateChangerListener	listener;
	private final DccdUser						user;

	public ProjectStateChanger(DccdUser user, ProjectStateChangerListener listener) throws NoSearchEngineListeningException
	{
		this.user = user;
		this.listener = listener;
		// RepoUtil.checkSearchEngineIsActive();
	}

	public void changeState(String storeId, DatasetState newState) throws RepositoryException, ServiceException
	{
		try
		{
			Project project = DccdDataService.getService().getProject(storeId);
			changeState(project, newState);
		}
		catch (DataServiceException e)
		{
			logger.info("A dataset with storeId " + storeId + " was not found");
			listener.onProjectNotFound(storeId);
		}
	}

	public void changeState(Project project, DatasetState newState) throws ServiceException
	{
		String oldDobState = project.getState();
		ProjectAdministrativeMetadata administrativeMetadata = project.getAdministrativeMetadata(); // .getAdministrativeState()

		DatasetState oldState = administrativeMetadata.getAdministrativeState();

		// only support DCCD Draft or Archived
		// DatasetState.DRAFT, or DatasetState.PUBLISHED
		if (!(newState == DatasetState.DRAFT || newState == DatasetState.PUBLISHED))
		{
			throw new ServiceException("Change to state " + newState + " not supported");
		}

		// Note also updates the last StateChanged and previous state...
		administrativeMetadata.setAdministrativeState(newState);

		try
		{
			DccdDataService.getService().updateProject(project);
		}
		catch (DataServiceException e)
		{
			throw new ServiceException(e);
		}

		String newDobState = project.getState();
		listener.onProjectStateChanged(project, oldState, newState, oldDobState, newDobState);
	}

}
