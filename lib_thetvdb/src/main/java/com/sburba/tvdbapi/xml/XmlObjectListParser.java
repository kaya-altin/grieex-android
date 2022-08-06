package com.sburba.tvdbapi.xml;

import java.util.Collection;
import java.util.Map;

public interface XmlObjectListParser<T> {
    Collection<T> parseListFromXmlString(String xml) throws XmlException;

    Collection<T> parseListFromXmlStrings(Map<String, String> xmlStrings)
            throws XmlException;
}