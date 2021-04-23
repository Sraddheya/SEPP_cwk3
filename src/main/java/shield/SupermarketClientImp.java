
package shield;

public class SupermarketClientImp implements SupermarketClient {

  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  public SupermarketClientImp(String endpoint) { this.endpoint = endpoint; }

  /**
   * Returns true if the operation occurred correctly (Supermarket is
   * registered or already registered).
   *
   * @param name name of the business
   * @param postCode post code of the business
   * @return true if the operation occurred correctly
   * @Exception if http request unsuccessful
   */
  @Override
  public boolean registerSupermarket(String name, String postCode) {
    // Make sure parameters are not null
    assert(!name.equals(null) && !postCode.equals(null));

    // Construct the endpoint request
    String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;

    try {
      // Perform request
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

  /**
   * Returns true if the operation occurred correctly. Assuming that the the Supermarket
   * UI has already made sure that a Shielding individual can only place one order a week.
   *
   * @param CHI CHI number of the shielding individual associated with this order
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   * @Exception if http request unsuccessful
   */
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    // Make sure parameters are not null
    assert(!CHI.equals(null) && orderNumber>=0);

    // Construct the endpoint request
    String request = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber + "&supermarket_business_name=" + name + "&supermarket_postcode=" + postcode;

    try {
      // Perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals("True")) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Returns true if the operation occurred correctly.
   *
   * @param orderNumber the order number
   * @param status status of the order for the requested number
   * @return true if the operation occurred correctly
   * @Exception if http request unsuccessful
   */
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    // Make sure parameters are not null
    assert(orderNumber>=0 && !status.equals(null));

    // Construct the endpoint request
    String request = "/updateSupermarketOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

    try {
      // Perform request
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
