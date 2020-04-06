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

    private boolean done = false;
    private long startPosition;
    private byte[] bytes;
    private final static Logger LOGGER = Logger.getLogger(AsyncHttpCallback.class.getName());

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        LOGGER.severe("Error executing HTTP request. " + e);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        bytes = response.body().bytes();
        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public AsyncHttpCallback startPosition(long startPosition) {
        setStartPosition(startPosition);
        return this;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
