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
 * General indication of a fatal exception. The Application should stop after this exception has been thrown.
 */
public class FatalException extends Exception
{
	private static final long	serialVersionUID	= 6727672012197161958L;

	public FatalException()
	{
	}

	public FatalException(String msg)
	{
		super(msg);
	}

	public FatalException(Throwable cause)
	{
		super(cause);
	}

	public FatalException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

}
