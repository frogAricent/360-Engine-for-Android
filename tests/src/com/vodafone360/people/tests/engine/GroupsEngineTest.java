
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
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Group;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.datatypes.ItemList;
import com.vodafone360.people.datatypes.ListOfLong;
import com.vodafone360.people.datatypes.ServerError;
import com.vodafone360.people.datatypes.StatusMsg;
import com.vodafone360.people.datatypes.ItemList.Type;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.groups.GroupsEngine;
import com.vodafone360.people.engine.login.LoginEngine;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.GroupPrivacy;
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
		IDLE, ADDING_USER_GROUP, ADDING_USER_GROUP_FAIL, DELETING_USER_GROUP, DELETING_USER_GROUP_FAIL, GETTING_GROUP_PRIVACY_SETTING, GETTING_GROUPS, GETTING_GROUP_FAIL,GETTING_GROUP_PRIVACY_SETTING_FAIL, SETTING_GROUP_PRIVACY_SETTING, SETTING_GROUP_PRIVACY_SETTING_FAIL, GET_NEXT_RUNTIME
	}

	private static final String LOG_TAG = "GroupsEngineTest";
	private EngineTestFramework mEngineTester = null;
	private GroupsEngine mEng = null;
	private MainApplication mApplication = null;
	private GroupsState mState = GroupsState.IDLE;
	TestModule mTestModule = new TestModule();
	EngineManager mEngineManager = null;
	LoginEngine mLoginEngine = null;

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
		
		mEngineManager= EngineManager.createEngineManagerForTest(null ,mEngineTester);
        mEngineManager.addEngineForTest(mEng);
        
        mLoginEngine = new LoginEngine(getInstrumentation().getTargetContext(), mEngineTester, mApplication.getDatabase());
        mEngineManager.addEngineForTest(mLoginEngine);
        
        mEng.setTestMode(true);
      
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
		//boolean testPass = true;
		mState = GroupsState.GET_NEXT_RUNTIME;
//		long runtime = mEng.getNextRunTimeForTest();
//		if (runtime != -1) {
//			testPass = false;
//		}
//
//		assertTrue("testGetNextRuntime() failed", testPass);
//		LogUtils.logI("testGetNextRuntime (SUCCESS) ****\n");
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
	// Takes to long
	public void testAddUserGroup() {
		mState = GroupsState.ADDING_USER_GROUP;
		String grpName = "TestGrp";

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiAddUserDefinedGroup(grpName);
		
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
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
	// Takes to long
	public void testDeleteUserDefinedGroup() {
		mState = GroupsState.DELETING_USER_GROUP;
		String grpName = "Friends";

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteUserDefinedGroup(grpName,new Long(172364));
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
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
	public void testDeleteUserDefinedGroupFail() {
		mState = GroupsState.DELETING_USER_GROUP_FAIL;
		String grpName = null;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteUserDefinedGroup(grpName,new Long(172364));
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}
	/**
	 ***************************************************************** 
	 * Method to test to get groups 
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	*/
	public void testGetGroups()
	{
		mState = GroupsState.GETTING_GROUPS;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetGroupsRequest();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** testGetGroups (SUCCESS) ****\n");
		
	}
	
	/**
	 ***************************************************************** 
	 * Method to test to get privacy settings on a group
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	//@MediumTest
	/**
	 * Commented as it is not used anywhere
	 */
	/*public void testGetPrivacySettings() {
		mState = GroupsState.GETTING_GROUP_PRIVACY_SETTING;
		String grpName = "Friends";

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiGetGroupPrivacySetting(grpName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** testGetPrivacySettings (SUCCESS) ****\n");
	}*/

	/**
	 ***************************************************************** 
	 * Method to test to get privacy settings on a group foir failure
	 * 
	 * @param : null
	 * @return : null
	 ***************************************************************** 
	 */
	@MediumTest
	// Breaks tests.
	/*public void testGetPrivacySettingsFail() {
	mState = GroupsState.GETTING_GROUP_PRIVACY_SETTING_FAIL;
		String grpName = null;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetGroupPrivacySetting(grpName);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}*/

	/**
	 **************************************************************** 
	 * Method to test to set privacy settings on a group
	 * 
	 * @param : null
	 * @return : null
	 **************************************************************** 
	 */
	//@MediumTest
	// Takes to long
	/*public void testSetPrivacySettings() {

		mState = GroupsState.SETTING_GROUP_PRIVACY_SETTING;
		String grpName = "Friends";
		int contentType = 1;
		int statusSettings = 1;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiSetGroupPrivacySetting(grpName, contentType,
					statusSettings);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** testSetPrivacySettings (SUCCESS) ****\n");
	}
*/
	/**
	 ***************************************************************** 
	 * Method to test to set privacy settings on a group for failure
	 * 
	 * @param : null
	 * @return: null
	 ***************************************************************** 
	 */
	//@MediumTest	
	// Breaks tests.
//	public void testSetPrivacySettingsFail() {
//		mState = GroupsState.SETTING_GROUP_PRIVACY_SETTING_FAIL;
//		String grpName = null;
//		int contentType = 0;
//		int statusSettings = -1;
//
//		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
//		try {
//			mEng.addUiSetGroupPrivacySetting(grpName, contentType,
//					statusSettings);
//		} catch (Exception e) {
//			e.printStackTrace();
//
//		}
//
//		// mEng.run();
//		ServiceStatus status = mEngineTester.waitForEvent();
//		assertFalse(ServiceStatus.SUCCESS == status);
//		Object data = mEngineTester.data();
//		assertNull(data);
//	}

	@Override
	public void reportBackToEngine(int reqId, EngineId engine) {
		ServerError err = new ServerError("Catastrophe");
		Log.d("TAG", "GroupsEngineTest.reportBackToEngine");
		ResponseQueue respQueue = ResponseQueue.getInstance();
		List<BaseDataType> data = new ArrayList<BaseDataType>();
		switch (mState) {
		case IDLE:
			break;
		case ADDING_USER_GROUP:
			LogUtils
					.logD("TAG GroupsEngineTest.reportBackToEngine Add new user group");
			Group grp = new Group();
			data.add(grp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.ADD_MY_GROUP_RESPONSE
							.ordinal()));
			LogUtils.logD("TAG GroupsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;
		case ADDING_USER_GROUP_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
			
		case DELETING_USER_GROUP:
			LogUtils.logD("TAG GroupsEngineTest.reportBackToEngine Delete a group");
			ListOfLong listOfLong = new ListOfLong();
			listOfLong.mLongList = null;
			listOfLong.mListSize = 1;
            respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.DELETE_MY_GROUP_RESPONSE.ordinal()));
            mEng.onCommsInMessage();
			break;
		case DELETING_USER_GROUP_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case GETTING_GROUP_PRIVACY_SETTING:
			StatusMsg msg4 = new StatusMsg();
        	msg4.mCode = "ok";
        	msg4.mDryRun = false;
        	msg4.mStatus = true;
            data.add(msg4);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.GET_PRIVACY_SETTINGS_RESPONSE.ordinal()));
            mEng.onCommsInMessage();
        	break;
		case GETTING_GROUP_PRIVACY_SETTING_FAIL:
			err.errorDescription = "Wrong Input Parameters";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case SETTING_GROUP_PRIVACY_SETTING:
			StatusMsg msg1 = new StatusMsg();
			msg1.mCode = "ok";
			msg1.mDryRun = false;
			msg1.mStatus = true;
            data.add(msg1);
            respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SET_PRIVACY_SETTINGS_RESPONSE.ordinal()));
            mEng.onCommsInMessage();
			break;
			
		case SETTING_GROUP_PRIVACY_SETTING_FAIL:
			err.errorDescription = "Wrong Input Parameters";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
			
		case GETTING_GROUPS : 
			LogUtils.logD("TAG GroupsEngineTest.reportBackToEngine Delete a group");
			ItemList list = new ItemList(Type.group_privacy);
			GroupItem item = new GroupItem();
			item.mId = new Long(1);
			item.mColor = "red";
			List<BaseDataType> list1 = new ArrayList<BaseDataType>();
			list1.add(item);
			list.mItemList = list1;
			list.mType = Type.group_privacy;
			data.add(list);
            respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.GET_GROUPS_RESPONSE.ordinal()));
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
