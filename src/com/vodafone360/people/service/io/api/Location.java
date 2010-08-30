package com.vodafone360.people.service.io.api;

import java.util.List;
import java.util.Map;

import android.os.Bundle;

import com.vodafone360.people.Settings;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.login.LoginEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.utils.LogUtils;

public class Location {
	
	 private final static String FUNCTION_GET_LOCATION_GEOCODING = "location/geocoding";
	 
	 private final static String FUNCTION_SEND_LOCATION_NUDGE = "location/locationnudge";
	 
	 /**
	  * Implementation of loaction/geocoding API. Parameters are;
	  * [auth], String countrycode, String cityname
	  * 
	  * @param engine handle to LocationEngine
	  * @param countrycode The country code
	  * @param citycode The city code
	  * @return request id generated for this request
	  */	 
	 public static int getGeoCodeAddress(BaseEngine engine,Object data) {
		 LogUtils.logD("Location.getGeoCodeAddress()");
	        if (LoginEngine.getSession() == null) {
	            LogUtils.logE("Identities.getAvailableLocation() Invalid session, return -1");
	            return -1;
	        }
	        
	        Bundle b=new Bundle();
	        b=(Bundle) data;
	        
	        String Country=b.getString("countrycode");
	        String City=b.getString("cityname");
	        
	        Request request = new Request(FUNCTION_GET_LOCATION_GEOCODING, Request.Type.GET_GEOCODE_ADDRESS,
	                engine.engineId(), false, Settings.API_REQUESTS_TIMEOUT_IDENTITIES);
	       
	        request.addData("countrycode", Country);
	        request.addData("cityname", City);
	        QueueManager queue = QueueManager.getInstance();
	        int requestId = queue.addRequest(request);
	        queue.fireQueueStateChanged();
	        return requestId;
	    }

	 /**
	  * Implementation of loaction/locationnudge API. Parameters are;
	  * [auth], String longitude, String latitude
	  * 
	  * @param engine handle to LocationEngine
	  * @param longitude The longitude of the location
	  * @param latitude The latitude of the location
	  * @return request id generated for this request
	  */	 
	 public static int sendLocationNudge(BaseEngine engine,Object data) {
	 		LogUtils.logD("Location.sendLocationNudge()");
	        if (LoginEngine.getSession() == null) {
	            LogUtils.logE("Identities.getAvailableLocation() Invalid session, return -1");
	            return -1;
	        }
	        
	        Bundle b=new Bundle();
	        b=(Bundle) data;
	        
	        String longitude=b.getString("longitude");
	        String latitude=b.getString("latitude");
	        Request request = new Request(FUNCTION_SEND_LOCATION_NUDGE, Request.Type.SEND_LOCATION_NUDGE,
	                engine.engineId(), false, Settings.API_REQUESTS_TIMEOUT_IDENTITIES);
	        request.addData("longitude", longitude);
	        request.addData("latitude", latitude);
	        QueueManager queue = QueueManager.getInstance();
	        int requestId = queue.addRequest(request);
	        queue.fireQueueStateChanged();
	        return requestId;
	    }

}
