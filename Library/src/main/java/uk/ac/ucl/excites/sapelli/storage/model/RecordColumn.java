/**
 * Sapelli data collection platform: http://sapelli.org
 *
 * Copyright 2012-2016 University College London - ExCiteS group
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


package uk.ac.ucl.excites.sapelli.storage.model;

/**
 * A composite column for storing Records of a specific Schema.
 *
 * @author mstevens
 */
public class RecordColumn extends ValueSetColumn<Record, Schema>
{

	public RecordColumn(String name, Schema schema, boolean optional)
	{
		super(name, schema, optional);
	}

	public RecordColumn(String name, Schema schema, boolean optional, Record defaultRecord)
	{
		super(name, schema, optional, defaultRecord);
	}

	public RecordColumn(String name, Schema schema, boolean optional, boolean includeSkipColsInStringSerialisation, boolean includeVirtualColsInStringSerialisation)
	{
		super(name, schema, optional, includeSkipColsInStringSerialisation, includeVirtualColsInStringSerialisation);
	}

	public RecordColumn(String name, Schema schema, boolean optional, Record defaultRecord, boolean includeSkipColsInStringSerialisation, boolean includeVirtualColsInStringSerialisation)
	{
		super(name, schema, optional, defaultRecord, includeSkipColsInStringSerialisation, includeVirtualColsInStringSerialisation);
	}

	@Override
	public Record getNewValueSet()
	{
		return columnSet.createRecord();
	}

	@Override
	public Class<Record> getType()
	{
		return Record.class;
	}

	@Override
	protected RecordColumn createCopy()
	{
		return new RecordColumn(this.name, columnSet, this.optional, this.defaultValue, this.includeSkipColsInStringSerialisation, this.includeVirtualColsInStringSerialisation);
	}

	@Override
	protected Record copy(Record value)
	{
		return new Record(value);
	}

}
