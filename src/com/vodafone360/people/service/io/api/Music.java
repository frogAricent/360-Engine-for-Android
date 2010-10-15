
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
import java.util.Vector;

import com.vodafone360.people.Settings;
import com.vodafone360.people.engine.BaseEngine;
import com.vodafone360.people.engine.login.LoginEngine;
import com.vodafone360.people.service.io.QueueManager;
import com.vodafone360.people.service.io.Request;
import com.vodafone360.people.utils.LogUtils;

/**
 * Implementation of Now+ Music APIs
 */
public class Music {
	
	private final static String DOWNLOADABLE_TRACK = "music/getdownloadabletracks";
	private final static String DD_FOR_TRACKS = "music/getddfortracks";
	private final static String LICENSE_TYPE = "ftmd-redownload";

	/**
	 * Implementation of music/GetDownloadableTrackBlock API. Parameters are; [auth], IntegerExpired [opt]
	 * Map<String List<String>> filterList [Opt]
	 * 
	 * @param engine handle to MusicEnginebak
	 * @return request id generated for this request
	 * 
	 */
	public static int getDownloadableTrackBlock(BaseEngine engine) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Music.GetDownloadableTrackBlock() Invalid session, return -1");
			return -1;
		}
		if (engine == null) {
			throw new NullPointerException("Auth.getPublicKey() engine cannot be NULL");
		}
		
		Request request = new Request(DOWNLOADABLE_TRACK,
				Request.Type.DOWNLOADABLE_TRACK, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		
		return requestId;
	}
	
	/**
	 * Implementation of music/GetDDForTracks API. Parameters are; [auth], IntegerExpired [opt]
	 * Map<String List<String>> filterList [Opt]
	 * 
	 * @param engine handle to MusicEnginebak
	 * @return request id generated for this request
	 * 
	 */
	public static int getDDForTracks(BaseEngine engine, List<String> trackIds) {
		if (LoginEngine.getSession() == null) {
			LogUtils.logE("Music.GetDDForTracks() Invalid session, return -1");
			return -1;
		}
		if (engine == null) {
			throw new NullPointerException("Auth.getPublicKey() engine cannot be NULL");
		}
		
		Request request = new Request(DD_FOR_TRACKS,
				Request.Type.DD_FOR_TRACK, engine.engineId(), false,
				Settings.API_REQUESTS_TIMEOUT_AUTH);
		request.addData("trackidlist", new Vector<Object>(trackIds));
		request.addData("licensetype", LICENSE_TYPE);

		QueueManager queue = QueueManager.getInstance();
		int requestId = queue.addRequest(request);
		queue.fireQueueStateChanged();
		
		return requestId;
	}

}
