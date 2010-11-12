
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
 * BaseDataType encapsulating trackIds of Recommended Music Tracks for the user retrieved from Now
 * + server
 */
public class MusicTracksResponse extends BaseDataType {

	public Long maxAge = null;
	public Integer startingAt = null;
	public Integer nrObjects = null;
	public Integer totalNoOfObjects= null;
	public List<MusicTrackObject> recommendedTracksList = new ArrayList<MusicTrackObject>();
	
	@Override
	public String toString() {
		String str =totalNoOfObjects + ", " + maxAge + ", " + startingAt + ", " + nrObjects.toString() + ", " + recommendedTracksList.toString();
		return str;
	}
	
	/**
	 * Tags associated with Recommended Music Tracks representing data items associated with
	 * it returned from server. 
	 * 
	 */
	public enum Tags {
		TOTAL_NO_OF_OBJECTS("totalnrofobjects"),
		MAXAGE("maxage"), 
		STARTING_AT("startingat"), 
		NR_OBJECTS("nrobjects"),
		RECOMMENDED_TRACKS("recommendedtracklist"),
		TOP_TRACKS("tracklist");

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

		/**
		 * Find Tags item for specified String.
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
			case MAXAGE:
				if(value != null)
					maxAge = (Long) value;
				break;
			case STARTING_AT:
				if(value != null)
					startingAt= (Integer) value;
				break;
			case TOTAL_NO_OF_OBJECTS:
				if(value != null)
					totalNoOfObjects = (Integer) value;
				break;
			case NR_OBJECTS:
				if(value != null)
					nrObjects = (Integer) value;
				break;	
			case RECOMMENDED_TRACKS:
			case TOP_TRACKS:
				Vector<Hashtable<String, Object>> musicVector= (Vector<Hashtable<String, Object>>)value;
				for (Hashtable<String, Object> hash : musicVector) {
					MusicTrackObject trackObject = new MusicTrackObject();
					trackObject = MusicTrackObject.createFromHashtable(hash);
					recommendedTracksList.add(trackObject);
				}
				break;
			default:
				// Do nothing.
				break;
			}
		}
	}

	/**
	 * Create Items from HashTable generated by Hessian-decoder
	 * 
	 * @param hash Hashtable representing ActivityItem
	 * @return MusicTracksResponse created from Hashtable
	 * 
	 **/
	public static MusicTracksResponse createFromHashtable(Hashtable<String, Object> hash) {
		MusicTracksResponse tracks = new MusicTracksResponse();
		Enumeration<String> e = hash.keys();
		
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Object value = hash.get(key);
			Tags tag = Tags.findTag(key);
			tracks.setValue(tag, value);
		}
		return tracks;
	}

	/**
	 * Create Hashtable from MusicRecommendedTracksResult parameters.
	 * 
	 * @param none
	 * @return Hashtable generated from MusicRecommendedTracksResult parameters.
	 */
	public Hashtable<String, Object> createHashtable() {
		Hashtable<String, Object> htab = new Hashtable<String, Object>();

		if (maxAge != null) {
			htab.put(Tags.MAXAGE.tag(), maxAge);
		}
		if (startingAt != null) {
			htab.put(Tags.STARTING_AT.tag(), startingAt);
		}
		if (totalNoOfObjects != null) {
			htab.put(Tags.TOTAL_NO_OF_OBJECTS.tag(), totalNoOfObjects);
		}
		if (nrObjects != null) {
			htab.put(Tags.NR_OBJECTS.tag(), nrObjects);
		}
		if (recommendedTracksList != null) {
			Vector<Object> v = new Vector<Object>();
			for (int i = 0; i < recommendedTracksList.size(); i++) {
				v.add(recommendedTracksList.get(i).createHashtable());
			}
			htab.put(Tags.RECOMMENDED_TRACKS.tag(), v);
		}
		return htab;
	}

	@Override
	public int getType() {
		return TRACKS_RESULTS;
	}
}