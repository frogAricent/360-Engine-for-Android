package com.vodafone360.people.datatypes;

import java.util.Hashtable;


public class ItemBlockRequest extends BaseDataType{

	public String nodeid = null;
	
	public Integer sublevels = null;
	
	public String grouprequestid = null;
	
	public Integer expired = null;
	
	public enum Tags {
		NODE_ID("nodeid"), SUBLEVELS("sublevels"), GROUPREQUESTID("grouprequestid"), EXPIRED(
				"expired");

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
		@SuppressWarnings("unused")
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
	 * @param value
	 *            Value associated with the tag
	 * @return void
	 */
	@SuppressWarnings("unused")
	private void setValue(Tags tag, Object value) {
		if (tag != null) {
			switch (tag) {
			case NODE_ID:
				nodeid = (String) value;
				break;

			case SUBLEVELS:
				sublevels = (Integer) value;
				break;

			case EXPIRED:
				expired = (Integer) value;
				break;

			case GROUPREQUESTID:
				grouprequestid = (String) value;
				break;

			default:
				// Do nothing.
				break;
			}
		}
	}
	/**
	 * Create Hashtable from Item Block parameters.
	 * 
	 * @param none
	 * @return Hashtable generated from Item Block parameters.
	 */
	public Hashtable<String, Object> createHashtable() {
		Hashtable<String, Object> htab = new Hashtable<String, Object>();

		if (nodeid != null) {
			htab.put(Tags.NODE_ID.tag(), nodeid);
		}
		if (sublevels != null) {
			htab.put(Tags.SUBLEVELS.tag(), sublevels);
		}
		if (grouprequestid != null) {
			htab.put(Tags.GROUPREQUESTID.tag(), grouprequestid);
		}
		if (expired != null) {
			htab.put(Tags.EXPIRED.tag(), expired);
		}
		return htab;
	}
	@Override
	public int getType() {
		return ITEM_BLOCK_REQUEST_DATATYPE;
	}

}
