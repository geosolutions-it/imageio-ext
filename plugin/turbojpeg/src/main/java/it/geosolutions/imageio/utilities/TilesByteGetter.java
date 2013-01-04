/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.utilities;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileScheduler;

/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 */
public class TilesByteGetter {
    
    byte[] bb;
    
    RenderedImage ri;
    
    int tileWidth;
    
    int tileHeight;
    
    int width;
    
    int height;
    
    final static boolean usePrefetching;
    
    static int multithreadingLevel;
    
    static {
        String mt = System.getProperty("it.geosolutions.tilesgetter.multithreading");
        if (mt != null) {
            try {
                multithreadingLevel = Integer.parseInt(mt);
            } catch (NumberFormatException nfe){
                System.out.println("Error parsing " + mt + " as integer; using default 1");
                multithreadingLevel = 1;
            }
        } else {
            multithreadingLevel = 1;
        }
        usePrefetching = Boolean.getBoolean("it.geosolutions.tilesgetter.usePrefetching");
        System.out.println("Multithreading level: " + multithreadingLevel + " prefetching: " + usePrefetching);
    }
   

    public TilesByteGetter(RenderedImage ri){
        this.ri = ri;
        tileWidth = ri.getTileWidth();
        tileHeight = ri.getTileHeight();
        width = ri.getWidth();
        height = ri.getHeight();
    }
    
    private class ByteGetter implements Callable<Integer> {

        RenderedImage ri;
        
        int tileX;
        
        int tileY;
        
        public ByteGetter(RenderedImage ri, final int tileX, final int tileY) {
            this.ri = ri;
            this.tileX = tileX;
            this.tileY = tileY;
        }

        public Integer call() {
            try {
                Raster tile = ri.getTile(tileX, tileY);
                final int nBands = ri.getSampleModel().getNumBands();
                DataBufferByte dbb = (DataBufferByte) tile.getDataBuffer();
                
                byte[] bytes = dbb.getData();
                final int h;
                final int w;
                if ((tileWidth * (tileX + 1) > width)) {
                    w = width - (tileX * tileWidth);
                } else {
                    w = tileWidth;
                }
                if ((tileHeight * (tileY + 1) > height)) {
                    h = height - (tileY * tileHeight);
                } else {
                    h = tileHeight;
                }
                
                final int localStripeLength = w * nBands;
                final int stripeLength = tileWidth * nBands;
                int tileSkipX = stripeLength * tileX; 
                
                int offset;
                for (int j=0; j < h; j++) {
                    offset = (((j + tileHeight * tileY) * width * nBands) + tileSkipX);
                    System.arraycopy(bytes, stripeLength * j, bb, offset, localStripeLength);
                }
                
                
                
            } catch (Exception e){
                
            }
            return 1;
                
        }
}
    
    public byte[] getBytes() throws InterruptedException{
        if (ri instanceof BufferedImage){
            Raster wr = ri.getTile(0, 0);
            return ((DataBufferByte) wr.getDataBuffer()).getData();
        } else {
            final int nX = ri.getNumXTiles();
            final int nY = ri.getNumYTiles();
            if (nX == 1 && nY == 1) {
                Raster wr = ri.getTile(0, 0);
                return ((DataBufferByte) wr.getDataBuffer()).getData();
            } else {
                final int size = ri.getHeight() * ri.getWidth() * ri.getSampleModel().getNumBands(); 
                bb = new byte[size];
                if (multithreadingLevel != 1) {
                    final int minTx = ri.getMinTileX();
                    final int minTy = ri.getMinTileY();
                    int TH = multithreadingLevel;
                    final TileScheduler ts = JAI.getDefaultInstance().getTileScheduler();
                    final List<Point> tiles = new ArrayList<Point>();
                    final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
                    final List<Callable<Integer>> queueX = new ArrayList<Callable<Integer>>();
                    final ThreadPoolExecutor ex = new ThreadPoolExecutor(TH, TH, 10000L, TimeUnit.SECONDS, queue);
                    ex.prestartAllCoreThreads();
                    int tx = 0;
                    int ty = 0;
                    for (int j = 0; j < nY; j++) {
                        for (int i = 0; i < nX; i++) {
                            tx = minTx + i;
                            ty = minTy + j;
                            tiles.add(new Point( tx, ty));
                            queueX.add(new ByteGetter(ri, tx, ty));
                        }
                    }
                    Point[] tilesArray = (Point[]) tiles.toArray(new Point[tiles.size()]);
                    ts.prefetchTiles(PlanarImage.wrapRenderedImage(ri), tilesArray);
                    ex.invokeAll(queueX);
                    ex.shutdown();
                } else {
                    if (usePrefetching){
                        final int minTx = ri.getMinTileX();
                        final int minTy = ri.getMinTileY();
                        TileScheduler ts = JAI.getDefaultInstance().getTileScheduler();
                        List<Point> tiles = new ArrayList<Point>();
                        int tx = 0;
                        int ty = 0;
                        for (int j = 0; j < nY; j++) {
                            for (int i = 0; i < nX; i++) {
                                tx = minTx + i;
                                ty = minTy + j;
                                tiles.add(new Point( tx, ty));
                            }
                        }
                        Point[] tilesArray = (Point[]) tiles.toArray(new Point[tiles.size()]);
                        ts.prefetchTiles(PlanarImage.wrapRenderedImage(ri), tilesArray);
                    }
                    bb = ((DataBufferByte)ri.getData().getDataBuffer()).getData();

                }
                return bb;
            }
        }
    }
}
