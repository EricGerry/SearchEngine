import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

public class TestAnsj {
    public static void main(String[] args) {
        String str = "My name is tangzhong";
        // 通过刚才的这个 parse 方法就直接分完. 再通过 getTerms 就得到分词结果.
        // 分词库分词英文的时候会把英文单词转成小写.
        List<Term> terms = ToAnalysis.parse(str).getTerms();
        for (Term term : terms) {
            // 分词结果的文本信息
            System.out.println(term.getName());
        }
    }
}
