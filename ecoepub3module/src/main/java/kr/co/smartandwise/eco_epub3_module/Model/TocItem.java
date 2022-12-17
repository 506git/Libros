package kr.co.smartandwise.eco_epub3_module.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class TocItem implements Parcelable {
    private String text;
    private String href;

    public TocItem(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents () {
        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags)
    {
        dest.writeString (text);
        dest.writeString (href);
    }

    public void readFromParcel(Parcel in) {
        this.text = in.readString();
        this.href = in.readString();
    }

    @SuppressWarnings("rawtypes")
    public static final Creator CREATOR = new Creator() {

        @Override
        public TocItem createFromParcel(Parcel in) {
            return new TocItem(in);
        }

        @Override
        public TocItem[] newArray(int size) {
            // TODO Auto-generated method stub
            return new TocItem[size];
        }

    };
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String toString() {
        return "{\"text\":\""+text+"\",\"href\":\""+href+"\"}";
    }
}