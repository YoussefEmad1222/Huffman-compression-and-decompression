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
  int recur(activity[] a, int i, int j, int[] dp) {
    if (i > j)
      return 0;
    if (dp[i] != -1)
      return dp[i];
    if (i == j) {
      dp[i] = a[i].weight;
      return a[i].weight;
    }

    int ans = recur(a, i + 1, j, dp);
    for (int k = i + 1 ; k <= j; k++) {
      if (a[k].start >= a[i].end) {
        ans = Math.max(ans, a[i].weight + recur(a, k, j, dp));
      }
    }
    return dp[i] = ans;
  }

  int activitySel(activity[] a) {
    int n = a.length;
    Arrays.sort(a, new Comparator<activity>() {
      public int compare(activity a1, activity a2) { return a1.end - a2.end; }
    });
    int[] dp = new int[n];
    Arrays.fill(dp, -1);
    int ans = recur(a, 0, n - 1, dp);
    for (int i = n-1; i >= 0; i--) {
      System.out.println(dp[i]);
    }
    return ans;
  }

  public static void main(String[] args) throws IOException {
    File f = new File(args[0]);
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
    activitySelection obj = new activitySelection();
    int ans = obj.activitySel(a);
    System.out.println(ans);
    String s = f.getName();
    String absolutePath = f.getAbsolutePath();
    String output = s.substring(0, s.length() - 4) + ".output.txt";
    absolutePath = absolutePath.substring(0, absolutePath.length() - s.length());
    absolutePath = absolutePath + output;
    FileWriter fw = new FileWriter(absolutePath);
    fw.write(String.valueOf(ans));
    fw.close();
  }
}