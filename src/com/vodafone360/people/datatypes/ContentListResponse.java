package com.vodafone360.people.datatypes;

/*
 ****************************************************************
 * Copyright (c) 2010 Aricent Technologies (Holdings) Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Aricent Technologies ("Confidential Information").You 
 * shall not disclose such Confidential Information and shall use 
 * it only in accordance with the terms of the license agreement 
 * you entered into with Aricent.
 ****************************************************************
 */
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import android.util.Log;

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating an ContentListResponse retrieved from Now + server
 */
public class ContentListResponse extends BaseDataType {

	public List<Long> mContentIdList = new ArrayList<Long>();

	private enum Tags {
		CONTENTIDLIST("contentidlist");

		private final String tag;

		/**
		 * Construct Tags item from supplied String
		 * 
		 * @param s
		 *            String value for Tags item.
		 */
		private Tags(String s) {
			tag = s;
		}

		/**
		 * Return String value associated with Tags item.
		 * 
		 * @return String value associated with Tags item.
		 */
		private String tag() {
			return tag;
		}

		/**
		 * Find Tags item for specified String
		 * 
		 * @param tag
		 *            String value to find Tags item for
		 * @return Tags item for specified String, null otherwise
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
	/**
	 * Create ContentListResponse from Hashtable (generated from Hessian encoded response).
	 * 
	 * @param hash
	 *            Hashtable containing ContentListResponse data
	 * @return ContentListResponse created from supplied Hashtable.
	 */
	public ContentListResponse createFromHashtable(
			Hashtable<String, Object> hash) {
		LogUtils.logI("HessianDecoder.createFromHashtable() hash["
				+ hash.toString() + "]");
		ContentListResponse content = new ContentListResponse();
		Enumeration<String> e = hash.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = hash.get(key);
			Tags tag = Tags.findTag(key);
			content.setValue(tag, value);
		}
		return content;
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
		if (tag != null) {
			switch (tag) {
			case CONTENTIDLIST:
				Vector<Long> contentIdVector = (Vector<Long>) value;
				for (Long l : contentIdVector) {
					mContentIdList.add(l);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public int getType() {
		return CONTENT_LIST_RESPONSE_DATATYPE;
	}
}
