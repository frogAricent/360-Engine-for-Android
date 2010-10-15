package com.vodafone360.people.datatypes;

/**
 * Music Download Descriptor POJO class.
 *
 */
public class MusicDDObject {
	private String name = "";
	private String installNotifyURI = "";
	private String suppressUserConfirmation = "";
	private String iconURI = "";
	private long size = 0;
	private String fileType = "";
	private String trackID= "";
	private String downloadURL = "";
	
	@Override
	public String toString() {
		return name + ", " + size + ", " + fileType + ", " + trackID + ", " + downloadURL;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the installNotifyURI
	 */
	public String getInstallNotifyURI() {
		return installNotifyURI;
	}
	/**
	 * @param installNotifyURI the installNotifyURI to set
	 */
	public void setInstallNotifyURI(String installNotifyURI) {
		this.installNotifyURI = installNotifyURI;
	}
	/**
	 * @return the suppressUserConfirmation
	 */
	public String getSuppressUserConfirmation() {
		return suppressUserConfirmation;
	}
	/**
	 * @param suppressUserConfirmation the suppressUserConfirmation to set
	 */
	public void setSuppressUserConfirmation(String suppressUserConfirmation) {
		this.suppressUserConfirmation = suppressUserConfirmation;
	}
	/**
	 * @return the iconURI
	 */
	public String getIconURI() {
		return iconURI;
	}
	/**
	 * @param iconURI the iconURI to set
	 */
	public void setIconURI(String iconURI) {
		this.iconURI = iconURI;
	}
	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}
	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}
	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	/**
	 * @return the trackID
	 */
	public String getTrackID() {
		return trackID;
	}
	/**
	 * @param trackID the trackID to set
	 */
	public void setTrackID(String trackID) {
		this.trackID = trackID;
	}
	/**
	 * @return the downloadURL
	 */
	public String getDownloadURL() {
		return downloadURL;
	}
	/**
	 * @param downloadURL the downloadURL to set
	 */
	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}
	

}
