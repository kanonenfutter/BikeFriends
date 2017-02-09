package eis.bikefriends;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by preim on 1/15/2017.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService{

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        registerToken(token);
    }

    private void registerToken(String token) {
        //// TODO: 1/15/2017  request register Token Database

    }
}
