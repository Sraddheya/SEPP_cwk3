
package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.time.LocalDateTime;
import java.io.InputStream;

public class CateringCompanyClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private CateringCompanyClient client;

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

    client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
  }

  /**
   * Systems tests
   */
  @Test
  public void testCateringCompanyNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postCode_part1 = String.valueOf(rand.nextInt(10));
    String postCode_part2 = String.valueOf(rand.nextInt(1000));
    String postCode = "EH" + postCode_part1 + "_" + postCode_part2;

    // Null parameters should be asserted
    //assertTrue(client.registerCateringCompany(null, null));
    //assertTrue(client.registerCateringCompany(name, null));
    //assertTrue(client.registerCateringCompany(null, postCode));

    // Incorrect postCode format should give exception
    assertFalse(client.registerCateringCompany(name, postCode_part1));

    // Correct new registration
    assertTrue(client.registerCateringCompany(name, postCode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), postCode);

    // Already registered
    assertTrue(client.registerCateringCompany(name, postCode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), postCode);
  }

  @Test
  public void testUpdateOrderStatus() {
    Random rand = new Random();
    String invalidStatus = String.valueOf(rand.nextInt(10000));
    int falseOrderNumber = rand.nextInt(10000);
    int orderNumber = 0;
    String[] statuses = {"packed", "dispatched", "delivered"};

    // Registering new Shielding Individual to make orders
    String CHI = String.valueOf(rand.nextInt(10000));
    String request1 = "/registerShieldingIndividual?CHI=" + CHI;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request1);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Registering new Shielding Individual to make orders
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = String.valueOf(rand.nextInt(10000));
    String request2 = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request2);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Placing order
    String request3 = "/placeOrder?individual_id=" + CHI + "&catering_business_name=" + name + "&catering_postcode=" + postCode;
    String data = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20},{\"id\":2,\"name\":\"tomatoes\",\"quantity\":2}]}";
    try {
      String response = ClientIO.doPOSTRequest(clientProps.getProperty("endpoint") + request3, data);
      orderNumber = Integer.parseInt(response);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Null/invalid parameters should be asserted
    //assertTrue(client.updateOrderStatus(-1, null));
    //assertTrue(client.updateOrderStatus(orderNumber, null));
    //assertTrue(client.updateOrderStatus(-1, statuses[0]));

    // Updating an order with invalid order status
    assertFalse(client.updateOrderStatus(orderNumber, invalidStatus));
    // Correctly update order status
    assertTrue(client.updateOrderStatus(orderNumber, statuses[0]));
    assertTrue(client.updateOrderStatus(orderNumber, statuses[1]));
    assertTrue(client.updateOrderStatus(orderNumber, statuses[2]));
    // Updating an already up to date order status
    assertFalse(client.updateOrderStatus(orderNumber, statuses[2]));
    // Updating an order that has not been placed
    assertFalse(client.updateOrderStatus(falseOrderNumber, statuses[0]));
  }

}
