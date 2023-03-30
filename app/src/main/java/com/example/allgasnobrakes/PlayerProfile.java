package com.example.allgasnobrakes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Observable;

/**
 * Contains player profile information
 * @author zhaoyu4 zhaoyu5
 * @version 3.0
 */
public class PlayerProfile extends Observable implements Serializable, EventListener {
    private String username;
    private String email;
    private String password;
    private ArrayList<HashedQR> QRList = new ArrayList<>();
    private final QRCounter profileSummary = new QRCounter(0, 0);

    /**
     * Constructor without password, for searching for friends account
     * @param username The username of the account
     * @param email The email of the account
     */
    public PlayerProfile(String username, String email) {
        super();
        this.username = username;
        this.email = email;
    }

    /**
     * Constructor with password, for the user's account
     * @param username The username of the account
     * @param email The username of the account
     * @param password The password of the account
     */
    public PlayerProfile(String username, String email, String password) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Constructor with password, with initialization of player profile summary, for the user's
     * account
     * @param username The username of the account
     * @param email The username of the account
     * @param password The password of the account
     * @param score The total QR score of the player account
     * @param count The total number of the QR codes that account has
     */
    public PlayerProfile(String username, String email, String password, int score, int count) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        profileSummary.assign(count, score);
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
     * @param sortOrder - the order by which to sort the QR code
     */
    public void retrieveQR(RecyclerView.Adapter QrAdapter, String sortOrder) {
        Query.Direction order;

        if (sortOrder.equals("Highest Score")) {
            order = Query.Direction.DESCENDING;
        } else {
            order = Query.Direction.ASCENDING;
        }

        FirebaseFirestore.getInstance().collection("QR")
                .whereArrayContains("OwnedBy", username)
                .orderBy("Score", order)
                .orderBy("Name", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QRList.clear();

                        // We get the hashed value for each of QRs that the player has...
                        for (QueryDocumentSnapshot QR : task.getResult()) {
                            HashedQR newQR = new HashedQR(QR.getId(), QR.get("Score", int.class),
                                    QR.get("Name", String.class), QR.get("Face", String.class));

                            FirebaseFirestore.getInstance().document("/QR/" + QR.getId() + "/Players/" + username).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot meta = task.getResult();
                                            newQR.setComment(meta.get("Comment", String.class));
                                            newQR.setLat(meta.get("Lat"));
                                            newQR.setLat(meta.get("Lon"));

                                            QRList.add(newQR);

                                            QrAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                        // Notify the recycler view to update
                        setChanged();
                        notifyObservers();
                    }
                });
    }

    /**
     * Removed the QR code from the user's account
     * @param QR The QR code to be deleted
     */
    public void deleteQR(HashedQR QR) {
        FirebaseFirestore.getInstance().document("/QR/" + QR.getHashedQR() + "/Players/" + username).delete();
        FirebaseFirestore.getInstance().document("/QR/" + QR.getHashedQR())
                .update("OwnedBy", FieldValue.arrayRemove(username));

        profileSummary.update(getUsername(), -1, -QR.getScore());
        setChanged();
        notifyObservers();
    }

    /**
     * Implementation of the UNDO function in case the user deleted a QR code by accident
     * @param QR The QR code to be re-added
     */
    public void addQR(HashedQR QR) {
        profileSummary.update(getUsername(), 1, QR.getScore());
        setChanged();
        notifyObservers();

        HashMap<String, Object> meta = new HashMap<>();

        meta.put("Comment", QR.getComment());
        meta.put("Lat", QR.getLat());
        meta.put("Lon", QR.getLon());

        FirebaseFirestore.getInstance().collection("QR").document(QR.getHashedQR())
                .collection("Players").document(username)
                .set(meta);

        FirebaseFirestore.getInstance().collection("QR").document(QR.getHashedQR())
                .update("OwnedBy", FieldValue.arrayUnion(username));

        Log.d("update", "2");
    }
}