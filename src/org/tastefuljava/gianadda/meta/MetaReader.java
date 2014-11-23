package org.tastefuljava.gianadda.meta;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.tastefuljava.gianadda.domain.Folder;
import org.tastefuljava.gianadda.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class MetaReader {
    private static final Logger LOG
            = Logger.getLogger(MetaReader.class.getName());

    private MetaReader() {
        throw new UnsupportedOperationException(
                "Cannot instanciate " + MetaReader.class.getName());
    }

    public static boolean check(File metaFile, Folder folder)
            throws IOException {
        if (!metaFile.isFile()) {
            return false;
        }
        Date fileDate = new Date(metaFile.lastModified());
        Date folderDate = folder.getDateTime();
        if (folderDate != null && fileDate.equals(folderDate)) {
            return false;
        }
        folder.setDateTime(fileDate);
        parse(metaFile, folder);
        return true;
    }

    private static void parse(File metaFile, Folder folder)
            throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            ParserHandler handler = new ParserHandler(folder);
            parser.parse(metaFile, handler);
        } catch (SAXException | ParserConfigurationException e) {
            LOG.log(Level.SEVERE, "Error reading project", e);
            throw new IOException(e.getMessage());
        }
    }

    private static class ParserHandler extends DefaultHandler {
        private static final String DTD_SYSTEM_ID = "folder-meta.dtd";
        private static final String DTD_PUBLIC_ID
                = "-//tastefuljava.org//Gianadda Folder Metadata File 1.0//EN";

        private final Folder folder;
        private final StringBuilder buf = new StringBuilder();

        public ParserHandler(Folder folder) {
            this.folder = folder;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws IOException, SAXException {
            if (DTD_PUBLIC_ID.equals(publicId)
                    || DTD_SYSTEM_ID.equals(systemId)) {
                InputSource source = new InputSource(
                        getClass().getResourceAsStream("folder-meta.dtd"));
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            }
            return super.resolveEntity(publicId, systemId);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new SAXException(e.getMessage());
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new SAXException(e.getMessage());
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attrs) throws SAXException {
            buf.setLength(0);
            switch (qName) {
                case "folder-meta":
                    folder.removeAllTags();
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            switch (qName) {
                case "folder-meta":
                    break;
                case "title":
                    folder.setTitle(buf.toString().trim());
                    break;
                case "link":
                    folder.setLink(buf.toString().trim());
                    break;
                case "pubDate":
                    folder.setPubDate(
                            Util.parseXsdDateTime(buf.toString().trim()));
                    break;
                case "tag":
                    folder.addTag(buf.toString().trim());
                    break;
                case "summary":
                    folder.setDescription(buf.toString());
                    break;
                case "content":
                    folder.setBody(buf.toString());
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            buf.append(ch, start, length);
        }
    }
}
