package it.geosolutions.imageioimpl.plugins.cog;

import it.geosolutions.imageio.utilities.SoftValueHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ByteChunksMerger {

    private ByteChunksMerger(){}

    public static Map<Long, byte[]> merge(Map<Long, byte[]> data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        }

        // Sort the entries by their keys (byte offsets)
        List<Map.Entry<Long, byte[]>> sortedEntries = new ArrayList<>(data.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        Map<Long, byte[]> mergedData = new SoftValueHashMap<>(0);
        long currentStart = sortedEntries.get(0).getKey();
        byte[] currentBytes = sortedEntries.get(0).getValue();

        for (int i = 1; i < sortedEntries.size(); i++) {
            Map.Entry<Long, byte[]> entry = sortedEntries.get(i);
            long nextStart = entry.getKey();
            byte[] nextBytes = entry.getValue();

            long currentEnd = currentStart + currentBytes.length - 1;
            long nextEnd = nextStart + nextBytes.length - 1;

            if (nextEnd <= currentEnd){
                //included -> skip data
            } else if (nextStart <= (currentEnd + 1)) {
                // intersection or touching
                int overlappingElements = (int)(currentEnd - nextStart + 1);
                int combinedLen = currentBytes.length + nextBytes.length - overlappingElements;
                byte[] combinedBytes = new byte[combinedLen];
                System.arraycopy(currentBytes, 0, combinedBytes, 0, currentBytes.length);
                System.arraycopy(nextBytes, overlappingElements, combinedBytes, currentBytes.length, combinedLen - currentBytes.length);
                currentBytes = combinedBytes;
            } else {
                // No overlap, add the current entry to the merged data
                mergedData.put(currentStart, currentBytes);
                currentStart = nextStart;
                currentBytes = nextBytes;
            }

        }

        // Add the last entry
        mergedData.put(currentStart, currentBytes);

        return mergedData;
    }
}
