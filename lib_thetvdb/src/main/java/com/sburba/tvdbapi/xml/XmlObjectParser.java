package com.sburba.tvdbapi.xml;

public interface XmlObjectParser<T> {
    T parseXmlString(String xmlString) throws XmlException;
}