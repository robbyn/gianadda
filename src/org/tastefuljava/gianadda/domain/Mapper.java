package org.tastefuljava.gianadda.domain;

public interface Mapper {
    void insert(Object obj);
    void update(Object obj);
    void delete(Object obj);
    Folder getRootFolder(String name);
}
