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

package uk.ac.ucl.excites.sapelli.storage;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ucl.excites.sapelli.shared.db.StoreHandle;
import uk.ac.ucl.excites.sapelli.shared.db.StoreHandle.StoreCreator;
import uk.ac.ucl.excites.sapelli.shared.db.exceptions.DBException;
import uk.ac.ucl.excites.sapelli.storage.db.RecordStore;
import uk.ac.ucl.excites.sapelli.storage.model.Model;
import uk.ac.ucl.excites.sapelli.storage.model.Record;
import uk.ac.ucl.excites.sapelli.storage.model.RecordReference;
import uk.ac.ucl.excites.sapelli.storage.model.Schema;
import uk.ac.ucl.excites.sapelli.storage.queries.RecordsQuery;
import uk.ac.ucl.excites.sapelli.storage.util.UnknownModelException;

/**
 * @author mstevens
 *
 */
public abstract class StorageClient
{
	
	// DYNAMICS------------------------------------------------------
	public final StoreHandle<RecordStore> recordStoreHandle = new StoreHandle<RecordStore>(new StoreCreator<RecordStore>()
	{
		@Override
		public RecordStore createStore() throws DBException
		{
			return createRecordStore();
		}
	});
	
	public Model getModel(long modelID) throws UnknownModelException
	{
		// First check reserved models:
		for(Model model : getReservedModels())
			if(model.getID() == modelID)
				return model;
		// Get client model:
		return getClientModel(modelID);
	}
	
	/**
	 * Subclasses can override this but *must* return at least the same models returned by the super implementation.
	 * 
	 * @return
	 */
	public List<Model> getReservedModels()
	{
		return new ArrayList<Model>();
	}
	
	/**
	 * Returns the name to be used for a table which will contain records of the given schema in
	 * back-end (relational) database storage (i.e. through a RecordStore implementation).
	 * 
	 * May be overridden by subclasses to add additional exceptional cases.
	 * 
	 * @return
	 */
	public String getTableName(Schema schema)
	{
		if(schema == Model.MODEL_SCHEMA)
			return "Models";
		if(schema == Model.META_SCHEMA)
			return "Schemata";
		else
			return "Table_" + schema.getModelID() + '_' + schema.getModelSchemaNumber(); // we don't use schema#name to avoid name clashes and illegal characters
	}
		
	/**
	 * @param modelID
	 * @return
	 * @throws UnknownModelException
	 */
	protected abstract Model getClientModel(long modelID) throws UnknownModelException;
	
	/**
	 * @param schemaID
	 * @param schemaVersion
	 * @return a matching {@link Schema} instance
	 * @throws {@link UnknownModelException} when no matching schema is found
	 */
	public abstract Schema getSchemaV1(int schemaID, int schemaVersion) throws UnknownModelException;
	
	public abstract void recordInserted(Record record);
	
	public abstract void recordUpdated(Record record);
	
	public abstract void recordDeleted(Record record);
	
	public abstract void recordDeleted(RecordReference recordReference);
	
	public abstract void recordsDeleted(RecordsQuery query, int numberOfDeletedRecords);
	
	/**
	 * Returns a new RecordStore instance
	 * 
	 * @return
	 * @throws DBException
	 */
	protected abstract RecordStore createRecordStore() throws DBException;
	
}
