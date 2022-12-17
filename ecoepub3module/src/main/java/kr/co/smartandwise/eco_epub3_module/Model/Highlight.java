package kr.co.smartandwise.eco_epub3_module.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Highlight implements Parcelable {
    private int id = -1;
    private long createTime = -1;
    private String idref = "";
    private String cfi = "";
    private String content = "";
    private String memo = "";
    private String color = "";

    public Highlight() {

    }

    public Highlight(Parcel in) {
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
        dest.writeString(this.idref);
        dest.writeString(this.cfi);
        dest.writeString(this.content);
        dest.writeString(this.memo);
        dest.writeString(this.color);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.createTime = in.readLong();
        this.idref = in.readString();
        this.cfi = in.readString();
        this.content = in.readString();
        this.memo = in.readString();
        this.color = in.readString();
    }

    @SuppressWarnings("rawtypes")
    public static final Creator CREATOR = new Creator() {

        @Override
        public Highlight createFromParcel(Parcel in) {
            return new Highlight(in);
        }

        @Override
        public Highlight[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Highlight[size];
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

    public String getMemo() {

        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
