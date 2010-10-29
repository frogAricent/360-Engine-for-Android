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

package com.vodafone360.people.engine.content;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;

import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.datatypes.Album;
import com.vodafone360.people.datatypes.AlbumListResponse;
import com.vodafone360.people.datatypes.AlbumResponse;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Content;
import com.vodafone360.people.datatypes.ContentListResponse;
import com.vodafone360.people.datatypes.ContentResponse;
import com.vodafone360.people.datatypes.ExternalResponseObject;
import com.vodafone360.people.datatypes.ServerError;
import com.vodafone360.people.datatypes.SystemNotification;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.Contents;
import com.vodafone360.people.service.transport.IConnection;
import com.vodafone360.people.service.transport.http.content.ContentManager;
import com.vodafone360.people.utils.LogUtils;

/***
 * Content engine for downloading and uploading all kind of content (pictures,
 * videos, files)
 * <p>
 * File Name : ContentEngine.java
 * <p>
 * Description : This class extends BaseEngine and contain various overridden
 * methods.
 * <p>
 * Revision History
 * <p>
 * ------------------------------------------------------------------------
 * <p>
 * Date Author SPR-Id Version Comments
 * <p>
 * - - 0.01 Initial Release
 * <p>
 */
public class ContentEngine extends BaseEngine {

	/**
	 * Constructor for the ContentEngine.
	 * 
	 * @param eventCallback
	 *            IEngineEventCallback for calling the constructor of the super
	 *            class
	 * @param dbHelper
	 *            Instance of DatabaseHelper
	 */
	public ContentEngine(final IEngineEventCallback eventCallback,
			final DatabaseHelper dbHelper) {
		super(eventCallback);
		this.mDbHelper = dbHelper;
		mEngineId = EngineId.CONTENT_ENGINE;
	}

	private ContentManager mContentManager = null;

	private IConnection mConnection;

	/**
	 * States for ContentEngine. States are based on the requests that the
	 * engine needs to handle.
	 */
	private enum State {
		IDLE, ADDING_ALBUM, UPDATING_ALBUM, PUBLISHING_ALBUM, DELETING_ALBUM, GETTING_ALBUM, ADDING_CONTENT_TO_ALBUM, DELETING_CONTENT_FROM_ALBUM, GETTING_CONTENT, UPLOADING_CONTENT, UPLOADING_AND_PUBLISHING, DOWNLOADING_CONTENT, PUBLISHING_CONTENT, DELETING_CONTENT
	}

	/**
	 * mutex for thread synchronization
	 */
	private Object mMutex = new Object();

	/**
	 * Stores the current state of the engine
	 */
	private State mState = State.IDLE;

	/**
	 * Queue with unprocessed ContentObjects.
	 */
	private FiFoQueue mUnprocessedQueue = new FiFoQueue();

	/**
	 * Queue with ContentObjects for downloads.
	 */
	private FiFoQueue mDownloadQueue = new FiFoQueue();

	/**
	 * Queue with ContentObjects for uploads.
	 */
	private FiFoQueue mUploadQueue = new FiFoQueue();

	/**
	 * Instance of DatabaseHelper.
	 */
	private DatabaseHelper mDbHelper;

	public boolean mJUnitTestMode = false ;
	/**
	 * Hashtable to match requests to ContentObjects.
	 */
	private Hashtable<Integer, ContentObject> requestContentObjectMatchTable = new Hashtable<Integer, ContentObject>();

	/** List array of Albums retrieved from Server. */
	private ArrayList<AlbumResponse> mAlbumList = new ArrayList<AlbumResponse>();

	/** Album retrieved from Server. */
	private AlbumListResponse mAlbumResponse = new AlbumListResponse();

	/** List array of Content IDs retrieved from Server. */
	private ArrayList<ContentListResponse> mContentIdList = new ArrayList<ContentListResponse>();

	/** List array of Content binaries retrieved from Server. */
	private ArrayList<ContentResponse> mContentResponseList = new ArrayList<ContentResponse>();

	/**
	 * Getter for the local instance of DatabaseHelper.
	 * 
	 * @return local instance of DatabaseHelper
	 */
	public final DatabaseHelper getDatabaseHelper() {
		return mDbHelper;
	}

	/**
	 * Processes one ContentObject.
	 * 
	 * @param co
	 *            ContentObject to be processed
	 */
	public final void processContentObject(final ContentObject co) {
		mUnprocessedQueue.add(co);
	}

	/**
	 * Iterates over the ContentObject list and processes every element.
	 * 
	 * @param list
	 *            List with ContentObjects which are to be processed
	 */
	public final void processContentObjects(final List<ContentObject> list) {
		for (ContentObject co : list) {
			processContentObject(co);
		}
	}

	/**
	 * Processes the main queue and splits it into the download and upload
	 * queues.
	 */
	private void processQueue() {
		ContentObject co;
		// picking unprocessed ContentObjects
		while ((co = mUnprocessedQueue.poll()) != null) {
			// putting them to downloadqueue ...
			if (co.getDirection() == ContentObject.TransferDirection.DOWNLOAD) {
				mDownloadQueue.add(co);
			} else {
				// ... or the uploadqueue
				mUploadQueue.add(co);
			}
		}
	}

	/**
	 * Determines the next RunTime of this Engine It first processes the
	 * in-queue and then look.
	 * 
	 * @return time in milliseconds from now when the engine should be run
	 */
	@Override
	public final long getNextRunTime() {
		processQueue();
		// if there are CommsResponses outstanding, run now
		if (isUiRequestOutstanding()) {
			return 0;
		}
		if (isCommsResponseOutstanding()) {
			return 0;
		}
		return (mDownloadQueue.size() + mUploadQueue.size() > 0) ? 0 : -1;
	}

	/**
	 * Empty implementation without function at the moment.
	 */
	@Override
	public void onCreate() {
		mContentManager = new ContentManager(this);
		mConnection = new ContentManager(this);
		mConnection.startThread();
		QueueManager.getInstance().addQueueListener(mConnection);

		mContentManager.start();
	}

	/**
	 * Empty implementation without function at the moment.
	 */
	@Override
	public void onDestroy() {
	}

	/**
	 * Empty implementation without function at the moment.
	 */
	@Override
	protected void onRequestComplete() {
	}

	/**
	 * Empty implementation without function at the moment.
	 */
	@Override
	protected void onTimeoutEvent() {
	}

	/**
	 * Processes the response Finds the matching contentobject for the repsonse
	 * using the id of the response and sets its status to done. At last the
	 * TransferComplete method of the ContentObject is called.
	 * 
	 * @param resp
	 *            Response object that has been processed
	 */
	@Override
	protected final void processCommsResponse(final DecodedResponse resp) {
		LogUtils.logD("ContentEngine processCommsResponse");
		switch (mState) {
		case ADDING_ALBUM:
			handleAddAlbumResponse(resp.mDataTypes);
			break;
		case DELETING_ALBUM:
			handleDeleteAlbumResponse(resp.mDataTypes);
			break;
		case GETTING_ALBUM:
			handleGetAlbumResponse(resp.mDataTypes);
			break;
		case UPDATING_ALBUM:
			handleUpdateAlbumResponse(resp.mDataTypes);
			break;
		case ADDING_CONTENT_TO_ALBUM:
			handleAddContentToAlbumResponse(resp.mDataTypes);
			break;
		case DELETING_CONTENT_FROM_ALBUM:
			handleDeleteContentFromAlbumResponse(resp.mDataTypes);
			break;
		case PUBLISHING_ALBUM:
			handlePublishAlbumResponse(resp.mDataTypes);
			break;
		case UPLOADING_CONTENT:
			handleUploadContentResponse(resp.mDataTypes);
			break;
		case GETTING_CONTENT:
			handleGetContentResponse(resp.mDataTypes);
			break;
		case PUBLISHING_CONTENT:
			handlePublishContentResponse(resp.mDataTypes);
			break;
		case DELETING_CONTENT:
			handleDeleteContentResponse(resp.mDataTypes);
			break;
		default: // do nothing.
			break;
		}
		ContentObject co = requestContentObjectMatchTable.remove(resp.mReqId);

		if (co == null) { // check if we have an invalid response
			return;
		}

		List<BaseDataType> mDataTypes = resp.mDataTypes;
		// Sometimes it is null or empty
		if (mDataTypes == null || mDataTypes.size() == 0) {
			co.setTransferStatus(ContentObject.TransferStatus.ERROR);
			RuntimeException exc = new RuntimeException(
					"Empty response returned");
			co.getTransferListener().transferError(co, exc);
			return;
		}

		Object data = mDataTypes.get(0);
		if (mDataTypes.get(0).getType() == BaseDataType.SERVER_ERROR_DATA_TYPE
				|| mDataTypes.get(0).getType() == BaseDataType.SYSTEM_NOTIFICATION_DATA_TYPE) {
			co.setTransferStatus(ContentObject.TransferStatus.ERROR);
			RuntimeException exc = new RuntimeException(data.toString());
			co.getTransferListener().transferError(co, exc);
		} else {
			co.setTransferStatus(ContentObject.TransferStatus.DONE);
			co.setExtResponse((ExternalResponseObject) data);
			co.getTransferListener().transferComplete(co);
		}

		switch (mState) {
		case ADDING_ALBUM:
			handleAddAlbumResponse(resp.mDataTypes);
			break;
		case DELETING_ALBUM:
			handleDeleteAlbumResponse(resp.mDataTypes);
			break;
		case GETTING_ALBUM:
			handleGetAlbumResponse(resp.mDataTypes);
			break;
		case UPDATING_ALBUM:
			handleUpdateAlbumResponse(resp.mDataTypes);
			break;
		case ADDING_CONTENT_TO_ALBUM:
			handleAddContentToAlbumResponse(resp.mDataTypes);
			break;
		case DELETING_CONTENT_FROM_ALBUM:
			handleDeleteContentFromAlbumResponse(resp.mDataTypes);
			break;
		case PUBLISHING_ALBUM:
			handlePublishAlbumResponse(resp.mDataTypes);
			break;
		case UPLOADING_CONTENT:
			handleUploadContentResponse(resp.mDataTypes);
			break;
		case GETTING_CONTENT:
			handleGetContentResponse(resp.mDataTypes);
			break;
		case PUBLISHING_CONTENT:
			handlePublishContentResponse(resp.mDataTypes);
			break;
		case DELETING_CONTENT:
			handleDeleteContentResponse(resp.mDataTypes);
			break;
		case UPLOADING_AND_PUBLISHING:
			handleUploadContentAndPublishResponse(resp.mDataTypes);
			break;
		default: // do nothing.
			break;
		}
	}

	/**
	 * Empty implementation of abstract method from BaseEngine.
	 */
	@Override
	protected void processUiRequest(final ServiceUiRequest requestId,
			final Object data) {
		LogUtils
				.logD("ContentEngine.processUiRequest() - reqID = " + requestId);
		switch (requestId) {
		case ADD_ALBUM:
			startAddAlbum((List<Album>) data);
			break;
		case DELETE_ALBUM:
			startDeleteAlbum((List<Long>) data);
			break;
		case GET_ALBUM:
			startGetAlbum((List<Long>) data);
			break;
		case UPDATE_ALBUM:
			startUpdateAlbum((List<Album>) data);
			break;
		case ADD_CONTENT_TO_ALBUM:
			startAddContentToAlbum(data);
			break;
		case DELETE_CONTENT_FROM_ALBUM:
			startDeleteContentFromAlbum(data);
			break;
		case PUBLISH_ALBUM:
			startPublishAlbum(data);
			break;
		case UPLOAD_CONTENT:
			startUploadContent((List<Content>) data);
			break;
		case GET_CONTENT:
			startGetContent(data);
			break;
		case PUBLISH_CONTENT:
			startPublishContent(data);
			break;
		case DELETE_CONTENT:
			startDeleteContent((List<Long>) data);
			break;
		case UPLOAD_CONTENT_AND_PUBLISH:
			startUploadContentAndPublish((List<Content>) data);
		default:
			completeUiRequest(ServiceStatus.ERROR_NOT_FOUND, null);
			break;
		}
	}

	/**
	 * run method of this engine iterates over the downloadqueue, makes requests
	 * out of ContentObjects and puts them into QueueManager queue.
	 */
	@Override
	public final void run() {
		if (isCommsResponseOutstanding() && processCommsInQueue()) {
			return;
		}
		if (processTimeout()) {
			return;
		}
		if (isUiRequestOutstanding()) {
			processUiQueue();
		}
		ContentObject co;
		boolean queueChanged = false;
		while ((co = mDownloadQueue.poll()) != null) {
			queueChanged = true;
			// set the status of this contentobject to transferring
			co.setTransferStatus(ContentObject.TransferStatus.TRANSFERRING);
			Request request = new Request(co.getUrl().toString(), co
					.getUrlParams(), engineId());
			QueueManager.getInstance().addRequest(request);
			// important: later we will match done requests back to the
			// contentobject using this map
			requestContentObjectMatchTable.put(request.getRequestId(), co);
		}
		if (queueChanged) {
			QueueManager.getInstance().fireQueueStateChanged();
		}
	}

	/**
	 * Change current IdentityEngine state.
	 * 
	 * @param newState
	 *            new state.
	 */
	private void newState(State newState) {
		State oldState = mState;
		synchronized (mMutex) {
			if (newState == mState) {
				return;
			}
			mState = newState;
		}
		LogUtils.logV("ContentEngine.newState: " + oldState + " -> " + mState);
	}

	/**
	 * Add request to add albums. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param albumList
	 *            Contains the list of Albums to be uploaded
	 * @return void
	 */
	public void addUiAddAlbumRequest(List<Album> albumlist) {
		LogUtils.logD("ContentsEngine.addUiAddAlbumRequest()");
		addUiRequestToQueue(ServiceUiRequest.ADD_ALBUM, albumlist);
	}

	/**
	 * Add request to delete albums. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param albumList
	 *            Contains the list of Album Ids to be deleted
	 * @return void
	 */
	public void addUiDeleteAlbumRequest(Object data) {
		LogUtils.logD("ContentsEngine.addUiDeleteAlbumRequest()");
		addUiRequestToQueue(ServiceUiRequest.DELETE_ALBUM, data);
	}

	/**
	 * Add request to get albums. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param albumList
	 *            Contains the list of Album Ids to be fetched
	 * @return void
	 */
	public void addUiGetAlbumRequest(Object data) {
		LogUtils.logD("ContentsEngine.addUiGetAlbumRequest()");
		addUiRequestToQueue(ServiceUiRequest.GET_ALBUM, data);
	}

	/**
	 * Add request to update albums. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param albumList
	 *            Contains the list of Albums to be updated
	 * @return void
	 */
	public void addUiUpdateAlbumRequest(List<Album> albumlist) {
		LogUtils.logD("ContentsEngine.addUiUpdateAlbumRequest()");
		addUiRequestToQueue(ServiceUiRequest.UPDATE_ALBUM, albumlist);
	}

	/**
	 * Add request to add content to albums. The request is added to the UI
	 * request and processed when the engine is ready.
	 * 
	 * @param data
	 *            Bundle containing the list of contents and album Id
	 * @return void
	 */
	public void addUiAddContentToAlbumRequest(Object data) {
		LogUtils.logD("ContentsEngine.addUiAddContentToAlbumRequest()");
		addUiRequestToQueue(ServiceUiRequest.ADD_CONTENT_TO_ALBUM, data);
	}

	/**
	 * Add request to delete contents from albums. The request is added to the
	 * UI request and processed when the engine is ready.
	 * 
	 * @param data
	 *            Bundle containing the list of contents and album Id
	 * @return void
	 */
	public void addUiDeleteContentFromAlbumRequest(Object data) {
		LogUtils.logD("ContentsEngine.addUiDeleteContentFromAlbumRequest()");
		addUiRequestToQueue(ServiceUiRequest.DELETE_CONTENT_FROM_ALBUM, data);
	}

	/**
	 * Add request to publish a list of albums. The request is added to the UI
	 * request and processed when the engine is ready.
	 * 
	 * @param data
	 *            Bundle containing the list of albums and the community id
	 * @return void
	 */
	public void addUiPublishAlbumRequest(Object data) {
		LogUtils.logD("ContentsEngine.addUiPublishAlbumRequest()");
		addUiRequestToQueue(ServiceUiRequest.PUBLISH_ALBUM, data);
	}

	/**
	 * Add request to upload a list of contents. The request is added to the UI
	 * request and processed when the engine is ready.
	 * 
	 * @param contentlist
	 *            The list of contents to be uploaded
	 * @return void
	 */
	public void addUiUploadContentRequest(List<Content> contentlist) {
		LogUtils.logD("ContentsEngine.addUiUploadContentRequest()");
		addUiRequestToQueue(ServiceUiRequest.UPLOAD_CONTENT, contentlist);
	}

	/**
	 * Add request to delete a list of contents. The request is added to the UI
	 * request and processed when the engine is ready.
	 * 
	 * @param contentlist
	 *            The list of contents to be deleted
	 * @return void
	 */
	public void addUiDeleteContentRequest(List<Long> contentlist) {
		LogUtils.logD("ContentsEngine.addUiDeleteContentRequest()");
		addUiRequestToQueue(ServiceUiRequest.DELETE_CONTENT, contentlist);
	}

	/**
	 * Add request to upload content and publish. The request is added to the UI
	 * request and processed when the engine is ready.
	 * 
	 * @param contentlist
	 *            The list of contents to be uploaded
	 * @return void
	 */
	public void addUiUploadContentAndPublishRequest(List<Content> contentlist) {
		LogUtils.logD("ContentsEngine.addUiUploadContentAndPublishRequest	()");
		addUiRequestToQueue(ServiceUiRequest.UPLOAD_CONTENT_AND_PUBLISH,
				contentlist);
	}

	/**
	 * Add request to publish contents to a community. The request is added to
	 * the UI request and processed when the engine is ready.
	 * 
	 * @param data
	 *            Bundle containing the list of contents to be published and the
	 *            community id
	 * @return void
	 */
	public void addUiPublishContentRequest(Object data) {
		LogUtils.logD("ContentsEngine.addUiPublishContentRequest()");
		addUiRequestToQueue(ServiceUiRequest.PUBLISH_CONTENT, data);
	}

	/**
	 * Add request to get contents. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param contentlist
	 *            The list of contents to be downloaded
	 * @return void
	 */
	public void addUiGetContentRequest(Object data) {
		LogUtils.logD("ContentsEngine.addUiGetContentRequest()");
		addUiRequestToQueue(ServiceUiRequest.GET_CONTENT, data);
	}

	/**
	 * Issue request to Add albums. (Request is not issued if there is currently
	 * no connectivity).
	 * 
	 * @param albumList
	 *            Albums list.
	 * @return void
	 */
	private void startAddAlbum(List<Album> albumList) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.ADDING_ALBUM);
		if (!setReqId(Contents.addAlbum(this, albumList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to Delete albums. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param albumList
	 *            Albums list.
	 * @return void
	 */
	private void startDeleteAlbum(List<Long> albumList) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.DELETING_ALBUM);
		if (!setReqId(Contents.deleteAlbum(this, albumList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to Get albums. (Request is not issued if there is currently
	 * no connectivity).
	 * 
	 * @param albumList
	 *            Albums list.
	 * @return void
	 */
	private void startGetAlbum(List<Long> albumList) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.GETTING_ALBUM);
		if (!setReqId(Contents.getAlbums(this, albumList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to Update albums. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param albumList
	 *            Albums list.
	 * @return void
	 */
	private void startUpdateAlbum(List<Album> albumList) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.UPDATING_ALBUM);
		if (!setReqId(Contents.updateAlbum(this, albumList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to delete contents from album. (Request is not issued if
	 * there is currently no connectivity).
	 * 
	 * @param data
	 *            Bundle containing the list of contents and the album Id
	 * @return void
	 */
	private void startDeleteContentFromAlbum(Object data) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.DELETING_CONTENT_FROM_ALBUM);
		Bundle b = (Bundle) data;
		Long albumId = b.getLong("aid");
		long contentids[] = b.getLongArray("contentids");

		List<Long> contentIdList = new ArrayList<Long>();
		for (int i = 0; i < contentids.length; i++) {
			contentIdList.add(new Long(contentids[i]));
		}

		if (!setReqId(Contents.deleteContentFromAlbum(this, contentIdList,
				albumId))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to add contents to album. (Request is not issued if there
	 * is currently no connectivity).
	 * 
	 * @param data
	 *            Bundle containing the list of contents and the album Id
	 * @return void
	 */
	private void startAddContentToAlbum(Object data) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.ADDING_CONTENT_TO_ALBUM);
		Bundle b = (Bundle) data;
		Long albumId = b.getLong("aid");
		long contentids[] = b.getLongArray("contentids");

		List<Long> contentIdList = new ArrayList<Long>();
		for (int i = 0; i < contentids.length; i++) {
			contentIdList.add(new Long(contentids[i]));
		}

		if (!setReqId(Contents.addContentToAlbum(this, contentIdList, albumId))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to Publish albums. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param data
	 *            Bundle containing the list of albums and the community id
	 * @return void
	 */
	private void startPublishAlbum(Object data) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.PUBLISHING_ALBUM);
		Bundle b = (Bundle) data;
		String communityId = b.getString("commid");
		long albumIds[] = b.getLongArray("albumids");

		List<Long> albumList = new ArrayList<Long>();
		for (int i = 0; i < albumIds.length; i++) {
			albumList.add(new Long(albumIds[i]));
		}

		if (!setReqId(Contents.publishAlbum(this, albumList, communityId))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to add contents. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param contentList
	 *            List of contents to be added
	 * @return void
	 */
	private void startUploadContent(List<Content> contentList) {
		System.out.println("ContentEngine.startUploadContent()");
		if (!checkConnectivity()) {
			return;
		}
		newState(State.UPLOADING_CONTENT);
		if (!setReqId(Contents.uploadContent(this, contentList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to add content and publish. (Request is not issued if there
	 * is currently no connectivity).
	 * 
	 * @param contentList
	 *            The list of contents to be added
	 * @return void
	 */
	private void startUploadContentAndPublish(List<Content> contentList) {
		System.out.println("ContentEngine.startUploadContentAndPublish()");
		if (!checkConnectivity()) {
			return;
		}
		newState(State.UPLOADING_AND_PUBLISHING);
		if (!setReqId(Contents.uploadContentAndPublish(this, contentList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to Publish contents. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param data
	 *            Bundle containing the list of contents and the community id
	 * @return void
	 */
	private void startPublishContent(Object data) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.PUBLISHING_CONTENT);
		Bundle b = (Bundle) data;
		String communityId = b.getString("commid");
		long contentIds[] = b.getLongArray("contentids");

		List<Long> contentList = new ArrayList<Long>();
		for (int i = 0; i < contentIds.length; i++) {
			contentList.add(new Long(contentIds[i]));
		}

		if (!setReqId(Contents.publishContent(this, contentList, communityId))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to Publish contents. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param data
	 *            Bundle containing the list of contents and filter list
	 * @return void
	 */
	private void startGetContent(Object data) {
		if (!checkConnectivity()) {
			return;
		}
		ArrayList<Long> contentIdList = new ArrayList<Long>();
		long contentIdArray[] = ((Bundle) data).getLongArray("contentidlist");
		if (contentIdArray != null) {
			System.out.println("Content id array not null");
			for (int i = 0; i < contentIdArray.length; i++) {
				contentIdList.add(contentIdArray[i]);
			}
		}

		newState(State.GETTING_CONTENT);
		if (!setReqId(Contents.getContents(this, contentIdList,
				prepareStringFilter((Bundle) data)))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Issue request to delete contents. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param contentList
	 *            The list of contents to be deleted
	 * @return void
	 */
	private void startDeleteContent(List<Long> contentList) {
		if (!checkConnectivity()) {
			return;
		}
		newState(State.DELETING_CONTENT);
		if (!setReqId(Contents.deleteContent(this, contentList))) {
			completeUiRequest(ServiceStatus.ERROR_BAD_SERVER_PARAMETER);
		}
	}

	/**
	 * Prepares the list of filters
	 * 
	 * @param filter
	 *            Bundle containing the filters
	 * @return Map<String , List<String>> of contentidlist
	 */
	private static Map<String, List<String>> prepareStringFilter(Bundle filter) {
		Map<String, List<String>> returnFilter = null;
		if (filter != null && filter.keySet().size() > 0) {
			returnFilter = new Hashtable<String, List<String>>();
			for (String key : filter.keySet()) {
				if (!key.equals("contentidlist"))
					returnFilter.put(key, filter.getStringArrayList(key));
			}
		} else {
			returnFilter = null;
		}
		return returnFilter;
	}

	/**
	 * Handle Server response to add albums request. The response is a list of
	 * Album ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleAddAlbumResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleAddAlbumResponse()");
		// ServiceStatus errorStatus =
		// genericHandleResponseType("AlbumResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ALBUM_RESPONSE_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mAlbumList.clear();
			for (BaseDataType item : data) {
				Log.d("handleAddAlbumResponse", "Reading Data");
				if (BaseDataType.ALBUM_RESPONSE_DATATYPE == item.getType()) {
					mAlbumList.add((AlbumResponse) item);
					LogUtils.logD("Album id: "
							+ ((AlbumResponse) item).mAlbumList);
				} else {
					LogUtils
							.logE("ContentEngine handleAddAlbumResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils.logE("ContentEngine handleAddAlbumResponse error status: "
					+ errorStatus.name());
		}
		newState(State.IDLE);
	}

	/**
	 * Handle Server response to get albums request.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleGetAlbumResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleGetAlbumResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("AlbumListResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ALBUM_LIST_RESPONSE_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			for (BaseDataType item : data) {
				if (BaseDataType.ALBUM_LIST_RESPONSE_DATATYPE == item.getType()) {
					mAlbumResponse = (AlbumListResponse) item;
					LogUtils.logD("Album id: " + mAlbumResponse.mAlbumList);
				} else {
					LogUtils
							.logE("ContentEngine handleGetAlbumResponse unexpected type");
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils.logE("ContentEngine handleGetAlbumResponse error status: "
					+ errorStatus.name());
		}
		completeUiRequest(ServiceStatus.SUCCESS);
		newState(State.IDLE);
	}

	/**
	 * Handle Server response to update albums request.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleUpdateAlbumResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleUploadAlbumResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("AlbumResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ALBUM_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mAlbumList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.ALBUM_RESPONSE_DATATYPE == item.getType()) {
					mAlbumList.add((AlbumResponse) item);
					LogUtils.logD("Album id: "
							+ ((AlbumResponse) item).mAlbumList);
				} else {
					LogUtils
							.logE("ContentEngine handleUploadAlbumResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleUploadAlbumResponse error status: "
							+ errorStatus.name());
		}
		completeUiRequest(ServiceStatus.SUCCESS);
		newState(State.IDLE);
	}

	/**
	 * Handle Server response to delete albums request. The response is a list
	 * of Album ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleDeleteAlbumResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleDeleteAlbumResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("AlbumResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ALBUM_RESPONSE_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mAlbumList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.ALBUM_RESPONSE_DATATYPE == item.getType()) {
					mAlbumList.add((AlbumResponse) item);
					LogUtils.logD("Album id: "
							+ ((AlbumResponse) item).mAlbumList);
				} else {
					LogUtils
							.logE("ContentEngine handleDeleteAlbumResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleDeleteAlbumResponse error status: "
							+ errorStatus.name());
		}
		completeUiRequest(ServiceStatus.SUCCESS);
		newState(State.IDLE);
	}

	/**
	 * Handle Server response to delete contents from albums request. The
	 * response is a list of Content ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleDeleteContentFromAlbumResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleDeleteContentFromAlbumResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("ContentListResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mContentIdList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE == item
						.getType()) {
					mContentIdList.add((ContentListResponse) item);
					LogUtils.logD("Content id: "
							+ ((ContentListResponse) item).mContentIdList);
				} else {
					LogUtils
							.logE("ContentEngine handleDeleteContentFromAlbumResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleDeleteContentFromAlbumResponse error status: "
							+ errorStatus.name());
		}
		newState(State.IDLE);
		completeUiRequest(ServiceStatus.SUCCESS);
	}

	/**
	 * Handle Server response to add content to albums request. The response is
	 * a list of Content ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleAddContentToAlbumResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleAddContentToAlbumResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("ContentListResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mContentIdList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE == item
						.getType()) {
					mContentIdList.add((ContentListResponse) item);
					LogUtils.logD("Content id: "
							+ ((ContentListResponse) item).mContentIdList);
				} else {
					LogUtils
							.logE("ContentEngine handleAddContentToAlbumResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleAddContentToAlbumResponse error status: "
							+ errorStatus.name());
		}
		newState(State.IDLE);
		completeUiRequest(ServiceStatus.SUCCESS);
	}

	/**
	 * Handle Server response to delete albums request. The response is a list
	 * of Album ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handlePublishAlbumResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handlePublishAlbumResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("AlbumResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.ALBUM_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mAlbumList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.ALBUM_RESPONSE_DATATYPE == item.getType()) {
					mAlbumList.add((AlbumResponse) item);
					LogUtils.logD("Album id: "
							+ ((AlbumResponse) item).mAlbumList);
				} else {
					LogUtils
							.logE("ContentEngine handlePublishAlbumResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handlePublishAlbumResponse error status: "
							+ errorStatus.name());
		}
		completeUiRequest(ServiceStatus.SUCCESS);
		newState(State.IDLE);

	}

	/**
	 * Handle Server response to add contents request. The response is a list of
	 * contents ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleUploadContentResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleUploadContentResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("ContentListResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mContentIdList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE == item
						.getType()) {
					mContentIdList.add((ContentListResponse) item);
					LogUtils.logD("Content id: "
							+ ((ContentListResponse) item).mContentIdList);
				} else {
					LogUtils
							.logE("ContentEngine handleUploadContentResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleUploadImageResponse error status: "
							+ errorStatus.name());
		}
		newState(State.IDLE);
		completeUiRequest(ServiceStatus.SUCCESS);
	}

	/**
	 * Handle Server response to upload contents and publish request. The
	 * response is a list of content ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleUploadContentAndPublishResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleUploadContentAndPublishResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("ContentListResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mContentIdList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE == item
						.getType()) {
					mContentIdList.add((ContentListResponse) item);
					LogUtils.logD("Content id: "
							+ ((ContentListResponse) item).mContentIdList);
				} else {
					LogUtils
							.logE("ContentEngine handleUploadContentAndPublishResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleUploadContentAndPublishResponse error status: "
							+ errorStatus.name());
		}
		newState(State.IDLE);
		completeUiRequest(ServiceStatus.SUCCESS);
	}

	/**
	 * Handle Server response to get contents. The response is a list of
	 * contents if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleGetContentResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleGetContentResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("ContentResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.CONTENT_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			for (BaseDataType item : data) {
				if (BaseDataType.CONTENT_RESPONSE_DATATYPE == item.getType()) {
					mContentResponseList.add((ContentResponse) item);
					LogUtils.logD("Album id: " + mContentResponseList);
				} else {
					LogUtils
							.logE("ContentEngine handleGetContentResponse Unexpected response");
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleGetContentResponse error status: "
							+ errorStatus.name());
		}
		completeUiRequest(ServiceStatus.SUCCESS);
		newState(State.IDLE);
	}

	/**
	 * Handle Server response to publish contents. The response is a list of
	 * content ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handlePublishContentResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handlePublishContentResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("ContentListResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mContentIdList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE == item
						.getType()) {
					mContentIdList.add((ContentListResponse) item);
					LogUtils.logD("Content id: "
							+ ((ContentListResponse) item).mContentIdList);
				} else {
					LogUtils
							.logE("ContentEngine handlePublishContentResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handlePublishContentResponse error status: "
							+ errorStatus.name());
		}
		newState(State.IDLE);
		completeUiRequest(ServiceStatus.SUCCESS);
	}

	/**
	 * Handle Server response to delete contents. The response is a list of
	 * content ids if successful or -1 in case of error
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleDeleteContentResponse(List<BaseDataType> data) {
		LogUtils.logD("ContentsEngine.handleDeleteContentResponse()");

		// ServiceStatus errorStatus =
		// genericHandleResponseType("ContentListResponse", data);
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE, data);

		if (errorStatus == ServiceStatus.SUCCESS) {
			//Print the response
			mContentIdList.clear();
			for (BaseDataType item : data) {
				if (BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE == item
						.getType()) {
					mContentIdList.add((ContentListResponse) item);
					LogUtils.logD("Content id: "
							+ ((ContentListResponse) item).mContentIdList);
				} else {
					LogUtils
							.logE("ContentEngine handleDeleteContentResponse Unexpected response");
					completeUiRequest(ServiceStatus.ERROR_UNEXPECTED_RESPONSE);
					return;
				}
			}
			//TODO:Handling of response from the server to be implemented
		} else {
			LogUtils
					.logE("ContentEngine handleDeleteContentResponse error status: "
							+ errorStatus.name());
		}
		newState(State.IDLE);
		completeUiRequest(ServiceStatus.SUCCESS);
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

	public boolean isUploading() {
		return mState == State.UPLOADING_CONTENT;
	}

	public boolean isUploadingAndPublishing() {
		return mState == State.UPLOADING_AND_PUBLISHING;
	}

	public boolean isDownloadingContent() {
		return mState == State.GETTING_CONTENT;
	}
	/**
	 * Sets the test mode flag. Used to bypass dependency with other modules
	 * while unit testing
	 */
	public void setTestMode(boolean mode) {
		mJUnitTestMode = mode;
	}

}
