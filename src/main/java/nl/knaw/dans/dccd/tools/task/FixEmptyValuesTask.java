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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import nl.knaw.dans.dccd.model.DccdUser;

import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;
import nl.knaw.dans.dccd.tools.project.EmptyValuesFixer;
import nl.knaw.dans.dccd.tools.project.ProjectProcessor;
import nl.knaw.dans.dccd.tools.repo.AbstractProjectProcessingTask;

/**
 * This task fixes a BUG in the upload process 
 * Each Tridas series the original Values element 
 * (with the correct variable and unit element) 
 * got duplicated and then the real Values was added, 
 * but with the wrong variable and unit elements. 
 */
public class FixEmptyValuesTask extends AbstractProjectProcessingTask
{
	static final Logger	logger	= LoggerFactory.getLogger(FixEmptyValuesTask.class);

	public FixEmptyValuesTask(String... idFilenames)
	{
		super(idFilenames);
	}

	public ProjectProcessor createProcessor(DccdUser user) throws NoSearchEngineListeningException
	{
		return new EmptyValuesFixer(user, this);
	}
}
