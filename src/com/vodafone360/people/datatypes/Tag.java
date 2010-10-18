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
import java.util.Vector;

/**
 * BaseDataType encapsulating an Tag retrieved from, or to be issued to, Now +
 * server
 */
public class Tag extends BaseDataType {

	public Long tagid = null;
	public Long userid = null;
	public String name = null;
	public String type = null;
	public EntityKey entitykey;
	public String exttagid;
	public Integer count;

	private enum Tags {

		TAG_ID("tagid"), USER_ID("userid"), NAME("name"), TYPE("type"), ENTITY_KEY(
				"entitykey"),
		// PROPERTIES("properties"),
		EXT_TAG_ID("exttagid"), COUNT("count");

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
	 * Create Tag containing ContactDetail parameters
	 * 
	 * @return Hashtable containing Tag detail parameters
	 */
	public Hashtable<String, Object> createHashtable() {
		Hashtable<String, Object> htab = new Hashtable<String, Object>();

		if (tagid != null) {
			htab.put(Tags.TAG_ID.tag(), tagid);
		}
		if (userid != null) {
			htab.put(Tags.USER_ID.tag(), userid);
		}
		if (name != null) {
			htab.put(Tags.NAME.tag(), name);
		}
		if (type != null) {
			htab.put(Tags.TYPE.tag(), type);
		}
		if (entitykey != null) {
			Vector<Object> v = new Vector<Object>();
			v.add(entitykey.createHastable());
			htab.put(Tags.ENTITY_KEY.tag(), v);
		}
		if (exttagid != null) {
			htab.put(Tags.EXT_TAG_ID.tag(), exttagid);
		}
		if (count != null) {
			htab.put(Tags.COUNT.tag(), count);
		}

		return htab;
	}

	/**
	 * Create Tag from Hashtable (generated from Hessian encoded response).
	 * 
	 * @param hash
	 *            Hashtable containing Tag data
	 * @return Tag created from supplied Hashtable.
	 */
	static public Tag createFromHashtable(Hashtable<String, Object> hash) {
		Tag tag = new Tag();
		Enumeration<String> e = hash.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = hash.get(key);
			Tags t = Tags.findTag(key);
			tag.setValue(t, value);
		}
		return tag;
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
	private void setValue(Tags tag, Object value) {
		if (tag != null) {
			switch (tag) {

			case TAG_ID:
				tagid = (Long) value;
				break;

			case USER_ID:
				userid = (Long) value;
				break;

			case NAME:
				name = (String) value;
				break;

			case TYPE:
				type = (String) value;
				break;

			case ENTITY_KEY:
				Vector<Hashtable<String, Object>> v = (Vector<Hashtable<String, Object>>) value;

				for (Hashtable<String, Object> hash : v) {
					EntityKey key = new EntityKey();
					key.createFromHashtable(hash);
					entitykey = key;
				}
				break;

			case EXT_TAG_ID:
				exttagid = (String) value;
				break;
			case COUNT:
				count = (Integer) value;
				break;
			default:
				break;
			}
		}
	}

	@Override
	public int getType() {
		return TAG_DATATYPE;
	}
}
