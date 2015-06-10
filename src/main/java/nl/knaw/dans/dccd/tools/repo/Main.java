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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entrance, with commandline argument handling
 */
public class Main 
{
	private static final Logger	logger	= LoggerFactory.getLogger(Main.class);

	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.err.println("Missing required parameter: spring context filename.");
			System.exit(-1);
		}

		String contextFilename = args[0];
		Application.setContext(contextFilename);
		logger.info("Started application with context " + contextFilename + " at " + Application.getStartDate().toString("yyyy-MM-dd-HH:mm:ss"));
		Application.getTaskRunner().execute();
	}
}
