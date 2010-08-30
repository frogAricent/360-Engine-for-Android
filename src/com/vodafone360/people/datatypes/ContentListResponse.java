package com.vodafone360.people.datatypes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import android.util.Log;

import com.vodafone360.people.utils.LogUtils;

public class ContentListResponse extends BaseDataType{
	

	public List<Long> mContentIdList = new ArrayList<Long>();
	
		 private enum Tags {
			 CONTENTIDLIST("contentidlist");

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

		 public ContentListResponse createFromHashtable(Hashtable<String, Object> hash) {
			  LogUtils.logI("HessianDecoder.createFromHashtable() hash[" + hash.toString() + "]");
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
		  
		  private void setValue(Tags tag, Object value) {
		        if (tag != null) {
		            switch (tag) {
		            case CONTENTIDLIST:
		            	Vector<Long> contentIdVector = (Vector<Long>)value;
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
