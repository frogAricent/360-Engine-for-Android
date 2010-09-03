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
import java.util.Map;

import android.util.Log;

import com.vodafone360.people.Settings;
import com.vodafone360.people.datatypes.Album;
import com.vodafone360.people.datatypes.Content;
import com.vodafone360.people.engine.content.ContentEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.utils.LogUtils;

/**
 * Implementation of Now+ Contents APIs
 */
public class Contents {

	private final static String FUNCTION_ADD_ALBUM = "content/addalbums";

	private final static String FUNCTION_DELETE_ALBUM = "content/deletealbum";

	private final static String FUNCTION_GET_ALBUM = "content/getalbum";

	private final static String FUNCTION_UPDATE_ALBUM = "content/updatealbum";

	private final static String FUNCTION_ADD_CONTENT_TO_ALBUM = "content/addca";

	private final static String FUNCTION_DELETE_CONTENT_FROM_ALBUM = "content/delca";

	private final static String FUNCTION_PUBLISH_ALBUM = "content/publishalbums";

	private final static String FUNCTION_ADD_CONTENT = "content/addcontents";

	private final static String FUNCTION_ADD_CONTENT_AND_PUBLISH = "content/addcontentsandpublish";

	private final static String FUNCTION_PUBLISH_CONTENT = "content/publishfiles";

	private final static String FUNCTION_DELETE_CONTENT = "content/deletecontents";

	private final static String FUNCTION_GET_CONTENT = "content/getcontents";

	/**
	 * Implementation of content/addalbums API. Parameters are; [auth], List
	 * <Content> list
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param albumlist
	 *            List of Albums to be added
	 * @return request id generated for this request
	 */
	public static int addAlbum(ContentEngine contentEngine,
			List<Album> albumList) {
		Request request = new Request(FUNCTION_ADD_ALBUM,
				Request.Type.ADD_ALBUM, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (albumList != null) {
			request.addData("albumlist", ApiUtils
					.createVectorOfAlbum(albumList));
		}
		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/getalbums API. Parameters are; [auth], List
	 * <Content> list
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param albumlist
	 *            List of Albums to be retrieved
	 * @return request id generated for this request
	 */
	public static int getAlbums(ContentEngine contentEngine,
			List<Long> albumList) {
		Request request = new Request(FUNCTION_GET_ALBUM,
				Request.Type.GET_ALBUM, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (albumList != null) {
			request.addData("albumidlist", ApiUtils
					.createVectorOfLong(albumList));
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/updatealbums API. Parameters are; [auth], List
	 * <Content> list
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param albumlist
	 *            List of Albums to be updated
	 * @return request id generated for this request
	 */
	public static int updateAlbum(ContentEngine contentEngine,
			List<Album> albumList) {

		Request request = new Request(FUNCTION_UPDATE_ALBUM,
				Request.Type.UPDATE_ALBUM, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (albumList != null) {
			request.addData("albumlist", ApiUtils
					.createVectorOfAlbum(albumList));
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/deletealbums API. Parameters are; [auth], List
	 * <Content> list
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param albumlist
	 *            List of Albums to be deleted
	 * @return request id generated for this request
	 */
	public static int deleteAlbum(ContentEngine contentEngine,
			List<Long> albumList) {
		Request request = new Request(FUNCTION_DELETE_ALBUM,
				Request.Type.DELETE_ALBUM, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (albumList != null) {
			request.addData("albumidlist", ApiUtils
					.createVectorOfLong(albumList));
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/addContentToAlbum API. Parameters are; [auth],
	 * List<Long> contentList, Long albumId
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param contentList
	 *            List of contents to be added
	 * @param albumId
	 *            Album to add the contents
	 * @return request id generated for this request
	 */
	public static int addContentToAlbum(ContentEngine contentEngine,
			List<Long> contentList, Long albumId) {
		Request request = new Request(FUNCTION_ADD_CONTENT_TO_ALBUM,
				Request.Type.ADD_CONTENT_TO_ALBUM, contentEngine.engineId(),
				false, Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (contentList != null) {
			request.addData("contentids", ApiUtils
					.createVectorOfLong(contentList));
		}
		if (albumId != null) {
			request.addData("aid", albumId);
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/deleteContentFromAlbum API. Parameters are;
	 * [auth], List <Content> list
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param contentList
	 *            List of contents to be deleted
	 * @param albumId
	 *            Album to delete the contents from
	 * @return request id generated for this request
	 */
	public static int deleteContentFromAlbum(ContentEngine contentEngine,
			List<Long> contentList, Long albumId) {
		Request request = new Request(FUNCTION_DELETE_CONTENT_FROM_ALBUM,
				Request.Type.DELETE_CONTENT_FROM_ALBUM, contentEngine
						.engineId(), false, Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (contentList != null) {
			request.addData("cidl", ApiUtils.createVectorOfLong(contentList));
		}
		if (albumId != null) {
			request.addData("aid", albumId);
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/publishalbums API. Parameters are; [auth],
	 * List<Long> albumList, String communityId
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param albumlist
	 *            List of Albums to be published
	 * @param communityId
	 *            Community to publish albums
	 * @return request id generated for this request
	 */
	public static int publishAlbum(ContentEngine contentEngine,
			List<Long> albumList, String communityId) {
		Request request = new Request(FUNCTION_PUBLISH_ALBUM,
				Request.Type.PUBLISH_ALBUM, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (albumList != null) {
			request.addData("albumids", ApiUtils.createVectorOfLong(albumList));
		}
		if (communityId != null) {
			request.addData("communityid", communityId);
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/getcontents API. Parameters are; [auth],
	 * List<Long> contentIdList, Map<String, List<String>> filterlist
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param contentIdList
	 *            List of contents to be downloaded
	 * @param filterlist
	 *            Map of the filter list
	 * @return request id generated for this request
	 */
	public static int getContents(ContentEngine contentEngine,
			List<Long> contentIdList, Map<String, List<String>> filterlist) {
		Request request = new Request(FUNCTION_GET_CONTENT,
				Request.Type.GET_CONTENT, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);

		if (contentIdList != null) {
			request.addData("contentidlist", ApiUtils
					.createVectorOfLong(contentIdList));
		}
		if (filterlist != null) {
			request.addData("filterlist", ApiUtils.createHashTable(filterlist));
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/uploadContents API. Parameters are; [auth],
	 * List<Content> contentList
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param contentList
	 *            List of contents to be uploaded
	 * @return request id generated for this request
	 */
	public static int uploadContent(ContentEngine contentEngine,
			List<Content> contentList) {
		Request request = new Request(FUNCTION_ADD_CONTENT,
				Request.Type.UPLOAD_CONTENT, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (contentList != null) {
			request.addData("contentlist", ApiUtils
					.createVectorOfContent(contentList));
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/uploadContentAndPublish API. Parameters are;
	 * [auth], List<Content> contentList
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param contentList
	 *            List of contents to be uploaded
	 * @param commid
	 *            The community to publish the contents
	 * @return request id generated for this request
	 */
	public static int uploadContentAndPublish(ContentEngine contentEngine,
			List<Content> contentList) {
		Request request = new Request(FUNCTION_ADD_CONTENT_AND_PUBLISH,
				Request.Type.UPLOAD_CONTENT_AND_PUBLISH, contentEngine
						.engineId(), false, Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (contentList != null) {
			request.addData("contentlist", ApiUtils
					.createVectorOfContent(contentList));
		}
		request.addData("commid", "facebook.com");

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/publishContents API. Parameters are; [auth],
	 * List<Long> contentList, String communityId
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param contentList
	 *            List of contents to be uploaded
	 * @param commid
	 *            The community to publish the contents
	 * @return request id generated for this request
	 */
	public static int publishContent(ContentEngine contentEngine,
			List<Long> contentList, String communityId) {
		Request request = new Request(FUNCTION_PUBLISH_CONTENT,
				Request.Type.PUBLISH_CONTENT, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (contentList != null) {
			request.addData("contentids", ApiUtils
					.createVectorOfLong(contentList));
		}
		if (communityId != null) {
			request.addData("commid", communityId);
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of content/deleteContents API. Parameters are; [auth],
	 * List<Long> contentList, String communityId
	 * 
	 * @param engine
	 *            handle to ContentsEngine
	 * @param contentList
	 *            List of contents to be deleted
	 * @return request id generated for this request
	 */
	public static int deleteContent(ContentEngine contentEngine,
			List<Long> contentList) {
		Request request = new Request(FUNCTION_DELETE_CONTENT,
				Request.Type.DELETE_CONTENT, contentEngine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		if (contentList != null) {
			request.addData("contentids", ApiUtils
					.createVectorOfLong(contentList));
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}
}
