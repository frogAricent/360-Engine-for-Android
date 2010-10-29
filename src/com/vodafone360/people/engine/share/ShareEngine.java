
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

package com.vodafone360.people.engine.share;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.datatypes.ItemList;
import com.vodafone360.people.datatypes.ListOfLong;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.Share;
import com.vodafone360.people.utils.LogUtils;

/***
 * Engine is responsible for handling features like sharing album with groups,
 * allowing and denying groups for sharing albums
 * 
 */
public class ShareEngine extends BaseEngine {

	/**
	 * Definitions of Share engines states; IDLE - engine is inactive
	 * SHARING_ALBUM: Sharing album with ME Profile, ALLOWING_GROUPS : allow
	 * sharing on groups ..etc
	 */
	public static enum State {
		IDLE, SHARING_ALBUM, GETTING_ALBUM_SHARED_WITH, ALLOWING_GROUP, DENYING_GROUP

	}

	private State mState = State.IDLE;

	/**
	 * mutex for thread synchronization
	 */
	private Object mMutex = new Object();
	
	public boolean mJUnitTestMode = false ;

	/** Definitions for expected data-types returned from Server. */
//	private static final String TYPE_SHARE_ALBUM = "ItemList";

	public ShareEngine(IEngineEventCallback eventCallback) {
		super(eventCallback);
		// TODO Auto-generated constructor stub
		mEngineId = EngineId.SHARE_ENGINE;
	}

	/**
	 * Return next run time for ShareEngine. Determined by whether we have a
	 * request we wish to issue, or there is a response that needs processing.
	 */
	@Override
	public long getNextRunTime() {
		if (isUiRequestOutstanding()) {
			return 0;
		}
		if (isCommsResponseOutstanding()) {
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

	// @Override
	// protected void processCommsResponse(Response resp) {
	// LogUtils.logD("ShareEngine.processCommsResponse() - resp = " + resp);
	// switch (mState) {
	// case IDLE:
	// LogUtils.logW("IDLE should never happend");
	// break;
	// case SHARING_ALBUM:
	// handleShareAlbum(resp.mDataTypes);
	// break;
	// case GETTING_ACCESSIBLE_CONTENTID:
	// handleGetSharedContentId(resp.mDataTypes);
	// break;
	// case ALLOWING_GROUP:
	// handleAllowGroup(resp.mDataTypes);
	// break;
	// case DENYING_GROUP:
	// handleDenyGroup(resp.mDataTypes);
	// break;
	// case GETTING_ALBUM_SHARED_WITH:
	// handleAlbumSharedWith(resp.mDataTypes);
	// break;
	// default:
	// LogUtils.logW("default should never happend");
	// break;
	// }
	//
	// }
	/**
	 * Handle an outstanding UI request.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void processUiRequest(ServiceUiRequest requestId, Object data) {
		LogUtils.logD("ShareEngine.processUiRequest - reqID = " + requestId);
		switch (requestId) {
		case SHARE_ALBUM:
			shareAlbum((Hashtable) data);
			break;

		case ALBUM_SHARED_WITH:
			albumSharedWith((EntityKey) data);
			break;

		case ALLOW_GROUP:
			allowGroup((Hashtable) data);
			break;

		case DENY_GROUP:
			denyGroup((Hashtable) data);
			break;
		}

	}

	@Override
	public void run() {
		LogUtils.logD("ShareEngine.run()");
		if (isCommsResponseOutstanding() && processCommsInQueue()) {
			LogUtils
					.logD("ShareEngine.ResponseOutstanding and processCommsInQueue. mState = "
							+ mState.name());
			return;
		}
		if (processTimeout()) {
			return;
		}
		if (isUiRequestOutstanding()) {
			processUiQueue();
		}

	}

	/**
	 * Add request to share album with groups. The request is added to the UI
	 * request and processed when the engine is ready.
	 * 
	 * @param groupID
	 *            : The groupid of the group to which the entities will be
	 *            shared.
	 * @param entityKey
	 *            : album entities which are being shared.
	 * @return void
	 */
	public void addUiShareAlbum(Long groupId, EntityKey entityKey) {
		LogUtils.logD("ShareEngine.addUiShareAlbum()");
		Hashtable<String, Object> inp = new Hashtable<String, Object>();
		inp.put("groupid", groupId);
		inp.put("entitykey", entityKey);
		addUiRequestToQueue(ServiceUiRequest.SHARE_ALBUM, inp);
	}

	/**
	 * Issue request to share album with groups. (Request is not issued if there
	 * is currently no connectivity).
	 * 
	 * @param input
	 *            : The hashtable consists of groupId and entitykey list.
	 * @return void
	 */
	private void shareAlbum(Hashtable<String, Object> input) {
		LogUtils.logD("ShareEngine.shareAlbum()");
		if (!checkConnectivity()) {
			return;
		}
		newState(State.SHARING_ALBUM);
		Long groupId = (Long) input.get("groupid");
		List<EntityKey> entityKeyList = new ArrayList<EntityKey>();
		entityKeyList.add((EntityKey) input.get("entitykey"));
		if (!setReqId(Share.shareWithGroup(this, groupId, entityKeyList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Handle Server response to share album with groups. The response should be
	 * a list of Entity keys for which the access was granted. The request is
	 * completed with ServiceStatus.SUCCESS or ERROR_UNEXPECTED_RESPONSE if the
	 * data-type retrieved are not Entity keys.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleShareAlbum(List<BaseDataType> data) {
		LogUtils.logD("ShareEngine: handleShareAlbum");
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ITEM_LIST_DATA_TYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			if (data.size() != 0) {
				LogUtils.logI("Granted access");
				//TODO:Handling of response from the server to be implemented
				
			} else {
				LogUtils.logE("No data received");
			}
		} else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER) {
			LogUtils.logE("Bad Server Parameter");
		} else {
			LogUtils.logE("Failure");
		}
		completeUiRequest(errorStatus, null);
		newState(State.IDLE);

	}

	/**
	 * Add request to get the groups with which an album is shared. The request
	 * is added to the UI request and processed when the engine is ready.
	 * 
	 * @param entityKey
	 *            : The key representing the album entity for which the shared
	 *            groups will be retrieved.
	 * @return void
	 */
	public void addUiSharedWith(EntityKey entityKey) {
		LogUtils.logD("ShareEngine.addUiSharedWith()");
		addUiRequestToQueue(ServiceUiRequest.ALBUM_SHARED_WITH, entityKey);
	}

	/**
	 * Issue request to get the groups with which an album is shared. (Request
	 * is not issued if there is currently no connectivity).
	 * 
	 * @param entitykey
	 *            : A key representing the album entity for which the shared
	 *            groups will be retrieved
	 * @return void
	 */
	private void albumSharedWith(EntityKey entityKey) {
		LogUtils.logD("ShareEngine.albumSharedWith()");
		if (!checkConnectivity()) {
			return;
		}
		newState(State.GETTING_ALBUM_SHARED_WITH);
		if (!setReqId(Share.sharedWith(this, entityKey))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Handle Server response to get the groups with which an album is shared.
	 * The response should be a List of long The request is completed with
	 * ServiceStatus.SUCCESS or ERROR_UNEXPECTED_RESPONSE if the data-type
	 * retrieved are not Entity keys.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleAlbumSharedWith(List<BaseDataType> data) {

		LogUtils.logD("ShareEngine: handleAlbumSharedWith");
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.LIST_OF_LONG_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			if (data.size() != 0) {
				LogUtils.logI("Groups received");
				//TODO:Handling of response from the server to be implemented
				
			} else {
				LogUtils.logE("No data received");
			}
		} else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER) {
			LogUtils.logE("Bad Server Parameter");
		} else {
			LogUtils.logE("Failure");
		}
		completeUiRequest(errorStatus, null);
		newState(State.IDLE);

	}

	/**
	 * Add request to Grants access for a group to album entities. The request
	 * is added to the UI request and processed when the engine is ready.
	 * 
	 * @param groupId
	 *            : The id of the group to which access to the entities will be
	 *            granted
	 * @param entityKey
	 *            : The keys of the album entities to which the group is being
	 *            allowed to access.
	 * @return void
	 */
	public void addUiAllowGroup(Long groupId, EntityKey entityKey) {
		LogUtils.logD("ShareEngine.addUiAllowGroup()");
		Hashtable<String, Object> inp = new Hashtable<String, Object>();
		if (groupId != null)
			inp.put("groupid", groupId);
		else
			LogUtils.logE("Group Id cannot be null");
		if (entityKey != null)
			inp.put("entitykey", entityKey);
		else
			LogUtils.logE("Entity Key cannot be null");
		addUiRequestToQueue(ServiceUiRequest.ALLOW_GROUP, inp);

	}

	/**
	 * Issue request to Grants access for a group to album entities. (Request is
	 * not issued if there is currently no connectivity).
	 * 
	 * @param input
	 *            : contains the group id
	 * @return void
	 */
	private void allowGroup(Hashtable<String, Object> input) {
		LogUtils.logD("ShareEngine.allowGroup()");
		if (!checkConnectivity()) {
			return;
		}
		newState(State.ALLOWING_GROUP);

		Long groupId = (Long) input.get("groupid");
		List<EntityKey> entityKeyList = new ArrayList<EntityKey>();
		entityKeyList.add((EntityKey) input.get("entitykey"));
		if (!setReqId(Share.allowGroup(this, groupId, entityKeyList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Handle Server response to to grants access for a group to album entities.
	 * The response should be a list of Entity keys for which the access was
	 * granted. The request is completed with ServiceStatus.SUCCESS or
	 * ERROR_UNEXPECTED_RESPONSE if the data-type retrieved are not Entity keys.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleAllowGroup(List<BaseDataType> data) {
		LogUtils.logD("ShareEngine: handleAllowGroup");
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ITEM_LIST_DATA_TYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			if (data.size() != 0) {
				LogUtils.logI("Allowed access");
				//TODO:Handling of response from the server to be implemented
				
			} else {
				LogUtils.logE("No data received");
			}
		} else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER) {
			LogUtils.logE("Bad Server Parameter");
		} else {
			LogUtils.logE("Failure");
		}
		completeUiRequest(errorStatus, null);
		newState(State.IDLE);

	}

	/**
	 * Add request to removes access for a group to album entities. The request
	 * is added to the UI request and processed when the engine is ready.
	 * 
	 * @param groupId
	 *            : The id of the group to which access to the entities will be
	 *            granted
	 * @param entityKey
	 *            : The keys of the album entities to which the group is being
	 *            allowed to access.
	 * @return void
	 */
	public void addUiDenyGroup(Long groupId, EntityKey entityKey) {
		LogUtils.logD("ShareEngine.addUiDenyGroup()");
		Hashtable<String, Object> inp = new Hashtable<String, Object>();
		inp.put("groupid", groupId);
		inp.put("entitykey", entityKey);
		addUiRequestToQueue(ServiceUiRequest.DENY_GROUP, inp);
	}

	/**
	 * Issue request request to removes access for a group to album entities.
	 * (Request is not issued if there is currently no connectivity).
	 * 
	 * @param input
	 *            : contains the group id
	 * @return void
	 */
	private void denyGroup(Hashtable<String, Object> input) {
		LogUtils.logD("ShareEngine.denyGroup()");
		if (!checkConnectivity()) {
			return;
		}
		newState(State.DENYING_GROUP);

		Long groupId = (Long) input.get("groupid");
		List<EntityKey> entityKeyList = new ArrayList<EntityKey>();
		entityKeyList.add((EntityKey) input.get("entitykey"));
		if (!setReqId(Share.denyGroup(this, groupId, entityKeyList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Handle Server response to to removes access for a group to album entities
	 * . The response should be a list of Entity keys for which the access was
	 * granted. The request is completed with ServiceStatus.SUCCESS or
	 * ERROR_UNEXPECTED_RESPONSE if the data-type retrieved are not Entity keys.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleDenyGroup(List<BaseDataType> data) {
		LogUtils.logD("ShareEngine: handleDenyGroup");
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ITEM_LIST_DATA_TYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			if (data.size() != 0) {
				LogUtils.logI("Denied access");
				//TODO:Handling of response from the server to be implemented
				
			} else {
				LogUtils.logE("No data received");
			}
		} else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER) {
			LogUtils.logE("Bad Server Parameter");
		} else {
			LogUtils.logE("Failure");
		}
		completeUiRequest(errorStatus, null);
		newState(State.IDLE);

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
	 * Change current ShareEngine state.
	 * 
	 * @param newState
	 *            new state.
	 */
	private void newState(State newState) {
		State oldState = mState;
		synchronized (mMutex) {
			if (newState == mState) {
				return;
			}
			mState = newState;
		}
		LogUtils.logV("ShareEngine.newState: " + oldState + " -> " + mState);
	}

	/**
	 * Called when a server response is received, processes the response based
	 * on the engine state.
	 * 
	 * @param resp
	 *            Response data from server
	 */
	@Override
	protected void processCommsResponse(DecodedResponse resp) {

		switch (mState) {
		case SHARING_ALBUM:
			handleShareAlbum(resp.mDataTypes);
			break;
		case ALLOWING_GROUP:
			handleAllowGroup(resp.mDataTypes);
			break;
		case DENYING_GROUP:
			handleDenyGroup(resp.mDataTypes);
			break;
		case GETTING_ALBUM_SHARED_WITH:
			handleAlbumSharedWith(resp.mDataTypes);
			break;
		default:
			LogUtils.logW("default should never happend");
		}
	}
	 /**
     * Sets the test mode flag.
     * Used to bypass dependency with other modules while unit testing
     */
    public void setTestMode(boolean mode){
    	mJUnitTestMode = mode;
    }
}
