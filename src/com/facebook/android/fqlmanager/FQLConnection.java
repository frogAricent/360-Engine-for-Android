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

package com.facebook.android.fqlmanager;

import java.io.IOException;

import com.facebook.android.listener.BaseRequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.Util;
import android.os.Bundle;

/*
 *  Class handles the facebook connection for fql query.
 */
public class FQLConnection {
	
	private static final String BASE_URL = "https://api.facebook.com/method/";
	private static Facebook fb;

	public FQLConnection(Facebook afb) {
		fb = afb;
	}
	
/**
 * 	API to send facebook query to the network
 *	@param methodName
 *	@param parameters
 *	@param listener
 *	@return void
 * @throws IOException 
 */
	public String request(final String methodName,
	            final Bundle parameters,
	            final BaseRequestListener listener) throws IOException{
			String response = null;
			try{
				String url = BASE_URL + methodName;
				parameters.putString("format", "json");
		        if (fb.isSessionValid()) {
		            parameters.putString(Facebook.TOKEN, fb.getAccessToken());
		        }
				response = Util.openUrl(url, "GET", parameters);
				if (listener != null) {
					listener.onComplete(response);
				}
			}catch (IOException e) {
				listener.onIOException(e);
				throw e;
			}

			return response;
	}

}
