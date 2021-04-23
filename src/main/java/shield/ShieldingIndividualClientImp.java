
package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private String endpoint;
  private String CHI;
  private Boolean registered = false;
  private String postcode;
  private String cater_name;
  private String cater_postcode;
  private List<MessagingFoodBox> food_Boxes;
  private List<prevOrders> orders = new ArrayList<prevOrders>();
  private MessagingFoodBox picked_Box;

  // Internal field to store information about a food box
  final class MessagingFoodBox {
    List<boxContents> contents;
    String delivered_by;
    String diet;
    String id;
    String name;
  }

  // Internal field to store information about the contents of a food box
  final class boxContents{
    int id;
    String name;
    int quantity;
  }

  // Internal field to store information about a placed order
  final class prevOrders{
    Integer orderId;
    String status;
    MessagingFoodBox foodBox;
    LocalDateTime datePlaced;
  }

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

  /**
   * Sets endpoint for all the following Http requests and initialises food_Boxes so
   * it can be used in subsequent functions such as getDietaryPreferenceForFoodBox.
   *
   * @param endpoint
   * @Exception if http request unsuccessful or
   *            if unmarshal unsuccessful
   */
  public ShieldingIndividualClientImp(String endpoint) {
    // Make sure parameters are not null
    assert(!endpoint.equals(null));

    this.endpoint = endpoint;

    // Construct the endpoint request
    String request_foodBox = "/showFoodBox?orderOption=catering&dietaryPreference=";

    try {
      // Perform request
      String response = ClientIO.doGETRequest(endpoint + request_foodBox);

      // Unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      this.food_Boxes = new Gson().fromJson(response, listType);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This method returns true if the operation occurred correctly (this includes
   * re-registrations) and false if input incorrect (null or CHI number not
   * respecting this format:
   * https://datadictionary.nhs.uk/attributes/community_health_index_number.html)
   * or any of the data retrieved from the server for the shielding individual is
   * null.
   *
   * @param CHI CHI number of the shielding individual
   * @return true if the operation occurred correctly
   * @CustomException if the CHI number if not 10 digits long
   * @Exception if CHI does not consist of all numeric digits or
   *            if CHI does not start with valid birth date or
   *            if http request unsuccessful or
   *            if unmarshal unsuccessful
   */
  @Override
  public boolean registerShieldingIndividual(String CHI) {
    // Make sure parameters are not null
    assert(!CHI.equals(null));

    // Make sure CHI format is correct
    // CHI has ten digits
    if (CHI.length()!=10) {
      try {
        throw new CustomException("CHI must be ten numeric digits long and start with your date of birth");
      } catch (CustomException e) {
        e.printStackTrace();
      }
      return false;
    }
    // CHI has only numeric digits and starts with a date
    try {
      Long.parseLong(CHI);
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
      LocalDateTime individual = LocalDate.parse(CHI.substring(0,6), dtf).atStartOfDay();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    // Construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI;

    // Setup the response recipient
    List<String> responseInfo = new ArrayList<String>();

    try {
      // Perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals("already registered")){
        return true;
      } else {
        // Unmarshal response
        Type listType = new TypeToken<List<String>>() {} .getType();
        responseInfo = new Gson().fromJson(response, listType);

        if (responseInfo.size()==4){
          this.registered = true;
          this.CHI = CHI;
          // Replacing so format of postcode is compatible with getDistance
          this.postcode = responseInfo.get(0).replace(" ", "_");
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Returns collection of food box ids if the operation occurred correctly.
   *
   * @param  dietaryPreference (of individual)
   * @return collection of food box ids with corresponding dietary preference
   * @Exception if http request unsuccessful or
   *            if unmarshal unsuccessful
   */
  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // Make sure parameters are not null
    assert(!dietaryPreference.equals(null));

    // Construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;

    // Setup the response recipient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    List<String> boxIds = new ArrayList<String>();

    try {
      // Perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // Unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // Gather required fields
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
   * @CustomException if individual is not registered or
   *                  no box has been picked or
   *                  order has already been placed that week
   * @Exception if http request unsuccessful
   */
  @Override
  public boolean placeOrder() {
    // Check is individual is registered
    if (registered==false){
      try {
        throw new CustomException("You must first register as a Shielding Individual");
      } catch (CustomException e) {
        e.printStackTrace();
      }
      return false;
    }

    // Check if box has been picked
    if (picked_Box==null){
      try {
        throw new CustomException("You must first pick a box");
      } catch (CustomException e) {
        e.printStackTrace();
      }
      return false;
    }

    // Get today's date
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();

    // Check order already placed this week
    LocalDateTime lastWeek = now.minusWeeks(1);
    for (prevOrders o : orders){
      if (o.datePlaced.compareTo(lastWeek)>=0 && !o.status.equals("cancelled")){
        try {
          throw new CustomException("Order has already been placed this week");
        } catch (CustomException e) {
          e.printStackTrace();
        }
        return false;
      }
    }

    // Construct the endpoint request
    String request = "/placeOrder?individual_id=" + CHI + "&catering_business_name=" + cater_name + "&catering_postcode=" + cater_postcode;

    // Construct data to be passed to post request
    String data = "{\"contents\": [";
    for (boxContents c : picked_Box.contents){
      data += "{\"id\":" + c.id + ",\"name\":\"" + c.name + "\",\"quantity\":" + c.quantity + "},";
    }
    data = data.substring(0, data.length()-1) + "]}";

    prevOrders newOrder = new prevOrders();

    try {
      // Perform request
      String response = ClientIO.doPOSTRequest(endpoint + request, data);

      newOrder.orderId = Integer.parseInt(response);
      newOrder.status = "placed";
      newOrder.foodBox = picked_Box;
      newOrder.datePlaced = now;
      picked_Box = null;
      orders.add(newOrder);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   * @IncorrectFormatException if order has already been packed
   */
  @Override
  public boolean editOrder(int orderNumber) {
    // Make sure parameters are valid
    assert(orderNumber>0);

    requestOrderStatus(orderNumber);

    for (prevOrders o : orders) {
      if (o.orderId == orderNumber) {
        // Check if order has already been packed
        if (!o.status.equals("placed")) {
          try {
            throw new CustomException("Order can no longer be amended");
          } catch (CustomException e) {
            e.printStackTrace();
          }
          return false;

        } else{

          // Construct the endpoint request
          String request = "/editOrder?order_id=" + orderNumber;

          // Construct data to be passed to post request
          String data = "{\"contents\": [";
          for (boxContents c : o.foodBox.contents) {
            data += "{\"id\":" + c.id + ",\"name\":\"" + c.name + "\",\"quantity\":" + c.quantity + "},";
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
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   * @CustomException if order has already been dispatched/can no longer be cancelled
   */
  @Override
  public boolean cancelOrder(int orderNumber) {
    // Make sure parameters are valid
    assert(orderNumber>0);

    requestOrderStatus(orderNumber);

    for (prevOrders o : orders) {
      if (o.orderId == orderNumber) {
        // Check if order is not dispatched
        if (o.status.equals("placed") || o.status.equals("packed")) {

          // Construct the endpoint request
          String request = "/cancelOrder?order_id=" + orderNumber;

          try {
            // Perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            if (response.equals("True")){
              return true;
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

        } else{

          try {
            throw new CustomException("Order can no longer be cancelled");
          } catch (CustomException e) {
            e.printStackTrace();
          }
          return false;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the operation occurred correctly.
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   * @CustomException if order number not found
   * @Exception if http request unsuccessful
   */
  @Override
  public boolean requestOrderStatus(int orderNumber) {
    // Make sure parameters are valid
    assert(orderNumber>0);

    // Construct the endpoint request
    String request = "/requestStatus?order_id=" + orderNumber;

    try {
      // Perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      for (prevOrders o : orders){
        if (o.orderId == orderNumber){
          switch(Integer.parseInt(response)) {
            case 0:
              o.status = "placed";
              break;
            case 1:
              o.status = "packed";
              break;
            case 2:
              o.status = "dispatched";
              break;
            case 3:
              o.status = "delivered";
              break;
            case 4:
              o.status = "cancelled";
              break;
            case -1:
              o.status = "not found";
              try {
                throw new CustomException("Order number was not found");
              } catch (CustomException e) {
                e.printStackTrace();
              }
              return false;
          }
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      throw new CustomException("Order number was not found");
    } catch (CustomException e) {
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
   * @Exception if http request unsuccessful or
   *            if unmarshal unsuccessful
   */
  @Override
  public Collection<String> getCateringCompanies() {
    // Construct the endpoint request
    String request = "/getCaterers";

    // Setup the response recipient
    List<String> responseCaterers = new ArrayList<String>();

    try {
      // Perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // Unmarshal response
      Type listType = new TypeToken<List<String>>() {} .getType();
      responseCaterers = new Gson().fromJson(response, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseCaterers;
  }

  /**
   * Returns the distance between two locations based on their post codes where postcodes
   * must start with EH and be separated by an underscore, e.g, EH11_2DR. If the postcodes
   * are incorrectly formatted, returns 0.
   *
   * @param postCode1 post code of one location
   * @param postCode2 post code of another location
   * @return the distance as a float between the two locations
   * @CustomException if the postcode format is incorrect
   * @Exception if http request unsuccessful
   */
  @Override
  public float getDistance(String postCode1, String postCode2) {
    // Make sure parameters are not null
    assert(!postCode1.equals(null) && !postCode2.equals(null));

    // Make sure postCode format is correct
    if (!postCode1.startsWith("EH") || !postCode1.contains("_") || !postCode2.startsWith("EH") || !postCode2.contains("_")) {
      try {
        throw new CustomException("Postcodes must start with EH and be separated by an underscore");
      } catch (CustomException e) {
        e.printStackTrace();
      }
      return 0;
    }

    // Construct the endpoint request
    String request = "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2;

    try {
      // Perform request
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

  // Helper function added in ShieldingIndividualClient
  @Override
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
    return food_Boxes.size();
  }

  /**
   * Returns the dietary preference that this specific food box satisfies
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return dietary preference
   */
  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    // Make sure parameters are valid
    assert(foodBoxId>0 && foodBoxId<=getFoodBoxNumber());

    MessagingFoodBox box = food_Boxes.get(foodBoxId-1);
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
    // Make sure parameters are valid
    assert(foodBoxId>0 && foodBoxId<=getFoodBoxNumber());

    MessagingFoodBox box = food_Boxes.get(foodBoxId-1);
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
    // Make sure parameters are valid
    assert(foodBoxId>0 && foodBoxId<=getFoodBoxNumber());

    MessagingFoodBox box = food_Boxes.get(foodBoxId-1);

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
   * @return the requested item name or null if item not in food box
   */
  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    // Make sure parameters are valid
    assert(itemId>0 && foodBoxId>0 && foodBoxId<=getFoodBoxNumber());

    MessagingFoodBox box = food_Boxes.get(foodBoxId-1);
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
   * @return the requested item quantity or 0 if item not in foodbox
   */
  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    // Make sure parameters are valid
    assert(itemId>0 && foodBoxId>0 && foodBoxId<=getFoodBoxNumber());

    MessagingFoodBox box = food_Boxes.get(foodBoxId-1);
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
    // Make sure parameters are valid
    assert(foodBoxId>0 && foodBoxId<=getFoodBoxNumber());

    this.picked_Box = food_Boxes.get(foodBoxId-1);
    return true;
  }

  /**
   * Returns true if the item quantity for the picked foodbox was changed to a valid
   * quantity (quantity can only be decreased).
   *
   * @param  itemId the food box id as last returned from the server
   * @param  quantity the food box item quantity to be set
   * @return true if the item quantity for the picked foodbox was changed
   * @CustomException if no box has been picked yet or
   *                  if item was not in the box or
   *                  if quantity was invalid
   */
  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    // Make sure parameters are valid
    assert(itemId>0 && quantity>=0);

    // Check if box has been picked
    if (picked_Box==null){
      try {
        throw new CustomException("You must first pick a box");
      } catch (CustomException e) {
        e.printStackTrace();
      }
      return false;
    }

    // Check if Item is in box
    if (!getItemIdsForFoodBox(Integer.parseInt(picked_Box.id)).contains(itemId)){
      try {
        throw new CustomException("Item is not in box");
      } catch (CustomException e) {
        e.printStackTrace();
      }
      return false;
    }

    for (boxContents c : picked_Box.contents){
      if (c.id == itemId){

        // Check if quantity is being decreased
        if (quantity >= c.quantity){
          try {
            throw new CustomException("Can only decrease quantity");
          } catch (CustomException e) {
            e.printStackTrace();
          }
          return false;
        }

        c.quantity = quantity;
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
    Collection<Integer> orderIds = new ArrayList<Integer>();

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
   * @return status of the order for the requested number or null if orderNumber is invalid
   */
  @Override
  public String getStatusForOrder(int orderNumber) {
    // Make sure parameters are valid
    assert(orderNumber>0);

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
    // Make sure parameters are valid
    assert(orderNumber>0);

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
   * @return name of the item for the requested order or null if orderNumber is invalid
   *         or null if item is not in box
   */
  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    // Make sure parameters are valid
    assert(itemId>0 && orderNumber>0);

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
   * @return quantity of the item for the requested order or 0 if orderNumber is invalid
   *    *         or 0 if item is not in box
   */
  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    // Make sure parameters are valid
    assert(itemId>0 && orderNumber>0);

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
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @param  quantity the food box item quantity to be set
   * @return true if quantity of the item for the requested order was changed
   * @IncorrectFormatException if order has already been packed or
   *                           if item is not in the box or
   *                           if quantity was not decreased
   */
  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    // Make sure parameters are valid
    assert(itemId>0 && orderNumber>0 && quantity>=0);

    requestOrderStatus(orderNumber);

    for (prevOrders o : orders){
      if (o.orderId == orderNumber){

        // Check if order has already been packed
        if (!o.status.equals("placed")){
          try {
            throw new CustomException("Order can no longer be amended");
          } catch (CustomException e) {
            e.printStackTrace();
          }
          return false;

        } else {

          // Check if item is in box
          if (!getItemIdsForFoodBox(Integer.parseInt(o.foodBox.id)).contains(itemId)){
            try {
              throw new CustomException("Item is not in box");
            } catch (CustomException e) {
              e.printStackTrace();
            }
            return false;

          } else {

            for (boxContents c : o.foodBox.contents){
              if (c.id == itemId){

                // Check if quantity is being decreased
                if (quantity >= c.quantity){
                  try {
                    throw new CustomException("Can only decrease quantity");
                  } catch (CustomException e) {
                    e.printStackTrace();
                  }
                  return false;
                }

                c.quantity = quantity;
              }
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Returns closest catering company serving orders based on Shielding Individuals
   * postcode.
   *
   * @return business name of catering company
   */
  @Override
  public String getClosestCateringCompany() {
    Collection<String> caterers = getCateringCompanies();
    float minDist = -1;

    for (String c: caterers){
      String[] caterInfo = c.split(",");
      float distance = getDistance(getPostcode(), caterInfo[2]);

      if(distance < minDist || minDist < 0){
        minDist = distance;
        this.cater_name = caterInfo[1];
        this.cater_postcode = caterInfo[2];
      }
    }
    return cater_name;
  }

}
