package org.tastefuljava.gianadda.domain;

public class Tag {
    private int id;
    private String label;

    public static Tag find(String label) {
        Mapper mapper = CurrentMapper.get();
        return mapper.queryOne(Tag.class, "byLabel", label);
    }

    public static Tag findOrCreate(String label) {
        Mapper mapper = CurrentMapper.get();
        Tag tag = mapper.queryOne(Tag.class, "byLabel", label);
        if (tag == null) {
            tag = new Tag();
            tag.setLabel(label);
            CurrentMapper.get().insert(tag);
        }
        return tag;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String tag) {
        this.label = tag;
    }

    public void update() {
        CurrentMapper.get().update(this);
    }

    public void delete() {
        Mapper mapper = CurrentMapper.get();
        mapper.apply(this, "unlink");
        mapper.delete(this);
    }

    @Override
    public String toString() {
        return label;
    }
}
