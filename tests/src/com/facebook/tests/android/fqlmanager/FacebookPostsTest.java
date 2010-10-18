package com.facebook.android.fqlmanager.tests;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.fqlmanager.FacebookPosts;
import com.facebook.android.listener.BaseRequestListener;
import com.facebook.android.listener.RequestCompleteCallback;
import com.facebook.android.ui.example;

import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.util.Log;

public class FacebookPostsTest extends InstrumentationTestCase {
	
	private final static String LOG_TAG = "FacebookPostsTest";	
	private Facebook mFb;
    private static final String[] mPermissions =
        new String[] {"publish_stream", "read_stream", "offline_access", "user_photos", "friends_photos"};
    boolean postResult = false;
    String postResultString;

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	
    	mFb = new Facebook();
    	/*
    	 * First execute the application without junit testcase and login to the facebook 
    	 * account.
    	 */
        SessionStore.restore(mFb, getInstrumentation().getContext());
      
        if (mFb.isSessionValid()) {
            SessionEvents.onLogoutBegin();
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
            asyncRunner.logout(getInstrumentation().getContext(), new LogoutRequestListener());
        } else {
            mFb.authorize(getInstrumentation().getContext(), example.APP_ID, mPermissions,
                    new LoginDialogListener());
        }

    }
    
    @Override
    protected void tearDown() throws Exception {
    	
    	super.tearDown();
    }
    
    public void testFetchPosts() {
    	Log.e(LOG_TAG, " testFetchPosts [Starts]");
    	
    	postResult = false;
    	FacebookPosts.fetchPosts(mFb, new PostRequestCallBack() );
    	assertTrue(postResult);
    	if (!postResult) {
    		Log.e(LOG_TAG, " Error Value : ["+postResultString+"]");
    	}
    	    	
    	Log.e(LOG_TAG, " testFetchPosts [Ends]");
    }

    public void testAddCommentPass() {
    	Log.e(LOG_TAG, " testAddCommentPass [Starts]");
    	postResult = false;
    	/*
    	 * Set post_id manually for the success scenario.
    	 */
    	String post_id =  "737344117_425242874117";
    	String comment = ":-)";
    	FacebookPosts.addComment(mFb, post_id, comment ,new PostRequestCallBack() );
    	
    	assertTrue(postResult);
    	if (!postResult) {
    		Log.e(LOG_TAG, " Error Value : ["+postResultString+"]");
    	}
    	Log.e(LOG_TAG, " testAddCommentPass [Ends]");
    	
    }
   
    public void testAddCommentFail() {
    	Log.e(LOG_TAG, " testAddCommentFail [Starts]");
    	postResult = false;
    	/*
    	 * Set junk post_id for fail scenario.
    	 */
    	String post_id =  "1233444_99999999";
    	String comment = ":-)";
    	FacebookPosts.addComment(mFb, post_id, comment ,new PostRequestCallBack() );
    	assertTrue(!postResult);
    	if (!postResult) {
    		Log.e(LOG_TAG, " Error Value : ["+postResultString+"]");
    	}
    	Log.e(LOG_TAG, " testAddCommentFail [Ends]");
    }
    
    
    public void testAddLikePass(){
    	Log.e(LOG_TAG, " testAddLikePass [Starts]");
    	postResult = false;
    	/*
    	 * Set junk post_id for fail scenario.
    	 */
    	String post_id =  "737344117_425242874117";
    	FacebookPosts.addLike(mFb, post_id, new PostRequestCallBack() );
    	assertTrue(postResult);
    	if (!postResult) {
    		Log.e(LOG_TAG, " Error Value : ["+postResultString+"]");
    	}
    	Log.e(LOG_TAG, " testAddLikePass [Ends]");   	
    }
    
    public void testAddLikeFail(){
    	Log.e(LOG_TAG, " testAddLikeFail [Starts]");
    	postResult = false;
    	/*
    	 * Set junk post_id for fail scenario.
    	 */
    	String post_id =  "1233444_99999999";
    	FacebookPosts.addLike(mFb, post_id, new PostRequestCallBack() );
    	assertTrue(!postResult);
    	if (!postResult) {
    		Log.e(LOG_TAG, " Error Value : ["+postResultString+"]");
    	}
    	Log.e(LOG_TAG, " testAddLikeFail [Ends]");   	
    }
    
    public void testRemoveCommentPass() {
    	Log.e(LOG_TAG, " testRemoveCommentPass [Starts]");
    	postResult = false;
    	/*
    	 * Set post_id manually for the success scenario.
    	 */
    	String comment_id =  "737344117_425242874117";
    	FacebookPosts.removeComment(mFb, comment_id, new PostRequestCallBack() );
    	assertTrue(postResult);
    	if (!postResult) {
    		Log.e(LOG_TAG, " Error Value : ["+postResultString+"]");
    	}
    	Log.e(LOG_TAG, " testRemoveCommentPass [Ends]");      	
    }

    public void testRemoveCommentFail() {
    	Log.e(LOG_TAG, " testRemoveCommentFail [Starts]");
    	postResult = false;
    	
    	/*
    	 * Set post_id manually for the success scenario.
    	 */
    	String comment_id =  "737344117_425242800000";
    	FacebookPosts.removeComment(mFb, comment_id, new PostRequestCallBack() );
    	assertTrue(!postResult);
    	if (!postResult) {
    		Log.e(LOG_TAG, " Error Value : ["+postResultString+"]");
    	}
    	Log.e(LOG_TAG, " testRemoveCommentFail [Ends]");      	
    }

   /*
    * Callback function onComplete is called when request is complete 
    */
    private class PostRequestCallBack implements RequestCompleteCallback {
    	
    	public void onComplete(final boolean isSuccess, final Object data) {
    		postResult = isSuccess;
    		postResultString = (String) data;
    	}
    }
    
    /*
     * Sample classes required for setup sesssions
     */
    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
            SessionEvents.onLoginError(error.getMessage());
        }
        
        public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }
    
    private class LogoutRequestListener extends BaseRequestListener {
        public void onComplete(String response) {
        }
    }
    	
}
