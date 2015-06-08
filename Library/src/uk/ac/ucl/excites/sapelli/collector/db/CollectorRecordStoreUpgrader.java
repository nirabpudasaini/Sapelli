/**
 * Sapelli data collection platform: http://sapelli.org
 * 
 * Copyright 2012-2014 University College London - ExCiteS group
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package uk.ac.ucl.excites.sapelli.collector.db;

import uk.ac.ucl.excites.sapelli.collector.CollectorClient;
import uk.ac.ucl.excites.sapelli.shared.db.StoreHandle.StoreUser;
import uk.ac.ucl.excites.sapelli.shared.db.exceptions.DBException;
import uk.ac.ucl.excites.sapelli.storage.db.sql.SQLRecordStore;
import uk.ac.ucl.excites.sapelli.storage.db.sql.Upgrader;

/**
 * @author mstevens
 *
 */
public class CollectorRecordStoreUpgrader extends Upgrader implements StoreUser
{
	
	private final CollectorClient client;

	public CollectorRecordStoreUpgrader(CollectorClient client)
	{
		this.client = client;
	}
	
	@Override
	public void upgrade(SQLRecordStore<?, ?, ?> recordStore, int oldVersion, int newVersion) throws DBException
	{
		//List<Record> convertedRecords = new ArrayList<Record>();
		
		// TODO add upgrade routines here
		
		// Persist dropped tables:
		//cleanup(recordStore);
		
		// Add converted records:
		//recordStore.store(convertedRecords);
	}
	
}
