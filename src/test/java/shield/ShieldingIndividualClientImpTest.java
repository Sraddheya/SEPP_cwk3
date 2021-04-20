/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClient client;

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
  }

  /**
   * System tests
   */
  @Test
  public void testShieldingIndividualNewRegistration() {
    Random rand = new Random();
    String falseCHI = String.valueOf(rand.nextInt(100000));
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    String temp = String.valueOf(rand.nextInt(10000));
    String CHI = date + temp;

    // Null parameters should be asserted
    //assertTrue(client.registerShieldingIndividual(null));

    // Too short CHI should give exception
    assertFalse(client.registerShieldingIndividual(date));
    // Not valid date should give exception
    assertFalse(client.registerShieldingIndividual(falseCHI + falseCHI));

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
    Random rand = new Random();
    String chi = String.valueOf(rand.nextInt(10000));

    client.registerShieldingIndividual(chi);
    client.getClosestCateringCompany();
    client.pickFoodBox(1);
    assertTrue(client.placeOrder());
  }

  /**
   * Unit tests
   */
  @Test
  public void testShowFoodBoxes() {
    assertEquals(client.showFoodBoxes("none").size(), 3);
  }

  @Test
  public void testGetCateringCompanies() {
    System.out.println(client.getCateringCompanies());
  }

  @Test
  public void testGetDistance() {
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
    assertEquals(client.getDistance(postCode_part11, postCode_part21), 0);
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
    //client.getDietaryPreferenceForFoodBox(-1);

    assertEquals(client.getDietaryPreferenceForFoodBox(1), "none");
    assertEquals(client.getDietaryPreferenceForFoodBox(2), "pollotarian");
    assertEquals(client.getDietaryPreferenceForFoodBox(5), "vegan");
  }

  @Test
  public void testGetItemsNumberForFoodBox(){
    // Null parameters should be asserted
    //client.getItemsNumberForFoodBox(-1);

    assertEquals(client.getItemsNumberForFoodBox(1), 3);
    assertEquals(client.getItemsNumberForFoodBox(2), 3);
    assertEquals(client.getItemsNumberForFoodBox(3), 3);
  }

  @Test
  public void testGetItemIdsForFoodBox(){
    // Null parameters should be asserted
    client.getItemIdsForFoodBox(-1);

    //assertEquals(client.getItemIdsForFoodBox(1), "[1, 2, 6]");
    assertEquals(client.getItemIdsForFoodBox(2), "[1, 3, 7]");
  }

  @Test
  public void testGetItemNameForFoodBox(){
    // Null parameters should be asserted
    //client.getItemNameForFoodBox(-1, -1);
    //client.getItemNameForFoodBox(1, -1);
    //client.getItemNameForFoodBox(-1, 1);

    assertEquals(client.getItemNameForFoodBox(1,1), "cucumbers");
    assertEquals(client.getItemNameForFoodBox(3,2), "onions");
    assertEquals(client.getItemNameForFoodBox(8,3), "bacon");
    assertEquals(client.getItemNameForFoodBox(4,1), null);
  }

  @Test
  public void testGetItemQuantityForFoodBox(){
    // Null parameters should be asserted
    //client.getItemQuantityForFoodBox(-1, -1);
    //client.getItemQuantityForFoodBox(1, -1);
    //client.getItemQuantityForFoodBox(-1, 1);

    assertEquals(client.getItemQuantityForFoodBox(1,1), 1);
    assertEquals(client.getItemQuantityForFoodBox(1,2), 2);
    assertEquals(client.getItemQuantityForFoodBox(4,3), 2);
    assertEquals(client.getItemQuantityForFoodBox(4,1), 0);
  }

  @Test
  public void testPickFoodBox(){
    assertTrue(client.pickFoodBox(1));
  }

  @Test
  public void testChangeItemQuantityForPickedFoodBox(){
    assertTrue(client.pickFoodBox(1));
    int quantity = client.getItemQuantityForFoodBox(1, 1);
    assertTrue(client.changeItemQuantityForPickedFoodBox(1, quantity-1));
  }

  @Test
  public void testOrders() {
    Random rand = new Random();
    String chi = String.valueOf(rand.nextInt(10000));

    client.registerShieldingIndividual(chi);
    client.getClosestCateringCompany();
    client.pickFoodBox(1);
    assertTrue(client.placeOrder());
    assertEquals(client.getOrderNumbers().size(),1);
  }

  @Test
  public void testGetClosestCateringCompany(){
    Random rand = new Random();
    String chi = String.valueOf(rand.nextInt(10000));
    String caterName = String.valueOf(rand.nextInt(10000));

    client.registerShieldingIndividual(chi);
    System.out.println(client.getPostcode());

    try {
      String response_cater = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerCateringCompany?business_name=" + caterName + "&postcode=" + client.getPostcode());
      // As the new Shielding Individual and catering company have the same address, this catering company should be the closest
      assertEquals(client.getClosestCateringCompany(), caterName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
  @Test
  public void testGetCateringCompanies() throws IOException {
    String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/getCaterers");
    String temp = "";

    for (String s: client.getCateringCompanies()){
      temp += ",\"" + s + "\"";
    }

    temp = temp.substring(1);
    temp = "[" + temp + "]";

    assertEquals(temp, response);
  }

  @Test
  public void testGetDistance() throws IOException {
    Random rand = new Random();
    String postCode1 = "EH1_" + String.valueOf(rand.nextInt(1000));
    String postCode2 = "EH1_" + String.valueOf(rand.nextInt(1000));

    String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2);
    assertEquals(client.getDistance(postCode1, postCode2), Float.parseFloat(response));
  }


  @Test
  public void testGetClosestCateringCompany(){
    Random rand = new Random();
    String chi = String.valueOf(rand.nextInt(10000));
    String name = String.valueOf(rand.nextInt(10000));

    client.registerShieldingIndividual(chi);

    try {
      String response_cater = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerCateringCompany?business_name=" + name + "&postcode=" + client.getPostcode().replace("_", ""));
      // As the new Shielding Individual and catering company have the same address, this catering company should be the closest
      assertEquals(client.getClosestCateringCompany(), name);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  */
}
