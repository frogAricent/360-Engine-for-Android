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
package com.facebook.android.fqlmanager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.utils.*;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.listener.AddCommentListener;
import com.facebook.android.listener.BaseRequestListener;
import com.facebook.android.listener.FeedListener;
import com.facebook.android.listener.RequestCompleteCallback;
import com.vodafone360.people.MainApplication;
import com.vodafone360.people.Settings;
import com.vodafone360.people.SettingsManager;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.tables.ActivitiesTable;
import com.vodafone360.people.database.tables.ContactSummaryTable;
import com.vodafone360.people.database.tables.StateTable;
import com.vodafone360.people.database.tables.ActivitiesTable.Field;
import com.vodafone360.people.database.utils.SqlUtils;
import com.vodafone360.people.datatypes.ActivityContact;
import com.vodafone360.people.datatypes.ActivityItem;
import com.vodafone360.people.datatypes.Contact;
import com.vodafone360.people.datatypes.ContactDetail;
import com.vodafone360.people.datatypes.ContactDetail.DetailKeys;
import com.vodafone360.people.datatypes.Content;
import com.vodafone360.people.datatypes.ActivityItem.Type;
import com.vodafone360.people.engine.meprofile.SyncMeDbUtils;
/*
 *  Class handling the facebook feeds request.
 */
public class FacebookEventHandler {

	private static FQLConnection mFQLConnection;
	private static int maxPostLimit;
	private static String photoAppKey;
	private static String linkAppKey;
	private static String statusAppKey;
	
	/*
	 *  Flags to ensure that one thread runs at a time
	 */
	private static boolean removeCommentThreadFlag = false;
	private static boolean addLikePostThreadFlag = false;
	private static boolean fetchPostsThreadFlag = false;
	/*
	 * FQL commands constants
	 */
	private static final String STREAM_GET = "stream.get";
	private static final String ADD_COMMENT = "stream.addComment";
	private static final String ADD_LIKE = "stream.addLike";
	private static final String REMOVE_COMMENT = "removeComment";

	//TODO:Remove this just for response time measurement
//	public static long time1 = null;
	
	/*
	 * Facebook commands TAGS
	 */
	private static final String FILTER_KEY = "filter_key";
	private static final String LIMIT = "limit";
	private static final String START_TIME = "start_time";
	private static final String POST_ID = "post_id";
	private static final String COMMENT = "comment";
	private static final String COMMENT_ID = "comment_id";
	
	private static HashMap<String, Long> hashFBIDLocalId = new HashMap<String, Long>();
	private static HashMap<Long, Long> hashLocalIDServerId = new HashMap<Long, Long>();
	private static Long mSelfLocalContactId = null;
	
	private static long feedsMinUpdatedTime = -1;
	
	/*
	 * Timers for scheduling fetchposts 
	 */
	 private static Timer tm; 
	
	public static synchronized void setFQLConnection (Facebook fb){
        mFQLConnection = new FQLConnection(fb);
	}

	public static FQLConnection getFQLConnection (){
        return mFQLConnection;
	}

	/**
	 *  API to fetch photo and link feeds from facebook
	 *  @param callBack
	 *       null/Callback interface to notify when the request
	 *       has completed.
	 */
	public static void fetchPosts(final RequestCompleteCallback callBack) {
		if (fetchPostsThreadFlag != true && mFQLConnection != null) {
			fetchPostsThreadFlag = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					SharedPreferences pref = MainApplication.getContext()
							.getSharedPreferences("FACEBOOK_TIMESTAMP", 0);
					
					Long updateTime = pref.getLong("TIMESTAMP", 0);
					setfeedsMinUpdatedTime(updateTime);
					if(updateTime < 0){
						updateTime = 0l;
					}
					try {
						syncFBIDLocalIdMap();
						syncLocalIDServerIdMap();
						fetchMeProfileIdFromState();
						maxPostLimit = Integer.parseInt(
								SettingsManager.getProperty(Settings.FB_MAX_POSTS_KEY));
						linkAppKey = SettingsManager.getProperty(Settings.FB_LINK_APP_KEY);
						photoAppKey = SettingsManager.getProperty(Settings.FB_PHOTO_APP_KEY);
						statusAppKey = SettingsManager.getProperty(Settings.FB_STATUS_APP_KEY);

						Bundle filter_id = new Bundle();
						filter_id.putString(LIMIT, "" + maxPostLimit);
						filter_id.putString(START_TIME, "" + updateTime);

						filter_id.putString(FILTER_KEY, linkAppKey);
						mFQLConnection.request(STREAM_GET, filter_id,
										new FeedListener());
						filter_id.putString(FILTER_KEY, statusAppKey);
						mFQLConnection.request(STREAM_GET, filter_id,
										new FeedListener());
						filter_id.putString(FILTER_KEY, photoAppKey);
						mFQLConnection.request(STREAM_GET, filter_id,
										new FeedListener());
						savelatestTimeStamp();
						if (callBack != null) {
							callBack.onComplete(true, null);
						}

						
					} catch (Exception e) {
						Log.e("Error in fetchPosts", e + "");
						if (callBack != null) {
							callBack.onComplete(false, e.getMessage());
						}
					}
					fetchPostsThreadFlag = false;
				}
			}).start();
		}
	}
/*
 *  API to fetch the list of comments for a given post id
 *  @param post_id
 *  	 post id on which comment to be posted
 */
	public static String getPostComments(String post_id) {
		String response = null;
		Bundle token = new Bundle();
		token.putString(POST_ID, post_id);
		try {
			response = mFQLConnection.request("stream.getComments", token,
				null);
		}catch (Exception e) {
			Log.e("Error in getPostComments", e + "");
		}
		return response;
	}
/*
 *  API to fetch the list of comments for a given post id
 *  @param post_id
 *  	 post id on which comment to be posted
 */
	public static String getUserInfo(String user_id) {
		String response = null;
		Bundle token = new Bundle();
		token.putString("uids", user_id);
		token.putString("fields", "name");
		try {
			response = mFQLConnection.request("users.getInfo", token,
				null);
		}catch (Exception e) {
			Log.e("Error in getUserInfo", e + "");
		}
		return response;
	}
/**
 *  API to add comment to the post on facebook
 *  @param callBack
 *       null/Callback interface to notify when the request
 *       has completed.
 *  @param post_id
 *  	 post id on which comment to be posted
 *  @param comment
 *  	 comment that needs to be posted
 */
	public static void addComment(String post_id, String comment,
			RequestCompleteCallback callBack) {
		if (mFQLConnection != null) {
			try {
				Bundle parameter = new Bundle();
				parameter.putString(POST_ID, post_id);
				String encodedComment = null;
				encodedComment = URLEncoder.encode(comment, "UTF-8");
				parameter.putString(COMMENT, encodedComment);
				String responseReceived = null;
				responseReceived = mFQLConnection.request(ADD_COMMENT, parameter,
						new AddCommentListener(callBack));
				
				if (responseReceived != null) {
					if ( responseReceived.contains("error_code")) {
						try {
							Util.parseJson(responseReceived);
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (FacebookError e) {
							if (callBack != null) {
								callBack.onComplete(false, e.getMessage());
							}
						}
					} else if (responseReceived.contains("html")) {
						if (callBack != null) {
							callBack.onComplete(false, responseReceived);
						}							
					} else {
						if (callBack != null) {
							callBack.onComplete(true, responseReceived);
						}
	
						//adding comment
							fetchMeProfileIdFromState();
							ActivityItem commentItem = new ActivityItem();
							String[] str1 = responseReceived.split("\"");
							String str2 = str1[1];
							
							
							commentItem.activityId = str2;
							commentItem.time = System.currentTimeMillis();
							commentItem.type = Type.ACTIVITY_COMMENT_FEED;
							commentItem.description = comment;
							commentItem.parentActivity = post_id;
							commentItem.store = "facebook.com";
							
							ActivityContact contact_activity = new ActivityContact();
							contact_activity.mLocalContactId = FacebookEventHandler.getMeProfileId();
							
							
							commentItem.contactList = new ArrayList<ActivityContact>();
							
							

							DatabaseHelper mdb = new DatabaseHelper(MainApplication.getContext());
							

							contact_activity.mName = ContactSummaryTable.fetchFormattedNamefromLocalContactId(contact_activity.mLocalContactId, mdb.getReadableDatabase());
							commentItem.contactList.add(contact_activity);
							List<ActivityItem> activityList = new ArrayList<ActivityItem>();
							activityList.add(commentItem);
							ActivityItem t1 = updatePostAfterComment(post_id, mdb);
							activityList.add(t1);
							mdb.getWritableDatabase().delete(ActivitiesTable.TABLE_NAME, Field.ACTIVITY_ID + " = '" + post_id +"'", null);
							ServiceStatus status  = mdb.addActivities(activityList);
							activityList = null;
							mdb.close();
	
						
						// updating the comment_count in the post
					}
				
				}
				
				
				} catch (Exception e) {
					Log.e("Error in fetchPosts", e + "");
					if (callBack != null) {
						callBack.onComplete(false, e.getMessage());
					}
				}
		}
	}
/**
 *  API to add like to the post on facebook
 *  @param callBack
 *       null/Callback interface to notify the application when the request
 *       has completed.
 *  @param post_id
 *  	 post id on which comment to be posted
 */
	public static void addLike(final String post_id,
			final RequestCompleteCallback callBack) {
		/*
		 *  verifying if a thread is already running
		 */
		if (addLikePostThreadFlag != true && mFQLConnection != null) {
			addLikePostThreadFlag = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Bundle parameter = new Bundle();
						parameter.putString(POST_ID, post_id);
						mFQLConnection.request(ADD_LIKE, parameter,
								new BaseRequestListener() {
									@Override
									public void onComplete(String response) {
										if(response.equals("true")) {
											if (callBack != null) {
												callBack.onComplete(true, response);
											}
										} else {
											try {
												Util.parseJson(response);
											} catch (JSONException e) {
												if (callBack != null) {
													callBack.onComplete(false, e.getMessage());
												}
											} catch (FacebookError e) {
												if (callBack != null) {
													callBack.onComplete(false, e.getMessage());
												}
											}
										}
									};
									@Override
									public void onIOException(IOException e) {
										if (callBack != null) {
											callBack.onComplete(false, e);
										}
									}
								}
						);
					} catch (Exception e) {
						Log.e("Error in fetchPosts", e + "");
						if (callBack != null) {
							callBack.onComplete(false, e.getMessage());
						}
					}
					addLikePostThreadFlag = false;
				}
			}).start();
		}
	}
/**
 *  API to add like to the post on facebook
 *  @param callBack
 *       null/Callback interface to notify the application when the request
 *       has completed.
 *  @param comment_id
 *  	 comment that needs to be removed
 */
	public static void removeComment(final String comment_id,
			final RequestCompleteCallback callBack) {
		/*
		 *  verifying if a thread is already running
		 */
		if (true != removeCommentThreadFlag && mFQLConnection != null) {
			removeCommentThreadFlag = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Bundle parameter = new Bundle();
						parameter.putString(COMMENT_ID, comment_id);
						mFQLConnection.request(REMOVE_COMMENT, parameter,
								new BaseRequestListener() {
							
							@Override
							public void onComplete(String response) {
								if(response.equals("true")) {
									if (callBack != null) {
										callBack.onComplete(true, response);
									}
								} else {
									try {
										Util.parseJson(response);
									} catch (JSONException e) {
										if (callBack != null) {
											callBack.onComplete(false, e.getMessage());
										}
									} catch (FacebookError e) {
										if (callBack != null) {
											callBack.onComplete(false, e.getMessage());
										}
									}
								}
							};
							@Override
							public void onIOException(IOException e) {
								if (callBack != null) {
									callBack.onComplete(false, e.getMessage());
								}
							}
						}
						);
					} catch (Exception e) {
						Log.e("Error in fetchPosts", e + "");
						if (callBack != null) {
							callBack.onComplete(false, e.getMessage());
						}
					}
					removeCommentThreadFlag = false;
				}
			}).start();
		}
	}

	private static void syncFBIDLocalIdMap()
	{
		
        DatabaseHelper mdb = new DatabaseHelper(MainApplication.getContext());

        String query = "SELECT StringVal, LocalContactId FROM ContactDetails WHERE Key=7";
        
        Cursor mCursor = mdb.getReadableDatabase().rawQuery(query, null);
		mCursor.moveToFirst();
		for (int iterator = 0; iterator < mCursor.getCount(); iterator++)
		{
			hashFBIDLocalId.put(SqlUtils.setString(mCursor,"StringVal"), SqlUtils.setLong(mCursor,"LocalContactId", null));
			mCursor.move(1);
		}
		mCursor.close();
		mdb.close();
	}
	public static Long getLocalIdFromFBID(final String FBId)
	{
		return hashFBIDLocalId.get(FBId);

	}
	private static void syncLocalIDServerIdMap()
	{
		
        DatabaseHelper mdb = new DatabaseHelper(MainApplication.getContext());

        String query = "SELECT LocalId, ServerId FROM Contacts";
        
        Cursor mCursor = mdb.getReadableDatabase().rawQuery(query, null);
		mCursor.moveToFirst();
		for (int iterator = 0; iterator < mCursor.getCount(); iterator++)
		{
			hashLocalIDServerId.put(SqlUtils.setLong(mCursor,"LocalId",null), SqlUtils.setLong(mCursor,"ServerId", null));
			mCursor.move(1);
		}
		mCursor.close();
		mdb.close();
	}
	public static Long getServerIdFromLocalID(final Long LocalId)
	{
		return hashLocalIDServerId.get(LocalId);

	}
	public static void fetchMeProfileIdFromState()
	{
        DatabaseHelper mdb = new DatabaseHelper(MainApplication.getContext());
        mSelfLocalContactId = StateTable.fetchMeProfileId(mdb.getReadableDatabase());
		mdb.close();

	}
	public static Long getMeProfileId()
	{
		return mSelfLocalContactId;
	}
	
	public static void setfeedsMinUpdatedTime(final long value) {
		feedsMinUpdatedTime = value;
	}
	
	public static long getfeedsMinUpdatedTime() {
		return feedsMinUpdatedTime;
	}
	
	/*
	 *  saving timeStamp when posts are the fetched from FB
	 */
	private static void savelatestTimeStamp(){
			SharedPreferences pref = MainApplication.getContext().getSharedPreferences("FACEBOOK_TIMESTAMP", 0);
			long latestPostTimestamp = getfeedsMinUpdatedTime();
			SharedPreferences.Editor edit = pref.edit();
			edit.putLong("TIMESTAMP", latestPostTimestamp);
			edit.commit();
			edit = null;
			pref = null;
	}
	
	private static ActivityItem updatePostAfterComment(final String activity_id, DatabaseHelper mdb) 
	{
        DatabaseHelper.trace(false, "DatabaseHelper.fetchFeedItemList()");

        String query = "SELECT * from " + ActivitiesTable.TABLE_NAME
        + " WHERE " + Field.ACTIVITY_ID + "=\"" + activity_id + "\"" ;
        
        Cursor cursor = mdb.getReadableDatabase().rawQuery(query, null); 
		cursor.moveToFirst();
		ActivityItem activityItem = new ActivityItem();
		
        activityItem.localActivityId =
            SqlUtils.setLong(cursor, Field.LOCAL_ACTIVITY_ID.toString(), null);
        activityItem.activityId =
            SqlUtils.setString(cursor, Field.ACTIVITY_ID.toString());
        activityItem.time =
            SqlUtils.setLong(cursor, Field.TIMESTAMP.toString(), null);
        activityItem.type =
            SqlUtils.setActivityItemType(cursor, Field.TYPE.toString());
        activityItem.uri = SqlUtils.setString(cursor, Field.URI.toString());
        activityItem.title =
            SqlUtils.setString(cursor, Field.TITLE.toString());
        activityItem.description =
            SqlUtils.setString(cursor, Field.DESCRIPTION.toString());
        activityItem.moreinfo =
            SqlUtils.setString(cursor, Field.MORE_INFO.toString());
        activityItem.previewUrl =
            SqlUtils.setString(cursor, Field.PREVIEW_URL.toString());
        activityItem.store =
            SqlUtils.setString(cursor, Field.STORE.toString());
        activityItem.activityFlags =
            SqlUtils.setInt(cursor, Field.FLAG.toString(), null);
        activityItem.parentActivity =
            SqlUtils.setString(cursor, Field.PARENT_ACTIVITY.toString());
        activityItem.hasChildren =
            SqlUtils.setBoolean(cursor, Field.HAS_CHILDREN.toString(),
                    activityItem.hasChildren);
        activityItem.visibilityFlags =
            SqlUtils.setInt(cursor, Field.VISIBILITY.toString(), null);
        activityItem.incoming =
            SqlUtils.setInt(cursor, Field.INCOMING.toString(), null);
                
        ActivityContact activityContact = new ActivityContact();
        ActivitiesTable.getQueryData(cursor, activityContact);
        
        
        cursor.close();
        
        int mNumberOfComments = 0;
        String[] str1 = null;
        String[] str2 = null;
		if(activityItem.moreinfo != null && activityItem.moreinfo.equalsIgnoreCase("") == false)
		{
			str1 = activityItem.moreinfo.split("COMMENT_COUNT:");
			if(str1 != null)
			{
				if(str1[1] != null)
				{
					str2 = str1[1].split(",");
					if(str2 != null)
					{
						mNumberOfComments = Integer.parseInt(str2[0]);
						mNumberOfComments ++;
					}
				}
			}
		}
		String newMoreInfo = new String();
		newMoreInfo = str1[0] + "COMMENT_COUNT:" + mNumberOfComments + "," + str2[1];	
		activityItem.moreinfo = newMoreInfo;
		List<ActivityContact> contactList = new ArrayList<ActivityContact>();
		contactList.add(activityContact);
		activityItem.contactList = contactList;
		//List<ActivityItem> activityList = new ArrayList<ActivityItem>();
		
		//activityList.add(activityItem);
		//mdb.removeDuplicatesFromDatabase(activityList);
		//ServiceStatus status  = mdb.addActivities(activityList);
		//activityList = null;
		return activityItem;
    }
}