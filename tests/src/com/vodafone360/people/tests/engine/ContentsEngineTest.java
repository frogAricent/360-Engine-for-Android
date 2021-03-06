
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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Instrumentation;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.datatypes.Album;
import com.vodafone360.people.datatypes.AlbumListResponse;
import com.vodafone360.people.datatypes.AlbumResponse;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Content;
import com.vodafone360.people.datatypes.ContentListResponse;
import com.vodafone360.people.datatypes.ContentResponse;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.datatypes.ServerError;
import com.vodafone360.people.engine.EngineManager;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.content.ContentEngine;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.utils.LogUtils;

/**
 * 
 * File Name : ContentsEngineTest.java 
 * Description : This class implements IEngineTestFrameworkObserver interface and contain various callback methods.
 *  
 * Revision History
 * --------------------------------------------------------------
 * Date		 Author		 SPR-Id		 Version		 Comments
 * 01-Sep-10 	-		 - 			  0.01 			 Initial Release
 * 
 * 
 */
public class ContentsEngineTest extends InstrumentationTestCase implements
		IEngineTestFrameworkObserver {

	/**
	 ******************************************************************* 
	 * States for ContentEngine. States are based on the requests that the
	 * engine needs to handle.
	 ******************************************************************* 
	 */

	private enum ContentsState {
		IDLE, 
		ADDING_ALBUM,
		ADDING_ALBUM_FAIL,
		UPDATING_ALBUM, 
		UPDATING_ALBUM_FAIL,
		DELETING_ALBUM,
		DELETING_ALBUM_FAIL,
		GETTING_ALBUM,
		GETTING_ALBUM_FAIL,
		PUBLISHING_ALBUM,
		PUBLISHING_ALBUM_FAIL,
		ADDING_CONTENT_TO_ALBUM,
		ADDING_CONTENT_TO_ALBUM_FAIL,
		DELETING_CONTENT_FROM_ALBUM, 
		DELETING_CONTENT_FROM_ALBUM_FAIL,
		GETTING_CONTENT,
		GETTING_CONTENT_FAIL,
		UPLOADING_CONTENT,
		UPLOADING_CONTENT_FAIL,
		UPLOADING_AND_PUBLISHING, 
		UPLOADING_AND_PUBLISHING_FAIL,
		DOWNLOADING_CONTENT,
		DOWNLOADING_CONTENT_FAIL, 
		PUBLISHING_CONTENT,
		PUBLISHING_CONTENT_FAIL,
		DELETING_CONTENT,
		DELETING_CONTENT_FAIL,
		GET_NEXT_RUNTIME,
		
	}

	private Object mObjectLock = new Object();

	private static final String LOG_TAG = "ContentEngineTest";
	private EngineTestFramework mEngineTester = null;
	private ContentEngine mEng = null;
	private MainApplication mApplication = null;
	private ContentsState mState = ContentsState.IDLE;
	EngineManager mEngineManager = null;

	/**
	 ******************************************************************* 
	 * All the initialisations are done here
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
		mEngineTester = new EngineTestFramework(this);
		mEng = new ContentEngine(mEngineTester, mApplication.getDatabase());
		mEngineTester.setEngine(mEng);
		mState = ContentsState.IDLE;
		
		mEngineManager= EngineManager.createEngineManagerForTest(null ,mEngineTester);
        mEngineManager.addEngineForTest(mEng);
                     
		mEng.setTestMode(true);
	}

	/**
	 ******************************************************************* 
	 * this method frees all the resources
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
	 * Method to test GetNextrunTime() method of contentEngine Determines the
	 * next RunTime of this Engine
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testGetNextRuntime() {
		boolean testPass = true;
		mState = ContentsState.GET_NEXT_RUNTIME;
		long runtime = mEng.getNextRunTime();
		if (runtime != -1) {
			testPass = false;
		}

		assertTrue("testGetNextRuntime() failed", testPass);
		LogUtils.logI("**** testGetNextRuntime (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test getting all the existing albums from the server
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	// Takes to long
	public void testGetAlbums() {
		mState = ContentsState.GETTING_ALBUM;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetAlbumRequest(null);
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** testGetAlbum (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test getting all the existing albums from the server with wrong
	 * inputs
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testGetAlbumsFail() {
		mState = ContentsState.GETTING_ALBUM_FAIL;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		List<Long> albumList = new ArrayList<Long>();
		albumList.add(new Long(0L));
		mEng.addUiGetAlbumRequest(albumList); // mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test adding an album on the server
	 * 
	 * @param : null
	 * @return : null
	 * ******************************************************************
	 */
	@MediumTest
	public void testAddAlbums() {
		mState = ContentsState.ADDING_ALBUM;

		List<Album> albumlist = new ArrayList<Album>();
		Album album = new Album();
		ArrayList<GroupItem> allGroupList = new ArrayList<GroupItem>();
		
		GroupItem grpItem = new GroupItem();
		grpItem.mId = Long.parseLong("10273123");
		grpItem.mUserId = Long.parseLong("12212916");
		grpItem.mName = "Friends";
		grpItem.mImageMimeType = null;
		grpItem.mColor = null;
		allGroupList.add(grpItem);
		
		album.mGrouplist = allGroupList;
		//album.mGrouplist.add(grpItem);
		album.mCreated = new Long(System.currentTimeMillis());
		album.mUpdated = new Long(System.currentTimeMillis());
		album.mTitle = "Test album";
		albumlist.add(album);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);

		try {
			mEng.addUiAddAlbumRequest(albumlist);

		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
//		data = mEngineTester.data();
//		assertTrue(data != null);
	}

	/**
	 ******************************************************************* 
	 * Method to test add an album on the server for failure case
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testAddAlbumsFail() {
		mState = ContentsState.ADDING_ALBUM_FAIL;
		List<Album> albumlist = new ArrayList<Album>();
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiAddAlbumRequest(albumlist);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test delete a list of albums on the server
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testDeleteAlbums() {
		mState = ContentsState.DELETING_ALBUM;
		List<Long> mAlbumList = new ArrayList<Long>();
		mAlbumList.add(new Long(1083369));

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteAlbumRequest(mAlbumList);

		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("testDeleteAlbum (SUCCESS)\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test delete a list of albums on the server for failure case
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testDeleteAlbumsFail() {
		mState = ContentsState.DELETING_ALBUM_FAIL;
		List<Long> mAlbumList = new ArrayList<Long>();
		mAlbumList.add(new Long(0L));
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetAlbumRequest(mAlbumList);
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test update an existing album on the server
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testUpdateAlbum() {
		boolean testPass = true;
		mState = ContentsState.ADDING_ALBUM;

		List<Album> albumlist = new ArrayList<Album>();
		Album album = new Album();
		GroupItem grpItem = new GroupItem();
		grpItem.mId = Long.parseLong("27");
		grpItem.mUserId = Long.parseLong("12212916");
		grpItem.mName = "Friends";
		grpItem.mImageMimeType = null;
		grpItem.mColor = "2";

		album.mGrouplist = new ArrayList<GroupItem>();
		album.mGrouplist.add(grpItem);

		album.mUpdated = new Long(System.currentTimeMillis());
		album.mAlbumid = new Long(1083369);
		albumlist.add(album);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiUpdateAlbumRequest(albumlist);
		} catch (Exception e) {
			testPass = false;
		}
		if (!testPass) {
			Log.e(LOG_TAG, "**** testUpdate (FAILED) ****\n");
		}

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** testUpdateAlbum (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test update an existing album on the server for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Breaks tests.
	public void testUpdateAlbumFail() {
		
		mState = ContentsState.UPDATING_ALBUM_FAIL;
		List<Album> albumlist = new ArrayList<Album>();
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiUpdateAlbumRequest(albumlist); // mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);
		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test for adding content to an album on the server
	 * 
	 * @param : null
	 * @return : null
	 *******************************************************************
	 */
	@MediumTest
	
	// Takes to long
	public void testAddContentToAlbum() {
		mState = ContentsState.ADDING_CONTENT_TO_ALBUM;
		long[] contentIdList = new long[2];
		contentIdList[0] = new Long(154930128);
		contentIdList[1] = new Long(154925364);
		Bundle b = new Bundle();
		b.putLongArray("contentids", contentIdList);
		b.putLong("aid", new Long(1100831));

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiAddContentToAlbumRequest(b);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** test Add contents to album (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test adding content on album on the server with wrongs inputs
	 * for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Breaks tests.
	public void testAddContentToAlbumFail() {
		mState = ContentsState.ADDING_CONTENT_TO_ALBUM_FAIL;
		
		long[] contentIdList = new long[2];
		contentIdList[0] = 0L;
		Bundle bundle = new Bundle();
		bundle.putLongArray("contentids", contentIdList);
		bundle.putLong("aid", new Long(0));

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiAddContentToAlbumRequest(bundle);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test delete content from the album
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Takes to long
	public void testDeleteContentFromAlbum() {
		mState = ContentsState.DELETING_CONTENT_FROM_ALBUM;
		long[] contentIdList = new long[1];
		contentIdList[0] = new Long(154925364);
		Bundle bundle = new Bundle();
		bundle.putLongArray("contentids", contentIdList);
		bundle.putLong("aid", new Long(1100831));

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteContentFromAlbumRequest(bundle);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** test Delete contents to album (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test delete content from the album for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Breaks tests.
	public void testDeleteContentFromAlbumFail() {
		mState = ContentsState.DELETING_CONTENT_FROM_ALBUM_FAIL;
		long[] contentIdList = new long[1];
		contentIdList[0] = new Long(0);
		Bundle bundle = new Bundle();
		bundle.putLongArray("contentids", contentIdList);
		bundle.putLong("aid", new Long(0));

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteContentFromAlbumRequest(bundle);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test publish an album on the server
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Takes to long
	public void testPublishAlbum() {
		mState = ContentsState.PUBLISHING_ALBUM;
		long[] mAlbumList = new long[1];
		mAlbumList[0] = new Long(1100831);
		Bundle bundle = new Bundle();
		bundle.putLongArray("albumids", mAlbumList);
		bundle.putString("commid", "facebook.com");

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiPublishAlbumRequest(bundle);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** test Publish album (SUCCESS) ****\n");

	}

	/**
	 ******************************************************************* 
	 * Method to test publish an album on server for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Breaks tests.
	public void testPublishAlbumFail() {
		mState = ContentsState.PUBLISHING_ALBUM_FAIL;
		long[] mAlbumList = new long[1];
		mAlbumList[0] = new Long(0);
		Bundle bundle = new Bundle();
		bundle.putLongArray("albumids", mAlbumList);
		bundle.putString("commid", "facebook.com");

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiPublishAlbumRequest(bundle);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test add content(image)
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Takes to long
	public void testAddContent() {
		mState = ContentsState.UPLOADING_CONTENT;
		List<Content> mContentList = new ArrayList<Content>();
		Content content = new Content();
		content.mBytesmime = "image/jpg";
		try {
			File file = new File("/sdcard/flowers.jpg");
			FileInputStream fis = new FileInputStream(file);
			int length = fis.available();
			Log.d("UploadImage", "Length of Byte Array: " + length);
			byte buffer[] = new byte[length];
			fis.read(buffer);
			content.mBytes = buffer;
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mContentList.add(content);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiUploadContentRequest(mContentList);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** test Add Content (SUCCESS) ****\n");

	}

	/**
	 ******************************************************************* 
	 * Method to test add content(image) for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************
	 */
	@MediumTest
	
	// Breaks tests.
	public void testAddContentFail() {
		mState = ContentsState.UPLOADING_CONTENT_FAIL;
		//Bundle bundle = new Bundle();

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetAlbumRequest(null);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test getting content(image) from the server
	 * 
	 * @param : null
	 * @return : null
	 *******************************************************************
	 */
	@MediumTest
	public void testGetContent() {
		mState = ContentsState.GETTING_CONTENT;
		List<Content> mContentList = new ArrayList<Content>();
		long[] contentList = new long[1];
		contentList[0] = 157218897;
		Bundle bundle = new Bundle();
		bundle.clear();
		ArrayList<String> getBytes = new ArrayList<String>();
		getBytes.add("true");

		ArrayList<String> size = new ArrayList<String>();
		size.add("240x320");

		bundle.putLongArray("contentidlist", contentList);
		bundle.putStringArrayList("getbytes", getBytes);
		bundle.putStringArrayList("size", size);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetContentRequest(bundle);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** test Get Contents (SUCCESS) ****\n");

	}

	/**
	 ******************************************************************* 
	 * Method to test getting content(image) from the server for failure case
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testGetContentFail() {
		mState = ContentsState.GETTING_CONTENT_FAIL;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		long[] contentList = null;
		Bundle bundle = new Bundle();
		bundle.clear();
		ArrayList<String> getBytes = new ArrayList<String>();

		ArrayList<String> size = new ArrayList<String>();

		bundle.putLongArray("contentidlist", contentList);
		bundle.putStringArrayList("getbytes", getBytes);
		bundle.putStringArrayList("size", size);

		
		mEng.addUiGetContentRequest(bundle);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test deleting content(image) from the server
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testDeleteContent() {
		mState = ContentsState.DELETING_CONTENT;
		List<Long> mContentList = new ArrayList<Long>();
		mContentList.add(new Long(153935910));

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteContentRequest(mContentList);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** test Delete Contents (SUCCESS) ****\n");

	}
	
	/**
	 ******************************************************************* 
	 * Method to test deleting content(image) from the server
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testUploadContentAndPublish() {
		mState = ContentsState.UPLOADING_AND_PUBLISHING;
		List<Content> mContentList = new ArrayList<Content>();
		Content content = new Content();
		content.mBytesmime = "image/jpg";
		try {
			File file = new File("/data/Sunset.jpg");
			FileInputStream fis = new FileInputStream(file);
			int length = fis.available();
			Log.d("UploadImage", "Length of Byte Array: " + length);
			byte buffer[] = new byte[length];
			fis.read(buffer);
			content.mBytes = buffer;
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mContentList.add(content);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiUploadContentAndPublishRequest(mContentList);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** test Delete Contents (SUCCESS) ****\n");

	}


	/**
	 ******************************************************************* 
	 * Method to test deleting content(image) from the server for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	// Breaks tests.
	public void testDeleteContentFail() {
		mState = ContentsState.DELETING_CONTENT_FAIL;
		List<Long> mContentList = new ArrayList<Long>();

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteContentRequest(mContentList);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test publishing content(image) from the server
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	
	// Takes to long
	public void testPublishContent() {
		mState = ContentsState.PUBLISHING_CONTENT;
		long[] mContentList = new long[1];
		mContentList[0] = new Long(154930128);
		Bundle bundle = new Bundle();
		bundle.putLongArray("contentids", mContentList);
		bundle.putString("commid", "facebook.com");

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiPublishContentRequest(bundle);

		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
//		Object data = mEngineTester.data();
//		assertTrue(data != null);
		LogUtils.logI("**** test Publish Contents (SUCCESS) ****\n");

	}

	/**
	 ******************************************************************* 
	 * Method to test publishing content on the server for failure
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testPublishContentFail() {
		
		mState = ContentsState.PUBLISHING_CONTENT_FAIL;
		long[] mContentList = new long[1];
		mContentList[0] = 0L;
		Bundle bundle = new Bundle();
		
		bundle.putLongArray("contentids", mContentList);
		bundle.putString("commid", "facebook.com");
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		
		mEng.addUiPublishContentRequest(bundle);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}


	@Override
	public void reportBackToEngine(int reqId, EngineId engine) {
		ServerError err = new ServerError("Catastrophe");
		AlbumResponse albrsp = new AlbumResponse();

		Log.d("TAG", "ContentsEngineTest.reportBackToEngine");
		ResponseQueue respQueue = ResponseQueue.getInstance();
		List<BaseDataType> data = new ArrayList<BaseDataType>();

		switch (mState) {
		case IDLE:
			break;
		case GETTING_ALBUM:
			LogUtils.logD("ContentsEngineTest.reportBackToEngine FETCH Albums");
			AlbumListResponse albumListid = new AlbumListResponse();
			data.add(albumListid);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.GET_ALBUMS_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case GETTING_ALBUM_FAIL:
			 ServerError err2 = new ServerError("Catastrophe");
             err2.errorDescription = "Fail";
             data.add(err2);
             respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
             mEng.onCommsInMessage();
             break;
             
		case ADDING_ALBUM:
			LogUtils.logD("ContentsEngineTest.reportBackToEngine Add new Album");
			data.add(albrsp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.ADD_ALBUMS_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case ADDING_ALBUM_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;

		case DELETING_ALBUM:
			LogUtils.logD("ContentsEngineTest.reportBackToEngine Delete Album");
			data.add(albrsp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.ADD_MY_GROUP_RESPONSE
							.ordinal()));
			Log.d("TAG", "ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case DELETING_ALBUM_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;

		case UPDATING_ALBUM:
			LogUtils.logD("ContentsEngineTest.reportBackToEngine Update Album");
			data.add(albrsp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.UPDATE_ALBUMS_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case UPDATING_ALBUM_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case ADDING_CONTENT_TO_ALBUM:
			LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Add content to Album");
			ContentListResponse addContentToAlbumResp = new ContentListResponse();
			data.add(addContentToAlbumResp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine,
					DecodedResponse.ResponseType.ADD_CONTENT_TO_ALBUM_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case ADDING_CONTENT_TO_ALBUM_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case DELETING_CONTENT_FROM_ALBUM:
			LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Delete content from Album");
			ContentListResponse deleteContentFromAlbumResp = new ContentListResponse();
			data.add(deleteContentFromAlbumResp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(
							reqId,
							data,
							engine,
							DecodedResponse.ResponseType.DELETE_CONTENT_FROM_ALBUM_RESPONSE
									.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case DELETING_CONTENT_FROM_ALBUM_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case PUBLISHING_ALBUM:
			LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Publish Album");
			AlbumResponse publishAlbumResp = new AlbumResponse();
			data.add(publishAlbumResp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine,
					DecodedResponse.ResponseType.PUBLISH_ALBUMS_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case PUBLISHING_ALBUM_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue
					.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case UPLOADING_CONTENT:
			LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Adding contents");
			ContentListResponse addContentResp = new ContentListResponse();
			data.add(addContentResp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.ADD_CONTENT_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case UPLOADING_CONTENT_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case GETTING_CONTENT:
			LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Getting contents");
			ContentResponse mContentResponse = new ContentResponse();
			data.add(mContentResponse);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.GET_CONTENT_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case GETTING_CONTENT_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case DELETING_CONTENT:
			LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Deleting contents");
			ContentListResponse deleteContentResp = new ContentListResponse();
			data.add(deleteContentResp);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine,
					DecodedResponse.ResponseType.DELETE_CONTENT_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case DELETING_CONTENT_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case PUBLISHING_CONTENT:
			LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Publishing contents");
			ContentListResponse publishedContent = new ContentListResponse();
			data.add(publishedContent);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine,
					DecodedResponse.ResponseType.PUBLISH_CONTENT_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case PUBLISHING_CONTENT_FAIL:
			err.errorDescription = "Fail";
			data.add(err);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,
							engine, DecodedResponse.ResponseType.SERVER_ERROR
									.ordinal()));
			mEng.onCommsInMessage();
			break;
		case UPLOADING_AND_PUBLISHING :
					LogUtils
					.logD("ContentsEngineTest.reportBackToEngine Adding contents");
			ContentListResponse addContentResp1 = new ContentListResponse();
			data.add(addContentResp1);
			respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data,
					engine, DecodedResponse.ResponseType.ADD_CONTENT_AND_PUBLISH_RESPONSE
							.ordinal()));
			LogUtils.logD("ContentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
	break;
		default:
			break;
		}
	}

	@Override
	public void onEngineException(Exception exp) {
		// TODO Auto-generated method stub

	}

}
