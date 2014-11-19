package org.tastefuljava.gianadda.domain;

import java.util.List;

public interface Mapper {
    void insert(Object obj);
    void update(Object obj);
    void delete(Object obj);
    <T> T queryOne(Class<T> clazz, String name, Object... parms);
    <T> List<T> query(Class<T> clazz, String name, Object... parms);
    void invoke(Class<?> clazz, String name, Object... parms);
    void apply(Object obj, String name, Object... parms);
    Folder getRootFolder(String name);
}
