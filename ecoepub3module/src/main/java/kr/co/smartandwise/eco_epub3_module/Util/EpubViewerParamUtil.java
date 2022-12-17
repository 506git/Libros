package kr.co.smartandwise.eco_epub3_module.Util;

import android.util.Base64;

import com.google.gson.Gson;

import kr.co.smartandwise.eco_epub3_module.Model.EpubViewerParam;

public class EpubViewerParamUtil {
    public static EpubViewerParam createNewObject(String contentId, String contentRootPath) {
        return new EpubViewerParam(contentId, contentRootPath);
    }

    public static EpubViewerParam createNewObjectFromBase64(String epubBase64) throws Exception {
        byte[] data = Base64.decode(epubBase64, Base64.DEFAULT);
        String paramJson = new String(data, "UTF-8");
        Gson gson = new Gson();
        EpubViewerParam param = gson.fromJson(paramJson, EpubViewerParam.class);

        return param;
    }

    public static String createNewBase64FromObject(EpubViewerParam param) throws Exception {
        Gson gson = new Gson();
        String paramJson = gson.toJson(param);
        String paramBase64 = Base64.encodeToString(paramJson.getBytes("UTF-8"), Base64.DEFAULT);

        return paramBase64;
    }


    public static String getPagingInfo(EpubViewerParam param) {
        return param.getPagingInfo();
    }

    public static String getLastReadIdref(EpubViewerParam param) {
        return param.getLastReadPageIdref();
    }

    public static String getLastReadCfi(EpubViewerParam param) {
        return param.getLastReadPageCfi();
    }
}
