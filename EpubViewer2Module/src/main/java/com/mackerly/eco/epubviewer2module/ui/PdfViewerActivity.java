package com.mackerly.eco.epubviewer2module.ui;

import android.content.Intent;
import android.os.Bundle;

import com.epapyrus.plugpdf.SimpleDocumentReader;
import com.epapyrus.plugpdf.SimpleReaderFactory;
import com.epapyrus.plugpdf.core.viewer.ReaderView;
import com.lemonsol.toolbox.utils.LogUtils;
import com.mackerly.eco.epubviewer2module.components.ResourceCallback;

public class PdfViewerActivity extends BaseActivity {

    public static final String INTENT_BOOK_ID = "INTENT_BOOK_ID";
    public static final String INTENT_FILE_PATH = "INTENT_FILE_PATH";
    public static final String INTENT_CHROME_YN = "INTENT_CHROME_YN";

    private String chromeYn;

    private SimpleDocumentReader mReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String bookId = intent.getStringExtra(INTENT_BOOK_ID);
        String filePath = intent.getStringExtra(INTENT_FILE_PATH);
        chromeYn = intent.getStringExtra(INTENT_CHROME_YN);

        mReader = SimpleReaderFactory.createSimpleViewer(this, state -> {
            LogUtils.debug("PDF loaded.");
            LogUtils.debug(state.toString());
        });

        byte[] pdfData = null;

        ResourceCallback.ResourceHandler resourceHandler = ResourceCallback.getInstance().getHandler();
        if (resourceHandler != null) {
            // TODO; replace hard coded fileType
            pdfData = resourceHandler.handleResource(bookId, "PDF", filePath);
        }

        if (pdfData != null) {
            mReader.openData(pdfData, pdfData.length, "");
            ReaderView.setEnableUseRecentPage(true);
        } else {
            mReader.openFile(filePath, "");
        }
    }

    @Override
    protected void onDestroy() {
        if (mReader.getDocument() != null) {
            mReader.save();
            mReader.clear();
        }
        super.onDestroy();
    }
}
