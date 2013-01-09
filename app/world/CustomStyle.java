package world;

import java.util.Comparator;


public class CustomStyle {

    protected String name = "default";
    protected String css = "default.scss";

    public CustomStyle(String name, String css) {
        super();
        this.name = name;
        this.css = css;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCSS() {
        return this.css;
    }

    public void setCSS(String css) {
        this.css = css;
    }

    public static final Comparator<CustomStyle> BY_NAME = new Comparator<CustomStyle>() {
        @Override
        public int compare(CustomStyle b1, CustomStyle b2) {
            return b1.name.compareTo(b2.name);
        }
    };

}
