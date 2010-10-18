
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.MusicDDForTrack;
import com.vodafone360.people.datatypes.MusicDDObject;
import com.vodafone360.people.datatypes.MusicDownloadableTrack;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.Music;
import com.vodafone360.people.utils.LogUtils;

/***
 * Music engine for restoring purchased music of the user
 * 
 */
public class MusicEngine extends BaseEngine implements Observer{

	public static final String DOWNLOADABLE_TRACK = "downloadble_music_track";
	public static final String DD_FOR_TRACK = "dd_for_track";
	public static final String DOWNLOAD_TRACK = "download_track";

	private MusicDownloadableTrack musicDownloadableTrack = new MusicDownloadableTrack();
	private MusicDDForTrack musicDDForTrack= new MusicDDForTrack();
	
	public static final String STATUSES[] = {"Downloading", "Paused", "Complete", "Cancelled", "Error"};
	public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    
    private int retryCount = 2;
    
    public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	
	public static interface IGetTrackIDs{
		void onGettingTrackIDs(IGetTrackIDs iGetTrackIds);
	}
	
	/** engine's current state **/

	private State mState = State.IDLE;

	/**
	 * mutex for thread synchronization
	 */
	private Object mMutex = new Object();

	/**
	 * Definitions of Music engines states; IDLE - engine is inactive
	 * RESTORE_MUSIC: Restoring purchased music of the user.
	 * 
	 */
	private enum State {
		IDLE, DOWNLOADABLE_TRACK, DD_FOR_TRACK
	}

	public MusicEngine(IEngineEventCallback eventCallback) {
		super(eventCallback);
		mEngineId = EngineId.MUSIC_ENGINE;
	}

	/**
	 * Return next run time for MusicEnginebak. Determined by whether we have a
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
		LogUtils.logD("MusicEnginebak.OnCreate()");
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
		LogUtils.logD("MusicEnginebak.onTimeoutEvent() in State: " + mState);
	}

	/**
	 * Handle an outstanding UI request.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void processUiRequest(ServiceUiRequest requestId, Object data) {
		LogUtils.logD("MusicEnginebak.processUiRequest() - reqID = " + requestId);
		switch (requestId) {
		case DOWNLOADABLE_TRACKS:
			startDownloadableTracks();
			break;
		case DD_FOR_TRACK:
			List<String> trackIds = (List<String>) data;
			startDDForTrack(trackIds);
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
		case DOWNLOADABLE_TRACK:
		case DD_FOR_TRACK:
		case IDLE:
		default:
			break;
		}
		LogUtils.logV("MusciEngine.newState(): " + oldState + " -> "
				+ mState);
	}

	/**
	 * Add request to downloadable Track. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param
	 * @return void
	 */
	public void addUiDownloadableTrackReq() {
		LogUtils.logD("MusicEnginebak.addUiDownloadableTrackReq()");
		addUiRequestToQueue(ServiceUiRequest.DOWNLOADABLE_TRACKS,null);
	}
	
	/**
	 * Add request to get DD for Track. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param
	 * @return void
	 */
	public void addDDForTrackReq(Object trackIds) {
		LogUtils.logD("MusicEnginebak.addUiDDForTrackReq()");
		addUiRequestToQueue(ServiceUiRequest.DD_FOR_TRACK,trackIds);
	}

	
	/**
	 * Issue request to Start Music Restore. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 */
	private void startDownloadableTracks() {
		LogUtils.logD("MusciEngine.startDownloadableTracks()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			error();
			return;
		}

		newState(State.DOWNLOADABLE_TRACK);
		if (!setReqId(Music.getDownloadableTrackBlock(this))) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			error();
			return;
		}
	}
	
	/**
	 * Issue request to DD For Track. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 */
	private void startDDForTrack(List<String> trackIds) {
		LogUtils.logD("MusciEngine.startDDForTrack()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			error();
			return;
		}

		newState(State.DD_FOR_TRACK);
		if (!setReqId(Music.getDDForTracks(this,trackIds))) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			error();
			return;
		}
	}

	/**
	 * Handle Server response to for Downloaded Music Tracks.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleGetDownloadableTracks(List<BaseDataType> data) {
		
		LogUtils.logD("MusicEnginebak.handleGetCommentResponse()");
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.DOWNLOADABLE_MUSIC, data);
		
		
		if (errorStatus == ServiceStatus.SUCCESS) {
			for (BaseDataType item : data) {
				if (BaseDataType.DOWNLOADABLE_MUSIC == item.getType()) {
					musicDownloadableTrack = (MusicDownloadableTrack) item;
					LogUtils.logD("Music Track Ids: "
							+ musicDownloadableTrack.trackIdList.toString());
					
				} else {
					LogUtils
							.logE("MusicEnginebak handleGetDownloadableTracks Unexpected response: "
									+ item.getType());
					error();
					return;
				}
			}
		} else {
			LogUtils
					.logE("MusicEnginebak handleGetDownloadableTracks error status: "
							+ errorStatus.name());
			error();
		}
		
//		this.addDDForTrackReq(musicDownloadableTrack.trackIdList);
		startDDForTrack(musicDownloadableTrack.trackIdList);
	}

	/**
	 * Handle Server response to for get DD for Tracks.
	 * 
	 * @param data
	 *            List of BaseDataTypes generated from Server response.
	 * @return void
	 */
	private void handleGetDDForTracks(List<BaseDataType> data) {
		
		LogUtils.logD("MusicEnginebak.handleGetDDForTracks()");
		ServiceStatus errorStatus = getResponseStatus(
				BaseDataType.DD_FOR_TRACKS, data);
		
		if (errorStatus == ServiceStatus.SUCCESS) {
			
			for (BaseDataType item : data) {
				if (BaseDataType.DD_FOR_TRACKS == item.getType()) {
					musicDDForTrack = (MusicDDForTrack) item;
					LogUtils.logD("Music id: "
							+ musicDDForTrack.downloadDescriptor.toString());
				} else {
					LogUtils
							.logE("MusicEnginebak handleGetDDForTracks Unexpected response: "
									+ item.getType());
					error();
					return;
				}
			}
		} else {
			LogUtils
					.logE("MusicEnginebak handleGetDDForTracks error status: "
							+ errorStatus.name());
			error();
			return;
		}

		String downloadDescriptor = musicDDForTrack.downloadDescriptor.toString();
		if(!(downloadDescriptor == null || "".equalsIgnoreCase(downloadDescriptor))){
			MusicDDParser ddParser = new MusicDDParser();		
			InputStream is = new ByteArrayInputStream(downloadDescriptor.getBytes());
			List<MusicDDObject> listOfDownloadableObject = ddParser.parse(is);
			
			mobserver.onGetdownloadableTrackList(listOfDownloadableObject,STATUSES[COMPLETE]);
			
//			downloadTrack(listOfDownloadableObject.get(0));
		}else{
			List<MusicDDObject> listOfDD = new ArrayList<MusicDDObject>();
			mobserver.onGetdownloadableTrackList(listOfDD,STATUSES[COMPLETE]);
		}
	}

	
	/**
	 * Run function called via EngineManager. Should have a UI, Comms response
	 * or timeout event to handle.
	 */
	@Override
	public void run() {
		LogUtils.logD("MusicEnginebak run");
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
	 * @param resp
	 *            Response data from server
	 * @return null
	 */
	@Override
	protected void processCommsResponse(DecodedResponse resp) {
		LogUtils.logD("CommentsEngine processCommsResponse");
		switch (mState) {
		case DOWNLOADABLE_TRACK:
			handleGetDownloadableTracks(resp.mDataTypes);
			break;
		case DD_FOR_TRACK:
			handleGetDDForTracks(resp.mDataTypes);
			break;
		default: // do nothing.
			break;
		}
	}

	
	
	private void downloadAllTrack(List<MusicDDObject> listOfDownloadableObject){
		
		
		for (MusicDDObject musicDDObject : listOfDownloadableObject) {
			try {

				MusicDownloader downloader = new MusicDownloader(musicDDObject);
				downloader.addObserver(this);
				downloader.downloadFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	/**
	 * there can be only one observer for the music restore
	 */
	public IMusicSyncObserver mobserver;
    public void addListener(IMusicSyncObserver observer){
          mobserver = observer;
     }
    
    public static interface IMusicSyncObserver {
          
          void onProgressEvent(float percent);
          
          void onDownloadTrack(String trackid, String status);
          
          void onGetdownloadableTrackList(List<MusicDDObject> MusicDataOject, String status);
          
     }
	
    List<MusicDDObject> musiclist=null;
	public void setdownloadableTrackList(List<MusicDDObject> list) {  
        
		musiclist = list;
    }
    public List<MusicDDObject> getdownloadableTrackList() {  
        
        return musiclist;
    }
    
    @Override
	public void update(Observable obj, Object data) {
		MusicDownloader downloader = (MusicDownloader)obj;
		if(downloader.getStatus() == MusicDownloader.DOWNLOADING){
			mobserver.onProgressEvent(downloader.getProgress());
		}
		else if(downloader.getStatus() == MusicDownloader.COMPLETE){
			mobserver.onDownloadTrack(downloader.getId(), MusicDownloader.STATUSES[MusicDownloader.COMPLETE]);
			broadcastMediaScannerConnection(downloader);
			
		}else if(downloader.getStatus() == MusicDownloader.ERROR){
			mobserver.onDownloadTrack(downloader.getId(), MusicDownloader.STATUSES[MusicDownloader.ERROR]);			
		}
		else if(downloader.getStatus() == MusicDownloader.CANCELLED){
			mobserver.onDownloadTrack(downloader.getId(), MusicDownloader.STATUSES[MusicDownloader.CANCELLED]);			
		}
		else if(downloader.getStatus() == MusicDownloader.PAUSED){
			mobserver.onDownloadTrack(downloader.getId(), MusicDownloader.STATUSES[MusicDownloader.PAUSED]);			
		}
	}
    
	private void broadcastMediaScannerConnection(MusicDownloader downloader) {
		// Tell the media scanner about the new file so that it is
		// immediately available to the user.
		
//		filePath+"/"+fileName+"."+fileExtn;
		final String path = (downloader.getFilePath()+"/"+downloader.getFileName()+"."+downloader.getFileExtn()).toString();
		
		MediaScannerConnection.scanFile(MainApplication.getContext(), new String[]{path}, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String path, Uri uri) {
						// TODO Auto-generated method stub
						LogUtils.logD("ExternalStorage" + "Scanned " + path
								+ ":");
						LogUtils.logD("ExternalStorage" + "-> uri=" + uri);
					}
				});
	}
    

	public void downloadTrack(MusicDDObject musicDDObject){
    	try {
    		
			MusicDownloader downloader = new MusicDownloader(musicDDObject);
			downloader.addObserver(this);
			downloader.downloadFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	private void error() {
    	if(getRetryCount()<0){
    		mobserver.onGetdownloadableTrackList(null,STATUSES[ERROR]);
    	}else{
    		retryCount = retryCount - 1;
    		setRetryCount(retryCount);
    		startDownloadableTracks();
    	}
        
    }

}
