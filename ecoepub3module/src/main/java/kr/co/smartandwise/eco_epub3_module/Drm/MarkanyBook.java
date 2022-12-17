package kr.co.smartandwise.eco_epub3_module.Drm;

public class MarkanyBook extends Book {

	private String host;
	private String bookId;
	private String libSeq;
	private String userPass;
	private String fileType;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public String getLibSeq() {
		return libSeq;
	}

	public void setLibSeq(String libSeq) {
		this.libSeq = libSeq;
	}

	public String getUserPass() {
		return userPass;
	}

	public void setUserPass(String userPass) {
		this.userPass = userPass;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

}
