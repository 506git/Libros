package com.mackerly.eco.epubviewer2module.components;

import java.io.IOException;

/**
 * Created by Kim, Hanchul.
 */

public class ResourceCallback {
    public interface ResourceHandler {
        byte[] handleResource(String bookId, String fileType, String url);
    }

    private static final ResourceCallback ourInstance = new ResourceCallback();
    private ResourceHandler handler;

    public static ResourceCallback getInstance() {
        return ourInstance;
    }

    private ResourceCallback() {
    }

    public ResourceHandler getHandler() {
        return handler;
    }

    public void setResourceHandler(ResourceHandler handler) {
        this.handler = handler;
    }
}
