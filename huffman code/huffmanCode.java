import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class huffmanCode {

  public static class huffmanNode {
    String data;
    int freq;
    huffmanNode left;
    huffmanNode right;
    public huffmanNode(String data, int freq) {
      this.data = data;
      this.freq = freq;
    }
  }

  private static String huffmanCodeString(huffmanNode root, String s, Map<String, String> huffmanCode) {
    if (root == null) {
      return "";
    }
    if (root.left == null && root.right == null) {
      huffmanCode.put(root.data, s);
      return s;
    }
    String s1 = huffmanCodeString(root.left, s + "0", huffmanCode);
    String s2 = huffmanCodeString(root.right, s + "1", huffmanCode);
    return s1 + s2;
  }

  private static huffmanNode huffmanCodeCompress(Map<String, Integer> freqMap) {
    PriorityQueue<huffmanNode> pq = new PriorityQueue<>((n1, n2) -> n1.freq - n2.freq);
    for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
      pq.add(new huffmanNode(entry.getKey(), entry.getValue()));
    }
    while (pq.size() > 1) {
      huffmanNode left = pq.poll();
      huffmanNode right = pq.poll();
      huffmanNode parent = new huffmanNode("", left.freq + right.freq);
      parent.left = left;
      parent.right = right;
      pq.add(parent);
    }
    return pq.poll();
  }

  public static void main(String[] args) {
    String inputFilePath = "./input.txt";
    int chunkSize =1;
    Map<String, Integer> freqMap = new HashMap<>();

    // Read the file in chunks of chunkSize bytes
    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFilePath))) {
      byte[] chunk = new byte[chunkSize];
      int bytesRead;
      while ((bytesRead = bis.read(chunk)) != -1) {
        if (bytesRead < chunkSize) {
          chunk = Arrays.copyOf(chunk, bytesRead); // Truncate the array
        }
        String chunkString = new String(chunk);
        freqMap.put(chunkString, freqMap.getOrDefault(chunkString, 0) + 1);
      }
    } catch (FileNotFoundException e) {
      System.err.println("File not found: " + inputFilePath);
      return;
    } catch (IOException e) {
      System.err.println("Error reading file: " + inputFilePath);
      e.printStackTrace();
      return;
    }

    // Build the Huffman tree
    huffmanNode root = huffmanCodeCompress(freqMap);
    if (root == null) {
      System.err.println("No data to compress");
      return;
    }
    // Build the Huffman code for each chunk
    Map<String, String> huffmanCode = new HashMap<>();
    huffmanCodeString(root, "", huffmanCode);
  }
}