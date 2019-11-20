package com.nullparams.hive.util;

public class ShortenMessage {

    public static String getShortDesc(String fullDescription) {

        while (fullDescription.contains("[%") ) {

            if (!fullDescription.contains("[%")) {
                break;
            }
            fullDescription = fullDescription.substring(0, fullDescription.indexOf("[%"))+fullDescription.substring(fullDescription.indexOf("%]")+2, fullDescription.length());
        }

        if (fullDescription.length() > 25) {
            return fullDescription.substring(0, 25).trim() + "...";
        } else {
            return fullDescription.trim();
        }
    }
}
