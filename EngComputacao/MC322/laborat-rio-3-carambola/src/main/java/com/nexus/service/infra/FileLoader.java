package com.nexus.service.infra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading files from the classpath.
 * 
 * <p>This class provides functionality to read text files from the application's
 * classpath and return their contents as a list of strings, with each line
 * represented as a separate string element.</p>
 * 
 * <p>The class is designed to work with resource files packaged within the
 * application JAR or available in the classpath during development.</p>
 */
public class FileLoader {
    
    /**
     * Loads a file from the classpath and returns its contents as a list of strings.
     * 
     * <p>This method reads a text file from the classpath, splits it into individual
     * lines, and returns each line as an element in a List. The method handles
     * different line ending formats (Windows, Unix, Mac) automatically.</p>
     * 
     * <p>If the file is empty, an empty list is returned. If the file contains
     * only whitespace, a list with empty strings corresponding to the line breaks
     * will be returned.</p>
     *
     * @param fileName the name of the file to load from the classpath
     * @return a List of strings, where each string represents a line from the file
     * @throws IOException if the file is not found in the classpath or cannot be read
     */
    public List<String> loadFile(String fileName) throws IOException {
        var resource = getClass().getClassLoader().getResourceAsStream(fileName);
        
        if (resource == null) {
            throw new IOException("Arquivo não encontrado no classpath: " + fileName);
        }
        
        List<String> lines = new ArrayList<String>();
        try (java.util.Scanner s = new java.util.Scanner(resource).useDelimiter("\\A")) {
            String content = s.hasNext() ? s.next() : "";
            lines = List.of(content.split("\\R"));
        }

        return lines;
    }
}