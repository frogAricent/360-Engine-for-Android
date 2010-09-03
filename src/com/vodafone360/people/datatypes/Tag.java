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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.vodafone360.people.datatypes.Comment.Tags;

import android.util.Log;

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
	// public Map<String, List<String>> properties;
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
		// if (properties != null) {
		// htab.put(Tags.PROPERTIES.tag(), properties);
		// }
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

			// case PROPERTIES:
			// properties = (Map<String, List<String>>)value;
			// break;

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
