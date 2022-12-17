package kr.co.smartandwise.eco_epub3_module.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class PaginationWebView extends BaseEpubWebView {

    public PaginationWebView(Context context) {
        super(context);
        this.init();
    }

    public PaginationWebView(Context context, AttributeSet attr) {
        super(context, attr);
        this.init();
    }

    public PaginationWebView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        this.init();
    }

    @Override
    protected void init() {
        super.init();
        this.setAlpha(0);
    }

    public void startPagination() {
        if (!"undefined".equals(this.openBookData) && this.openBookData != null) {
            if (this.paginationState == PaginationState.DOING) {
                this.paginationState = PaginationState.STOP;
                this.callJavascript("ReadiumSDK.reader.stopPagination();");
            } else {
                this.paginationState = PaginationState.DOING;
                this.isPaginationNeeded = false;
                this.callJavascript("ReadiumSDK.reader.trigger(ReadiumSDK.Events.SETUP_PAGINATION, JSON.parse('" + openBookData + "'))");
            }
        }
    }
}
