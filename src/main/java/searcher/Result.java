package searcher;

// 表示一条搜索结果. 是根据 DocInfo 得到的
public class Result {
    private String title;
    // 当前这个场景中, 这两个 URL 就填成一样的内容了
    private String showUrl;
    private String clickUrl;
    private String desc; // 描述. 网页正文的摘要信息, 一般要包含查询词(查询词的一部分)

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShowUrl() {
        return showUrl;
    }

    public void setShowUrl(String showUrl) {
        this.showUrl = showUrl;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "Result{" +
                "title='" + title + '\'' +
                ", showUrl='" + showUrl + '\'' +
                ", clickUrl='" + clickUrl + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
