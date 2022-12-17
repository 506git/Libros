package kr.co.smartandwise.eco_epub3_module.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import kr.co.smartandwise.eco_epub3_module.Activity.EpubContentListActivity;
import kr.co.smartandwise.eco_epub3_module.Activity.EpubViewerActivity;
import kr.co.smartandwise.eco_epub3_module.Model.Highlight;
import kr.co.smartandwise.eco_epub3_module.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HighlightFragment extends Fragment {

    static Bundle args;
    static ArrayList<Highlight> highlightList = new ArrayList<Highlight>();

    public static HighlightFragment newInstance(ArrayList<Highlight> extHighlightList) {
        HighlightFragment fragment = new HighlightFragment();
        highlightList = extHighlightList;
        args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public HighlightFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.highlightList = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pager_item_list, container, false);

        TextView emptyList = (TextView)rootView.findViewById(android.R.id.empty);
        ListView itemList = (ListView)rootView.findViewById(android.R.id.list);

        emptyList.setText(getResources().getString(R.string.no_highlight));
        itemList.setAdapter(new ChapterAdapter(getActivity(), R.layout.item_highlight, highlightList));
        itemList.setEmptyView(emptyList);

        return rootView;
    }

    class ChapterAdapter extends ArrayAdapter<Highlight> {
        Activity activity;
        LayoutInflater inflater;

        public ChapterAdapter(Activity activity, int layout, List<Highlight> highlightList) {
            super(activity, layout, highlightList);
            this.activity = activity;
            this.inflater = LayoutInflater.from(activity);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_highlight, null);
            }

            final Highlight item = getItem(position);


            LinearLayout listFrame = (LinearLayout)convertView.findViewById(R.id.highlight_layout);

            TextView highlightDate = (TextView)convertView.findViewById(R.id.highlight_date);
            TextView highlightText = (TextView)convertView.findViewById(R.id.highlight_text);
            TextView highlightMemo = (TextView)convertView.findViewById(R.id.highlight_memo);

            listFrame.setBackgroundColor(Color.parseColor(item.getColor()));
            listFrame.getBackground().setAlpha(51); // 51 is 20% (0 to 255)

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
            Date createTime = new Date(item.getCreateTime());

            highlightDate.setText(sdf.format(createTime));
            highlightText.setText(item.getContent());
            highlightMemo.setText(item.getMemo());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, EpubViewerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("highlightIdref", item.getIdref());
                    intent.putExtra("highlightCfi", item.getCfi());
                    activity.setResult(EpubContentListActivity.HIGHLIGHT_RESULT_CODE, intent);
                    activity.finish();
                }
            });
            return convertView;
        }
    }
}
