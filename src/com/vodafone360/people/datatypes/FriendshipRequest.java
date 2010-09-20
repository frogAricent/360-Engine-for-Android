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

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating an FriendshipRequest retrieved from, or to be
 * issued to, Now + server
 */
public class FriendshipRequest extends BaseDataType {

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return FRIENDSHIP_REQUEST_DATATYPE;
	}

	/**
	 * Tags for fields associated with FriendshipRequest items.
	 */
	private enum Tags {
		USER_PROFILE("userprofile"), MESSAGE("message"), REQUEST_ID("requestid"), TIME_STAMP(
				"timestamp");

		private final String tag;

		/**
		 * Constructor creating Tags item for specified String.
		 * 
		 * @param s
		 *            String value for Tags item.
		 */
		private Tags(String s) {
			tag = s;
		}

		/**
		 * String value associated with Tags item.
		 * 
		 * @return String value for Tags item.
		 */
		private String tag() {
			return tag;
		}
	}

	public UserProfile mUserProfile;
	public String mMessage;
	public Long mRequestId;
	public Long mTimeStamp;

	/**
	 * Find Tags item for specified String.
	 * 
	 * @param tag
	 *            String value to search for in Tag
	 * @return Tags item for specified String, NULL otherwise.
	 */
	private Tags findTag(String tag) {
		for (Tags tags : Tags.values()) {
			if (tag.compareTo(tags.tag()) == 0) {
				return tags;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		Date time = null;
		if (mTimeStamp != null) {
			time = new Date(mTimeStamp);
		} else {
			time = new Date(0);
		}
		StringBuffer ret = new StringBuffer();
		ret.append("Friendship Request data: \n\tUserID:" + mUserProfile.userID
				+ "\n\t Contact Id:" + mUserProfile.contactID + "\n\tMessage: "
				+ mMessage + "\n\tRequest ID: " + mRequestId
				+ "\n\tTimeStamp: " + time);
		return ret.toString();
	}

	/**
	 * Create FriendshipRequest from Hashtable.
	 * 
	 * @param hash
	 *            Hashtable containing FriendshipRequest information.
	 * @return FriendshipRequest generated from Hashtable.
	 */
	public static FriendshipRequest createFromHashtable(
			Hashtable<String, Object> hash) {
		FriendshipRequest profile = new FriendshipRequest();
		Enumeration<String> e = hash.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = hash.get(key);
			Tags tag = profile.findTag(key);
			profile.setValue(tag, value);
		}

		return profile;
	}

	/**
	 * Sets the value of the member data item associated with the specified tag.
	 * 
	 * @param tag
	 *            Current tag
	 * @param value
	 *            Value associated with the tag
	 */
	@SuppressWarnings("unchecked")
	private void setValue(Tags tag, Object value) {
		switch (tag) {
		case USER_PROFILE:
			mUserProfile = UserProfile
					.createFromHashtable((Hashtable<String, Object>) value);
			break;

		case MESSAGE:
			mMessage = (String) value.toString();
			break;

		case REQUEST_ID:
			mRequestId = (Long) value;
			break;

		case TIME_STAMP:
			mTimeStamp = (Long) value;
			break;

		default:
			LogUtils.logW("setValue: Unknown tag - " + tag + "[" + value + "]");
		}
	}

}
