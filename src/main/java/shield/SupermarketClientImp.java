/**
 *
 */

package shield;

// Added imports for reading and writing
import java.io.*;
import java.util.Arrays;

public class SupermarketClientImp implements SupermarketClient {

  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  public SupermarketClientImp(String endpoint) { this.endpoint = endpoint; }

  @Override
  public boolean registerSupermarket(String name, String postCode) {
    String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("registered new") || response.equals("already registered")) {
        this.registered = true;
        this.name = name;
        this.postcode = postCode;
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  //Helper function for updateOrderStatus
  public static String getSubarray(String[] arr, int beg, int end) {
    String returned = "";
    for (int i = beg; i < end; i++) {
      returned = returned + arr[i] + ",";
    }
    returned = returned + arr[end];
    return returned;
  }

  public static String getStatusCode(String status) {
    int code = 0;
    if (status.equals("packed")) {
      code = 1;
    } else if (status.equals("dispatched")) {
      code = 2;
    } else if (status.equals("delivered")) {
      code = 3;
    }
    return Integer.toString(code);
  }

  // **UPDATE2** ADDED METHOD
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    return false;
  }

  // **UPDATE**
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    String path = "C:\\Users\\Phoebe\\flask_SEPP_cwk3\\orders.csv";
    String tempPath = "C:\\Users\\Phoebe\\flask_SEPP_cwk3\\temp.csv";
    File oldFile = new File (path);
    File newFile = new File(tempPath);
    boolean flag = false;
    try {
      FileWriter fw = new FileWriter(tempPath,true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter pw = new PrintWriter(bw);
      FileReader fr = new FileReader (path);
      BufferedReader br = new BufferedReader(fr);
      //keeping header line as it is
      String line = br.readLine();
      pw.println(line);
      //reading rest of the lines
      line = br.readLine();
      while (line != null) {
        String[] splitLine = line.split(",");
        if (Integer.parseInt(splitLine[0]) == orderNumber) {
          String keep = getSubarray(splitLine, 0, 19);
          pw.println(keep + "," + getStatusCode(status));
          flag = true;
        } else {
          pw.println(getSubarray(splitLine, 0, 20));
        }
        line = br.readLine();
      }
      br.close();
      pw.flush();
      pw.close();
      oldFile.delete();
      File dump = new File(path);
      newFile.renameTo(dump);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  @Override
  public boolean isRegistered() { return registered; }

  @Override
  public String getName() { return name; }

  @Override
  public String getPostCode() { return postcode; }
}
