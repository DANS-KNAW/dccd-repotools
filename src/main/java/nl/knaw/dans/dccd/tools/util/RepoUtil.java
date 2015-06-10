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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import java.util.List;

//import nl.knaw.dans.common.lang.repo.DmoStoreEventListener;
//import nl.knaw.dans.common.lang.reposearch.RepoSearchListener;
//import nl.knaw.dans.easy.data.Data;
import nl.knaw.dans.dccd.tools.exceptions.NoSearchEngineListeningException;

public class RepoUtil
{
	private static final Logger	logger	= LoggerFactory.getLogger(RepoUtil.class);

	private RepoUtil()
	{
		// static class
	}

	public static void checkSearchEngineIsActive() throws NoSearchEngineListeningException
	{
		// TODO implement this for the DCCD
    	/*
        List<DmoStoreEventListener> listeners = Data.getEasyStore().getListeners();
        boolean searchEngineActive = false;

        for (DmoStoreEventListener listener : listeners)
        {
            logger.info("Events will be fired at " + listener);
            if (listener instanceof RepoSearchListener)
            {
                searchEngineActive = true;
            }
        }
        if (!searchEngineActive)
        {
            String msg = "Purge dataset event will not be fired at searchEngine. Aborting operation.";
            throw new NoSearchEngineListeningException(msg);
        }
    	 */
	}

}
