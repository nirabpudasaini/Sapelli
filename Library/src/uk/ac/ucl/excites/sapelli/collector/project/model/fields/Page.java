/**
 * 
 */
package uk.ac.ucl.excites.sapelli.collector.project.model.fields;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ucl.excites.sapelli.collector.control.Controller;
import uk.ac.ucl.excites.sapelli.collector.project.model.Field;
import uk.ac.ucl.excites.sapelli.collector.project.model.Form;
import uk.ac.ucl.excites.sapelli.collector.project.model.Trigger;
import uk.ac.ucl.excites.sapelli.collector.project.ui.CollectorUI;
import uk.ac.ucl.excites.sapelli.collector.project.ui.FieldUI;
import uk.ac.ucl.excites.sapelli.storage.model.Column;
import uk.ac.ucl.excites.sapelli.util.CollectionUtils;

/**
 * A Page of a {@link Form}.
 * 
 * @author mstevens
 *
 */
public class Page extends Field
{
	
	private final List<Field> fields;
	private final List<Trigger> triggers;

	public Page(Form form, String id)
	{
		super(form, id);
		fields = new ArrayList<Field>();
		triggers = new ArrayList<Trigger>();
	}
	
	public void addField(Field field)
	{
		fields.add(field);
	}

	public List<Field> getFields()
	{
		return fields;
	}
	
	public void addTrigger(Trigger trigger)
	{
		triggers.add(trigger);
	}

	/**
	 * @return the triggers
	 */
	public List<Trigger> getTriggers()
	{
		return triggers;
	}

	@Override
	public boolean isNoColumn()
	{
		for(Field field : fields)
			if(!field.isNoColumn())
				return false;
		return true;
	}

	@Override
	protected List<Column<?>> createColumns()
	{
		List<Column<?>> columns = new ArrayList<Column<?>>(); 
		for(Field f : fields)
			CollectionUtils.addAllIgnoreNull(columns, f.getColumns());
		return columns;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ucl.excites.collector.project.model.Field#createColumn()
	 */
	@Override
	protected Column<?> createColumn()
	{
		throw new UnsupportedOperationException("Page fields require multiple columns, call createColumns() instead.");
	}

	@Override
	public boolean enter(Controller controller)
	{
		return controller.enterPage(this);
	}

	@Override
	public FieldUI createUI(CollectorUI collectorUI)
	{
		return collectorUI.createPageUI(this);
	}
	
}
