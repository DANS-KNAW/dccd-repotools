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

import java.io.IOException;
import java.util.Properties;

import javax.naming.NamingException;

import nl.knaw.dans.common.ldap.management.ApacheDSServerBuilder;
import nl.knaw.dans.dccd.application.services.DccdConfigurationService;
import nl.knaw.dans.dccd.tools.exceptions.FatalTaskException;
import nl.knaw.dans.dccd.tools.repo.AbstractTask;

public class RebuildApacheDSServerTask extends AbstractTask
{
	static Properties settings = DccdConfigurationService.getService().getSettings();
	static final String LDAP_URL = settings.getProperty("ldap.url");//"ldap://localhost:10389"
	static final String LDAP_PRINCIPAL = settings.getProperty("ldap.securityPrincipal");
	static final String LDAP_CREDENTIALS = settings.getProperty("ldap.securityCredentials");

	@Override
	public void run() throws FatalTaskException
	{
		// Run the serverbuilder 
		try
		{
			ApacheDSServerBuilder serverBuilder = new ApacheDSServerBuilder(LDAP_URL, LDAP_PRINCIPAL, LDAP_CREDENTIALS);
			serverBuilder.buildServer(); 
		}
		catch (NamingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
