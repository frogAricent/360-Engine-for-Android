
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

package com.vodafone360.people.engine.music;

import java.util.List;

import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.MusicTracksResponse;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.Music;
import com.vodafone360.people.utils.LogUtils;

/***
 * Widget engine for fetching recommended tracks of the user
 * 
 */
public class WidgetEngine extends BaseEngine {

	private MusicTracksResponse tracks = new MusicTracksResponse();
	
	/** engine's current state **/

	private State mState = State.IDLE;

	/**
	 * mutex for thread synchronization
	 */
	private Object mMutex = new Object();

	/**
	 * Definitions of WidgetEngine states; IDLE - engine is inactive
	 * TOP_ANONYMOUS_TRACKS: Fetching top anonymous tracks for the user.
	 * RECOMMENDED_TRACKS: Fetching top anonymous tracks for the user.
	 * 
	 */
	private enum State {
		IDLE, TOP_ANONYMOUS_TRACKS, RECOMMENDED_TRACKS
	}

	public WidgetEngine(IEngineEventCallback eventCallback) {
		super(eventCallback);
		mEngineId = EngineId.WIDGET_ENGINE;
	}

	/**
	 * Return next run time for WidgetEngine. Determined by whether we have a
	 * request we wish to issue, or there is a response that needs processing.
	 */
	@Override
	public long getNextRunTime() {
		if (isCommsResponseOutstanding()) {
			return 0;
		}
		if (isUiRequestOutstanding()) {
			return 0;
		}
		return getCurrentTimeout();
	}

	@Override
	public void onCreate() {
		LogUtils.logD("WidgetEngine.OnCreate()");
		mState = State.IDLE;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRequestComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onTimeoutEvent() {
		LogUtils.logD("WidgetEngine.onTimeoutEvent() in State: " + mState);
	}

	/**
	 * Handle an outstanding UI request.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void processUiRequest(ServiceUiRequest requestId, Object data) {
		LogUtils.logD("MusicEngine.processUiRequest() - reqID = " + requestId);
		switch (requestId) {
		case TOP_ANONYMOUS_TRACKS:
			startFetchTopTracks();
			break;
		case RECOMMENDED_TRACKS:
			startRecommendedTracks();
			break;
		default:
			completeUiRequest(ServiceStatus.ERROR_NOT_FOUND, null);
			break;
		}
	}

	/**
	 * Changes the state of the engine.
	 * 
	 * @param newState
	 *            The new state
	 */
	private void newState(State newState) {
		State oldState = mState;
		synchronized (mMutex) {
			mState = newState;
		}
		switch (mState) {
		case TOP_ANONYMOUS_TRACKS:
		case RECOMMENDED_TRACKS:
		case IDLE:
		default:
			break;
		}
		LogUtils.logV("WidgetEngine.newState(): " + oldState + " -> "
				+ mState);
	}
	
	/**
	 * Add request to Fetch recommended Track. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param
	 * @return void
	 */
	public void addUiRecommendedTrackReq() {
		LogUtils.logD("WidgetEngine.addUiRecommendedTrackReq()");
		addUiRequestToQueue(ServiceUiRequest.RECOMMENDED_TRACKS,null);
	}
	
	/**
	 * Add request to downloadable Track. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param
	 * @return void
	 */
	public void addUiTopTrackReq() {
		LogUtils.logD("WidgetEngine.addUiRecommendedTrackReq()");
		addUiRequestToQueue(ServiceUiRequest.TOP_ANONYMOUS_TRACKS,null);
	}
	
	
	/**
	 * Issues request to Start Top 20 Tracks Request. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 */
	private void startFetchTopTracks() {
		LogUtils.logD("WidgetEngine.startFetchTopTracks()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}

		newState(State.TOP_ANONYMOUS_TRACKS);
		if (!setReqId(Music.getTrackBlock(this))) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}
	}
	
	/**
	 * Issue request to get Music Recommended Tracks. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 */
	private void startRecommendedTracks() {
		LogUtils.logD("MusciEngine.startRecommendedTracks()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}

		newState(State.RECOMMENDED_TRACKS);
		if (!setReqId(Music.getRecommendedTrackBlock(this))) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}
	}
	
	/**
	 * Handle Server response to get recommended Tracks.
	 * 
	 * @param data List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleGetRecommendedTracks(List<BaseDataType> data) {
		LogUtils.logD("WidgetEngine.handleGetRecommendedTracks()");
		ServiceStatus errorStatus = getResponseStatus(BaseDataType.TRACKS_RESULTS, data);
		
		if (errorStatus == ServiceStatus.SUCCESS) {
			for (BaseDataType item : data) {
				if (BaseDataType.TRACKS_RESULTS == item.getType()) {
					tracks = (MusicTracksResponse) item;
					LogUtils.logD("Music id: "+ tracks.toString());
				} else {
					LogUtils.logE("WidgetEngine handleGetRecommendedTracks Unexpected response: "
							+ item.getType());
					return;
				}
			}
		} else {
			LogUtils
					.logE("WidgetEngine handleGetRecommendedTrack error status: "
							+ errorStatus.name());
			return;
		}
		completeUiRequest(errorStatus, tracks.recommendedTracksList);
        newState(State.IDLE);
	}
	
	
	/**
	 * Handle Server response to get TOP 20 Tracks.
	 * 
	 * @param data List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleGetTop20Tracks(List<BaseDataType> data) {
		LogUtils.logD("WidgetEngine.handleGetTop20Tracks()");
		ServiceStatus errorStatus = getResponseStatus(BaseDataType.TRACKS_RESULTS, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			for (BaseDataType item : data) {
				if (BaseDataType.TRACKS_RESULTS == item.getType()) {
					tracks = (MusicTracksResponse) item;
					LogUtils.logD("Widget id: "+ tracks.toString());
				} else {
					LogUtils.logE("WidgetEngine handleGetTop20Tracks Unexpected response: "
							+ item.getType());
					return;
				}
			}
		} else {
			LogUtils
					.logE("WidgetEngine handleGetTop20Tracks error status: "
							+ errorStatus.name());
			return;
		}
		
		completeUiRequest(errorStatus, tracks.recommendedTracksList);
        newState(State.IDLE);
	}
	
	/**
	 * Run function called via EngineManager. Should have a UI, Comms response
	 * or timeout event to handle.
	 */
	@Override
	public void run() {
		LogUtils.logD("WidgetEngine run");
		processTimeout();
		if (isCommsResponseOutstanding() && processCommsInQueue()) {
			return;
		}
		if (isUiRequestOutstanding()) {
			processUiQueue();
		}
	}

	/**
	 * Called when a server response is received, processes the response based
	 * on the engine state.
	 * 
	 * @param resp Response data from server
	 * @return null
	 */
	@Override
	protected void processCommsResponse(DecodedResponse resp) {
		LogUtils.logD("CommentsEngine processCommsResponse");
		switch (mState) {
		case TOP_ANONYMOUS_TRACKS:
			handleGetTop20Tracks(resp.mDataTypes);
			break;
		case RECOMMENDED_TRACKS:
			handleGetRecommendedTracks(resp.mDataTypes);
			break;
		default: // do nothing.
			break;
		}
	}
}
