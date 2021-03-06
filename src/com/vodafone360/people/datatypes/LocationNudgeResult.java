
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

package com.vodafone360.people.datatypes;

import java.util.Enumeration;
import java.util.Hashtable;

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating an LocationNudgeResult retrieved from, or to be
 * issued to, Now + server
 */
public class LocationNudgeResult extends BaseDataType {
	public Boolean success;
	public String reason;

	private enum Tags {

		SUCCESS("success"), REASON("reason");

		private final String tag;

		/**
		 * Constructor for Tags item.
		 * 
		 * @param s
		 *            String value associated with Tag.
		 */
		private Tags(String s) {
			tag = s;
		}

		/**
		 * String value associated with Tags item.
		 * 
		 * @return String value associated with Tags item.
		 */
		private String tag() {
			return tag;
		}

	}

	/**
	 * Create LocationNudgeResult from Hashtable (generated from Hessian encoded
	 * response).
	 * 
	 * @param Hashtable containing LocationNudgeResult data
	 * @return LocationNudgeResult created from supplied Hashtable.
	 */
	public LocationNudgeResult createFromHashtable(
			Hashtable<String, Object> hash) {
		LocationNudgeResult cont = new LocationNudgeResult();
		Enumeration<String> e = hash.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = hash.get(key);
			Tags tag = cont.findTag(key);
			cont.setValue(tag, value);
		}
		return cont;
	}

	/**
	 * Find Tags item for specified String
	 * 
	 * @param tag
	 *            String value to find Tags item for
	 * @return Tags item for specified String, null otherwise
	 */
	private Tags findTag(String tag) {
		for (Tags tags : Tags.values()) {
			if (tag.compareTo(tags.tag()) == 0) {
				return tags;
			}
		}
		LogUtils.logE("Contact.findTag - Unsupported contact tag: " + tag);
		return null;
	}

	/**
	 * Sets the value of the member data item associated with the specified tag.
	 * 
	 * @param tag
	 *            Current tag
	 * @param val
	 *            Value associated with the tag
	 * @return void
	 */
	private void setValue(Tags tag, Object value) {
		if (tag == null) {
			LogUtils.logE("Contact setValue tag is null");
			return;
		}

		switch (tag) {
		case SUCCESS:
			success = (Boolean) value;
			break;
		case REASON:
			reason = (String) value;
			break;

		default:
			// Do nothing.
			break;
		}
	}

	@Override
	public int getType() {
		return LOCATION_NUDGE_RESULT_DATATYPE;
	}
}
