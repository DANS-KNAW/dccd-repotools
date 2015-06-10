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

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.ServiceException;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProjectProcessor implements ProjectProcessor
{

	protected static final Logger	logger	= LoggerFactory.getLogger(AbstractProjectProcessor.class);
	protected final ProjectProcessListener	listener;
	protected final DccdUser	user;

	public AbstractProjectProcessor(DccdUser user, ProjectProcessListener listener) throws NoSearchEngineListeningException
	{
		this.user = user;
		this.listener = listener;
		// RepoUtil.checkSearchEngineIsActive();
	}
	
	abstract void process(Project project) throws ServiceException;
	
	public void process(String storeId) throws RepositoryException, ServiceException
	{
		// get the project
		try
		{
			Project project = DccdDataService.getService().getProject(storeId);
			process(project);
		}
		catch (DataServiceException e)
		{
			logger.info("A project with storeId \"" + storeId + "\" was not found", e);
			listener.onProjectNotFound(storeId);
		}
	}

}
