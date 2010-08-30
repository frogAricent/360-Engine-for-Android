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

import com.vodafone360.people.datatypes.Album;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.Content;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.datatypes.Identity;
import com.vodafone360.people.datatypes.LoginDetails;
import com.vodafone360.people.datatypes.RegistrationDetails;
import com.vodafone360.people.datatypes.ContactSummary.OnlineStatus;
import com.vodafone360.people.engine.presence.NetworkPresence;
import com.vodafone360.people.service.PersistSettings.InternetAvail;
import com.vodafone360.people.service.agent.NetworkAgentState;

/***
 * Interface to expose service functionality to the UI classes.
 */
public interface IPeopleService {
    /***
     * Allows the Activity to register a Handler, so that it can receive new
     * call back events from the Service layer.
     * 
     * @param Handler to listen for call back events.
     */
    void addEventCallback(Handler uiHandler);

    /***
     * Allows the Activity to unregister a Handler, so it will no longer receive
     * call back events from the Service layer. Usually called on an Activity's
     * onDestroy() method.
     * 
     * @param Handler to listen for call back events.
     */
    void removeEventCallback(Handler uiHandler);

    /***
     * Called by the UI to determine if the Service is currently logged into the
     * Vodafone 360 back end. Called by the StartActivity and other key classes,
     * so the work flow can be diverted back to the Landing page if the user has
     * been signed out for any reason.
     * 
     * @return TRUE Service is logged into the back end system, FALSE user must
     *         log in before they can use the Application.
     */
    boolean getLoginRequired();

    /***
     * Sets a preference that disables the showing of further roaming
     * notifications.
     * 
     * @param TRUE show further roaming notifications, FALSE stop showing
     */
    void setShowRoamingNotificationAgain(boolean showAgain);

    /***
     * Gets the type of roaming notification to show to the user
     * 
     * @return ROAMING_DIALOG_GLOBAL_ON Data roaming is on,
     *         ROAMING_DIALOG_GLOBAL_OFF Data roaming is off
     */
    int getRoamingNotificationType();

    /***
     * Gets the current IS_ROAMING_ALLOWED value for the device, which is set by
     * the user in the
     * "Menu > Settings > Wireless Controls > Mobile network settings > Data roaming"
     * check box.
     * 
     * @return TRUE when roaming is permitted, FALSE when roaming is not
     *         permitted.
     */
    boolean getRoamingDeviceSetting();

    /***
     * Sets the current Data connectivity preference (i.e. connect, connect when
     * not roaming, or never connect), although this value is ignored during
     * initial sign up.
     * 
     * @see com.vodafone360.people.service.PersistSettings.InternetAvail
     * @param InternetAvail New data Settings changes
     */
    void notifyDataSettingChanged(InternetAvail internetAvail);

    /***
     * Fetched the current Terms of Service information from the Vodafone 360
     * back end system. The result is sent to the registered Handler some time
     * later after the download process has finished.
     */
    void fetchTermsOfService();

    /***
     * Fetched the current Privacy Statement information from the Vodafone 360
     * back end system. The result is sent to the registered Handler some time
     * later after the download process has finished.
     */
    void fetchPrivacyStatement();

    /***
     * Log into the Vodafone 360 back end system using the given credentials.
     * 
     * @param LoginDetails object containing user name, password, etc.
     */
    void logon(LoginDetails loginDetails);

    /***
     * Asks the back end server to check the validity of the given user name.
     * 
     * @param String User name to check.
     */
    void fetchUsernameState(String username);

    /***
     * Signs up a new user to the Vodafone 360 back end using the given
     * Registration details.
     * 
     * @param RegistrationDetails Registration details
     */
    void register(RegistrationDetails details);

    /***
     * Begins the process of synchronising contacts with the Vodafone 360 back
     * end. This is designed to run in the foreground and be shown to the user
     * during the initial sign up process.
     */
    void startContactSync();

    /***
     * Begins the process of synchronising contacts with the Vodafone 360 back
     * end. This is designed to run in the background and is called every time
     * the ContactListActivity is shown to the user.
     * 
     * @param delay the delay in milliseconds from now when the sync should
     *            start
     */
    void startBackgroundContactSync(long delay);

    /**
     * Pings the service about user activity.
     */
    void pingUserActivity();

    /**
     * 
     * Gets all third party identities and adds the mobile identity
     * from 360 to them.
     * 
     * @return A list of all 3rd party identities the user is signed in to plus 
     * the 360 identity mobile. If the retrieval failed the list will
     * be empty.
     * 
     */
    public ArrayList<Identity> getAvailableThirdPartyIdentities();
    
    /**
     * 
     * Gets all third party identities the user is currently signed up for. 
     * 
     * @return A list of 3rd party identities the user is signed in to or an 
     * empty list if something  went wrong retrieving the identities. 
     * 
     */
    public ArrayList<Identity> getMyThirdPartyIdentities();
    
    /**
     * 
     * Takes all third party identities that have a chat capability set to true.
     * It also includes the 360 identity mobile.
     * 
     * @return A list of chattable 3rd party identities the user is signed in to
     * plus the mobile 360 identity. If the retrieval identities failed the 
     * returned list will be empty.
     * 
     */
    public ArrayList<Identity> getMy360AndThirdPartyChattableIdentities();

    /***
     * Begins the process of retrieving all Third party Accounts from the
     * Vodafone 360 back end. The response is sent to any currently registered
     * Activity handlers.
     * 
     * @param Bundle filter the kind of identities to return.
     */
    //void fetchAvailableIdentities(Bundle data);

    /***
     * Calls the set identity capability status API
     * 
     * @param network Social Network Name
     * @param identityId Social Network Identifier
     * @param identityCapabilityStatus Social Network capability status Bundle
     */
    void setIdentityStatus(String network, String identityId, boolean identityStatus);

    /***
     * Validate the given Social Network identity
     * 
     * @param dryRun Set to true to validate credentials without actually
     *            signing the server up.
     * @param network Social Network Name
     * @param username Login user name
     * @param password Login password
     * @param identityCapabilityStatus Social Network capability status Bundle
     */
    void validateIdentityCredentials(boolean dryRun, String network, String username,
            String password, Bundle identityCapabilityStatus);

    /***
     * Push the UpdateEngine to immediately check for an updated version of the
     * client.
     */
    void checkForUpdates();

    /***
     * Push the UpdateEngine to check if a new update frequency has been set and
     * to act accordingly.
     */
    void setNewUpdateFrequency();

    /***
     * Push the ActivitiesEngine Engine to begin synchronising Activities
     */
    void startStatusesSync();

    /***
     * Returns the current state of the Network Agent. Used for testing only.
     */
    NetworkAgentState getNetworkAgentState();

    /***
     * Overrides the current state of the Network Agent. Used for testing only.
     * 
     * @param state A new overriding state.
     */
    void setNetworkAgentState(NetworkAgentState state);

    /***
     * Request a refresh of the currently known Presence information (used for
     * testing only)
     * 
     * @param contactId Provide a contactId to receive detailed presence
     *            information for the given contact only
     * @param contactId Set this to -1 to receive less detailed presence
     *            information but for every contact
     */
    void getPresenceList(long contactId);
    
    /**
     * Change current global (all identities) availability state.
     * @param status Availability to set for all identities we have. 
     */
    void setAvailability(OnlineStatus status);
    
    /**
     * Change current availability state for a single network.
	 * @param presence Network-presence to set
     */
    void setAvailability(NetworkPresence presence);

    /***
     * Allows an Activity to indicate to the Service that it is ready and able
     * to handle incoming unsolicited UI events. This should be called in an
     * Activities onResume() method, to indicate that the activity is currently
     * on screen.
     * 
     * @param handler to accept incoming unsolicited UI events from the Service.
     * @param contactId Provide a contactId to receive updates for the given
     *            contact only. Set this to -1 to receive updates for every
     *            contact. Set this to NULL not to receive contact updates.
     * @param chat - TRUE if the Handler expects chat messages.
     */
    void subscribe(Handler handler, Long contactId, boolean chat);

    /***
     * Allows the Activity to indicate that it is no longer in the foreground
     * and will not handle incoming UI events correctly. This should be called
     * in an Activities onPause() method, to indicate that the Activity is not
     * on screen.
     * 
     * @param handler that should no longer receive incoming unsolicited UI
     *            events from the Service
     */
    void unsubscribe(Handler handler);

    /**
     * This method should be used to send a message to a contact
     * 
     * @param to LocalContactIds of ContactSummary/TimelineSummary items the
     *            message is intended for. Current protocol version only
     *            supports a single recipient.
     * @param body Message text
     */
    void sendMessage(long toLocalContactId, String body, int socialNetworkId);


    /**
     * This method should be called to retrieve status updates in
     * StatusListActivity, @see ActivitiesEngine.
     */
    void getStatuses();

    /**
     * This method should be called to retrieve older timelines in
     * TimelineListActivity, @see ActivitiesEngine.
     */
    void getMoreTimelines();

    /**
     * This method should be called to retrieve older statuses in
     * StatusListActivity, @see ActivitiesEngine.
     */
    void getOlderStatuses();

    /**
     * This method triggers the Me Profile upload
     */
    void uploadMeProfile();

    /**
     * This method triggers the Me Profile status text upload
     * 
     * @param statusText String - new Me Profile status text
     */
    void uploadMyStatus(String statusText);

    /**
     * This method triggers the Me Profile download, is currently called by UI
     */
    void downloadMeProfileFirstTime();

    /**
     * This method should be called to update the Chat Notifications.
     */
    void updateChatNotification(long localContactId);

    /**
     * Adds comment to an entity in the platform
     * 
     * @param albumList Provide a list of Albums to be updated.
     */
    void postComment(List <Comment> commentsList);
    
    /**
     * Deletes a list of Comments in the platform
     * 
     * @param albumList Provide a list of Albums to be updated.
     */
    void deleteComment(Bundle data);
    
    /**
     * Gets a list of Comments from the platform
     * 
     * @param entitykeylist Provide a list of comments to be retrieved.
     */
    void getComment(List<EntityKey> entitykeylist);
    
    /**
     * Updates a list of Comments in the platform
     * 
     * @param commentsList Provide a list of comments to be updated.
     */
    void updateComment(List<Comment> commentsList);
    
    /**
     * Adds a list of Albums to the platform
     * 
     * @param albumList Provide a list of Albums to be added to the platform.
     */
    void addAlbum(List<Album> albumList);
    
    
    /**
     * Deletes a list of Albums from the platform
     * 
     * @param albumList Provide a list of Albums to be added to the platform.
     */
    void deleteAlbum(List<Long> albumList);

    
    /**
     * Gets a list of Albums from the platform
     * 
     * @param albumList Provide a list of Albums to be added to the platform.
     */
    void getAlbum(List<Long> albumList);
    
    /**
     * Updates a list of Albums in the platform
     * 
     * @param albumList Provide a list of Albums to be updated.
     */
    void updateAlbum(List<Album> albumList);
    
    /**
     * Adds a list of content to an Album
     * 
     * @param data Bundle containing the list of Content and the Album Id
     */
    void addContentToAlbum(Bundle data);

    /**
     * Deletes a list of content from an Album
     * 
     * @param data Bundle containing the list of Content and the Album Id
     */
	void deleteContentFromAlbum(Bundle data);
	
	 /**
     * Publishes a list of Albums to a community
     * 
     * @param data Bundle containing the list of Album Ids
     * 			and the community Id  
     */
    void publishAlbum(Bundle data);

    /**
     * Adds a list of content to the platform
     * 
     * @param contentlist The list of contents to be added
     */
    void addContent(List<Content> contentlist);

    /**
     * Gets a list of contents from the platform
     * 
     * @param data Bundle containing the list of Content Ids
     * 			and the filter list
     */
	void getContent(Bundle data);

	 /**
     * Publishes a list of contents to a community
     * 
     * @param data Bundle containing the list of content Ids
     * 			and the community Id  
     */
	void publishContent(Bundle data);

	/**
     * Deletes a list of content from the platform
     * 
     * @param contentlist The list of contents to be deleted
     */
	void deleteContent(List<Long> contentList);
	
	/**
     * Deletes a list of content from the platform
     * 
     * @param b The bundle containing the location information
     */
	void startLocationEngine(Bundle b);
	
	/**
     * Deletes a list of content from the platform
     * 
     * @param b The bundle containing the location information
     */
	void sendLocationNudge(Bundle b);
	
	 /**
     * This method should be called to add user defined groups
     * 
     */
    void addUserGroup(String groupName);
       
    /**
     * This method should be called to delete user defined groups
     * 
     */
    void deleteUserGroup(String groupName);
    
    /**
     * This method should be called to get group privacy setting
     * 
     */
    void getGroupPrivacySetting(String groupName);
    
    /**
     * This method should be called to set group privacy setting
     * 
     */
    void setGroupPrivacySetting(String groupName, int contentType, int status);

    /**
     * Method to Share an Album with a group 
     */
    void shareAlbum(Long groupId, EntityKey entityId);
    
    /**
     * Method to Share an Album with a group. But no notification is sent to user
     */
    void allowGroup(Long groupId, EntityKey entityId);
    
    /**
     * Method to get the groups with which an album is shared
     */
    void albumSharedWith(EntityKey entityId);
    
    /**
     * Method to deny permission to an album for a group
     */
    void denyAlbum(Long groupId, EntityKey entityId);

    /**
     * Method to send friend request
     */
    void sendFriendRequest(Bundle data);
    
    /**
     * Method to get friend request
     */
    void getFriendRequest();
   
    /**
     * Method to approve friend request
     */
    void approveFriendRequest(List<Long> requestIdList);
    
    /**
     * Method to reject friend request
     */
    void rejectFriendRequest(List<Long> requestIdList);
    
    /**
     * Method to remove friend
     */
    void removeFriendRequest(List<Long> userIdList);
}
