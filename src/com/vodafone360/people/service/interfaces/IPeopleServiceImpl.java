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

package com.vodafone360.people.service.interfaces;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vodafone360.people.ApplicationCache;
import com.vodafone360.people.MainApplication;
import com.vodafone360.people.datatypes.Album;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.Content;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.datatypes.Identity;
import com.vodafone360.people.datatypes.LoginDetails;
import com.vodafone360.people.datatypes.RegistrationDetails;
import com.vodafone360.people.datatypes.ContactSummary.OnlineStatus;
import com.vodafone360.people.engine.EngineManager;
import com.vodafone360.people.engine.BaseEngine.IEngineEventCallback;
import com.vodafone360.people.engine.presence.NetworkPresence;
import com.vodafone360.people.service.RemoteService;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.PersistSettings.InternetAvail;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.agent.NetworkAgentState;
import com.vodafone360.people.service.agent.UiAgent;
import com.vodafone360.people.utils.LogUtils;


/***
 * @see com.vodafone360.people.engine.BaseEngine.IEngineEventCallback
 * @see com.vodafone360.people.service.interfaces.IPeopleService
 */
public class IPeopleServiceImpl implements IPeopleService, IEngineEventCallback {
    private final List<Handler> mUiEventCallbackList = new ArrayList<Handler>();

    private IWorkerThreadControl mWorkerThreadControl;

    private RemoteService mService;

    private NetworkAgent mNetworkAgent;

    private UiAgent mHandlerAgent;

    private ApplicationCache mApplicationCache;

    /**
     * Initialises the object, creating the UiAgent.
     * 
     * @param workerThreadControl Provides access to worker thread control
     *            functions.
     * @param service Provides access to remote service functions (mainly used
     *            to retrieve context).
     */
    public IPeopleServiceImpl(IWorkerThreadControl workerThreadControl, RemoteService service) {
        mWorkerThreadControl = workerThreadControl;
        mService = service;
        mHandlerAgent = new UiAgent((MainApplication)service.getApplication(), service);
        mApplicationCache = ((MainApplication)service.getApplication()).getCache();
    }

    /***
     * Sets the ServiceAgent, as this needs to be called after the constructor.
     * 
     * @param agent Handle to ServiceAgent.
     */
    public void setNetworkAgent(NetworkAgent agent) {
        mNetworkAgent = agent;
    }

    /***
     * @see com.vodafone360.people.engine.BaseEngine.IEngineEventCallback#onUiEvent(UiEvent,
     *      int, int, Object)
     */
    @Override
    public void onUiEvent(ServiceUiRequest event, int arg1, int arg2, Object data) {
        synchronized (mUiEventCallbackList) {
            for (Handler handler : mUiEventCallbackList) {
                Message msg = handler.obtainMessage(event.ordinal(), data);
                msg.arg1 = arg1;
                msg.arg2 = arg2;
                if (!handler.sendMessage(msg)) {
                    LogUtils.logE("IPeopleServiceImpl.onUiEvent() Sending msg FAILED");
                }
            }
        }
    }

    /***
     * @see com.vodafone360.people.engine.BaseEngine.IEngineEventCallback#kickWorkerThread()
     */
    @Override
    public void kickWorkerThread() {
        mWorkerThreadControl.kickWorkerThread();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#addEventCallback(Handler)
     */
    @Override
    public void addEventCallback(Handler uiHandler) {
        synchronized (mUiEventCallbackList) {
            if (!mUiEventCallbackList.contains(uiHandler)) {
                mUiEventCallbackList.add(uiHandler);
            }
        }
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#removeEventCallback(Handler)
     */
    @Override
    public void removeEventCallback(Handler uiHandler) {
        synchronized (mUiEventCallbackList) {
            mUiEventCallbackList.remove(uiHandler);
        }
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#checkForUpdates()
     */
    @Override
    public void checkForUpdates() {
        EngineManager.getInstance().getUpgradeEngine().checkForUpdates();
    }

    /**
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getMyThirdPartyIdentities()
     */
    public ArrayList<Identity> getMyThirdPartyIdentities() {
    	return EngineManager.getInstance().getIdentityEngine().getMyThirdPartyIdentities();
    }

    /**
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getMy360AndThirdPartyIdentities()
     */
    public ArrayList<Identity> getAvailableThirdPartyIdentities() {
    	return EngineManager.getInstance().getIdentityEngine().getAvailableThirdPartyIdentities();
    }
    
    /**
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getMy360AndThirdPartyChattableIdentities()
     */
    public ArrayList<Identity> getMy360AndThirdPartyChattableIdentities() {
    	return EngineManager.getInstance().getIdentityEngine().getMy360AndThirdPartyChattableIdentities();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#fetchPrivacyStatement()
     */
    @Override
    public void fetchPrivacyStatement() {
        EngineManager.getInstance().getLoginEngine().addUiFetchPrivacyStatementRequest();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#fetchTermsOfService()
     */
    @Override
    public void fetchTermsOfService() {
        EngineManager.getInstance().getLoginEngine().addUiFetchTermsOfServiceRequest();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#fetchUsernameState(String)
     */
    @Override
    public void fetchUsernameState(String userName) {
        EngineManager.getInstance().getLoginEngine().addUiGetUsernameStateRequest(userName);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getLoginRequired()
     */
    @Override
    public boolean getLoginRequired() {
        EngineManager manager = EngineManager.getInstance();
        return manager.getLoginEngine().getLoginRequired();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getRoamingNotificationType()
     */
    @Override
    public int getRoamingNotificationType() {
        return mService.getNetworkAgent().getRoamingNotificationType();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getRoamingDeviceSetting()
     */
    @Override
    public boolean getRoamingDeviceSetting() {
        return mService.getNetworkAgent().getRoamingDeviceSetting();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#notifyDataSettingChanged(InternetAvail)
     */
    @Override
    public void notifyDataSettingChanged(InternetAvail val) {
        mService.getNetworkAgent().notifyDataSettingChanged(val);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#logon(LoginDetails)
     */
    @Override
    public void logon(LoginDetails loginDetails) {
        EngineManager manager = EngineManager.getInstance();
        manager.getLoginEngine().addUiLoginRequest(loginDetails);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#register(RegistrationDetails)
     */
    @Override
    public void register(RegistrationDetails details) {
        EngineManager.getInstance().getLoginEngine().addUiRegistrationRequest(details);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#setNewUpdateFrequency()
     */
    @Override
    public void setNewUpdateFrequency() {
        EngineManager.getInstance().getUpgradeEngine().setNewUpdateFrequency();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#setShowRoamingNotificationAgain(boolean)
     */
    @Override
    public void setShowRoamingNotificationAgain(boolean showAgain) {
        mService.getNetworkAgent().setShowRoamingNotificationAgain(showAgain);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#startContactSync()
     */
    @Override
    public void startContactSync() {
        EngineManager.getInstance().getGroupsEngine().addUiGetGroupsRequest();
        EngineManager.getInstance().getContactSyncEngine().addUiStartFullSync();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#startBackgroundContactSync(long)
     */
    @Override
    public void startBackgroundContactSync(long delay) {
        EngineManager.getInstance().getGroupsEngine().addUiGetGroupsRequest();
        EngineManager.getInstance().getContactSyncEngine().addUiStartServerSync(delay);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#pingUserActivity()
     */
    @Override
    public void pingUserActivity() {
        EngineManager.getInstance().getContactSyncEngine().pingUserActivity();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#validateIdentityCredentials(boolean,
     *      String, String, String, Bundle)
     */
    @Override
    public void validateIdentityCredentials(boolean dryRun, String network, String username,
            String password, Bundle identityCapabilityStatus) {
        EngineManager.getInstance().getIdentityEngine().addUiValidateIdentityCredentials(dryRun,
                network, username, password, identityCapabilityStatus);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#startStatusesSync()
     */
    @Override
    public void startStatusesSync() {
        EngineManager.getInstance().getActivitiesEngine().addStatusesSyncRequest();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getNetworkAgentState()
     */
    @Override
    public NetworkAgentState getNetworkAgentState() {
        return mNetworkAgent.getNetworkAgentState();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#setNetowrkAgentState(NetworkAgentState)
     */
    @Override
    public void setNetworkAgentState(NetworkAgentState state) {
        mNetworkAgent.setNetworkAgentState(state);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getPresenceList(long)
     */
    @Override
    public void getPresenceList(long contactId) {
        EngineManager.getInstance().getPresenceEngine().getPresenceList();
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#setAvailability(OnlineStatus)
     */    
    @Override
    public void setAvailability(OnlineStatus status) {
        EngineManager.getInstance().getPresenceEngine().setMyAvailability(status);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#setAvailability(NetworkPresence)
     */    
    @Override
    public void setAvailability(NetworkPresence presence) {
        EngineManager.getInstance().getPresenceEngine().setMyAvailability(presence);
    }
    

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#subscribe(Handler,
     *      long, boolean)
     */
    @Override
    public void subscribe(Handler handler, Long contactId, boolean chat) {
        mHandlerAgent.subscribe(handler, contactId, chat);
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#unsubscribe(Handler)
     */
    @Override
    public void unsubscribe(Handler handler) {
        mHandlerAgent.unsubscribe(handler);
    }

    /**
     * @see com.vodafone360.people.engine.BaseEngine.IEngineEventCallback#getUiAgent()
     */
    @Override
    public UiAgent getUiAgent() {
        return mHandlerAgent;
    }

    /**
     * @see com.vodafone360.people.engine.BaseEngine.IEngineEventCallback#getApplicationCache()
     */
    @Override
    public ApplicationCache getApplicationCache() {
        return mApplicationCache;
    }

    /**
     * @see com.vodafone360.people.service.interfaces.IPeopleService#sendMessage(long,
     *      String, int)
     */
    @Override
    public void sendMessage(long localContactId, String body, int networkId) {
        EngineManager.getInstance().getPresenceEngine()
                .sendMessage(localContactId, body, networkId);
    }

    @Override
    public void setIdentityStatus(String network, String identityId, boolean identityStatus) {
        EngineManager.getInstance().getIdentityEngine().addUiSetIdentityStatus(network, identityId,
                identityStatus);
    }

    @Override
    public void getStatuses() {
        EngineManager.getInstance().getActivitiesEngine().addStatusesSyncRequest();
    }

    @Override
    public void getMoreTimelines() {
        EngineManager.getInstance().getActivitiesEngine().addOlderTimelinesRequest();
    }

    @Override
    public void getOlderStatuses() {
        EngineManager.getInstance().getActivitiesEngine().addGetOlderStatusesRequest();
    }

    @Override
    public void uploadMeProfile() {
        EngineManager.getInstance().getSyncMeEngine().addUpdateMeProfileContactRequest();
    }

    @Override
    public void uploadMyStatus(String statusText) {
        EngineManager.getInstance().getSyncMeEngine().addUpdateMyStatusRequest(statusText);
    }

    @Override
    public void downloadMeProfileFirstTime() {
        EngineManager.getInstance().getSyncMeEngine().addGetMeProfileContactFirstTimeRequest();
    }

    @Override
    public void updateChatNotification(long localContactId) {
        mHandlerAgent.updateChat(localContactId, false);
        
    }

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#postComment(List)
     */
	 @Override
	 public void postComment(List <Comment> commentsList){
	    EngineManager.getInstance().getCommentsEngine().addUiPostCommentRequest(commentsList);
	 }
	   
	 /***
	 * @see com.vodafone360.people.service.interfaces.IPeopleService#deleteComment(Bundle)
	 */
	 @Override
	 public void deleteComment(Bundle data){
	    EngineManager.getInstance().getCommentsEngine().addUiDeleteCommentRequest(data);
	 }

	/***
	* @see com.vodafone360.people.service.interfaces.IPeopleService#deleteComment(Bundle)
	*/
	@Override
	public void getComment(List<EntityKey> entitykeylist) {
	    EngineManager.getInstance().getCommentsEngine().addUiGetCommentRequest(entitykeylist);
		
	}
	
	/***
	* @see com.vodafone360.people.service.interfaces.IPeopleService#deleteComment(Bundle)
	*/
	@Override
	public void updateComment(List<Comment> commentsList) {
	    EngineManager.getInstance().getCommentsEngine().addUiUpdateCommentRequest(commentsList);
	}
	
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#addAlbum(List)
     */
    @Override
	public void addAlbum(List<Album> albumList) {
        EngineManager.getInstance().getContentEngine().addUiAddAlbumRequest(albumList);
	}
    
    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#deleteAlbum(List)
     */
    @Override
	public void deleteAlbum(List<Long> albumList) {
        EngineManager.getInstance().getContentEngine().addUiDeleteAlbumRequest(albumList);
	}

    /***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getAlbum(List)
     */
	@Override
	public void getAlbum(List<Long> albumList) {
        EngineManager.getInstance().getContentEngine().addUiGetAlbumRequest(albumList);
	}
	
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#updateAlbum(List)
     */
	@Override
	public void updateAlbum(List<Album> albumList) {
		Log.d("IPeopleServiceImpl", "updateAlbum");
        EngineManager.getInstance().getContentEngine().addUiUpdateAlbumRequest(albumList);
	}
	
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#addContentToAlbum(List)
     */
	@Override
	public void addContentToAlbum(Bundle data) {
		Log.d("IPeopleServiceImpl", "addContentToAlbum");
        EngineManager.getInstance().getContentEngine().addUiAddContentToAlbumRequest(data);
	}

	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#deleteContentFromAlbum(List)
     */
	@Override
	public void deleteContentFromAlbum(Bundle data) {
		Log.d("IPeopleServiceImpl", "deleteContentFromAlbum");
        EngineManager.getInstance().getContentEngine().addUiDeleteContentFromAlbumRequest(data);
	}
	
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#publishAlbum(List)
     */
	@Override
	public void publishAlbum(Bundle data) {
		Log.d("IPeopleServiceImpl", "publishAlbum");
        EngineManager.getInstance().getContentEngine().addUiPublishAlbumRequest(data);
	}
	
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#addContent(List)
     */
	@Override
	public void addContent(List<Content> contentlist) {
		Log.d("IPeopleServiceImpl", "uploadImage");
	    EngineManager.getInstance().getContentEngine().addUiUploadContentRequest(contentlist);
	}
	  
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#getContent(Bundle)
     */
	@Override
	public void getContent(Bundle data) {
		Log.d("IPeopleServiceImpl", "getContent");
        EngineManager.getInstance().getContentEngine().addUiGetContentRequest(data);
	}
	
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#publishContent(Bundle)
     */
	@Override
	public void publishContent(Bundle data) {
		Log.d("IPeopleServiceImpl", "publishContent");
        EngineManager.getInstance().getContentEngine().addUiPublishContentRequest(data);
	}

	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#deleteContent(List)
     */
	@Override
	public void deleteContent(List<Long> contentList) {
		Log.d("IPeopleServiceImpl", "deleteContent");
        EngineManager.getInstance().getContentEngine().addUiDeleteContentRequest(contentList);
	}
	
	/***
     * @see com.vodafone360.people.service.interfaces.IPeopleService#startLocationEngine(Bundle)
     */
	
	  @Override
	    public void startLocationEngine(Bundle b) {
	    	LogUtils.logD("LocationEngine.startLocationEngine()");
	    	EngineManager.getInstance().getLocationEngine().addUiGetMyLocation(b);
	    }
	  
	  /***
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#sendLocationNudge(Bundle)
	     */
	    @Override
	    public void sendLocationNudge(Bundle b){
	    	LogUtils.logD("LocationEngine.sendLocationNudge()");
	    	EngineManager.getInstance().getLocationEngine().addUiSendLocationNudge(b);
	    }
	    
	    /***
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#addUserGroup()
	     */
	    @Override
	    public void addUserGroup(String groupName) {
	    	EngineManager.getInstance().getGroupsEngine().addUiAddUserDefinedGroup(groupName);
	    }
	    
	    /***
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#deleteUserGroup()
	     */
	    @Override
	    public void deleteUserGroup(String groupName) {
	    	EngineManager.getInstance().getGroupsEngine().addUiDeleteUserDefinedGroup(groupName);
	    }
	    
	    /***
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#getGroupPrivacySetting()
	     */
	    @Override
	    public void getGroupPrivacySetting(String groupName) {
	    	EngineManager.getInstance().getGroupsEngine().addUiGetGroupPrivacySetting(groupName);
	    }
	    
	    /***
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#setGroupPrivacySetting()
	     */
	    @Override
	    public void setGroupPrivacySetting(String groupName, int contentType, int status) {
	    	EngineManager.getInstance().getGroupsEngine().addUiSetGroupPrivacySetting(groupName, contentType, status);
	    }
	    
	    /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#shareAlbum()
	     */
	    
	    public void shareAlbum(Long groupId, EntityKey entityId){
	    	EngineManager.getInstance().getShareEngine().addUiShareAlbum(groupId, entityId);
	    }
	    
	    /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#allowGroup()
	     */
	    public void allowGroup(Long groupId, EntityKey entityId){
	    	EngineManager.getInstance().getShareEngine().addUiAllowGroup(groupId, entityId);
	    }
	    
	    /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#albumSharedWith()
	     */
	    public void albumSharedWith(EntityKey entityId){
	    	EngineManager.getInstance().getShareEngine().addUiSharedWith(entityId);
	    }
	    
	    /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#denyAlbum()
	     */
	    public void denyAlbum(Long groupId, EntityKey entityId){
	    	EngineManager.getInstance().getShareEngine().addUiDenyGroup(groupId, entityId);
	    }

	    /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#approveFriendRequest()
	     */
		@Override
		public void approveFriendRequest(List<Long> requestIdList) {
			EngineManager.getInstance().getContactSyncEngine().addUiApproveFriendReq(requestIdList);
		}

		 /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#getFriendRequest()
	     */
		@Override
		public void getFriendRequest() {
			EngineManager.getInstance().getContactSyncEngine().addUiGetFriendReq();
		}

		 /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#rejectFriendRequest()
	     */
		@Override
		public void rejectFriendRequest(List<Long> requestIdList) {
			EngineManager.getInstance().getContactSyncEngine().addUiRejectFriendReq(requestIdList);

		}

		 /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#removeFriendRequest()
	     */
		@Override
		public void removeFriendRequest(List<Long> userIdList) {
			EngineManager.getInstance().getContactSyncEngine().addUiRemoveFriend(userIdList);
		}

		 /**
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#sendFriendRequest()
	     */
		@Override
		public void sendFriendRequest(Bundle data) {
			EngineManager.getInstance().getContactSyncEngine().addUiSendFriendReq(data);
		}
		
		/***
	     * @see com.vodafone360.people.service.interfaces.IPeopleService#fetchMyIdentities(Bundle)
	     */
	    @Override
	    public void deleteIdentity(Bundle bundle) {
	        EngineManager.getInstance().getIdentityEngine().addUiDeleteIdentity(bundle);
	    }
	    
}