package kr.co.smartandwise.eco_epub3_module.Model;

public class SearchResult {
	private String keyword;
	private String idref;
	private String cfi;
	private int offset;
	private String surroundingText;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
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

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getSurroundingText() {
		return surroundingText;
	}

	public void setSurroundingText(String surroundingText) {
		this.surroundingText = surroundingText;
	}
}
