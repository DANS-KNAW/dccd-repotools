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

import java.util.List;

import net.opengis.gml.schema.PointType;
import net.opengis.gml.schema.Pos;
import nl.knaw.dans.dccd.application.services.DataServiceException;
import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.ServiceException;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.ObjectEntity;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasObject;

public class ProjectCoordinateSwapper extends AbstractProjectProcessor
{

	public ProjectCoordinateSwapper(DccdUser user, ProjectProcessListener listener) throws NoSearchEngineListeningException
	{
		super(user, listener);
	}

	@Override
	void process(Project project) throws ServiceException
	{
		logger.debug("report for: " + project.getStoreId());
		
		StringBuilder messageSb = new StringBuilder("");
		
		// Store id
		messageSb.append("\"" + project.getStoreId() + "\", ");
		
		//title=
		messageSb.append("\"" + project.getTitle() + "\", ");
//		
//		DatasetState status = project.getAdministrativeMetadata().getAdministrativeState();
//		//status=
//		messageSb.append("\"" + status + "\", ");
//
//		//ownerId=
//		messageSb.append("\"" + project.getOwnerId() + "\", ");
//		
//		String orgId = project.getCreationMetadata().getOrganisation().getId();
//		//organisationId=
//		messageSb.append("\"" + orgId + "\"");
	
		// get all object entities
		List<Entity> entityList = project.entityTree.getEntities();
		for(Entity entity : entityList)
		{
			if (entity instanceof ObjectEntity)
			{
				ObjectEntity objectEntity = (ObjectEntity)entity;
				TridasObject tridasObject = (TridasObject) objectEntity.getTridasAsObject();
				if (tridasObject.isSetLocation())
				{
					TridasLocation location = tridasObject.getLocation();					
					// Handle geo location; lng, lat
					if (location.isSetLocationGeometry())
					{
						TridasLocationGeometry locationGeometry = location.getLocationGeometry();
						// for now only use Points 
						if (locationGeometry.isSetPoint() && locationGeometry.getPoint().isSetPos()) 
						{
							PointType point = locationGeometry.getPoint();
							Pos pos = point.getPos();
							// we need the two first coordinates
							if (pos.isSetValues() && pos.getValues().size() > 1)
							{
								List<Double> values = pos.getValues();
								// swap
								double val_0 = values.get(0);
								values.set(0, values.get(1));
								values.set(1, val_0);
								
								messageSb.append("\"" + "swapped to (" +  values.get(0) + ","+ values.get(1) + ")\", ");
								
//								objectEntity.setDirty(true); // ?? won't the hash work??? 
							}
						}
					}
				}
			}
		}
		
		// update the project
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

}
