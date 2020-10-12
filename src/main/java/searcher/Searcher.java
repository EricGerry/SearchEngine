package searcher;

import common.DocInfo;
import index.Index;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class WeightComparator implements Comparator<Index.Weight> {
    @Override
    public int compare(Index.Weight o1, Index.Weight o2) {
        // 如果 o1 < o2 返回一个 < 0
        // 如果 o1 > o2 返回一个 > 0
        // 如果 o1 == o2 返回 0
        return o2.weight - o1.weight;
    }
}

/**
 * 通过这个类来完成核心的搜索过程
 */
public class Searcher {
    private Index index = new Index();

    public Searcher() throws IOException {
        index.build("d:/raw_data.txt");
    }

    public List<Result> search(String query) {
        // 1. [分词] 针对查询词进行分词
        List<Term> terms = ToAnalysis.parse(query).getTerms();
        // 2. [触发] 针对查询词的分词结果依次查找倒排索引, 得到一大堆相关的 docId
        // 这个 ArrayList 中就保存 每个分词结果 得到的倒排拉链的整体结果
        ArrayList<Index.Weight> allTokenResult = new ArrayList<>();
        for (Term term : terms) {
            // 此处得到的 word 就已经是全小写的内容了. 索引中的内容也是小写的
            String word = term.getName();
            List<Index.Weight> invertedList = index.getInverted(word);
            if (invertedList == null) {
                // 用户输入的这部分词很生僻, 在所有文档中都不存在
                continue;
            }
            allTokenResult.addAll(invertedList);
        }
        // 3. [排序] 按照权重进行降序排序
        //    匿名内部类
        allTokenResult.sort(new WeightComparator());
        // 4. [包装结果] 根据刚才查找到的 docId 在正排中查找 DocInfo, 包装成 Result 对象
        ArrayList<Result> results = new ArrayList<>();
        for (Index.Weight weight : allTokenResult) {
            // 根据 weight 中包含的 docId 找到对应的 DocInfo 对象
            DocInfo docInfo = index.getDocInfo(weight.docId);
            Result result = new Result();
            result.setTitle(docInfo.getTitle());
            result.setShowUrl(docInfo.getUrl());
            result.setClickUrl(docInfo.getUrl());
            // GenDesc 做的事情是从正文中摘取一段摘要信息. 根据当前的这个词, 找到这个词在正文中的位置
            // 再把这个位置周围的文本都获取到. 得到了一个片段
            result.setDesc(GenDesc(docInfo.getContent(), weight.word));
            results.add(result);
        }
        return results;
    }

    // 这个方法根据当前的词, 提取正文中的一部分内容作为描述.
    // 以下的实现逻辑还是咱们拍脑门出来的.
    private String GenDesc(String content, String word) {
        // 查找 word 在 content 中出现的位置.
        // word 里内容已经是全小写了. content 里头还是大小写都有.
        int firstPos = content.toLowerCase().indexOf(word);
        if (firstPos == -1) {
            // 极端情况下, 某个词只在标题中出现, 而没在正文中出现, 在正文中肯定找不到了
            // 这种情况非常少见, 咱们暂时不考虑
            return "";
        }
        // 从 firstPos 开始往前找 60 个字符, 作为描述开始. 如果前面不足 60 个, 就从正文头部开始;
        int descBeg = firstPos < 60 ? 0 : firstPos - 60;
        // 从描述开始往后找 160 个字符作为整个描述内容. 如果后面不足 160 个, 把剩下的都算上.
        if (descBeg + 160 > content.length()) {
            return content.substring(descBeg);
        }
        return content.substring(descBeg, descBeg + 160) + "...";
    }
}
