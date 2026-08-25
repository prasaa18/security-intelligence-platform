package com.securityintel.parser;

import com.securityintel.model.Environment;

public interface SecurityReportParser {
    
    /**
     * Checks if this parser can handle the given report content
     * @param content The raw report content as JSON string
     * @return true if this parser can parse the content, false otherwise
     */
    boolean supports(String content);
    
    /**
     * Parses the security report content into a normalized format
     * @param content The raw report content as JSON string
     * @param serviceName The service name this report belongs to
     * @param environment The environment where this scan was performed
     * @return ParsedSecurityReport containing normalized findings
     * @throws SecurityReportParseException if parsing fails
     */
    ParsedSecurityReport parse(String content, String serviceName, Environment environment) 
        throws SecurityReportParseException;
    
    /**
     * Returns the name of the tool this parser handles
     * @return Tool name for identification
     */
    String getToolName();
}