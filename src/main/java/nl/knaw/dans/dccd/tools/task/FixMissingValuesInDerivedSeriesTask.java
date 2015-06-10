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

import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;
import nl.knaw.dans.dccd.tools.project.MissingValuesInDerivedSeriesFixer;
import nl.knaw.dans.dccd.tools.project.ProjectProcessor;
import nl.knaw.dans.dccd.tools.repo.AbstractProjectProcessingTask;

public class FixMissingValuesInDerivedSeriesTask extends AbstractProjectProcessingTask
{
	public FixMissingValuesInDerivedSeriesTask(String... idFilenames)
	{
		super(idFilenames);
	}

	@Override
	public ProjectProcessor createProcessor(DccdUser user) throws NoSearchEngineListeningException
	{
		return new MissingValuesInDerivedSeriesFixer(user, this);
	}

}
