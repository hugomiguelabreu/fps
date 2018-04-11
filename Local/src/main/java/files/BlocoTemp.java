package files;

public class BlocoTemp {

    private int size;
    private byte[] data;

    public BlocoTemp(int size, byte[] data) {
        this.size = size;
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }
}
