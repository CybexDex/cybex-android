package com.cybex.provider.http.entity;

public class EtoBanner {

    private String id;
    private int banner;
    private String adds_banner;
    private String adds_banner__lang_en;
    private int index;

    public EtoBanner(String id, int banner, String adds_banner, String adds_banner__lang_en, int index) {
        this.id = id;
        this.banner = banner;
        this.adds_banner = adds_banner;
        this.adds_banner__lang_en = adds_banner__lang_en;
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBanner() {
        return banner;
    }

    public void setBanner(int banner) {
        this.banner = banner;
    }

    public String getAdds_banner() {
        return adds_banner;
    }

    public void setAdds_banner(String adds_banner) {
        this.adds_banner = adds_banner;
    }

    public String getAdds_banner__lang_en() {
        return adds_banner__lang_en;
    }

    public void setAdds_banner__lang_en(String adds_banner__lang_en) {
        this.adds_banner__lang_en = adds_banner__lang_en;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
