package com.example.otc;

import android.util.Log;

import com.google.common.hash.Hashing;
import okhttp3.*;
import java.nio.charset.StandardCharsets;

public class ApiService {

    private static final String BASE_URL = "https://xtpshareapimanagement.azure-api.net";
    private static ApiService instance;
    private OkHttpClient client;

    private ApiService() {
        client = new OkHttpClient();
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    // Utility method to hash the password
    private String hashPassword(String password) {
//        return BCrypt.hashpw(password, BCrypt.gensalt());
        return Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
    }

    // User Registration (Sign Up)
    public void userRegister(String email, String password, Callback callback) {
        String hashedPassword = hashPassword(password);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/EP/Register").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("passwordHash", hashedPassword);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void userRegister_Google(String tokenID, Callback callback){

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/Google/RegisterAndGetSession").newBuilder();
        urlBuilder.addQueryParameter("tokenID", tokenID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();
        Log.d("Request:",request.url().url().toString());

        client.newCall(request).enqueue(callback);
    }

    // Session Creation (in Sign In)
    public void sessionCreate(String email, String password, Callback callback) {
        String hashedPassword = hashPassword(password);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/session/Create").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("passwordHash", hashedPassword);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Account Deletion
    public void accountDelete(String sessionID, Callback callback) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/Delete").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Authentication Deletion
    public void authDelete(String sessionID, int authID, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/auth/Delete").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);
        urlBuilder.addQueryParameter("authID", String.valueOf(authID));

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Get Authentication by Session
    public void authGetBySession(String sessionID, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/auth/GetBySession").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Register Authentication
    public void authRegister(String sessionID, AuthType authType, String authValue, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/auth/Register").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);
        urlBuilder.addQueryParameter("authType", String.valueOf(authType));
        urlBuilder.addQueryParameter("authValue", authValue);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void authExistsAny(String sessionID, Callback callback){
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/auth/OneDriveExistsAndExpired").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get() // Empty body for GET request
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void UpdateOneDrive(String sessionID, String authValue, Callback callback){
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/auth/Register").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);
        urlBuilder.addQueryParameter("authValue", authValue);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Assign Code (get code from api)
    public void codeAssign(String sessionID, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/otc/Assign").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Ping (for testing purposes ping the api if it works)
    public void ping(Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/Ping").newBuilder();

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Session Check (check if user has correct session it still exists etc to Sign In him automatically or not MainActivity)
    public void sessionCheck(String sessionID, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/session/Check").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // User Change Password
    public void userChangePassword(String sessionID, String oldPassword, String newPassword, Callback callback) {
        String oldHashedPassword = hashPassword(oldPassword);
        String newHashedPassword = hashPassword(newPassword);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/EP/ChangePassword").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);
        urlBuilder.addQueryParameter("passHashOld", oldHashedPassword);
        urlBuilder.addQueryParameter("passHashNew", newHashedPassword);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // User Verify Email (in VerificationCode to verify users email)
    public void userVerifyEmail(String email, String code, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/EP/VerifyEmail").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("code", code);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // User is Email verified (to check in Sign In process if user has verified email)
    public void isEmailVerified(String email, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/EP/IsVerified").newBuilder();
        urlBuilder.addQueryParameter("email", email);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // User Login/Password check (Sign In operation checks if user wrote correct credentials)
    public void userPasswordCheck(String email, String password, Callback callback) {
        String hashedPassword = hashPassword(password);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/EP/CheckPassword").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        urlBuilder.addQueryParameter("passwordHash", hashedPassword);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // User if user exists
    public void doesUserExist(String email, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/Exists").newBuilder();
        urlBuilder.addQueryParameter("email", email);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    // User Logout/session delete (at Log Out delete session, sessionID etc, used in SharedPrefsManager)
    public void sessionDelete(String sessionID, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/session/Delete").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // User Verify linked email
    public void authVerifyEmail(String sessionID, String authValue, String code, Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/auth/VerifyEmail").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);
        urlBuilder.addQueryParameter("authValue", authValue);
        urlBuilder.addQueryParameter("code", code);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getTransferHistory(String sessionID, Callback callback){
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/transfer/GetHistoryBySession").newBuilder();
        urlBuilder.addQueryParameter("sessionID", sessionID);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void resend_verify_email(String email, Callback callback)
    {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/api/user/EP/ResendVerificationEmail").newBuilder();
        urlBuilder.addQueryParameter("email", email);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0]))
                .build();

        client.newCall(request).enqueue(callback);
    }
}
