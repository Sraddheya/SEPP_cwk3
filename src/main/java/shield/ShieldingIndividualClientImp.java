/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private String endpoint;
  private String CHI;
  private Boolean registered = false;

  // internal field only used for transmission purposes
  final class MessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    transient List<String> contents;

    String delivered_by;
    String diet;
    String id;
    String name;
  }

  public ShieldingIndividualClientImp(String endpoint) { this.endpoint = endpoint; }

  @Override
  public boolean registerShieldingIndividual(String CHI) {
    this.CHI = CHI;
    // construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("registered new") || response.equals("already registered")){
        this.registered = true;
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
    List<String> responseCaterer = new ArrayList<String>();

    List<String> caterer = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      Type listType = new TypeToken<List<String>>() {} .getType();
      responseCaterer = new Gson().fromJson(response, listType);

      for (String c : responseCaterer) {
        caterer.add(c);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return caterer;
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
    return 0;
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
    return null;
  }
}
