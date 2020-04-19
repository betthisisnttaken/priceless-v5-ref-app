
import com.mastercard.developer.interceptors.OkHttpOAuth1Interceptor;
import com.mastercard.developer.utils.AuthenticationUtils;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.*;
import org.openapitools.client.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Tutorial {

    public Tutorial() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, NoSuchProviderException, ApiException {

        // Read in application properties.
        ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
        BigDecimal partnerID = new BigDecimal(resourceBundle.getString("mastercard.api.partnerid"));
        PrivateKey signingKey = AuthenticationUtils.loadSigningKey(
                resourceBundle.getString("mastercard.api.p12.path"),
                resourceBundle.getString("mastercard.api.key.alias"),
                resourceBundle.getString("mastercard.api.keystore.password"));
        String consumerKey = resourceBundle.getString("mastercard.api.consumer.key");

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(resourceBundle.getString("mastercard.api.basePath"));
        apiClient.setDebugging(true);

        // Add the Interceptor that will ensure the keys are added to every request.
        apiClient.setHttpClient(apiClient.getHttpClient().newBuilder().addInterceptor(new OkHttpOAuth1Interceptor(consumerKey, signingKey)).build());

        // Create session for Priceless API calls.
        InlineObject2 sessionCreateParams = new InlineObject2();
        sessionCreateParams.setPartnerId(partnerID);
        SessionsApi session = new SessionsApi(apiClient);
        SessionResponse mSession = session.sessionCreate(sessionCreateParams);
        // Store session cookie to pass into all Priceless API calls.
        String sessionCookie = mSession.getData().getSessionCookie();

        // Get all languages available.
        LanguagesApi languagesApi = new LanguagesApi(apiClient);
        InlineResponse2006 langApiResponse = languagesApi.languages(partnerID, sessionCookie);
        System.out.println("Languages available: " + langApiResponse.toString());

        // Get language ID for English (US) so we can filter products to English (US) only.
        Languages englishUS = langApiResponse.getData().stream()
                .filter(language -> "English (US)".equals(language.getLanguageName()))
                .findAny()
                .orElse(null);

        // Get all product categories and their codes - can be used to filter product list.
        CategoriesApi categoriesAPI = new CategoriesApi(apiClient);
        InlineResponse2007 catApiResponse = categoriesAPI.categories(partnerID, sessionCookie);
        System.out.println("Categories available: " + catApiResponse.toString());

        // Get all locations where products are available - can be used to filter product list.
        LocationsApi locationsAPI = new LocationsApi(apiClient);
        InlineResponse2005 locationsApiResponse = locationsAPI.locations(partnerID, sessionCookie);
        System.out.println("Locations available: " + locationsApiResponse.toString());

        // Get information about the Products - list of products, product info, product inventory.
        ProductsApi productsAPI = new ProductsApi(apiClient);
        BigDecimal checkoutOnly = BigDecimal.valueOf(1); // 1 is products that can be checked out via API
        String categoryIds = ""; // Comma delimited list of category ids to filter
        BigDecimal additionalFields = BigDecimal.valueOf(0); // 1 is to include extra data

        // Get geographic ID for Chicago so we can filter products to Chicago region only.
        Geographics chicago = locationsApiResponse.getData().stream()
                .filter(geographic -> "Chicago".equals(geographic.getGeographicName()))
                .findAny()
                .orElse(null);

        InlineResponse200 productIDsInlineResponse = productsAPI.allPartnerProductIds(partnerID, englishUS.getLanguageId(), chicago.getGeographicId(), checkoutOnly, categoryIds, sessionCookie, additionalFields);
        System.out.println("Products linked to your Partner ID: " + productIDsInlineResponse.toString());

        // TODO: Find a VALID product from the list assigned to our Partner ID.
        //BigDecimal productID = BigDecimal.valueOf(140944); // 133459, 140944, *141487, 145599, 145767, 146544, 146778, 158137, 160147

        // We'll take the first product returned.
        BigDecimal productID = productIDsInlineResponse.getData().get(0).getProductId();

        // Get count of how many products left in stock.
        ProductInventory productInventory = productsAPI.inventory(productID, partnerID, sessionCookie);
        System.out.println("Product Inventory: " + productInventory.toString());

        // Ensure there is enough inventory.
        if (productInventory.getData().getInventoryCount().equals(0)) return;

        // Get detailed product info to display to customers.
        ProductInfo productInfo = productsAPI.productInfo(productID, partnerID, englishUS.getLanguageId(), sessionCookie);
        System.out.println("Product Information: " + productInfo.toString());

        // Get estimated price for the product based on number of people and other variables.
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName("Evans");
        shippingAddress.setLastName("Luke");
        shippingAddress.setLine1("123 Priceless Ave.");
        shippingAddress.setCity("San Francisco");
        shippingAddress.setState("CA");
        shippingAddress.setPostalCode("94109");
        shippingAddress.setCountry("US");
        shippingAddress.setPhone("5556667878");

        InlineObject estimateCreateParams = new InlineObject();
        estimateCreateParams.setPartnerId(partnerID);
        estimateCreateParams.setSessionCookie(sessionCookie);
        estimateCreateParams.setShippingAddress(shippingAddress);
        estimateCreateParams.setBillingAddress(shippingAddress);

        OrderItems orderItems = new OrderItems();
        orderItems.setProductId(productID);

        // Number of people needs to match one of the variants for the product. Using first variant here.
        orderItems.setPeoplePerItem(productInfo.getData().getVariants().get(0).getPeoplePerItem());
        orderItems.setQuantity(BigDecimal.valueOf(1));
        ArrayList<OrderItems> orderItemsArrayList = new ArrayList<OrderItems>();
        orderItemsArrayList.add(orderItems);
        estimateCreateParams.setItems(orderItemsArrayList);

        EstimatesApi estimatesApi = new EstimatesApi(apiClient);
        InlineResponse2002 estimateCreateResponse = estimatesApi.estimateCreate(estimateCreateParams);
        System.out.println("Estimate Response: " + estimateCreateResponse.toString());

        // Place the Order (use Order ID returned from Estimate).
        InlineObject1 ordersCreateParams = new InlineObject1();
        ordersCreateParams.setPartnerId(partnerID);
        ordersCreateParams.setSessionCookie(sessionCookie);
        ordersCreateParams.setShippingAddress(shippingAddress);
        ordersCreateParams.setBillingAddress(shippingAddress);
        ordersCreateParams.setItems(orderItemsArrayList);

        // Pull in the order id from the estimate to be used in the order call. Valid for 15 mins.
        ordersCreateParams.setOrderId(estimateCreateResponse.getData().getOrderId());
        ordersCreateParams.setEmail("luke@priceless.com");
        ordersCreateParams.setLanguageId(englishUS.getLanguageId());

        Payment payment = new Payment();
        PaymentCreditCard creditCard = new PaymentCreditCard();
        creditCard.setCardHolderName("Luke Evans");
        creditCard.setCardNumber("5555555555554444");
        creditCard.setCcv(BigDecimal.valueOf(274));
        creditCard.setExpirationMonth(BigDecimal.valueOf(10));
        creditCard.setExpirationYear(BigDecimal.valueOf(2022));
        payment.setCreditCard(creditCard);
        ordersCreateParams.setPayment(payment);

        OrdersApi ordersApi = new OrdersApi(apiClient);
        InlineResponse2003 ordersCreateResponse = ordersApi.orderCreate(ordersCreateParams);
        System.out.println("Create Order Response: " + ordersCreateResponse.toString());

        // Check the order status. Use the API Order ID returned from placing the order.
        InlineResponse2004 ordersStatusesResponse = ordersApi.orderStatuses(ordersCreateResponse.getData().getOrderId(), partnerID);
        System.out.println("Order status: " + ordersStatusesResponse.toString());

    }

    // Driver Function
    public static void main(String[] args)
    {
        try {

            Tutorial obj = new Tutorial();

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

    }
}
