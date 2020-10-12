package index;

import common.DocInfo;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Index {
    // word 这个词在 docId 文档中对应的权重是多少
    static public class Weight {
        public String word;
        public int docId;
        // weight 生成公式: 10 * 标题中出现的次数 + 正文中出现的次数
        public int weight;
    }

    // 索引类需要包含两方面的内容. 正排索引, 倒排索引
    // 正排索引是 docId => DocInfo 直接把 docId 作为数组下标就行了
    private ArrayList<DocInfo> forwardIndex = new ArrayList<>();
    // 倒排索引 词 => 一组 docId
    // 不光能得到每个词在哪些文档中出现过, 还想知道这个词在该文档中的权重是多少.
    private HashMap<String, ArrayList<Weight>> invertedIndex = new HashMap<>();

    // Index 类需要提供的方法
    // 查正排
    public DocInfo getDocInfo(int docId) {
        return forwardIndex.get(docId);
    }

    // 查倒排
    public ArrayList<Weight> getInverted(String term) {
        return invertedIndex.get(term);
    }

    // 构建索引, 把 raw_data.txt 文件内容读取出来, 加载到内存上面的数据结构中
    // raw_data.txt 是一个行文本文件. 每一行都有三个部分. 每个部分之间使用 \3 分割
    // 索引模块中最复杂的部分
    public void build(String inputPath) throws IOException {
        class Timer {
            public long readFileTime;
            public long buildForwardTime;
            public long buildInvertedTime;
            public long logTime;
        }
        Timer timer = new Timer();

        long startTime = System.currentTimeMillis();
        System.out.println("build start!");

        // 1. 打开文件, 并且按行读取文件内容
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(inputPath)));
        // 2. 读取到的每一行.
        // 加时间是有技巧的. 不应该在比较大的循环中加入过多的打印信息. 打印信息会很多, 看起来很不方便.
        // 打印函数本身也是有时间消耗的.
        while (true) {

            long t1 = System.currentTimeMillis();

            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }

            long t2 = System.currentTimeMillis();

            // 3. 构造正排的过程: 按照 \3 来切分, 切分结果构造成一个 DocInfo 对象, 并加入到正排索引中
            DocInfo docInfo = buildForward(line);

            long t3 = System.currentTimeMillis();

            // 4. 构造倒排的过程: 把 DocInfo 对象里面的内容进一步处理, 构造出倒排
            buildInverted(docInfo);

            long t4 = System.currentTimeMillis();

            // System.out.println("Build " + docInfo.getTitle() + " done!");

            long t5 = System.currentTimeMillis();

            timer.readFileTime += (t2 - t1);
            timer.buildForwardTime += (t3 - t2);
            timer.buildInvertedTime += (t4 - t3);
            timer.logTime += (t5 - t4);
        }
        bufferedReader.close();

        long finishTime = System.currentTimeMillis();
        System.out.println("build finish! time: " + (finishTime - startTime) + " ms");
        System.out.println("readFileTime: " + timer.readFileTime
                + " buildForwardTime: " + timer.buildForwardTime
                + " buildInvertedTime: " + timer.buildInvertedTime
                + " logTime: " + timer.logTime);
    }

    private void buildInverted(DocInfo docInfo) {
        class WordCnt {
            public int titleCount;
            public int contentCount;

            public WordCnt(int titleCount, int contentCount) {
                this.titleCount = titleCount;
                this.contentCount = contentCount;
            }
        }
        HashMap<String, WordCnt> wordCntHashMap = new HashMap<>();
        // 前 4 个操作都是为了给后面的计算权重做铺垫
        // 针对 DocInfo 中的 title 和 content 进行分词, 再根据分词结果构建出 Weight 对象, 更新倒排索引
        // 1. 先针对标题分词
        List<Term> titleTerms = ToAnalysis.parse(docInfo.getTitle()).getTerms();
        // 2. 遍历分词结果, 统计标题中的每个词出现的次数
        for (Term term : titleTerms) {
            // 此处 word 已经是被转成小写了
            String word = term.getName();
            WordCnt wordCnt = wordCntHashMap.get(word);
            if (wordCnt == null) {
                // 当前这个词在哈希表中不存在
                wordCntHashMap.put(word, new WordCnt(1, 0));
            } else {
                // 当前这个词已经在哈希表中存在, 直接修改 titleCount 即可
                wordCnt.titleCount++;
            }
        }
        // 3. 再针对正文分词
        List<Term> contentTerms = ToAnalysis.parse(docInfo.getContent()).getTerms();
        // 4. 遍历分词结果, 统计正文中的每个词出现的次数
        for (Term term : contentTerms) {
            String word = term.getName();
            WordCnt wordCnt = wordCntHashMap.get(word);
            if (wordCnt == null) {
                // 当前这个词在哈希表中不存在
                wordCntHashMap.put(word, new WordCnt(0, 1));
            } else {
                wordCnt.contentCount++;
            }
        }
        // 5. 遍历 HashMap, 依次构建 Weight 对象并更新倒排索引的映射关系
        for (HashMap.Entry<String, WordCnt> entry : wordCntHashMap.entrySet()) {
            Weight weight = new Weight();
            weight.word = entry.getKey();
            weight.docId = docInfo.getDocId();
            // weight = 标题中出现次数 * 10 + 正文中出现次数
            weight.weight = entry.getValue().titleCount * 10 + entry.getValue().contentCount;
            // weight 加入到倒排索引中. 倒排索引是一个 HashMap, value 就是 Weight 构成的 ArrayList
            // 先根据这个词, 找到 HashMap 中对应的这个 ArrayList, 称为 "倒排拉链"
            ArrayList<Weight> invertedList = invertedIndex.get(entry.getKey());
            if (invertedList == null) {
                // 当前这个键值对不存在, 就新加入一个键值对就可以了
                invertedList = new ArrayList<>();
                invertedIndex.put(entry.getKey(), invertedList);
            }
            // 到了这一步, invertedIndex 已经是一个合法的 ArrayList 了, 就可以把 weight 直接加入即可
            invertedList.add(weight);
        }
    }

    private DocInfo buildForward(String line) {
        // 把这一行按照 \3 切分
        // 分出来的三个部分就是一个文档的 标题, URL, 正文
        String[] tokens = line.split("\3");
        if (tokens.length != 3) {
            // 发现文件格式有问题~~
            // 当前场景下咱们有 1w 多个文件. 如果某个文件出现格式问题,
            // 不应该让某个问题影响到整体的索引构建
            // 此处我们的处理方式仅仅就是打印了日志.
            System.out.println("文件格式存在问题: " + line);
            return null;
        }
        // 把新的 docInfo 插入到 数组末尾.
        // 如果数组是 0 个元素, 新的元素所处在的下标就是 0
        DocInfo docInfo = new DocInfo();
        // id 就是正排索引数组下标
        docInfo.setDocId(forwardIndex.size());
        docInfo.setTitle(tokens[0]);
        docInfo.setUrl(tokens[1]);
        docInfo.setContent(tokens[2]);
        forwardIndex.add(docInfo);
        return docInfo;
    }
}
