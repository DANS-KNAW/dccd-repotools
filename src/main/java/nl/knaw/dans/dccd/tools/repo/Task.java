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

import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;

/**
 * Executes a task in the configuration of a repository-system.
 * <p/>
 * In order to start up a new repository-system, a lot of configuration needs to be done. A lot of it can be automated. Each Task carries out a small step in
 * the configuration of the repository-system.
 * <p/>
 * A Task should be repeatable. Running the same Task over and over again
 * <ul>
 * <li>should leave the repository in a consistent state as far as this Task's responsibility stretches;</li>
 * <li>should not lead to doubling of resources;</li>
 * <li>if settings change between two consecutive runs, should undo previous changes and configure the repository according to the new settings.</li>
 * </ul>
 * 
 * @author ecco
 */
public interface Task
{

	void run() throws FatalTaskException;

	int getErrorCount();

	String getTaskName();

	boolean needsAuthentication();

	String getReportFile();

}
