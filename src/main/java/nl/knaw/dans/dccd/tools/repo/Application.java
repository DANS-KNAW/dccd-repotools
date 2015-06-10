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

import nl.knaw.dans.dccd.tools.exceptions.FatalRuntimeException; // import nl.knaw.dans.easy.tools.util.Dialogue;
import nl.knaw.dans.dccd.tools.util.Dialogue;
import nl.knaw.dans.dccd.tools.util.Printer;

import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

// NOTE this class has the domain (DCCD) specific stuff, not likely to be commonized
import nl.knaw.dans.dccd.application.services.DccdUserService;
import nl.knaw.dans.dccd.application.services.UserServiceException;
import nl.knaw.dans.dccd.authn.UsernamePasswordAuthentication;
import nl.knaw.dans.dccd.model.DccdUser;

/**
 * 
 */
public class Application
{
	private static String				APP_CONTEXT;

	public static final String			BN_TASKRUNNER	= "taskRunner";

	private static Application			INSTANCE;

	private static DccdUser				APPLICATION_USER;

	private final ApplicationContext	context;

	private final DateTime				startDate;

	public static void setContext(String contextFilename)
	{
		APP_CONTEXT = contextFilename;
	}

	public static DateTime getStartDate()
	{
		return getInstance().startDate;
	}

	public static TaskRunner getTaskRunner()
	{
		return (TaskRunner) getInstance().context.getBean(BN_TASKRUNNER);
	}

	private static Application getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new Application(APP_CONTEXT);
		}
		return INSTANCE;
	}

	private Application(String contextFilename)
	{
		startDate = new DateTime();
		context = new FileSystemXmlApplicationContext(contextFilename);
		((AbstractApplicationContext) context).registerShutdownHook();
	}

	public static DccdUser authenticate()
	{
		if (APPLICATION_USER == null)
		{
			Printer.println("Authentication required");

			try
			{
				boolean authenticated = false;
				int tryCount = 0;
				String username = null;
				String pass;
				while (!authenticated && tryCount < 3)
				{
					tryCount++;
					username = Dialogue.getInput("username:");
					pass = Dialogue.readPass("password:");

					// authenticated = Data.getUserRepo().authenticate(username, pass);
					UsernamePasswordAuthentication authentication = DccdUserService.getService().newUsernamePasswordAuthentication();
					authentication.setUserId(username);
					authentication.setCredentials(pass);
					DccdUserService.getService().authenticate(authentication);
					if (authentication.isCompleted())
						authenticated = true;

					if (!authenticated)
					{
						System.out.println("Invalid username or pass.");
					}
				}
				if (!authenticated)
				{
					throw new FatalRuntimeException("Invalid username or pass.");
				}
				else
				{
					APPLICATION_USER = DccdUserService.getService().getUserById(username);
				}
				// checkSecurityConfirmation();

				// Maybe only allow when ADMIN ???
			}
			catch (UserServiceException e)
			{
				throw new FatalRuntimeException("Unable to authenticate", e);
			}
		}
		return APPLICATION_USER;
	}

	// private static void checkSecurityConfirmation()
	// {
	// 	if (Security.getAuthz() instanceof NoAuthz)
	// 	{
	// 		boolean confirmed = Dialogue.confirm("Warn! Security implemented by " + Security.getAuthz() + "\nDo you want to continue?");
	// 		if (!confirmed)
	// 		{
	// 			throw new AbortException("Aborted by user.");
	// 		}
	// 	}
	// }

}
