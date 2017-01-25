/*
 * Copyright 2016 The Johns Hopkins University Applied Physics Laboratory LLC
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.jhuapl.dorset.demos;

import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.replies.EmojiSlackReply;

import java.util.Map;

public class SlackUtils {

    /**
     * Create emoji dictionary
     * 
     * @param session  Slack session
     * @return emojis
     */
    public Map<String, String> createEmojiDict(SlackSession session) {
        SlackMessageHandle<EmojiSlackReply> handle = session.listEmoji();
        Map<String, String> emojis = handle.getReply().getEmojis();
        return emojis;
    }

}


