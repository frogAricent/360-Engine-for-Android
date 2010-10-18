package com.vodafone360.people.tests.engine;
/*
 ****************************************************************
 * Copyright (c) 2010 Aricent Technologies (Holdings) Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Aricent Technologies ("Confidential Information").You 
 * shall not disclose such Confidential Information and shall use 
 * it only in accordance with the terms of the license agreement 
 * you entered into with Aricent.
 ****************************************************************
 */
import java.util.ArrayList;
import java.util.List;

import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.datatypes.ListOfLong;
import com.vodafone360.people.datatypes.ServerError;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.share.ShareEngine;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.tests.TestModule;
import com.vodafone360.people.utils.LogUtils;
/**
 * 
 * File Name : ShareEngineTest.java 
 * Description : This class implements IEngineTestFrameworkObserver interface and contain various callback methods.
 *  
 * Revision History
 * --------------------------------------------------------------
 * Date		 Author		 SPR-Id		 Version		 Comments
 * 01-Sep-10 	-		  - 		  0.01 			 Initial Release
 * 
 * 
 */
public class ShareEngineTest extends InstrumentationTestCase implements
		IEngineTestFrameworkObserver {

	/**
	 * States for ShareEngine. States are based on the requests that the engine
	 * needs to handle.
	 */

	private enum ShareState {
		IDLE,
		SHARING_ALBUM,
		SHARING_ALBUM_FAIL,
		GETTING_ALBUM_SHARED_WITH,
		GETTING_ALBUM_SHARED_WITH_FAIL,
		ALLOWING_GROUP,
		ALLOWING_GROUP_FAIL,
		DENYING_GROUP,
		DENYING_GROUP_FAIL,
		GET_NEXT_RUNTIME
	}

	private static final String LOG_TAG = "ShareEngineTest";
	private EngineTestFramework mEngineTester = null;
	private ShareEngine mEng = null;
	private MainApplication mApplication = null;
	private ShareState mState = ShareState.IDLE;
	TestModule mTestModule = new TestModule();


	/**
	 ******************************************************************* 
	 * Method to initialize all the resources 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mApplication = (MainApplication) Instrumentation.newApplication(
				MainApplication.class, getInstrumentation().getTargetContext());
		mApplication.onCreate();
		mEngineTester = new EngineTestFramework(this);
	    mEng = new ShareEngine(mEngineTester);
	    mEngineTester.setEngine(mEng);
	    mState = ShareState.IDLE;
	}

	/**
	 ******************************************************************* 
	 * Method to release all the resoures  
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@Override
	protected void tearDown() throws Exception {
		// stop our dummy thread?
		mEngineTester.stopEventThread();
		mEngineTester = null;
		mEng = null;
		// call at the end!!!
		super.tearDown();
	}

	/**
	 ******************************************************************* 
	 * Method to test getNextruntime() for Share engine 
	 * 
	 * @param : null
	 * @return : null
	 *******************************************************************
	 */
	@MediumTest
	    public void testGetNextRuntime() {
	        boolean testPass = true;
	        mState = ShareState.GET_NEXT_RUNTIME;
	        long runtime = mEng.getNextRunTime();
	        if (runtime != -1) {
	            testPass = false;
	        }

	        assertTrue("testGetNextRuntime() failed", testPass);
	        LogUtils.logI("testGetNextRuntime (SUCCESS) ****\n");
	    }
	 
	
	/**
	 ******************************************************************* 
	 * Method to test sharing album with groups for success
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Takes to long
	public void testShareAlbum() {
		boolean testPass = true;
		mState = ShareState.SHARING_ALBUM;
		Long groupId = null;
    	EntityKey entityKey = new EntityKey();
    	entityKey.setEntityId(new Long(1124934));
    	entityKey.setUserId(null);
    	entityKey.setEntityType("CONTENT");
    	groupId = new Long(10273123);
		
    	NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiShareAlbum(groupId, entityKey);
		} catch (Exception e) {
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		 LogUtils.logI("**** test Share Album Test (SUCCESS) ****\n");
	}


	/**
	 ******************************************************************* 
	 * Method to test sharing album with groups for success for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Breaks tests.
	public void testShareAlbumFail() {
		mState = ShareState.SHARING_ALBUM_FAIL;
		String grpName = null;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		
		mEng.addUiShareAlbum(null, null);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}


	/**
	 ******************************************************************* 
	 * Method to test for getting the groups with which an album is shared
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Takes to long
	public void testGetAlbumsSharedWith() {
		boolean testPass = true;
		mState = ShareState.GETTING_ALBUM_SHARED_WITH;
		EntityKey entityKey = new EntityKey();
    	entityKey.setEntityId(new Long(1124934));
    	entityKey.setUserId(null);
    	entityKey.setEntityType("album");

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
		
			mEng.addUiSharedWith(entityKey);
		} catch (Exception e) {
			testPass = false;
		}

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		 LogUtils.logI("**** test getting all the groups for shared albums (SUCCESS) ****\n");
	}


	/**
	 ******************************************************************* 
	 * Method to test for getting the groups with which an album is shared for failure case
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Breaks tests.
	public void testGetAlbumsSharedWithFail() {
		mState = ShareState.GETTING_ALBUM_SHARED_WITH_FAIL;
		
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiSharedWith(null);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test for Granting access for a group to album entities
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Takes to long
	public void testAllowGroup() {
		boolean testPass = true;
		mState = ShareState.ALLOWING_GROUP;
		Long grpId = null;
    	EntityKey entityKey = new EntityKey();
    	entityKey.setEntityId(new Long(1124934));
    	entityKey.setUserId(null);
    	entityKey.setEntityType("album");
    	grpId = new Long(10273123);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiAllowGroup(grpId, entityKey);
		} catch (Exception e) {
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		 LogUtils.logI("**** test allow group(SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test for Granting access for a group to album entities
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Breaks tests.
	public void testAllowGroupFail() {
		mState = ShareState.ALLOWING_GROUP_FAIL;
		String grpName = null;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiAllowGroup(null, null);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test for denying access for a group to album entities
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Takes to long
	public void testDenyGroup() {
		boolean testPass = true;
		mState = ShareState.DENYING_GROUP;
		Long groupId = null;
    	EntityKey entityKey = new EntityKey();
    	entityKey.setEntityId(new Long(1096489));
    	entityKey.setUserId(null);
    	entityKey.setEntityType("CONTENT");
    	groupId = new Long(10018390);
		
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiDenyGroup(groupId, entityKey);
		} catch (Exception e) {
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** test Deny group (SUCCESS) ****\n");
	}


	/**
	 ******************************************************************* 
	 * Method to test for denying access for a group to album entities for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	@Suppress // Breaks tests.
	public void testSetPrivacySettingsFail() {
		mState = ShareState.DENYING_GROUP_FAIL;
		
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try
		{
		mEng.addUiDenyGroup(null,null);
		}catch(Exception e)
		{
		 e.printStackTrace();
			
		}
		
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}

	@Override
	public void reportBackToEngine(int reqId, EngineId engine) {

		ServerError err = new ServerError("Catastrophe");
		Log.d("TAG", "ShareEngineTest.reportBackToEngine");
		ResponseQueue respQueue = ResponseQueue.getInstance();
		List<BaseDataType> data = new ArrayList<BaseDataType>();
		
		switch (mState) {
		case IDLE:
			break;
		case ALLOWING_GROUP:    //Fall through
		case SHARING_ALBUM: 	//Fall through
		case DENYING_GROUP:
			LogUtils.logD("TAG ShareEngineTest.reportBackToEngine");
			ListOfLong groupIds = new ListOfLong();
			data.add(groupIds);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.SHARE_ALBUM_RESPONSE.ordinal()));
			LogUtils.logD("TAG GroupsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;
			
		case SHARING_ALBUM_FAIL:
		case ALLOWING_GROUP_FAIL:
		case DENYING_GROUP_FAIL:
			err.errorDescription = "Fail";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();
            break;

		case GETTING_ALBUM_SHARED_WITH:
			LogUtils.logD("TAG ShareEngineTest.reportBackToEngine");
			ListOfLong groupId = new ListOfLong();
			data.add(groupId);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.GET_GROUPS_SHARED_WITH_RESPONSE.ordinal()));
			LogUtils.logD("TAG GroupsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;
			
		case GETTING_ALBUM_SHARED_WITH_FAIL:
			err.errorDescription = "Wrong Input Parameters";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();
			break;
		
		case GET_NEXT_RUNTIME:
             break;
		default:
		}
	}
	 
	@Override
	public void onEngineException(Exception exp) {
		// TODO Auto-generated method stub

	}
}
