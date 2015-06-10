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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.common.lang.util.FileUtil;
import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.ServiceException;
import nl.knaw.dans.dccd.application.services.TreeRingDataFileService;
import nl.knaw.dans.dccd.application.services.TreeRingDataFileServiceException;
import nl.knaw.dans.dccd.model.DccdOriginalFileBinaryUnit;
import nl.knaw.dans.dccd.model.DccdTreeRingData;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.EntityTree;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.ValuesEntity;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasValues;

public class MissingValuesInDerivedSeriesFixer extends AbstractProjectProcessor
{

	public MissingValuesInDerivedSeriesFixer(DccdUser user, ProjectProcessListener listener) throws NoSearchEngineListeningException
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
					
					// FIX it
					boolean result = fixIt(project, series, resultList.get(0));
					// TODO log if success or failure
				}
			}
		}
		
		// rebuild entity tree
		// Value entities might have been added => 
		// Recreate the whole entity tree no matter what is already there!
		EntityTree entityTree = project.entityTree;
		entityTree.buildTree(project.getTridas());
		// make sure that the EntityTree stream is also updated in the repository, by setting it to dirty
		project.entityTree.setDirty(true);

		// update the project in the store
		try
		{
			DccdDataService.getService().updateProject(project);
		}
		catch (DataServiceException e)
		{
			throw new ServiceException(e);
		}

		// report
		if (missing.isEmpty())
		{
			messageSb.append(" -> OK, no need to fix");
		}
		else
		{
			messageSb.append(" -> was fixing missing values for: ");
			for(String filename : missing)
			{
				messageSb.append("\n\t " + filename);
			}
		}
		
		listener.onProjectProcessed(project, messageSb.toString());
	}

	private boolean fixIt(Project project, DerivedSeriesEntity series, String filename)
	{
		boolean success = true;
		
		// TODO
		//- haal ORG_FILE op uit de store
		//- converteer naar TRiDaS
		//- haal values uit tridas en plak ze in de derived series
		//- update de series / store

		DccdOriginalFileBinaryUnit unit = findOriginalFileBinaryUnitWithName(project, filename);
		
		if (unit == null)
		{
			
			// NO unit found; Could not be fixed!
			logger.error("unit not found for filename: " + filename);
		}
		else
		{
			String id = unit.getUnitId();
			// get the url
			URL fileURL = DccdDataService.getService().getFileURL(project.getSid(), id);
			logger.debug("URL: " + fileURL);
			
			File tempDirectory = null;
			InputStream in;
			try
			{
				in = fileURL.openStream();
			
				// We need a File for the conversion, so create one in a temp directory...
				tempDirectory = createOrgfilesTempDirectory();
				String dstFilename = tempDirectory.getPath() + File.separator + filename;
				
		        FileOutputStream out = new FileOutputStream(dstFilename);//Overwrite the file.
		        byte[] buf = new byte[16*1024];// what is the optimal size?
		        int len;
		        while ((len = in.read(buf)) > 0){
		          out.write(buf, 0, len);
		        }
				out.close();
				
				// now feed it to he conversion?
				String formatString = "Heidelberg"; // assume this is correct!!!!!
				File file = new File(dstFilename);
				DccdTreeRingData treeringData = TreeRingDataFileService.load(file, formatString);
				
				// get the values from the produced project
				addDataToDerivedSeriesEntity(series, treeringData);
				
				// set this entity to dirty... or is that automatic?
				// make sure its updated
//				series.setDirty(true);
				
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Fix failed: could not covert data ");
				success = false;
			}
			catch (TreeRingDataFileServiceException e)
			{
				logger.error("Fix failed: could not covert data", e);
				success = false;
			}
			finally
			{
				// cleanup
				if (tempDirectory != null)
				{
					try
					{
						FileUtil.deleteDirectory(tempDirectory);
					}
					catch (IOException e)
					{
					}
				}
				
			}
		}
		
		return success;
	}
	
	private DccdOriginalFileBinaryUnit findOriginalFileBinaryUnitWithName(Project project, String filename)
	{
		DccdOriginalFileBinaryUnit foundUnit = null;

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
				foundUnit = unit;
				break;// found!
			}
		}

		return foundUnit;
	}
	
	public File createOrgfilesTempDirectory()
	{
		final String FOLDER_PREFIX = "dccd_upload_original_files";
		
		File orgfilesTempDirectory = null;
		
		// construct tmp folder nane
		// for storing original files
		// get the system temp folder
		String tempDir = System.getProperty("java.io.tmpdir");
		//String orgFilesDir = tempDir + File.pathSeparator + 
		//					"dccd-upload-original-files-"+ Session.get().getId();
		//?
		// make sure file is on disk and in the right location
		
		try
		{
			orgfilesTempDirectory = FileUtil.createTempDirectory( new File(tempDir), FOLDER_PREFIX);
		}
		catch (IOException e)
		{
			logger.error("Could not create folder for original files", e);
			// What to do about it?
		}
		
		logger.info("using temp dir for original files: " + orgfilesTempDirectory.getPath());
		
		return orgfilesTempDirectory;
	}

	private void addDataToDerivedSeriesEntity(DerivedSeriesEntity derivedSeries, DccdTreeRingData treeringData)
	{
		List<TridasValues> tridasValuesList = treeringData.getTridasValuesForDerivedSeries();

		// Note that each TridasValues instance has a group (or list) of value instances
		logger.debug("Found groups of values: " + tridasValuesList.size());

		if (tridasValuesList.isEmpty())
		{
			logger.warn("NO derived series values found in uploaded TreeRingData: " + treeringData.getFileName());
			return; // nothing to do
		}

		addValuesToSeriesEntity((Entity) derivedSeries, tridasValuesList);
	}

	private void addValuesToSeriesEntity (Entity series,
			List<TridasValues> tridasValuesList)
	{
		// Get a list of all the (empty) values subelements 
		// and fill them with the given ones, then if any are left create new ones

		// Get all 'empty' values (placeholders) and try to fill those
		List<TridasValues> placeholderTridasValues = getEmptyTridasValues(series);
		
		// calculate the number of values to add to the placeholders
		int numberOfValuesToAdd = tridasValuesList.size();
		int numberOfPlaceholders = placeholderTridasValues.size();
		int numberOfValuesToAddToPlaceholders = numberOfValuesToAdd; // only fill what we have
		if (numberOfValuesToAdd > numberOfPlaceholders)
			numberOfValuesToAddToPlaceholders = numberOfPlaceholders; // fill all placeholders
		
		// add to placeholders
		for(int i=0; i < numberOfValuesToAddToPlaceholders; i++)
		{
			placeholderTridasValues.get(i).getValues().addAll(tridasValuesList.get(i).getValues());
		}
		
		// if there is any left to add, create new entities for them
		if (numberOfValuesToAdd > numberOfPlaceholders)
		{
			for(int i=numberOfPlaceholders; i < numberOfValuesToAdd; i++)
			{
				TridasValues tridasValues = tridasValuesList.get(i);
				ValuesEntity valuesEntity = new ValuesEntity(tridasValues);
				series.getDendroEntities().add(valuesEntity);
				
				// Add to the tridas
				// Note: we can't use series.connectTridasObjectTree(), 
				// because that would duplicate existing values
				Object tridasAsObject = series.getTridasAsObject();
				// should implement ITridasSeries
				((ITridasSeries)tridasAsObject).getValues().add(tridasValues);
			}
		}
	}
	
	private List<TridasValues> getEmptyTridasValues(Entity series)
	{
		List<TridasValues> emptyValues = new ArrayList<TridasValues>();
		
		// assume series entity and filter subentities for Values entity
		List<Entity> subEntities = series.getDendroEntities();
		for(Entity subEntity : subEntities)
		{
			if (subEntity instanceof ValuesEntity)
			{
				TridasValues valuesFromEntity = (TridasValues) subEntity.getTridasAsObject();
				if (!valuesFromEntity.isSetValues())
				{
					emptyValues.add(valuesFromEntity);
				}
			}
		}
		
		return emptyValues;
	}

}
