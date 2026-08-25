package com.securityintel.parser;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SecurityReportParserFactory {

    private final List<SecurityReportParser> parsers;

    public SecurityReportParserFactory(List<SecurityReportParser> parsers) {
        this.parsers = parsers;
    }

    /**
     * Finds the appropriate parser for the given report content
     * @param content The raw report content as JSON string
     * @return Optional containing the parser if found, empty otherwise
     */
    public Optional<SecurityReportParser> getParser(String content) {
        return parsers.stream()
                .filter(parser -> parser.supports(content))
                .findFirst();
    }

    /**
     * Gets all available parsers
     * @return List of all registered parsers
     */
    public List<SecurityReportParser> getAllParsers() {
        return parsers;
    }

    /**
     * Gets parser by tool name
     * @param toolName The tool name to search for
     * @return Optional containing the parser if found
     */
    public Optional<SecurityReportParser> getParserByTool(String toolName) {
        return parsers.stream()
                .filter(parser -> parser.getToolName().equalsIgnoreCase(toolName))
                .findFirst();
    }
}