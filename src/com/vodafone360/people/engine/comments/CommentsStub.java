package com.vodafone360.people.engine.comments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.tables.StateTable;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.engine.EngineManager;

public class CommentsStub {

    private DatabaseHelper mDatabaseHelper;

    public CommentsStub(MainApplication pMainApplication){
        mDatabaseHelper = pMainApplication.getDatabase();
    }
    
    public void stub(){
    	List<Comment> commentsList = new ArrayList<Comment>();
    	Comment comment = new Comment();
    	comment.mCommentId = null;
    	comment.mInappropriate = false;
    	comment.mText = "Test comment";
    	
    	EntityKey entityKey = new EntityKey();
	    	//entityKey.mEntityId = new Long(1089443);
    		entityKey.mEntityId = new Long(1117109);
	    	entityKey.mEntityType = "album";
	    	
	    	comment.mEntityKey = entityKey;
	    	commentsList.add(comment);
			
	    	EngineManager.getInstance().getCommentsEngine().addUiPostCommentRequest(commentsList);
    }
    
    public void deleteComment(){
    	Long ownerId = StateTable.fetchMeProfileId(mDatabaseHelper.getReadableDatabase());
    	long[] commentsList = new long[1];
    	commentsList[0] = new Long(122327);
    	Bundle data = new Bundle();
		data.putLongArray("commentidlist", commentsList);
		data.putLong("ownerid", ownerId);
		
	    EngineManager.getInstance().getCommentsEngine().addUiDeleteCommentRequest(data);
    }
    public void getComment()
    {
    	List<EntityKey> entitykeylist = new ArrayList<EntityKey>();
    	EntityKey entityKey = new EntityKey();
    	entityKey.mEntityId = new Long(1101033);
    	entityKey.mEntityType = "album";
       	entitykeylist.add(entityKey);
	   	EngineManager.getInstance().getCommentsEngine().addUiGetCommentRequest(entitykeylist);
    }
    
    public void updateCommment(){
    	List<Comment> commentsList = new ArrayList<Comment>();
    	Comment comment = new Comment();
    	comment.mCommentId = new Long(122547);
    	comment.mInappropriate = false;
    	comment.mText = "Update comment";
    	
    	EntityKey entityKey = new EntityKey();
	    	//entityKey.mEntityId = new Long(1089443);
    		entityKey.mEntityId = new Long(1101033);
	    	entityKey.mEntityType = "album";
	    	comment.mEntityKey = entityKey;
	    	commentsList.add(comment);
			
	    	EngineManager.getInstance().getCommentsEngine().addUiUpdateCommentRequest(commentsList);
    }
}
