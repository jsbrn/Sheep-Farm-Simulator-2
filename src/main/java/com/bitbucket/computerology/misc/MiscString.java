package com.bitbucket.computerology.misc;

import java.util.Enumeration;
import java.util.Vector;

public class MiscString {
    
    /**
     * Wraps the given text. Credit to Robert Hanson from <i>progcookbook.com</i>. I modified it slightly
     * to check the actual on-screen width of the String with the given font.
     * @param text The text to wrap.
     * @param len The maximum number of characters per line.
     * @return An array of Strings.
     */
    public static String[] wrap(String text, int len, org.newdawn.slick.Font font) {
        // return empty array for null text
        if (text == null)
        return new String [] {};

        // return text if len is zero or less
        if (len <= 0)
        return new String [] {text};
        
        // return text if the font is null
        if (font == null)
        return new String [] {text};

        // return text if less than length
        if (font.getWidth(text) <= len)
        return new String [] {text};

        char [] chars = text.toCharArray();
        Vector lines = new Vector();
        StringBuilder line = new StringBuilder();
        StringBuffer word = new StringBuffer();

        for (int i = 0; i < chars.length; i++) {
          word.append(chars[i]);

          if (chars[i] == ' ') {
            if (font.getWidth(line.toString())
                    +font.getWidth(word.toString()) > len) {
              lines.add(line.toString());
              line.delete(0, line.length());
            }

            line.append(word);
            word.delete(0, word.length());
          }
        }

        // handle any extra chars in current word
        if (word.length() > 0) {
          if (font.getWidth(line.toString())
                  +font.getWidth(word.toString()) > len) {
            lines.add(line.toString());
            line.delete(0, line.length());
          }
          line.append(word);
        }

        // handle extra line
        if (line.length() > 0) {
          lines.add(line.toString());
        }

        String [] ret = new String[lines.size()];
        int c = 0; // counter
        for (Enumeration e = lines.elements(); e.hasMoreElements(); c++) {
          ret[c] = (String) e.nextElement();
        }

        return ret;
    }
}
