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
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.vodafone360.people.datatypes.Content;
/*
 *  Class handling the facebook feeds request.
 */
public class FacebookEventHandler {

    private static MainApplication mApplication;
	private static FQLConnection mFQLConnection;
	private static int maxPostLimit;
	private static long fetchPostScheduleTime;
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

	/*
	 * Facebook commands TAGS
	 */
	private static final String FILTER_KEY = "filter_key";
	private static final String LIMIT = "limit";
	private static final String START_TIME = "start_time";
	private static final String POST_ID = "post_id";
	private static final String COMMENT = "comment";
	private static final String COMMENT_ID = "comment_id";
	
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
					
					Long startTime = pref.getLong("TIMESTAMP", 0);
					if(startTime < 0){
						startTime = 0l;
					}
					try {
						maxPostLimit = Integer.parseInt(
								SettingsManager.getProperty(Settings.FB_MAX_POSTS_KEY));
						linkAppKey = SettingsManager.getProperty(Settings.FB_LINK_APP_KEY);
						photoAppKey = SettingsManager.getProperty(Settings.FB_PHOTO_APP_KEY);
						statusAppKey = SettingsManager.getProperty(Settings.FB_STATUS_APP_KEY);

						Bundle filter_id = new Bundle();
						filter_id.putString(FILTER_KEY, photoAppKey);
						filter_id.putString(LIMIT, "" + maxPostLimit);
						filter_id.putString(START_TIME, "" + startTime);
						mFQLConnection.request(STREAM_GET, filter_id,
										new FeedListener());
						filter_id.putString(FILTER_KEY, linkAppKey);
						mFQLConnection.request(STREAM_GET, filter_id,
										new FeedListener());
						filter_id.putString(FILTER_KEY, statusAppKey);
						mFQLConnection.request(STREAM_GET, filter_id,
										new FeedListener());
						if (callBack != null) {
							callBack.onComplete(true, null);
						}

						pref = null;

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
				parameter.putString(COMMENT, comment);
				mFQLConnection.request(ADD_COMMENT, parameter,
						new AddCommentListener(callBack));
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
//	/**
//	 *  API to start fetch post scheduler
//	 */
//	public static void fetchPostSchedulerStart ()
//	{
//		if (tm == null) {
//			fetchPostScheduleTime = Long.parseLong( SettingsManager.getProperty
//					(Settings.FB_POST_SCHEDULE_TIME));
//			tm = new Timer();
//			tm.schedule( new TimerTask () {
//	        @Override
//	        public void run () {
//	        	fetchPosts(null);
//	        }
//	       }
//			, 0, fetchPostScheduleTime);
//		}
//	}
//	/**
//	 *  API to stop fetch post scheduler
//	 */
//	public static void fetchPostSchedulerStop ()
//	{
//		if (tm != null) {
//			tm.cancel();
//		}
//	}
}
