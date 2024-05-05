// Authors: Youssef Emad

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Arrays;

/*
ID:20012281
Name: Youssef Emad Mohamed Habib

I acknowledge that I am aware of the academic integrity guidelines of this
course, and that I worked on this assignment independently without any
unauthorized help
*/
public class Main {
    public static Map<chunk, Long> readFromFile(int chunkSize, String inputFilePath) throws IOException {
        File file = new File(inputFilePath);
        long numChunks = file.length() / chunkSize;
        long numOfPossibleChunks = (long) Math.pow(256, chunkSize);
        long minNumChunks = Math.min(numChunks, numOfPossibleChunks);
        int numberOfChunks = minNumChunks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) minNumChunks;
        Map<chunk, Long> chunkFreq = new HashMap<>(numberOfChunks);
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath))) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead < chunkSize) {
                    byte[] actualBytesRead = new byte[bytesRead];
                    System.arraycopy(buffer, 0, actualBytesRead, 0, bytesRead);
                    buffer = actualBytesRead;
                }
                chunk chunk = new chunk(buffer);
                if (chunkFreq.containsKey(chunk)) {
                    chunkFreq.put(chunk, chunkFreq.get(chunk) + 1);
                } else {
                    chunkFreq.put(chunk, 1L);
                }
                buffer = new byte[chunkSize];
            }
            return chunkFreq;
        } catch (IOException e) {
            throw new IOException("Error reading from file", e);
        }

    }

    public static void compress(String inputFile, String outputFileName, int chunkSize) throws IOException {
        Map<chunk, Long> chunkFreq = readFromFile(chunkSize, inputFile);
        HuffmanNode root = buildHuffmanTree(chunkFreq);
        Map<chunk, String> huffmanCodes = new HashMap<>(chunkFreq.size());
        encode(root, "", huffmanCodes);
        long totalBits = 0;
        for (Map.Entry<chunk, String> entry : huffmanCodes.entrySet()) {
            chunk key = entry.getKey();
            String value = entry.getValue();
            totalBits += value.length() * chunkFreq.get(key);
        }

        compressFile(inputFile, outputFileName, huffmanCodes, chunkSize, totalBits);
    }

    private static void compressFile(String inputFile, String outputFileName, Map<chunk, String> huffmanCodes, int chunkSize, long totalBits) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile)); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFileName)); DataOutputStream dOut = new DataOutputStream(bos)) {
            StringBuilder sb = new StringBuilder();
            dOut.writeLong(totalBits);
            dOut.writeInt(huffmanCodes.size());
            for (Map.Entry<chunk, String> entry : huffmanCodes.entrySet()) {
                dOut.writeInt(entry.getKey().data.length);
                bos.write(entry.getKey().data);
                dOut.writeInt(entry.getValue().length());
                for (char c : entry.getValue().toCharArray()) {
                    sb.append(c);
                    if (sb.length() == 8) {
                        dOut.writeByte(Integer.parseInt(sb.toString(), 2));
                        sb.setLength(0);
                    }
                }
                if (!sb.isEmpty()) {
                    while (sb.length() < 8) {
                        sb.append("0");
                    }
                    dOut.writeByte(Integer.parseInt(sb.toString(), 2));
                    sb.setLength(0);
                }
            }


            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                if (bytesRead < chunkSize) {
                    byte[] actualBytesRead = new byte[bytesRead];
                    System.arraycopy(buffer, 0, actualBytesRead, 0, bytesRead);
                    buffer = actualBytesRead;
                }
                chunk chunk = new chunk(buffer);
                String code = huffmanCodes.get(chunk);
                for (char c : code.toCharArray()) {
                    sb.append(c);
                    if (sb.length() == 8) {
                        bos.write(Integer.parseInt(sb.toString(), 2));
                        sb.setLength(0);
                    }
                }
                buffer = new byte[chunkSize];
            }
            if (!sb.isEmpty()) {
                while (sb.length() < 8) {
                    sb.append("0");
                }
                bos.write(Integer.parseInt(sb.toString(), 2));
                sb.setLength(0);
            }
            //we need now to get the compression ratio
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(inputFile);
        long inputFileSize = file.length();
        File file2 = new File(outputFileName);
        long outputFileSize = file2.length();
        System.out.println("Successfully compressed file");
        System.out.println();
        System.out.println("Compression ratio: " + (float) outputFileSize / inputFileSize);

    }

    public static void encode(HuffmanNode root, String s, Map<chunk, String> huffmanCodes) {
        if (root == null) {
            return;
        }
        if (root.data != null) {
            if (s.isEmpty()) {
                s = "0";
            }
            huffmanCodes.put(new chunk(root.data), s);
            return;
        }
        encode(root.left, s + "0", huffmanCodes);
        encode(root.right, s + "1", huffmanCodes);
    }

    public static HuffmanNode buildHuffmanTree(Map<chunk, Long> chunkFreq) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(Comparator.comparingLong(l -> l.freq));
        chunkFreq.forEach((key, value) -> pq.add(new HuffmanNode(key.data, value)));
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            if (right == null) throw new AssertionError();
            if (left == null) throw new AssertionError();
            HuffmanNode parent = new HuffmanNode(null, left.freq + right.freq);
            parent.left = left;
            parent.right = right;
            pq.add(parent);
        }
        return pq.poll();
    }

    public static void decompress(String inputFile, String outputFile) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile)); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            Map<chunk, String> huffmanCodes = new HashMap<>();
            long totalBits = readHeaders(bis, huffmanCodes);
            Map<String, chunk> huffmanCodesReverse = new HashMap<>(huffmanCodes.size());
            for (Map.Entry<chunk, String> entry : huffmanCodes.entrySet()) {
                huffmanCodesReverse.put(entry.getValue(), entry.getKey());
            }
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int i = 0;
            int bytesRead;
            while ((bytesRead = bis.read()) != -1) {
                byte buffer = (byte) bytesRead;
                for (int j = 7; j >= 0; j--) {
                    sb.append((buffer >> j) & 1);
                    totalBits--;
                    if (huffmanCodesReverse.containsKey(sb.toString())) {
                        String Code = sb.toString();
                        for (byte b : huffmanCodesReverse.get(Code).data) {
                            bytes[i++] = b;
                            if (i == 1024) {
                                bos.write(bytes);
                                i = 0;
                            }
                        }
                        sb.setLength(0);
                    }
                    if (totalBits == 0) {
                        break;
                    }
                }
            }
            if (i != 0) {
                bos.write(bytes, 0, i);
            }
        }

    }

    private static long readHeaders(BufferedInputStream bis, Map<chunk, String> huffmanCodes) throws IOException {
        long totalBits;
        DataInputStream dIn = new DataInputStream(bis);
        totalBits = dIn.readLong();
        int huffmanCodesSize = dIn.readInt();
        for (int i = 0; i < huffmanCodesSize; i++) {
            int chunkSize = dIn.readInt();
            byte[] chunkData = new byte[chunkSize];
            bis.read(chunkData);
            int codeLength = dIn.readInt();
            StringBuilder sb = new StringBuilder();
            while (codeLength > 0) {
                byte code = dIn.readByte();
                for (int j = 7; j >= 0 && codeLength > 0; j--) {
                    sb.append((code >> j) & 1);
                    codeLength--;
                }
            }
            huffmanCodes.put(new chunk(chunkData), sb.toString());
        }
        return totalBits;
    }

/*
    ID:20012281
    Name: Youssef emad mohamed habib

    I acknowledge that I am aware of the academic integrity guidelines of this
    course, and that I worked on this assignment independently without any
    unauthorized help
*/
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter the mode and the file name");
            return;
        }
        String mode = args[0];
        if (mode.equals("c")) {
            String inputFileName = args[1];
            int chunkSize = Integer.parseInt(args[2]);
            int index = inputFileName.lastIndexOf(File.separator);
            String outputFileName = inputFileName.substring(0, index) + File.separator + "20012281" + "." + chunkSize + "." + inputFileName.substring(index + 1) + ".hc";
            long startTime = System.currentTimeMillis();
            compress(inputFileName, outputFileName, chunkSize);
            long endTime = System.currentTimeMillis();
            System.out.println("Time taken to compress the file: " + (endTime - startTime) + "ms");
        } else if (mode.equals("d")) {
            String inputFileName = args[1];
            int index = inputFileName.lastIndexOf(File.separator);
            String outputFileName = inputFileName.substring(0, index) + File.separator + "extracted." + inputFileName.substring(index + 1, inputFileName.length() - 3);
            long startTime = System.currentTimeMillis();
            decompress(inputFileName, outputFileName);
            long endTime = System.currentTimeMillis();
            System.out.println("Successfully decompressed");
            System.out.println("Time taken to decompress the file: " + (endTime - startTime) + "ms");
        }
    }

    public static class chunk implements Comparable<chunk> {
        byte[] data;

        public chunk(byte[] data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || getClass() != o.getClass()) return false;

            chunk chunk = (chunk) o;
            return Arrays.equals(data, chunk.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }

        @Override
        public int compareTo(chunk o) {
            return Arrays.compare(data, o.data);
        }
    }

    public static class HuffmanNode {
        byte[] data;
        long freq;
        HuffmanNode left;
        HuffmanNode right;

        public HuffmanNode(byte[] data, long freq) {
            this.data = data;
            this.freq = freq;
        }
    }
}
