/**
 *
 */

package shield;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

/**
 *
 */

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
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = String.valueOf(rand.nextInt(10000));

    assertTrue(client.registerCateringCompany(name, postCode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
  }

  @Test
  public void testUpdateOrderStatus() {
    int[] orderNumbers = {12345,23456,34567,45678,56789};
    String[] statuses = {"packed", "dispatched", "delivered"};
    int selectedOrderIndex = new Random().nextInt(orderNumbers.length);
    int selectedOrder = orderNumbers[selectedOrderIndex];
    int selectedStatusIndex = new Random().nextInt(statuses.length);
    String selectedStatus = statuses[selectedStatusIndex];

    System.out.println(selectedOrder);
    System.out.println(selectedStatus);
    assertTrue(client.updateOrderStatus(selectedOrder, selectedStatus));
  }
}
