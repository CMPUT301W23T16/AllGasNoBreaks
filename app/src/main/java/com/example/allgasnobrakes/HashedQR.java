package com.example.allgasnobrakes;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

/**
 * Describes the hashed QR code
 * @author zhaoyu4 zhaoyu5
 * @version 2.0
 */
public class HashedQR implements Comparator<HashedQR>, Serializable {
    private final String hashedQR;
    private int score;
    private String name;
    private String face;
    private String comment;
    private Object lat;
    private Object lon;
    private boolean location;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Sort QR code by increasing score first, then alphabetically by name
     * @param o1 - the first HashedQR object to be compared.
     * @param o2 - the second HashedQR object to be compared.
     * @return - a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(HashedQR o1, HashedQR o2) {
        if (o1.score == o2.getScore()) {
            return o1.name.compareTo(o2.getName());
        } else {
            return o1.score - o2.getScore();
        }
    }

    /**
     * Constructor to initialize a dummy QR code
     */
    public HashedQR() {
        this.hashedQR = "hashedQR";
        this.score = 10;
        this.name = "name";
    }

    public HashedQR(String hashedQR, String comment, Object lat, Object lon) {
        this.hashedQR = hashedQR;
        this.comment = comment;
        this.lat = lat;
        this.lon = lon;

        FirebaseFirestore.getInstance().collection("QR").document(hashedQR)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot QRCode = task.getResult();
                            name = QRCode.get("Name", String.class);
                            face = QRCode.get("Face", String.class);
                            score = QRCode.get("Score", int.class);
                            Log.d("Score", String.format(Locale.CANADA, "%s" , QRCode.get("Score")));
                        }
                    }
                });
    }

    /**
     * Constructor to initialize a QR code with the specific information
     * @param hashedQR The hashed value of the QR code
     * @param score The score of the QR code
     * @param name The name of the QR
     * @param face The unique visual representation of the QR
     */
    public HashedQR(String hashedQR, int score, String name, String face) {
        this.hashedQR = hashedQR;
        this.score = score;
        this.name = name;
        this.face = face;
    }

    public HashedQR(String hashedQR, int score, String name, String face,
                    String comment, Object lat, Object lon) {

        this.hashedQR = hashedQR;
        this.score = score;
        this.name = name;
        this.face = face;
        this.comment = comment;
        this.lat = lat;
        this.lon = lon;

        if (lat.equals("")) {
            location = false;
        }

        DocumentReference QR = db.collection("QR").document(hashedQR);
        QR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();

                    if (! ds.exists()) {
                        QR.set(new HashMap<String, Object>(){{
                            put("Face", face);
                            put("Hash", hashedQR);
                            put("Name", name);
                            put("Score", score);
                            put("Owned by", new HashMap<>());
                        }});
                    }
                }
            }
        });

    }

    public String getHashedQR() {
        return hashedQR;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public String getFace() {
        return face;
    }

    public String getComment() {
        return comment;
    }

    public Object getLat() {
        return lat;
    }

    public Object getLon() {
        return lon;
    }
}
