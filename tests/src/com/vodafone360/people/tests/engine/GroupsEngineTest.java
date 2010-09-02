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
import com.vodafone360.people.datatypes.Group;
import com.vodafone360.people.datatypes.ItemList;
import com.vodafone360.people.datatypes.ServerError;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.groups.GroupsEngine;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.tests.TestModule;
import com.vodafone360.people.utils.LogUtils;

/**
 * 
 * File Name : GroupsEngineTest.java 
 * Description : This class implements IEngineTestFrameworkObserver interface and contain various callback methods.
 *  
 * Revision History
 * --------------------------------------------------------------
 * Date		 	Author		 SPR-Id		 Version		 Comments
 * 01-Sep-10 	       		  - 		  0.01 			 Initial Release
 * 
 * 
 */

public class GroupsEngineTest extends InstrumentationTestCase implements
		IEngineTestFrameworkObserver {

	/**
	 * States for GroupsEngine. States are based on the requests that the engine
	 * needs to handle.
	 */
	private enum GroupsState {
		IDLE, ADDING_USER_GROUP, ADDING_USER_GROUP_FAIL, DELETING_USER_GROUP, DELETING_USER_GROUP_FAIL, GETTING_GROUP_PRIVACY_SETTING, GETTING_GROUP_PRIVACY_SETTING_FAIL, SETTING_GROUP_PRIVACY_SETTING, SETTING_GROUP_PRIVACY_SETTING_FAIL, GET_NEXT_RUNTIME
	}

	private static final String LOG_TAG = "GroupsEngineTest";
	private EngineTestFramework mEngineTester = null;
	private GroupsEngine mEng = null;
	private MainApplication mApplication = null;
	private GroupsState mState = GroupsState.IDLE;
	TestModule mTestModule = new TestModule();

	/**
	 ***************************************************************** 
	 * This method is used to initialize GroupsEngine and application object
	 * 
	 * @param : null
	 * @return: null
	 ***************************************************************** 
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mApplication = (MainApplication) Instrumentation.newApplication(
				MainApplication.class, getInstrumentation().getTargetContext());
		mApplication.onCreate();
		mEngineTester = new EngineTestFramework(this);
		mEng = new GroupsEngine(getInstrumentation().getTargetContext(),
				mEngineTester, mApplication.getDatabase());
		mEngineTester.setEngine(mEng);
		mState = GroupsState.IDLE;
	}

	/**
	 ***************************************************************** 
	 * This method releases all the resources
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
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
	 ***************************************************************** 
	 * Method to test getNextruntime() for Groups engine
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	@MediumTest
	public void testGetNextRuntime() {
		boolean testPass = true;
		mState = GroupsState.GET_NEXT_RUNTIME;
		long runtime = mEng.getNextRunTime();
		if (runtime != -1) {
			testPass = false;
		}

		assertTrue("testGetNextRuntime() failed", testPass);
		LogUtils.logI("testGetNextRuntime (SUCCESS) ****\n");
	}

	/**
	 ***************************************************************** 
	 * Method to test add a user defined group on the server.
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Takes to long
	public void testAddUserGroup() {
		boolean testPass = true;
		mState = GroupsState.ADDING_USER_GROUP;
		String grpName = "TestGrp";

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiAddUserDefinedGroup(grpName);
		} catch (Exception e) {
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** testAddGroupsTest (SUCCESS) ****\n");
	}

	/**
	 **************************************************************** 
	 * Method to test add a user defined group on the server for failure
	 * 
	 * @param : null
	 * @return : null
	 **************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Breaks tests.
	public void testAddUserGroupFail() {
		mState = GroupsState.ADDING_USER_GROUP_FAIL;
		String grpName = null;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiAddUserDefinedGroup(grpName);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ***************************************************************** 
	 * Method to test delete an existing user defined group from the server
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Takes to long
	public void testDeleteUserDefinedGroup() {
		boolean testPass = true;
		mState = GroupsState.DELETING_USER_GROUP;
		String grpName = "Friends";

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {

			mEng.addUiDeleteUserDefinedGroup(grpName);
		} catch (Exception e) {
			testPass = false;
		}

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** testDeleteUsergroup (SUCCESS) ****\n");
	}

	/**
	 ***************************************************************** 
	 * Method to test delete an existing user defined group from the server for
	 * failure
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Breaks tests.
	public void testDeleteUserDefinedGroupFail() {
		mState = GroupsState.DELETING_USER_GROUP_FAIL;
		String grpName = null;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteUserDefinedGroup(grpName);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ***************************************************************** 
	 * Method to test to get privacy settings on a group
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Takes to long
	public void testGetPrivacySettings() {
		boolean testPass = true;
		mState = GroupsState.GETTING_GROUP_PRIVACY_SETTING;
		String grpName = "Friends";

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiGetGroupPrivacySetting(grpName);
		} catch (Exception e) {
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** testGetPrivacySettings (SUCCESS) ****\n");
	}

	/**
	 ***************************************************************** 
	 * Method to test to get privacy settings on a group foir failure
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Breaks tests.
	public void testGetPrivacySettingsFail() {
		mState = GroupsState.GETTING_GROUP_PRIVACY_SETTING_FAIL;
		String grpName = null;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetGroupPrivacySetting(grpName);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 **************************************************************** 
	 * Method to test to set privacy settings on a group
	 * 
	 * @param : null
	 * @return : null
	 **************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Takes to long
	public void testSetPrivacySettings() {
		boolean testPass = true;
		mState = GroupsState.SETTING_GROUP_PRIVACY_SETTING;
		String grpName = "Friends";
		int contentType = 1;
		int statusSettings = 1;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiSetGroupPrivacySetting(grpName, contentType,
					statusSettings);
		} catch (Exception e) {
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** testSetPrivacySettings (SUCCESS) ****\n");
	}

	/**
	 ***************************************************************** 
	 * Method to test to set privacy settings on a group for failure
	 * 
	 * @param : null
	 * @return: null
	 ***************************************************************** 
	 */
	@MediumTest
	@Suppress
	// Breaks tests.
	public void testSetPrivacySettingsFail() {
		mState = GroupsState.SETTING_GROUP_PRIVACY_SETTING_FAIL;
		String grpName = null;
		int contentType = 0;
		int statusSettings = -1;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiSetGroupPrivacySetting(grpName, contentType,
					statusSettings);
		} catch (Exception e) {
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
		Log.d("TAG", "GroupsEngineTest.reportBackToEngine");
		ResponseQueue respQueue = ResponseQueue.getInstance();
		List<BaseDataType> data = new ArrayList<BaseDataType>();
		ItemList groupRelationList = new ItemList(
				ItemList.Type.contact_group_relation);
		switch (mState) {
		case IDLE:
			break;
		case ADDING_USER_GROUP:
			LogUtils
					.logD("TAG GroupsEngineTest.reportBackToEngine Add new user group");
			Group grp = new Group();
			data.add(grp);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.ADD_MY_GROUP_RESPONSE
							.ordinal()));
			LogUtils.logD("TAG GroupsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;
		case ADDING_USER_GROUP_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueue(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case DELETING_USER_GROUP:
			LogUtils
					.logD("TAG GroupsEngineTest.reportBackToEngine Delete a group");
			ItemList listOfLongsGroupId = new ItemList(ItemList.Type.long_value);
			data.add(listOfLongsGroupId);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,
					engine,
					DecodedResponse.ResponseType.DELETE_MY_GROUP_RESPONSE
							.ordinal()));
			LogUtils.logD("TAG GroupsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;
		case DELETING_USER_GROUP_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueue(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case GETTING_GROUP_PRIVACY_SETTING:
			break;
		case GETTING_GROUP_PRIVACY_SETTING_FAIL:
			err.errorDescription = "Wrong Input Parameters";
			data.add(err);
			respQueue
					.addToResponseQueue(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case SETTING_GROUP_PRIVACY_SETTING:
			break;
		case SETTING_GROUP_PRIVACY_SETTING_FAIL:
			err.errorDescription = "Wrong Input Parameters";
			data.add(err);
			respQueue
					.addToResponseQueue(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
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
