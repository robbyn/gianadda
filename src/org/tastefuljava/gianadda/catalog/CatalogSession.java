package org.tastefuljava.gianadda.catalog;

import java.io.Closeable;
import org.apache.ibatis.session.SqlSession;

public class CatalogSession implements Closeable {
    private final SqlSession session;

    CatalogSession(SqlSession session) {
        this.session = session;
    }

    public void commit() {
        session.commit();
    }

    @Override
    public void close() {
        session.close();
    }

    public <T> T getMapper(Class<T> intf) {
        return session.getMapper(intf);
    }
}
