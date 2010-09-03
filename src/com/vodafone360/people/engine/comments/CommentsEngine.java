package com.vodafone360.people.engine.comments;
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

import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.util.Log;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.CommentListResponse;
import com.vodafone360.people.datatypes.CommentsResponse;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.datatypes.StatusMsg;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.Auth;
import com.vodafone360.people.service.io.api.Comments;
import com.vodafone360.people.utils.LogUtils;

/***
 * Comments engine for posting, getting, updating and deleting comments on
 * entities (album, contacts etc)
 * <p>
 * File Name : CommentsEngine.java
 * <p>
 * Description : This class extends BaseEngine and contain various overridden
 * methods.
 * <p>
 * Revision History
 * <p>
 * ------------------------------------------------------------------------
 * <p>
 * Date Author SPR-Id Version Comments
 * <p>
 * - - 0.01 Initial Release
 * <p>
 */
public class CommentsEngine extends BaseEngine {

	public static final String POSTING_COMMENT = "posting_comment";

	public static final String DELETING_COMMENT = "deleting_comment";

	/**
	 * GETTING_COMMENT
	 */

	public static final String GETTING_COMMENT = "getting_comment";

	public static final String UPDATING_COMMENT = "updating_comment";

	private ArrayList<Comment> mComment = new ArrayList<Comment>();

	private ArrayList<CommentsResponse> mCommentsResponse = new ArrayList<CommentsResponse>();

	private CommentListResponse mCommentListResponse = new CommentListResponse();

	private Comment mCommentResponse = new Comment();
	/** engine's current state **/

	private State mState = State.IDLE;

	// private boolean mRequestActivitiesRequired;
	//
	// private Context mContext;
	//
	// private Hashtable<Integer, String> mActiveRequests = new
	// Hashtable<Integer, String>();
	//
	// private DatabaseHelper mDb;
	//	   
	/**
	 * mutex for thread synchronization
	 */
	private Object mMutex = new Object();

	/**
	 * Definitions of Comments engines states; IDLE - engine is inactive
	 * POST_COMMENT: Post comment for an Entity, DELETE_COMMENT: Delete comment
	 * of an Entity
	 */
	private enum State {
		IDLE, POST_COMMENT, DELETE_COMMENT, GET_COMMENT, UPDATE_COMMENT
	}

	public CommentsEngine(IEngineEventCallback eventCallback) {
		super(eventCallback);
		mEngineId = EngineId.COMMENTS_ENGINE;
		// mDb = db;
		// mContext = context;
	}

	/**
	 * Return next run time for CommentsEngine. Determined by whether we have a
	 * request we wish to issue, or there is a response that needs processing.
	 */
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
		LogUtils.logD("CommentsEngine.OnCreate()");
		mState = State.IDLE;
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
		LogUtils.logD("CommentsEngine.onTimeoutEvent() in State: " + mState);
	}

	/**
	 * Handle an outstanding UI request.
	 */
	@Override
	protected void processUiRequest(ServiceUiRequest requestId, Object data) {
		LogUtils.logD("LoginEngine.processUiRequest() - reqID = " + requestId);
		switch (requestId) {
		case POST_COMMENT:
			startPostComment((List<Comment>) data);
			break;
		case DELETE_COMMENT:
			startDeleteComment(data);
			break;
		case GET_COMMENT:
			startGetComment((List<EntityKey>) data);
			break;
		case UPDATE_COMMENT:
			startUpdateComment((List<Comment>) data);
			break;
		default:
			completeUiRequest(ServiceStatus.ERROR_NOT_FOUND, null);
		}
	}

	/**
	 * Changes the state of the engine.
	 * 
	 * @param newState
	 *            The new state
	 */
	private void newState(State newState) {
		State oldState = mState;
		synchronized (mMutex) {
			mState = newState;
		}
		switch (mState) {
		case POST_COMMENT:
		case DELETE_COMMENT:
		case GET_COMMENT:
		case UPDATE_COMMENT:
		case IDLE:
		default:
			break;
		}
		LogUtils.logV("ActivitiesEngine.newState(): " + oldState + " -> "
				+ mState);
	}

	/**
	 * Add request to post comments. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param commentsList
	 *            The list of comments to be posted
	 * @return void
	 */
	public void addUiPostCommentRequest(List<Comment> commentsList) {
		LogUtils.logD("CommentsEngine.addUiPostCommentRequest()");
		addUiRequestToQueue(ServiceUiRequest.POST_COMMENT, commentsList);
	}

	/**
	 * Add request to delete comments. The request is added to the UI request
	 * and processed when the engine is ready.
	 * 
	 * @param data
	 *            Bundle contains the list of comments to be deleted and the
	 *            owner ID of the comments
	 * @return void
	 */
	public void addUiDeleteCommentRequest(Object data) {
		LogUtils.logD("CommentsEngine.addUiDeleteCommentRequest()");
		addUiRequestToQueue(ServiceUiRequest.DELETE_COMMENT, data);
	}

	/**
	 * addUiGetCommentRequest- getting the Comment
	 * 
	 * @param commentsList
	 * @return void
	 */
	public void addUiGetCommentRequest(List<EntityKey> entitykeylist) {
		LogUtils.logD("CommentsEngine.addUiGetCommentRequest()");
		addUiRequestToQueue(ServiceUiRequest.GET_COMMENT, entitykeylist);
	}

	/**
	 * Update Comment
	 * 
	 * @param commentsList
	 * @return void
	 */
	public void addUiUpdateCommentRequest(List<Comment> commentsList) {
		LogUtils.logD("CommentsEngine.addUiUpdateCommentRequest()");
		addUiRequestToQueue(ServiceUiRequest.UPDATE_COMMENT, commentsList);
	}

	/**
	 * Issue request to Delete comments. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param data
	 *            Bundled request data.
	 * @return void
	 */
	private void startDeleteComment(Object data) {
		LogUtils.logD("CommentsEngine.startDeleteComment()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}
		Bundle b = (Bundle) data;
		Long ownerId = b.getLong("ownerid");
		long commentIds[] = b.getLongArray("commentidlist");

		List<Long> commentsList = new ArrayList<Long>();
		for (int i = 0; i < commentIds.length; i++) {
			commentsList.add(new Long(commentIds[i]));
		}

		newState(State.DELETE_COMMENT);
		if (!setReqId(Comments.deleteComment(this, commentsList, ownerId))) {
			Log.d("CommentsEngine", "Error in startPostComment");
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}
	}

	/**
	 * Issue request to post comments. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param commentsList
	 *            The list of comments to be added.
	 * @return void
	 */
	private void startPostComment(List<Comment> commentsList) {
		LogUtils.logD("CommentsEngine.startPostComment()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}

		newState(State.POST_COMMENT);
		if (!setReqId(Comments.postComment(this, commentsList))) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}
	}

	/**
	 * Issue request to get comments. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param entityKeyList
	 * @return void
	 */
	private void startGetComment(List<EntityKey> entitykeylist) {
		LogUtils.logD("CommentsEngine.startPostComment()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}

		newState(State.GET_COMMENT);
		if (!setReqId(Comments.getComment(this, entitykeylist))) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}
	}

	/**
	 * Issue request to update comments. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param commentsList
	 * @return void
	 */
	private void startUpdateComment(List<Comment> commentsList) {
		LogUtils.logD("CommentsEngine.startUpdateComment()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}

		newState(State.UPDATE_COMMENT);
		if (!setReqId(Comments.updateComment(this, commentsList))) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}
	}

	/**
	 * Handle Server response to delete comments request.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleDeleteCommentResponse(List<BaseDataType> data) {
		LogUtils.logD("CommentsEngine.handlePostCommentResponse()");

		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.COMMENTS_RESPONSE_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			mComment.clear();

			for (BaseDataType item : data) {
				if (BaseDataType.COMMENTS_RESPONSE_DATATYPE == item.getType()) {
					mCommentsResponse.add((CommentsResponse) item);
					LogUtils.logD("Comment id: "
							+ ((CommentsResponse) item).mCommentIdList);
				} else {
					LogUtils
							.logE("Comemnts Engine: handleDeleteCommentResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
		} else {
			LogUtils
					.logE("CommentsEngine handleDeleteCommentResponse error status: "
							+ errorStatus);
		}
	}

	/**
	 * Handle Server response to post comments request.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handlePostCommentResponse(List<BaseDataType> data) {
		LogUtils.logD("CommentsEngine.handlePostCommentResponse()");
		// ServiceStatus errorStatus =
		// genericHandleResponseType("CommentsResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.COMMENTS_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			mCommentsResponse.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.COMMENTS_RESPONSE_DATATYPE == item.getType()) {
					mCommentsResponse.add((CommentsResponse) item);
					LogUtils.logD("Comment id: "
							+ ((CommentsResponse) item).mCommentIdList);
				} else {
					LogUtils
							.logE("CommentsEngine: handlePostCommentResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
		} else {
			LogUtils
					.logE("CommentsEngine handlePostCommentResponse error status: "
							+ errorStatus);
		}
	}

	/**
	 * Handle Server response to get comments request.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleGetCommentResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleGetCommentResponse()");

		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.COMMENT_LIST_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			for (BaseDataType item : data) {
				mCommentsResponse.clear();
				if (BaseDataType.COMMENT_LIST_DATATYPE == item.getType()) {
					mCommentListResponse = (CommentListResponse) item;
					LogUtils.logD("Comment id: "
							+ mCommentListResponse.mCommentList);
				} else {
					LogUtils
							.logE("CommentsEngine handleGetCommentResponse Unexpected response: "
									+ item.getType());
					return;
				}
			}
		} else {
			LogUtils
					.logE("CommentsEngine handleGetCommentResponse error status: "
							+ errorStatus.name());
		}
		completeUiRequest(ServiceStatus.SUCCESS);
		newState(State.IDLE);
	}

	/**
	 * Handle Server response to update comments request.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleUpdateCommentResponse(List<BaseDataType> data) {
		LogUtils.logD("CommentsEngine.handleUpdateCommentResponse()");
		// ServiceStatus errorStatus =
		// genericHandleResponseType("CommentsResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.COMMENTS_RESPONSE_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			mCommentsResponse.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.COMMENTS_RESPONSE_DATATYPE == item.getType()) {
					mCommentsResponse.add((CommentsResponse) item);
					LogUtils.logD("Comment id: "
							+ (((CommentsResponse) item).mCommentIdList));
				} else {
					LogUtils
							.logE("CommentsEngine handleUpdateCommentResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
		} else {
			LogUtils
					.logE("CommentsEngine handlePostCommentResponse error status: "
							+ errorStatus);
		}
	}

	/**
	 * Run function called via EngineManager. Should have a UI, Comms response
	 * or timeout event to handle.
	 */
	@Override
	public void run() {
		LogUtils.logD("CommentsEngine run");
		processTimeout();
		if (isCommsResponseOutstanding() && processCommsInQueue()) {
			return;
		}
		if (isUiRequestOutstanding()) {
			processUiQueue();
		}
	}

	/**
	 * Called when a server response is received, processes the response based
	 * on the engine state.
	 * 
	 * @param resp
	 *            Response data from server
	 * @return null
	 */
	@Override
	protected void processCommsResponse(DecodedResponse resp) {
		LogUtils.logD("CommentsEngine processCommsResponse");
		switch (mState) {
		case POST_COMMENT:
			handlePostCommentResponse(resp.mDataTypes);
			break;
		case DELETE_COMMENT:
			handleDeleteCommentResponse(resp.mDataTypes);
			break;
		case GET_COMMENT:
			handleGetCommentResponse(resp.mDataTypes);
			break;
		case UPDATE_COMMENT:
			handleUpdateCommentResponse(resp.mDataTypes);
			break;
		default: // do nothing.
			break;
		}
		// switch (resp.mDataTypes.get(0).getType())
		// {
		// case BaseDataType.COMMENTS_RESPONSE_DATATYPE:
		// handlePostCommentResponse(resp.mDataTypes);
		// break;
		// case BaseDataType.COMMENTS_RESPONSE_DATATYPE:
		// handleDeleteCommentResponse(resp.mDataTypes);
		// break;
		// case BaseDataType.COMMENT_LIST_DATATYPE:
		// handleGetCommentResponse(resp.mDataTypes);
		// break;
		// case BaseDataType.COMMENTS_RESPONSE_DATATYPE:
		// handleUpdateCommentResponse(resp.mDataTypes);
		// break;
		// default: // do nothing.
		// break;
		// }
	}
}
