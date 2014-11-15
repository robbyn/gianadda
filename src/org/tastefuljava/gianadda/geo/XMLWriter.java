package org.tastefuljava.gianadda.geo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class XMLWriter {
    private final PrintWriter out;
    private final List<String> tagStack = new ArrayList<>();
    private boolean format = true;
    private boolean inAttributes = false;
    private boolean hasSubtags = false;
    private boolean lnBefore = true;

    public XMLWriter(PrintWriter out) {
        this.out = out;
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    public XMLWriter(Writer out) {
        this(new PrintWriter(out));
    }

    public XMLWriter(OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    public XMLWriter(OutputStream out, String encoding) throws IOException {
        this(new OutputStreamWriter(out, encoding));
    }

    public XMLWriter(File file) throws IOException {
        this(new FileWriter(file));
    }

    public void close() {
        out.close();
    }

    public boolean getFormat() {
        return format;
    }

    public void setFormat(boolean newValue) {
        format = newValue;
    }

    public void startTag(String name) {
        if (inAttributes) {
            out.print('>');
        }
        if (format && !lnBefore) {
            out.println();
        }
        indent();
        out.print('<');
        out.print(name);
        inAttributes = true;
        hasSubtags = false;
        lnBefore = false;
        tagStack.add(name);
    }

    public void attribute(String name, String value) {
        if (!inAttributes) {
            throw new RuntimeException("Attributes not allowed here");
        }
        if (value != null) {
            out.print(" ");
            out.print(name);
            out.print("=\"");
            printEscaped(value);
            out.print('"');
        }
    }

    public void data(String data) {
        if (inAttributes) {
            out.print('>');
            inAttributes = false;
        }
        printEscaped(data);
    }

    public void endTag() {
        String name = tagStack.remove(tagStack.size()-1);
        if (inAttributes) {
            out.print(" />");
            inAttributes = false;
        } else {
            if (format && hasSubtags) {
                if (!lnBefore) {
                    out.println();
                }
                indent();
            }
            out.print("</");
            out.print(name);
            out.print('>');
        }
        if (format) {
            out.println();
        }
        lnBefore = true;
        hasSubtags = true;
    }

    public void writeTagged(String tag, String data) {
        if (data != null) {
            startTag(tag);
            data(data);
            endTag();
        }
    }

    private void indent() {
        if (format) {
            for (int i = 0; i < tagStack.size(); ++i) {
                out.print("   ");
            }
        }
    }

    private void printEscaped(String value) {
        char chars[] = value.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            lnBefore = false;
            switch (c) {
                case '<':
                    out.print("&lt;");
                    break;
                case '>':
                    out.print("&gt;");
                    break;
                case '&':
                    out.print("&amp;");
                    break;
                case '"':
                    out.print("&quot;");
                    break;
                case '\n':
                    out.println();
                    lnBefore = true;
                    break;
                default:
                    if (c >= 32 && c < 127) {
                        out.print(c);
                    } else {
                        out.print("&#" + Integer.toString(c) + ";");
                    }
                    break;
            }
        }
    }
}
