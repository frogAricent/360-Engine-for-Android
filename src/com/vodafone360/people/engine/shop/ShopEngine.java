package com.vodafone360.people.engine.shop;

import java.util.List;

import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.ItemBlockRequest;
import com.vodafone360.people.datatypes.ItemBlockResponse;
import com.vodafone360.people.datatypes.ItemBlockResponseList;
import com.vodafone360.people.datatypes.MusicTracksResponse;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager;
import com.vodafone360.people.engine.BaseEngine.IEngineEventCallback;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.service.ServiceUiRequest;
import com.vodafone360.people.service.agent.NetworkAgent;
import com.vodafone360.people.service.io.ResponseQueue.DecodedResponse;
import com.vodafone360.people.service.io.api.Comments;
import com.vodafone360.people.service.io.api.Shop;
import com.vodafone360.people.utils.LogUtils;

public class ShopEngine extends BaseEngine {

	 /**
     * All engines must set this field to a unique ID
     */
    protected EngineId mEngineId = EngineId.SHOP_ENGINE;

    /**
     * Callback provided by {@link EngineManager}
     */
    protected IEngineEventCallback mEventCallback;

	/** engine's current state **/

	private State mState = State.IDLE;

	/**
	 * mutex for thread synchronization
	 */
	private Object mMutex = new Object();
	
	private ItemBlockResponseList blocks = new ItemBlockResponseList();

	/**
	 * Definitions of ShopEngine states; IDLE - engine is inactive
	 * GET_ITEM_BLOCK : Allows to retrieve a list of itemblocks
	 * 
	 */
	private enum State {
		IDLE, GET_ITEM_BLOCK
	}

	public ShopEngine(IEngineEventCallback mUiEventCallback) {
		super(mUiEventCallback);
		mEngineId = EngineId.SHOP_ENGINE;
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
		
		LogUtils.logD("ShopEngine.OnCreate()");
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
		LogUtils.logD("ShopEngine.onTimeoutEvent() in State: " + mState);
		
	}

	@Override
	protected void processCommsResponse(DecodedResponse resp) {
		LogUtils.logD("ShopEngine processCommsResponse");
		switch (mState) {
		case GET_ITEM_BLOCK:
			handleGetItemBlocks(resp.mDataTypes);
			break;
		default: // do nothing.
			break;
		}
		
	}

	@Override
	protected void processUiRequest(ServiceUiRequest requestId, Object data) {
		LogUtils.logD("LoginEngine.processUiRequest() - reqID = " + requestId);
		switch (requestId) {
		
		case GET_ITEM_BLOCK:
			startGetItemBlocks((List<ItemBlockRequest>)data);
			break;
			
		default:
			completeUiRequest(ServiceStatus.ERROR_NOT_FOUND, null);
		}
			
	}
	/**
	 * Run function called via EngineManager. Should have a UI, Comms response
	 * or timeout event to handle.
	 */
	@Override
	public void run() {
		LogUtils.logD("ShopEngine run");
		processTimeout();
		if (isCommsResponseOutstanding() && processCommsInQueue()) {
			return;
		}
		if (isUiRequestOutstanding()) {
			processUiQueue();
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
		case GET_ITEM_BLOCK:
		case IDLE:
		default:
			break;
		}
		LogUtils.logV("ShopEngine.newState(): " + oldState + " -> "
				+ mState);
	}
	/**
	 * Add request to fetch Item Blocks. The request is added to the UI request and
	 * processed when the engine is ready.
	 * 
	 * @param
	 * @return void
	 */
	public void addUiGetItemBlocksReq(List<ItemBlockRequest> blockrequestlist) {
		LogUtils.logD("ShopEngine.addUiGetItemBlocksReq()");
		addUiRequestToQueue(ServiceUiRequest.GET_ITEM_BLOCK,blockrequestlist);
	}
	/**
	 * Issue request to fetch Item Blocks. (Request is not issued if there is
	 * currently no connectivity).
	 * 
	 * @param blockrequestlist
	 *            The list of Item Blocks to be added.
	 * @return void
	 */
	private void startGetItemBlocks(List<ItemBlockRequest> blockrequestlist) {
		LogUtils.logD("ShopEngine.startGetItemBlocks()");
		if (NetworkAgent.getAgentState() != NetworkAgent.AgentState.CONNECTED) {
			completeUiRequest(ServiceStatus.ERROR_COMMS, null);
			return;
		}

		newState(State.GET_ITEM_BLOCK);
		if (!setReqId(Shop.getItemBlocks(this, blockrequestlist))) {
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
	private void handleGetItemBlocks(List<BaseDataType> data) {
		LogUtils.logD("ShopEngine.handleGetItemBlocks()");
		ServiceStatus errorStatus = getResponseStatus(BaseDataType.ITEM_BLOCK_RESPONSE_LIST_DATATYPE, data);
		if (errorStatus == ServiceStatus.SUCCESS) {
			for (BaseDataType item : data) {
				if (BaseDataType.ITEM_BLOCK_RESPONSE_LIST_DATATYPE == item.getType()) {
					blocks = (ItemBlockResponseList) item;
					String id =  blocks.mBlockList.get(0).refnodeid;
					LogUtils.logD("block id :: "+id);
				} else {
					LogUtils.logE("ShopEngine handleGetItemBlocks Unexpected response: "
							+ item.getType());
					return;
				}
			}
		} else {
			LogUtils
					.logE("ShopEngine handleGetItemBlocks error status: "
							+ errorStatus.name());
			return;
		}
		completeUiRequest(errorStatus,null);
        newState(State.IDLE);
	}
	
}
