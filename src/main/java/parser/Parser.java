package parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

// 遍历文档目录, 读取所有的 html 文档内容, 把结果解析成行文本文件.
// 每一行都对应一个文档. 每一行中都包含 文档标题, 文档的 URL, 文档的正文(去掉 html 标签的内容)
// Parser 是一个单独的可执行的类(带有 main 方法)
public class Parser {
    // 下载好的 Java API 文档在哪
    private static final String INPUT_PATH = "D:\\jdk1.8\\docs\\api";
    // 预处理模块输出文件存放的目录
    private static final String OUTPUT_PATH = "D:\\raw_data.txt";

    public static void main(String[] args) throws IOException {
        FileWriter resultFileWriter = new FileWriter(new File(OUTPUT_PATH));
        // 通过 main 完成整个预处理的过程
        // 1. 枚举出 INPUT_PATH 下所有的 html 文件(递归)
        ArrayList<File> fileList = new ArrayList<>();
        enumFile(INPUT_PATH, fileList);
        // 2. 针对枚举出来的html文件路径进行遍历, 依次打开每个文件, 并读取内容.
        //    把内容转换成需要的结构化的数据(DocInfo对象)
        for (File f : fileList) {
            System.out.println("converting " + f.getAbsolutePath() + " ...");
            // 最终输出的 raw_data 文件是一个行文本文件. 每一行对应一个 html 文件.
            // line 这个对象就对应到一个文件.
            String line = convertLine(f);
            // 3. 把得到的结果写入到最终的输出文件中(OUTPUT_PATH). 写成行文本的形式
            resultFileWriter.write(line);
        }
        resultFileWriter.close();
    }

    // 此处咱们的 raw_data 文件使用行文本来表示只是一种方式而已.
    // 完全也可以使用 json 或者 xml 或者其他任何你喜欢的方式来表示都行
    private static String convertLine(File f) throws IOException {
        // 1. 根据 f 转换出 标题
        String title = convertTitle(f);
        // 2. 根据 f 转换出 url
        String url = convertUrl(f);
        // 3. 根据 f 转换成正文, a) 去掉 html 标签; b) 去掉换行符
        String content = convertContent(f);
        // 4. 把这三个部分拼成一行文本
        //    \3 起到分割三个部分的效果. \3 ASCII 值为 3 的字符
        //    在一个 html 这样的文本文件中是不会存在 \3 这种不可见字符
        //    类似的, 使用 \1 \2 \4 \5....来分割效果也是一样
        return title + "\3" + url + "\3" + content + "\n";
    }

    private static String convertContent(File f) throws IOException {
        // 这个方法做两件事情:
        // 1. 把 html 中的标签去掉
        // 2. 把 \n 也去掉
        // 一个一个字符读取并判定
        FileReader fileReader = new FileReader(f);
        boolean isContent = true;
        StringBuilder output = new StringBuilder();
        while (true) {
            int ret = fileReader.read();
            if (ret == -1) {
                // 读取完毕了
                break;
            }
            char c = (char)ret;
            if (isContent) {
                // 当前这部分内容是正文
                if (c == '<') {
                    isContent = false;
                    continue;
                }
                if (c == '\n' || c == '\r') {
                    c = ' ';
                }
                output.append(c);
            } else {
                // 当前这个部分内容是标签
                // 不去写 output
                if (c == '>') {
                    isContent = true;
                }
            }
        }
        fileReader.close();
        return output.toString();
    }

    private static String convertUrl(File f) {
        // URL 线上文档对应的 URL
        // 线上文档 URL 形如:
        // https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html
        // 本地目录文档路径形如:
        // D:\jdk1.8\docs\api\java/util\Collection.html
        // 线上文档的 URL 由两个部分构成.
        // part1: https://docs.oracle.com/javase/8/docs/api  固定的
        // part2: /java/util/Collection.html 和本地文件的路径密切相关.
        // 此处对于 浏览器 来说, / 或者 \ 都能识别.
        String part1 = "https://docs.oracle.com/javase/8/docs/api";
        String part2 = f.getAbsolutePath().substring(INPUT_PATH.length());
        return part1 + part2;
    }

    private static String convertTitle(File f) {
        // 把文件名(不是全路径, 去掉.html后缀)作为标题就可以了
        // 文件名: EntityResolver.html
        // 全路径: D:\jdk1.8\docs\api\org\xml\sax\EntityResolver.html
        String name = f.getName();
        return name.substring(0, name.length() - ".html".length());
    }

    // 当这个方法递归完毕后, 当前 inputPath 目录下所有子目录中的 html 的路径就都被收集到
    // fileList 这个 List 中了
    private static void enumFile(String inputPath, ArrayList<File> fileList) {
        // 递归的把 inputPath 对应的全部目录和文件都遍历一遍
        File root = new File(inputPath);
        // listFiles 相当于 Linux 上的 ls 命令.
        // 就把当前目录下所有的文件和目录都罗列出来了. 当前目录就是 root 对象所对应的目录
        File[] files = root.listFiles();
        // System.out.println(Arrays.toString(files));
        // 遍历当前这些目录和文件路径, 分别处理
        for (File f : files) {
            if (f.isDirectory()) {
                // 如果当前这个 f 是一个目录. 递归调用 enumFile
                enumFile(f.getAbsolutePath(), fileList);
            } else if (f.getAbsolutePath().endsWith(".html")) {
                // 如果当前 f 不是一个目录, 看文件后缀是不是 .html. 如果是就把这个文件的对象
                // 加入到 fileList 这个 List 中
                fileList.add(f);
            }
        }
    }
}
