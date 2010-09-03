package com.vodafone360.people.service.io.api;

/*
 ****************************************************************
 * Copyright (c) 2010 Aricent Technologies (Holdings) Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Aricent Technologies ("Confidential Information").You 
 * shall not disclose such Confidential Information and shall use 
 * it only in accordance with the terms of the license agreement 
 * you entered into with Aricent.
 ****************************************************************
 */
import java.util.List;

import android.util.Log;

import com.vodafone360.people.Settings;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;

/**
 * Implementation of Now+ Comments APIs
 */
public class Comments {
	private final static String FUNCTION_POST_COMMENT = "comments/addcomments";

	private final static String FUNCTION_DELETE_COMMENT = "comments/deletecomments";

	private final static String FUNCTION_GET_COMMENT = "comments/getcomments";

	private final static String FUNCTION_UPDATE_COMMENT = "comments/updatecomments";

	private final static String COMMENT_LIST = "commentlist";

	private final static String ENTITY_KEY_LIST = "entitykeylist";

	/**
	 * Implementation of comments/addcomments API. Parameters are; [auth], List
	 * <Comment> commentlist
	 * 
	 * @param engine
	 *            handle to CommentsEngine
	 * @param commentlist
	 *            List of Comments to be added
	 * @return request id generated for this request
	 */
	public static int postComment(BaseEngine engine, List<Comment> list) {
		if (engine == null) {
			throw new NullPointerException(
					"Auth.getPublicKey() engine cannot be NULL");
		}

		Request request = new Request(FUNCTION_POST_COMMENT,
				Request.Type.POST_COMMENT, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		request.addData(COMMENT_LIST, ApiUtils.createVectorOfComment(list));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of comments/deletecomments API. Parameters are; [auth],
	 * List <Comment> list, Long ownerId
	 * 
	 * @param engine
	 *            handle to CommentsEngine
	 * @param list
	 *            List of Comments to be deleted
	 * @param ownerId
	 *            Owner Id of the comment
	 * @return request id generated for this request
	 */
	public static int deleteComment(BaseEngine engine, List<Long> list,
			Long ownerId) {
		if (engine == null) {
			throw new NullPointerException(
					"Auth.getPublicKey() engine cannot be NULL");
		}

		Request request = new Request(FUNCTION_DELETE_COMMENT,
				Request.Type.DELETE_COMMENT, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);

		request.addData("ownerid", ownerId);
		request.addData("commentidlist", ApiUtils.createVectorOfLong(list));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of comments/getcomments API. Parameters are; [auth], List
	 * <EntityKey> entitykeylist
	 * 
	 * @param engine
	 *            handle to CommentsEngine
	 * @param entitykeylist
	 *            List of entities for which comments are retrieved
	 * @return request id generated for this request
	 */
	public static int getComment(BaseEngine engine,
			List<EntityKey> entitykeylist) {
		if (engine == null) {
			throw new NullPointerException(
					"Auth.getPublicKey() engine cannot be NULL");
		}

		Request request = new Request(FUNCTION_GET_COMMENT,
				Request.Type.GET_COMMENT, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		request.addData(ENTITY_KEY_LIST, ApiUtils
				.createVectorOfEntityKey(entitykeylist));
		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of comments/updatecomments API. Parameters are; [auth],
	 * List <Comment> list
	 * 
	 * @param engine
	 *            handle to CommentsEngine
	 * @param list
	 *            List of Comments to be updated
	 * @return request id generated for this request
	 */
	public static int updateComment(BaseEngine engine, List<Comment> list) {
		if (engine == null) {
			throw new NullPointerException(
					"Auth.getPublicKey() engine cannot be NULL");
		}

		Request request = new Request(FUNCTION_UPDATE_COMMENT,
				Request.Type.UPDATE_COMMENT, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		request.addData(COMMENT_LIST, ApiUtils.createVectorOfComment(list));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}
}
