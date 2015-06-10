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

import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.dccd.application.services.ServiceException;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.ValuesEntity;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasGenericField;

public class MissingValuesInDerivedSeriesDetector extends AbstractProjectProcessor
{

	public MissingValuesInDerivedSeriesDetector(DccdUser user, ProjectProcessListener listener) throws NoSearchEngineListeningException
	{
		super(user, listener);
	}

	@Override
	void process(Project project) throws ServiceException
	{
		logger.debug("Checking: " + project.getStoreId());
		StringBuilder messageSb = new StringBuilder("Project: " + project.getStoreId());
		
		List<String> missing = new ArrayList<String>();
		
		List<DerivedSeriesEntity> derivedList = project.getDerivedSeriesEntities();
		for (DerivedSeriesEntity series : derivedList)
		{
			int valuesCounter = 0;
			List<Entity> entityList = series.getDendroEntities();
			for (Entity entity : entityList)
			{
				if (entity instanceof ValuesEntity)
					valuesCounter++;// found one!
			}
			
			// has it got any values?
			if (valuesCounter == 0)
			{
				// no values
				// but has it got that genericfield...?
				List<String> resultList = new ArrayList<String>();
				TridasDerivedSeries tridas = (TridasDerivedSeries) series.getTridasAsObject();
				if (tridas.isSetGenericFields())
				{
					// Ok we have potential candidates
					List<TridasGenericField> fields = tridas.getGenericFields();
					for (TridasGenericField field : fields)
					{
						if (field.isSetValue() && field.isSetName() && Project.DATAFILE_INDICATOR_UPLOADED.equalsIgnoreCase(field.getName()))
						{
							resultList.add(field.getValue());
						}
					}
				}
				// but I only want one?
				if (!resultList.isEmpty())
				{
					//just the first one and WARN if more?
					// missed this one
					logger.debug("missing values for uploaded file: " + resultList.get(0));
					missing.add(resultList.get(0));
				}
			}
		}
		
		// report
		if (missing.isEmpty())
		{
			messageSb.append(" -> OK");
		}
		else
		{
			messageSb.append(" -> missing values for: ");
			for(String filename : missing)
			{
				messageSb.append("\n\t " + filename);
			}
		}
		
		listener.onProjectProcessed(project, messageSb.toString());
	}

}
