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
package com.facebook.android.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.downloader.FBDownloader;
import com.facebook.android.fqlmanager.FacebookEventHandler;
import com.vodafone360.people.MainApplication;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.tables.ContactDetailsTable;
import com.vodafone360.people.database.tables.ContactsTable;
import com.vodafone360.people.database.tables.StateTable;
import com.vodafone360.people.datatypes.ActivityContact;
import com.vodafone360.people.datatypes.ActivityItem;
import com.vodafone360.people.datatypes.ActivityItem.Type;
import com.vodafone360.people.service.ServiceStatus;
/*
 *  FeedListener class implements the on complete for album 
 *  feeds and link feeds
 */
public class FeedListener extends BaseRequestListener {
	private static HashMap<String, String> hashNonFrdInfo = new HashMap<String, String>();
	private static int maxCommentInFeed = 2;
	
	@Override
	public void onComplete(String response) {		
		JSONObject js = null;

		DatabaseHelper mdb = new DatabaseHelper(MainApplication.getContext());
		SQLiteStatement statement1 =
            ContactDetailsTable.fetchLocalFromStringValStatement(mdb.getReadableDatabase());
		SQLiteStatement statement2 =
            ContactsTable.fetchServerFromLocalIdStatement(mdb.getReadableDatabase());
		
		try {
			js = Util.parseJson(response);

			if (js.has("posts")) {
				JSONArray postFeeds = (JSONArray) js.getJSONArray("posts");
				JSONArray profiles = (JSONArray) js.getJSONArray("profiles");
				List<ActivityItem> activityList = new ArrayList<ActivityItem>();
				int count = postFeeds.length();
				for (int i = 0; i < count; i++) {
					ActivityItem postItem = new ActivityItem();
					JSONObject postObj = (JSONObject) postFeeds.getJSONObject(i);

					postItem.activityId = postObj.getString("post_id");
					
					postItem.time = postObj.getLong("created_time") * 1000 ;
					
					switch (postObj.getInt("type")) {
					case 247:
						postItem.type = Type.ACTIVITY_ALBUM_FEED;
						break;
					case 80:
						postItem.type = Type.ACTIVITY_LINK_FEED;
						break;
					case 46:
						postItem.type = Type.ACTIVITY_STATUS_UPDATE_FEED;
						break;						
					default:
						postItem.type = Type.ACTIVITY_EVENT_UNKNOWN;
					
					};

					JSONObject attachment_obj = postObj.getJSONObject("attachment");
					if(attachment_obj.has("name")){
						postItem.title = attachment_obj.getString("name");
					}
					if(attachment_obj.has("description")){
						postItem.description = attachment_obj.getString("description");
					}
					
					if(postItem.type == Type.ACTIVITY_ALBUM_FEED){

						if(attachment_obj.has("media")){
							JSONArray mediaList_obj = attachment_obj.getJSONArray("media");
							if (mediaList_obj != null && mediaList_obj.length() > 0) {
								for(int j = 0; j < mediaList_obj.length(); j++){
									JSONObject mediaItem_obj = (JSONObject) mediaList_obj.get(j);
									ActivityItem photoItem = new ActivityItem();
									photoItem.type = Type.ACTIVITY_PHOTO_FEED;
									photoItem.title = mediaItem_obj.getString("alt");
									photoItem.parentActivity = postItem.activityId;
									photoItem.previewUrl = mediaItem_obj.getString("src");
									photoItem.store = "facebook.com";
									if(photoItem.previewUrl != null){
										FBDownloader downloader = new FBDownloader();
										String filePath = downloader.downloadPhoto(photoItem);
										if(filePath != null){
											photoItem.uri = filePath;
										}
									}
									activityList.add(photoItem);									
								}
							}
						}
						
					}else if(postItem.type == Type.ACTIVITY_LINK_FEED){
						postItem.previewUrl = attachment_obj.getString("href");
						
					}
					postItem.store = "facebook.com";
					postItem.activityFlags = ActivityItem.STATUS_ITEM;

					StringBuilder moreInfoField = new StringBuilder();
					
					JSONObject comment_obj = postObj.getJSONObject("comments");
					JSONObject like_obj = postObj.getJSONObject("likes");
					
					int commentCount = 0;
					if(comment_obj.has("count")){
						commentCount = comment_obj.getInt("count");
					}else{
						commentCount = 0;
					}
					
					int likeCount = 0;
					if(like_obj.has("count")){

						likeCount = like_obj.getInt("count");
					}else{
						likeCount = 0;
					}
					moreInfoField.append("LIKE_COUNT:");
					moreInfoField.append(likeCount);
					moreInfoField.append(",");
					moreInfoField.append("COMMENT_COUNT:");
					moreInfoField.append(commentCount);
					moreInfoField.append(",");
					if(postObj.has("message")){
						moreInfoField.append("MESSAGE:");
						moreInfoField.append(postObj.getString("message"));
						//moreInfoField.append(",");
					}
					
					postItem.moreinfo = moreInfoField.toString();
					postItem.hasChildren = commentCount > 0;
					/*
					 *  Get the localId and serverId from activity table
					 */
					ActivityContact contact = new ActivityContact();
					contact.mUserId = postObj.getLong("source_id");
					
					if (postObj.getLong("source_id") != postObj.getLong("viewer_id")) {
						Long localId = ContactDetailsTable.fetchLocalFromFacebookId(contact.mUserId, statement1);
						Long serverID = ContactsTable.fetchServerFromLocalId(localId, statement2);
						contact.mContactId = serverID;
						contact.mLocalContactId = localId;
					} else {
						contact.mLocalContactId = StateTable.fetchMeProfileId(mdb.getReadableDatabase());
					}
					for(int j = 0; j < profiles.length(); j++){
						JSONObject profile_obj = profiles.getJSONObject(j);
						if(profile_obj.getLong("id") == contact.mUserId){
							contact.mName = profile_obj.getString("name");
							contact.mAvatarUrl = profile_obj.getString("pic_square");
							break;
						}
					}
					postItem.contactList = new ArrayList<ActivityContact>();
					postItem.contactList.add(contact);
					
					activityList.add(postItem);
					/*
					 * Handles the comments received for a feed.
					 */
					if(commentCount <= maxCommentInFeed){
						JSONArray commentList = comment_obj.getJSONArray("comment_list");
						for(int j = 0; j < commentList.length(); j++){
							ActivityItem commentItem = new ActivityItem();
							JSONObject comment = commentList.getJSONObject(j);							
							commentItem.activityId = comment.getString("id");
							commentItem.time = comment.getLong("time") * 1000;
							commentItem.type = Type.ACTIVITY_COMMENT_FEED;
							commentItem.description = comment.getString("text");
							commentItem.parentActivity = postItem.activityId;
							commentItem.store = "facebook.com";
							
							ActivityContact contact_activity = new ActivityContact();
							contact_activity.mUserId = comment.getLong("fromid");
							if (comment.getLong("fromid") != postObj.getLong("viewer_id")) {
								Long localId = ContactDetailsTable.fetchLocalFromFacebookId(contact_activity.mUserId, statement1);
								Long serverID = ContactsTable.fetchServerFromLocalId(localId, statement2);
								contact_activity.mContactId = serverID;
								contact_activity.mLocalContactId = localId;
								
								for(int k = 0; k < profiles.length(); k++){
									JSONObject profile_obj = profiles.getJSONObject(k);
									if(profile_obj.getLong("id") == contact_activity.mUserId){
										contact_activity.mName = profile_obj.getString("name");
										contact_activity.mAvatarUrl = profile_obj.getString("pic_square");
										break;
									}
								}
							} else {
								contact.mLocalContactId = StateTable.fetchMeProfileId(mdb.getReadableDatabase());
							}
							commentItem.contactList = new ArrayList<ActivityContact>();
							commentItem.contactList.add(contact_activity);
							activityList.add(commentItem);
						}
				
					} else {
						/*
						 * Fetch comments from comment table if it is more than maxCommentInFeed
						 */
						String commentResponse = FacebookEventHandler.getPostComments(postItem.activityId);
			        	JSONArray commentList;
						try {
							commentList = new JSONArray(commentResponse);
				        	int CommentCount = commentList.length();
				        	for (int k = 0; k < CommentCount; k++) {

								ActivityItem commentItem = new ActivityItem();
								JSONObject comment = commentList.getJSONObject(k);							
								commentItem.activityId = comment.getString("id");
								commentItem.time = comment.getLong("time")* 1000;;
								commentItem.type = Type.ACTIVITY_COMMENT_FEED;
								commentItem.description = comment.getString("text");
								commentItem.parentActivity = postItem.activityId;
								commentItem.store = "facebook.com";
								
								ActivityContact contact_activity = new ActivityContact();
								contact_activity.mUserId = comment.getLong("fromid");
								if (comment.getLong("fromid") != postObj.getLong("viewer_id")) {
								Long localId = ContactDetailsTable.fetchLocalFromFacebookId(contact_activity.mUserId, statement1);
									if (localId != null) {
										Long serverID = ContactsTable.fetchServerFromLocalId(localId, statement2);
										contact_activity.mContactId = serverID;
										contact_activity.mLocalContactId = localId;
									} else {
										if (hashNonFrdInfo.get(contact_activity.mUserId.toString()) == null ) { 
											/*
											 * Fetch name field for contacts not in friend list
											 */
											String userInfoResponse = FacebookEventHandler.getUserInfo(contact_activity.mUserId.toString());
											JSONArray userInfoList;
											try {
												userInfoList = new JSONArray(userInfoResponse);
												JSONObject userInfo = userInfoList.getJSONObject(0);
												contact_activity.mName = userInfo.getString("name");
												hashNonFrdInfo.put(contact_activity.mUserId.toString(), contact_activity.mName);
											}catch (JSONException e) {
												// TODO Auto-generated catch block
												Log.v("JSON Exception", ""+e.getMessage());
											}
										} else {
											contact_activity.mName = hashNonFrdInfo.get(contact_activity.mUserId.toString());
										}
									}
								} else {
									contact.mLocalContactId = StateTable.fetchMeProfileId(mdb.getReadableDatabase());
								}
								commentItem.contactList = new ArrayList<ActivityContact>();
								commentItem.contactList.add(contact_activity);
								activityList.add(commentItem);
				        	}
				        	
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.v("JSON Exception", ""+e.getMessage());
						}
						
					}
					postItem = null;		 

				}
				if (activityList != null) {
					mdb.removeDuplicatesFromDatabase(activityList);
					ServiceStatus status  = mdb.addActivities(activityList);
					savelatestTimeStamp(activityList);
					Log.v("End of parsing", ""+status.toString());
				}
			}
			
		} catch (JSONException e) {
			Log.v("JSON Exception", ""+e.getMessage());
		} catch (FacebookError e) {
			Log.v("FacebookError Exception", ""+e.getMessage());
		}finally{
			mdb.close();
			statement1.close();
			statement2.close();
		}
	}
	/*
	 *  saving timeStamp when posts are the fetched from FB
	 */
	private void savelatestTimeStamp(List<ActivityItem> activityList){
		if(activityList.size() > 0){
			SharedPreferences pref = MainApplication.getContext().getSharedPreferences("FACEBOOK_TIMESTAMP", 0);
			long latestPostTimestamp = findLastStatusUpdateTime(activityList);
			SharedPreferences.Editor edit = pref.edit();
			edit.putLong("TIMESTAMP", ((latestPostTimestamp /1000)+1));
			edit.commit();
			edit = null;
			pref = null;
		}
	}

	/*
	 *  Extracting last timeStamp when posts were the fetched from FB
	 */
	private Long findLastStatusUpdateTime(List<ActivityItem> activityList) {
        Long ret = Long.MIN_VALUE;
        for (ActivityItem ai : activityList) {
            if (ai.time != null && (ai.time.compareTo(ret) > 0)) {
                ret = ai.time;
            }
        }
        return ret;
    }
}
