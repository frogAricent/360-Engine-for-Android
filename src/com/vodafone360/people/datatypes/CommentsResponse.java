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
 * BaseDataType encapsulating an CommentsResponse retrieved from
 *  Now + server
 */
public class CommentsResponse extends BaseDataType {

	public List<Long> mCommentIdList = new ArrayList<Long>();

	 /**
     * Tags associated with CommentsResponse item.
     */
    private enum Tags {
        COMMENT_ID("commentidlist");

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
         * String value for Tags item.
         * 
         * @return String value for Tags item.
         */
        private String tag() {
            return tag;
        }
    }
    /**
     * Find Tags item for specified String
     * 
     * @param tag String value to find Tags item for
     * @return Tags item for specified String, null otherwise
     */
    private Tags findTag(String tag) {
        for (Tags tags : Tags.values()) {
            if (tag.compareTo(tags.tag()) == 0) {
                return tags;
            }
        }
        return null;
    }
    
    /**
     * Create CommentsResponse from Hashtable generated by Hessian-decoder
     * 
     * @param hash Hashtable containing CommentsResponse parameters
     * @return CommentsResponse created from hashtable
     */
    public CommentsResponse createFromHashtable(Hashtable<String, Object> hash) {

        Enumeration<String> e = hash.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            Tags tag = findTag(key);
            setValue(tag, hash.get(key));
        }
        return this;
    }

    /**
     * Sets the value of the member data item associated with the specified tag.
     * 
     * @param tag Current tag
     * @param value Value associated with the tag
     */
    @SuppressWarnings("unchecked")
	private void setValue(Tags tag, Object value) {
        if (tag != null) {
        	switch (tag) {
	            case COMMENT_ID:
	            	Vector<Long> vector = (Vector<Long>)value;
	                 for (Long l : vector) {
	                	 mCommentIdList.add(l);
	                 }
	                break;
	            default:
	                break;
	        }
        }
    }

	@Override
	public int getType() {
		return COMMENTS_RESPONSE_DATATYPE;
	}
}
