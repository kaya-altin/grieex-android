package com.grieex.core;

import android.text.Html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CoreUtils {
    public static ParseResult GetText(String strData, String strStartString, String strEndString, int nStartPos) {
        int nStart = strData.indexOf(strStartString, nStartPos);
        if (nStart == -1) {
            return new ParseResult(nStartPos, "");
        }
        nStart += strStartString.length();

        int nEnd;
        if (strEndString.length() == 0) {
            nEnd = strData.length();
        } else {
            nEnd = strData.indexOf(strEndString, nStart);
        }
        if (nEnd == -1) {
            return new ParseResult(nStartPos, "");
        }
        nEnd -= nStart;

        String strResult = strData.substring(nStart, nStart + nEnd);
        int iStartPos = nStart + nEnd + strEndString.length();

        return new ParseResult(iStartPos, strResult);
    }

    public static ParseResult GetSubText(String strHtml, String strStart, String strEnd, String strStartSubString, String strEndSubString, int nStart) {
        int nStringStart = 0;
        ParseResult strSubItem;
        ParseResult strSubItemsData;

        strSubItemsData = GetText(strHtml, strStart, strEnd, nStart);

        strSubItem = GetText(strSubItemsData.text, strStartSubString, strEndSubString, nStringStart);

        return new ParseResult(nStart, strSubItem.text);
    }

    public static String StripHtml(String str) {
        return Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY).toString();
    }

    public static String StripHtml2(String str) {
        Pattern replace = Pattern.compile("<[^>]*>");
        Matcher matcher = replace.matcher(str);
        return matcher.replaceAll("");
    }

    public static String StripBlanks(String str) {
        Pattern replace = Pattern.compile("\\s+");
        Matcher matcher = replace.matcher(str);
        return matcher.replaceAll(" ").trim();
    }

    public static String RemoveSpecialCharacters(String str) {
        char[] buffer = new char[str.length()];
        int idx = 0;

        for (char c : str.toCharArray()) {
            if ((c >= '0' && c <= '9')) {
                buffer[idx] = c;
                idx++;
            }
        }

        return new String(buffer, 0, idx);
    }

}
