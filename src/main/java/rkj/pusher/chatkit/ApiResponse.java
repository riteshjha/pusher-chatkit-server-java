package rkj.pusher.chatkit;

import org.json.JSONObject;

/**
 * Pusher Chatkit Java Server SDK
 * Format the api response
 * 
 * @author Ritesh Jha (mailrkj@gmail.com)
 * @version 0.1.0
 */
public class ApiResponse extends JSONObject {
   

    public ApiResponse(){
        try{
            this.put("status", 400);
            this.put("message", "");
        }catch(Exception e){}
    }

    /**
     * Set Api https status code
     * @param Integer status
     * @return ApiResponse
     */
    public ApiResponse setStatus(Integer status){
        try{
            this.put("status", status);
        }catch(Exception e){}
        return this;
    }

    /**
     * Set Api response message
     * @param String message
     * @return ApiResponse
     */
    public ApiResponse setMessage(String message){
        try{
            this.put("message", message);
        }catch(Exception e){}
        return this;
    }

    /**
     * Set Api payload
     * @param String key
     * @param String value
     * @return ApiResponse
     */
    public ApiResponse setPayload(String key, Object value){
        try{
            this.put(key, value);
        }catch(Exception e){}
        return this;
    }

    /**
     * Get Api http status code
     * @return Integer | null
     */
    public Integer getStatus(){
        try{
            return this.getInt("status");
        }catch(Exception e){}
        return null;
    }
}
