/*
import com.mastercard.developer.oauth.OAuth;
import com.mastercard.developer.utils.SecurityUtils;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
//[DH]import io.swagger.client.ApiClient;
import okio.Buffer;
import org.openapitools.client.ApiClient;
//[DH]import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PrivateKey;

//[DH]@Service
public class ApiClientService {
    private ApiClient apiClient = null;

    public ApiClient getApiClient() throws Exception {
        if (apiClient == null) {
            createNewApiClient();
        }
        return apiClient;
    }


    private synchronized void createNewApiClient() throws Exception {
        // TODO: Get these from applications.properties
        String consumerKey = "YDUUAwzkE5nBVUpeJcO2AeOqzMyLjmweetHBOl8d52d5bc35!2c94bec224cd4fce8e66661a33d8b4bf0000000000000000"; // FIXME: This should be your real key
        String p12FilePath = "Priceless_v5_Ref_App-sandbox.p12";  // FIXME: This is your private key file, should be somewhere secure
        String keyAlias = "keyalias";
        String signingKeyPassword = "keystorepassword"; // FIXME: Get from somewhere secure

        SigningInterceptor signingInterceptor = new SigningInterceptor(consumerKey, p12FilePath, keyAlias, signingKeyPassword);
        apiClient = new ApiClient();
        apiClient.getHttpClient().networkInterceptors().add(signingInterceptor);

        //[DH]log.info("Talking to sandbox");

        String basePath = apiClient.getBasePath().replaceAll("api","sandbox.api");

        apiClient.setBasePath(basePath);
    }

    private static class SigningInterceptor implements Interceptor {

        private PrivateKey privateKey;
        private String consumerKey;

        public SigningInterceptor(
                String consumerKey,
                String p12FilePath,
                String keyAlias,
                String signingKeyPassword) throws Exception {
            this.consumerKey = consumerKey;
            this.privateKey = SecurityUtils.loadPrivateKey(p12FilePath, keyAlias, signingKeyPassword);
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            final Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            String body = buffer.readUtf8();
            String authHeader = OAuth.getAuthorizationHeader(
                    request.uri(),
                    request.method(),
                    body,
                    Charset.forName("utf-8"),
                    consumerKey,
                    privateKey
            );

            Request signedRequest = request.newBuilder()
                    .addHeader("Authorization", authHeader)
                    .build();
            return chain.proceed(signedRequest);
        }
    }
}*/