package com.facebook.android.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.vodafone360.people.datatypes.ActivityItem;
import android.util.Log;
/*
 * FBDownloader downloads the pics from facebook
 */
public class FBDownloader {
	
	protected static final String TAG = "FBDownloader";
	private static boolean isCanceled = false;
	private static final int READ_BLOCK_SIZE = 30 * 1024;
	private static final String STORAGE_PATH = "/sdcard/photofeed/";
	
	private String filePath;
	
	/**
     *****************************************************************
     * General descriptions of contructors goes here
     *****************************************************************
     */

    public FBDownloader() {
        //Do nothing
    }
    
    public FBDownloader(String filePath){
        this.filePath = filePath;
    }
    
	public String downloadPhoto(String url){
	    return connect(url);
	}
	/*
	 * Download the pic from URL
	 */
	public String downloadPhoto(final ActivityItem item){
		filePath = STORAGE_PATH;
		return connect(item.previewUrl);
		
	}
	
	public String connect(String url){
	    Log.d(TAG, url);
        boolean isSuccess = false;
        String fileName = null;
        if(isCanceled){
            return fileName;
        }
        HttpClient client = new DefaultHttpClient();
        
        HttpGet method = new HttpGet(url);
        try{
                HttpResponse response = client.execute(method);
                
                fileName = url.substring(url.lastIndexOf('/')+1);
                fileName = STORAGE_PATH + fileName;
                isSuccess = createFile(response.getEntity().getContent(), url, fileName);
               
         }catch (Exception e) {
             Log.d(TAG, e.getMessage());
         }
         finally{
             
//                   if( downloadList.size() == 0){
//                       
//                         break;
//                     }
         }
         return isSuccess ? fileName : null;
	}
	
	
	/*
	 * Store the file in the SDcard
	 */	
	private boolean createFile(InputStream istream, String url, String fileName)
	{
		byte[] buff = null;
		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;
		boolean ok = false;
		try {
			File f = new File(filePath);
			if(!f.exists()){
				f.mkdirs();
			}
			fOut = new FileOutputStream(fileName);

			// Write the stream to the file
			Log.v(TAG, "fileName: " + fileName);

			osw = new OutputStreamWriter(fOut);
			buff = new byte[READ_BLOCK_SIZE];

			while (true) {
				int i = istream.read(buff);
				if (i == -1) {
					break;
				}
				fOut.write(buff, 0, i);
				ok = true;
			}

		} catch (Exception e) {
			ok = false;
			Log.e(TAG, e.getMessage());
			
		} finally {
		try {
			Log.v(TAG, "Success: " + ok);

		// deleting if file has no data or partial file
			if (!ok) {
				File f = null;
				if (fileName.length() != 0) {
					f = new File(fileName);
				}
				if (f != null && f.exists()) {
					f.delete();
				}
				f = null;
			} else {
				Log.d(TAG, "Successfully written the file with name: " + fileName);
			}
			if (osw != null) {
				try {
					osw.close();
					osw = null;
				} catch (Exception e) {
					osw = null;
					Log.e(TAG, "Exception while closing the Output Stream Writer");
				}
			}
			if (fOut != null) {
				try {
					fOut.close();
					fOut = null;
				} catch (Exception e) {
					fOut = null;
					Log.e(TAG, "Exception while closing the File Output Stream");
				}
			}
			if (buff != null) {
				buff = null;
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			
			}
		}
		return ok;
	}
}
