
package shield;

import org.junit.jupiter.api.*;

import java.io.IOException;
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

  @Test
  public void testSupermarketNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = String.valueOf(rand.nextInt(10000));

    // Null parameters should be asserted
    //assertTrue(client.registerSupermarket(null, null));
    //assertTrue(client.registerSupermarket(name, null));
    //assertTrue(client.registerSupermarket(null, postCode));

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
  public void testRecordSupermarketOrder(){
    Random rand = new Random();
    String CHI1 = String.valueOf(rand.nextInt(10000));
    String CHI2 = String.valueOf(rand.nextInt(10000));
    String falseCHI = String.valueOf(rand.nextInt(10000));
    Integer orderNumber = rand.nextInt(10000);
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = String.valueOf(rand.nextInt(10000));

    // Register new supermarket to place order
    assertTrue(client.registerSupermarket(name, postCode));

    // Registering new Shielding Individual to make orders for
    String request1 = "/registerShieldingIndividual?CHI=" + CHI1;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request1);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Registering second new Shielding Individual to make orders for
    String request2 = "/registerShieldingIndividual?CHI=" + CHI2;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request2);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Null parameters should be asserted
    //assertTrue(client.recordSupermarketOrder(null, -1));
    //assertTrue(client.recordSupermarketOrder(CHI, -1));
    //assertTrue(client.recordSupermarketOrder(null, orderNumber));

    System.out.println(CHI1 + " " + falseCHI + " " + orderNumber);

    // Trying to place order with unregistered CHI
    assertFalse(client.recordSupermarketOrder(falseCHI, orderNumber));
    // Correctly record order
    assertTrue(client.recordSupermarketOrder(CHI1, orderNumber));
    // Trying to place another order with same number
    assertFalse(client.recordSupermarketOrder(CHI2, orderNumber));
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

    // Registering new Supermarket to record orders
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = String.valueOf(rand.nextInt(10000));
    String request2 = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request2);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Record order
    String request = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber + "&supermarket_business_name=" + name + "&supermarket_postcode=" + postCode;
    try {
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);
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
