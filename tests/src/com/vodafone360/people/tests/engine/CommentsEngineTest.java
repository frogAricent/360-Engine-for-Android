
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
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.tables.StateTable;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.CommentListResponse;
import com.vodafone360.people.datatypes.CommentsResponse;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.datatypes.ServerError;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.comments.CommentsEngine;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.utils.LogUtils;

/**
 * 
 * File Name : CommentsEngineTest.java 
 * Description : This class implements IEngineTestFrameworkObserver interface and contain various callback methods.
 *  
 * Revision History
 * --------------------------------------------------------------
 * Date		 Author		 SPR-Id		 Version		 Comments
 * 01-Sep-10 	-		 - 			  0.01 			 Initial Release
 * 
 * 
 */

public class CommentsEngineTest extends InstrumentationTestCase implements
		IEngineTestFrameworkObserver {

	/**
	 * ***************************************************************** 
	 * States for CommentsEngine. States are based on the requests that the
	 * engine needs to handle.
	 ****************************************************************** 
	 */

	private enum CommentsState {
		IDLE, 
		POST_COMMENT, 
		POST_COMMENT_FAIL, 
		DELETE_COMMENT, 
		DELETE_COMMENT_FAIL, 
		GET_COMMENT, 
		GET_COMMENT_FAIL,
		UPDATE_COMMENT,
		UPDATE_COMMENT_FAIL,
		GET_NEXT_RUNTIME
	}

	private static final String LOG_TAG = "CommentsEngineTest";
	private EngineTestFramework mEngineTester = null;
	private CommentsEngine mEng = null;
	private MainApplication mApplication = null;
	private CommentsState mState = CommentsState.IDLE;
	private static DatabaseHelper mDatabaseHelper = null;

	/**
	 ******************************************************************  
	 * Method to initialize all the resources 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mEngineTester = new EngineTestFramework(this);
		mEng = new CommentsEngine(mEngineTester);

		mApplication = (MainApplication) Instrumentation.newApplication(
				MainApplication.class, getInstrumentation().getTargetContext());
		mEngineTester.setEngine(mEng);
		mState = CommentsState.IDLE;
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
		mEngineTester.stopEventThread();
		mEngineTester = null;
		mEng = null;
		// call at the end!!!
		super.tearDown();
	}

	/**
	 ******************************************************************* 
	 * Method to test getNextruntime() for Groups engine 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
    public void testGetNextRuntime() {
        boolean testPass = true;
        mState = CommentsState.GET_NEXT_RUNTIME;
        long runtime = mEng.getNextRunTime();
        if (runtime != -1) {
            testPass = false;
        }

        assertTrue("testGetNextRuntime() failed", testPass);
        LogUtils.logI("**** testGetNextRuntime (SUCCESS) ****\n");
    }
 
	/**
	 ******************************************************************* 
	 * Method to test to post comments on entity elements like contacts, albums 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Takes to long
	public void testPostComment() {
		boolean testPass = true;
		mState = CommentsState.POST_COMMENT;
		List<Comment> commentsList = new ArrayList<Comment>();
		Comment comment = new Comment();
		comment.mCommentId = null;
		comment.mInappropriate = false;
		comment.mText = "Test Comment On Albums";

		EntityKey entityKey = new EntityKey();
		entityKey.mEntityId = new Long(1101033);
		entityKey.mEntityType = "album";
		comment.mEntityKey = entityKey;
		commentsList.add(comment);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiPostCommentRequest(commentsList);
		} catch (Exception e) {
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** testPostCommment (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test to post comments on entity elements like contacts,albums for failure 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Breaks tests.
	public void testPostCommentFail() {
		mState = CommentsState.POST_COMMENT_FAIL;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiPostCommentRequest(null);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}

	/**
	 ******************************************************************* 
	 * Method to test to get comments posted by the user 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Takes to long
	public void testGetComment() {
		boolean testPass = true;
		mState = CommentsState.GET_COMMENT;
		List<EntityKey> entitykeylist = new ArrayList<EntityKey>();
		EntityKey entityKey = new EntityKey();
		entityKey.mEntityId = new Long(1101033);
		entityKey.mEntityType = "album";

		entitykeylist.add(entityKey);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiGetCommentRequest(entitykeylist);
		} catch (Exception e) {
			e.printStackTrace();
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** testGetComments (SUCCESS) ****\n");
	}


	/**
	 ******************************************************************* 
	 * Method to test to get comments posted by the user for failure 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Breaks tests.
	public void testGetCommentFail() {
		mState = CommentsState.GET_COMMENT_FAIL;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiGetCommentRequest(null);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}


	/**
	 ******************************************************************* 
	 * Method to test to delete comments posted by the user 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Takes to long
	public void testDeleteComment() {
		mDatabaseHelper = mApplication.getDatabase();
		boolean testPass = true;

		mState = CommentsState.DELETE_COMMENT;

		Long ownerId = StateTable.fetchMeProfileId(mDatabaseHelper.getReadableDatabase());
		long[] commentsList = new long[1];
		commentsList[0] = new Long(122327);
		Bundle bundle = new Bundle();
		bundle.putLongArray("commentidlist", commentsList);
		bundle.putLong("ownerid", ownerId);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiDeleteCommentRequest(bundle);
		} catch (Exception e) {
			e.printStackTrace();
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** DeleteComment (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test to delete comments posted by the user for failure 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Breaks tests.
	public void testDeleteCommentFail() {
		mState = CommentsState.DELETE_COMMENT_FAIL;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteCommentRequest(null);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}
	
	/**
	 ******************************************************************* 
	 * Method to test to update comments posted by the user 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Takes to long
	public void testUpdateComment() {
		mDatabaseHelper = mApplication.getDatabase();
		boolean testPass = true;

		mState = CommentsState.UPDATE_COMMENT;

		List<Comment> commentsList = new ArrayList<Comment>();
    	Comment comment = new Comment();
    	comment.mCommentId = new Long(122547);
    	comment.mInappropriate = false;
    	comment.mText = "Update comment";
    	
    	EntityKey entityKey = new EntityKey();
	    	//entityKey.mEntityId = new Long(1089443);
    		entityKey.mEntityId = new Long(1101033);
	    	entityKey.mEntityType = "album";
	    	comment.mEntityKey = entityKey;
	    	commentsList.add(comment);

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiDeleteCommentRequest(commentsList);
		} catch (Exception e) {
			e.printStackTrace();
			testPass = false;
		}
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		Object data = mEngineTester.data();
		assertTrue(data != null);
		LogUtils.logI("**** UpdateComment (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test to update comments posted by the user for failure case 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */	
	@MediumTest
	@Suppress // Breaks tests.
	public void testUpdateCommentFail() {
		mState = CommentsState.UPDATE_COMMENT_FAIL;

		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		mEng.addUiDeleteCommentRequest(null);
		// mEng.run();
		ServiceStatus status = mEngineTester.waitForEvent();
		assertFalse(ServiceStatus.SUCCESS == status);

		Object data = mEngineTester.data();
		assertNull(data);
	}


	@Override
	public void reportBackToEngine(int reqId, EngineId engine) {
		ServerError err = new ServerError("Catastrophe");
		Log.d("TAG", "LocationEngineTest.reportBackToEngine");
		ResponseQueue respQueue = ResponseQueue.getInstance();
		List<BaseDataType> data = new ArrayList<BaseDataType>();
		CommentsResponse mCommentsResponse = new CommentsResponse();

		switch (mState) {
		case IDLE:
			break;
		case POST_COMMENT:
			Log.d("TAG", "CommentsEngineTest.reportBackToEngine Post Comment");
			data.add(mCommentsResponse);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.POST_COMMENTS_RESPONSE.ordinal()));
			Log.d("TAG", "CommentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case POST_COMMENT_FAIL:
			err.errorDescription = "Fail";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();

		case GET_COMMENT:
			Log.d("TAG", "CommentsEngineTest.reportBackToEngine Get comments");
			CommentListResponse mComment1 = new CommentListResponse();
			data.add(mComment1);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.GET_COMMENTS_RESPONSE.ordinal()));
			Log.d("TAG", "CommentsEngineTest.reportBackToEngine add to Q");
		mEng.onCommsInMessage();
			break;

		case GET_COMMENT_FAIL:
			err.errorDescription = "Fail";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();
		case DELETE_COMMENT:
			Log.d("TAG","CommentsEngineTest.reportBackToEngine Delete comments");
			data.add(mCommentsResponse);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.DELETE_COMMENTS_RESPONSE.ordinal()));
			Log.d("TAG", "CommentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case DELETE_COMMENT_FAIL:
			err.errorDescription = "Fail";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();
		case UPDATE_COMMENT:
			Log.d("TAG","CommentsEngineTest.reportBackToEngine Delete comments");
			data.add(mCommentsResponse);
			respQueue.addToResponseQueue(new DecodedResponse(reqId, data,engine,DecodedResponse.ResponseType.UPDATE_COMMENTS_RESPONSE.ordinal()));
			Log.d("TAG", "CommentsEngineTest.reportBackToEngine add to Q");
			mEng.onCommsInMessage();
			break;

		case UPDATE_COMMENT_FAIL:
			err.errorDescription = "Fail";
            data.add(err);
            respQueue.addToResponseQueue(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.SERVER_ERROR.ordinal()));
            mEng.onCommsInMessage();
		default:
		
		}

	}

	@Override
	public void onEngineException(Exception exp) {
		// TODO Auto-generated method stub
}
}