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
import nl.knaw.dans.dccd.tools.project.OriginalValueFilesIngester;
import nl.knaw.dans.dccd.tools.project.ProjectProcessor;
import nl.knaw.dans.dccd.tools.repo.AbstractProjectProcessingTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * When the Values file where uploaded and converted, 
 * but due to a BUG not ingested as original files. 
 * This Task must get these files and ingest them as Fedora.
 * datastream (ORIGINAL_FILE_) into the Project's digital object. 
 * It should check which files where uploaded and are not ingested allready.
 */
public class IngestOriginalValueFilesTask extends AbstractProjectProcessingTask
{
	private static final Logger	logger	= LoggerFactory.getLogger(IngestOriginalValueFilesTask.class);

	public IngestOriginalValueFilesTask(String... idFilenames)
	{
		super(idFilenames);
	}

	public ProjectProcessor createProcessor(DccdUser user) throws NoSearchEngineListeningException
	{
		return new OriginalValueFilesIngester(user, this);
	}
}
