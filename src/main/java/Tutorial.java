
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
        SessionRequest sessionCreateParams = new SessionRequest();
        sessionCreateParams.setPartnerId(partnerID);
        SessionsApi session = new SessionsApi(apiClient);
        SessionResponse mSession = session.sessionCreate(sessionCreateParams);
        // Store session cookie to pass into all Priceless API calls.
        String sessionCookie = mSession.getData() != null ? mSession.getData().getSessionCookie() : "";

        // Get all languages available.
        LanguagesApi languagesApi = new LanguagesApi(apiClient);
        ProductLanguages langApiResponse = languagesApi.languages(partnerID, sessionCookie);
        System.out.printf("Languages available: %s%n", langApiResponse.toString());

        // Get language ID for English (US) so we can filter products to English (US) only.
        // TODO: Set this to any valid language in your sandbox.
        ProductLanguagesData englishUS = langApiResponse.getData() != null ? langApiResponse.getData().stream()
                    .filter(language -> "English (US)".equals(language.getLanguageName()))
                    .findAny()
                    .orElse(null) : null;

        if (englishUS == null)
            return;

        // Get all product categories and their codes - can be used to filter product list.
        CategoriesApi categoriesAPI = new CategoriesApi(apiClient);
        Categories catApiResponse = categoriesAPI.categories(partnerID, sessionCookie);
        System.out.printf("Categories available: %s%n", catApiResponse.toString());

        // Get all locations where products are available - can be used to filter product list.
        LocationsApi locationsAPI = new LocationsApi(apiClient);
        Geographics locationsApiResponse = locationsAPI.locations(partnerID, sessionCookie);
        System.out.printf("Locations available: %s%n", locationsApiResponse.toString());

        // Get information about the Products - list of products, product info, product inventory.
        ProductsApi productsAPI = new ProductsApi(apiClient);
        BigDecimal checkoutOnly = BigDecimal.valueOf(1); // 1 is products that can be checked out via API
        String categoryIds = ""; // Comma delimited list of category ids to filter
        BigDecimal additionalFields = BigDecimal.valueOf(0); // 1 is to include extra data

        // Get geographic ID for Chicago so we can filter products to Chicago region only.
        // TODO: Set this to any valid region in your sandbox.
        GeographicsData chicago = locationsApiResponse.getData() != null ? locationsApiResponse.getData().stream()
                    .filter(geographic -> "Chicago".equals(geographic.getGeographicName()))
                    .findAny()
                    .orElse(null) : null;

        if (chicago == null)
            return;

        AllProductIdsStruct productIDsResponse = productsAPI.allPartnerProductIds(partnerID, englishUS.getLanguageId(), chicago.getGeographicId(), checkoutOnly, categoryIds, sessionCookie, additionalFields);
        System.out.printf("Products linked to your Partner ID: %s%n", productIDsResponse.toString());

       // We'll take the first product returned.
        if (productIDsResponse.getData() == null || productIDsResponse.getData().isEmpty()) return;
        BigDecimal productID = productIDsResponse.getData().get(0).getProductId();

        // Get count of how many products left in stock.
        ProductInventory productInventory = productsAPI.inventory(productID, partnerID, sessionCookie);
        System.out.printf("Product Inventory: %s%n", productInventory.toString());

        // Ensure there is enough inventory.
        if (productInventory.getData() == null || productInventory.getData().getInventoryCount() == null || productInventory.getData().getInventoryCount().equals(BigDecimal.valueOf(0))) return;

        // Get detailed product info to display to customers.
        ProductInfo productInfo = productsAPI.productInfo(productID, partnerID, englishUS.getLanguageId(), sessionCookie);
        System.out.printf("Product Information: %s%n", productInfo.toString());

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

        EstimateRequest estimateCreateParams = new EstimateRequest();
        estimateCreateParams.setPartnerId(partnerID);
        estimateCreateParams.setSessionCookie(sessionCookie);
        estimateCreateParams.setShippingAddress(shippingAddress);
        estimateCreateParams.setBillingAddress(shippingAddress);

        OrderItems orderItems = new OrderItems();
        orderItems.setProductId(productID);

        // Number of people needs to match one of the variants for the product. Using first variant here.
        if (productInfo.getData() == null || productInfo.getData().getVariants() == null || productInfo.getData().getVariants().isEmpty()) return;
        orderItems.setPeoplePerItem(productInfo.getData().getVariants().get(0).getPeoplePerItem());
        orderItems.setQuantity(BigDecimal.valueOf(1));
        ArrayList<OrderItems> orderItemsArrayList = new ArrayList<>();
        orderItemsArrayList.add(orderItems);
        estimateCreateParams.setItems(orderItemsArrayList);

        EstimatesApi estimatesApi = new EstimatesApi(apiClient);
        EstimateResponse estimateCreateResponse = estimatesApi.estimateCreate(estimateCreateParams);
        System.out.printf("Estimate Response: %s%n", estimateCreateResponse.toString());

        // Place the Order (use Order ID returned from Estimate).
        OrdersRequest ordersCreateParams = new OrdersRequest();
        ordersCreateParams.setPartnerId(partnerID);
        ordersCreateParams.setSessionCookie(sessionCookie);
        ordersCreateParams.setShippingAddress(shippingAddress);
        ordersCreateParams.setBillingAddress(shippingAddress);
        ordersCreateParams.setItems(orderItemsArrayList);

        // Pull in the order id from the estimate to be used in the order call. Valid for 15 mins.
        if (estimateCreateResponse.getData() == null) return;
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
        OrdersResponse ordersCreateResponse = ordersApi.orderCreate(ordersCreateParams);
        System.out.printf("Create Order Response: %s%n", ordersCreateResponse.toString());

        // Check the order status. Use the API Order ID returned from placing the order.
        if (ordersCreateResponse.getData() == null) return;
        OrdersStatusResponse ordersStatusesResponse = ordersApi.orderStatuses(ordersCreateResponse.getData().getOrderId(), partnerID);
        System.out.printf("Order status: %s%n", ordersStatusesResponse.toString());

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
