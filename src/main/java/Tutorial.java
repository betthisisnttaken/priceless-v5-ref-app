
import com.mastercard.developer.interceptors.OkHttpOAuth1Interceptor;
import com.mastercard.developer.utils.AuthenticationUtils;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.*;
import org.openapitools.client.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;

public class Tutorial {

    public Tutorial() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, NoSuchProviderException, ApiException {

        System.out.println("In Tutorial Const");

        BigDecimal mPartnerID = BigDecimal.valueOf(522);

        PrivateKey signingKey = AuthenticationUtils.loadSigningKey(
                "/Users/derekhumphreys/Documents/MCD_Sandbox_Priceless_v5_Ref_App_API_Keys/Priceless_v5_Ref_App-sandbox.p12",
                "keyalias",
                "keystorepassword");

        System.out.println("Have private key");

        String consumerKey = "YDUUAwzkE5nBVUpeJcO2AeOqzMyLjmweetHBOl8d52d5bc35!2c94bec224cd4fce8e66661a33d8b4bf0000000000000000";

        System.out.println("Set consumer key");

        ApiClient apiClient = new ApiClient();
        System.out.println("After new ApiClient");

        apiClient.setBasePath("https://stage.api.mastercard.com/pricelessapiv5");
        System.out.println("After ApiClient set Base Path");

        apiClient.setDebugging(true);
        System.out.println("After ApiClient set debugging");

        apiClient.setHttpClient(apiClient.getHttpClient().newBuilder().addInterceptor(new OkHttpOAuth1Interceptor(consumerKey, signingKey)).build());
        System.out.println("After ApiClient set HttpClient");

        //[DH]ServiceApi serviceApi = new ServiceApi(client);
        //[DH]ApiCallback mListener = new A();
        InlineObject2 sessionCreateParams = new InlineObject2();
        sessionCreateParams.setPartnerId(mPartnerID);
        System.out.println("After crete sessionCreateParams with 522");

        SessionsApi session = new SessionsApi(apiClient);
        System.out.println("After new SessionsApi");

        //[DH]langApi.languagesCall(BigDecimal.valueOf(522), session.sessionCreate(mInlineObject2).toString(), mListener);
        InlineResponse2008 mSession = session.sessionCreate(sessionCreateParams);
        String sessionCookie = mSession.getData().getSessionCookie();
        System.out.println("After create session");

        LanguagesApi languagesApi = new LanguagesApi(apiClient);
        System.out.println("After new LanguagesApi");

        InlineResponse2006 langApiResponse = languagesApi.languages(mPartnerID, sessionCookie);
        System.out.println("After languagesApi.languages" + langApiResponse.toString());

        CategoriesApi categoriesAPI = new CategoriesApi(apiClient);
        InlineResponse2007 catApiResponse = categoriesAPI.categories(mPartnerID, sessionCookie);
        System.out.println("After categoriesAPI.categories" + catApiResponse.toString());

        LocationsApi locationsAPI = new LocationsApi(apiClient);
        InlineResponse2005 locationsApiResponse = locationsAPI.locations(mPartnerID, sessionCookie);
        System.out.println("After locationsAPI.locations" + locationsApiResponse.toString());

        ProductsApi productsAPI = new ProductsApi(apiClient);
        BigDecimal languageId = BigDecimal.valueOf(1); // English US
        BigDecimal geographicId = BigDecimal.valueOf(0); // 330 is New York
        BigDecimal checkoutOnly = BigDecimal.valueOf(1); // 1 is products that can be checked out via API
        String categoryIds = ""; // Comma delimited list of category ids to filter
        BigDecimal additionalFields = BigDecimal.valueOf(0); // 1 is to include extra data
        BigDecimal productID = BigDecimal.valueOf(141487); // 133459, 140944, 141487, 145599, 145767, 146544, 146778, 158137, 160147

        InlineResponse200 productIDsInlineResponse = productsAPI.allPartnerProductIds(mPartnerID, languageId, geographicId, checkoutOnly, categoryIds, sessionCookie, additionalFields);
        System.out.println("After productsAPI.allPartnerProductIds" + productIDsInlineResponse.toString());

        ProductInventory productInventory = productsAPI.inventory(productID, mPartnerID, sessionCookie);
        System.out.println("After productsAPI.inventory" + productInventory.toString());

        ProductInfo productInfo = productsAPI.productInfo(productID, mPartnerID, languageId, sessionCookie);
        System.out.println("After productsAPI.productInfo" + productInfo.toString());

        //InlineResponse2001 productTranslations = productsAPI.productTranslations(productID, mPartnerID, sessionCookie);
        //System.out.println("After productsAPI.productTranslations" + productTranslations.toString());

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName("Donald");
        shippingAddress.setLastName("Duck");
        shippingAddress.setLine1("114 5th Avenue");
        shippingAddress.setCity("New York");
        shippingAddress.setState("NY");
        shippingAddress.setPostalCode("10011");
        shippingAddress.setCountry("USA");
        shippingAddress.setPhone("555 5555");

        InlineObject estimateCreateParams = new InlineObject();
        estimateCreateParams.setPartnerId(mPartnerID);
        estimateCreateParams.setSessionCookie(sessionCookie);
        estimateCreateParams.setShippingAddress(shippingAddress);
        estimateCreateParams.setBillingAddress(shippingAddress);

        OrderItems orderItems = new OrderItems();
        orderItems.setProductId(productID);
        orderItems.setPeoplePerItem(BigDecimal.valueOf(1));
        orderItems.setQuantity(BigDecimal.valueOf(1));
        ArrayList<OrderItems> orderItemsArrayList = new ArrayList<OrderItems>();
        orderItemsArrayList.add(orderItems);
        estimateCreateParams.setItems(orderItemsArrayList);

        EstimatesApi estimatesApi = new EstimatesApi(apiClient);
        InlineResponse2002 estimateCreateResponse = estimatesApi.estimateCreate(estimateCreateParams);
        System.out.println("After estimatesApi.estimateCreate" + estimateCreateResponse.toString());

        // The JSON coming back confuses it - it does not match the YAML ?
        
    }

    // Driver Function
    public static void main(String[] args)
    {
        System.out.println("BEGIN");
        try {
            Tutorial obj = new Tutorial();
            System.out.println("END");
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
/*
class A implements ApiCallback{
    @Override
    public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
        System.out.println("Failed");
    }

    @Override
    public void onSuccess(Object result, int statusCode, Map responseHeaders) {
        System.out.println("Success");
    }

    @Override
    public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
        System.out.println("Uploading");
    }

    @Override
    public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
        System.out.println("Downloading");
    }
}
*/