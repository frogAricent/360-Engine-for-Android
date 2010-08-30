package com.vodafone360.people.datatypes;


import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;





import android.os.Parcel;
import android.os.Parcelable;

public class Group extends BaseDataType implements Parcelable {
	
	
	private enum Tags {
        USER_ID("userid"),
        NAME("name"),
        ID("id"),
        COLOR("color"),
        NETWORK("network");
        

        private final String tag;

        /**
         * Construct Tags item from supplied String.
         * 
         * @param s String value for Tags item.
         */
        private Tags(String s) {
            tag = s;
        }

        /**
         * String value for Tags item.
         * 
         * @return String value for Tags item.
         */
        private String tag() {
            return tag;
        }

        /**
         * Find Tags item for specified String.
         * 
         * @param tag String value to find in Tags items.
         * @return Tags item for specified String, NULL otherwise.
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
	
	public Long mId = null;
	
	public Long mUserId = null;
	
	public String mName = null;
	
	public String mImageMimeType = null;

	public ByteBuffer mImageBytes = null;
	
	public String mColor = null; 
	
	public String mNetwork = null;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	/**
     * Populate GroupItem from supplied Hashtable.
     * 
     * @param hash Hashtable containing identity capability details
     * @return GroupItem instance
     */
    public Group createFromHashtable(Hashtable<String, Object> hash) {
        Enumeration<String> e = hash.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            Object value = hash.get(key);
            Tags tag = Tags.findTag(key);
            if (tag != null)
                setValue(tag, value);
        }

        return this;
    }
    
    /**
     * Sets the value of the member data item associated with the specified tag.
     * 
     * @param tag Current tag
     * @param val Value associated with the tag
     */
    private void setValue(Tags tag, Object val) {
        switch (tag) {
            case NETWORK:
                mNetwork = (String)val;
                break;

            case ID:
                mId = (Long)val;
                break;

            case NAME:
                mName = (String)val;
                break;

           

            case USER_ID:
                mUserId = (Long)val;
                break;

            case COLOR:
                mColor = (String)val;
                break;

            
                
            default:
                // Do nothing.
                break;
        }
    }
	
	/**
     * Create Hashtable from Group parameters.
     * 
     * @return Hashtable generated from Group parameters.
     */
    public Hashtable<String, Object> createHashtable() {
        Hashtable<String, Object> htab = new Hashtable<String, Object>();

        if (mId != null) {
            htab.put(Tags.ID.tag(), mId);
        }
        if (mUserId != null) {
            htab.put(Tags.USER_ID.tag(), mUserId);
        }
        if (mName != null) {
            htab.put(Tags.NAME.tag(), mName);
        }
        if (mImageMimeType != null) {
            htab.put("imagemimetype", mImageMimeType);
        }
        if (mImageBytes != null) {
            htab.put("imagebytes", mImageBytes);
        }
        if (mColor != null) {
            htab.put(Tags.COLOR.tag(), mColor);
        }
        if (mNetwork != null){
        	htab.put(Tags.NETWORK.tag(), mNetwork);
        }

        return htab;
    }
	
	private enum MemberData {
        ID,
        USER_ID,
        NAME,
        IMAGE_MIME_TYPE,
        IMAGE_BYTES,
        COLOR,
        NETWORK;
    }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		boolean [] validDataList = new boolean[MemberData.values().length];
		int initialPos = dest.dataPosition();
		dest.writeBooleanArray(validDataList); // Plaace holder for real array
		
		if(mId != null){
			validDataList[MemberData.ID.ordinal()] = true;
			dest.writeLong(mId);
		}
		if(mUserId != null){
			validDataList[MemberData.USER_ID.ordinal()] = true;
			dest.writeLong(mUserId);
		}
		if(mName != null){
			validDataList[MemberData.NAME.ordinal()] = true;
			dest.writeString(mName);
		}
		if(mImageMimeType != null){
			validDataList[MemberData.IMAGE_MIME_TYPE.ordinal()] = true;
			dest.writeString(mImageMimeType);
		}
		if (mImageBytes != null) {
            validDataList[MemberData.IMAGE_BYTES.ordinal()] = true;
            dest.writeByteArray(mImageBytes.array());
        }
		if (mColor != null) {
            validDataList[MemberData.COLOR.ordinal()] = true;
            dest.writeString(mColor);
        }
		if (mNetwork != null) {
            validDataList[MemberData.NETWORK.ordinal()] = true;
            dest.writeString(mNetwork);
        }
		
		int currentPos = dest.dataPosition();
        dest.setDataPosition(initialPos);
        dest.writeBooleanArray(validDataList); // Real array.
        dest.setDataPosition(currentPos);
	}

	@Override
	public int getType() {
		return GROUP_DATATYPE;
	}

}
