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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating an ContentResponse retrieved from Now + server
 */
public class ContentResponse extends BaseDataType {

	public List<Long> mContentIdList = new ArrayList<Long>();

	private enum Tags {
		CONTENTLIST("contentlist"), ITEMS("items"), UPDATED("updated");

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

		private static Tags findTag(String tag) {
			for (Tags tags : Tags.values()) {
				if (tag.compareTo(tags.tag()) == 0) {
					return tags;
				}
			}
			return null;
		}
	}

	public List<Content> mContentList = new ArrayList<Content>();

	public Integer mItems = null;

	public Long mUpdated = null;

	/**
	 * Create ContentResponse from Hashtable (generated from Hessian encoded
	 * response).
	 * 
	 * @param hash
	 *            Hashtable containing ContentResponse data
	 * @return ContentResponse created from supplied Hashtable.
	 */
	public ContentResponse createFromHashtable(Hashtable<String, Object> hash)
			throws IOException {
		LogUtils.logI("HessianDecoder.createFromHashtable() hash["
				+ hash.toString() + "]");
		ContentResponse content = new ContentResponse();
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
	@SuppressWarnings("unchecked")
	private void setValue(Tags tag, Object value) throws IOException {
		if (tag != null) {
			switch (tag) {
			case CONTENTLIST:
				Vector<Hashtable<String, Object>> contentsVector = (Vector<Hashtable<String, Object>>) value;
				for (Hashtable<String, Object> hash : contentsVector) {
					Content content = new Content();
					content = content.createFromHashtable(hash);
					mContentList.add(content);
				}
				break;
			case ITEMS:
				if (mItems == null) {
					mItems = (Integer) value;
				}
				break;
			case UPDATED:
				if (mUpdated == null) {
					mUpdated = (Long) value;
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public int getType() {
		return CONTENT_RESPONSE_DATATYPE;
	}
}
