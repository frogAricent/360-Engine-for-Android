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
package com.facebook.android.listener;

import java.io.IOException;
import org.json.JSONException;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;;

/*
 *  AddCommentListener class implements the onComplete for addComment event 
 */
public class AddCommentListener extends BaseRequestListener {
	private RequestCompleteCallback callBack;
	
	public AddCommentListener (RequestCompleteCallback callBack) {
		this.callBack = callBack;
	}
	
	@Override
	public void onComplete(String response) {
		if ( response.contains("error_code")) {
			try {
				Util.parseJson(response);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (FacebookError e) {
				if (callBack != null) {
					callBack.onComplete(false, e.getMessage());
				}
			}
		} else if (response.contains("html")) {
			if (callBack != null) {
				callBack.onComplete(false, response);
			}							
		} else {
			if (callBack != null) {
				callBack.onComplete(true, response);
			}
		}
	};
	
	@Override
	public void onIOException(IOException e) {
		if (callBack != null) {
			callBack.onComplete(false, e.getMessage());
		}
	};
}
