package com.bitbucket.computerology.misc;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

public class MiscString {

    /**
     * Wraps the given text. Credit to Robert Hanson from <i>progcookbook.com</i>. I modified it slightly
     * to check the actual on-screen width of the String with the given font.
     *
     * @param text  The text to wrap.
     * @param width The maximum line width from pixels.
     * @return An array of Strings.
     */
    public static String[] wrap(String text, int width, org.newdawn.slick.Font font) {
        // return empty array for null text
        if (text == null)
            return new String[]{};

        // return text if len is zero or less
        if (width <= 0)
            return new String[]{text};

        // return text if the font is null
        if (font == null)
            return new String[]{text};

        // return text if less than length
        if (font.getWidth(text) <= width)
            return new String[]{text};

        char[] chars = text.toCharArray();
        Vector lines = new Vector();
        StringBuilder line = new StringBuilder();
        StringBuffer word = new StringBuffer();

        for (int i = 0; i < chars.length; i++) {
            word.append(chars[i]);

            if (chars[i] == ' ') {
                if (font.getWidth(line.toString())
                        + font.getWidth(word.toString()) > width) {
                    lines.add(line.toString());
                    line.delete(0, line.length());
                }

                line.append(word);
                word.delete(0, word.length());
            }
        }

        // handle any extra chars from current word
        if (word.length() > 0) {
            if (font.getWidth(line.toString())
                    + font.getWidth(word.toString()) > width) {
                lines.add(line.toString());
                line.delete(0, line.length());
            }
            line.append(word);
        }

        // handle extra line
        if (line.length() > 0) {
            lines.add(line.toString());
        }

        String[] ret = new String[lines.size()];
        int c = 0; // counter
        for (Enumeration e = lines.elements(); e.hasMoreElements(); c++) {
            ret[c] = (String) e.nextElement();
        }

        return ret;
    }

    /**
     * Breaks any String object into a list of Strings. The character "\n" acts as a breakpoint.
     *
     * @param s The String to parse.
     * @return An ArrayList of Strings for your viewing pleasure.
     */
    public static ArrayList<String> parseString(String s) {
        ArrayList<String> strs = new ArrayList<String>();
        String command = "";
        for (int i = 0; i != s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                strs.add(command);
                command = "";
            } else {
                command += c;
                if (i == s.length() - 1) {
                    strs.add(command);
                }
            }
        }
        return strs;
    }

}
