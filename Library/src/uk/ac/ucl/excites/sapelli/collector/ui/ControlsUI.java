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

package uk.ac.ucl.excites.sapelli.collector.ui;

import java.util.Arrays;

import uk.ac.ucl.excites.sapelli.collector.control.Controller;
import uk.ac.ucl.excites.sapelli.collector.model.Form;
import uk.ac.ucl.excites.sapelli.collector.ui.fields.FieldUI;

/**
 * Abstract class to represent the controls UI (i.e. back/cancel/fwd buttons, maybe others later)
 * 
 * @author mstevens
 *
 * @param <V>
 * @param <UI>
 */
public abstract class ControlsUI<V, UI extends CollectorUI<V, UI>>
{
	
	// Statics-------------------------------------------------------
	static public enum Control
	{
		BACK,
		//UP,
		CANCEL,
		FORWARD
	}
	
	static public enum State
	{
		HIDDEN,
		SHOWN_DISABLED, // "grayed out"
		SHOWN_ENABLED
	}
	
	// Dynamics------------------------------------------------------
	protected Controller controller;
	protected UI collectorUI;
	protected boolean enabled;
	
	private Form currentForm;
	private State[] controlStates;
	
	public ControlsUI(Controller controller, UI collectorUI)
	{
		this.controller = controller;
		this.collectorUI = collectorUI;
		this.enabled = true;
	}
	
	/**
	 * @return a platform-specific UI element (e.g. an Android View instance).
	 */
	protected abstract V getPlatformView();
	
	public void update(FieldUI<?, V, UI> fieldUI)
	{
		// Form change?
		if(fieldUI.getField().getForm() != currentForm)
		{
			currentForm = fieldUI.getField().getForm();
			updateForm(currentForm);
		}
		
		// What do we need to show?
		State[] newControlStates = new State[Control.values().length];
		for(Control control : ControlsUI.Control.values())
			newControlStates[control.ordinal()] = fieldUI.getControlState(control); // takes into account the current FormMode
		
		// Is this different from the currently shown controls?
		if(!Arrays.equals(newControlStates, controlStates))
		{
			controlStates = newControlStates;
			updateControlStates(controlStates);
		}
	}
	
	protected abstract void updateForm(Form newForm);
	
	public void setControlStates(State[] newControlStates) {
		if(!Arrays.equals(newControlStates, controlStates))
		{
			controlStates = newControlStates;
			updateControlStates(controlStates);
		}
	}
	
	protected abstract void updateControlStates(State[] newControlStates);
	
	public void disable()
	{
		enabled = false;
	}
	
	public void enable()
	{
		enabled = true;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public boolean isControlEnabled(Control control)
	{
		return enabled && controlStates[control.ordinal()] == State.SHOWN_ENABLED;
	}
	
	/**
	 * @param control
	 * @param hardwareKeyPress
	 */
	public void handleControlEvent(Control control, boolean hardwareKeyPress)
	{
		if(!isControlEnabled(control))
			return;
		
		// Log interaction:
		controller.addLogLine((hardwareKeyPress ? "KEY" : "CLICK") + "_CONTROL_" + control.name(), controller.getCurrentField().getID());
		
		// pass the control event to the current field UI in case it wants to do something unusual:
		if (!collectorUI.getCurrentFieldUI().handleControlEvent(control)) {
		// if the field UI didn't do anything with the control event, then handle in the default way:
			switch(control)
			{
				case BACK :				
					controller.goBack(true);
					break;
				case CANCEL : 
					controller.cancelAndRestartForm();
					break;
				case FORWARD :
					controller.goForward(true);
					break;
				default : return;
			}
		}
	}
	
	public abstract int getCurrentHeightPx();
	
}
