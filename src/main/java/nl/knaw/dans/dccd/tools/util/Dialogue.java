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
package nl.knaw.dans.dccd.tools.util;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

import nl.knaw.dans.dccd.tools.exceptions.FatalRuntimeException;

/**
 * Commandline interface for input like username and password
 */
public class Dialogue
{

	public static boolean confirm(String question)
	{
		System.out.print(question + " [y][n] ");
		String answer = readInput();
		boolean confirmed = answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
		return confirmed;
	}

	public static String getInput(String prompt)
	{
		System.out.print(prompt + " ");
		String answer = readInput();
		return answer;
	}

	private static String readInput()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input;
		try
		{
			input = br.readLine();
		}
		catch (IOException e)
		{
			throw new FatalRuntimeException("Cannot read user input", e);
		}

		return input;
	}

	public static String readPass(String prompt)
	{
		Console c = System.console();
		if (c == null)
		{
			System.out.println("Warning! Input for password not concealed.");
			return getInput(prompt);
		}
		else
		{
			char[] pass = c.readPassword(prompt + " ");
			return String.valueOf(pass);
		}

	}

}
