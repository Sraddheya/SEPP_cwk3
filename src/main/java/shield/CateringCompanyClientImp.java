
package shield;

public class CateringCompanyClientImp implements CateringCompanyClient {

  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  public class IncorrectFormatException extends Exception {

    public IncorrectFormatException(String message) {
      super(message);
    }
  }

  public CateringCompanyClientImp(String endpoint) { this.endpoint = endpoint; }

  /**
   * Returns true if the operation occurred correctly (catering company is
   * registered or already registered).
   *
   * @param name name of the business
   * @param postCode post code of the business
   * @return true if the operation occurred correctly
   * @Exception if the format of the postcode is incorrect
   */
  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    // Make sure parameters are not null
    assert(!name.equals(null) && !postCode.equals(null));

    // Make sure postCode format is correct
    if (!postCode.startsWith("EH") && !postCode.contains("_")) {
      try {
        throw new IncorrectFormatException("Postcodes must start with EH and be separated by an underscore");
      } catch (IncorrectFormatException e) {
        e.printStackTrace();
      }
      return false;
    }

    // construct the endpoint request
    String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;

    try {
      // perform request
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
   * Returns true if the operation occurred correctly
   *
   * @param orderNumber the order number
   * @param status status of the order for the requested number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    // Make sure parameters are not null
    assert(orderNumber>=0 && !status.equals(null));

    // construct the endpoint request
    String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

    try {
      // perform request
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
