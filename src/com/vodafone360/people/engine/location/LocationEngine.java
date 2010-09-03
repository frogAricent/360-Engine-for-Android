/*
* Copyright (c) 2010 Aricent Technologies (Holdings) Ltd.
* All rights reserved.
*
* This software is the confidential and proprietary information of 
* Aricent Technologies ("Confidential Information").  You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered 
* into with Aricent.
*/
package com.vodafone360.people.engine.location;

import java.util.List;

import android.os.Bundle;

import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.LocationNudgeResult;
import com.vodafone360.people.datatypes.LongGeocodeAddress;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.Location;
import com.vodafone360.people.utils.LogUtils;

/***
 * Engine is responsible for handling features like sending locationnudge and getting owns location
 * <p>
 * File Name : LocationEngine.java
 * <p> 
 * Description : This class extends BaseEngine and contain various callback and overridden methods.
 * <p>
 * Revision History
 * <p>
 * ------------------------------------------------------------------------
 * <p>
 * Date		Author		 SPR-Id		 Version		 Comments
 * <p>
 * - 	       		   		- 		  	0.01 	 		 Initial Release
 * <p>
 */
public class LocationEngine extends BaseEngine { 
	
	/**
     * List of states for LoginEngine.
     */
    private enum State {
        NOT_INITIALISED,
        IDLE,
        SEND_LOCATION_NUDGE,
        GET_GEOCODE_ADDRESS
    }
    
    /**
     * Current state of the engine
     */
    private State mState = State.NOT_INITIALISED;
    
    /**
     * mutex for thread synchronization
     */
    private Object mMutex = new Object();

    /**
     * Add request to fetch the current user's identities.
     * 
     * @param filter Bundle containing parameters for fetch identities request.
     *            This contains the set of filters applied to GetMyIdentities
     *            API call.
     * @return null
     */
    public void addUiGetMyLocation(Bundle b) {
        LogUtils.logD("LocationEngine.addUiGetMyLocation()");
        addUiRequestToQueue(ServiceUiRequest.GET_GEOCODE_ADDRESS,b);
    }
    
    /**
     * Add request to send locationNudge on me profile
     * 
     * @param filter Bundle containing parameters to send location details like longitude, lattitude
     *            This contains the set of filters applied to sendLocationnudge
     *            API call.
     * @return null
     */
    public void  addUiSendLocationNudge(Bundle b) {
        LogUtils.logD("LocationEngine.addUiSendLocationNudge()");
        addUiRequestToQueue(ServiceUiRequest.LOACTION_NUDGE,b);
    }
    
    /**
     * Constructor
     * 
     * @param eventCallback IEngineEventCallback allowing engine to report back.
     */
	public LocationEngine(IEngineEventCallback eventCallback) {
		super(eventCallback);
        mEngineId = EngineId.LOCATION_ENGINE;
	}

	@Override
	public long getNextRunTime() {
		  if (isCommsResponseOutstanding()) {
	            return 0;
	        }
	        if (isUiRequestOutstanding()) {
	            return 0;
	        }
	        return getCurrentTimeout();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onRequestComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onTimeoutEvent() {
		// TODO Auto-generated method stub
		
	}

	/**
	   * Called when a server response is received, processes the response based
	   * on the engine state.
	   * 
	   * @param resp Response data from server
	   */
	@Override
	protected void processCommsResponse(final DecodedResponse resp) {
		LogUtils.logD("LocationEngine.processCommsResponse");
		
		  switch (mState) {
	          case NOT_INITIALISED:
	              break;
	                 
	          case GET_GEOCODE_ADDRESS:
	              handleGetGeocodeAddressResult(resp.mDataTypes);
	              break;
	              
	          case SEND_LOCATION_NUDGE:
	              handleSendLocationNudgeResult(resp.mDataTypes);
	              break;
	              
	          default:
	              break;
      }
	}
	
	/**
	 * Handle Server response to get user's current location. 
	 * The request is completed with ServiceStatus.SUCCESS or ERROR_UNEXPECTED_RESPONSE
	 * 
	 * @param data List of BaseDataTypes generated from Server response.
	 */
	  private void handleGetGeocodeAddressResult(List<BaseDataType> data) {
	        Bundle bu = null;
	        ServiceStatus errorStatus = getResponseStatus(BaseDataType.LONG_GEOCODE_ADDRESS_DATATYPE, data);

	        if (errorStatus == ServiceStatus.SUCCESS) {
	        	 LongGeocodeAddress l1 = (LongGeocodeAddress)(data.get(0));
	        	 LogUtils.logD("LocationEngine handleGetGeocodeAddressResult: " + l1.CITY_NAME);
	        }else
		        completeUiRequest(errorStatus, bu);
		        newState(State.IDLE);
	    }
	  
	  /**
		 * Handle Server response to send locationnudge on ME profile 
		 * The request is completed with ServiceStatus.SUCCESS or ERROR_UNEXPECTED_RESPONSE
		 * 
		 * @param data List of BaseDataTypes generated from Server response.
		 */
	  private void handleSendLocationNudgeResult(List<BaseDataType> data) {
		    LogUtils.logD("LocationEngine.handleSendLocationNudgeResult");
	        Bundle bu = null;
	        ServiceStatus errorStatus = getResponseStatus(BaseDataType.LOCATION_NUDGE_RESULT_DATATYPE, data);

	        if (errorStatus == ServiceStatus.SUCCESS) {
	        	LocationNudgeResult l1 = (LocationNudgeResult)data.get(0);
	        	LogUtils.logD("LocationEngine.handleGetGeocodeAddressResult(): "+l1.success.toString());
	        }
	        else
	        	LogUtils.logE("LocationEngine.handleGetGeocodeAddressResult() Error ");
	        	completeUiRequest(errorStatus, bu);
	        	newState(State.IDLE);
	    	}

	  
	 /**
     * Issue any outstanding UI request.
     * 
     * @param requestType Request to be issued.
     * @param dara Data associated with the request.
     */
	@Override
	protected void processUiRequest(ServiceUiRequest requestType, Object data) {
        LogUtils.logD("LocationEngine.processUiRequest()");
        switch (requestType) {     
            case GET_GEOCODE_ADDRESS:
                startFetchLocation(data);
                break;
            case LOACTION_NUDGE:
                startSendLocationNudge(data);
                break;
            default:
                completeUiRequest(ServiceStatus.ERROR_NOT_FOUND, null);
                break;
        }
	}
	
	/**
	 * Issue request to fetch locaiton. (Request is not issued if
	 * there is currently no connectivity).
	 * 
	 * @param data Object containing the location details.
	 */	
    private void startFetchLocation(Object data) {
        if (!checkConnectivity()) {
            return;
        }
        newState(State.GET_GEOCODE_ADDRESS);
        if (!setReqId(Location.getGeoCodeAddress(this,data))) {
            completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
        }
    }
    
    /**
     * Issue request to send location nudge. (Request is not issued if
     * there is currently no connectivity).
     * 
     * @param data Object containing the location details.
     */
    private void  startSendLocationNudge(Object data){
        if (!checkConnectivity()) {
            return;
        }
        newState(State.SEND_LOCATION_NUDGE);
        if (!setReqId(Location.sendLocationNudge(this,data))) {
            completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
        }
    }
    
    /**
     * Change current IdentityEngine state.
     * 
     * @param newState new state.
     */
    private void newState(State newState) {
        State oldState = mState;
        synchronized (mMutex) {
            if (newState == mState) {
                return;
            }
            mState = newState;
        }
        LogUtils.logV("LocationEngine.newState: " + oldState + " -> " + mState);
    }
    
    /**
     * Get Connectivity status from NetworkAgent.
     * 
     * @return true if NetworkAgent reports we have connectivity, false
     *         otherwise (complete outstanding request with ERROR_COMMS).
     */
    private boolean checkConnectivity() {
        if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
            completeUiRequest(ServiceStatus.ERROR_COMMS, null);
            return false;
        }
        return true;
    }

    /**
     * Run function called via EngineManager. Should have a UI, Comms response
     * or timeout event to handle.
     */
	@Override
	public void run() {
		if (isCommsResponseOutstanding() && processCommsInQueue()) {
            return;
        }
		 if (isUiRequestOutstanding()) {
	            processUiQueue();
	        }
	}
}
