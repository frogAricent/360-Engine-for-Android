package com.vodafone360.people.datatypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.vodafone360.people.utils.LogUtils;

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
		USER_PROFILE("userprofile"), 
		MESSAGE("message"), 
		REQUEST_ID("requestid"), 
		TIME_STAMP("timestamp");

		private final String tag;

		/**
		 * Constructor creating Tags item for specified String.
		 * 
		 * @param s String value for Tags item.
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
	 * @param tag String value to search for in Tag
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
	 * @param hash Hashtable containing FriendshipRequest information.
	 * @return FriendshipRequest generated from Hashtable.
	 */
	public static FriendshipRequest createFromHashtable(Hashtable<String, Object> hash) {
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
	 * @param tag Current tag
	 * @param value Value associated with the tag
	 */
	private void setValue(Tags tag, Object value) {
		switch (tag) {
		case USER_PROFILE:
				mUserProfile = UserProfile.createFromHashtable((Hashtable<String, Object>) value);
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
