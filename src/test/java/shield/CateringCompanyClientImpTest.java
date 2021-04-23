/**
 * We did not write separate unit and systems tests for CateringCompanyClientImpTest as we felt that
 * each method encompassed a use case in and of itself.
 */

package shield;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
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

  @Test
  public void testCateringCompanyNewRegistration() {
    // Generate post code
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String temp1 = String.valueOf(rand.nextInt(10));
    String temp2 = Character.toString( (char) (rand.nextInt(26) + 65));
    String temp3 = Character.toString( (char) (rand.nextInt(26) + 65));
    String postCode = "EH" + temp1 + "_" + temp1 + temp2 + temp3;

    // Null parameters should be asserted
    //client.registerCateringCompany(null, null);
    //client.registerCateringCompany(name, null);
    //client.registerCateringCompany(null, postCode);

    // Incorrect postCode format should give exception
    assertFalse(client.registerCateringCompany(name, "EH" + temp1));
    assertFalse(client.registerCateringCompany(name, temp1 + "_"));

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
    // Registering new Shielding Individual to place orders
    Random rand = new Random();
    String temp = String.valueOf(rand.nextInt(10000 - 1000) + 1000);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    String CHI = date + temp;
    String request1 = "/registerShieldingIndividual?CHI=" + CHI;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request1);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Registering new Catering Company to update Orders
    String name = String.valueOf(rand.nextInt(10000));
    String temp1 = String.valueOf(rand.nextInt(10));
    String temp2 = Character.toString( (char) (rand.nextInt(26) + 65));
    String temp3 = Character.toString( (char) (rand.nextInt(26) + 65));
    String postCode = "EH" + temp1 + "_" + temp1 + temp2 + temp3;
    String request2 = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request2);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Place order
    int orderNumber = 0;
    String request3 = "/placeOrder?individual_id=" + CHI + "&catering_business_name=" + name + "&catering_postcode=" + postCode;
    String data = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":2},{\"id\":2,\"name\":\"tomatoes\",\"quantity\":2}]}";
    try {
      String response = ClientIO.doPOSTRequest(clientProps.getProperty("endpoint") + request3, data);
      orderNumber = Integer.parseInt(response);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String invalidStatus = String.valueOf(rand.nextInt(10000));
    String[] statuses = {"packed", "dispatched", "delivered"};
    int falseOrderNumber = rand.nextInt(10000);

    // Null/invalid parameters should be asserted
    //client.updateOrderStatus(-1, null);
    //client.updateOrderStatus(orderNumber, null);
    //client.updateOrderStatus(-1, statuses[0]);

    // Updating an order with invalid order status
    assertFalse(client.updateOrderStatus(orderNumber, invalidStatus));
    // Updating an order that has not been placed
    assertFalse(client.updateOrderStatus(falseOrderNumber, statuses[0]));

    // Correctly update order status
    String request4 = "/requestStatus?order_id=" + orderNumber;
    try {
      // Packed
      assertTrue(client.updateOrderStatus(orderNumber, statuses[0]));
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request4);
      assertEquals(response, "1");

      // Dispatched
      assertTrue(client.updateOrderStatus(orderNumber, statuses[1]));
      response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request4);
      assertEquals(response, "2");

      // Delivered
      assertTrue(client.updateOrderStatus(orderNumber, statuses[2]));
      response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request4);
      assertEquals(response, "3");

      // Updating to an already up to date order status
      assertFalse(client.updateOrderStatus(orderNumber, statuses[2]));
      response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request4);
      assertEquals(response, "3");

      // Updating to a previous update status
      assertFalse(client.updateOrderStatus(orderNumber, statuses[0]));
      response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request4);
      assertEquals(response, "3");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
