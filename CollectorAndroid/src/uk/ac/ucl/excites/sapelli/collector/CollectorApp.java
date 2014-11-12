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

package uk.ac.ucl.excites.sapelli.collector;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ucl.excites.sapelli.collector.db.CollectorPreferences;
import uk.ac.ucl.excites.sapelli.collector.db.PrefProjectStore;
import uk.ac.ucl.excites.sapelli.collector.db.ProjectRecordStore;
import uk.ac.ucl.excites.sapelli.collector.db.ProjectStore;
import uk.ac.ucl.excites.sapelli.collector.io.AndroidFileStorageProvider;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageException;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageProvider;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageRemovedException;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageUnavailableException;
import uk.ac.ucl.excites.sapelli.collector.util.CrashReporter;
import uk.ac.ucl.excites.sapelli.collector.util.ProjectRunHelpers;
import uk.ac.ucl.excites.sapelli.shared.db.Store;
import uk.ac.ucl.excites.sapelli.shared.db.StoreClient;
import uk.ac.ucl.excites.sapelli.shared.db.exceptions.DBException;
import uk.ac.ucl.excites.sapelli.shared.io.FileHelpers;
import uk.ac.ucl.excites.sapelli.shared.util.TimeUtils;
import uk.ac.ucl.excites.sapelli.storage.db.RecordStore;
import uk.ac.ucl.excites.sapelli.storage.db.sql.sqlite.android.AndroidSQLiteRecordStore;
import uk.ac.ucl.excites.sapelli.util.Debug;
import uk.ac.ucl.excites.sapelli.util.DeviceControl;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;

import com.crashlytics.android.Crashlytics;

/**
 * Application App to keep the db4o object throughout the life-cycle of the Collector
 * 
 * @author Michalis Vitos, mstevens
 * 
 */
public class CollectorApp extends Application implements StoreClient
{

	// STATICS------------------------------------------------------------
	static protected final String TAG = "CollectorApp";
	
	static private final String DATABASE_BASENAME = "Sapelli";
	static private final String DEMO_PREFIX = "Demo_";
	
	static private final boolean USE_PREFS_FOR_PROJECT_STORAGE = false;
	
	static private final String CRASHLYTICS_VERSION_INFO = "VERSION_INFO";
	static private final String CRASHLYTICS_BUILD_INFO = "BUILD_INFO";
	static public final String CRASHLYTICS_DEVICE_ID_CRC32 = "SAPELLI_DEVICE_ID_CRC32";
	static public final String CRASHLYTICS_DEVICE_ID_MD5 = "SAPELLI_DEVICE_ID_MD5";
	static public final String PROPERTY_LAST_PROJECT = "SAPELLI_LAST_RUNNING_PROJECT"; // used as a System property as well as on Crashlytics
	
	public static enum StorageStatus
	{
		UNKNOWN, STORAGE_OK, STORAGE_UNAVAILABLE, STORAGE_REMOVED
	}
	
	// DYNAMICS-----------------------------------------------------------
	private BuildInfo buildInfo;
	
	private SapelliCollectorClient collectorClient;
	private RecordStore recordStore = null;
	private ProjectStore projectStore = null;
	//private TransmissionStore transmissionStore = null; 
	private Map<Store, Set<StoreClient>> storeClients;

	// Files storage:
	private FileStorageProvider fileStorageProvider;
	private FileStorageException fileStorageException = null;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		// Build info:
		this.buildInfo = BuildInfo.GetInstance(getApplicationContext());
		
		Debug.d("CollectorApp started.\nBuild info:\n" + buildInfo.getAllInfo());

		// Start Crashlytics for bugs reporting
		if(!BuildConfig.DEBUG)
		{
			Crashlytics.start(this);
			Crashlytics.setString(CRASHLYTICS_VERSION_INFO, buildInfo.getVersionInfo());
			Crashlytics.setString(CRASHLYTICS_BUILD_INFO, buildInfo.getBuildInfo());
		}
	
		// Collector client:
		this.collectorClient = new SapelliCollectorClient();
		
		// Store clients:
		storeClients = new HashMap<Store, Set<StoreClient>>();
		
		// Initialise file storage:
		try
		{
			this.fileStorageProvider = initialiseFileStorage(); // throws FileStorageException
		}
		catch(FileStorageException fse)
		{
			this.fileStorageException = fse; // postpone throwing until getFileStorageProvider() is called!
		}
		
		// Set up a CrashReporter (will use dumps folder):
		if(fileStorageProvider != null)
			Thread.setDefaultUncaughtExceptionHandler(new CrashReporter(fileStorageProvider, getResources().getString(R.string.app_name)));

		// Create shortcut to Sapelli Collector on Home Screen:
		// Get collector preferences:
		CollectorPreferences pref = new CollectorPreferences(getApplicationContext());
		if(pref.isFirstInstallation())
		{
			// Create shortcut
			ProjectRunHelpers.createCollectorShortcut(getApplicationContext());
			// Set first installation to false
			pref.setFirstInstallation(false);
		}
	}
	
	/**
	 * @return
	 * @throws FileStorageException
	 */
	private FileStorageProvider initialiseFileStorage() throws FileStorageException
	{
		File sapelliFolder = null;
		
		// Get collector preferences:
		CollectorPreferences pref = new CollectorPreferences(getApplicationContext());
		
		// Try to get Sapelli folder path from preferences:
		try
		{
			sapelliFolder = new File(pref.getSapelliFolderPath());
		}
		catch(NullPointerException npe) {}

		// Did we get the folder path from preferences? ...
		if(sapelliFolder == null)
		{	// No: first installation or reset
			
			// Find appropriate files dir (using application-specific folder, which is removed upon app uninstall!):
			File[] paths = DeviceControl.getExternalFilesDirs(this, null);
			if(paths != null && paths.length != 0)
			{
				// We count backwards because we prefer secondary external storage (which is likely to be on an SD card rather unremovable memory)
				for(int p = paths.length - 1; p >= 0; p--)
					if(paths[p] != null && isMountedReadableWritableDir(paths[p]))
					{
						sapelliFolder = paths[p];
						break;
					}
			}

			// Do we have a path?
			if(sapelliFolder != null)
				// Yes: store it in the preferences:
				pref.setSapelliFolder(sapelliFolder.getAbsolutePath());
			else
				// No :-(
				throw new FileStorageUnavailableException();
		}
		else
		{	// Yes, we got path from preferences, check if it is available ...
			if(!isMountedReadableWritableDir(sapelliFolder))
				// No :-(
				throw new FileStorageRemovedException(sapelliFolder.getAbsolutePath());
		}

		// If we get here this means we have a non-null sapelliFolder object representing an accessible path...
		
		// Try to get the Android Downloads folder
		File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if(!isMountedReadableWritableDir(downloadsFolder))
		{
			// Try to create the dir.
			downloadsFolder.mkdirs();

			// Check again
			if(!isMountedReadableWritableDir(downloadsFolder))
				// No :-(
				throw new FileStorageRemovedException(downloadsFolder.getAbsolutePath());
		}
		
		// Return path provider
		return new AndroidFileStorageProvider(sapelliFolder, downloadsFolder); // Android specific subclass of FileStorageProvider, which generates .nomedia files
	}
	
	/**
	 * Returns a FileStorageProvider when file storage is available or throws an FileStorageUnavailableException or an FileStorageRemovedException if it is not
	 * 
	 * @return a PathProvider object
	 * @throws FileStorageException
	 */
	public FileStorageProvider getFileStorageProvider() throws FileStorageException
	{
		if(fileStorageProvider != null && fileStorageException == null)
			return fileStorageProvider;
		if(fileStorageException != null)
			throw fileStorageException;
		else
			throw new FileStorageUnavailableException(); // this shouldn't happen
	}
	
	/**
	 * Check if a directory is on a mounted storage and writable/readable
	 * 
	 * @param dir
	 * @return
	 */
	private boolean isMountedReadableWritableDir(File dir)
	{
		// Accept both Mounted and Unknown Media. The Unknown Media is used in Android when a path isn't backed by known storage media i.e. the SD Card on
		// Samsung Xcover 2. However, we still check that the dir is not null and that we have read/write access to it.
		return (dir != null)
				&& (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(dir)) || EnvironmentCompat.MEDIA_UNKNOWN.equals(EnvironmentCompat.getStorageState(dir)))
				&& FileHelpers.isReadableWritableDirectory(dir);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Debug.d(newConfig.toString());
	}
	
	public BuildInfo getBuildInfo()
	{
		return buildInfo;
	}

	/**
	 * Returns a prefix to be used on storage identifiers (DB4O filenames, SharedPref's names, etc.) when in demo mode
	 * (if not in demo mode the prefix is empty).
	 * The goal is to separate demo-mode storage from non-demo-mode installations and previous demo installations.
	 * 
	 * @return
	 */
	public String getDemoPrefix()
	{
		return (buildInfo.isDemoBuild() ? DEMO_PREFIX + FileHelpers.makeValidFileName(TimeUtils.getTimestampForFileName(buildInfo.getTimeStamp())) : "");
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		Debug.d("onLowMemory() called!");
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
		// This method is for use in emulated process environments. It will never be called on
		// a production Android device, where processes are removed by simply killing them; no
		// user code (including this callback) is executed when doing so.
		Debug.d("Should never be called!");
	}

	/**
	 * Called by a StoreClient to request a ProjectStore object
	 * 
	 * @param client
	 * @return
	 * @throws Exception
	 */
	public ProjectStore getProjectStore(StoreClient client) throws Exception
	{
		if(projectStore == null)
		{
			if(USE_PREFS_FOR_PROJECT_STORAGE)
				projectStore = new PrefProjectStore(this, getFileStorageProvider(), getDemoPrefix());
			else
				projectStore = new ProjectRecordStore(getRecordStore(client), getFileStorageProvider()); // TODO sort out storeclient mess!
			//projectStore = new DB4OProjectStore(getFilesDir(), getDemoPrefix() /*will be "" if not in demo mode*/ + DATABASE_BASENAME);
			collectorClient.setProjectStore(projectStore); // !!!
			storeClients.put(projectStore, new HashSet<StoreClient>());
		}
		storeClients.get(projectStore).add(client); //add to set of clients currently using the projectStore
		return projectStore;
	}
	
	public SapelliCollectorClient getCollectorClient() throws Exception
	{
		return collectorClient;
	}
	
	/**
	 * @param client
	 * @return
	 */
	public RecordStore getRecordStore(StoreClient client) throws Exception
	{
		if(recordStore == null)
		{
			//recordStore = new DB4ORecordStore(getCollectorClient(client), getFileStorageProvider().getDBFolder(true), getDemoPrefix() /*will be "" if not in demo mode*/ + DATABASE_BASENAME);
			recordStore = new AndroidSQLiteRecordStore(collectorClient, this, getFileStorageProvider().getDBFolder(true), getDemoPrefix() /*will be "" if not in demo mode*/ + DATABASE_BASENAME);
			storeClients.put(recordStore, new HashSet<StoreClient>());
		}
		storeClients.get(recordStore).add(client); //add to set of clients currently using the projectStore
		return recordStore;
	}
	
	/**
	 * Called by a DataAccessClient to signal it will no longer use its DataAccess object 
	 * 
	 * @param store
	 * @param client
	 */
	public void discardStoreUsage(Store store, StoreClient client)
	{
		if(store == null)
			return;
		
		// Remove client for this store:
		Set<StoreClient> clients = storeClients.get(store);
		
		if(clients != null)
			clients.remove(client);
		// Finalise if no longer used by other clients:
		if(clients == null || clients.isEmpty())
		{
			try
			{
				store.finalise();
			}
			catch(DBException ignore) { }
			storeClients.remove(store); // remove empty set

			//Slightly dirty but acceptable:
			if(store == projectStore)
				projectStore = null;
			else if(store == recordStore)
				recordStore = null;
			//else if(store == transmissionStore)
			//	transmissionStore = null;
		}
	}
	
	public void backupStores() throws Exception
	{
		File exportFolder = getFileStorageProvider().getTempFolder(true);
		for(Store store : new Store[] { getProjectStore(this), getRecordStore(this) })
		{
			store.backup(exportFolder);
			discardStoreUsage(store, this);
		}
	}
	
}
