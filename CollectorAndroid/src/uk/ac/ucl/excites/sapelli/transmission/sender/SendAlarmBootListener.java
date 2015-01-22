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

package uk.ac.ucl.excites.sapelli.transmission.sender;

import uk.ac.ucl.excites.sapelli.transmission.sender.util.SendAlarmInitialiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver that listens for device boot events and when one is received, starts the SendAlarmInitialiser service.
 * 
 * Note that this BroadcastReceiver is only registered to listen for boot events if Sapelli determines that there is at least one project
 * with records to send - see {@link SendAlarmManager#checkBootReceiver(Context)}.
 * 
 * @author Michalis Vitos
 *
 */
public class SendAlarmBootListener extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Start AlarmSchduler service to schedule alarms for the required projects
		Intent alarmScheduler = new Intent(context, SendAlarmInitialiser.class);
		context.startService(alarmScheduler);
	}
}