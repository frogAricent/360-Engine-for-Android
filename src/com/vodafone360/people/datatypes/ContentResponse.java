package com.vodafone360.people.datatypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import android.util.Log;

import com.vodafone360.people.utils.LogUtils;

public class ContentResponse extends BaseDataType {

	public List<Long> mContentIdList = new ArrayList<Long>();
	
	 private enum Tags {
		 CONTENTLIST("contentlist"),
		 ITEMS("items"),
		 UPDATED("updated");
		 
	        private final String tag;

	        /**
	         * Construct Tags item from supplied String
	         * 
	         * @param s String value for Tags item.
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
	    
	  public ContentResponse createFromHashtable(Hashtable<String, Object> hash) throws IOException{
		  LogUtils.logI("HessianDecoder.createFromHashtable() hash[" + hash.toString() + "]");
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
	  
	  private void setValue(Tags tag, Object value) throws IOException {
	        if (tag != null) {
	            switch (tag) {
	            case CONTENTLIST:
	            	Vector<Hashtable<String, Object>> contentsVector = (Vector<Hashtable<String, Object>>)value;
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
	                	mUpdated = (Long)value;
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
