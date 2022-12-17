package kr.co.smartandwise.eco_epub3_module.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Bookmark implements Parcelable {
    private int id = 0;
    private long createTime = 0;
    private String idref = "";
    private String cfi = "";
    private String content = "";

    public Bookmark() {

    }

    public Bookmark(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents () {
        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags)
    {
        dest.writeInt(this.id);
        dest.writeLong(this.createTime);
        dest.writeString (this.idref);
        dest.writeString (this.cfi);
        dest.writeString (this.content);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.createTime = in.readLong();
        this.idref = in.readString();
        this.cfi = in.readString();
        this.content = in.readString();
    }

    @SuppressWarnings("rawtypes")
    public static final Creator CREATOR = new Creator() {

        @Override
        public Bookmark createFromParcel(Parcel in) {
            return new Bookmark(in);
        }

        @Override
        public Bookmark[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Bookmark[size];
        }

    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getIdref() {
        return idref;
    }

    public void setIdref(String idref) {
        this.idref = idref;
    }

    public String getCfi() {
        return cfi;
    }

    public void setCfi(String cfi) {
        this.cfi = cfi;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
