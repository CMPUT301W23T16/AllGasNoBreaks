package com.example.allgasnobrakes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains player profile information
 * @author zhaoyu4
 * @version 2.0
 */
public class PlayerProfile implements Serializable {
    private String username;
    private String email;
    private String password;
    private ArrayList<HashedQR> QRList = new ArrayList<>();
    private QRCounter profileSummary;

    public PlayerProfile(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public PlayerProfile(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public PlayerProfile(String username, String email, String password, int score, int count) {
        this.username = username;
        this.email = email;
        this.password = password;
        profileSummary = new QRCounter(score, count);
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<HashedQR> getQRList() {
        return QRList;
    }

    public QRCounter getProfileSummary() {
        return profileSummary;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setQRList(ArrayList<HashedQR> QRList) {
        this.QRList = QRList;
    }

    /**
     * Retrieves the collection of HashedQR that a player has collected and stores it locally. Also
     * notifies the view that displays this information to update itself with the latest data.
     * @param QrAdapter - the view to be updated
     */
    public void retrieveQR(RecyclerView.Adapter QrAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = db.collection("Users")
                .document(username).collection("QRRef");

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> QRS = new ArrayList<>();

                // We get the hashed value for each of QRs that the player has...
                for (QueryDocumentSnapshot QRRef : task.getResult()) {
                    DocumentReference hashedQR = db.document(QRRef.getString("QRReference"));
                    QRS.add(hashedQR.getId());
                }

                for (String i : QRS) {Log.d("Test", i);}

                // Then in the QR database, we retrieve the details of those QRs and store the data locally
                if (!QRS.isEmpty()) {
                    db.collection("/QR")
                            .whereIn("Hash", QRS)
                            .orderBy("Score", Query.Direction.DESCENDING)
                            .orderBy("Name", Query.Direction.ASCENDING)
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QRList.clear();
                                        int score = 0;
                                        for (QueryDocumentSnapshot QR : task.getResult()) {
                                            Log.d("GetQR", QR.getId() + " => " + QR.getData());
                                            String QRHash = QR.getId();
                                            String QRName = (String) QR.get("Name");
                                            Number QRScore = (Number) QR.get("Score");
                                            score += QRScore.intValue();
                                            QRList.add(new HashedQR(QRHash, QRScore.intValue(), QRName));
                                            // QRList.sort(new HashedQR().reversed());
                                            QrAdapter.notifyDataSetChanged(); // Notify the view to update
                                        }
                                        profileSummary.update(QRS.size(), score);
                                    } else {
                                        Log.d("GetQR", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            }
        });
        Log.d("Test", "Called");
    }

    public void deleteQR(String hash) {
        FirebaseFirestore.getInstance().collection("Users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                int count = ((Number) task.getResult().get("QR Count")).intValue();
                int score = ((Number) task.getResult().get("Total Score")).intValue();
                FirebaseFirestore.getInstance().collection("Users").document(username).update("QR Count", count-1);
            }
        });
        FirebaseFirestore.getInstance().collection("Users")
                .document(username).collection("QR")
                .document(hash).delete();
    }

    public void addQR(String hash) {
        FirebaseFirestore.getInstance().collection("Users")
                .document(username).collection("QR")
                .document(hash)
                .set(new HashMap<String, Object>(){
                    {put("QRReference", "QR/" + hash);}
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // These are a method which gets executed when the task is succeeded
                        Log.d("Add QR", "Data has been added successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // These are a method which gets executed if there’s any problem
                        Log.d("Add QR", "Data could not be added!" + e.toString());
                    }
                });
    }
}