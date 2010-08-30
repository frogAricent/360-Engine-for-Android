package com.vodafone360.people.datatypes;

import java.util.Enumeration;
import java.util.Hashtable;

import com.vodafone360.people.utils.LogUtils;

public class LocationNudgeResult extends BaseDataType
{
		public Boolean success;
		public String reason ;
			     
	    
	    private enum Tags {
	    	
	        SUCCESS("success"),
	        REASON("reason");
	       

	        private final String tag;

	        /**
	         * Constructor for Tags item.
	         * 
	         * @param s String value associated with Tag.
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
		
		public static LocationNudgeResult createFromHashtable(Hashtable<String, Object> hash) {
			LocationNudgeResult cont = new LocationNudgeResult();
	        Enumeration<String> e = hash.keys();
	        while (e.hasMoreElements()) {
	            String key = e.nextElement();
	            Object value = hash.get(key);
	            Tags tag = cont.findTag(key);
	            cont.setValue(tag, value);
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
		 
		 private void setValue(Tags tag, Object value) {
		        if (tag == null) {
		            LogUtils.logE("Contact setValue tag is null");
		            return;
		        }
		    	
		        switch (tag) {
		            case SUCCESS:
		            	success = (Boolean) value;
		                break;
		            case REASON:
		            	reason = (String)value;
		                break;
		           		           
		            default:
		                // Do nothing.
		                break;
		        }
		    }

		@Override
		public int getType() {
			return LOCATION_NUDGE_RESULT_DATATYPE;
		}
	}
