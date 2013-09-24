/**
 * 
 */
package uk.ac.ucl.excites.transmission;

import uk.ac.ucl.excites.transmission.sms.SMSTransmission;

/**
 * @author mstevens
 *
 */
public class IncompleteTransmissionException extends Exception
{

	private static final long serialVersionUID = 1L;

	private Transmission transmission;
	
	public IncompleteTransmissionException(Transmission transmission)
	{
		this(transmission, "Incomplete transmission");
	}
	
	public IncompleteTransmissionException(SMSTransmission transmission)
	{
		this(transmission, "Incomplete transmission, " + (transmission.getTotalNumberOfParts() - transmission.getCurrentNumberOfParts()) + "/" + transmission.getTotalNumberOfParts() + " parts missing");
	}

	/**
	 * @param detailMessage
	 */
	public IncompleteTransmissionException(Transmission transmission, String detailMessage)
	{
		super(detailMessage);
		this.transmission = transmission;
	}

	/**
	 * @return the transmission
	 */
	public Transmission getTransmission()
	{
		return transmission;
	}

}
