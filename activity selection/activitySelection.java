import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

class activitySelection {

  public static class activity {
    int start;
    int end;
    int weight;
    activity(int start, int end, int weight) {
      this.start = start;
      this.end = end;
      this.weight = weight;
    }
  }
 private int getNextAct(activitySelection.activity[] a, int low, int end) {
    int high = a.length - 1;
    int mid;
    while (low <= high) {
      mid = low + (high - low) / 2;
      if (a[mid].start >= end) {
        high = mid - 1;
      } else {
        low = mid + 1;
      }
    }

    return low;
  }
 private int activitySelect(activity[] a, int i, int[] dp) {
    if (i >= a.length) {
      return 0;
    }
    if (dp[i] != -1) {
      return dp[i];
    }
    if (i == a.length - 1) {
      dp[i] = a[i].weight;
      return dp[i];
    }
    int weightWithoutI = activitySelect(a, i + 1, dp);
    int weightWithI = a[i].weight + activitySelect(a, getNextAct(a, i + 1, a[i].end), dp); 
    dp[i] = Math.max(weightWithoutI, weightWithI);
    return dp[i];
  }

 

  private int activitySel(activity[] a) {
    int n = a.length;
    Arrays.sort(a, new Comparator<activity>() {
      public int compare(activity a1, activity a2) {
        if (a1.start == a2.start) {
          return a1.end - a2.end;  // If start times are equal, sort by end time
        } else {
          return a1.start - a2.start;
        }
      }
    });
    int[] dp = new int[n];
    Arrays.fill(dp, -1);
    int ans = activitySelect(a, 0, dp);
    return ans;
  }
  
  //get the data from the file and store it in an array
 private activity[] getData(String filename) throws IOException {
    File f = new File(filename);
    Scanner scanner = new Scanner(f);
    int n = scanner.nextInt();
    activity[] a = new activity[n];
    for (int i = 0; i < n; i++) {
      int start = scanner.nextInt();
      int end = scanner.nextInt();
      int weight = scanner.nextInt();
      a[i] = new activity(start, end, weight);
    }
    scanner.close();
    return a;
  }
  //write the output to a file
  private void writeOutputToFile(int ans, String s, String absolutePath) throws IOException {
    String output = s.substring(0, s.length() - 4) + ".output.txt";
    absolutePath = absolutePath.substring(0, absolutePath.length() - s.length());
    absolutePath = absolutePath + output;
    FileWriter fw = new FileWriter(absolutePath);
    fw.write(String.valueOf(ans));
    fw.close();
  }


  public static void main(String[] args) throws IOException {
    File f = new File(args[0]);
    activity[] a = new activitySelection().getData(args[0]);
    activitySelection obj = new activitySelection();
    int ans = obj.activitySel(a);
    System.out.println(ans);
    String s = f.getName();
    String absolutePath = f.getAbsolutePath();
    new activitySelection().writeOutputToFile(ans, s, absolutePath);

  }
}