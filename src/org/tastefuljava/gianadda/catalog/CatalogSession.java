package org.tastefuljava.gianadda.catalog;

import java.io.Closeable;
import org.apache.ibatis.session.SqlSession;
import org.tastefuljava.gianadda.domain.CurrentMapper;
import org.tastefuljava.gianadda.domain.Mapper;

public class CatalogSession implements Closeable {
    private final SqlSession session;

    CatalogSession(SqlSession session) {
        boolean ok = false;
        this.session = session;
        try {
            Mapper map = session.getMapper(Mapper.class);
            CurrentMapper.set(map);
            ok = true;
        } finally {
            if (!ok) {
                session.close();
            }
        }
    }

    public void commit() {
        session.commit();
    }

    @Override
    public void close() {
        CurrentMapper.set(null);
        session.close();
    }

    public <T> T getMapper(Class<T> intf) {
        return session.getMapper(intf);
    }
}
