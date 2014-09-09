package org.tastefuljava.gianadda.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.tastefuljava.gianadda.util.Util;

public class Folder {
    private int id;
    private Folder parent;
    private String name;
    private String title;
    private String description;
    private Map<String,Picture> pictures;
    private Map<String,Folder> subfolders;

    public static Folder getRoot(String name) {
        return CurrentMapper.get().getRootFolder(name);
    }

    public int getId() {
        return id;
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void insert() {
        CurrentMapper.get().insertFolder(this);
        if (parent != null) {
            parent.subfolderAdded(this);
        }
    }

    public void update() {
        CurrentMapper.get().updateFolder(this);
    }

    public void delete() {
        if (parent != null) {
            parent.subfolderRemoved(this);
        }
        CurrentMapper.get().deleteFolder(id);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public int getLevel() {
        return parent == null ? 0 : parent.getLevel()+1;
    }

    public String getPath() {
        return parent == null || parent.isRoot()
                ? name : parent.getPath() + "/" + name;
    }

    public String getUrlPath() {
        String esc = Util.urlEncode(name);
        return parent == null || parent.isRoot()
                ? esc : parent.getUrlPath() + "/" + esc;
    }

    public Picture getPicture(String name) {
        requirePictures();
        return pictures.get(name);
    }

    public List<Picture> getPictures() {
        requirePictures();
        return new ArrayList<>(pictures.values());
    }

    public Map<String,Picture> getPictureMap() {
        requirePictures();
        return new TreeMap<>(pictures);
    }

    public Folder getSubfolder(String name) {
        requireSubfolders();
        return subfolders.get(name);
    }

    public List<Folder> getSubfolders() {
        requireSubfolders();
        return new ArrayList<>(subfolders.values());
    }

    public Map<String,Folder> getSubfolderMap() {
        requireSubfolders();
        return new TreeMap<>(subfolders);
    }

    void pictureAdded(Picture pic) {
        if (pictures != null) {
            pictures.put(pic.getName(), pic);
        }
    }

    void pictureRemoved(Picture pic) {
        if (pictures != null) {
            pictures.remove(pic.getName());
        }
    }

    void subfolderAdded(Folder folder) {
        if (subfolders != null) {
            subfolders.put(folder.getName(), folder);
        }
    }

    void subfolderRemoved(Folder folder) {
        if (subfolders != null) {
            subfolders.remove(folder.getName());
        }
    }

    private void requirePictures() {
        if (pictures == null) {
            pictures = new TreeMap<>();
            for (Picture pic: CurrentMapper.get().getFolderPictures(id)) {
                pictures.put(pic.getName(), pic);
            }
        }
    }

    private void requireSubfolders() {
        if (subfolders == null) {
            subfolders = new TreeMap<>();
            for (Folder child: CurrentMapper.get().getSubfolders(id)) {
                subfolders.put(child.getName(), child);
            }
        }
    }
}
