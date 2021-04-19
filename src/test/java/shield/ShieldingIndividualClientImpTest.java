/**
 *
 */

package shield;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;


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
    String chi = String.valueOf(rand.nextInt(10000));

    //Test if new shielding individual registered
    assertTrue(client.registerShieldingIndividual(chi));
    assertTrue(client.isRegistered());
    assertEquals(client.getCHI(), chi);
    //Test if shielding individual already registered
    assertTrue(client.registerShieldingIndividual(chi));
    assertTrue(client.isRegistered());
    assertEquals(client.getCHI(), chi);
  }

  @Test
  public void testPlaceOrder() {
    Random rand = new Random();
    String chi = String.valueOf(rand.nextInt(10000));

    client.registerShieldingIndividual(chi);
    client.getClosestCateringCompany();
    /**
    client.pickFoodBox(1);
    int quantity = client.getItemQuantityForFoodBox(1, 1);
    client.changeItemQuantityForPickedFoodBox(1, quantity-1);
     */
    assertEquals(client.placeOrder(), true);
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
    String postCode1 = "EH11_2DR";
    String postCode2 = "EH11_3DR";
    String response;
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
    assertEquals(client.getDietaryPreferenceForFoodBox(1), "none");
    assertEquals(client.getDietaryPreferenceForFoodBox(2), "pollotarian");
    assertEquals(client.getDietaryPreferenceForFoodBox(5), "vegan");
  }

  @Test
  public void testGetItemsNumberForFoodBox(){
    assertEquals(client.getItemsNumberForFoodBox(1), 3);
    assertEquals(client.getItemsNumberForFoodBox(2), 3);
    assertEquals(client.getItemsNumberForFoodBox(3), 3);
  }

  @Test
  public void testGetItemIdsForFoodBox(){
    //assertEquals(client.getItemIdsForFoodBox(1), "[1, 2, 6]");
    assertEquals(client.getItemIdsForFoodBox(2), "[1, 3, 7]");
  }

  @Test
  public void testGetItemNameForFoodBox(){
    assertEquals(client.getItemNameForFoodBox(1,1), "cucumbers");
    assertEquals(client.getItemNameForFoodBox(3,2), "onions");
    assertEquals(client.getItemNameForFoodBox(8,3), "bacon");
    assertEquals(client.getItemNameForFoodBox(4,1), null);
  }

  @Test
  public void testGetItemQuantityForFoodBox(){
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
