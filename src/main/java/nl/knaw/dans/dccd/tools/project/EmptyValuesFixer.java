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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasValues;

import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.ServiceException;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;
import nl.knaw.dans.dccd.model.entities.ValuesEntity;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

/**
 * Each Tridas series the original Values element 
 * (with the correct variable and unit element) 
 * got duplicated and then the real Values was added, 
 * but with the wrong variable and unit elements. 
 */
public class EmptyValuesFixer extends AbstractProjectProcessor
{
	protected static final Logger	logger	= LoggerFactory.getLogger(EmptyValuesFixer.class);
	
	public EmptyValuesFixer(DccdUser user, ProjectProcessListener listener) throws NoSearchEngineListeningException
	{
		super(user, listener);
	}

	void process(Project project) throws ServiceException
	{
		StringBuilder messageSb = new StringBuilder("");

		logger.debug("Processing project " + project.getStoreId() + " with title: " + project.getTitle());

		boolean needsUpdate = false;

		// Measurements
		List<MeasurementSeriesEntity> measurementList = project.getMeasurementSeriesEntities();
		for (MeasurementSeriesEntity series : measurementList)
		{
			logger.debug("MeasurementSeries " + series.getId());
			if (processSeries(series))
				needsUpdate = true;
		}

		// Derived
		List<DerivedSeriesEntity> derivedList = project.getDerivedSeriesEntities();
		for (DerivedSeriesEntity series : derivedList)
		{
			logger.debug("DerivedSeries " + series.getId());
			if (processSeries(series))
				needsUpdate = true;
		}

		if (needsUpdate)
		{
			messageSb.append("Fixed empty values");

			// TESTS could comment out this section or disable base on a property/configuration

			// make sure that the EntityTree stream is updated in the repository, by setting it to dirty
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

		}
		else
		{
			messageSb.append("No empty values fix needed");
		}

		listener.onProjectProcessed(project, messageSb.toString());
	}

	// TODO refactoring would benefit from having a common baseclass/interface for derived and measuerement series entity!

	private boolean processSeries(Entity series)
	{
		boolean fixed = false;

		if (!((series instanceof MeasurementSeriesEntity) || (series instanceof DerivedSeriesEntity)))
		{
			throw new IllegalArgumentException("Entity must be MeasurementSeriesEntity or DerivedSeriesEntity");
		}

		List<ValuesEntity> emptyValuesEntities = new ArrayList<ValuesEntity>();
		List<ValuesEntity> nonEmptyValuesEntities = new ArrayList<ValuesEntity>();

		separateEmptyFromNonEmptyValues(series, emptyValuesEntities, nonEmptyValuesEntities);

		// Fix BUG only when 3 values and 2 empty
		// get variable and units from the first empty one
		// and place them in the non-empty one
		if (emptyValuesEntities.size() == 2 && nonEmptyValuesEntities.size() == 1)
		{
			logger.debug("Fixing empty values of series " + series.getId());
			// copy variable and unit
			ValuesEntity emptyValuesEntity = emptyValuesEntities.get(0);
			ValuesEntity nonEmptyValuesEntity = nonEmptyValuesEntities.get(0);
			copyVariableAndUnit(emptyValuesEntity, nonEmptyValuesEntity);

			// make sure its updated
			nonEmptyValuesEntity.setDirty(true);

			// remove empty values
			removeValues(series, emptyValuesEntities);
			fixed = true;
		}
		else
		{
			logger.debug("NOT Fixing series " + series.getId());
		}

		return fixed;
	}

	private void separateEmptyFromNonEmptyValues(Entity series, List<ValuesEntity> emptyValuesEntities, List<ValuesEntity> nonEmptyValuesEntities)
	{
		if (!((series instanceof MeasurementSeriesEntity) || (series instanceof DerivedSeriesEntity)))
		{
			throw new IllegalArgumentException("Entity must be MeasurementSeriesEntity or DerivedSeriesEntity");
		}

		List<Entity> entityList = series.getDendroEntities();
		int valuesCount = 0;
		int emptyValuesCount = 0;

		// empty lists
		emptyValuesEntities.clear();
		nonEmptyValuesEntities.clear();

		for (Entity entity : entityList)
		{
			// should be all ValuesEntities, but just to be sure
			if (entity instanceof ValuesEntity)
			{
				valuesCount++;
				ValuesEntity valuesEntiy = (ValuesEntity) entity;
				// check if empty
				TridasValues tridasValues = (TridasValues) valuesEntiy.getTridasAsObject();
				if (!tridasValues.isSetValues())
				{
					emptyValuesCount++;
					emptyValuesEntities.add(valuesEntiy);
				}
				else
				{
					nonEmptyValuesEntities.add(valuesEntiy);
				}
			}
		}
		logger.debug(series.getClass().getName() + "with " + valuesCount + " values of which " + emptyValuesCount + " empty");
		int numberOfNonEmptyValues = valuesCount - emptyValuesCount;
		if (numberOfNonEmptyValues == 0)
			logger.warn("Found only empty values!");

	}

	private void copyVariableAndUnit(ValuesEntity entityFrom, ValuesEntity entityTo)
	{
		TridasValues tridasValues = (TridasValues) entityFrom.getTridasAsObject();
		TridasUnitless unitless = tridasValues.getUnitless();
		TridasUnit unit = tridasValues.getUnit();
		TridasVariable variable = tridasValues.getVariable();

		tridasValues = (TridasValues) entityTo.getTridasAsObject();
		tridasValues.setUnitless(unitless);
		tridasValues.setUnit(unit);
		tridasValues.setVariable(variable);
	}

	private void removeValues(Entity series, List<ValuesEntity> entiiesToRemove)
	{
		if (!((series instanceof MeasurementSeriesEntity) || (series instanceof DerivedSeriesEntity)))
		{
			throw new IllegalArgumentException("Entity must be MeasurementSeriesEntity or DerivedSeriesEntity");
		}

		// First empty Values
		TridasMeasurementSeries tridasSeries = (TridasMeasurementSeries) series.getTridasAsObject();
		ValuesEntity emptyValuesEntity = entiiesToRemove.get(0);
		TridasValues tridasValues = (TridasValues) emptyValuesEntity.getTridasAsObject();
		// remove from tridas
		if (tridasSeries.getValues().remove(tridasValues) == false)
		{
			logger.error("Could no remove tridas Values ");
		}
		// also remove from entities
		if (series.getDendroEntities().remove(emptyValuesEntity) == false)
		{
			logger.error("Could not remove Values Entity");
		}

		// Next empty Values
		emptyValuesEntity = entiiesToRemove.get(1);
		tridasValues = (TridasValues) emptyValuesEntity.getTridasAsObject();
		// remove from tridas
		if (tridasSeries.getValues().remove(tridasValues) == false)
		{
			logger.error("Could no remove tridas Values ");
		}

		// also remove from entities
		if (series.getDendroEntities().remove(emptyValuesEntity) == false)
		{
			logger.error("Could not remove Values Entity");
		}

		// series.buildEntitySubTree();

		// make sure its updated
		series.setDirty(true);
	}
}
