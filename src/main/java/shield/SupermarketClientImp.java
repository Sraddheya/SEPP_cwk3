/**
 *
 */

package shield;

public class SupermarketClientImp implements SupermarketClient {

  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  public SupermarketClientImp(String endpoint) { this.endpoint = endpoint; }

  /** TO DO
   * endpoints not working for now so leave as is
   */
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

  // **UPDATE2** ADDED METHOD
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    String request = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber + "&supermarket_business_name=" + getName() + "&supermarket_postcode=" + getPostCode();
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("True")) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  // **UPDATE**
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    String request = "/updateSupermarketOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("True")) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean isRegistered() { return registered; }

  @Override
  public String getName() { return name; }

  @Override
  public String getPostCode() { return postcode; }
}
