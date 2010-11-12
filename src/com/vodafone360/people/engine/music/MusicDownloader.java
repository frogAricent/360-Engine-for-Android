package com.vodafone360.people.engine.music;
/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at
 * src/com/vodafone360/people/VODAFONE.LICENSE.txt or
 * http://github.com/360/360-Engine-for-Android
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at src/com/vodafone360/people/VODAFONE.LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2010 Vodafone Sales & Services Ltd.  All rights reserved.
 * Use is subject to license terms.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Observable;

import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;

import com.vodafone360.people.Settings;
import com.vodafone360.people.datatypes.MusicDDObject;

/**
 * This class downloads a file from a URL.
 *
 */
public class MusicDownloader extends Observable {
    
    /**
     * Max size of download buffer.
     */
    private static final int MAX_BUFFER_SIZE = 1024 * 1000;
    
    /**
     * These are the status names.
     */
    public static final String STATUSES[] = {"Downloading", "Paused", "Complete", "Cancelled", "Error"};
    
    /**
     * These are the status codes.
     */
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;

    private String id;
    private URL url; // download URL
    private int size; // size of download in bytes
    private int downloaded; // number of bytes downloaded
    private int status; // current status of download
    private String fileName; 
    private String fileExtn; 
    private String filePath; 
    private String installNotifyURI;

    public String getInstallNotifyURI() {
		return installNotifyURI;
	}

	public void setInstallNotifyURI(String installNotifyURI) {
		this.installNotifyURI = installNotifyURI;
	}

	private int retryCount = 2;
    
    public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getFileExtn() {
		return fileExtn;
	}

	public void setFileExtn(String fileExtn) {
		this.fileExtn = fileExtn;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
     * Constructor for Download.
     * @param url
     */
    public MusicDownloader(MusicDDObject musicDDObject) {
    	    	
    	this.id = musicDDObject.getTrackID();
		
		try {
			this.url = new URL(musicDDObject.getDownloadURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String filePath = "C:\\MusicDownload\\";
		this.filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		this.fileName = musicDDObject.getName();
		this.fileExtn = musicDDObject.getFileType().substring(musicDDObject.getFileType().lastIndexOf("/"));
		if(fileExtn.contains("3gp"))
			fileExtn = "3gp";
		setInstallNotifyURI(musicDDObject.getInstallNotifyURI());
        
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        
    }
    
        
    /**
     * Get this download's URL.
     * @return
     */
    public String getUrl() {
        return url.toString();
    }
    
    /**
     * Get this download's size.
     * @return
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Get this download's progress.
     * @return
     */
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }
    
    /**
     * Get this download's status.
     * @return
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Pause this download.
     */
    public void pause() {
        status = PAUSED;
        stateChanged();
    }
    
    /**
     * Resume this download.
     */
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
    }
    
    /**
     * Cancel this download.
     */
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }
    
    /**
     * Mark this download as having an error.
     */
    private void error() {
    	if(getRetryCount()<0){
    		status = ERROR;
            stateChanged();
    	}else{
    		retryCount = retryCount - 1;
    		setRetryCount(retryCount);
    		downloadFile();
    	}
        
    }
    
    /**
     * download is complete go notify
     */
    private void complete(){
    	installNotify();
    }
   /* *//**
     * Start or resume downloading.
     *//*
    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }*/
    
   /* *//**
     * Get file name portion of URL.
     * @param url
     * @return
     *//*
    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }*/
    
    /**
     * Download file.
     */
    public void downloadFile() {
        RandomAccessFile file = null;
        InputStream stream = null;
        try {
            // Open connection to URL.
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            
            // Specify what portion of file to download.
            connection.setRequestProperty("Range",
                    "bytes=" + downloaded + "-");
            
            // Connect to server.
            connection.connect();
            
            // Make sure response code is in the 200 range.
            if (connection.getResponseCode() / 100 != 2) {
                error();
                return;
            }
            
            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
                return;
            }
            
            /**
             * Set the size for this download if it
             * hasn't been already set. 
             */
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }
            
            /**
             * Open file and seek to the end of it.
             */
            String str = filePath+"/"+fileName+"."+fileExtn;
//            String str = "/mnt/sdcard/music.3gp";
            
            file = new RandomAccessFile(str, "rw");
            file.seek(downloaded);
            stream = connection.getInputStream();

            while (status == DOWNLOADING){
            	/**
            	 * Size buffer according to how much of the
            	 * file is left to download. 
            	 */
            	byte buffer[];
            	if (size - downloaded > MAX_BUFFER_SIZE) {
            		buffer = new byte[MAX_BUFFER_SIZE];
            	} else {
            		buffer = new byte[size - downloaded];
            	}

            	/**
            	 * Read from server into buffer.
            	 */
            	int read = stream.read(buffer);
            	if (read == -1)
            		break;

            	/**
            	 * Write buffer to file.
            	 */
            	file.write(buffer, 0, read);
            	downloaded += read;
            	stateChanged();
            }
            
            /**
             * Change status to complete if this point was reached because downloading has finished. 
             * 
             **/
            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
                complete();
            }
        } catch (Exception e) {
        	e.printStackTrace();
            error();
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {}
            }
            
            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {}
            }
        }
    }
    
    /**
     * Notify observers that this download's status has changed.
     */
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
    
	/**
     * Download file.
     */
	public void installNotify() {

		// Create a new HttpClient and Post Header  
	    
	    
	    try {  
	        // Add your data  
            
            URL url = new URL(getInstallNotifyURI());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setDoOutput(true);
            String encodedData = URLEncoder.encode("900 Success");
            OutputStream os = conn.getOutputStream();
            os.write(encodedData.getBytes());

            int responseCode = conn.getResponseCode();
          
	    } catch (ClientProtocolException e) {  
	        // TODO Auto-generated catch block  
	    } catch (IOException e) {  
	        // TODO Auto-generated catch block  
	    }  
	    
	}
} 