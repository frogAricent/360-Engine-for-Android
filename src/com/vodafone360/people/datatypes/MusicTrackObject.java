
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
 * BaseDataType encapsulating Recommended Music Track Object retrieved from Now + server
 */
public class MusicTrackObject extends BaseDataType {

	public String seedid = ""; //Trackid which was used as a seed to recommend this track
	public String trackid = ""; //The identifier of the requested track
	public String albumid = ""; //The identifier of the album that contains the track.
	public String artistid = ""; // (optional): The identifier of the artist performing the track.
	public String albumname = ""; //(optional): The name of the album that contains the track.
	public Integer indexinalbum = null; //(optional): the ordinal index of this track in the album
	public String artistname = ""; //(optional): The name of the artist performing the track.
	public String genreid = ""; //(optional): The identifier of the genre associated with the track.
	public Integer playbackseconds = null; //(optional): The amount of time, in seconds, required to play the track.
//	public String previewurl = ""; //(optional): A url pointing to a prelisten clip
	public List<String> licensetypelist = new ArrayList<String>(); //(optional): The license types under which the track is available
	public String name = ""; //(optional): The name to be used for display purposes.
	public String image = ""; //(optional): The location of the image for this object.
//	public Byte embeddedimage = null; //(optional): embedded image
	public String grouptitle = ""; //(optional): A free text field indicating the group of the playlist, of the chart or a text with the source of the recommendation
//	public Integer rating = null; //(optional): Rating associated to the content and express as a integer percent value. If used for the common 5 stars representation the values are: 0, 20, 40, 60, 80, 100
	
	
	@Override
	public String toString() {
		return trackid + ", " +  name + ", "  + albumname + ", " + artistname;
	}
	/**
	 * Tags associated with Recommended Music Track Object representing data items associated with
	 * it returned from server. 
	 * 
	 */
	public enum Tags {
		SEED_ID("seedid"),
		TRACK_ID("trackid"), 
		ALBUM_ID("albumid"), 
		ARTIST_ID("artistid"), 
		ALBUM_NAME("albumname"), 
		INDEX_IN_ALBUM("indexinalbum"), 
		ARTIST_NAME("artistname"), 
		GENRE_ID("genreid"), 
		PLAYBACK_SECONDS("playbackseconds"), 
//		PREVIEW_URL("previewurl"), 
		LICENSETYPE("licensetypelist"), 
		NAME("name"), 
		IMAGE("image"), 
//		EMBEDDED_IMAGE("embeddedimage"), 
		GROUP_TITLE("grouptitle");
//		RATING("rating");

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
			case SEED_ID:
				seedid = (String) value;
				break;
			case TRACK_ID:
				trackid = (String) value;
				break;
			case ALBUM_ID:
				albumid = (String) value;
				break;
			case ARTIST_ID:
				artistid =  (String) value;
				break;
			case ALBUM_NAME:
				albumname = (String) value; 
				break;
			case INDEX_IN_ALBUM:
				indexinalbum = (Integer) value;
				break;
			case ARTIST_NAME:
				artistname = (String) value;
				break;
			case GENRE_ID:
				genreid = (String) value;
				break;
			case PLAYBACK_SECONDS:
				playbackseconds = (Integer) value;
				break;
//			case PREVIEW_URL:
//				previewurl = (String) value;
//				break;
			case LICENSETYPE:
				Vector<String> vals = (Vector<String>)value;
				for (String licenseTypes : vals) {
					licensetypelist.add(licenseTypes);
				}
				break;
			case NAME:
				name = (String) value;
				break;
			case IMAGE:
				image = (String) value;
				break;
//			case EMBEDDED_IMAGE:
//				embeddedimage = (Byte) value;
//				break;
			case GROUP_TITLE:
				grouptitle = (String) value;
				break;
//			case RATING:
//				rating = (Integer) value;
//				break;
			default:
				// Do nothing.
				break;
			}
		}
	}

	/**
	 * Create Items from HashTable generated by Hessian-decoder
	 * 
	 * @param hash Hashtable representing MusicTrackObject
	 * @return Recommended Music Tracks created from Hashtable
	 * 
	 **/
	public static MusicTrackObject createFromHashtable(Hashtable<String, Object> hash) {
		MusicTrackObject tracks = new MusicTrackObject();
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
	 * Create Hashtable from MusicTrackObject parameters.
	 * 
	 * @param none
	 * @return Hashtable generated from MusicTrackObject parameters.
	 */
	public Hashtable<String, Object> createHashtable() {
		Hashtable<String, Object> htab = new Hashtable<String, Object>();
		if (seedid != null) {
			htab.put(Tags.SEED_ID.tag(), seedid);
		}
		if (trackid != null) {
			htab.put(Tags.TRACK_ID.tag(), trackid);
		}
		if (albumid!= null) {
			htab.put(Tags.ALBUM_ID.tag(), albumid);
		}
		if (artistid!= null) {
			htab.put(Tags.ARTIST_ID.tag(), artistid);
		}
		if (indexinalbum!= null) {
			htab.put(Tags.INDEX_IN_ALBUM.tag(), indexinalbum);
		}
		if (artistname!= null) {
			htab.put(Tags.ARTIST_NAME.tag(), artistname);
		}
		if (genreid!= null) {
			htab.put(Tags.GENRE_ID.tag(), genreid);
		}
		if (playbackseconds!= null) {
			htab.put(Tags.PLAYBACK_SECONDS.tag(), playbackseconds);
		}
//		if (previewurl!= null) {
//			htab.put(Tags.PREVIEW_URL.tag(), previewurl);
//		}
		if (licensetypelist!= null) {
			Vector<String> vL = new Vector<String>();
			for (String l : licensetypelist) {
				vL.add(l);
			}
		}
		if (name!= null) {
			htab.put(Tags.NAME.tag(), name);
		}
		if (image!= null) {
			htab.put(Tags.IMAGE.tag(), image);
		}
//		if (embeddedimage!= null) {
//			htab.put(Tags.EMBEDDED_IMAGE.tag(), embeddedimage);
//		}
		if (grouptitle!= null) {
			htab.put(Tags.GROUP_TITLE.tag(), grouptitle);
		}
//		if (rating!= null) {
//			htab.put(Tags.RATING.tag(), rating);
//		}
		return htab;
	}

	@Override
	public int getType() {
		return TRACKS;
	}
	
}
