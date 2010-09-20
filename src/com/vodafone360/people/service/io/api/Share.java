
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

package com.vodafone360.people.service.io.api;

import java.util.List;

import com.vodafone360.people.Settings;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.login.LoginEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.utils.LogUtils;

/**
 * Implementation of Now+ Share APIs
 */
public class Share {

	private final static String FUNCTION_SHARE_ALBUM = "share/sharewithgroup";
	private final static String FUNCTION_SHARED_WITH = "share/sharedwith";
	private final static String FUNCTION_ALLOW_GROUP = "share/allowgroup";
	private final static String FUNCTION_DENY_GROUP = "share/denygroup";

	/**
	 * Implementation of share/sharewithgroup API.
	 * 
	 * @param engine
	 *            Handle to Share engine
	 * @param groupID
	 *            id of the group to be shared
	 * @param entitykeylist
	 *            list of entity key(album id's) that are being shared.
	 * @return request id generated for this request.
	 */
	public static int shareWithGroup(BaseEngine engine, Long groupId,
			List<EntityKey> entityKeyList) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Share.shareWithGroup() Invalid session, return -1");
			return -1;
		}

		LogUtils.logI("Engine:" + engine.toString());
		if (groupId == null) {
			LogUtils.logE("Share.shareWithGroup() Group id cannot be NULL");
			return -1;
		}
		if (entityKeyList == null) {
			LogUtils
					.logE("Share.shareWithGroup() EntityKey list cannot be NULL");
			return -1;
		}

		Request request = new Request(FUNCTION_SHARE_ALBUM,
				Request.Type.SHARE_ALBUM, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_CONTACTS);
		request.addData("groupid", groupId);
		request.addData("entitykeylist", ApiUtils
				.createVectorOfEntityKey(entityKeyList));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of share/sharedwith API.
	 * 
	 * @param engine
	 *            Handle to Share engine
	 * @param entitykeylist
	 *            The album entity for which the shared groups will be retrieved
	 * @return request id generated for this request.
	 */
	public static int sharedWith(BaseEngine engine, EntityKey entityKey) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Share.sharedWith() Invalid session, return -1");
			return -1;
		}
		LogUtils.logI("Engine:" + engine.toString());

		if (entityKey == null) {
			LogUtils.logE("Share.sharedWith() EntityKey cannot be NULL");
			return -1;
		}

		Request request = new Request(FUNCTION_SHARED_WITH,
				Request.Type.GET_GROUPS_SHARED_WITH, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_CONTACTS);

		request.addData("entitykey", entityKey.createHastable());

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of share/allowgroup API.
	 * 
	 * @param engine
	 *            Handle to ContactSync engine
	 * @param groupId
	 *            id of the group being to be shared
	 * @param entitykeylist
	 *            The album entity for which the shared groups will be retrieved
	 * @return request id generated for this request.
	 */
	public static int allowGroup(BaseEngine engine, Long groupId,
			List<EntityKey> entityKeyList) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Share.allowGroup() Invalid session, return -1");
			return -1;
		}

		LogUtils.logI("Engine:" + engine.toString());
		if (groupId == null) {
			LogUtils.logE("Share.allowGroup() Group id cannot be NULL");
			return -1;
		}
		if (entityKeyList == null) {
			LogUtils.logE("Share.allowGroup() EntityKey list cannot be NULL");
			return -1;
		}

		Request request = new Request(FUNCTION_ALLOW_GROUP,
				Request.Type.ALLOW_GROUP, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_CONTACTS);

		request.addData("groupid", groupId);
		request.addData("entitykeylist", ApiUtils
				.createVectorOfEntityKey(entityKeyList));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of share/denygroup API.
	 * 
	 * @param engine
	 *            Handle to ContactSync engine
	 * @param groupId
	 *            Id of the group to to be denied
	 * @param entitykeylist
	 *            the album entity for which the shared groups will be retrieved
	 * @return request id generated for this request.
	 */
	public static int denyGroup(BaseEngine engine, Long groupId,
			List<EntityKey> entityKeyList) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Share.denyGroup() Invalid session, return -1");
			return -1;
		}

		LogUtils.logI("Engine:" + engine.toString());
		if (groupId == null) {
			LogUtils.logE("Share.denyGroup() Group id cannot be NULL");
			return -1;
		}
		if (entityKeyList == null) {
			LogUtils.logE("Share.denyGroup() EntityKey list cannot be NULL");
			return -1;
		}

		Request request = new Request(FUNCTION_DENY_GROUP,
				Request.Type.DENY_GROUP, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_CONTACTS);
		request.addData("groupid", groupId);
		request.addData("entitykeylist", ApiUtils
				.createVectorOfEntityKey(entityKeyList));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

}
