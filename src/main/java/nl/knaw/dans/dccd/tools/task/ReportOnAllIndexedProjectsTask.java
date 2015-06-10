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

import java.io.StringWriter;

import nl.knaw.dans.common.lang.search.SearchHit;
import nl.knaw.dans.common.lang.search.SearchResult;
import nl.knaw.dans.common.lang.search.SortOrder;
import nl.knaw.dans.common.lang.search.simple.SimpleSearchRequest;
import nl.knaw.dans.common.lang.search.simple.SimpleSortField;
//import nl.knaw.dans.common.wicket.components.search.model.SearchRequestBuilder;
import nl.knaw.dans.dccd.application.services.DccdSearchService;
import nl.knaw.dans.dccd.application.services.SearchServiceException;
import nl.knaw.dans.dccd.search.DccdProjectSB;
import nl.knaw.dans.dccd.search.DccdSB;
import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;
import nl.knaw.dans.dccd.tools.repo.AbstractTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportOnAllIndexedProjectsTask extends AbstractTask
{	
	protected static final Logger	logger	= LoggerFactory.getLogger(ReportOnAllIndexedProjectsTask.class);

	// use double quotes and separate with a comma
	static final String STR_RESULT_START = "\"";
	static final String STR_RESULT_FIELD_SEPARATOR = "\",\"";
	static final String STR_RESULT_END = "\"";

	@Override
	public void run() throws FatalTaskException
	{
		//logger.debug("Start running task...");
		
		// search and process results in parts, a bit like paging
		final int partSize = 100;
		SearchResult<? extends DccdSB> searchResults;
		try 
		{
			searchResults = getResultsFor(0, partSize);
		} 
		catch (SearchServiceException e1) 
		{
			throw new FatalTaskException("Cannot search.", e1, this);
		}
		processResults(searchResults);
		
		int totalNumberOfHits = searchResults.getTotalHits();
		System.out.println("total: " + totalNumberOfHits);
		
		if (totalNumberOfHits > partSize)
		{
			// be nice and wait a while
			final int DELAY_MILLISECONDS = 1000;
			try 
			{
				Thread.sleep(DELAY_MILLISECONDS);
			} 
			catch (InterruptedException e) 
			{
				throw new FatalTaskException("Cannot sleep.", e, this);
			}
			
			// do the rest
			int numberOfParts = (totalNumberOfHits + partSize -1)/partSize; // round up
			for (int partIndex = 1; partIndex < numberOfParts; partIndex++)
			{
				int offset = partIndex * partSize;

				System.out.println("offset next part: " + offset);
				try 
				{
					searchResults = getResultsFor(offset, partSize);
				} 
				catch (SearchServiceException e) 
				{
					throw new FatalTaskException("Cannot search.", e, this);
				}
				
				processResults(searchResults);
			}
		}		
	}
	
	private SearchResult<? extends DccdSB> getResultsFor(final int offset, final int limit) throws SearchServiceException
	{
		SearchResult<? extends DccdSB> searchResults = null;
		
		SimpleSearchRequest request = new SimpleSearchRequest();
		request.setLimit(limit)	;
		request.setOffset(offset);
		//Show Project and not the standard Object result
		request.addFilterBean(DccdProjectSB.class);
		request.addSortField(new SimpleSortField(DccdProjectSB.OWNER_ID_NAME, SortOrder.ASC));
		request.addSortField(new SimpleSortField(DccdProjectSB.PID_NAME, SortOrder.ASC));
		//logger.debug("Start searching...");
		searchResults = DccdSearchService.getService().doSearch(request);
		//logger.debug("number of projects found: " + searchResults.getTotalHits());

		return searchResults;
	}
	
	private void processResults(final SearchResult<? extends DccdSB> searchResults) 
	{
		
		for(SearchHit<? extends DccdSB> hit : searchResults.getHits())
		{
			DccdSB dccdSB = hit.getData();
			
			String resultStr = getResultAsString(dccdSB);
			System.out.println(resultStr);
			
			report(resultStr);
		}
	}
	
	private String getResultAsString(final DccdSB dccdSB)
	{
		java.io.StringWriter sw = new StringWriter();
		
		sw.append(STR_RESULT_START);
		
		sw.append(dccdSB.getPid());
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(dccdSB.getOwnerId());
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(dccdSB.getAdministrativeState());
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(dccdSB.getAdministrativeStateLastChange().toString());
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(dccdSB.getPermissionDefaultLevel());
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(dccdSB.getTridasProjectIdentifier());
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(escapeString(dccdSB.getTridasProjectTitle()));
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(dccdSB.hasLat()?dccdSB.getLat().toString():"");
		sw.append(STR_RESULT_FIELD_SEPARATOR);
		sw.append(dccdSB.hasLng()?dccdSB.getLng().toString():"");
		
		sw.append(STR_RESULT_END);
		
		return sw.toString();
	}
	
	private static final String escapeString(final String str)
	{
		// double quotes
		return str.replaceAll("\"", "\\\\\"");
	}
}
