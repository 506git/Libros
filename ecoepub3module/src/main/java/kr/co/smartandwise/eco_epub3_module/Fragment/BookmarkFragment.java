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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.smartandwise.eco_epub3_module.Activity.EpubContentListActivity;
import kr.co.smartandwise.eco_epub3_module.Activity.EpubViewerActivity;
import kr.co.smartandwise.eco_epub3_module.Model.Bookmark;
import kr.co.smartandwise.eco_epub3_module.R;

public class BookmarkFragment extends Fragment {

    static Bundle args;
    static ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();

    public static BookmarkFragment newInstance(ArrayList<Bookmark> extBookmarkList) {
        BookmarkFragment fragment = new BookmarkFragment();
        bookmarkList = extBookmarkList;
        args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public BookmarkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.bookmarkList = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pager_item_list, container, false);

        TextView emptyList = (TextView)rootView.findViewById(android.R.id.empty);
        ListView itemList = (ListView)rootView.findViewById(android.R.id.list);

        emptyList.setText(getResources().getString(R.string.no_bookmark));
        itemList.setAdapter(new ChapterAdapter(getActivity(), R.layout.item_bookmark, bookmarkList));
        itemList.setEmptyView(emptyList);

        return rootView;
    }

    class ChapterAdapter extends ArrayAdapter<Bookmark> {
        Activity activity;
        LayoutInflater inflater;

        public ChapterAdapter(Activity activity, int layout, List<Bookmark> bookmarkList) {
            super(activity, layout, bookmarkList);
            this.activity = activity;
            this.inflater = LayoutInflater.from(activity);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_bookmark, null);
            }

            final Bookmark item = getItem(position);

            TextView bookmarkDate = (TextView)convertView.findViewById(R.id.bookmark_date);
            TextView bookmarkText = (TextView)convertView.findViewById(R.id.bookmark_text);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
            Date createTime = new Date(item.getCreateTime());

            bookmarkDate.setText(sdf.format(createTime));
            bookmarkText.setText(item.getContent());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, EpubViewerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("bookmarkIdref", item.getIdref());
                    intent.putExtra("bookmarkCfi", item.getCfi());
                    activity.setResult(EpubContentListActivity.BOOKMARK_RESULT_CODE, intent);
                    activity.finish();
                }
            });
            return convertView;
        }
    }
}
