package com.vodafone360.people.tests.engine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.vodafone360.people.MainApplication;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.MusicDDForTrack;
import com.vodafone360.people.datatypes.MusicDDObject;
import com.vodafone360.people.datatypes.MusicDownloadableTrack;
import com.vodafone360.people.engine.EngineManager;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.music.MusicDownloader;
import com.vodafone360.people.engine.music.MusicEngine;
import com.vodafone360.people.engine.music.MusicEngine.IMusicSyncObserver;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.utils.LogUtils;

/**
 * 
 * File Name : MusicEngineTest.java Description : This class implements
 * IEngineTestFrameworkObserver interface and contain various callback methods.
 * 
 * Revision History
 * -------------------------------------------------------------- Date Author
 * SPR-Id Version Comments 01-Sep-10 - - 0.01 Initial Release
 * 
 * 
 */
public class MusicEngineTest extends InstrumentationTestCase implements
		IEngineTestFrameworkObserver,Observer{

	/**
	 * ***************************************************************** States
	 * for CommentsEngine. States are based on the requests that the engine
	 * needs to handle.
	 ****************************************************************** 
	 */

	private enum MusicState {
		IDLE,
		DOWNLOADABLE_TRACK,
		DOWNLOADABLE_TRACK_FAIL,
		DD_FOR_TRACK,
		DD_FOR_TRACK_FAIL,
		GET_NEXT_RUNTIME
	}

	private EngineTestFramework mEngineTester = null;
	private MusicEngine mEng = null;
	private MainApplication mApplication = null;
	private MusicState mState = MusicState.IDLE;
	private static DatabaseHelper mDatabaseHelper = null;
	EngineManager mEngineManager = null;

	/**
	 ****************************************************************** 
	 * Method to initialize all the resources
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mEngineTester = new EngineTestFramework(this);
		mEng = new MusicEngine(mEngineTester);

		mApplication = (MainApplication) Instrumentation.newApplication(
				MainApplication.class, getInstrumentation().getTargetContext());
		
		mEngineTester.setEngine(mEng);
		mState = MusicState.IDLE;
		
		mEngineManager= EngineManager.createEngineManagerForTest(null ,mEngineTester);
        mEngineManager.addEngineForTest(mEng);
        
        mEng.setTestMode(true);
	}

	/**
	 ******************************************************************* 
	 * Method to release all the resoures
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@Override
	protected void tearDown() throws Exception {
		mEngineTester.stopEventThread();
		mEngineTester = null;
		mEng = null;
		// call at the end!!!
		super.tearDown();
	}

	/**
	 ******************************************************************* 
	 * Method to test getNextruntime() for Music engine
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testGetNextRuntime() {
		boolean testPass = true;
		mState = MusicState.GET_NEXT_RUNTIME;
		long runtime = mEng.getNextRunTimeForTest();
		if (runtime != -1) {
			testPass = false;
		}

		assertTrue("testGetNextRuntime() failed", testPass);
		LogUtils.logI("**** testGetNextRuntime (SUCCESS) ****\n");
	}

	/*public void testInstallNotify()
	{
		final MusicDDObject input = new MusicDDObject();
		input.setName("Won't Back Down");
		input.setInstallNotifyURI("http://mlprx-v1.mlayer.prd.sp.vodafone.com/VodafoneMusicLayer/mlinn/9e12c7187f6c8e3142608f6fe732f28b");
		input.setFileType("audio/3gpp");
		input.setTrackID("DE-17209803");
		input.setDownloadURL("http://mlprx-v1.mlayer.prd.sp.vodafone.com/VodafoneMusicLayer/mldnl/e96bc758ab9ebc9345813b8f6f5a30ff");
		input.setIconURI("http://mlprx-v1.mlayer.prd.sp.vodafone.com/VodafoneMusicLayer/mlimg/?path=album%2F600x600%2F486%2F274329686_328581724_20100616075731%2Ejpg&mimetype=jpg&width=600&height=600&t=1287386735360&mlWW=0&mlWH=0&mlRT=SCALE");
		input.setSuppressUserConfirmation(getName());
		input.setSize(1);
		

		MusicDownloader data1 = new MusicDownloader(input);
		data1.setFilePath("e:\\Xml\\");
		data1.setFileName("sample1");
		data1.setFileExtn("3gp");
		data1.setId("DE-17209803");
		data1.installNotify();
	}
	public void testDownloadTrack()
	{
		final MusicDDObject input = new MusicDDObject();
		input.setName("Won't Back Down");
		input.setInstallNotifyURI("http://google.com");
		input.setFileType("audio/3gpp");
		input.setTrackID("DE-17209803");
		input.setDownloadURL("http://google.com");
		input.setIconURI("http://google.com");
		input.setSuppressUserConfirmation(getName());
		input.setSize(1);
		

		MusicDownloader data1 = new MusicDownloader(input);
		data1.setFilePath("e:\\Xml\\");
		data1.setFileName("sample1");
		data1.setFileExtn("3gp");
		data1.setId("DE-17209803");
		mEng.downloadTrack(input);
	}*/
	/**
	 ******************************************************************* 
	 * Method to test to downloadable track 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testDownloadableTrack() {
		mState = MusicState.DOWNLOADABLE_TRACK;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			mEng.addUiDownloadableTrackReq();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** testDownloadableTrack (SUCCESS) ****\n");
	}

	/**
	 ******************************************************************* 
	 * Method to test to DDForTrack track 
	 * 
	 * @param : null
	 * @return : null
	 ******************************************************************* 
	 */
	@MediumTest
	public void testDDForTrack() {
		mState = MusicState.DD_FOR_TRACK;
		NetworkAgent.setAgentState(NetworkAgent.AgentState.CONNECTED);
		try {
			List<String> trackIds = new ArrayList<String>();
			trackIds.add("DE-17209806");
			MyObserver observer = new MyObserver();
			mEng.setObserver(observer);
			mEng.addDDForTrackReq(trackIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceStatus status = mEngineTester.waitForEvent();
		assertEquals(ServiceStatus.SUCCESS, status);
		LogUtils.logI("**** testDownloadableTrack (SUCCESS) ****\n");
	}

	@MediumTest
	public void testUpdate() throws FileNotFoundException {
		final MusicDDObject input = new MusicDDObject();
		input.setName("Won't Back Down");
		input.setInstallNotifyURI("http://mlprx-v1.mlayer.prd.sp.vodafone.com/VodafoneMusicLayer/mlinn/9e12c7187f6c8e3142608f6fe732f28b");
		input.setFileType("audio/3gpp");
		input.setTrackID("DE-17209803");
		input.setDownloadURL("http://mlprx-v1.mlayer.prd.sp.vodafone.com/VodafoneMusicLayer/mldnl/e96bc758ab9ebc9345813b8f6f5a30ff");
		input.setIconURI("http://mlprx-v1.mlayer.prd.sp.vodafone.com/VodafoneMusicLayer/mlimg/?path=album%2F600x600%2F486%2F274329686_328581724_20100616075731%2Ejpg&mimetype=jpg&width=600&height=600&t=1287386735360&mlWW=0&mlWH=0&mlRT=SCALE");
		input.setSuppressUserConfirmation(getName());
		input.setSize(1);
		
		MusicDownloader data1 = new MusicDownloader(input);
		data1.setFilePath("e:\\Xml\\");
		data1.setFileName("sample1");
		data1.setFileExtn("3gp");
		data1.setId("DE-17209803");
		MyObserver observer = new MyObserver();
		mEng.setObserver(observer);
		mEng.update(data1, input);
		
		
	}
	class MyObserver implements IMusicSyncObserver
	{
		@Override
		public void onDownloadTrack(String trackid, String status) {
			 Log.d("MUSIC",
                       "MusicRestoreController.startRestore().new IMusicSyncObserver() {...}.onDownloadTrack()"
                               + trackid + " status " + status);
			
		}
		@Override
		public void onGetdownloadableTrackList(
				List<MusicDDObject> MusicDataOject, String status) {
			Log.d("MUSIC", "onGetdownloadableTrackList " + MusicDataOject);
			
		}
		@Override
		public void onProgressEvent(float percent) {
			 Log.d("MUSIC",
                   "MusicRestoreController.startRestore().new IMusicSyncObserver() {...}.onProgressEvent()"
                           + percent);
			
		}
		
	}
	/**
	 * Method to read file as String 
	 * @param filePath
	 * @return
	 * @throws java.io.IOException
	 */
	private static String readFileAsString(String filePath) throws java.io.IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}
	
	@Override
	public void reportBackToEngine(int reqId, EngineId engine) {
		Log.d("TAG", "LocationEngineTest.reportBackToEngine");
		ResponseQueue respQueue = ResponseQueue.getInstance();
		List<BaseDataType> data = new ArrayList<BaseDataType>();

		switch (mState) {
		case IDLE:
			break;
		case DOWNLOADABLE_TRACK:
			
			Log.d("TAG", "MusicEngineTest.reportBackToEngine Downloadable Comment");
			MusicDownloadableTrack track = new MusicDownloadableTrack(BaseDataType.DOWNLOADABLE_MUSIC);
			track.resultCode = "result";
			track.success = true;
			List<String> mList = new ArrayList<String>();
			mList.add("DE-17209806");
			track.trackIdList = mList;
			data.add(track);
            respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.DOWNLOADABLE_TRACK.ordinal()));
            mEng.onCommsInMessage();
            break;

		case DD_FOR_TRACK:
			Log.d("TAG", "MusicEngineTest.reportBackToEngine Downloadable Comment");
			MusicDDForTrack track1 = new MusicDDForTrack(BaseDataType.DD_FOR_TRACKS);
			track1.ddResultCode = "resultcode";
			track1.ddResultText = "resultText";
			try {
				track1.downloadDescriptor = readFileAsString("\\data\\sample.xml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			data.add(track1);
            respQueue.addToResponseQueueFromTest(new DecodedResponse(reqId, data, engine, DecodedResponse.ResponseType.DD_FOR_TRACK.ordinal()));
            mEng.onCommsInMessage();
            break;
		default:
			break;
		}

	}

	@Override
	public void onEngineException(Exception exp) {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(Observable observable, Object data) {
		
	}


}