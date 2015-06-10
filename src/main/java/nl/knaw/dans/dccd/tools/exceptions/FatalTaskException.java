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
package nl.knaw.dans.dccd.tools.exceptions;

import nl.knaw.dans.dccd.tools.repo.Task;

/**
 * Indicates a fatal exception has occurred while executing one of the tasks. The application should stop after throwing such an exception.
 */
public class FatalTaskException extends Exception
{
	private static final long	serialVersionUID	= -1137682956431751415L;

	private final Task			thrower;

	public FatalTaskException(Task thrower)
	{
		super();
		this.thrower = thrower;
	}

	public FatalTaskException(String msg, Task thrower)
	{
		super(msg);
		this.thrower = thrower;
	}

	public FatalTaskException(Throwable e, Task thrower)
	{
		super(e);
		this.thrower = thrower;
	}

	public FatalTaskException(String msg, Throwable e, Task thrower)
	{
		super(msg, e);
		this.thrower = thrower;
	}

	public Task getThrower()
	{
		return thrower;
	}

}
