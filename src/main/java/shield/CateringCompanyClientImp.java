/**
 *
 */

package shield;

public class CateringCompanyClientImp implements CateringCompanyClient {

  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  public CateringCompanyClientImp(String endpoint) { this.endpoint = endpoint; }

  /** TO DO
   * if equals to integer then true
   * make sure postcode is valid
   * can a catering company be registered twice but with different addresses?
   */
  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;
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

  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;
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
