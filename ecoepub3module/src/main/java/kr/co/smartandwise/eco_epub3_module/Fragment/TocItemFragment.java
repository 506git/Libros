package kr.co.smartandwise.eco_epub3_module.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import kr.co.smartandwise.eco_epub3_module.Activity.EpubContentListActivity;
import kr.co.smartandwise.eco_epub3_module.Activity.EpubViewerActivity;
import kr.co.smartandwise.eco_epub3_module.Model.TocItem;
import kr.co.smartandwise.eco_epub3_module.R;

import java.util.ArrayList;
import java.util.List;

public class TocItemFragment extends Fragment {

    static Bundle args;
    static ArrayList<TocItem> tocList = new ArrayList<TocItem>();

    public static TocItemFragment newInstance(ArrayList<TocItem> extTocList) {
        TocItemFragment fragment = new TocItemFragment();
        tocList = extTocList;
        args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public TocItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.tocList = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pager_item_list, container, false);

        TextView emptyList = (TextView)rootView.findViewById(android.R.id.empty);
        ListView itemList = (ListView)rootView.findViewById(android.R.id.list);

        emptyList.setText(getResources().getString(R.string.no_toc_item));
        itemList.setAdapter(new ChapterAdapter(getActivity(), R.layout.item_toc_item, tocList));
        itemList.setEmptyView(emptyList);

        return rootView;
    }

    class ChapterAdapter extends ArrayAdapter<TocItem> {
        Activity activity;
        LayoutInflater inflater;

        public ChapterAdapter(Activity activity, int layout, List<TocItem> tocList) {
            super(activity, layout, tocList);
            this.activity = activity;
            this.inflater = LayoutInflater.from(activity);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_toc_item, null);
            }

            final TocItem item = getItem(position);

            TextView listText = (TextView)convertView.findViewById(R.id.toc_text);

            listText.setText(item.getText());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, EpubViewerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("tocHref", item.getHref());
                    activity.setResult(EpubContentListActivity.TOC_RESULT_CODE, intent);
                    activity.finish();
                }
            });
            return convertView;
        }
    }
}
