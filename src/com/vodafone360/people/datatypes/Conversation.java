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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.vodafone360.people.service.io.rpg.RpgPushMessage;
import com.vodafone360.people.utils.LogUtils;

public class Conversation extends BaseDataType {
    private String mConversationId; // the unique server-supplied conversation
                                    // id

    private Long mUserId; // the "from" user

    private String mType = "c1";

    private List<String> mTos;

    private static final String CONVERSATION_ID = "conversation";

    private static final String TOS = "tos";

    public enum Tags {
        USERID("userid"),
        TYPE("type"),
        PAYLOAD("payload");

        private final String mTag;

        private Tags(String tag) {
            mTag = tag;
        }

        public String tag() {
            return mTag;
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

    /**
     * Create ChatMessage from Hashtable generated by Hessian-decoder
     * 
     * @param hash Hashtable containing ChatMessage parameters
     */
    public void createFromHashtable(Hashtable<String, Object> hash) {
        LogUtils.logI("Conversation.createFromHashtable() hash[" + hash.toString() + "]");
        Enumeration<String> e = hash.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            Tags tag = Tags.findTag(key);
            if (tag != null) {
                setValue(tag, hash.get(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setValue(Tags key, Object value) {
        switch (key) {
            case PAYLOAD:
                Hashtable payload = (Hashtable)value;
                if (payload.containsKey(CONVERSATION_ID)) {
                    mConversationId = (String)payload.get(CONVERSATION_ID);
                }
                if (payload.containsKey(TOS)) {
                    mTos = (List<String>)payload.get(TOS);
                }
                break;
            case USERID:
                mUserId = (Long)value;
                break;
            case TYPE:
                mType = (String)value;
                break;
            default:
                LogUtils.logE("Conversation.setValue() key[" + key + "] value[" + value
                        + "] Unsupported KEY");
        }
    }

    public Conversation() {
    }

    protected Conversation(RpgPushMessage msg) {
        createFromHashtable(msg.mHash);
    }

    @Override
    public int getType() {
        return CONVERSATION_DATA_TYPE;
    }

    public String getConversationId() {
        return mConversationId;
    }

    public void setConversationId(String conversationId) {
        this.mConversationId = conversationId;
    }

    public List<String> getTos() {
        return mTos;
    }

    public void setTos(List<String> tos) {
        this.mTos = tos;
    }

    public String getConversationType() {
        return mType;
    }

    public Long getUserId() {
        return mUserId;
    }

    public void setUserId(Long userId) {
        this.mUserId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = 
            new StringBuilder("Conversation [CONVERSATION_ID=");
        sb.append(CONVERSATION_ID);
        sb.append(", TOS="); sb.append(TOS);
        sb.append(", mConversationId="); sb.append(mConversationId);
        sb.append(", mUserId=");  sb.append(mUserId); 
        sb.append(", mTos="); sb.append(mTos); 
        sb.append(", mType="); sb.append(mType); sb.append("]");
        return sb.toString();
    }
}
