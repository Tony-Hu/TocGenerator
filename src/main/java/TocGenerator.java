import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TocGenerator {

  public static class Info {
    String name;
    String title;
    String link;
    String label;
    String difficulty;
    public Info(String name, String title, String link, String label, String difficulty) {
      this.name = name;
      this.title = title;
      this.link = link;
      this.label = label;
      this.difficulty = difficulty;
    }

    @Override
    public String toString() {
      return name + ", " + title + ", " + link + ", " + label + ", " + difficulty;
    }
  }

  public static final String MAC_SRC_PATH = "/Users/tony/CSUEB/Java/ShuaTi/src/main/java";
  public static final String WIN_SRC_PATH = "D:\\CSUEB\\Java\\ShuaTi\\src\\main\\java";
  public static final String MAC_OUT_NAME = "/Users/tony/CSUEB/Java/ShuaTi/README.md";
  public static final String WIN_OUT_NAME = "D:\\CSUEB\\Java\\ShuaTi\\README.md";
  public static final String GITHUB_SRC = "https://github.com/Tony-Hu/ShuaTi-Online.Judge.Problems.Solving/tree/master/src/main/java";
  public static final String TEMPLATE_NAME = "template.md";
  public static final Map<String, String> labelMap = new HashMap<>();
  public static boolean isWindows;

  static {
    labelMap.put("string", "String");
    labelMap.put("binarySearch", "Binary Search");
    labelMap.put("unionFind", "Union Find");
    labelMap.put("bfs", "BFS");
    labelMap.put("bitOperation", "Bit Operation");
    labelMap.put("array", "Array");
    labelMap.put("mathematics", "Mathematics");
    labelMap.put("dp", "Dynamic Programming");
    labelMap.put("stack", "Stack");
    labelMap.put("tree", "Tree");
  }

  public static void main(String[] args) throws IOException {
    String os = System.getProperty("os.name");
    String path;
    if (os.toLowerCase().contains("windows")) {
      path = WIN_SRC_PATH;
      isWindows = true;
    } else {
      path = MAC_SRC_PATH;
    }

    List<Info> fileInfo = getFileInfo(path);
    Scanner tempLateScanner = new Scanner(new File(TEMPLATE_NAME));
    generateReadmeDotMd(fileInfo, tempLateScanner);
  }

  private static List<Info> getFileInfo(String path) throws FileNotFoundException {
    File root = new File(path);
    String[] list = root.list();
    List<Info> fileInfo = new ArrayList<>();
    for (String name : list) {
      File current = new File(path, name);
      if (current.isDirectory()) {
        parseFiles(current, fileInfo);
      }
    }
    fileInfo.sort((info1, info2) -> {
      int i1 = Integer.valueOf(info1.name.substring(8));
      int i2 = Integer.valueOf(info2.name.substring(8));
      return Integer.compare(i1, i2);
    });

    return fileInfo;
  }

  private static void parseFiles(File dir, List<Info> fileInfo) throws FileNotFoundException {
    String[] fileName = dir.list();
    for (String file : fileName) {
      if (!file.contains(".java")) {
        continue;
      }
      Scanner scanner = new Scanner(new File(dir, file));
      String title = null;
      String link = null;
      String difficulty = null;
      while (scanner.hasNextLine()) {
        String current = scanner.nextLine();
        if (current.contains("Link")) {
          link = current.substring(current.indexOf(":") + 2);
        } else if (current.contains("Title")) {
          title = current.substring(current.indexOf(":") + 2);
        } else if (current.contains("Difficulty")) {
          difficulty = current.substring(current.indexOf(":") + 2);
          break;
        }
      }
      fileInfo.add(new Info(file.split("\\.")[0], title, link, dir.getName(), difficulty));
      scanner.close();
    }
  }

  private static void generateReadmeDotMd(List<Info> fileInfo, Scanner templateScanner) throws IOException {
    String outName;
    if (isWindows) {
      outName = WIN_OUT_NAME;
    } else {
      outName = MAC_OUT_NAME;
    }

    File file = new File(outName);
    if (!file.exists()) {
      file.createNewFile();
    }
    try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      while (templateScanner.hasNextLine()) {
        writer.write(templateScanner.nextLine() + "\n");
      }
      templateScanner.close();
      generateStatistic(writer, fileInfo);

      generateHeader(writer);
      generateContent(writer, fileInfo);
      generateTail(writer);
    }
  }

  private static void generateStatistic(BufferedWriter writer, List<Info> fileInfo) throws IOException {
    generateStatHeader(writer);
    generateStatContent(writer, fileInfo);
    generateTail(writer);
    writer.write("<br>\n");
  }

  private static void generateStatHeader(BufferedWriter writer) throws IOException {
    writer.write("<table>\n" +
        "  <tbody>\n");
    writer.write("    <tr>\n" +
        "      <th>No. </th>\n" +
        "      <th>Category</th>\n" +
        "      <th>Sum</th>\n" +
        "      <th>Easy</th>\n" +
        "      <th>Medium</th>\n" +
        "      <th>Hard</th>\n" +
        "    </tr>\n");
  }

  private static void generateStatContent(BufferedWriter writer, List<Info> fileInfo) throws IOException {
    List<Map.Entry<String, String>> list = new ArrayList<>(labelMap.entrySet());
    list.sort(Comparator.comparing(Map.Entry::getKey));

    int j = 1;
    for (Map.Entry<String, String> entry : list) {
      long easy = fileInfo.stream().filter(i -> entry.getKey().equals(i.label) && "Easy".equals(i.difficulty)).count();
      long medium = fileInfo.stream().filter(i -> entry.getKey().equals(i.label) && "Medium".equals(i.difficulty)).count();
      long hard = fileInfo.stream().filter(i -> entry.getKey().equals(i.label) && "Hard".equals(i.difficulty)).count();
      writer.write("    <tr>\n" +
          "      <td>" + j++ + "</td>\n" +
          "      <td><a href=\"" + GITHUB_SRC + "/"  + entry.getKey() + "\">"+ entry.getValue() + "</a></td>\n" +
          "      <td>" + (easy + medium + hard) + "</td>\n" +
          "      <td>" + easy + "</td>\n" +
          "      <td>" + medium + "</td>\n" +
          "      <td>" + hard + "</td>\n" +
          "    </tr>\n");
    }
    writer.write("    <tr>\n" +
        "      <td></td>\n" +
        "      <td><b>Total</b></td>\n" +
        "      <td>" + fileInfo.size() + "</td>\n" +
        "      <td>" + fileInfo.stream().filter(i -> "Easy".equals(i.difficulty)).count() + "</td>\n" +
        "      <td>" + fileInfo.stream().filter(i -> "Medium".equals(i.difficulty)).count() + "</td>\n" +
        "      <td>" + fileInfo.stream().filter(i -> "Hard".equals(i.difficulty)).count() + "</td>\n" +
        "    </tr>\n");
  }

  private static void generateHeader(BufferedWriter writer) throws IOException {
    writer.write("<table>\n" +
        "  <tbody>\n");
    writer.write("    <tr>\n" +
        "      <th>No. (Link to <a href=\"" + GITHUB_SRC + "\">src</a>)</th>\n" +
        "      <th>Difficulty</th>\n" +
        "      <th>Title (Link to <a href=\"https://www.lintcode.com/problem/\">LintCode</a>)</th>\n" +
        "      <th>Category</th>\n" +
        "      <th>Note</th>\n" +
        "    </tr>\n");
  }

  private static void generateTail(BufferedWriter writer) throws IOException {
    writer.write("  </tbody>\n</table>");
    writer.flush();
  }

  private static void generateContent(BufferedWriter writer, List<Info> fileInfo) throws IOException {
    for (Info info : fileInfo) {
      writer.write("    <tr>\n" +
          "      <td><a href=\"" + GITHUB_SRC +  "/" + info.label + "/" + info.name + ".java" + "\">" + info.name + "</a></td>\n" +
          "      <td>" + info.difficulty + "</td>\n" +
          "      <td><a href=\"" + info.link + "\">" + info.title + "</a></td>\n" +
          "      <td><a href=\"" + GITHUB_SRC+ "/" + info.label + "\">" + labelMap.get(info.label) + "</a></td>\n" +
          "      <td></td>\n" +
          "    </tr>\n");
    }
  }
}
