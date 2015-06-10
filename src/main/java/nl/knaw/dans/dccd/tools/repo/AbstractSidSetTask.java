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
package nl.knaw.dans.dccd.tools.repo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import nl.knaw.dans.dccd.tools.exceptions.FatalException;
import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;

/**
 * Abstract task that can read or convert a set of system id's from files. When the id's need to be converted for use in the repository; this can be done as
 * well
 */
public abstract class AbstractSidSetTask extends AbstractTask
{

	private final String[]		idFilenames;
	private final IdConverter	idConverter;

	private int					originalIdNotFoundCount;

	public AbstractSidSetTask(String... idFilenames)
	{
		this(null, idFilenames);
	}

	public AbstractSidSetTask(IdConverter idConverter, String... idFilenames)
	{
		this.idConverter = idConverter;
		this.idFilenames = idFilenames;
	}

	protected Set<String> loadSids() throws FatalTaskException, IOException, FatalException
	{
		Set<String> sidSet = new LinkedHashSet<String>();
		for (String idFilename : idFilenames)
		{
			readFile(idFilename, sidSet);
		}
		return sidSet;
	}

	private void readFile(String idFilename, Set<String> sidSet) throws FatalTaskException, IOException, FatalException
	{
		System.out.print("Reading file: " + idFilename);
		int count = 0;
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(idFilename, "r");
			String id = null;
			while ((id = raf.readLine()) != null)
			{
				// skip comment and empty lines
				if (!id.startsWith("#") && !id.trim().isEmpty())
				{
					addSid(idFilename, sidSet, id.trim());
					if (count++ % 10 == 0)
					{
						System.out.print(".");
					}
				}
			}
			System.out.println();
		}
		catch (IOException e)
		{
			throw new FatalTaskException("Unable to read id list: " + idFilename, this);
		}
		finally
		{
			if (raf != null)
			{
				raf.close();
			}
		}
	}

	private void addSid(String idFilename, Set<String> sidSet, String id) throws FatalException
	{
		if (idConverter == null)
		{
			sidSet.add(id);
		}
		else
		{
			List<String> list = idConverter.convert(id);
			if (list.isEmpty())
			{
				onOriginalIdNotConverted(idFilename, id);
			}
			else
			{
				sidSet.addAll(list);
			}
		}
	}

	protected void onOriginalIdNotConverted(String idFilename, String originalId)
	{
		originalIdNotFoundCount++;
		report("original id not converted", idFilename, originalId);
	}

	public int getOriginalIdNotFoundCount()
	{
		return originalIdNotFoundCount;
	}

	public String[] getIdFilenames()
	{
		return idFilenames;
	}

	public String getIdFilenamesToString()
	{
		return Arrays.deepToString(idFilenames);
	}

	public IdConverter getIdConverter()
	{
		return idConverter;
	}

}
