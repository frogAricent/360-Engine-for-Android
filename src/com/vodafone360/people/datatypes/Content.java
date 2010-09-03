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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.util.Log;

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating an Content retrieved from, or to be issued to, Now
 * + server
 */
public class Content extends BaseDataType {

	public Long mContentid = null; // (optional); // Unique identifier for the
	// file.

	public String mRemoteid = null; // Unique identifier for a remote file.

	public String mExtfid = null; // (optional); // External identifier for the
	// message. Internal use only.

	public String mTitle = null; // (optional); // Title of the file.

	public String mFilename = null; // (optional); // Name of the file.

	public String mDescription = null; // (optional); // Description of the
	// file.

	public byte[] mBytes = null; // Contains the binary information of the file.

	public String mBytesmime = null; // Contains the mime type information of
	// the file.

	public String mPreviewurl = null; // Defines an http url that the client can
	// use to retrieve binary data of the
	// preview image.

	public String mStore = null; // (optional) Name of the store type for this
	// file. This field contains information
	// about the originator network. By default,
	// should be set to local

	public Long mTime = null; // (optional) Timestamp representing the time of
	// the file. Can be not related to
	// creation/updated time.

	public List<Tag> mTaglist = new ArrayList<Tag>(); // The list of Tags which
	// are associated with
	// the content item

	public Long mMaxage = null; // The number of seconds the file can be cached
	// without re-requesting it

	public String mSystem = null; // Describes the usage of the content i.e
	// 'profile'. Default is null.

	public Map<String, String> mMeta; // Meta data

	public List<Comment> mComments = new ArrayList<Comment>(); // Comments

	public List<Album> mAlbumlist = new ArrayList<Album>();// albums

	public List<Long> mAlbumIdList = new ArrayList<Long>();// albums

	public Integer mTagscount; // Number of tags associated with this entity.

	public Integer mCommentscount; // Number of comments associated with this
	// entity.

	public String mUploadedviaappid; // App id of client that uploaded the file.

	public String mUploadedviaapptype; // App type of client that uploaded the
	// file.

	public Long mFilesize;

	public String mUrl;

	private enum Tags {
		CONTENTID("contentid"), REMOTEID("remoteid"), EXT_FILE_ID("extfid"), DESCRIPTION(
				"description"), TITLE("title"), STORE("store"), TIME("time"), FILE_NAME(
				"filename"), BYTES("bytes"), BYTES_MIME_TYPE("bytesmime"), PREVIEW_URL(
				"previewurl"), TAG_LIST("taglist"), MAX_AGE("maxage"), SYSTEM(
				"system"), METADATA("meta"), COMMENTS("comments"), ALBUM_LIST(
				"albumlist"), ALBUMID_LIST("albumidlist"), TAGS_COUNT(
				"tagscount"), COMMENTS_COUNT("commentscount"), UPLOADED_VIA_APP_ID(
				"uploadedviaappid"), UPLOADED_VIOA_APP_TYPE(
				"uploadedviaapptype"), FILE_SIZE("filesize"), URL("url");

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
	 * Create Content containing ContactDetail parameters
	 * 
	 * @return Hashtable containing Content detail parameters
	 */
	public Hashtable<String, Object> createHashtable() {
		Hashtable<String, Object> htab = new Hashtable<String, Object>();

		if (mContentid != null) {
			htab.put(Tags.CONTENTID.tag(), mContentid);
		}
		if (mRemoteid != null) {
			htab.put(Tags.REMOTEID.tag(), mRemoteid);
		}
		if (mFilename != null) {
			htab.put(Tags.FILE_NAME.tag(), mFilename);
		}
		if (mTitle != null) {
			htab.put(Tags.TITLE.tag(), mTitle);
		}
		if (mFilename != null) {
			htab.put(Tags.FILE_NAME.tag(), mFilename);
		}
		if (mBytes != null) {
			htab.put(Tags.BYTES.tag(), mBytes);
		}
		if (mBytesmime != null) {
			htab.put(Tags.BYTES_MIME_TYPE.tag(), mBytesmime);
		}
		if (mTaglist != null) {
			Vector<Object> v = new Vector<Object>();
			for (int i = 0; i < mTaglist.size(); i++) {
				v.add(mTaglist.get(i).createHashtable());
			}
			htab.put(Tags.TAG_LIST.tag(), v);
		}
		if (mTagscount != null) {
			htab.put(Tags.TAGS_COUNT.tag(), mTagscount);
		}
		if (mAlbumlist != null) {
			Vector<Object> v = new Vector<Object>();
			for (int i = 0; i < mAlbumlist.size(); i++) {
				v.add(mAlbumlist.get(i).createHashtable());
			}
			htab.put(Tags.ALBUM_LIST.tag(), v);
		}
		if (mAlbumIdList != null) {
			Vector<Long> vL = new Vector<Long>();
			for (Long l : mAlbumIdList) {
				vL.add(l);
			}
			htab.put(Tags.ALBUMID_LIST.tag(), vL);
		}
		if (mCommentscount != null) {
			htab.put(Tags.COMMENTS_COUNT.tag(), mCommentscount);
		}
		if (mComments != null) {
			Vector<Object> v = new Vector<Object>();
			for (int i = 0; i < mComments.size(); i++) {
				v.add(mComments.get(i).createHashtable());
			}
			htab.put(Tags.COMMENTS.tag(), v);
		}
		if (mSystem != null) {
			htab.put(Tags.SYSTEM.tag(), mSystem);
		}
		if (mMaxage != null) {
			htab.put(Tags.MAX_AGE.tag(), mMaxage);
		}
		if (mMeta != null) {
			htab.put(Tags.METADATA.tag(), mMeta);
		}
		if (mUploadedviaapptype != null) {
			htab.put(Tags.UPLOADED_VIOA_APP_TYPE.tag(), mUploadedviaapptype);
		}
		if (mUploadedviaappid != null) {
			htab.put(Tags.UPLOADED_VIA_APP_ID.tag(), mUploadedviaappid);
		}
		if (mFilesize != null) {
			htab.put(Tags.FILE_SIZE.tag(), mFilesize);
		}
		if (mUrl != null) {
			htab.put(Tags.URL.tag(), mUrl);
		}
		if (mPreviewurl != null) {
			htab.put(Tags.PREVIEW_URL.tag(), mPreviewurl);
		}
		if (mMeta != null) {
			htab.put(Tags.METADATA.tag(), mMeta);
		}
		if (mExtfid != null) {
			htab.put(Tags.EXT_FILE_ID.tag(), mExtfid);
		}
		if (mDescription != null) {
			htab.put(Tags.DESCRIPTION.tag(), mDescription);
		}
		if (mStore != null) {
			htab.put(Tags.STORE.tag(), mStore);
		}
		if (mTime != null) {
			htab.put(Tags.TIME.tag(), mTime);
		}

		return htab;
	}

	/**
	 * Create Content from Hashtable (generated from Hessian encoded response).
	 * 
	 * @param hash
	 *            Hashtable containing Content data
	 * @return Content created from supplied Hashtable.
	 */
	public Content createFromHashtable(Hashtable<String, Object> hash)
			throws IOException {
		LogUtils.logI("HessianDecoder.createFromHashtable() hash["
				+ hash.toString() + "]");
		Content content = new Content();
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
	private void setValue(Tags tag, Object value) throws IOException {
		if (tag != null) {
			switch (tag) {

			case CONTENTID:
				mContentid = (Long) value;
				LogUtils.logD("Content.setValue setting content id = "
						+ mContentid);

				break;

			case REMOTEID:
				mRemoteid = (String) value;
				break;

			case TITLE:
				mExtfid = (String) value;
				break;

			case FILE_NAME:
				mTitle = (String) value;
				break;

			case BYTES:
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(bos);
					oos.writeObject(value);
					oos.flush();
					oos.close();
					bos.close();
					mBytes = bos.toByteArray();
				} catch (Exception e) {
					LogUtils.logE("Exception in encoding byte array");
				}
				break;

			case BYTES_MIME_TYPE:
				mBytesmime = (String) mBytesmime;
				break;

			case PREVIEW_URL:
				mPreviewurl = (String) value;
				break;

			case TAG_LIST:
				Vector<Hashtable<String, Object>> tagListVector = (Vector<Hashtable<String, Object>>) value;
				for (Hashtable<String, Object> hash : tagListVector) {
					Tag t = new Tag();
					t = Tag.createFromHashtable(hash);
					mTaglist.add(t);
				}
				break;

			case MAX_AGE:
				mMaxage = (Long) value;
				break;

			case SYSTEM:
				mSystem = (String) value;
				break;

			// case METADATA:
			// // mMeta = (Long)value;
			// //change
			// break;

			case COMMENTS:
				Vector<Hashtable<String, Object>> commentsVector = (Vector<Hashtable<String, Object>>) value;
				for (Hashtable<String, Object> hash : commentsVector) {
					Comment comment = new Comment();
					comment = Comment.createFromHashtable(hash);
					mComments.add(comment);
				}
				break;

			case ALBUM_LIST:
				Vector<Hashtable<String, Object>> albumsVector = (Vector<Hashtable<String, Object>>) value;
				for (Hashtable<String, Object> hash : albumsVector) {
					Album album = new Album();
					album.createFromHashtable(hash);
					mAlbumlist.add(album);
				}
				break;
			case ALBUMID_LIST:
				Vector<Long> gL = (Vector<Long>) value;
				for (Long l : gL) {
					mAlbumIdList.add(l);
				}
				break;

			case TAGS_COUNT:
				mTagscount = (Integer) mTagscount;
				break;

			case COMMENTS_COUNT:
				mCommentscount = (Integer) value;
				break;

			case UPLOADED_VIA_APP_ID:
				mUploadedviaappid = (String) value;
				break;

			case UPLOADED_VIOA_APP_TYPE:
				mUploadedviaapptype = (String) value;
				break;

			case FILE_SIZE:
				mFilesize = (Long) value;
				break;

			case URL:
				mUrl = (String) value;
				break;

			default:
				break;
			}
		}
	}

	@Override
	public int getType() {
		return CONTENT_DATATYPE;
	}
}
