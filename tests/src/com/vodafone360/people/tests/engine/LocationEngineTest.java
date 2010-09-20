
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

package com.vodafone360.people.tests.engine;

import java.util.ArrayList;
import java.util.List;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.LocationNudgeResult;
import com.vodafone360.people.datatypes.LongGeocodeAddress;
import com.vodafone360.people.datatypes.ServerError;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.location.LocationEngine;
import com.vodafone360.people.service.RemoteService;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.utils.LogUtils;
/**
 * 
 * File Name : LocationEngineTest.java 
 * Description : This class implements IEngineTestFrameworkObserver interface and contain various callback methods.
 *  
 * Revision History
 * --------------------------------------------------------------
 * Date		 Author		 SPR-Id		 Version		 Comments
 * 01-Sep-10 	-		 - 			  0.01 			 Initial Release
 * 
 * 
 */
public class LocationEngineTest extends InstrumentationTestCase implements
		IEngineTestFrameworkObserver {

	/**
	 * States for LocationEngine. States are based on the requests that the
	 * engine needs to handle.
	 */

	private enum LocationState {
		NOT_INITIALISED,
		IDLE, 
		SEND_LOCATION_NUDGE, 
		SEND_LOCATION_NUDGE_FAIL, 
		GET_GEOCODE_ADDRESS, 
		GET_GEOCODE_ADDRESS_FAIL,
		GET_NEXT_RUNTIME
	}

	private static final String LOG_TAG = "LocationEngineTest";
	private EngineTestFramework mEngineTester = null;
	private LocationEngine mEng = null;
	private MainApplication mApplication = null;
	private LocationState mState = LocationState.IDLE;


	/**
	 ******************************************************************* 
	 * Method to initialize the resources  
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		getInstrumentation().getTargetContext().startService(new Intent(getInstrumentation().getTargetContext(),RemoteService.class));
		mEngineTester = new EngineTestFramework(this);
		mEng = new LocationEngine(mEngineTester);
		mApplication = (MainApplication) Instrumentation.newApplication(
				MainApplication.class, getInstrumentation().getTargetContext());
		mEngineTester.setEngine(mEng);
		mState = LocationState.IDLE;
	}


	/**
	 ******************************************************************* 
	 * Method to release all the resources   
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@Override
	protected void tearDown() throws Exception {

		mEngineTester.stopEventThread();
		mEngineTester = null;
		mEng = null;
		// call at the end!!!
		super.tearDown();
	}


	/**
	 ******************************************************************* 
	 * Method to test nextRunTime for Location Engine  
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testGetNextRuntime() {
        boolean testPass = true;
        mState = LocationState.GET_NEXT_RUNTIME;
        long runtime = mEng.getNextRunTime();
        if (runtime != -1) {
            testPass = false;
        }
        assertTrue("testGetNextRuntime() failed", testPass);
        LogUtils.logI("**** testGetNextRuntime (SUCCESS) ****\n");
    }
 

	/**
	 ******************************************************************* 
	 * Method to test to send location nudge on ME profile  
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Takes to long
	public void testSendLocationNudge() {
		boolean testPass = true;
		mState = LocationState.SEND_LOCATION_NUDGE;

		Bundle getbundle = new Bundle();
		getbundle.putString("latitude", "12.97622");
		getbundle.putString("longitude", "77.603294");

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try
		{
		mEng.addUiSendLocationNudge(getbundle);
		}catch(Exception e){
			e.printStackTrace();
			testPass = false;			
		}
		
		//mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** testSendLocationNudge (SUCCESS) ****\n");
	}


	/**
	 ******************************************************************* 
	 * Method to test to send location nudge on ME profile for failure  
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Breaks test
	public void testSendLocationNudgeFail() {
		mState = LocationState.SEND_LOCATION_NUDGE_FAIL;
		Bundle getbundle = null;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiSendLocationNudge(getbundle);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}


	/**
	 ******************************************************************* 
	 * Method to test to get current location  
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Takes to long
	public void testGetGeoCodeInput() {
		boolean testPass = true;
		mState = LocationState.GET_GEOCODE_ADDRESS;
		Bundle getbundle = new Bundle();
		getbundle.putString("countrycode", "IN-KA");
		getbundle.putString("cityname", "Bangalore");

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try
		{
		mEng.addUiGetMyLocation(getbundle);
		}catch(Exception e)
		{
			e.printStackTrace();
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("LOGTAG **** testGetGeoCodeLocation (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test to get current location for failure  
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Breaks test
	public void testGetGeoCodeInputFail() {
		mState = LocationState.GET_GEOCODE_ADDRESS_FAIL;
		Bundle getbundle = null;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetMyLocation(getbundle);
	// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	@Override
	public void reportBackToEngine(int reqId, EngineId engine) {

		ServerError err = new ServerError("Catastrophe");
		LogUtils.logD("TAG LocationEngineTest.reportBackToEngine");
		ResponseQueue respQueue = ResponseQueue.getInstance();
		List<BaseDataType> data = new ArrayList<BaseDataType>();

		switch (mState) {
		case IDLE:
			break;
		case GET_GEOCODE_ADDRESS:
			LogUtils.logD("TAG LocationEngineTest.reportBackToEngine Get location nudge");
			LongGeocodeAddress mGeoCode = new LongGeocodeAddress();
			data.add(mGeoCode);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.SEND_LOCATION_NUDGE_RESPONSE.ordinal()));
			mEng.onCommsInMessage();
			break;

		case GET_GEOCODE_ADDRESS_FAIL:
			err.errorDescription = "Wrong Input Parameters";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();
			break;

		case SEND_LOCATION_NUDGE:
			LogUtils.logD("TAG LocationEngineTest.reportBackToEngine Add new Album");
			LocationNudgeResult mNudge = new LocationNudgeResult();
			data.add(mNudge);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.GET_LOCATION_RESPONSE.ordinal()));
			LogUtils.logD("TAG LocationEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case SEND_LOCATION_NUDGE_FAIL:
			err.errorDescription = "Wrong Input Parameters";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();
			break;

		default:
		}

	}

	@Override
	public void onEngineException(Exception exp) {
		// TODO Auto-generated method stub

	}
}