package com.securityintel.parser;

public class SecurityReportParseException extends Exception {
    
    public SecurityReportParseException(String message) {
        super(message);
    }
    
    public SecurityReportParseException(String message, Throwable cause) {
        super(message, cause);
    }
}