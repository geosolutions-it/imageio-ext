/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2019, GeoSolutions
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Callback for asynchronous HTTP requests for OkHttp
 *
 * @author joshfix
 * Created on 2019-09-24
 */
public class AsyncHttpCallback implements Callback {

    enum Status {
        DONE,
        FAILED,
        IN_PROGRESS;
    }

    private Status status = Status.IN_PROGRESS;
    private long startPosition;
    private long endPosition;
    private byte[] bytes;
    private final static Logger LOGGER = Logger.getLogger(AsyncHttpCallback.class.getName());

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        LOGGER.severe("Error executing HTTP request. " + e);
        status = Status.FAILED;
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        try {
            bytes = response.body().bytes();
            status = Status.DONE;
        } catch (IOException ioe) {
            status = Status.FAILED;
            throw ioe;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void resetStatus() {
        status = Status.IN_PROGRESS;
    }

    public AsyncHttpCallback initRange(long[] range) {
        startPosition = range[0];
        endPosition = range[1];
        return this;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
