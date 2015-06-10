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

/**
 * Indicates user aborts execution.
 */
public class AbortException extends RuntimeException
{
	private static final long	serialVersionUID	= -1795980479717783998L;

	public AbortException()
	{
	}

	public AbortException(String msg)
	{
		super(msg);
	}

	public AbortException(Throwable cause)
	{
		super(cause);
	}

	public AbortException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
