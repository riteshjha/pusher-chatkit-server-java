package rkj.pusher.chatkit;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;


/**
 * Pusher Chatkit Java Server SDK
 * Map<String, String> options = new HashMap<>()
 * options.put("instanceLocator", "");
 * options.put("key", "");
 * options.put("expireIn", ""); //value should be in seconds, this is optional, default one day
 * ChatKit chatkit = new ChatKit(options);
 * 
 * @author Ritesh Jha (mailrkj@gmail.com)
 * @version 0.0.1
 */
public class ChatKit {

    private String apiEndPoint; 
    private String instance = "";
    private String key = "";
    private String secret = "";
    private Date expireIn = null;
    private String token = null;

    /**
     * Instantiate Class with Options
     * @param Map options      Options to configure the Chatkit instance.
     *                         instanceLocator - your Chatkit instance locator
     *                         key - your Chatkit instance's key
     *                         expireIn (optional) - your Chatkit instance's key
     * 
     */
    public ChatKit(Map<String, String> options) throws Exception {

        if (!options.containsKey("instanceLocator")) {
            throw new Exception("You must provide an instance_locator");
        }

        if (!options.containsKey("key")) {
            throw new Exception("You must provide a key");
        }

        String[] instanceSplit = options.get("instanceLocator").split(":");
        String[] keySplit = options.get("key").split(":");
        
        if(instanceSplit.length != 3) throw new MissingFormatArgumentException("v1:us1:instance");
        if(keySplit.length != 2) throw new MissingFormatArgumentException("key:secret");

        this.instance = instanceSplit[2];
        this.key = keySplit[0];
        this.secret = keySplit[1];

        //construct endpoint
        this.apiEndPoint = "https://us1.pusherplatform.io/services/chatkit/v1/" + this.instance + "/" ;

        if (options.containsKey("expireIn")) {
            this.expireIn = getDateFromMinute( Long.parseLong(options.get("expireIn")));
        }else{
            this.expireIn = getDateFromMinute( 24 * 60 * 60 ); // 24 hours
        }
    }

    /**
     * Convert Minute to Java Date
     * @param Long minute
     * @return Date
     */
    protected Date getDateFromMinute(long seconds) {
        long ttlMillis = seconds * 1000; //convert to milliseconds
        long expMillis = System.currentTimeMillis() + ttlMillis;
        return new Date(expMillis);
    }

    /**
     * Main Get api request
     * @param String service
     * @return JsonResponse
     */
    protected ApiResponse apiRequest(String service) throws Exception {
        return apiRequest(service, "get", null);
    }

    /**
     * Main api request
     * @param String service
     * @param String method
     * @param Map requestData
     * @return
     */
    protected ApiResponse apiRequest(String service, String method, Map<String, Object> requestData) throws Exception {
        String requestUrl = apiEndPoint + service;
        //System.out.println(method + "<=>" + requestUrl);
        ApiResponse apiResponse = null;

        switch (method.toLowerCase()){
            case "post":
            case "put":
            case "delete":
                apiResponse = requestWithBody(requestUrl, requestData, method);
                break;
            case "get":
                apiResponse = getRequest(requestUrl);
                break;
            default:
                apiResponse = null;
                break;
        }
       
        return apiResponse;
    }

    /**
     * Chatkit Get request api
     * @param String requestUrl
     * @return JsonResponse 
     */
    protected ApiResponse getRequest(String requestUrl) throws Exception {
        GetRequest getRequest = Unirest.get(requestUrl);

        HttpResponse<JsonNode> response = getRequest
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .asJson();

        return (response != null) ? handleResponse(response) : null;
    }


    /**
     * Handle CHatkit POST api
     * @param String requestUrl
     * @param Map requestData
     * @return JsonResponse | null
     */
    protected ApiResponse requestWithBody(String requestUrl, Map<String, Object> requestData, String method) throws Exception {
        HttpRequestWithBody requestWithBody = null;
        
        if(method.equals("put")){
            requestWithBody = Unirest.put(requestUrl);
        }else if(method.equals("delete")){
            requestWithBody = Unirest.delete(requestUrl);
        }else{
            requestWithBody = Unirest.post(requestUrl);
        }

        requestWithBody
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json");

        if(requestData != null){
            JSONObject json = new JSONObject(requestData);
            requestWithBody.body(json.toString());
        }
        
        HttpResponse<JsonNode> response = requestWithBody.asJson();

        return (response != null) ? handleResponse(response) : null;
    }

    /**
     * Handle Chatkit Api response
     * @param HttpResponse<com.mashape.unirest.http.JsonNode response
     * @return JsonResponse
     */
    protected ApiResponse handleResponse(HttpResponse<JsonNode> response) throws JSONException {
        //System.out.println("Status => " + response.getStatus());
        ApiResponse apiResponse = new ApiResponse();
        
        if (response.getStatus() == 201 || response.getStatus() == 200 || response.getStatus() == 204) {
            apiResponse.setStatus(200)
                        .setPayload("payload", handleResponseData(response.getBody()));
                    
        } else {
            JSONObject responseBody = response.getBody().getObject();
            apiResponse.setStatus(response.getStatus())
                    .setPayload("error", responseBody.optString("error"))
                    .setMessage(responseBody.optString("error_description"));
        }
        
        return apiResponse;
    }

    /**
     * Format chatkit response data
     * @param com.mashape.unirest.http.JsonNode responseBody
     * @return null | Object
     */
    protected Object handleResponseData(JsonNode responseBody) throws JSONException {
        if(responseBody == null){
            return null;
        } else if(responseBody.isArray()){
            JSONArray data = responseBody.getArray();
            List<JSONObject> results = new ArrayList<JSONObject>();
            for (int i = 0; i < data.length(); i++) {
                results.add((JSONObject)data.get(i));
            }
            return results;
        }else{
            return responseBody.getObject();
        }
    }

    /**
     * Sample method to construct a JWT
     * @param String subject
     * @param boolen su
     */
    protected String generateToken( String userId, boolean su) {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", signatureAlgorithm);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = secret.getBytes();
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        String issueString = "api_keys/"+ key;
        JwtBuilder builder = Jwts.builder()
                                    .setHeader(header)
                                    .setIssuedAt(new Date())
                                    .setExpiration(expireIn)
                                    .setIssuer(issueString)
                                    .claim("instance", instance)
                                    .signWith(signatureAlgorithm, signingKey);

        if(userId != null){
            builder.setSubject(userId);
        }
                                    
        if (su){
            builder.claim("su", true);
        }

        return builder.compact();
    }

    /**
     * Generate Admin token
     * @return String
     */
    protected String serverToken(){
        return generateToken(null, true);
    }

    /**
     * Autenticate chatkit user
     * @param String userId
     * @return JsonResponse
     */
    public ApiResponse authenticate( String userId ) throws Exception {
        if(userId == null){
            throw new Exception("You must provide a user id");
        } 

        String accessToken = generateToken(userId, false);
        ApiResponse responseBody = new ApiResponse();
        responseBody.setStatus(200)
                    .setPayload("access_token", accessToken)
                    .setPayload("token_type", "access_token")
                    .setPayload("expires_in", (long) expireIn.getTime()/1000)
                    .setPayload("user_id", userId);
    
        return responseBody;
    }

    /**
     * Get Chatkit user
     * @param String userId
     * @return JsonResponse
     */
    public ApiResponse getUser(String userId) throws Exception {
        token = serverToken();
        return apiRequest("users/"+ userId);
    }

     /**
     * Create Chatkit user
     * @param String userId
     * @param Map data
     * @return JsonResponse
     */
    public ApiResponse createUser(String userId, Map<String, Object> data) throws Exception {
        token = serverToken();

        if (userId == null) {
            throw new Exception("You must provide an id");
        }

        if (!data.containsKey("name") || data.get("name").toString().length() == 0 ) {
            throw new Exception("You must provide a name");
        }

        data.put("id", userId);
        return apiRequest("users", "post", data);
    }

    /**
     * Update Chatkit user
     * @param String userId
     * @param Map data
     * @return JsonResponse
     */
    public ApiResponse updateUser(String userId, Map<String, Object> data) throws Exception {

        if (userId == null) {
            throw new Exception("You must provide an id");
        }

        token = generateToken(userId, true);
        return apiRequest("users/"+ userId, "put", data);
    }

    /**
     * Delete Chatkit user
     * @param String userId
     * @return JsonResponse
     */
    public ApiResponse deleteUser(String userId) throws Exception {
        token = serverToken();
        return apiRequest("users/"+ userId, "delete", null);
    }

    /**
     * Create chatkit room
     * @param String creatorId
     * @param Map data
     * @return JsonResponse
     */
    public ApiResponse createRoom(String creatorId, Map<String, Object> data) throws Exception {

        if (creatorId == null ) {
            throw new Exception("You must provide the id of the user that you wish to create the room");
        }

        data.put("creator_id", creatorId);
        token = generateToken(creatorId, true);
        return apiRequest("rooms", "post", data);
    }    
}
