package edu.jhuapl.dorset.demos;

public enum EmailType {

    INBOX("INBOX"),
    COMPLETE("Complete"),
    ERROR("Error"),
    UNREAD("unread email"),
    READ("read email");
    
    private final String type;
    private EmailType(String type) {
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
    public static EmailType getType(String value) {
        for (EmailType type : EmailType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
    
}