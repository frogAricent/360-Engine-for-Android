
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

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating an LongGeocodeAddress retrieved from, or to be
 * issued to, Now + server
 */
public class LongGeocodeAddress extends BaseDataType {

	public String COUNTRY_NAME;
	public String CITY_NAME;
	public String AREA_NAME;
	public String STREET_NAME;
	public String LATITUDE;
	public String LONGITUDE;

	private enum Tags {

		COUNTRY_NAME("countryname"), CITY_NAME("cityname"), AREA_NAME(
				"areaname"), STREET_NAME("streetname"), LATITUDE("latitude"), LONGITUDE(
				"longitude");

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
	 * Create LongGeocodeAddress from Hashtable (generated from Hessian encoded
	 * response).
	 * 
	 * @param hash
	 *            Hashtable containing LongGeocodeAddress data
	 * @return LongGeocodeAddress created from supplied Hashtable.
	 */
	@SuppressWarnings("unchecked")
	public LongGeocodeAddress createFromHashtable(Hashtable<String, Object> hash) {

		LongGeocodeAddress cont = new LongGeocodeAddress();
		Enumeration<String> e = hash.keys();

		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = hash.get(key);

			if (value != null) {
				Vector v = (Vector) value;
				if (v != null) {
					int s = v.size();
					for (int i = 0; i < s; i++) {
						Hashtable has = (Hashtable) v.elementAt(i);
						Enumeration<String> e1 = has.keys();

						while (e1.hasMoreElements()) {
							String key1 = e1.nextElement();

							Object value1 = has.get(key1);
							Tags tag = cont.findTag(key1);
							cont.setValue(tag, value1.toString());
						}
					}

				}
			}
		}

		return cont;
	}

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
		case COUNTRY_NAME:
			COUNTRY_NAME = (String) value;
			break;
		case CITY_NAME:
			CITY_NAME = (String) value;
			break;
		case AREA_NAME:
			AREA_NAME = (String) value;
			break;
		case STREET_NAME:
			STREET_NAME = (String) value;
			break;
		case LATITUDE:
			LATITUDE = (String) value;
			break;
		case LONGITUDE:
			LONGITUDE = (String) value;
			break;

		default:
			// Do nothing.
			break;
		}
	}

	@Override
	public int getType() {
		return LONG_GEOCODE_ADDRESS_DATATYPE;
	}
}
