// common 包表示每个模块都可能用到的公共信息
package common;

// 表示一个文档对象(HTML对象)
// 根据这些内容后面才能制作索引, 完成搜索过程.
public class DocInfo {
    // docId 文档的唯一身份标识(不能重复)
    private int docId;
    // 该文档的标题. 简单粗暴的使用文件名来表示.
    // Collection.html => Collection
    private String title;
    // 该文档对应的线上文档的 URL. 根据本地文件路径可以构造出线上文档的 URL
    private String url;
    // 该文档的正文. 把 html 文件中的 html 标签去掉, 留下的内容
    private String content;

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "DocInfo{" +
                "docId=" + docId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
