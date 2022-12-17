package kr.co.smartandwise.eco_epub3_module.Drm;

public enum Drm {
	YES24("yes24lib"), MARKANY("mekia"), NANET("nanet"), NONE("none");
	
	private String schemePrefix;
	
	Drm(String scheme) {
		this.schemePrefix = scheme;
	}
	
	public String getSchemePrefix() {
		return this.schemePrefix;
	}
}
