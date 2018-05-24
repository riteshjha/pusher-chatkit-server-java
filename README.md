# Pusher Chatkit JAVA Server SDK
Find out more about Chatkit [here](https://pusher.com/chatkit).

## Installation
Place the jar file from dist folder to your project lib folder.

## Chatkit Instance
Go to your pusher chatkit dashboard and find your `instance_locator` and `key` and use it to create Chatkit instance

```java

    Map<String, String> options = new HashMap<>();
    options.put("instanceLocator", "YOUR_INSTANCE_LOCATOR");
    options.put("key", "YOUR_KEY");

    //value in seconds, this is optional, default value is (24 * 60 * 60) seconds means 24 hours
    options.put("expireIn", 3600);
    ChatKit chatKit = new ChatKit(options);

```

## Authentication
To authenticate a user (a Chatkit client) or you can say AWT token, use the `authenticate` function.

```java

    String userId = "someuser"; //the existing user at chatkit
    ApiResponse responseBody = new ApiResponse();
    try {
        responseBody = chatKit.authenticate(userId);
    }catch(Exception e){
        System.out.println(e.getMessage());
    }

    //just check the response
    System.out.println(responseBody.toString());

```

## Creating and Updating user
To create a user you must provide an `id` and a `name`. You can optionally provide an `avatar_url (String)` and `custom_data (Map<String,String>)`.

```java

    String userId = "someuser"; 
    Map<String, Object> data = new HashMap<>();
    Map<String, String> customData = new HashMap<>();
    customData.put("email", getEmail());

    data.put("id", userId);
    data.put("name", "Some User");
    data.put("avatar_url", "avatar url");
    data.put("custom_data", customData);

    try{
        ApiResponse apiResponse = chatKit.getUser(userId); //check user already created
        if(apiResponse.getStatus() == 404){
            chatKit.createUser(data);
        }else{
            chatKit.updateUser(data);
        }
    }catch(Exception e){
        System.out.println(e.getMessage());
    }

```