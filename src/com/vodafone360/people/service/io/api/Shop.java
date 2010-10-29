package com.vodafone360.people.service.io.api;

import java.util.List;

import com.vodafone360.people.Settings;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.ItemBlockRequest;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;

public class Shop {

	private final static String FUNCTION_GET_ITEM_BLOCKS = "superstore/getitemblocks";
	
	private final static String BLOCK_LIST = "blockrequestlist";
	
	public static int getItemBlocks(BaseEngine engine,List<ItemBlockRequest> blockrequestlist) {
		if (engine == null) {
			throw new NullPointerException(
					"Auth.getPublicKey() engine cannot be NULL");
		}

		Request request = new Request(FUNCTION_GET_ITEM_BLOCKS,
				Request.Type.GET_ITEM_BLOCK,engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		request.addData(BLOCK_LIST, ApiUtils.createVectorOfItemBlock(blockrequestlist));
		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

}
