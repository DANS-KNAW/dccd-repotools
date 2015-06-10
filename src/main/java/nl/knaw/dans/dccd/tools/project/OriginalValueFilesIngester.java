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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasMeasurementSeries;

import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.ServiceException;
import nl.knaw.dans.dccd.model.DccdOriginalFileBinaryUnit;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

/**
 * Fixes a problem resulting from a BUG in the values files upload 
 * The value files where uploaded and converted, the values where also stored in the TRiDaS, 
 * but the original value files where NOT stored. 
 * Need to get the files from a folder with a name derived from the SID, 
 * the colon replaced by an underscore for example 
 * dccd:801 -> data/dccd_801
 */
public class OriginalValueFilesIngester extends AbstractProjectProcessor
{
	private static final Logger				logger	= LoggerFactory.getLogger(OriginalValueFilesIngester.class);

	public OriginalValueFilesIngester(DccdUser user, ProjectProcessListener listener) throws NoSearchEngineListeningException
	{
		super(user, listener);
	}

	void process(Project project) throws ServiceException
	{
		StringBuilder messageSb = new StringBuilder("File(s) added: ");

		logger.debug("Processing project " + project.getStoreId() + " with title: " + project.getTitle());

		String foldername = getFolderName(project);
		logger.debug("Original files located at: " + foldername);

		// get the names of the uploaded files
		List<String> namesOfUploadedValueFiles = getNamesOfUploadedValueFiles(project);
		logger.debug("Number of uploaded value files: " + namesOfUploadedValueFiles.size());

		for (String filename : namesOfUploadedValueFiles)
		{
			logger.debug("Checking uploaded value file: " + filename);

			// check if there is a Original file datastream, and when not, add it
			if (!hasOriginalFileWithName(project, filename))
			{
				logger.debug("file was not ingested yet, try adding it...");
				// add it
				File treeRingDataFile = findOriginalFile(foldername, filename);
				if (treeRingDataFile == null)
				{
					logger.warn("file " + filename + " was NOT available, skipping it.");
					// TODO add to the message

					continue;
				}

				try
				{
					// NOTE the naming of the datastream 
					// assumes that the latest added ends with the size
					// ORIGINAL_FILE_3 is the third being added, 
					// so if one in the middle is missing we are in trouble
					project.addOriginalFile(treeRingDataFile);
					messageSb.append("\t" + filename);
				}
				catch (IOException e)
				{
					throw new ServiceException(e);
				}
			}
			else
			{
				logger.debug("file was already ingested, skipping it.");
			}
		}

		// update the project in the store
		try
		{
			DccdDataService.getService().updateProject(project);
		}
		catch (DataServiceException e)
		{
			throw new ServiceException(e);
		}

		listener.onProjectProcessed(project, messageSb.toString());
	}

	String getFolderName(Project project)
	{
		// use the SID, but replace the colon with an underscore
		String foldername = project.getStoreId();

		foldername = foldername.replaceAll(":", "_");
		return foldername;
	}

	private File findOriginalFile(String foldername, String filename)
	{
		// what is the location of the "data" folder
		String dataFolderName = "data";

		// Note: The actual filename could have differences in case
		// Therefore find the file with the requested name, but ignore case
		File orgfilesDirectory = new File(dataFolderName + File.separator + foldername);
		File fileFound = null;
		File[] listFiles = orgfilesDirectory.listFiles();
		for (int i = 0; i < listFiles.length; i++)
		{
			if (listFiles[i].getName().compareToIgnoreCase(filename) == 0)
			{
				fileFound = listFiles[i];
				break; // found!
			}
		}

		return fileFound;
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
