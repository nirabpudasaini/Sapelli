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

package uk.ac.ucl.excites.sapelli.storage.util;

/**
 * @author mstevens
 *
 */
public class UnknownModelException extends Exception
{

	private static final long serialVersionUID = 2L;

	private Long modelID;
	private Integer schemaID;
	private Integer schemaVersion;
	
	/**
	 * @param modelID
	 */
	public UnknownModelException(long modelID)
	{
		super(String.format("Unknown model (ID = %d).", modelID));
		this.modelID = modelID;
	}
	
	/**
	 * @param schemaID
	 * @param schemaVersion
	 */
	public UnknownModelException(int schemaID, int schemaVersion)
	{
		super(String.format("Unknown model/schema (schemaID = %d; schemaVersion = %d).", schemaID, schemaVersion));
		this.schemaID = schemaID;
		this.schemaVersion = schemaVersion;
	}

	/**
	 * @return the modelID
	 */
	public Long getModelID()
	{
		return modelID;
	}

	/**
	 * @return the schemaID
	 */
	public Integer getSchemaID()
	{
		return schemaID;
	}

	/**
	 * @return the schemaVersion
	 */
	public Integer getSchemaVersion()
	{
		return schemaVersion;
	}
	
}