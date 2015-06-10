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

import junit.framework.TestCase;
import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;
import nl.knaw.dans.dccd.tools.task.ReportOnAllIndexedProjectsTask;


public class TaskOnlineTest extends TestCase {
	
	public void testReportOnAllIndexedProjectsTask()
	{
		ReportOnAllIndexedProjectsTask task = new ReportOnAllIndexedProjectsTask();
		try {
			task.run();
		} catch (FatalTaskException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
