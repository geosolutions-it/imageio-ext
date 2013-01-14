package it.geosolutions.imageio.plugins.nitronitf;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.File;
import java.io.IOException;

import nitf.IOInterface;
import nitf.NITFException;

/**
 * implementation of the IOInterface, which allows to do buffered read operations on top of a FileInputStream.
 * @author Daniele Romagnoli, GeoSolutions SaS
 */
public class IOFileInputStream extends IOInterface {

    private FileImageInputStreamExt stream;

    private File file;

    long size = 0;

    public IOFileInputStream(FileImageInputStreamExt stream) {
        this.stream = stream;
        this.file = stream.getFile();
        size = this.file.length();
    }

    @Override
    public void read(byte[] buf) throws NITFException {
        try {
            stream.read(buf);
        } catch (IOException e) {
            throw new NITFException(e);

        }
    }

    @Override
    public byte[] read(int size) throws NITFException {
        byte[] b = new byte[size];
        read(b);
        return b;

    }

    @Override
    public void close() throws NITFException {
        try {
            stream.close();
        } catch (IOException e) {
            throw new NITFException(e);
        }
    }

    @Override
    public long getSize() throws NITFException {
        return size;

    }

    @Override
    public int getMode() throws NITFException {
        return NITF_ACCESS_READONLY;
    }

    @Override
    public void read(byte[] buf, int size) throws NITFException {
        try {
            stream.read(buf, 0, size);
        } catch (IOException e) {
            throw new NITFException(e);
        }

    }

    @Override
    public boolean canSeek() {
        return true;
    }

    @Override
    public long seek(long offset, int whence) throws NITFException {
        try {
            long pos = stream.getStreamPosition();
            switch (whence) {
            case IOInterface.SEEK_CUR:
                if (offset + pos > size)
                    throw new NITFException("Attempting to seek past buffer boundary.");
                stream.seek((int) (pos + offset));
                break;
            case IOInterface.SEEK_END:
                throw new NITFException("SEEK_END is unsupported with MemoryIO.");
            case IOInterface.SEEK_SET:
                if (offset > size)
                    throw new NITFException("Attempting to seek past buffer boundary.");
                stream.seek((int) (offset));
                break;
            }
            return stream.getStreamPosition();
        } catch (IOException ioe) {
            throw new NITFException(ioe);
        }
    }

    @Override
    public long tell() throws NITFException {
        try {
            return stream.getStreamPosition();
        } catch (IOException e) {
            throw new NITFException(e);
        }
    }

    @Override
    public void write(byte[] buf, int size) throws NITFException {
        throw new UnsupportedOperationException();
    }

}
