
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * BaseDataType encapsulating an PrivacySetting retrieved from, or to be issued
 * to, Now + server
 */
public class PrivacySetting extends BaseDataType {
	
	private static final String PRIVACYSETTINGLIST = "PrivacySetting";

	/**
	 * Tags associated with PrivacySetting item.
	 */
	private enum Tags {
		GROUP_ID("groupid"), CONTENT_TYPE("contenttype"), STATE("state");

		private final String tag;

		/**
		 * Construct Tags item from supplied String.
		 * 
		 * @param s
		 *            String value for Tags item.
		 */
		private Tags(String s) {
			tag = s;
		}

		/**
		 * String value for Tags item.
		 * 
		 * @return String value for Tags item.
		 */
		private String tag() {
			return tag;
		}

		/**
		 * Find Tags item for specified String.
		 * 
		 * @param tag
		 *            String value to find in Tags items.
		 * @return Tags item for specified String, NULL otherwise.
		 */
		private static Tags findTag(String tag) {
			for (Tags tags : Tags.values()) {
				if (tag.compareTo(tags.tag()) == 0) {
					return tags;
				}
			}
			return null;
		}
	}

	public Long mGroupId;

	public int mContentType;

	public int mState;

	/**
	 * Populate PrivacySetting from supplied Hashtable.
	 * 
	 * @param hash
	 *            Hashtable containing privacy setting details
	 * @return PrivacySetting instance
	 */
	public PrivacySetting createFromHashtable(Hashtable<String, Object> hash) {
		Enumeration<String> e = hash.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = hash.get(key);
			Tags tag = Tags.findTag(key);
			if (tag != null)
				setValue(tag, value);
		}

		return this;
	}

	/**
	 * Populates data from Hashtable
	 * 
	 * @param hash
	 *            Hashtable containing PrivacySetting data
	 * @return PrivacySetting list
	 */
	@SuppressWarnings("unchecked")
	public static List<PrivacySetting> populateFromHashtable(
			Hashtable<String, Object> hash) {
		List<PrivacySetting> privSetList = new ArrayList<PrivacySetting>();
		Vector<Hashtable<String, Object>> vect = (Vector<Hashtable<String, Object>>) hash
		.get(PRIVACYSETTINGLIST);
		for (Hashtable<String, Object> msghash : vect) {
			PrivacySetting privSet = new PrivacySetting();
			privSetList.add(privSet.createFromHashtable(msghash));
		}
		return privSetList;
	}

	/**
	 * Populates group settings data from Hashtable
	 * 
	 * @param hash
	 *            Hashtable containing GroupsPrivacySetting data
	 * @return GroupsPrivacySetting list
	 */
	@SuppressWarnings("unchecked")
	public static List<PrivacySetting> populatefromHashtableGroupSetting(
			Hashtable inp) {
		List<PrivacySetting> privSetList = new ArrayList<PrivacySetting>();
		Enumeration er = inp.elements();
		
		while (er.hasMoreElements()) {
			Object data = er.nextElement();
			privSetList = populateList((Vector) data);
			break;
		}
		return privSetList;
	}

	/**
	 * Populates data from Hashtable
	 * 
	 * @param hash
	 *            Hashtable containing PrivacySetting data
	 * @return PrivacySetting list
	 */
	@SuppressWarnings("unchecked")
	public static List<PrivacySetting> populateList(Vector inp) {
		List<PrivacySetting> privSetList = new ArrayList<PrivacySetting>();
		Enumeration er;
		er = inp.elements();
		while (er.hasMoreElements()) {
			PrivacySetting ps = new PrivacySetting();
			Object data = er.nextElement();
			ps.createFromHashtable((Hashtable<String, Object>) data);
			privSetList.add(ps);
		}

		return privSetList;
	}

	/**
	 * Sets the value of the member data item associated with the specified tag.
	 * 
	 * @param tag
	 *            Current tag
	 * @param val
	 *            Value associated with the tag
	 */
	private void setValue(Tags tag, Object val) {
		switch (tag) {
		case GROUP_ID:
			mGroupId = (Long) val;
			break;

		case CONTENT_TYPE:
			mContentType = (Integer) val;
			break;

		case STATE:
			mState = (Integer) val;
			break;

		default:
			// Do nothing.
			break;
		}
	}

	/**
	 * Create Hashtable from PrivacySetting parameters.
	 * 
	 * @return Hashtable generated from GroupItem parameters.
	 */
	public Hashtable<String, Object> createHashtable() {
		Hashtable<String, Object> htab = new Hashtable<String, Object>();

		if (mGroupId != null) {
			htab.put(Tags.GROUP_ID.tag(), mGroupId);

			if (mContentType != 0) {
				htab.put(Tags.CONTENT_TYPE.tag(), mContentType);
			}
			if ((mState == 0) || (mState == 1)) {
				htab.put(Tags.STATE.tag(), mState);
			}
		}
		return htab;
	}

	@Override
	public int getType() {
		return PRIVACY_SETTING_DATATYPE;
	}

}
