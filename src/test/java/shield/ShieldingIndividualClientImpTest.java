/**
 *
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClient client;
  private String cater_name;
  private String cater_postCode;

  private Properties loadProperties(String propsFilename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties props = new Properties();

    try {
      InputStream propsStream = loader.getResourceAsStream(propsFilename);
      props.load(propsStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return props;
  }

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);

    client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));

    // Registering new Catering company
    Random rand = new Random();
    cater_name = String.valueOf(rand.nextInt(10000));
    String temp1 = String.valueOf(rand.nextInt(10));
    String temp2 = Character.toString( (char) (rand.nextInt(26) + 65));
    String temp3 = Character.toString( (char) (rand.nextInt(26) + 65));

    cater_postCode = "EH" + temp1 + "_" + temp1 + temp2 + temp3;
    String request = "/registerCateringCompany?business_name=" + cater_name + "&postcode=" + cater_postCode;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * System tests
   */
  @Test
  public void testShieldingIndividualNewRegistration() {
    // Create CHI
    Random rand = new Random();
    String temp = String.valueOf(rand.nextInt(10000));
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    String CHI = date + temp;

    // Null parameters should be asserted
    //client.registerShieldingIndividual(null);

    // CHI not 10 digits should give exception
    assertFalse(client.registerShieldingIndividual(date));
    // CHI not all numeric digits should give exception
    assertFalse(client.registerShieldingIndividual(date + "abcd"));
    // Not valid date should give exception
    assertFalse(client.registerShieldingIndividual("000000" + temp));

    //Test if new shielding individual registered
    assertTrue(client.registerShieldingIndividual(CHI));
    assertTrue(client.isRegistered());
    assertEquals(client.getCHI(), CHI);
    //Test if shielding individual already registered
    assertTrue(client.registerShieldingIndividual(CHI));
    assertTrue(client.isRegistered());
    assertEquals(client.getCHI(), CHI);
  }

  @Test
  public void testPlaceOrder() {
    // Create CHI
    Random rand = new Random();
    String temp = String.valueOf(rand.nextInt(10000));
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    String CHI = date + temp;

    // Individual not registered yet should give exception
    assertFalse(client.placeOrder());

    assertTrue(client.registerShieldingIndividual(CHI));
    client.getClosestCateringCompany();

    // Box not picked yet should give exception
    assertFalse(client.placeOrder());

    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());

    // Order already placed this week should give exception
    assertTrue(client.pickFoodBox(1));
    assertFalse(client.placeOrder());
  }

  @Test
  public void testRandom(){
    /**DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String nextWeek = dtf.format(now.plusWeeks(1));
    String today = dtf.format(now);
    System.out.println(nextWeek + " " + today);

    LocalDateTime date_nextWeek = LocalDate.parse(nextWeek, dtf).atStartOfDay();
    LocalDateTime date_today = LocalDate.parse(today, dtf).atStartOfDay();
    System.out.println(date_nextWeek + " " + date_today);

    assertTrue(date_nextWeek.compareTo(date_today)>=0);
    assertTrue(date_today.compareTo(date_today)>=0);*/
  }

  /**
   * Unit tests
   */
  @Test
  public void testShowFoodBoxes() {
    // Null parameters should be asserted
    //client.showFoodBoxes(null);

    // Valid dietary preferences
    assertEquals(client.showFoodBoxes("none").size(), 3);
    assertEquals(client.showFoodBoxes("pollotarian").size(), 1);
    assertEquals(client.showFoodBoxes("vegan").size(), 1);
    // Invalid dietary preference should return an empty list
    assertEquals(client.showFoodBoxes("Kosher").size(), 0);
  }

  @Test
  public void testGetCateringCompanies() {
    System.out.println(client.getCateringCompanies());
  }

  @Test
  public void testGetDistance() {
    // Create postcodes
    Random rand = new Random();
    String postCode_part11 = String.valueOf(rand.nextInt(10));
    String postCode_part12 = String.valueOf(rand.nextInt(1000));
    String postCode1 = "EH" + postCode_part11 + "_" + postCode_part12;
    String postCode_part21 = String.valueOf(rand.nextInt(10));
    String postCode_part22 = String.valueOf(rand.nextInt(1000));
    String postCode2 = "EH" + postCode_part21 + "_" + postCode_part22;
    String response;

    // Null parameters should be asserted
    //client.getDistance(null, null);
    //client.getDistance(postCode1, null);
    //client.getDistance(null, postCode2);

    // Incorrect postCode format should give exception
    assertEquals(client.getDistance(postCode_part12, postCode_part22), 0);
    assertEquals(client.getDistance(postCode_part11 + "_" + postCode_part12, postCode_part21 + "_" + postCode_part22), 0);
    assertEquals(client.getDistance("EH" + postCode_part12, "EH" + postCode_part22), 0);

    try {
      response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2);
      assertEquals(client.getDistance(postCode1, postCode2), Float.parseFloat(response));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetFoodBoxNumber(){
    assertEquals(client.getFoodBoxNumber(), 5);
  }

  @Test
  public void testGetDietaryPreferenceForFoodBox(){
    // Null parameters should be asserted
    //client.getDietaryPreferenceForFoodBox(0);
    //client.getDietaryPreferenceForFoodBox(6);

    assertEquals(client.getDietaryPreferenceForFoodBox(1), "none");
    assertEquals(client.getDietaryPreferenceForFoodBox(2), "pollotarian");
    assertEquals(client.getDietaryPreferenceForFoodBox(5), "vegan");
  }

  @Test
  public void testGetItemsNumberForFoodBox(){
    // Null parameters should be asserted
    //client.getItemsNumberForFoodBox(0);
    //client.getItemsNumberForFoodBox(6);

    assertEquals(client.getItemsNumberForFoodBox(1), 3);
    assertEquals(client.getItemsNumberForFoodBox(2), 3);
    assertEquals(client.getItemsNumberForFoodBox(3), 3);
  }

  @Test
  public void testGetItemIdsForFoodBox(){
    Collection<Integer> knownIds = new ArrayList<>();
    knownIds.add(1);
    knownIds.add(3);
    knownIds.add(7);

    // Null parameters should be asserted
    //client.getItemIdsForFoodBox(0);
    //client.getItemIdsForFoodBox(6);

    assertEquals(client.getItemIdsForFoodBox(2), knownIds);
  }

  @Test
  public void testGetItemNameForFoodBox(){
    // Null parameters should be asserted
    //client.getItemNameForFoodBox(0, 0);
    //client.getItemNameForFoodBox(1, 0);
    //client.getItemNameForFoodBox(1, 6);
    //client.getItemNameForFoodBox(0, 1);

    assertEquals(client.getItemNameForFoodBox(1,1), "cucumbers");
    assertEquals(client.getItemNameForFoodBox(3,2), "onions");
    assertEquals(client.getItemNameForFoodBox(8,3), "bacon");
    assertEquals(client.getItemNameForFoodBox(4,1), null);
  }

  @Test
  public void testGetItemQuantityForFoodBox(){
    // Null parameters should be asserted
    //client.getItemQuantityForFoodBox(0, 0);
    //client.getItemQuantityForFoodBox(1, 0);
    //client.getItemQuantityForFoodBox(1, 6);
    //client.getItemQuantityForFoodBox(0, 1);

    assertEquals(client.getItemQuantityForFoodBox(1,1), 1);
    assertEquals(client.getItemQuantityForFoodBox(1,2), 2);
    assertEquals(client.getItemQuantityForFoodBox(4,3), 2);
    assertEquals(client.getItemQuantityForFoodBox(4,1), 0);
  }

  @Test
  public void testPickFoodBox(){
    // Null parameters should be asserted
    //client.pickFoodBox(0);
    //client.pickFoodBox(6);

    assertTrue(client.pickFoodBox(1));
  }

  @Test
  public void testChangeItemQuantityForPickedFoodBox(){
    // Null parameters should be asserted
    //client.changeItemQuantityForPickedFoodBox(0, -1);
    //client.changeItemQuantityForPickedFoodBox(1, -1);
    //client.changeItemQuantityForPickedFoodBox(0, 1);

    // Box not picked yet should give exception
    assertFalse(client.changeItemQuantityForPickedFoodBox(1, 1));

    assertTrue(client.pickFoodBox(1));
    int quantity = client.getItemQuantityForFoodBox(1, 1);

    // Item not in food box should hive exception
    assertFalse(client.changeItemQuantityForPickedFoodBox(3, 0));

    // Quantity is being increased should give exception
    assertFalse(client.changeItemQuantityForPickedFoodBox(1, quantity+1));

    assertTrue(client.changeItemQuantityForPickedFoodBox(1, quantity-1));
  }

  @Test
  public void testOrders() {
    // Create CHI
    Random rand = new Random();
    String temp = String.valueOf(rand.nextInt(10000));
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    String CHI = date + temp;

    assertTrue(client.registerShieldingIndividual(CHI));

    // GET_ORDER_NUMBERS
    // No orders have been placed yet so size should be 0
    assertEquals(client.getOrderNumbers().size(),0);

    // After order placed, size should be 1
    client.getClosestCateringCompany();
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    assertEquals(client.getOrderNumbers().size(),1);

    Collection<Integer> temp_orderNumbers = client.getOrderNumbers();
    Integer orderNumber = null;
    for (Integer i : temp_orderNumbers){
      orderNumber = i;
    }

    // GET_STATUS_FOR_ORDER
    // Null parameters should be asserted
    //client.getStatusForOrder(0);

    // Invalid/non existent orderNumber should return null
    assertEquals(client.getStatusForOrder(1000000000), null);

    assertEquals(client.getStatusForOrder(orderNumber), "placed");

    // GET_ITEM_IDS_FOR_ORDER
    // Null parameters should be asserted
    //client.getItemIdsForOrder(0);

    // We know the IDs because foodBox 1 was ordered
    Collection<Integer> knownIds = new ArrayList<>();
    knownIds.add(1);
    knownIds.add(2);
    knownIds.add(6);

    assertEquals(client.getItemIdsForOrder(orderNumber), knownIds);

    // GET_ITEM_NAME_FOR_ORDER
    // Null parameters should be asserted
    //client.getItemNameForOrder(0, 0);
    //client.getItemNameForOrder(1, 0);
    //client.getItemNameForOrder(0, 1);

    // Invalid/non existent orderNumber should return null
    assertEquals(client.getItemNameForOrder(1, 1000000000), null);
    // ItemId not in box should return null
    assertEquals(client.getItemNameForOrder(3, orderNumber), null);

    assertEquals(client.getItemNameForOrder(1, orderNumber), "cucumbers");

    // GET_ITEM_QUANTITY_FOR_ORDER
    // Null parameters should be asserted
    //client.getItemQuantityForOrder(0, 0);
    //client.getItemQuantityForOrder(1, 0);
    //client.getItemQuantityForOrder(0, 1);

    // Invalid/non existent orderNumber should return 0
    assertEquals(client.getItemQuantityForOrder(1, 1000000000), 0);
    // ItemId not in box should return null
    assertEquals(client.getItemQuantityForOrder(3, orderNumber), 0);

    assertEquals(client.getItemQuantityForOrder(1, orderNumber), 1);
  }

  @Test
  public void testGetClosestCateringCompany(){
    // Register new shielding individual
    Random rand = new Random();
    String temp = String.valueOf(rand.nextInt(10000));
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    String CHI = date + temp;
    client.registerShieldingIndividual(CHI);

    // Register new catering company
    String caterName = String.valueOf(rand.nextInt(10000));
    try {
      String response_cater = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerCateringCompany?business_name=" + caterName + "&postcode=" + client.getPostcode());
    } catch (IOException e) {
      e.printStackTrace();
    }

    // As the new Shielding Individual and catering company have the same address, this catering company should be the closest
    assertEquals(client.getClosestCateringCompany(), caterName);
  }

  @Test
  public  void testSetItemQuantityForOrder(){

  }

}
