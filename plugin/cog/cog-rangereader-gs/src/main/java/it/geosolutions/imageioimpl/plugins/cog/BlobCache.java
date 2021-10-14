/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2021, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.cog;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.geosolutions.imageio.core.ExtCaches;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Grabbing a {@link Storage} object and a {@link Blob} both incur in network call
 * penalties. This class caches both to avoid repeated accesses, the performance different
 * is significant.
 */
class BlobCache {

    private final static Logger LOGGER = Logger.getLogger(BlobCache.class.getName());

    private static class BlobKey {
        Storage storage;
        BlobId blobId;

        public BlobKey(Storage storage, BlobId blobId) {
            this.storage = storage;
            this.blobId = blobId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlobKey cacheKey = (BlobKey) o;
            return Objects.equals(storage, cacheKey.storage) && Objects.equals(blobId,
                    cacheKey.blobId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storage, blobId);
        }
    }

    static final LoadingCache<BlobKey, Optional<Blob>> BLOB_CACHE =
            CacheBuilder.newBuilder().weakValues().build(new CacheLoader<BlobKey,
                    Optional<Blob>>() {
                @Override
                public Optional<Blob> load(BlobKey key) {
                    return Optional.ofNullable(key.storage.get(key.blobId));
                }
            });

    static final LoadingCache<String, Storage> STORAGE_CACHE =
            CacheBuilder.newBuilder().build(new CacheLoader<String,
                    Storage>() {
                @Override
                public Storage load(String key) {
                    try (InputStream is = new URL(key).openStream()) {
                        GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                        Storage storage =
                                StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                        if (storage == null) {
                            LOGGER.log(Level.SEVERE, "Failed to connect to Google Storage using " +
                                    " explicitly provided credentials : " + key);
                            throw new RuntimeException("Failed to create a Google Storage " +
                                    "connection with the provided credentials");
                        }
                        return storage;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create a Google Storage connection"
                                , e);
                    }

                }
            });

    static final Storage DEFAULT_STORAGE = StorageOptions.getDefaultInstance().getService();

    static {
        ExtCaches.addListener(() -> {
            STORAGE_CACHE.invalidateAll();
            BLOB_CACHE.invalidateAll();

        });
    }

    static Blob getBlob(String auth, BlobId blobId) {
        try {
            Storage storage = auth != null ? STORAGE_CACHE.get(auth) : DEFAULT_STORAGE;
            return BLOB_CACHE.get(new BlobKey(storage, blobId)).orElse(null);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to look up blob in cache: " + blobId, e);
        }
    }
}
