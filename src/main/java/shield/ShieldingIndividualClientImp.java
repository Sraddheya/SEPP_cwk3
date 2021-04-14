/**
 * To implement
 */

package shield;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
  private List<MessagingFoodBox> foodBoxes;

  // internal field only used for transmission purposes
  final class MessagingFoodBox {
    List<boxContents> contents;
    String delivered_by;
    String diet;
    String id;
    String name;
  }

  final class boxContents{
    String id;
    String name;
    String quantity;
  }

  public ShieldingIndividualClientImp(String endpoint) { this.endpoint = endpoint; }

  @Override
  public boolean registerShieldingIndividual(String CHI) {
    // construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response!=null){
        String[] result = response.split(",");
        //List<String> result = new ArrayList<String>(Arrays.asList(response.split(",")));
        this.registered = true;
        this.CHI = CHI;
        this.postcode = result[0];
        this.name = result[1];
        this.surname = result[2];
        this.number = result[3];
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

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

  // **UPDATE2** REMOVED PARAMETER
  @Override
  public boolean placeOrder() {
    // construct the endpoint request
    String request = "/placeOrder?individual_id=1234&catering_business_name=catering1&catering_postcode=eh0111";
    String data = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":200},{\"id\":2,\"name\":\"tomatoes\",\"quantity\":2}]}";

    try {
      // perform request
      String response = ClientIO.doPOSTRequest(endpoint + request, data);
      System.out.println("response: " + response);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public boolean editOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    return false;
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    // construct the endpoint request
    String request = "/getCaterers";

    // setup the response recipient
    List<String> responseCaterers = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      Type listType = new TypeToken<List<String>>() {} .getType();
      responseCaterers = new Gson().fromJson(response, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseCaterers;
  }

  // **UPDATE**
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

  @Override
  public int getFoodBoxNumber() {
    return foodBoxes.size();
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    return null;
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    return 0;
  }

  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
    return null;
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    return null;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    return 0;
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    return false;
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    return false;
  }

  @Override
  public Collection<Integer> getOrderNumbers() {
    return null;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {
    return null;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    return null;
  }

  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    return 0;
  }

  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    return false;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

  // **UPDATE**
  @Override
  public String getClosestCateringCompany() {
    List<String> cateringCompanies = (List<String>) getCateringCompanies();
    float minDist = -1;
    for (String cater: cateringCompanies){
      String[] info = cater.split(",");
      if ((getDistance(postcode, info[2]) < minDist) || (minDist < 0)){
        this.cater_name = info[1];
        this.cater_postcode = info[2];
        minDist = getDistance(postcode, cater_postcode);
      }
    }
    return cater_name;
  }
}
