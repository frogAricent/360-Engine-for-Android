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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.vodafone360.people.Settings;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.datatypes.PrivacySetting;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.login.LoginEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.utils.LogUtils;

/**
 * Implementation of Now+ Group privacy APIs
 */
public class GroupPrivacy {

	private final static String FUNCTION_ADD_CONTACT_GROUP_RELATIONS = "groupprivacy/addcontactgrouprelations";

	private final static String FUNCTION_DELETE_CONTACT_GROUP_RELATIONS_EXT = "groupprivacy/deletecontactgrouprelationsext";

	private final static String FUNCTION_GET_GROUPS = "groupprivacy/getgroups";

	private final static String FUNCTION_CREATE_GROUPS = "groupprivacy/creategroups";

	private final static String FUNCTION_DELETE_GROUPS = "groupprivacy/deletegroups";

	private final static String FUNCTION_GET_GROUP_PRIVACY_SETTING = "groupprivacy/getprivacysettings";

	private final static String FUNCTION_SET_GROUP_PRIVACY_SETTING = "groupprivacy/setprivacysettings";

	/**
	 * Implementation of groupprivacy/addcontactgrouprelations API. Parameters
	 * are; [auth], List<Long> contactidlist, List<Group> grouplist
	 * 
	 * @param engine
	 *            handle to ContactSyncEngine
	 * @param contactidlist
	 *            List of contacts ids associated with this request.
	 * @param grouplist
	 *            List of groups associated with this request.
	 * @return request id generated for this request
	 */
	public static int addContactGroupRelations(BaseEngine engine,
			List<Long> contactidlist, List<GroupItem> grouplist) {
		if (LoginEngine.getSession() == null) {
			LogUtils
					.logE("GroupPrivacy.addContactGroupRelations() Invalid session, return -1");
			return -1;
		}
		if (contactidlist == null) {
			LogUtils
					.logE("GroupPrivacy.addContactGroupRelations() contactidlist cannot be NULL");
			return -1;
		}
		if (grouplist == null) {
			LogUtils
					.logE("GroupPrivacy.addContactGroupRelations() grouplist cannot be NULL");
			return -1;
		}

		Request request = new Request(FUNCTION_ADD_CONTACT_GROUP_RELATIONS,
				Request.Type.CONTACT_GROUP_RELATIONS, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_GROUP_PRIVACY);
		request.addData("contactidlist", new Vector<Object>(contactidlist));
		request.addData("grouplist", ApiUtils.createVectorOfGroup(grouplist));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of groupprivacy/deletecontactgrouprelationsext API.
	 * Parameters are; [auth], Long groupid, List<Long> contactidlist
	 * 
	 * @param engine
	 *            handle to IdentitiesEngine
	 * @param groupid
	 *            Group ID.
	 * @param contactidlist
	 *            List of contact IDs to delete group relations.
	 * @return request id generated for this request
	 */
	public static int deleteContactGroupRelationsExt(BaseEngine engine,
			Long groupid, List<Long> contactidlist) {
		if (LoginEngine.getSession() == null) {
			LogUtils
					.logE("GroupPrivacy.deleteContactGroupRelationsExt() Invalid session, return -1");
			return -1;
		}
		if (groupid == null) {
			LogUtils
					.logE("GroupPrivacy.deleteContactGroupRelationsExt() groupid cannot be NULL");
			return -1;
		}
		if (contactidlist == null) {
			LogUtils
					.logE("GroupPrivacy.deleteContactGroupRelationsExt() contactidlist cannot be NULL");
			return -1;
		}
		if (contactidlist.size() == 0) {
			LogUtils
					.logE("GroupPrivacy.deleteContactGroupRelationsExt() contactidlist.size cannot be 0");
			return -1;
		}

		Request request = new Request(
				FUNCTION_DELETE_CONTACT_GROUP_RELATIONS_EXT,
				Request.Type.STATUS, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_GROUP_PRIVACY);
		request.addData("groupid", groupid);
		request.addData("contactidlist", new Vector<Object>(contactidlist));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of groupprivacy/getgroups API. Parameters are; [auth],
	 * Integer pageindex [opt], Integer pagesize [opt]
	 * 
	 * @param engine
	 *            handle to IdentitiesEngine
	 * @param pageindex
	 *            Page index.
	 * @param pagesize
	 *            PAge size.
	 * @return request id generated for this request
	 */
	public static int getGroups(BaseEngine engine, Integer pageindex,
			Integer pagesize) {
		if (LoginEngine.getSession() == null) {
			LogUtils
					.logE("GroupPrivacy.GetGroups() Invalid session, return -1");
			return -1;
		}
		Request request = new Request(FUNCTION_GET_GROUPS,
				Request.Type.GROUP_LIST, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_GROUP_PRIVACY);
		if (pageindex != null) {
			request.addData("pageindex", pageindex);
		}
		if (pagesize != null) {
			request.addData("pagesize", pagesize);
		}

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of groupprivacy/creategroups API. Parameters are; [auth],
	 * String name
	 * 
	 * @param engine
	 *            Handle to Groups engine
	 * @param name
	 *            of the group
	 * @return request id generated for this request.
	 */
	public static int addUserGroup(BaseEngine engine, String name) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Contacts.addUserGroup() Invalid session, return -1");
			return -1;
		}
		if (name == null) {
			LogUtils.logE("Contacts.addUserGroup() Group name cannot be NULL");
			return -1;
		}

		List<GroupItem> input = new ArrayList<GroupItem>();
		GroupItem group = new GroupItem();
		group.mName = name;
		Request request = new Request(FUNCTION_CREATE_GROUPS,
				Request.Type.ADD_GROUP, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_CONTACTS);
		input.add(group);
		request.addData("grouplist", ApiUtils.createVectorOfGroup(input));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of groupprivacy/deletegroups API. Parameters are; [auth],
	 * Long groupId
	 * 
	 * @param engine
	 *            Handle to Groups engine
	 * @param groupId
	 *            Id of the group
	 * @return request id generated for this request.
	 */
	public static int deleteUserGroup(BaseEngine engine, Long groupId) {
		if (LoginEngine.getSession() == null) {
			LogUtils
					.logE("GroupPrivacy.deleteUserGroup() Invalid session, return -1");
			return -1;
		}
		if (groupId == null) {
			LogUtils
					.logE("GroupPrivacy.deleteUserGroup() Group ID cannot be NULL");
			return -1;
		}

		List<Long> input = new ArrayList<Long>();
		input.add(groupId);

		Request request = new Request(FUNCTION_DELETE_GROUPS,
				Request.Type.DELETE_GROUP, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_CONTACTS);
		request.addData("groupidlist", ApiUtils.createVectorOfLong(input));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of groupprivacy/getprivacysettings API. Parameters are;
	 * [auth], Long groupidlist
	 * 
	 * @param engine
	 *            Handle to Groups engine
	 * @param groupId
	 *            Group ID to fetch the Privacy Settings for
	 * @return requestId
	 */

	public static int getGroupPrivacySetting(BaseEngine engine, Long groupId) {
		Request request = new Request(FUNCTION_GET_GROUP_PRIVACY_SETTING,
				Request.Type.GET_GROUP_PRIVACY, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_GROUP_PRIVACY);
		List<Long> groupIdList = new ArrayList<Long>();
		groupIdList.add(groupId);
		request
				.addData("groupidlist", ApiUtils
						.createVectorOfLong(groupIdList));
		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}

	/**
	 * Implementation of groupprivacy/setprivacysettings API. Parameters are;
	 * [auth], PrivacySetting privacysettinglist
	 * 
	 * @param engine
	 *            Handle to Groups engine
	 * @param ps
	 *            Privacy Settings for the group
	 * @return requestId
	 */
	public static int setGroupPrivacySetting(BaseEngine engine,
			List<PrivacySetting> ps) {
		Request request = new Request(FUNCTION_SET_GROUP_PRIVACY_SETTING,
				Request.Type.SET_GROUP_PRIVACY, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_GROUP_PRIVACY);
		// Add the required data to the request
		request.addData("privacysettinglist", ApiUtils
				.createVectorOfPrivacySetting(ps));

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}
}
