package it.geosolutions.imageioimpl.plugins.tiff;

import io.airlift.compress.zstd.ZstdDecompressor;
import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;

import javax.imageio.IIOException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.DataFormatException;

public class TIFFZSTDDecompressor extends it.geosolutions.imageio.plugins.tiff.TIFFDecompressor {

    ZstdDecompressor decompressor = new ZstdDecompressor();

    @Override
    public void decodeRaw(byte[] b, int dstOffset, int bitsPerPixel, int scanlineStride) throws IOException {
        stream.seek(offset);
        byte[] srcData = new byte[byteCount];
        stream.readFully(srcData);

        int bytesPerRow = (srcWidth*bitsPerPixel + 7)/8;
        byte[] buf;
        int bufOffset;
        if(bytesPerRow == scanlineStride) {
            buf = b;
            bufOffset = dstOffset;
        } else {
            buf = new byte[bytesPerRow*srcHeight];
            bufOffset = 0;
        }


        decompressor.decompress(srcData, 0, byteCount, buf, bufOffset, bytesPerRow*srcHeight);

//        try {
//            decompressor.decompress(srcData, , byteCount, buf, bufOffset, bytesPerRow*srcHeight);
//        } catch(DataFormatException dfe) {
//            throw new IIOException(I18N.getString("TIFFZSTDDecompressor"),
//                    dfe);
//        }

//            if (bitsPerSample[0] == 8) {
//                for (int j = 0; j < srcHeight; j++) {
//                    int count = bufOffset + samplesPerPixel * (j * srcWidth + 1);
//                    for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
//                        buf[count] += buf[count - samplesPerPixel];
//                        count++;
//                    }
//                }
//            }




//        if(bytesPerRow != scanlineStride) {
//            int off = 0;
//            for (int y = 0; y < srcHeight; y++) {
//                System.arraycopy(buf, off, b, dstOffset, bytesPerRow);
//                off += bytesPerRow;
//                dstOffset += scanlineStride;
//            }
//        }






    }
}
