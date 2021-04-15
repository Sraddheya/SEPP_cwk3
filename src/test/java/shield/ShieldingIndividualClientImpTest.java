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
   * Systems test
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
  }

  @Test
  public void testPlaceOrder() {
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
}
