package it.geosolutions.imageio.cog;

import it.geosolutions.imageio.utilities.SoftValueHashMap;
import it.geosolutions.imageioimpl.plugins.cog.ByteChunksMerger;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class ByteChunksMergerTest {

    @Test
    public void merge_whenNotConnected_expectNoChange(){
        Map<Long, byte[]> orgData = new SoftValueHashMap<>(0);
        orgData.put(0L, new byte[] {0,1,2,3});
        orgData.put(10L, new byte[] {10,11,12,13});

        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(2, mergedData.size());
        Assert.assertArrayEquals(new byte[]{0,1,2,3}, mergedData.get(0L));
        Assert.assertArrayEquals(new byte[]{10,11,12,13}, mergedData.get(10L));
    }

    @Test
    public void merge_whenIntersecting_expectMerged(){
        Map<Long, byte[]> orgData = new SoftValueHashMap<>(0);
        orgData.put(0L, new byte[] {0,1,2,3});
        orgData.put(3L, new byte[] {3,4,5});

        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(1, mergedData.size());
        Assert.assertEquals(6, mergedData.get(0L).length);
        Assert.assertArrayEquals(new byte[]{0, 1, 2, 3, 4, 5}, mergedData.get(0L));
    }

    @Test
    public void merge_whenTouching_expectMerged(){
        Map<Long, byte[]> orgData = new SoftValueHashMap<>(0);
        orgData.put(0L, new byte[] {0,1,2,3});
        orgData.put(4L, new byte[] {4,5,6,7});

        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(1, mergedData.size());
        Assert.assertEquals(8, mergedData.get(0L).length);
        Assert.assertArrayEquals(new byte[] {0,1,2,3,4,5,6,7}, mergedData.get(0L));
    }

    @Test
    public void merge_whenIncluded_expectMerged(){
        Map<Long, byte[]> orgData = new SoftValueHashMap<>(0);
        orgData.put(2L, new byte[]{2,3});
        orgData.put(0L, new byte[]{0,1,2,3});

        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(1, mergedData.size());
        Assert.assertArrayEquals(new byte[]{0,1,2,3}, mergedData.get(0L));
    }

    @Test
    public void merge_whenCombinationOfEverything_expectCorrectMerged(){
        Map<Long, byte[]> orgData = new SoftValueHashMap<>(0);
        orgData.put(0L, new byte[] {0,1,2,3});
        orgData.put(2L, new byte[] {2,3,4,5});               // intersecting
        orgData.put(1L, new byte[] {1,2});                   // included
        orgData.put(10L, new byte[] {10,11,12,13});          // new interval
        orgData.put(14L, new byte[] {14,15,16,17});          // touching
        orgData.put(100L, new byte[] {100,101});             // not connected


        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(3, mergedData.size());
        Assert.assertArrayEquals(new byte[] {0,1,2,3,4,5}, mergedData.get(0L));
        Assert.assertArrayEquals(new byte[] {10,11,12,13,14,15,16,17}, mergedData.get(10L));
        Assert.assertArrayEquals(new byte[] {100,101}, mergedData.get(100L));
    }

    @Test
    public void merge_whenGivenNull_expectEmptyMap(){
        Map<Long, byte[]> orgData = null;

        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(0, mergedData.size());
    }

    @Test
    public void merge_whenGivenEmpty_expectEmptyMap(){
        Map<Long, byte[]> orgData = Collections.emptyMap();

        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(0, mergedData.size());
    }

    @Test
    public void merge_whenSingeEntryMap_expectEmptyMap(){
        Map<Long, byte[]> orgData = new SoftValueHashMap<>(0);
        orgData.put(4L, new byte[]{4,5,6,7});

        Map<Long, byte[]> mergedData = ByteChunksMerger.merge(orgData);

        Assert.assertEquals(1, mergedData.size());
        Assert.assertArrayEquals(new byte[]{4,5,6,7}, mergedData.get(4L));
    }

}
