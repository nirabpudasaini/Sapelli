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

package uk.ac.ucl.excites.sapelli.collector.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import uk.ac.ucl.excites.sapelli.collector.model.fields.PhotoField;
import uk.ac.ucl.excites.sapelli.collector.model.fields.PhotoField.FlashMode;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * Camera operator class
 * 
 * Based on:
 * 	- http://stackoverflow.com/a/6421467/1084488
 * 	- http://stackoverflow.com/a/8003222/1084488
 * 	- http://www.vogella.com/code/de.vogella.camera.api/codestartpage.html
 *  - http://android-er.blogspot.co.uk/2012/08/determine-best-camera-preview-size.html
 * 
 * To do's:
 * 	- TODO Fix issue: auto flash mode never seems to flash (on XCover)
 *  - TODO Capture using hardware shutter button
 * 
 * @author mstevens
 */
public class CameraController implements SurfaceHolder.Callback
{

	static private final String TAG = "CameraController";

	static private final int NO_CAMERA_FOUND = -1;
	static private final int ROTATION = 90;
	static private final int VIDEO_CAPTURE_QUALITY = CamcorderProfile.QUALITY_HIGH; // TODO decide which quality
	private Camera camera;
	private int cameraID = NO_CAMERA_FOUND;
	private PhotoField.FlashMode flashMode = PhotoField.DEFAULT_FLASH_MODE;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private MediaRecorder videoRecorder;
	private Surface previewSurface; // keep a reference to this for video
	private FileOutputStream videoFos;
	private boolean recordingHint; // if set to true, reduces time to start video recording

	public CameraController()
	{
		this(false);
	}

	public CameraController(boolean frontFacing)
	{
		this(frontFacing, false);
	}
	
	public CameraController(boolean frontFacing, boolean recordingHint)
	{
		this.cameraID = findCamera(frontFacing);
		this.recordingHint = recordingHint;
	}

	public int findCamera(boolean frontFacing)
	{
		for(int c = 0; c < Camera.getNumberOfCameras(); c++)
		{
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(c, info);
			if(info.facing == (frontFacing ? CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK))
				return c;
		}
		return NO_CAMERA_FOUND;
	}

	public void setFlashMode(FlashMode flashMode)
	{
		this.flashMode = flashMode;
	}

	public boolean foundCamera()
	{
		return(cameraID != NO_CAMERA_FOUND);
	}

	public void startPreview()
	{
		if(!inPreview && cameraConfigured && camera != null)
		{
			camera.startPreview();
			inPreview = true;
		}
	}

	public void stopPreview()
	{
		if(camera != null)
			camera.stopPreview();
		inPreview = false;
	}

	public void takePicture(final PictureCallback callback)
	{
		if(camera != null )
		{
			try {
				//TODO lock camera here?
				
				// Use auto focus if the camera supports it
				String focusMode = camera.getParameters().getFocusMode();
				if(focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO))
				{
					camera.autoFocus(new AutoFocusCallback()
					{
						@Override
						public void onAutoFocus(boolean success, Camera camera)
						{
							camera.takePicture(null, null, callback);
						}
					});
				}
				else
					camera.takePicture(null, null, callback);
			} catch (RuntimeException e) {
				Log.e(TAG,"Camera in use by another process.", e);
			}
		}
	}
	
	public void startVideoCapture(File outputFile)
	{
		try {
	        if (camera != null) {
		        // set up a FileOutputStream for the output file:
		        videoFos = new FileOutputStream(outputFile);

		        // unlock the camera for use by MediaRecorder:
		        camera.unlock();

		        // configure MediaRecorder (MUST call in this order):
		        if (videoRecorder == null) {
			        videoRecorder = new MediaRecorder();
			        videoRecorder.setCamera(camera);
			        videoRecorder
			                .setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			        videoRecorder
			                .setVideoSource(MediaRecorder.VideoSource.CAMERA);
			        videoRecorder.setProfile(CamcorderProfile.get(cameraID,
			                VIDEO_CAPTURE_QUALITY));
			        // TODO encorce mp4?
			        videoRecorder.setOrientationHint(ROTATION);
		        }
		        videoRecorder.setOutputFile(videoFos.getFD());
		        videoRecorder.setPreviewDisplay(previewSurface);
		        // prepare MediaRecorder:
		        videoRecorder.prepare();
		        // start MediaRecorder (and start recording video):
		        videoRecorder.start();
	        }
        } catch (FileNotFoundException e) {
	        Log.e(TAG,"Could not open stream to output file.",e);
        } catch (IOException e) {
	        Log.e(TAG,"Error when trying to record video.",e);
        }
	}
	
	public void stopVideoCapture() {
		videoRecorder.stop();
		videoRecorder.reset();
		camera.lock();
	}

	public void close()
	{
		if(camera != null)
		{
			stopPreview();
			camera.release();
			camera = null;
			cameraConfigured = false;
		}
		if (videoRecorder != null) {
			videoRecorder.release();
			videoRecorder = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if(foundCamera())
		{
			try
			{
				camera = Camera.open(cameraID);
			}
			catch(Exception e)
			{
				Log.e(TAG, "Could not open camera.", e);
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		if(camera != null && holder.getSurface() != null)
		{
			try
			{
				camera.setPreviewDisplay(holder);
				previewSurface = holder.getSurface();
			}
			catch(Throwable t)
			{
				Log.e(TAG, "Exception in setPreviewDisplay()", t);
				return;
			}
			if(!cameraConfigured)
			{			
				// Preview orientation:
				camera.setDisplayOrientation(ROTATION); // TODO optionally make this change with device orientation?
				Camera.Parameters parameters = camera.getParameters();

				// IMAGE orientation (as opposed to preview):
				parameters.setRotation(ROTATION); // should match preview
				
				// recording hint (not supported below API level 14):
				if (Build.VERSION.SDK_INT >= 14)
					parameters.setRecordingHint(recordingHint);
				
				// Preview size:
				Camera.Size previewSize = getBestPreviewSize(width, height, parameters);
				if(previewSize != null)
					parameters.setPreviewSize(previewSize.width, previewSize.height);

				// Set scene mode:
				List<String> sceneModes = parameters.getSupportedSceneModes();
				if(sceneModes != null && sceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO))
					parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

				// Set white balance:
				List<String> whiteBalanceModes = parameters.getSupportedWhiteBalance();
				if(whiteBalanceModes != null && whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_AUTO))
					parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

				// Set focus mode:
				if(parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

				// Flash mode:
				try
				{
					parameters.setFlashMode(getAppropriateFlashMode(parameters));
				}
				catch(NullPointerException e)
				{
					Log.e(TAG, "Exception in setFlashMode()", e);
				}

				//Resulting file:
				//	Format:
				parameters.setPictureFormat(ImageFormat.JPEG);
				parameters.set("jpeg-quality", 100);
				//	Size:
				Camera.Size pictureSize = getLargestPictureSize(parameters);
				if(pictureSize != null)
					parameters.setPictureSize(pictureSize.width, pictureSize.height);

				camera.setParameters(parameters);
				cameraConfigured = true;
			}
		}
		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		close();
	}

	private String getAppropriateFlashMode(Camera.Parameters parameters)
	{
		List<String> availableModes = parameters.getSupportedFlashModes();
		if(availableModes != null)
		{
			switch(flashMode)
			{
				case ON :	if(availableModes.contains(Camera.Parameters.FLASH_MODE_ON))
								return Camera.Parameters.FLASH_MODE_ON;
							break;
				case AUTO :	if(availableModes.contains(Camera.Parameters.FLASH_MODE_AUTO))
								return Camera.Parameters.FLASH_MODE_AUTO;
							break;
				case OFF :	if(availableModes.contains(Camera.Parameters.FLASH_MODE_OFF))
								return Camera.Parameters.FLASH_MODE_OFF;
							break;
			}
		}
		return parameters.getFlashMode(); //leave as is
	}
	
	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters)
	{
		Camera.Size result = null;
		for(Camera.Size size : parameters.getSupportedPreviewSizes())
		{
			if(size.width <= width && size.height <= height)
			{
				if(result == null)
					result = size;
				else
				{
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;
					if(newArea > resultArea)
						result = size;
				}
			}
		}
		return result;
	}

	private Camera.Size getLargestPictureSize(Camera.Parameters parameters)
	{
		Camera.Size result = null;
		for(Camera.Size size : parameters.getSupportedPictureSizes())
		{
			if(result == null)
				result = size;
			else
			{
				if(size.width * size.height > result.width * result.height)
					result = size;
			}
		}
		return result;
	}

}
