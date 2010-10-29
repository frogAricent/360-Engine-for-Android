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
package com.vodafone360.people.engine.groups;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.tables.GroupsTable;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.datatypes.ItemList;
import com.vodafone360.people.datatypes.PrivacySetting;
import com.vodafone360.people.datatypes.PrivacySettingList;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.GroupPrivacy;
import com.vodafone360.people.utils.LogUtils;

/***
 * Engine is responsible for handling features like adding user defined groups,
 * updating groups, deleting groups and getting groups etc
 * 
 **/
public class GroupsEngine extends BaseEngine {
    /**
     * Max number of groups to fetch from server in one request.
     */
    private static final int MAX_DOWN_PAGE_SIZE = 24;

    /**
     * Current page number being fetched.
     */
    private int mPageNo;

    /**
     * Total number of groups fetched from server.
     */
    private int mNoOfGroupsFetched;
    
    /**
     * Instance of database helper
     */
    private DatabaseHelper mDb;
    
    public static enum State{
		IDLE,
		ADDING_USER_GROUP,
		GETTING_USER_GROUP,
		DELETING_USER_GROUP,
		GETTING_GROUP_PRIVACY_SETTING,
		SETTING_GROUP_PRIVACY_SETTING
	}

    public boolean mJUnitTestMode = false ;
	/**
	 * To maintain the state of the Engine
	 */
	State mState = State.IDLE;

	/**
	 * Context
	 */
	Context mContext;

	/**
	 * True if the add user group req is pending to be serviced
	 */
	boolean mAddUserGroupReq;
	
	/**
	 * True if the delete user group req is pending to be serviced
	 */
	boolean mDeleteUserGroupReq;
	
	/**
	 * True if the get group privacy setting list req is pending to be serviced
	 */
	boolean mGetGroupPrivacySettingReq;
	
	/**
	 * True if the set group privacy setting list req is pending to be serviced
	 */
	boolean mSetGroupPrivacySettingReq;
	
	/**
	 * Mutex for thread synchronization
	 */
	private Object mMutex = new Object();
	/**
	 * Constructor
	 * 
	 * @param context
	 * @param eventCallback
	 * @param db
	 */
    
    public GroupsEngine(Context context, IEngineEventCallback eventCallback, DatabaseHelper db) {
        super(eventCallback);
        mEngineId = EngineId.GROUPS_ENGINE;
        mDb = db;
    }
    
    @Override
    public long getNextRunTime() {
        // we only run if we have a request or a response in the queue
        if (isUiRequestOutstanding() || isCommsResponseOutstanding()) {
            return 0;
        }
    	if(mAddUserGroupReq == true) {
			return 0;
		}
    	
    	if(mGetGroupPrivacySettingReq == true) {
			return 0;
		}
    	
    	if(mDeleteUserGroupReq == true) {
			return 0;
		}
    	
    	if(mSetGroupPrivacySettingReq == true) {
			return 0;
		}
		long retval = getCurrentTimeout();
		return retval; 
//        return -1;
    }

    @Override
    public void onCreate() {
        // nothing needed
    }

    @Override
    public void onDestroy() {
        // nothing needed
    }

    @Override
    protected void onRequestComplete() {
        // nothing needed
    }

    @Override
    protected void onTimeoutEvent() {
    }
    
    /**
     * Called when a server response is received, processes the response based
     * on the engine state.
     * 
     * @param resp Response data from server
     * @return null
     */
    @Override
    protected void processCommsResponse(DecodedResponse resp) {
        switch(mState){
			case IDLE:
				LogUtils.logE("IDLE SHOULD NEVER HAPPEN");
				break;
			case ADDING_USER_GROUP:
				handleAddUserGroupResponse(resp);
				break;
			case DELETING_USER_GROUP:
				handleDeleteUserGroupResponse(resp);
				break;
			case GETTING_GROUP_PRIVACY_SETTING:
				handleGetGroupPrivacySettingResponse(resp);
				break;
			case SETTING_GROUP_PRIVACY_SETTING:
				handleSetGroupPrivacySettingResponse(resp);
				break;
			case GETTING_USER_GROUP:
				handleGetGroupsResponse(resp);
				break;
			default:
				break;
		}
    }

    /**
     * Issue any outstanding UI request.
     * 
     * @param requestType Request to be issued.
     * @param dara Data associated with the request.
     */
    @Override
    protected void processUiRequest(ServiceUiRequest requestId, Object data) {
        switch (requestId) {
			case ADD_USER_GROUP:
				String groupName = (String)data;
				startAddUserGroup(groupName);
				break;
				
			case DELETE_USER_GROUP:
				Long groupId = (Long)data;
				startDeleteUserGroup(groupId);
				break;
				
            case GET_GROUPS:
                requestFirstGroupsPage();
                break;
                
        	case GET_GROUP_PRIVACY_SETTING:
    			//LogUtils.logD("GroupsEngine.processUiRequest() - Get group privacy setting");
    			startGetGroupPrivacySetting(data);
    			break;
    			
    		case SET_GROUP_PRIVACY_SETTING:
    			//LogUtils.logD("GroupsEngine.processUiRequest() - Set group privacy setting");
    			startSetGroupPrivacySetting(data);
    			break;
        }
    }

    /**
     * Run function called via EngineManager. Should have a UI, Comms response
     * or timeout event to handle.
     */
    @Override
    public void run() {
        if (isUiRequestOutstanding() && processUiQueue()) {
            return;
        }
        if (isCommsResponseOutstanding() && processCommsInQueue()) {
            return;
        }
        if (processTimeout()) {
            return;
        }
    }

    /**
     * Adds a request to get groups from the backend that are associated with
     * the server contacts.
     * 
     * @param void
     * @return void
     */
    public void addUiGetGroupsRequest() {
        LogUtils.logD("GroupsEngine.addUiGetGroupsRequest()");
        addUiRequestToQueue(ServiceUiRequest.GET_GROUPS, null);
    }
    
    /**
     * Requests the first group page.
     */
    private void requestFirstGroupsPage() {
        mPageNo = 0;
        mNoOfGroupsFetched = 0;
        newState(State.GETTING_USER_GROUP);
        requestNextGroupsPage();
        newState(GroupsEngine.State.GETTING_USER_GROUP);
    }
    
    /**
     * Requests the next page of groups from the server.
     */
    private void requestNextGroupsPage() {
    	
        if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
            completeUiRequest(ServiceStatus.ERROR_COMMS);
            return;
        }
        
        int reqId = GroupPrivacy.getGroups(this, mPageNo, MAX_DOWN_PAGE_SIZE);
        setReqId(reqId);
    }
    
    /**
	 * Adds UI request to add user defined group
	 * 
	 * @param groupName
	 * @return void
	 */

	public void addUiAddUserDefinedGroup(String groupName) {
		LogUtils.logD("GroupsEngine.addUiAddUserDefinedGroup()");
		addUiRequestToQueue(ServiceUiRequest.ADD_USER_GROUP, groupName);
		mAddUserGroupReq = true;
	}

	/**
	 * Adds UI request to delete user defined group
	 * 
	 * @param groupName
	 * @return void
	 */
	public void addUiDeleteUserDefinedGroup(String groupName) {
		LogUtils.logD("GroupsEngine.addUiDeleteUserDefinedGroup()");
		List<GroupItem> groupList = new ArrayList<GroupItem>();
		Long groupId = null;
		groupList = fetchGroups(mDb, mContext);

		if(groupList.size() != 0){
			//LogUtils.logI("GroupsEngine.addUiDeleteUserDefinedGroup - Fetched Group list from database");
			for(GroupItem gr : groupList){
				if(gr.mName.equals(groupName)){
					LogUtils.logD("Deleting group "+gr.mName);
					//LogUtils.logI("Group Name: " + gr.mName + " GroupID: " + gr.mId);
					groupId = gr.mId;
					break;
				}
			}
		}else{
			LogUtils.logE("GroupsEngine.addUiDeleteUserDefinedGroup - Group list from database is empty");
		}
		addUiRequestToQueue(ServiceUiRequest.DELETE_USER_GROUP, groupId);
		mDeleteUserGroupReq = true;
	}
	
	/**
	 * Adds UI request to get group privacy settings
	 * 
	 * @param groupName
	 * @return void
	 */
	
	public void addUiGetGroupPrivacySetting(String groupName) {
		LogUtils.logD("GroupsEngine.addUiGetGroupPrivacySetting()");
		List<GroupItem> groupList = new ArrayList<GroupItem>();
		GroupItem group = new GroupItem();
		groupList = fetchGroups(mDb, mContext);

		if(groupList.size() != 0){
			for(GroupItem gr : groupList){
				if(gr.mName.equals(groupName)){
					group.mName = gr.mName;
					group.mId = gr.mId;
				}
			}

		}else{
			LogUtils.logE("GroupsEngine.addUiGetGroupPrivacySetting - Group list from database is empty");
		}
		LogUtils.logI("Getting privacy settings for "+group.mName);
		addUiRequestToQueue(ServiceUiRequest.GET_GROUP_PRIVACY_SETTING, group.mId);
		mGetGroupPrivacySettingReq = true;
	}
	
	

	/**
	 * Adds UI request to set group privacy settings
	 * 
	 * @param groupName
	 * @param contentType
	 * @param status
	 * @return void
	 */
	public void addUiSetGroupPrivacySetting(String groupName, int contentType, int status) {
		LogUtils.logD("GroupsEngine.addUiSetGroupPrivacySetting()");
		List<GroupItem> groupList = new ArrayList<GroupItem>();
		GroupItem group = new GroupItem();
		groupList = fetchGroups(mDb, mContext);

		if(groupList.size() != 0){
			for(GroupItem gr : groupList){
				if(gr.mName.equals(groupName)){
					group.mName = gr.mName;
					group.mId = gr.mId;
				}
			}

		}else{
			LogUtils.logE("GroupsEngine.addUiSetGroupPrivacySetting - Group list from database is empty");
		}
		LogUtils.logI("Setting privacy settings for "+group.mName);
		Bundle data = new Bundle();
		data.putLong("groupid", group.mId);
		data.putInt("contenttype", contentType);
		data.putInt("status", status);
		addUiRequestToQueue(ServiceUiRequest.SET_GROUP_PRIVACY_SETTING, data);
		mSetGroupPrivacySettingReq = true;
	}

	/**
	 * Sends request to server to add a user defined group
	 * 
	 * @param groupName
	 * @return void
	 */
	private void startAddUserGroup(String groupName) {
		mAddUserGroupReq = false;
		LogUtils.logD("GroupsEngine.startAddUserGroup()");
		if (!checkConnectivity()) {
			return;
		}
		newState(State.ADDING_USER_GROUP);
		ArrayList<GroupItem> grpList = new ArrayList<GroupItem>();
		GroupItem grpItem = new GroupItem();
		grpItem.mName = groupName;
		grpList.add(grpItem);
		if (!setReqId(GroupPrivacy.addUserGroup(this, grpList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}
	
	
	/**
	 * Sends request to server to delete a user defined group
	 * 
	 * @param groupId
	 * @return void
	 */
	private void startDeleteUserGroup(Long groupId) {
		mDeleteUserGroupReq = false;
		LogUtils.logD("GroupsEngine.startDeleteUserGroup()");
		if (!checkConnectivity()) {
			return;
		}
		if(groupId == null || groupId == -1){
			LogUtils.logE("GroupsEngine.startDeleteUserGroup - GroupId canot be null");
			completeUiRequest(ServiceStatus.ERROR_NOT_FOUND);
		}
		newState(State.DELETING_USER_GROUP);
		ArrayList<Long> grpList = new ArrayList<Long>();
		grpList.add(groupId);
		if (!setReqId(GroupPrivacy.deleteUserGroup(this, grpList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Sends request to get group privacy settings
	 * 
	 * @param data
	 * @return void
	 */
	private void startGetGroupPrivacySetting(Object data){
		mGetGroupPrivacySettingReq = false;
		LogUtils.logD("GroupsEngine.startGetGroupPrivacySetting()");
		Long groupId = (Long)data;
		if (!checkConnectivity()) {
			return;
		}
		
		if(groupId == null || groupId == -1){
			LogUtils.logE("GroupsEngine.startGetGroupPrivacySetting - GroupId canot be null");
			completeUiRequest(ServiceStatus.ERROR_NOT_FOUND);
		}
		newState(State.GETTING_GROUP_PRIVACY_SETTING);
		if (!setReqId(GroupPrivacy.getGroupPrivacySetting(this, groupId))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}
	
	/**
	 * Sends request to set group privacy settings
	 * 
	 * @param data
	 * @return void
	 */
	private void startSetGroupPrivacySetting(Object data){
		mSetGroupPrivacySettingReq = false;
		LogUtils.logD("GroupsEngine.startSetGroupPrivacySetting()");
		Bundle bundle = (Bundle)data;
		Long groupId = (Long)bundle.get("groupid");
		int contentType = (Integer)bundle.get("contenttype");
		int status = (Integer)bundle.get("status");
		if (!checkConnectivity()) {
			return;
		}
		
		if(groupId == null || groupId == -1){
			LogUtils.logE("GroupsEngine.startSetGroupPrivacySetting - GroupId canot be null");
			completeUiRequest(ServiceStatus.ERROR_NOT_FOUND);
		}
		
		newState(State.SETTING_GROUP_PRIVACY_SETTING);
		List<PrivacySetting> psList = new ArrayList<PrivacySetting>();
		PrivacySetting ps = new PrivacySetting();
		ps.mGroupId = groupId;
		ps.mContentType = contentType;
		ps.mState = status;
		psList.add(ps);
		if (!setReqId(GroupPrivacy.setGroupPrivacySetting(this, psList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Handles response received from server to add a User Group
	 * 
	 * @param resp
	 * @return void
	 */
	private void handleAddUserGroupResponse(DecodedResponse resp) {
		LogUtils.logD("GroupsEngine.handleAddUserGroupResponse");
//		ServiceStatus errorStatus = genericHandleResponseType("Group", resp.mDataTypes);
		ServiceStatus errorStatus = getResponseStatus(BaseDataType.GROUP_DATATYPE, resp.mDataTypes);

		if(errorStatus == ServiceStatus.SUCCESS){
			LogUtils.logI("GroupsEngine.handleAddUserGroupResponse() - User Group Added.");
			//TODO:Handling of response from the server to be implemented
		}else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER){
			LogUtils.logE("GroupsEngine.handleAddUserGroupResponse() - Bad Server Parameter");
			
		}else{
			LogUtils.logE("GroupsEngine.handleAddUserGroupResponse() - Failure");
			
		}
		newState(State.IDLE);
		completeUiRequest(errorStatus,null);
	}
	
	
	/**
	 * Handles response received from server to delete a User Group
	 * 
	 * @param resp
	 * @return void
	 */
	private void handleDeleteUserGroupResponse(DecodedResponse resp) {
		LogUtils.logD("GroupsEngine.handleDeleteUserGroupResponse");
//		ServiceStatus errorStatus = genericHandleResponseType("ListOfLong", resp.mDataTypes);
		ServiceStatus errorStatus = getResponseStatus(BaseDataType.LONG_DATATYPE, resp.mDataTypes);

		if(errorStatus == ServiceStatus.SUCCESS){
			LogUtils.logI("Group Deleted.");
			//TODO:Handling of response from the server to be implemented
		}else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER){
			LogUtils.logE("handleDeleteUserGroupResponse() - Bad Server Parameter");
		}else{
			LogUtils.logE("handleDeleteUserGroupResponse() - Failure");
		}
		newState(State.IDLE);
		completeUiRequest(errorStatus,null);
	}

	/**
	 * Handles response received from server to get group privacy setting
	 * 
	 * @param resp
	 * @return void
	 */
	private void handleGetGroupPrivacySettingResponse(DecodedResponse resp) {
		LogUtils.logD("GroupsEngine.handleGetGroupPrivacySettingResponse");
//		ServiceStatus errorStatus = genericHandleResponseType("PrivacySettingList", resp.mDataTypes);
		ServiceStatus errorStatus = getResponseStatus(BaseDataType.PRIVACY_SETTING_LIST_DATATYPE, resp.mDataTypes);

		if(errorStatus == ServiceStatus.SUCCESS){
			LogUtils.logI("Received GroupPrivacy settings");
			//TODO:Handling of response from the server to be implemented
			displayGroupPrivacySettings(resp.mDataTypes);
		}else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER){
			LogUtils.logE("handleGetGroupPrivacySettingResponse() - Bad Server Parameter");
		}else{
			LogUtils.logE("handleGetGroupPrivacySettingResponse() - Failure");
		}
		newState(State.IDLE);
		completeUiRequest(errorStatus,null);
	}
	
	
	/**
	 * Handles response received from server to set group privacy setting
	 * 
	 * @param resp
	 * @return void
	 */
	private void handleSetGroupPrivacySettingResponse(DecodedResponse resp) {
		LogUtils.logD("GroupsEngine.handleSetGroupPrivacySettingResponse");
//		ServiceStatus errorStatus = genericHandleResponseType("PrivacySetting", resp.mDataTypes);
		ServiceStatus errorStatus = getResponseStatus(BaseDataType.PRIVACY_SETTING_LIST_DATATYPE, resp.mDataTypes);

		if(errorStatus == ServiceStatus.SUCCESS){
			LogUtils.logI("Privacy Settings set for the group");
			//TODO:Handling of response from the server to be implemented
		}else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER){
			LogUtils.logE("handleSetGroupPrivacySettingResponse() - Bad Server Parameter");
		}else{
			LogUtils.logE("handleSetGroupPrivacySettingResponse() - Failure");
		}
		newState(State.IDLE);
		completeUiRequest(errorStatus,null);
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
	 * Change current IdentityEngine state.
	 * 
	 * @param newState new state.
	 * @return void
	 */
	private void newState(State newState) {
		State oldState = mState;
		synchronized (mMutex) {
			if (newState == mState) {
				return;
			}
			mState = newState;
		}
		LogUtils.logV("GroupsEngine.newState: " + oldState + " -> " + mState);
	}
	
	/**
	 * Displays the privacy settings received 
	 * @param privacySettings
	 */
	
	private void displayGroupPrivacySettings(List<BaseDataType> privacySettings){
		//LogUtils.logI("Received List size is "+privacySettings.size());
		PrivacySettingList psl = (PrivacySettingList)privacySettings.get(0);
		for(PrivacySetting obj : psl.mItemList){
			LogUtils.logI("GroupId:" + obj.mGroupId + " ContentType:"+ obj.mContentType+" State:"+obj.mState);
		}
	}
	
	/**
	 * Handles the list of groups received from the server
	 * 
	 * @param resp
	 * @return void
	 */
	
	private void handleGetGroupsResponse(DecodedResponse resp){
    	ServiceStatus status = 
            BaseEngine.getResponseStatus(BaseDataType.ITEM_LIST_DATA_TYPE, resp.mDataTypes);
        if (status == ServiceStatus.SUCCESS) {
            final List<GroupItem> tempGroupList = new ArrayList<GroupItem>();
            for (int i = 0; i < resp.mDataTypes.size(); i++) {
                ItemList itemList = (ItemList)resp.mDataTypes.get(i);
                if (itemList.mType != ItemList.Type.group_privacy) {
                    completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
                    return;
                }
                for (int j = 0; j < itemList.mItemList.size(); j++) {
                    tempGroupList.add((GroupItem)itemList.mItemList.get(j));
                }
            }
            LogUtils.logI("GroupsEngine.handleGetGroupsResponse() - No of groups "
                    + tempGroupList.size());
            if (mPageNo == 0) {
                mDb.deleteAllGroups(); // clear old groups if we request the first groups page
            }
            status = GroupsTable.addGroupList(tempGroupList, mDb.getWritableDatabase());
            if (ServiceStatus.SUCCESS != status) {
                completeUiRequest(status);
                return;
            }
            mNoOfGroupsFetched += tempGroupList.size();
            if (tempGroupList.size() < MAX_DOWN_PAGE_SIZE) {
                completeUiRequest(ServiceStatus.SUCCESS);
                return;
            }
            mPageNo++;
            requestNextGroupsPage();
            return;
        }
        completeUiRequest(status);
    }
	
	  /**
     * Fetches the list of groups from the database.
     * 
     * @param databaseHelper - Handle to the database.
     * @param context - Android context
     * @return List of groups.
     */
    public static ArrayList<GroupItem> fetchGroups(DatabaseHelper databaseHelper, Context context) {
        final ArrayList<GroupItem> mGroups = new ArrayList<GroupItem>();
        ServiceStatus mServiceStatusError = GroupsTable.fetchGroupList(mGroups, databaseHelper
                .getReadableDatabase());
        if (mServiceStatusError == ServiceStatus.SUCCESS) {
            LogUtils.logI("GroupsEngine.fetchGroups() [" + mGroups.size() + "]");

        } else {
            LogUtils.logI("GroupsEngine.fetchGroups() mServiceStatusError ["
                    + mServiceStatusError.toString() + "]");
//            showError(context, mServiceStatusError);
        }
        return mGroups;
    }

    /**
     * Sets the test mode flag.
     * Used to bypass dependency with other modules while unit testing
     */
    public void setTestMode(boolean mode){
    	mJUnitTestMode = mode;
    }
}
