package org.tastefuljava.gianadda.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Files {
    private static final Logger LOG = Logger.getLogger(Files.class.getName());

    private static final String[] EMPTY_STRING_ARRAY = {};

    private Files() {
        // Private constructor to prevent instanciation
    }

    public static String[] list(File dir, final Pattern pattern) {
        String[] result = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return matches(name, pattern);
            }
        });
        return result == null ? EMPTY_STRING_ARRAY : result;
    }

    public static String[] listFiles(File dir, final Pattern pattern) {
        String[] result = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (!matches(name, pattern)) {
                    return false;
                }
                return new File(dir, name).isFile();
            }
        });
        return result == null ? EMPTY_STRING_ARRAY : result;
    }

    public static String[] listDirs(File dir, final Pattern pattern) {
        String[] result = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (!matches(name, pattern)) {
                    return false;
                }
                return new File(dir, name).isDirectory();
            }
        });
        return result == null ? EMPTY_STRING_ARRAY : result;
    }

    public static void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            delete(file);
        }
    }

    public static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDir(file);
        } else {
            deleteFile(file);
        }
    }

    public static void deleteFile(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Could not delete " + file);
        }
    }

    public static void deleteDir(File dir) throws IOException {
        String[] names = list(dir, null);
        for (String name: names) {
            delete(new File(dir, name));
        }
        if (!dir.delete()) {
            throw new IOException("Could not delete dir " + dir);
        }
    }

    public static void rename(File source, File dest) throws IOException {
        if (!source.renameTo(dest)) {
            throw new IOException(
                    "Could not rename " + source + " into " + dest);
        }
    }

    public static void mkdirs(File dir) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException(
                    "Could not create directory " + dir);
        }
    }

    public static void zip(File source, File zipFile, Pattern pattern)
            throws IOException {
        try (OutputStream out = new FileOutputStream(zipFile);
                ZipOutputStream zip = new ZipOutputStream(out)) {
            if (matches(source.getName(), pattern)) {
                if (source.isDirectory()) {
                    zipDir(source, null, zip, pattern);
                } else {
                    zipFile(source, null, zip);
                }
            }
        }
    }

    public static void unzip(File zipFile, File dir, Pattern pattern)
            throws IOException {
        try (ZipFile zip = new ZipFile(zipFile, ZipFile.OPEN_READ)) {
            for (Enumeration enm = zip.entries(); enm.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry)enm.nextElement();
                String path = entry.getName().replace('\\', '/');
                File file = new File(dir, path);
                String name = file.getName();
                if (matches(name, pattern)) {
                    LOG.log(Level.INFO, "extracting {0} to {1}",
                            new Object[]{entry.getName(), file});
                    if (entry.isDirectory()) {
                        mkdirs(file);
                    } else {
                        long timestamp = entry.getTime();
                        try (InputStream in = zip.getInputStream(entry)) {
                            mkdirs(file.getParentFile());
                            save(in, file);
                        }
                        file.setLastModified(timestamp);
                    }
                }
            }
        }
    }

    public static void save(InputStream in, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            copy(in, out);
        }
    }

    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] buf = new byte[8192];
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            out.write(buf, 0, n);
        }
    }

    public static void copy(File source, File dest) throws IOException {
        if (source.isDirectory()) {
            copyDir(source, dest);
        } else {
            copyFile(source, dest);
        }
    }

    public static void copyText(BufferedReader in, PrintWriter out)
            throws IOException {
        for (String s = in.readLine(); s != null; s = in.readLine()) {
            out.println(s);
        }
    }

    public static void copyText(Reader reader, Writer writer)
            throws IOException {
        copyText(bufferedReader(reader), printWriter(writer));
    }


    public static void copyText(InputStream in, String inenc, OutputStream out,
            String outenc) throws IOException {
        copyText(new InputStreamReader(in, inenc),
                new OutputStreamWriter(out, outenc));
    }

    public static void copyText(InputStream in, OutputStream out, String enc)
            throws IOException {
        copyText(in, enc, out, enc);
    }

    public static BufferedReader bufferedReader(Reader reader) {
        return reader instanceof BufferedReader
                ? (BufferedReader)reader : new BufferedReader(reader);
    }

    public static PrintWriter printWriter(Writer writer) {
        return writer instanceof PrintWriter
                ? (PrintWriter)writer : new PrintWriter(writer);
    }

    public static File getProgramDir() {
        try {
            String path = System.getProperty("program-dir");
            if (path == null) {
                path = Files.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().g‌​etPath();
            }
            File file = new File(path);
            if (file.isDirectory()) {
                if (!file.getName().equals("classes")) {
                    return file;
                }
                file = file.getParentFile();
                if (!file.getName().equals("build")
                        && !file.getName().equals("WEB-INF")) {
                    return file;
                }
                return file.getParentFile();
            } else {
                return file.getParentFile();
            }
        } catch (URISyntaxException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static void copyDir(File source, File dest) throws IOException {
        mkdirs(dest);
        for (String name: list(source, null)) {
            copy(new File(source, name), new File(dest, name));
        }
    }

    private static void copyFile(File source, File dest) throws IOException {
        mkdirs(dest.getParentFile());
        try (InputStream in = new FileInputStream(source)) {
            save(in, dest);
        }
        dest.setLastModified(source.lastModified());
    }

    private static boolean matches(String s, Pattern pattern) {
        if (s == null) {
            return false;
        }
        return pattern == null || pattern.matcher(s).matches();
    }

    private static void zipFile(File file, String path, ZipOutputStream zip)
            throws IOException {
        path = addPath(path, file.getName());
        ZipEntry entry = new ZipEntry(path);
        entry.setTime(file.lastModified());
        zip.putNextEntry(entry);
        try (InputStream in = new FileInputStream(file)) {
            copy(in, zip);
        }
    }

    private static void zipDir(File dir, String path, ZipOutputStream zip,
            Pattern pattern) throws IOException {
        path = addPath(path, dir.getName());
        String[] names = list(dir, pattern);
        for (String name: names) {
            File file = new File(dir, name);
            if (file.isDirectory()) {
                zipDir(file, path, zip, pattern);
            } else {
                zipFile(file, path, zip);
            }
        }
    }

    private static String addPath(String p1, String p2) {
        if (p1 == null) {
            return p2;
        } else {
            if (!p1.endsWith("/")) {
                p1 += '/';
            }
            return p1 += p2;
        }
    }
}
