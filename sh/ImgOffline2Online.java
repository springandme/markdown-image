

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 将指定 md 文件或文件目录的 md 文件中的 OFFLINE_PATH 替换为 ONLINE_PATH
 *
 *  javac -encoding utf-8 ImgOffline2Online.java
 * @author: hliushi
 * @date: 2023/2/14 18:27
 **/
public class ImgOffline2Online {


    public static final String OFFLINE_PATH = "D:\\\\image\\\\img-local-backup\\\\markdown-image\\\\img\\\\";

    public static final String ONLINE_PATH = "https://raw.githubusercontent.com/springandme/markdown-image/main/img/";

    public static final String PATH_OUT = "D:\\study\\online-md-notes\\";
    public static final String MD_DIR_OR_FILE_PATH = "C:\\Users\\Hliushi\\Desktop\\ImgOffline2Online Test.md";
    public static final String MD_GLOB = "*.md";

    /**
     * public static final String OFFLINE_PATH = "D:\\\\image\\\\img-local-backup\\\\markdown-image\\\\img\\\\";
     * // 匹配 markdown 图片public static final String REGEX_MARKDOWN_IMG = "\\!\\[.*?\]\\((.?)\)";
     * // 匹配网络图片public static final String REGEX_NET_IMG = "<img(.?)src=\"(.?)"(.?)>";
     *
     * @param args
     */
    public static void main(String[] args) {
        List<Path> dirFileNames = getPathList(MD_DIR_OR_FILE_PATH, MD_GLOB);
        for (Path dirFileName : dirFileNames) {
            File file = dirFileName.toFile();
            List<String> lines = readAllLines(file.getAbsolutePath(), 0, Integer.MAX_VALUE);
            lines.replaceAll(s -> s.replaceAll(OFFLINE_PATH, ONLINE_PATH));
            writeLines(PATH_OUT + file.getName(), lines);
        }
    }

    /**
     * 读取文件所有的行
     *
     * @param pathStr    路径
     * @param startIndex 开始行下标
     * @param endIndex   结束行下标
     * @return 列表内容
     */
    public static List<String> readAllLines(String pathStr, int startIndex, int endIndex) {
        Path path = Paths.get(pathStr);
        try {
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            final int size = allLines.size();
            if (endIndex > size) {
                endIndex = size;
            }
            return allLines.subList(startIndex, endIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写入行内容到指定列
     *
     * @param pathStr 路径
     * @param lines   行内容
     */
    public static void writeLines(final String pathStr, final Collection<String> lines) {
        try {
            Path path = Paths.get(pathStr);
            if (!Files.exists(path)) {
                createFile(pathStr);
            }
            Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.SYNC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 递归获取所有对应目录的文件
     *
     * @param rootPathStr 根路径
     * @param glob        文件正则表达式
     * @return 文件列表
     */
    public static List<Path> getPathList(String rootPathStr, String glob) {
        Path rootPath = Paths.get(rootPathStr);
        List<Path> pathList = new ArrayList<>();

        try {
            if (Files.isDirectory(rootPath)) {
                Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
                            for (Path path : stream) {
                                pathList.add(path);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                pathList.add(rootPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return pathList;
    }

    public static boolean createFile(final String filePath) {
        if (null == filePath || "".equals(filePath)) {
            return false;
        }

        if (exists(filePath)) {
            return true;
        }

        File file = new File(filePath);

        // 父类文件夹的处理
        File dir = file.getParentFile();
        if (notExists(dir)) {
            boolean mkdirResult = dir.mkdirs();
            if (!mkdirResult) {
                return false;
            }
        }
        // 创建文件
        try {
            return file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean exists(String filePath, LinkOption... options) {
        if (null == filePath || "".equals(filePath)) {
            return false;
        }

        Path path = Paths.get(filePath);
        return Files.exists(path, options);
    }

    public static boolean notExists(final File file) {
        return !file.exists();
    }
}
