package org.tastefuljava.gianadda.domain;

import java.util.List;

public interface Mapper {
    Picture getPictureById(int id);
    Picture getPictureByName(Folder folder,String name);
    List<Picture> getFolderPictures(Folder folder);
    void insertPicture(Picture pic);
    void updatePicture(Picture pic);
    void deletePicture(Picture pic);

    Folder getFolderById(int id);
    Folder getFolderByName(Folder folder,String name);
    Folder getRootFolder(String name);
    List<Folder> getSubfolders(Folder folder);
    void insertFolder(Folder folder);
    void updateFolder(Folder folder);
    void deleteFolder(Folder folder);
}
