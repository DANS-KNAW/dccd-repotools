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
import nl.knaw.dans.dccd.model.DccdOriginalFileBinaryUnit;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasMeasurementSeries;

/**
 * Detects a problem with the values files upload 
 * If the value files where uploaded and converted, the values stored in the TRiDaS, 
 * but the original value files where NOT stored. 
 */
public class MissingOriginalValueFilesDetector extends AbstractProjectProcessor
{
	private static final Logger				logger	= LoggerFactory.getLogger(MissingOriginalValueFilesDetector.class);

	public MissingOriginalValueFilesDetector(DccdUser user, ProjectProcessListener listener) throws NoSearchEngineListeningException
	{
		super(user, listener);
	}

	void process(Project project) throws ServiceException
	{
		StringBuilder messageSb = new StringBuilder("");

		logger.debug("Processing project " + project.getStoreId() + " with title: " + project.getTitle());

		// get the names of the uploaded files
		List<String> namesOfUploadedValueFiles = getNamesOfUploadedValueFiles(project);
		logger.debug("Number of uploaded value files: " + namesOfUploadedValueFiles.size());

		List<String> missingFilenames = new ArrayList<String>();
		for (String filename : namesOfUploadedValueFiles)
		{
			logger.debug("Checking uploaded value file: " + filename);

			// check if there is a Original file datastream, and when not, add it
			if (!hasOriginalFileWithName(project, filename))
			{
				logger.debug("file was not ingested yet.");
				missingFilenames.add(filename);
			}
			else
			{
				logger.debug("file was already ingested.");
			}
		}

		if (missingFilenames.isEmpty())
		{
			messageSb.append("OK");
		}
		else
		{
			messageSb.append("\"" + project.getTitle() + "\" is missing file(s): ");
			for (String filename: missingFilenames)
			{
				messageSb.append("\"" + filename + "\" ");
			}
		}
		
		listener.onProjectProcessed(project, messageSb.toString());
	}

	private boolean hasOriginalFileWithName(Project project, String filename)
	{
		boolean result = false;

		List<DccdOriginalFileBinaryUnit> units = project.getOriginalFileBinaryUnits();
		for (DccdOriginalFileBinaryUnit unit : units)
		{
			if (unit.getFileName() == null)
			{
				logger.warn("unit without a filename id: " + unit.getUnitId() + ", label: " + unit.getUnitLabel());
				continue;
			}

			if (unit.getFileName().equalsIgnoreCase(filename))// contentEquals(filename))
			{
				result = true;
				break;// found!
			}
		}

		return result;
	}

	private static List<String> getNamesOfUploadedValueFiles(Project project)
	{
		List<String> resultList = new ArrayList<String>();

		List<MeasurementSeriesEntity> measurementList = project.getMeasurementSeriesEntities();
		// only series with external file ref's
		for (MeasurementSeriesEntity measurementSeries : measurementList)
		{
			// check the genericFields
			TridasMeasurementSeries tridas = (TridasMeasurementSeries) measurementSeries.getTridasAsObject();

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
		}

		// also derived series
		List<DerivedSeriesEntity> derivedList = project.getDerivedSeriesEntities();
		// only series with external file ref's
		for (DerivedSeriesEntity derivedtSeries : derivedList)
		{
			// check the genericFields
			TridasDerivedSeries tridas = (TridasDerivedSeries) derivedtSeries.getTridasAsObject();

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
		}

		return resultList;
	}

}
