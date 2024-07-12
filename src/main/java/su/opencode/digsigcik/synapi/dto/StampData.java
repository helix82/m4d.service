package su.opencode.digsigcik.synapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StampData {
    @JsonProperty("type")
    private String type;
    @JsonProperty("text")
    private String text;
    @JsonProperty("position")
    private String position;
    @JsonProperty("padding")
    private Integer padding;
    @JsonProperty("margin")
    private Integer margin;

    @JsonProperty("family")
    private String family;
    @JsonProperty("style")
    private String style;
    @JsonProperty("size")
    private Integer size;

    @JsonProperty("width")
    private Integer width;
    @JsonProperty("height")
    private Integer height;
    @JsonProperty("round")
    private Integer round;
    @JsonProperty("color")
    private String color;
    @JsonProperty("inverse")
    private Boolean inverse;



    @JsonProperty("items")
    private StampData[] items;

    public String getType() {
        return (type==null && text!=null)?"text":type;
    }

    public String getText() {
        return text;
    }

    public String getPosition() {
        return (position==null)?"center":position;
    }

    public String getColor() {
        return color;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getPadding() {
        return padding;
    }

    public Integer getMargin() {
        return margin;
    }

    public StampData[] getItems() {
        return items;
    }

    public Integer getRound() {
        return round;
    }

    public Integer getSize() {
        return size;
    }

    public String getFamily() {
        return family;
    }

    public String getStyle() {
        return style;
    }

    public Boolean getInverse() {
        return inverse;
    }
}

