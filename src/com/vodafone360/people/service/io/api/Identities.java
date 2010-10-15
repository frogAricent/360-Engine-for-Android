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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.vodafone360.people.Settings;
import com.vodafone360.people.datatypes.Identity;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.login.LoginEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.utils.LogUtils;

/**
 * Implementation of Now+ Identities APIs (used for access to 3rd party accounts
 * such as Facebook).
 */
public class Identities {

    private final static String FUNCTION_GET_AVAILABLE_IDENTITIES = "identities/getavailableidentities";

    private final static String FUNCTION_GET_MY_IDENTITIES = "identities/getmyidentities";

    // AA private final static String FUNCTION_SET_IDENTITY_CAPABILITY_STATUS =
    // "identities/setidentitycapabilitystatus";
    private final static String FUNCTION_SET_IDENTITY_STATUS = "identities/setidentitystatus";

    private final static String FUNCTION_VALIDATE_IDENTITY_CREDENTIALS = "identities/validateidentitycredentials";

    private final static String FUNCTION_DELETE_IDENTITY = "identities/deleteidentity";
    
    private final static String FUNCTION_GET_IDENTITIES_TEXT = "identities/getidentitiestext";
    
    public final static String ENABLE_IDENTITY = "enable";

    public final static String DISABLE_IDENTITY = "disable";

    //public final static String SUSPENDED_IDENTITY = "suspended";

    /**
     * Implementation of identities/getavailableidentities API. Parameters are;
     * [auth], Map<String, List<String>> filterlist [opt]
     * 
     * @param engine handle to IdentitiesEngine
     * @param filterlist List of filters the get identities request is filtered
     *            against.
     * @return request id generated for this request
     */
    public static int getAvailableIdentities(BaseEngine engine, Map<String, List<String>> filterlist) {
        if (LoginEngine.getSession() == null) {
            LogUtils.logE("Identities.getAvailableIdentities() Invalid session, return -1");
            return -1;
        }
        Request request = new Request(FUNCTION_GET_AVAILABLE_IDENTITIES, Request.Type.GET_AVAILABLE_IDENTITIES,
                engine.engineId(), false, Settings.API_REQUESTS_TIMEOUT_IDENTITIES);
        if (filterlist != null) {
            request.addData("filterlist", ApiUtils.createHashTable(filterlist));
        }

        QueueManager queue = QueueManager.getInstance();
        int requestId = queue.addRequest(request);
        queue.fireQueueStateChanged();
        return requestId;
    }

    /**
     * Implementation of identities/getmyidentities API. Parameters are; [auth],
     * Map<String, List<String>> filterlist [opt]
     * 
     * @param engine handle to IdentitiesEngine
     * @param filterlist List of filters the get identities request is filtered
     *            against.
     * @return request id generated for this request
     */
    public static int getMyIdentities(BaseEngine engine, Map<String, List<String>> filterlist) {
        if (LoginEngine.getSession() == null) {
            LogUtils.logE("Identities.getMyIdentities() Invalid session, return -1");
            return -1;
        }
        Request request = new Request(FUNCTION_GET_MY_IDENTITIES, Request.Type.GET_MY_IDENTITIES, engine
                .engineId(), false, Settings.API_REQUESTS_TIMEOUT_IDENTITIES);
        if (filterlist != null) {
            request.addData("filterlist", ApiUtils.createHashTable(filterlist));
        }

        QueueManager queue = QueueManager.getInstance();
        int requestId = queue.addRequest(request);
        queue.fireQueueStateChanged();
        return requestId;
    }

    /**
     * @param engine
     * @param network
     * @param identityid
     * @param identityStatus
     * @return
     */
    public static int setIdentityStatus(BaseEngine engine, String network, String identityid,
            String identityStatus) {
        if (LoginEngine.getSession() == null) {
            LogUtils.logE("Identities.setIdentityStatus() Invalid session, return -1");
            return -1;
        }
        if (identityid == null) {
            LogUtils.logE("Identities.setIdentityStatus() identityid cannot be NULL");
            return -1;
        }
        if (network == null) {
            LogUtils.logE("Identities.setIdentityStatus() network cannot be NULL");
            return -1;
        }
        if (identityStatus == null) {
            LogUtils.logE("Identities.setIdentityStatus() identity status cannot be NULL");
            return -1;
        }

        Request request = new Request(FUNCTION_SET_IDENTITY_STATUS,
                Request.Type.EXPECTING_STATUS_ONLY, engine.engineId(), false,
                Settings.API_REQUESTS_TIMEOUT_IDENTITIES);
        request.addData("network", network);
        request.addData("identityid", identityid);
        request.addData("status", identityStatus);

        QueueManager queue = QueueManager.getInstance();
        int requestId = queue.addRequest(request);
        queue.fireQueueStateChanged();
        return requestId;
    }

    /**
     * Implementation of identities/validateidentitycredentials API. Parameters
     * are; [auth], Boolean dryrun [opt], String network [opt], String username,
     * String password, String server [opt], String contactdetail [opt], Map
     * identitycapabilitystatus [opt]
     * 
     * @param engine handle to IdentitiesEngine
     * @param dryrun Whether this is a dry-run request.
     * @param network Name of network.
     * @param username User-name.
     * @param password Password.
     * @param server
     * @param contactdetail
     * @param identitycapabilitystatus Capabilities for this identity/network.
     * @return request id generated for this request
     */
    public static int validateIdentityCredentials(BaseEngine engine, Boolean dryrun,
            String network, String username, String password, String server, String contactdetail,
            Map<String, Boolean> identitycapabilitystatus) {
        if (LoginEngine.getSession() == null) {
            LogUtils.logE("Identities.validateIdentityCredentials() Invalid session, return -1");
            return -1;
        }
        if (network == null) {
            LogUtils.logE("Identities.validateIdentityCredentials() network cannot be NULL");
            return -1;
        }
        if (username == null) {
            LogUtils.logE("Identities.validateIdentityCredentials() username cannot be NULL");
            return -1;
        }
        if (password == null) {
            LogUtils.logE("Identities.validateIdentityCredentials() password cannot be NULL");
            return -1;
        }

        Request request = new Request(FUNCTION_VALIDATE_IDENTITY_CREDENTIALS,
                Request.Type.EXPECTING_STATUS_ONLY, engine.engineId(), false,
                Settings.API_REQUESTS_TIMEOUT_IDENTITIES);
        if (dryrun != null) {
            request.addData("dryrun", dryrun);
        }
        request.addData("network", network);
        request.addData("username", username);
        request.addData("password", password);

        if (server != null) {
            request.addData("server", server);
        }
        if (contactdetail != null) {
            request.addData("contactdetail", contactdetail);
        }
        if (identitycapabilitystatus != null) {
            request.addData("identitycapabilitystatus", new Hashtable<String, Object>(
                    identitycapabilitystatus));
        }

        QueueManager queue = QueueManager.getInstance();
        int requestId = queue.addRequest(request);
        queue.fireQueueStateChanged();
        return requestId;
    }
    
    /**
	 * Implementation of identities/deleteidentity API. Parameters are;
	 * 
	 * @param engine handle to IdentitiesEngine
	 * @param sns name of the identity to be deleted
	 * @return request id generated for this request
	 */
	public static int deleteIdentity(BaseEngine engine, String sns, String networkId) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Identities.deleteIdentity() Invalid session, return -1");
			return -1;
		}
		Request request = new Request(FUNCTION_DELETE_IDENTITY, Request.Type.COMMON,
				engine.engineId(), false, Settings.API_REQUESTS_TIMEOUT_IDENTITIES);

		Identity query = new Identity();

		
		//query.mIdentityId = base.mIdentityId;
		if ((sns != null)&&(networkId != null)){
			query.mNetwork = sns;
			query.mIdentityId = networkId;
		}else{
			if(sns == null){
			LogUtils.logD("Identities.deleteIdentity() - network name is null");
			}else{
				LogUtils.logD("Identities.deleteIdentity() - network id is null");
			}
			return -1;
		}
		
		request.addData("network", query.mNetwork);
		request.addData("identityid", query.mIdentityId);


		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		return requestId;
	}
	/**
	 * Implementation of identities/getidentitiestext API. Parameters are;
	 * 
	 * @param engine handle to IdentitiesEngine
	 * @param networklist The list of network codes 
	 * @return
	 */
	public static int getTextIdentities(BaseEngine engine,
			List <String> networklist) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Identities.getTextIdentities() Invalid session, return -1");
			return -1;
		}
		if (engine == null) {
			throw new NullPointerException("Auth.getPublicKey() engine cannot be NULL");
		}
		System.out.println("In Identities Service Io class getTextIdentities() function. " +
				"Before posting the  request. EngineId: " + engine.engineId());
		
		Request request = new Request(FUNCTION_GET_IDENTITIES_TEXT,
				Request.Type.GET_IDENTITIES_TEXT, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		
		if (networklist != null) {
            request.addData("networklist", ApiUtils.createVectorOfString(networklist));
        }
		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		System.out.println("In Identities Service Io class getTextIdentities()function. " +
		"After posting the  request. Request Id: " + requestId);
		return requestId;
	}

}
