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