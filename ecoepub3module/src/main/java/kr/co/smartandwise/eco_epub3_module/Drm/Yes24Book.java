package kr.co.smartandwise.eco_epub3_module.Drm;

public class Yes24Book extends Book {
	private String libId;
	private String userPw;
	private String eBookId;
	private String licenseId;
	private String rtnUrl;

	public String getLibId() {
		return libId;
	}

	public void setLibId(String libId) {
		this.libId = libId;
	}

	public String getUserPw() {
		return userPw;
	}

	public void setUserPw(String userPw) {
		this.userPw = userPw;
	}

	public String geteBookId() {
		return eBookId;
	}

	public void seteBookId(String eBookId) {
		this.eBookId = eBookId;
	}

	public String getLicenseId() {
		return licenseId;
	}

	public void setLicenseId(String licenseId) {
		this.licenseId = licenseId;
	}

	public String getRtnUrl() {
		return rtnUrl;
	}

	public void setRtnUrl(String rtnUrl) {
		this.rtnUrl = rtnUrl;
	}

}
