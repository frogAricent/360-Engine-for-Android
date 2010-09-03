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
import java.util.Vector;

import com.vodafone360.people.utils.LogUtils;

import android.util.Log;

/**
 * BaseDataType encapsulating an Album retrieved from, or to be issued
 * to, Now + server
 */
public class Album extends BaseDataType {
	
	/**
     * Album unique identifier.
     */
	public Long mAlbumid = null;
	
	/**
     * Album name.
     */
	public String mTitle = null;
	
	/**
     * Album name in URL encoded format.
     */
	public String mSlug = null;
	
	/**
     * Defines an icon for the album. Id of the Content that represents the icon.
     */
	public Long mIconid = null;
	
	/**
     * Defines an icon url for the album.
     */
	public String mIconurl = null;
	
	/**
     * True if the album can be only read. False otherwise.
     */
	public Boolean mReadonly = null;
	
	/**
     * Creation timestamp of the entity.
     */
	public Long mCreated = null;
	
	/**
     * Timestamp of the last change of the entity.
     */
	public Long mUpdated = null;
	
	/**
     * List of privacy groups for the album.
     */
	public List<GroupItem> mGrouplist = null;
	
	/**
     * Id of the last content item added to the album.
     */
	public Long mLastaddedcontentid = null;
	
	/**
     * Url of the last content item added to the album.
     */
	public String mLastaddedcontenturl = null;
	
	/**
     * Tags for fields associated with Album.
     */
    public enum Tags {
    	ALBUM_ID("albumid"), 
    	TITLE("title"),
    	SLUG("slug"),
    	ICON_ID("iconid"),
    	ICON_URL("iconurl"),
    	READONLY("readonly"),
    	CREATED("created"),
    	UPDATED("updated"),
    	GROUP_LIST("grouplist"),
    	LAST_ADDED_CONTENT_ID("lastaddedcontentid"), 
    	LAST_ADDED_CONTENT_URL("lastaddedcontenturl");

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

        /**
         * Find Tags item for specified String.
         * 
         * @param tag String value to find Tags item for
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
    
    private Tags findTag(String tag) {
        for (Tags tags : Tags.values()) {
            if (tag.compareTo(tags.tag()) == 0) {
                return tags;
            }
        }
        return null;
    }
    
    /**
     * Sets the value of the member data item associated with the specified tag.
     * 
     * @param tag Current tag
     * @param val Value associated with the tag
     * @return void
     */
    private void setValue(Tags tag, Object value) {
        if (tag != null) {
            switch (tag) {
                case ALBUM_ID:
                	mAlbumid = (Long)value;
                    break;

                case TITLE:
                	mTitle = (String)value;
                    break;

                case SLUG:
                    mSlug = (String)value;
                    break;

                case ICON_ID:
                    mIconid = (Long)value;
                    break;

                case ICON_URL:
                    mIconurl = (String)value;
                    break;

                case READONLY:
                    mReadonly = (Boolean)value;
                    break;

                case CREATED:
                    mCreated = (Long)value;
                    break;
                
                case UPDATED:
                    mUpdated = (Long)value;
                    break;

                case GROUP_LIST:
                	Vector<Hashtable<String, Object>> groupListVector = (Vector<Hashtable<String, Object>>)value;
	                 for (Hashtable<String, Object> hash : groupListVector) {
	                	 GroupItem t = new GroupItem();
	                     t = t.createFromHashtable(hash);
	                     mGrouplist.add(t);
	                 }
                     break;
                
                case LAST_ADDED_CONTENT_ID:
                	mLastaddedcontentid = (Long)value;
                    break;
                
                case LAST_ADDED_CONTENT_URL:
                	mLastaddedcontenturl = (String)value;
                    break;

                default:
                    // Do nothing.
                    break;
            }
        }
    }

	 /**
     * Create Album item from HashTable generated by Hessian-decoder
     * 
     * @param hash Hashtable representing Album
     * @return Album created from Hashtable
     */
    public Album createFromHashtable(Hashtable<String, Object> hash) {
    	Album album = new Album();
        Enumeration<String> e = hash.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            Object value = hash.get(key);
            Tags tag = Tags.findTag(key);
            album.setValue(tag, value);
        }
        return album;
    }
    
    /**
     * Create Hashtable from Album parameters.
     * 
     * @param none
     * @return Hashtable generated from Album parameters.
     */
    public Hashtable<String, Object> createHashtable() {
        Hashtable<String, Object> htab = new Hashtable<String, Object>();
      
        
        if (mAlbumid != null) {
            htab.put(Tags.ALBUM_ID.tag(), mAlbumid);
        }
        if (mTitle != null) {
            htab.put(Tags.TITLE.tag(), mTitle);
        }
        if (mSlug != null) {
            htab.put(Tags.SLUG.tag(), mSlug);
        }
        if (mIconid != null) {
            htab.put(Tags.ICON_ID.tag(), mIconid);
        }
        if (mIconurl != null) {
            htab.put(Tags.ICON_URL.tag(), mIconurl);
        }
        if (mReadonly != null) {
            htab.put(Tags.READONLY.tag(), mReadonly);
        }
        if (mCreated != null) {
            htab.put(Tags.CREATED.tag(), mCreated);
        }
        if (mUpdated != null) {
            htab.put(Tags.UPDATED.tag(), mUpdated);
        }
        if (mGrouplist != null) {
        	 Vector<Object> v = new Vector<Object>();
             for (int i = 0; i < mGrouplist.size(); i++) {
                 v.add(mGrouplist.get(i).createHashtable());
             }
             htab.put(Tags.GROUP_LIST.tag(), v);
        }
        if (mLastaddedcontentid != null) {
            htab.put(Tags.LAST_ADDED_CONTENT_ID.tag(), mLastaddedcontentid);
        }
        if (mLastaddedcontenturl != null) {
            htab.put(Tags.LAST_ADDED_CONTENT_URL.tag(), mLastaddedcontenturl);
        }
        return htab;
    }

	@Override
	public int getType() {
		return ALBUM_DATATYPE;
	}

}
