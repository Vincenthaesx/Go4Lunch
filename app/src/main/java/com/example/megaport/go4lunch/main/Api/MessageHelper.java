package com.example.megaport.go4lunch.main.Api;

import com.example.megaport.go4lunch.main.Models.Message;
import com.example.megaport.go4lunch.main.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

public class MessageHelper {

    // --- CREATE ---

    public static Task<DocumentReference> createMessageForChat(String textMessage, User userSender){

        // Create the Message object
        Message message = new Message(textMessage, userSender);

        // Store Message to Firestore
        return ChatHelper.getChatCollection()
                .add(message);
    }

    public static Task<DocumentReference> createMessageWithImageForChat(String urlImage, String textMessage, User userSender){

        // Creating Message with the URL image
        Message message = new Message(textMessage, urlImage, userSender);

        // Storing Message on Firestore
        return ChatHelper.getChatCollection()
                .add(message);
    }

    // --- GET ---

    public static Query getAllMessageForChat(){
        return ChatHelper.getChatCollection()
                .orderBy("dateCreated")
                .limit(50);
    }


}
