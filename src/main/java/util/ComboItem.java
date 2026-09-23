package util;

public class ComboItem {
    public final int id;
    public final String label;

    public static final ComboItem EMPTY = new ComboItem(-1, "-");

    public ComboItem(int id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComboItem)) return false;
        return id == ((ComboItem) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
