package here.com;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;


import com.here.android.mpa.mapping.SupportMapFragment;

public class AuthFragment extends Fragment {

    private SupportMapFragment m_fragment;
    FrameLayout authLayout;
    Button btnStart;


    public AuthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        final View authView = inflater.inflate(R.layout.fragment_auth, container, false);

        authLayout = authView.findViewById(R.id.frameLayout);
        btnStart = authView.findViewById(R.id.btn_start);

        btnStart.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                authLayout.setVisibility(View.INVISIBLE);
                m_fragment = MapFragmentView.m_mapFragment;
                m_fragment.getMapGesture().setAllGesturesEnabled(true);
            }
        });

        return authView;
    }

}
