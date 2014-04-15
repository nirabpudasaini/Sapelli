/**
 * 
 */
package uk.ac.ucl.excites.sapelli.collector.model.fields;

import uk.ac.ucl.excites.sapelli.collector.control.Controller;
import uk.ac.ucl.excites.sapelli.collector.model.FieldParameters;
import uk.ac.ucl.excites.sapelli.collector.model.Form;
import uk.ac.ucl.excites.sapelli.storage.model.Column;
import uk.ac.ucl.excites.sapelli.storage.model.columns.ForeignKeyColumn;

/**
 * @author mstevens
 *
 */
public class BelongsToField extends Relationship
{

	//STATICS -------------------------------------------------------
	static public final String PARAMETER_EDIT = "edit";
	static public final String PARAMETER_WAITING_FOR_RELATED_FORM = "watingForRelatedForm";
	
	// Dynamics------------------------------------------------------
	
	/**
	 * @param form
	 * @param id
	 */
	public BelongsToField(Form form, String id)
	{
		super(form, id);
		this.noColumn = false;
	}
	
	public void setRelatedForm(Form relatedForm)
	{
		if(!relatedForm.isProducesRecords())
			throw new IllegalArgumentException("The related form of a BelongsTo field must produce records!");
		super.setRelatedForm(relatedForm);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ucl.excites.collector.project.model.Field#createColumn()
	 */
	@Override
	protected Column<?> createColumn()
	{	
		return new ForeignKeyColumn(id, relatedForm.getSchema(), (optional != Optionalness.NEVER)); // (BelongsTo)
	}
	
	@Override
	public ForeignKeyColumn getColumn()
	{
		return (ForeignKeyColumn) super.getColumn();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ucl.excites.sapelli.collector.model.Field#enter(uk.ac.ucl.excites.sapelli.collector.control.Controller, uk.ac.ucl.excites.sapelli.collector.model.FieldParameters, boolean)
	 */
	@Override
	public boolean enter(Controller controller, FieldParameters arguments, boolean withPage)
	{
		return controller.enterBelongsTo(this, arguments);
	}
	
}
