package org.tastefuljava.gianadda.domain;

public interface Mapper {
    Picture getPictureById(int id);
    void insertPicture(Picture pic);
    void updatePicture(Picture pic);
    void deletePicture(Picture pic);

    Folder getFolderById(int id);
    Folder getRootFolder(String name);
    void insertFolder(Folder folder);
    void updateFolder(Folder folder);
    void deleteFolder(Folder folder);
}
