/**
 * For this class, since there are mostly only getter methods and methods for a use case, we
 * could not think of many unit tests that would check something different from the system test. This is
 * why most of the tests in this class act as both unit and system tests.
 *
 * Where possible we used Http requests but in some tests we used methods declared in SupermarketClientImp,
 * however in those cases, we made sure that the method worked well by testing them first.
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

public class SupermarketClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private SupermarketClient client;

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

    client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
  }

  /**
   * Combination of system and unit tests
   */

  @Test
  public void testSupermarketNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = String.valueOf(rand.nextInt(10000));

    // Null parameters should be asserted
    //client.registerSupermarket(null, null);
    //client.registerSupermarket(name, null);
    //client.registerSupermarket(null, postCode);

    // Correct new registration
    assertTrue(client.registerSupermarket(name, postCode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), postCode);

    // Already registered
    assertTrue(client.registerSupermarket(name, postCode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), postCode);
  }

  @Test
  public void testUpdateOrderStatus(){
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

    // Registering new Supermarket to update Orders
    String name = String.valueOf(rand.nextInt(10000));
    String temp1 = String.valueOf(rand.nextInt(10));
    String temp2 = Character.toString( (char) (rand.nextInt(26) + 65));
    String temp3 = Character.toString( (char) (rand.nextInt(26) + 65));
    String postCode = "EH" + temp1 + "_" + temp1 + temp2 + temp3;
    String request2 = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request2);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Place order
    Integer orderNumber = rand.nextInt(10000);
    String request3 = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber + "&supermarket_business_name=" + name + "&supermarket_postcode=" + postCode;
    try {
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request3);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String invalidStatus = String.valueOf(rand.nextInt(10000));
    String[] statuses = {"packed", "dispatched", "delivered"};
    int falseOrderNumber = rand.nextInt(10000);

    // Null parameters should be asserted
    //client.updateOrderStatus(-1, null);
    //client.updateOrderStatus(orderNumber, null);
    //client.updateOrderStatus(-1, statuses[0]);

    // Updating an order with invalid order status
    assertFalse(client.updateOrderStatus(orderNumber, invalidStatus));
    // Updating an order that has not been placed
    assertFalse(client.updateOrderStatus(falseOrderNumber, statuses[0]));
    // Correctly update order status
    assertTrue(client.updateOrderStatus(orderNumber, statuses[0]));
    assertTrue(client.updateOrderStatus(orderNumber, statuses[1]));
    assertTrue(client.updateOrderStatus(orderNumber, statuses[2]));
    // Updating an already up to date order status
    assertFalse(client.updateOrderStatus(orderNumber, statuses[2]));
  }

  /**
   * Unit tests
   */

  @Test
  public void testRecordSupermarketOrder(){
    // Registering new Shielding Individual to place orders
    Random rand = new Random();
    String temp1 = String.valueOf(rand.nextInt(10000 - 1000) + 1000);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy");
    LocalDateTime now = LocalDateTime.now();
    String date = dtf.format(now);
    String CHI1 = date + temp1;
    String request1 = "/registerShieldingIndividual?CHI=" + CHI1;
    try {
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request1);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Register second new Shielding Individual to place orders
    String temp2 = String.valueOf(rand.nextInt(10000 - 1000) + 1000);
    String CHI2 = date + temp2;
    String request2 = "/registerShieldingIndividual?CHI=" + CHI2;
    try {
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request2);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Registering new Supermarket to place Orders
    String name = String.valueOf(rand.nextInt(10000));
    String temp11 = String.valueOf(rand.nextInt(10));
    String temp12 = Character.toString( (char) (rand.nextInt(26) + 65));
    String temp13 = Character.toString( (char) (rand.nextInt(26) + 65));
    String postCode = "EH" + temp11 + "_" + temp11 + temp12 + temp13;
    assertTrue(client.registerSupermarket(name, postCode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), postCode);

    String falseCHI = String.valueOf(rand.nextInt(10000));
    Integer orderNumber = rand.nextInt(10000);

    // Null parameters should be asserted
    //client.recordSupermarketOrder(null, -1);
    //client.recordSupermarketOrder(CHI1, -1);
    //client.recordSupermarketOrder(null, orderNumber);

    // Trying to place order with unregistered CHI
    assertFalse(client.recordSupermarketOrder(falseCHI, orderNumber));
    // Correctly record order
    assertTrue(client.recordSupermarketOrder(CHI1, orderNumber));
    // Trying to place another order with same number
    assertFalse(client.recordSupermarketOrder(CHI2, orderNumber));
  }
}
