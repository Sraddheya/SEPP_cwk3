/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private String endpoint;
  private String CHI;
  private Boolean registered = false;
  private String postcode;
  private String name;
  private String surname;
  private String number;
  private String cater_name;
  private String cater_postcode;
  private List<MessagingFoodBox> allFoodBoxes;
  private List<prevOrders> orders = new ArrayList<prevOrders>();
  private MessagingFoodBox pickedBox;

  // internal field only used for transmission purposes
  final class MessagingFoodBox {
    List<boxContents> contents;
    String delivered_by;
    String diet;
    String id;
    String name;
  }

  final class boxContents{
    int id;
    String name;
    int quantity;
  }

  final class prevOrders{
    Integer orderId;
    String status;
    MessagingFoodBox foodBox;
  }

  /**
   * Sets endpoint for all the following HTTP requests and initialises allFoodBoxes
   *
   * @param endpoint
   */
  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;

    // construct the endpoint request
    String request_foodBox = "/showFoodBox?orderOption=catering&dietaryPreference=";

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request_foodBox);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      this.allFoodBoxes = new Gson().fromJson(response, listType);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns true if the operation occurred correctly (i,e. client is newly
   * registered or already registered) and false if input incorrect (null or CHI
   * number not respecting this format:
   * https://datadictionary.nhs.uk/attributes/community_health_index_number.html)
   * or any of the data retrieved from the server for the shielding individual is
   * null.
   *
   * @param CHI CHI number of the shielding individual
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean registerShieldingIndividual(String CHI) {
    //REGISTERING INDIVIDUAL
    // construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI;

    // setup the response recipient
    List<String> responseInfo = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals("already registered")){
        this.registered = true;
        this.CHI = CHI;
        return true;
      } else {
        // unmarshal response
        Type listType = new TypeToken<List<String>>() {} .getType();
        responseInfo = new Gson().fromJson(response, listType);

        if (responseInfo.size()==4){
          this.registered = true;
          this.CHI = CHI;
          this.postcode = responseInfo.get(0).replace(" ", "_");
          this.name = responseInfo.get(1);
          this.surname = responseInfo.get(2);
          this.number = responseInfo.get(3);
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Returns collection of food box ids if the operation occurred correctly
   *
   * @param  dietaryPreference (of individual)
   * @return collection of food box ids withe corresponding dietary preference
   */
  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;

    // setup the response recipient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    List<String> boxIds = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        boxIds.add(b.id);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return boxIds;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean placeOrder() {
    // construct the endpoint request
    String request = "/placeOrder?individual_id=" + CHI + "&catering_business_name=" + cater_name + "&catering_postcode=" + cater_postcode;

    // construct data to be passed to post request
    String data = "{\"contents\": [";
    for (boxContents c : pickedBox.contents){
      data += "{\"id\":" + c.id + ",\"name\":\"" + c.name + "\",\"quantity\":" + c.quantity + "},";
    }
    data = data.substring(0, data.length()-1) + "]}";

    prevOrders newOrder = new prevOrders();

    try {
      // perform request
      String response = ClientIO.doPOSTRequest(endpoint + request, data);
      newOrder.orderId = Integer.parseInt(response);
      newOrder.status = "placed";
      newOrder.foodBox = pickedBox;
      pickedBox = null;
      this.orders.add(newOrder);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public boolean editOrder(int orderNumber) {
    // construct the endpoint request
    String request = "/editOrder?order_id=" + orderNumber;

    // construct data to be passed to post request
    String data = "{\"contents\": [";
    for (prevOrders o : orders){
      if (o.orderId == orderNumber){
        for (boxContents c : pickedBox.contents){
          data += "{\"id\":" + c.id + ",\"name\":\"" + c.name + "\",\"quantity\":" + c.quantity + "},";
        }
      }
    }
    data = data.substring(0, data.length()-1) + "]}";

    try {
      // perform request
      String response = ClientIO.doPOSTRequest(endpoint + request, data);

      if (response.equals("True")){
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    // construct the endpoint request
    String request = "/cancelOrder?order_id=" + orderNumber;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals("True")){
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    // construct the endpoint request
    String request = "/requestStatus?order_id=" + orderNumber;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      for (prevOrders o : orders){
        if (o.orderId == orderNumber){
          //switch
        }
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Returns collection of catering companies and their locations in the format
   * [positionOfCaterer1,nameOfCaterer1,postcodeOfCaterer1,
   *  positionOfCaterer2,nameOfCaterer2,postcodeOfCaterer2]
   *
   * @return collection of catering companies and their locations
   */
  @Override
  public Collection<String> getCateringCompanies() {
    // construct the endpoint request
    String request = "/getCaterers";

    // setup the response recipient
    List<String> responseCaterers = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<String>>() {} .getType();
      responseCaterers = new Gson().fromJson(response, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseCaterers;
  }

  /**
   * Returns the distance between two locations based on their post codes where postcodes
   * must start with EH and be separated by an underscore, e.g, EH11_2DR
   *
   * @param postCode1 post code of one location
   * @param postCode2 post code of another location
   * @return the distance as a float between the two locations
   */
  @Override
  public float getDistance(String postCode1, String postCode2) {
    // construct the endpoint request
    String request = "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      return Float.parseFloat(response);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public boolean isRegistered() {
    return registered;
  }

  @Override
  public String getCHI() {
    return CHI;
  }

  // Helper Function added in ShieldingIndividualClient
  public String getPostcode() {
    return postcode;
  }

  /**
   * Returns the number of available food boxes after quering the server
   *
   * @return number of available food boxes after quering the server
   */
  @Override
  public int getFoodBoxNumber() {
    return allFoodBoxes.size();
  }

  /**
   * Returns the dietary preference that this specific food box satisfies
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return dietary preference
   */
  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    MessagingFoodBox box = allFoodBoxes.get(foodBoxId-1);
    return box.diet;
  }

  /**
   * Returns the number of items in this specific food box (not the quantity
   * of each item). For example if a box has:
   *  - 3 bananas
   *  - 5 bottles of milk
   * it should return 2.
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return number of items in the food box
   */
  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    MessagingFoodBox box = allFoodBoxes.get(foodBoxId-1);
    return box.contents.size();
  }

  /**
   * Returns the collection of item ids of the requested foodbox
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return collection of item ids of the requested foodbox
   */
  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodBoxId) {
    MessagingFoodBox box = allFoodBoxes.get(foodBoxId-1);

    List<Integer> itemIDs = new ArrayList<Integer>();

    for (boxContents c: box.contents){
      itemIDs.add(c.id);
    }
    return itemIDs;
  }

  /**
   * Returns the item name of the item in the requested foodbox
   *
   * @param  itemId the food box id as last returned from the server
   * @param  foodBoxId the food box id as last returned from the server
   * @return the requested item name
   */
  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    MessagingFoodBox box = allFoodBoxes.get(foodBoxId-1);
    for (boxContents c: box.contents){
      if (c.id == itemId){
        return c.name;
      }
    }
    return null;
  }

  /**
   * Returns the item quantity of the item in the requested foodbox
   *
   * @param  itemId the food box id as last returned from the server
   * @param  foodBoxId the food box id as last returned from the server
   * @return the requested item quantity
   */
  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    MessagingFoodBox box = allFoodBoxes.get(foodBoxId-1);
    for (boxContents c: box.contents){
      if (c.id == itemId){
        return c.quantity;
      }
    }
    return 0;
  }

  /**
   * Returns true if the requested foodbox was picked and foodboxId passed was valid.
   *
   * This method marks internally in the client a specific food box (using its Id)
   * that is to be used for placing an order via the placeOrder() method.
   * While this box is marked, but not yet placed, the
   * changeItemQuantityForPickedFoodBox() can be used to change quantities.
   * Once an order is successfully placed via PlaceOrder(), the marked box
   * should be cleared.
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return true if the requested foodbox was picked
   */
  @Override
  public boolean pickFoodBox(int foodBoxId) {
    this.pickedBox = allFoodBoxes.get(foodBoxId-1);
    return true;
  }

  /**
   * Returns true if the item quantity for the picked foodbox was changed to a valid
   * quantity (quantity can only be decreased).
   *
   * @param  itemId the food box id as last returned from the server
   * @param  quantity the food box item quantity to be set
   * @return true if the item quantity for the picked foodbox was changed
   */
  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    for (boxContents c : pickedBox.contents){
      if (c.id == itemId){
        System.out.println(c.quantity);
        c.quantity = quantity;
        System.out.println(c.quantity);
      }
    }
    return true;
  }

  /**
   * Returns the collection of the order numbers placed.
   *
   * @return collection of the order numbers placed
   */
  @Override
  public Collection<Integer> getOrderNumbers() {
    List<Integer> orderIds = new ArrayList<Integer>();

    for (prevOrders o : orders){
      orderIds.add(o.orderId);
    }
    return orderIds;
  }

  /**
   * Returns the status of the order for the requested number as stored locally by
   * the client.
   *
   * @param orderNumber the order number
   * @return status of the order for the requested number
   */
  @Override
  public String getStatusForOrder(int orderNumber) {
    for (prevOrders o : orders){
      if (o.orderId == orderNumber){
        return o.status;
      }
    }
    return null;
  }

  /**
   * Returns the item ids for the items of the requested order as stored locally by
   * the client.
   *
   * @param  orderNumber the order number
   * @return item ids for the items of the requested order
   */
  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    List<Integer> itemIDs = new ArrayList<Integer>();

    for (prevOrders o : orders){
      if (o.orderId == orderNumber){
        for (boxContents c: o.foodBox.contents){
          itemIDs.add(c.id);
        }
      }
    }
    return itemIDs;
  }

  /**
   * Returns the name of the item for the requested order as stored locally by
   * the client.
   *
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @return name of the item for the requested order
   */
  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    for (prevOrders o : orders){
      if (o.orderId == orderNumber){
        for (boxContents c: o.foodBox.contents){
          if (c.id == itemId){
            return c.name;
          }
        }
      }
    }
    return null;
  }

  /**
   * Returns the quantity of the item for the requested order as stored locally by
   * the client.
   *
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @return quantity of the item for the requested order
   */
  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    for (prevOrders o : orders){
      if (o.orderId == orderNumber){
        for (boxContents c: o.foodBox.contents){
          if (c.id == itemId){
            return c.quantity;
          }
        }
      }
    }
    return 0;
  }

  /**
   * Returns true if quantity of the item for the requested order was changed.
   *
   * This method changes the quantities for a placed order as stored locally
   * by the client.
   * In order to sync with the server, one needs to call the editOrder()
   * method separately.
   *
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @param  quantity the food box item quantity to be set
   * @return true if quantity of the item for the requested order was changed
   */
  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    for (prevOrders o : orders){
      if (o.orderId == orderNumber && o.status.equals("placed")){
        for (boxContents c : pickedBox.contents){
          if (c.id == itemId){
            System.out.println(c.quantity);
            c.quantity = quantity;
            System.out.println(c.quantity);
          }
        }
      }
    }
    return false;
  }

  /**
   * Returns closest catering company serving orders based on our location
   *
   * @return business name of catering company
   */
  @Override
  public String getClosestCateringCompany() {
    Collection<String> caterers = getCateringCompanies();
    float minDist = -1;

    for (String c: caterers){
      String[] caterInfo = c.split(",");
      float distance = getDistance(postcode, caterInfo[2]);

      if(distance < minDist || minDist < 0){
        this.cater_name = caterInfo[1];
        this.cater_postcode = caterInfo[2];
      }
    }
    return cater_name;
  }

}
