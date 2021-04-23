
package shield;

public class CateringCompanyClientImp implements CateringCompanyClient {

  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  public class CustomException extends Exception {

    /**
     * Custom exception to help identify the exact cause through the error message.
     *
     * @param message the message thrown that informs the user of the exact issue
     */
    public CustomException(String message) {
      super(message);
    }
  }

  public CateringCompanyClientImp(String endpoint) { this.endpoint = endpoint; }

  /**
   * Returns true if the operation occurred correctly (catering company is registered
   * or already registered).
   *
   * We decided to throw an exception if the postcode does not start with EH and is not
   * separated by an underscore, e.g, EH11_2DR, so that the post code is compatible with
   * the rest of the system (to be precise the method getDistance in the
   * ShieldingIndividualClientImp class).
   *
   * @param name name of the business
   * @param postCode post code of the business
   * @return true if the operation occurred correctly
   * @CustomException if the format of the postcode is incorrect
   * @Exception if http request unsuccessful
   */
  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    // Make sure parameters are not null
    assert(!name.equals(null) && !postCode.equals(null));

    // Make sure postCode format is correct
    if (!postCode.startsWith("EH") || !postCode.contains("_")) {
      try {
        throw new CustomException("Postcodes must start with EH and be separated by an underscore");
      } catch (CustomException e) {
        e.printStackTrace();
      }
      return false;
    }

    // Construct the endpoint request
    String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;

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
   * Returns true if the operation occurred correctly
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
    String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

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
