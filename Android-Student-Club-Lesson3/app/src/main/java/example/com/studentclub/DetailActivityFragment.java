package example.com.studentclub;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        // set the text of the textView contained in the fragment.
        ((TextView) rootView.findViewById(R.id.textView)).setText(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));


        return rootView;
    }
}
