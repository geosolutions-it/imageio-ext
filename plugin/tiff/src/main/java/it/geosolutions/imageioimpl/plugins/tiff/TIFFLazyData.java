package it.geosolutions.imageioimpl.plugins.tiff;

import java.io.IOException;

import it.geosolutions.imageio.plugins.tiff.TIFFTag;

import javax.imageio.stream.ImageInputStream;

public class TIFFLazyData {

    private ImageInputStream stream;
    
    private long startPosition;
    
    private int type;
    
    private int count;
    
    private int size;
    
    public TIFFLazyData() {
        
    }
    
    public TIFFLazyData(ImageInputStream stream, long startPosition, int type,
            int count) {
        super();
        this.stream = stream;
        this.startPosition = startPosition;
        this.type = type;
        this.count = count;
        this.size = TIFFTag.getSizeOfType(type);
    }
    
    public long getAsLong(final int index) {
        checkIndex(index);
        long val;
        try {
            stream.mark();
            stream.seek(startPosition + index * size);
            val = stream.readUnsignedInt();
            stream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    
        return val;
    }
    
    public long getAsLong8(final int index) {
        checkIndex(index);
        long val;
        try {
            stream.mark();
            stream.seek(startPosition + index * size);
            val = stream.readLong();
            stream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    
        return val;
    }

    private void checkIndex(int index) {
        if (index > count) {
            throw new IllegalArgumentException("Specified index (" + index + ") must be lower than Count:" + count);
        }
            
        
    }
}
