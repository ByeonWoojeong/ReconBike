package app.cosmos.reconbike.DTO;

public class Notice {

    public String title;
    public String content;
    public String image;
    public String type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Notice(String title, String content, String image, String type) {
        this.title = title;
        this.content = content;
        this.image = image;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Notice{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", image='" + image + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
