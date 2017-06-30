/*
 * Copyright 2017 The Johns Hopkins University Applied Physics Laboratory LLC
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

public enum FolderType {

    INBOX("INBOX"),
    COMPLETE("Complete"),
    ERROR("Error");
    
    private final String type;
    private FolderType(String type) {
        this.type = type;
    }

    /**
     * Get the String value of the email type
     * 
     * @return the String value of the email type
     */
    public String getValue() {
        return type;
    }
    
    /**
     * Get the email type
     * 
     * @param value   the String value of the email type
     * @return the email type
     */
    public static FolderType getType(String value) {
        for (FolderType type : FolderType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
    
}