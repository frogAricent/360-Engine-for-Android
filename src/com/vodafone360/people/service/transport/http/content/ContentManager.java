
package com.vodafone360.people.service.transport.http.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;

import android.util.Log;

import com.vodafone360.people.Settings;
import com.vodafone360.people.SettingsManager;
import com.vodafone360.people.engine.content.ContentEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.service.transport.DecoderThread;
import com.vodafone360.people.service.transport.IConnection;
import com.vodafone360.people.service.transport.http.HttpConnectionThread;
import com.vodafone360.people.service.utils.hessian.HessianUtils;
import com.vodafone360.people.utils.LogUtils;

public class ContentManager extends Thread implements IConnection {
    private URI mApiUrl;

    private HttpConnectionThread mHttpConnection;

    private DecoderThread mDecoder;

    private boolean mIsConnectionRunning;
    
    private ContentEngine mContentEngine;

    public ContentManager(ContentEngine contentEngine) {
     	mContentEngine = contentEngine;
     	if (mDecoder == null) {
             mDecoder = new DecoderThread();
         }
         if (!mDecoder.getIsRunning()) {
             mDecoder.startThread();
         }
    	mHttpConnection = new HttpConnectionThread(mDecoder);
        mHttpConnection.setHttpClient();

        try {
            mApiUrl = (new URL(SettingsManager.getProperty(Settings.SERVER_URL_HESSIAN_KEY)))
                    .toURI();
        } catch (MalformedURLException e) {
            LogUtils.logE("Error defining URL");
        } catch (URISyntaxException e) {
            LogUtils.logE("Error defining URI");
        }
    }

    public void run() {
    	LogUtils.logD("ContentManager run()");
        while (mIsConnectionRunning) {
            if (mContentEngine.isUploading() || mContentEngine.isUploadingAndPublishing()
            		|| mContentEngine.isDownloadingContent()) {
	            handleUploadContentRequest();
	            synchronized (this) {
		            try {
			            wait();
		            } catch (InterruptedException e) {
		            }
	            }
            }
            else{
	            synchronized (this) {
		            try {
			            wait();
		            } catch (InterruptedException e) {
		            }
	            }
            }
        }
    }

     public void handleUploadContentRequest() {
     	mIsConnectionRunning = false;
        List<Request> requests = QueueManager.getInstance().getApiRequests();
        if (null == requests) {
        	LogUtils.logE("ContentManager handleUploadContentRequest(): No request to handle");
            return;
        }
        HttpConnectionThread.logI("ContentManager.handleUploadContentRequest()",
                "Looking for content upload requests");

        for (int i = 0; i < requests.size(); i++) {
            Request request = requests.get(i);
            request.setActive(true);

            List<Integer> reqIds = new ArrayList<Integer>();
            reqIds.add(request.getRequestId());

            try {
                HttpConnectionThread.logI("ContentManager.handleUploadContentRequest()", "Request: "
                        + request.getRequestId());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                request.writeToOutputStream(baos, false);
                byte[] payload = baos.toByteArray();

                if (Settings.ENABLED_TRANSPORT_TRACE) {
                    HttpConnectionThread.logI("ContentManager.handleUploadContentRequest()",
                            "\n \n \nUPLOADING: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                                    + HessianUtils.getInHessian(new ByteArrayInputStream(payload),
                                            true));
                }

                HttpResponse resp = mHttpConnection.postHTTPRequest(payload, mApiUrl,
                        Settings.HTTP_HEADER_CONTENT_TYPE);
                mHttpConnection.handleApiResponse(resp, reqIds);
            } catch (Exception e) {
                mHttpConnection.addErrorToResponseQueue(reqIds);
            }
        }
        mIsConnectionRunning = true;
    }
     
    @Override
    public boolean getIsConnected() {
        return true;
    }

    @Override
    public boolean getIsRpgConnectionActive() {
        return true;
    }

    @Override
    public void notifyOfRegainedNetworkCoverage() {
    }

    @Override
    public void notifyOfUiActivity() {
    }

    @Override
    public void onLoginStateChanged(boolean isLoggedIn) {
    }

    @Override
    public void startThread() {
        mIsConnectionRunning = true;
        start();
    }

    @Override
    public void stopThread() {
        mIsConnectionRunning = false;
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void notifyOfItemInRequestQueue() {
    	LogUtils.logD("ContentManager notifyOfItemInRequestQueue()");
        synchronized (this) {
            notify();
        }
    }
}
