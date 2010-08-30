package com.vodafone360.people.datatypes;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;



import com.vodafone360.people.utils.LogUtils;

public class LongGeocodeAddress extends BaseDataType {

	public String COUNTRY_NAME;
	public String CITY_NAME;
	public String AREA_NAME;
	public String STREET_NAME;
	public String LATITUDE;
	public String LONGITUDE;
     
    
    private enum Tags {
    	
        COUNTRY_NAME("countryname"),
        CITY_NAME("cityname"),
        AREA_NAME("areaname"),
        STREET_NAME("streetname"),
        LATITUDE("latitude"),
        LONGITUDE("longitude");
        

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
	
	public LongGeocodeAddress createFromHashtable(Hashtable<String, Object> hash) {
		
		System.out.println("LongGeocodeAddress.createFromHashtable() START ");
		LongGeocodeAddress cont = new LongGeocodeAddress();
        Enumeration<String> e = hash.keys();
        Hashtable data = null;   
       
        
      while (e.hasMoreElements()) 
      {
    	  String key = e.nextElement();
    	  System.out.println("keykeykeykeykeykeykeykey : " + key);
          Object value = hash.get(key);
          
         if(value != null)
          {
        	  Vector v = (Vector) value;
        	  if(v != null)
        	  {
        		  int s = v.size();
        		  for(int i=0; i<s; i++)
        		  {
        			  Hashtable has = (Hashtable) v.elementAt(i);
        			  System.out
							.println("LongGeocodeAddress.createFromHashtable() : v.elementAt(i) : "+v.elementAt(i).toString());
        			  Enumeration<String> e1 = has.keys();
        		      
        		        while (e1.hasMoreElements()) {
        		            String key1 = e1.nextElement();
        		            System.out
									.println("LongGeocodeAddress.createFromHashtable() keyssssssss : "+key1.toString());
        		            
        		            Object value1 =has.get(key1);
        		            System.out
							.println("LongGeocodeAddress.createFromHashtable() value : "+value1.toString());
        		            Tags tag = cont.findTag(key1);
        		            cont.setValue(tag, value1.toString());
        		        }
        		  }
        		  
        	  }
          }
          
//          Tags tag = cont.findTag(key);
//           
//          cont.setValue(tag, value);
//          data = (Hashtable)hash.get(key); 
      
      }
      
       System.out.println("LongGeocodeAddress.createFromHashtable() cont.CITY_NAME.toString() : "+cont.CITY_NAME.toString());
        return cont;
    }
	
	 private Tags findTag(String tag) {
		 System.out.println("LongGeocodeAddress.findTag() ---> "+tag);
	        for (Tags tags : Tags.values()) {
	            if (tag.compareTo(tags.tag()) == 0) {
	            	System.out.println("LongGeocodeAddress.findTag()"+tags);
	                return tags;
	            }
	        }
	        LogUtils.logE("Contact.findTag - Unsupported contact tag: " + tag);
	        return null;
	    }
	 
	 private void setValue(Tags tag, Object value) {
		 System.out.println("LongGeocodeAddress.setValue() tag : "+tag+" :  value :"+value);
		 
	        if (tag == null) {
	            LogUtils.logE("Contact setValue tag is null");
	            return;
	        }
	        
	       
	    	
	        switch (tag) {
	            case COUNTRY_NAME:
	            	COUNTRY_NAME = (String)value;
	            	System.out.println("LongGeocodeAddress.setValue() COUNTRY_NAME : "+COUNTRY_NAME);
	                break;
	            case CITY_NAME:
	            	CITY_NAME = (String)value;
	            	System.out.println("LongGeocodeAddress.setValue() CITY_NAME : "+CITY_NAME);
	                break;
	            case AREA_NAME:
	            	AREA_NAME = (String)value;
	            	System.out.println("LongGeocodeAddress.setValue() AREA_NAME : "+AREA_NAME);
	                break;
	            case STREET_NAME:
	            	STREET_NAME = (String)value;
	                break;
	            case LATITUDE:
	            	LATITUDE = (String)value;
	            	System.out.println("LongGeocodeAddress.setValue() LATITUDE : "+LATITUDE);
	                break;
	            case LONGITUDE:
	            	LONGITUDE = (String)value;
	            	System.out.println("LongGeocodeAddress.setValue() LONGITUDE : "+LONGITUDE);
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
