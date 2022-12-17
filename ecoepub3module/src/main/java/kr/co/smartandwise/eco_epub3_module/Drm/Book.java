package kr.co.smartandwise.eco_epub3_module.Drm;

public class Book {
	private Drm drm;
	private String ePubFileName;
	private String userId;
	
	public Drm getDrm() {
		return drm;
	}
	
	public void setDrm(Drm drm) {
		this.drm = drm;
	}
	
	public String getePubFileName() {
		return ePubFileName;
	}
	
	public void setePubFileName(String ePubFileName) {
		this.ePubFileName = ePubFileName;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
}