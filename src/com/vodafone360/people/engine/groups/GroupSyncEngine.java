package com.vodafone360.people.engine.groups;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.vodafone360.people.ApplicationCache;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.DatabaseHelper.DatabaseChangeType;
import com.vodafone360.people.database.tables.ContactsTable;
import com.vodafone360.people.database.tables.GroupsChangeLogTable;
import com.vodafone360.people.database.tables.GroupsTable;
import com.vodafone360.people.database.tables.GroupsTable.Field;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Group;
import com.vodafone360.people.datatypes.GroupIdListResponse;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.datatypes.ItemList;
import com.vodafone360.people.datatypes.ListOfLong;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.agent.UiAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.GroupPrivacy;
import com.vodafone360.people.utils.LogUtils;

public class GroupSyncEngine extends BaseEngine{

	/**
	 * States for Groups Sync Engine
	 */
	public static enum State{
		IDLE,
		GETTING_USER_GROUP,
		ADDING_USER_GROUP,
		DELETING_USER_GROUP
	}

	/**
	 * Groups sync timeout
	 */
	private static final Long GROUPS_SYNC_TIMEOUT_MS = 5000L;

	/**
	 * Mutex for thread synchronization
	 */
	private Object mMutex = new Object();

	private UiAgent mUiAgent = mEventCallback.getUiAgent();
	private ApplicationCache mCache = mEventCallback.getApplicationCache();
	/**
	 * The context of the People service
	 */
	private Context mContext = null;

	/**
	 * Current page number being fetched.
	 */
	private int mPageNo;

	/**
	 * Total number of groups fetched from server.
	 */
	private int mNoOfGroupsFetched;

	/**
	 * Current state of the groups sync engine 
	 */
	private State mState = State.IDLE;

	/**
	 * Database changed flag. Will be set to true if at any stage of the contact
	 * sync the NowPlus database is changed.
	 */
	private boolean mDatabaseChanged;

	/**
	 * True if the groups sync must be started soon
	 */
	private boolean mGroupsSyncRequired = false;

	/**
	 * True if the first group sync is complete
	 */
	private boolean mFirstGroupSyncComplete = false;

	/**
	 * List of added user groups that have not yet been synced with the server
	 */
	private ArrayList<GroupItem> mAddedGroups = new ArrayList<GroupItem>();

	/**
	 * List of deleted user groups that have not yet been synced with the server
	 */
	private ArrayList<Long> mDeletedGroups = new ArrayList<Long>();

	/**
	 * Timeout for syncing the groups
	 */
	//private Long mGroupsSyncTimeout = null;

	/**
	 * Backup of the previous active request before processing the new one.
	 */
	private ServiceUiRequest mActiveUiRequestBackup = null;

	/**
	 * DatabaseHelper object used for accessing NowPlus database.
	 */
	private DatabaseHelper mDb;

	private Handler mDbChangeHandler = new Handler() {
		/**
		 * Processes a database change event
		 */
		@Override
		public void handleMessage(Message msg) {
			processServiceMessage(msg);
		}
	};

	/***
	 * GroupSyncEngine Constructor
	 * @param eventCallback
	 * @param context
	 * @param db 
	 */

	public GroupSyncEngine(Context context, IEngineEventCallback eventCallback ,
			DatabaseHelper db) {
		super(eventCallback);
		mDb = db;
		mEngineId = EngineId.GROUPS_ENGINE;
		mContext = context;
		mDb.addEventCallback(mDbChangeHandler);

	}

	@Override
	public long getNextRunTime() {
		LogUtils.logD("GroupsSyncEngine.getNextRunTime()");
		if (isCommsResponseOutstanding()) {
			return 0;
		}
		if (isUiRequestOutstanding() && mActiveUiRequest == null) {
			return 0;
		}
		if(mGroupsSyncRequired){
			return 0;
		}
		/*if (mGroupsSyncTimeout != null) {
			if (mGroupsSyncTimeout < System.currentTimeMillis()) {
				return 0;
			}
		}*/
		/*if(mDatabaseChanged){
			return 0;
		}*/
		return getCurrentTimeout();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		mDb.removeEventCallback(mDbChangeHandler);

	}

	@Override
	protected void onRequestComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onTimeoutEvent() {
		startSyncIfRequired();
		//setTimeoutIfRequired();

	}



	@Override
	protected void processCommsResponse(DecodedResponse resp) {
		LogUtils.logD("Response = " + resp);
		LogUtils.logD("GroupSyncEngine.processCommsResponse - Response Datatype:"+resp.mDataTypes.get(0).getType());
		switch(mState){
		case ADDING_USER_GROUP:
			LogUtils.logD("ADDING_USER_GROUP");
			handleAddUserGroupResponse(resp);
			break;

		case DELETING_USER_GROUP:
			LogUtils.logD("DELETING_USER_GROUP");
			handleDeleteUserGroupResponse(resp);
			break;

		case GETTING_USER_GROUP:
			handleGetGroupsResponse(resp);
			break;
		}

	}

	@Override
	protected void processUiRequest(ServiceUiRequest requestId, Object data) {
		LogUtils.logD("GroupSyncEngine.processUiRequest");
		switch(requestId){
		case ADD_USER_GROUP:
			startGroupSync();
			break;

		case GET_GROUPS:
			requestFirstGroupsPage();
			break;

		case DELETE_USER_GROUP:
			startGroupSync();
			break;

		case ADD_OR_DELETE_USER_GROUP:
			LogUtils.logD("GroupSyncEngine.processUiRequest - add or delete user group");
			startGroupSync();
			break;
		}

	}

	@Override
	public void run() {
		LogUtils.logD("GroupsSyncEngine.run()");
		if (processTimeout()) {
			return;
		}

		if (isUiRequestOutstanding()) {
			mActiveUiRequestBackup = mActiveUiRequest;
			if (processUiQueue()) {
				return;
			}
		}

		if (isCommsResponseOutstanding() && processCommsInQueue()) {
			return;
		}
		if(mGroupsSyncRequired){
			onDbChanged();
		}
		/*if(mGroupsSyncTimeout < System.currentTimeMillis()){
			onDbChanged();
		}*/



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
		//newState(State.GETTING_USER_GROUP);
	}

	/**
	 * Requests the next page of groups from the server.
	 */
	private void requestNextGroupsPage() {

		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS);
			return;
		}

		int reqId = GroupPrivacy.getGroups(this, mPageNo, 24);
		setReqId(reqId);
	}


	/**
	 * Starts syncing of groups
	 */

	private void startGroupSync() {
		LogUtils.logD("GroupSyncEngine.startGroupSync: Engine State - "+mState.toString());
		//Sync has started. So make the sync variable false
		mGroupsSyncRequired = false;
		switch(mState){
		case IDLE:
			LogUtils.logD("GroupSyncEngine.startGroupSync - IDLE");
			newState(State.ADDING_USER_GROUP);
			if(mAddedGroups.size() != 0){
				addGroups();
			}else{
				LogUtils.logD("No new groups to be added. Verify for groups to be deleted.");
				startGroupSync();
			}
			break;

		case ADDING_USER_GROUP:
			LogUtils.logD("GroupSyncEngine.startGroupSync - ADDING_USER_GROUP");
			newState(State.DELETING_USER_GROUP);
			if(mDeletedGroups.size() != 0){
				deleteGroups();
			}else{
				LogUtils.logD("No new groups to be deleted. Completing UI request");
				GroupsChangeLogTable.deleteAllGroups(mDb.getWritableDatabase(), mDb.getReadableDatabase());
				newState(State.IDLE);
				completeUiRequest(ServiceStatus.SUCCESS);
			}
			break;

		case DELETING_USER_GROUP:
			LogUtils.logD("GroupSyncEngine.startGroupSync - DELETING_USER_GROUP");
			newState(State.IDLE);
			GroupsChangeLogTable.deleteAllGroups(mDb.getWritableDatabase(), mDb.getReadableDatabase());
			completeUiRequest(ServiceStatus.SUCCESS);
			//addUiGetGroupsRequest();
			break;

		default:
			LogUtils.logE("Default state. Ideally should never happen.");
		}


	}



	/**
	 * Called when the engine receives a DB changed event, in case the event is of USER_GROUP type 
	 */

	private void onDbChanged() {
		LogUtils.logD("GroupsSYncEngine.onDbChanged");
		ServiceStatus status;
		mAddedGroups.clear();
		mDeletedGroups.clear();
		status = GroupsChangeLogTable.fetchUploadGroupList(mAddedGroups, mDb.getReadableDatabase());
		if(status != ServiceStatus.SUCCESS){
			LogUtils.logE("Could not fetch upload group list from table.Groups not uploaded");
			mAddedGroups.clear();
		}
		status = GroupsChangeLogTable.fetchDeleteGroupList(mDeletedGroups, mDb.getReadableDatabase());
		if(status != ServiceStatus.SUCCESS){
			LogUtils.logE("Could not fetch deleted group list from table.Groups not deleted");
			mDeletedGroups.clear();
		}
		//addUiRequestToQueue(ServiceUiRequest.ADD_USER_GROUP,null);
		//addUiRequestToQueue(ServiceUiRequest.DELETE_USER_GROUP,null);
		if((mAddedGroups.size() == 0) && (mDeletedGroups.size() == 0)){
			LogUtils.logE("No request sent to server as no data was fetched from DB");
			mGroupsSyncRequired = false;
		}else{
			LogUtils.logD("GroupsSYncEngine.onDbChanged() - Adding Ui request to the engine queue");
			addUiRequestToQueue(ServiceUiRequest.ADD_OR_DELETE_USER_GROUP,null);
		}


	}

	/**
	 * Sends a request to server to add a list of user defined groups
	 * 
	 */

	private void addGroups() {
		LogUtils.logD("GroupsEngine.addGroups()");
		if (!checkConnectivity()) {
			return;
		}
		LogUtils.logD("Adding groups:"+mAddedGroups.get(0).toString());
		if (!setReqId(GroupPrivacy.addUserGroup(this, mAddedGroups))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}



	}

	/**
	 * Sends a request to server to delete a list of user defined groups
	 * 
	 */

	private void deleteGroups() {
		LogUtils.logD("GroupsEngine.deleteGroups()");
		if (!checkConnectivity()) {
			return;
		}
		LogUtils.logD(mDeletedGroups.size()+" groups are being deleted");
		LogUtils.logD("Deleting group Id's:"+mDeletedGroups.toString());
		if (!setReqId(GroupPrivacy.deleteUserGroup(this, mDeletedGroups))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}



	}

	/**
	 * Handles the list of groups received from the server
	 * 
	 * @param resp
	 * @return void
	 */

	private void handleGetGroupsResponse(DecodedResponse resp){
		LogUtils.logD("GroupSyncEngine.handleGetGroupsResponse");
		newState(State.IDLE);
		ServiceStatus status = 
			BaseEngine.getResponseStatus(BaseDataType.ITEM_LIST_DATA_TYPE, resp.mDataTypes);
		if (status == ServiceStatus.SUCCESS) {
			List<GroupItem> tempGroupList = new ArrayList<GroupItem>();
			List<GroupItem> newGroupList = new ArrayList<GroupItem>();
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
			//Modify the grouplist to suit UI needs.
			//All, Smart groups, pre defined user groups, user defined groups

			if (!mFirstGroupSyncComplete) {
				reOrderGroupList(tempGroupList, newGroupList);
			}
			status = GroupsTable.addGroupList(newGroupList, mDb.getWritableDatabase());
			if (ServiceStatus.SUCCESS != status) {
				completeUiRequest(status);
				return;
			}
			mNoOfGroupsFetched += tempGroupList.size();
			if (tempGroupList.size() < 24) {
				completeUiRequest(ServiceStatus.SUCCESS);
				return;
			}
			mPageNo++;
			requestNextGroupsPage();
			return;
		}
		mFirstGroupSyncComplete = true;
		completeUiRequest(status);

		/**********************/
		//Verify the tables of the group
		ArrayList<GroupItem> grpList = new ArrayList<GroupItem>();
		LogUtils.logD("GroupSyncEngine.handleGetGroupsResponse");
		GroupsTable.fetchGroupListInOrder(grpList, mDb.getReadableDatabase());
		ArrayList<GroupItem> newlyAddedGroups = new ArrayList<GroupItem>();
		Cursor c = GroupsChangeLogTable.fetchGroupListCursor( mDb.getReadableDatabase());
		if(c != null){
			while(c.moveToNext()){
				String contactList = c.getString(c.getColumnIndex(GroupsChangeLogTable.Field.CONTACTLIST.toString()));
				String namesList[] = contactList.split(";");
				Long contactIdList[] = new Long[namesList.length];
				for(int i = 0; i < namesList.length; i++){
					contactIdList[i] = Long.valueOf(namesList[i]);
				}

			}
			//addContactGroupRelations(newlyAddedGroups);
		}else{
			LogUtils.logD("GroupSyncEngine.handleGetGroupsRequest - No new relations to be added.");
		}
	}

	/**
	 * Handle response received from server to add a User Group
	 * @param resp
	 */
	private void handleAddUserGroupResponse(final DecodedResponse resp) {
		//mActiveGroupAddRequest = false;
		LogUtils.logD("GroupSyncEngine.handleAddUserGroupResponse");
		List<GroupItem> newlyAddedGroupList = new ArrayList<GroupItem>();
		List<GroupItem> editedAddedGroupList = new ArrayList<GroupItem>();
		ServiceStatus errorStatus = BaseEngine.getResponseStatus(BaseDataType.ITEM_LIST_DATA_TYPE,
				resp.mDataTypes);
		if(errorStatus == ServiceStatus.SUCCESS){
			LogUtils.logD("GroupSyncEngine.handleAddUserGroupResponse() - User Groups Added.");
			//Store data in DB as required by UI
			List<GroupIdListResponse> tempGroupList = new ArrayList<GroupIdListResponse>();
			LogUtils.logD("Response datatype list size is "+resp.mDataTypes.size());
			for (int i = 0; i < resp.mDataTypes.size(); i++) {
				ItemList itemList = (ItemList)resp.mDataTypes.get(i);
				if (itemList.mType != ItemList.Type.group_id_list) {
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}else{
					LogUtils.logD("Group id's are found.");
				}
				for (int j = 0; j < itemList.mItemList.size(); j++) {
					tempGroupList.add((GroupIdListResponse)itemList.mItemList.get(j));
				}
			}
			LogUtils.logI("GroupsEngine.handleGetGroupsResponse() - No of groups "+tempGroupList.size());
			ArrayList<GroupItem> dbGrouplist = new ArrayList<GroupItem>();
			GroupsTable.fetchGroupList(dbGrouplist, mDb.getReadableDatabase());
			for(int it = 0 ; it < mAddedGroups.size(); it++){
				GroupItem groupChange = mAddedGroups.get(it);
				Long grpId = tempGroupList.get(it).mItem;
				String status = tempGroupList.get(it).mStat;
				if (!isGroupAlreadyPresent(grpId)) {
					//New group was added
					LogUtils.logD("GroupSyncEngine.handleAddUserGroupsResponse - New group was added");
					if (status.equals("ok")) {
						groupChange.mGroupType = 0;
						groupChange.mId = tempGroupList.get(it).mItem;
//						groupChange.mDisplayOrder = (GroupsTable
//								.getGroupCursor(mDb.getReadableDatabase())
//								.getCount());
						newlyAddedGroupList.add(groupChange);
					} else {
						LogUtils.logE(groupChange.mName
								+ " groups was not added.");
					}
				}else{
					//Group was edited
					LogUtils.logD("GroupSyncEngine.handleAddUserGroupsResponse - Existing group was edited");
					if (status.equals("ok")) {
						for(GroupItem grp: dbGrouplist){
							if(grp.mId.equals(tempGroupList.get(it).mItem)){
								groupChange.mDisplayOrder = grp.mDisplayOrder;
								groupChange.mGroupType = grp.mGroupType;
								groupChange.mUserId = grp.mUserId;
								groupChange.mId = grp.mId;
								groupChange.mColor = grp.mColor;
								break;
							}
						}
						editedAddedGroupList.add(groupChange);
					} else {
						LogUtils.logE(groupChange.mName
								+ " groups was not added.");
					}
				}
			}
			
			//Update db if new groups were added
			if (newlyAddedGroupList.size() != 0) {
				ServiceStatus status = GroupsTable.updateGroupIds(
						newlyAddedGroupList, mDb.getWritableDatabase());
				if (ServiceStatus.SUCCESS != status) {
					LogUtils
							.logE("GroupsSyncEngine.handleAddUserGroupResponse - Could not add groups to Groups Table");

				} else {
					LogUtils
							.logE("GroupsSyncEngine.handleAddUserGroupResponse - Groups added to Groups Table");
				}
				addContactsToNewGroups(newlyAddedGroupList);
				for(GroupItem grp : newlyAddedGroupList){
					GroupsChangeLogTable.deleteGroup(grp, mDb.getWritableDatabase());
				}
			}
			
			//Update db if groups were edited with the edited information
			if (editedAddedGroupList.size() != 0) {
				ServiceStatus status = GroupsTable.updateGroupNames(
						editedAddedGroupList, mDb.getWritableDatabase());
				if (ServiceStatus.SUCCESS != status) {
					LogUtils
							.logE("GroupsSyncEngine.handleAddUserGroupResponse - Could not update groups in Groups Table");

				} else {
					LogUtils
							.logE("GroupsSyncEngine.handleAddUserGroupResponse - Groups updtaed in Groups Table");
				}
				for(GroupItem grp : editedAddedGroupList){
					GroupsChangeLogTable.deleteGroup(grp, mDb.getWritableDatabase());
				}
			}

		}else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER){
			LogUtils.logE("GroupsEngine.handleAddUserGroupResponse() - Bad Server Parameter");
		}else{
			LogUtils.logE("GroupsEngine.handleAddUserGroupResponse() - Failure");
			addGroups();
		}

		/**********************/
		//Verify the tablles of the group
		ArrayList<GroupItem> grpList = new ArrayList<GroupItem>();
		LogUtils.logD("GroupSyncEngine.handleAddUserGroupResponse");
		GroupsTable.fetchGroupListInOrder(grpList, mDb.getReadableDatabase());
		//addContactGroupRelations(newlyAddedGroupList);
		//Finished adding groups. Now start with deleting groups.
		startGroupSync();

	}

	/**
	 * Handle response received from server to delete a User Group
	 * @param resp
	 */
	private void handleDeleteUserGroupResponse(DecodedResponse resp) {
		ServiceStatus errorStatus = BaseEngine.getResponseStatus(BaseDataType.UNKNOWN_DATA_TYPE,
				resp.mDataTypes);
		if(errorStatus == ServiceStatus.SUCCESS){
			LogUtils.logI("GroupsEngine.handleDeleteUserGroupResponse() - Received Success. Group Deleted.");
			List<GroupIdListResponse> tempGroupList = new ArrayList<GroupIdListResponse>();
			for (int i = 0; i < resp.mDataTypes.size(); i++) {/*
				ListOfLong itemList = (ListOfLong)resp.mDataTypes.get(i);
				LogUtils.logD("Group id's are found.");
				for (int j = 0; j < itemList.mListSize; j++) {
					
					LogUtils.logD("Group Id received:"+itemList.mLongList.get(j));
					//LogUtils.logD("Status received:"+tempGroupList.get(j).mStat);
				}
			*/}

		}else if (errorStatus == ServiceStatus.ERROR_BAD_SERVER_PARAMETER){
			LogUtils.logE("GroupsEngine.handleDeleteUserGroupResponse() - Bad Server Parameter");
		}else{
			LogUtils.logE("GroupsEngine.handleDeleteUserGroupResponse() - Failure");
		}

		GroupsChangeLogTable.deleteAllGroups(mDb.getWritableDatabase(), mDb.getReadableDatabase());
		newState(State.IDLE);
		completeUiRequest(errorStatus,null);

		/**********************/
		//Verify the tablles of the group
		ArrayList<GroupItem> grpList = new ArrayList<GroupItem>();
		GroupsTable.fetchGroupList(grpList, mDb.getReadableDatabase());
		for(GroupItem grp: grpList){
			LogUtils.logD("*********Group:"+grp.mName);
		}
		newState(State.IDLE);
		mGroupsSyncRequired = false;
	}

	/**
	 * Called when a database change event is received from the DatabaseHelper.
	 * Only internal database change events are processed, external change
	 * events are generated by the contact sync engine.
	 * 
	 * @param msg The message indicating the type of event
	 */
	private void processServiceMessage(Message message) {
		LogUtils.logD("GroupsSyncEngine.processDbMessage()");
		final ServiceUiRequest event = ServiceUiRequest.getUiEvent(message.what);
		LogUtils.logD("Database change type:"+event.toString());
		// final DatabaseHelper.DatabaseChangeType changeType = 
		switch (event) {
		case DATABASE_CHANGED_EVENT:
			LogUtils.logD("GroupsSyncEngine - Data base has changed");
			if (message.arg1 == DatabaseHelper.DatabaseChangeType.USER_GROUP.ordinal()) {
				LogUtils.logV("GroupSyncEngine.processDbMessage - Groups have changed");
				startSyncTimer();
			} 
			break;

		default:
			// Do nothing.
			break;
		}
	}


	/**
	 * Starts the timer for syncing the groups added/deleted
	 */
	private void startSyncTimer() {
		LogUtils.logD("GroupsSyncEngine.startSyncTimer()");
		/*if (!Settings.ENABLE_GROUPS_SYNC) {
            return;
        }*/
		synchronized (this) {
			if (mCurrentTimeout == null) {
				LogUtils.logI("GroupsSyncEngine - will sync groups with server shortly...");
			}
			mCurrentTimeout = System.currentTimeMillis() + GROUPS_SYNC_TIMEOUT_MS;
			//mGroupsSyncTimeout = System.currentTimeMillis();
			mEventCallback.kickWorkerThread();

		}

	}


	/**
	 * Based on current timeout values schedules a new sync if required.
	 */
	private void startSyncIfRequired() {
		LogUtils.logD("GroupsSyncEngine.startSyncIfRequired");
		/*if (mFirstTimeSyncStarted && !mFirstTimeSyncComplete) {
            mFullSyncRequired = true;
            mFullSyncRetryCount = 0;
        }*/

		LogUtils.logD("Group Sync Required = TRUE");
		mGroupsSyncRequired = true;
		mEventCallback.kickWorkerThread();
		mCurrentTimeout = null;
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
		LogUtils.logV("GroupsEngine.newState: " + oldState + " -> " + mState);
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
	 * Reorder the group list to display the groups in the following order
	 * All, Smart Groups, Pre defined User Groups, User defined groups
	 * 
	 *  @param tempGroupList - list received from the server
	 *  @param newGroupList - new re ordered list to be populated
	 */
	private void reOrderGroupList(List<GroupItem> tempGroupList, List<GroupItem> newGroupList){
		LogUtils.logD("GroupsTable.reOrderGroupList");
		newGroupList.clear();
		Integer order = 1;
		for(GroupItem grp:tempGroupList){
			if((grp.mGroupType!= null)&&(grp.mGroupType.equals(3))){
				grp.mDisplayOrder = order;
				order++;
				newGroupList.add(grp);
				//tempGroupList.remove(grp);
			}
		}
		//Enter pre defined user groups
		for(GroupItem grp:tempGroupList){
			if((grp.mGroupType!= null)&&(grp.mGroupType.equals(2))){
				grp.mDisplayOrder = order;
				order++;
				newGroupList.add(grp);
				//tempGroupList.remove(grp);
			}
		}

		// if(tempGroupList.size() != 0){
		for(GroupItem grp:tempGroupList){
			if((grp.mGroupType == null)||(grp.mGroupType.equals(0))){
				grp.mDisplayOrder = order;
				order++;
				newGroupList.add(grp);
				//tempGroupList.remove(grp);
			}
		}
		//}

	}

	/**
	 * Add contact group relations between the newly added groups and the contacts
	 */
	private void addContactsToNewGroups(List<GroupItem> groupList){
		LogUtils.logD("GroupSyncEngine.addContactsToNewGroups");
		for(GroupItem grp : groupList){
			LogUtils.logD("Addin contacts to group "+grp.mName);
			ArrayList<Long> contactList = new ArrayList<Long>();
			ServiceStatus status = GroupsChangeLogTable.fetchGroupContacts(grp, contactList, mDb.getReadableDatabase());
			if (status == ServiceStatus.SUCCESS) {
				for (Long contId : contactList) {
					mDb.addContactToGroup(contId, grp.mId);
				}
			}else{
				LogUtils.logE("Could not read the contacts for "+grp.mName+" group or no contacts for this group to be added");
			}
		}
	}
	
	/**
	 * Checks if the group is already present in server. If already present the response received from the server was 
	 * for editing group details
	 */
	
	private boolean isGroupAlreadyPresent(Long groupId){
		ArrayList<GroupItem> groupList =  new ArrayList<GroupItem>();
		GroupsTable.fetchGroupList(groupList, mDb.getReadableDatabase());
		
		for(GroupItem group:groupList ){
			if((group.mId!= null)&&(group.mId.equals(groupId))){
				return true;
			}
		}
		return false;
	}



}
