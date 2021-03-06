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

package uk.ac.ucl.excites.sapelli.collector.model.fields;

import java.io.File;
import java.util.Set;

import uk.ac.ucl.excites.sapelli.collector.io.FileStorageProvider;
import uk.ac.ucl.excites.sapelli.collector.model.Form;
import uk.ac.ucl.excites.sapelli.collector.ui.CollectorUI;
import uk.ac.ucl.excites.sapelli.collector.ui.fields.MediaUI;
import uk.ac.ucl.excites.sapelli.shared.util.CollectionUtils;
import uk.ac.ucl.excites.sapelli.shared.util.Objects;

/**
 * @author Michalis Vitos, mstevens
 *
 */
public class PhotoField extends MediaField
{

	// STATICS-------------------------------------------------------
	static public final String MEDIA_TYPE_JPEG = "PHOTO_JPEG";
	static public final String EXTENSION_JPEG = "jpg";
	static public final String MIME_TYPE_JPEG = "image/jpeg";
	
	static public enum FlashMode
	{
		AUTO,
		ON,
		OFF
	}
	
	static public final boolean DEFAULT_USE_NATIVE_APP = false;
	static public final boolean DEFAULT_USE_FRONT_FACING_CAMERA = false;
	static public final FlashMode DEFAULT_FLASH_MODE = FlashMode.AUTO;
	
	// DYNAMICS------------------------------------------------------
	private boolean useFrontFacingCamera = DEFAULT_USE_FRONT_FACING_CAMERA;
	private FlashMode flashMode;
	private String captureButtonImageRelativePath;
	
	public PhotoField(Form form, String id, String caption)
	{
		super(form, id, caption, DEFAULT_USE_NATIVE_APP);
		flashMode = DEFAULT_FLASH_MODE;
	}
	
	/**
	 * @return the useFrontFacingCamera
	 */
	public boolean isUseFrontFacingCamera()
	{
		return useFrontFacingCamera;
	}

	/**
	 * @param useFrontFacingCamera the useFrontFacingCamera to set
	 */
	public void setUseFrontFacingCamera(boolean useFrontFacingCamera)
	{
		this.useFrontFacingCamera = useFrontFacingCamera;
	}

	/**
	 * @return the flashMode
	 */
	public FlashMode getFlashMode()
	{
		return flashMode;
	}

	/**
	 * @param flashMode the flashMode to set
	 */
	public void setFlashMode(FlashMode flashMode)
	{
		this.flashMode = flashMode;
	}
	
	/**
	 * @return the captureButtonImageRelativePath
	 */
	public String getCaptureButtonImageRelativePath()
	{
		return captureButtonImageRelativePath;
	}

	/**
	 * @param captureButtonImageRelativePath the captureButtonImageRelativePath to set
	 */
	public void setCaptureButtonImageRelativePath(String captureButtonImageRelativePath)
	{
		this.captureButtonImageRelativePath = captureButtonImageRelativePath;
	}

	@Override
	public String getMediaType()
	{
		return MEDIA_TYPE_JPEG;
	}

	@Override
	protected String getFileExtension(String mediaType)
	{
		return EXTENSION_JPEG;
	}
	
	@Override
	protected String getFileMimeType(String mediaType)
	{
		return MIME_TYPE_JPEG;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ucl.excites.sapelli.collector.model.Field#addFiles(java.util.Set, uk.ac.ucl.excites.sapelli.collector.io.FileStorageProvider)
	 */
	@Override
	public void addFiles(Set<File> filesSet, FileStorageProvider fileStorageProvider)
	{
		super.addFiles(filesSet, fileStorageProvider); // !!!
		
		CollectionUtils.addIgnoreNull(filesSet, fileStorageProvider.getProjectImageFile(form.project, captureButtonImageRelativePath));
	}

	@Override
	public <V, UI extends CollectorUI<V, UI>> MediaUI<PhotoField,V, UI> createUI(UI collectorUI)
	{
		return collectorUI.createPhotoUI(this);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true; // references to same object
		if(obj instanceof PhotoField)
		{
			PhotoField that = (PhotoField) obj;
			return	super.equals(that) && // MediaField#equals(Object)
					this.useFrontFacingCamera == that.useFrontFacingCamera &&
					this.flashMode.ordinal() == that.flashMode.ordinal() &&
					Objects.equals(this.captureButtonImageRelativePath, that.captureButtonImageRelativePath);
		}
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		int hash = super.hashCode(); // MediaField#hashCode()
		hash = 31 * hash + (useFrontFacingCamera ? 0 : 1);
		hash = 31 * hash + flashMode.ordinal();
		hash = 31 * hash + (captureButtonImageRelativePath == null ? 0 : captureButtonImageRelativePath.hashCode());
		return hash;
	}
	
}
