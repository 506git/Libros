package kr.co.smartandwise.eco_epub3_module.Activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;

import kr.co.smartandwise.eco_epub3_module.Fragment.BookmarkFragment;
import kr.co.smartandwise.eco_epub3_module.Fragment.HighlightFragment;
import kr.co.smartandwise.eco_epub3_module.Fragment.TocItemFragment;
import kr.co.smartandwise.eco_epub3_module.Model.Bookmark;
import kr.co.smartandwise.eco_epub3_module.Model.Highlight;
import kr.co.smartandwise.eco_epub3_module.Model.TocItem;
import kr.co.smartandwise.eco_epub3_module.R;

public class EpubContentListActivity extends AppCompatActivity {
    public static final int TOC_RESULT_CODE = 1;
    public static final int BOOKMARK_RESULT_CODE = 2;
    public static final int HIGHLIGHT_RESULT_CODE = 3;
    
    ImageView mTocItemIcon;
    ImageView mBookmarkIcon;
    ImageView mHighlightIcon;

    FrameLayout mTocItemTab;
    FrameLayout mBookmarkTab;
    FrameLayout mHighlightTab;

    ActionBar actionBar = null;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    ArrayList<TocItem> tocItemList = null;
    ArrayList<Bookmark> bookmarkList = null;
    ArrayList<Highlight> highlightList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_content_list);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue_theme)));
        actionBar.setTitle("");
        
        mTocItemIcon = (ImageView) findViewById(R.id.toc_item_icon);
        mBookmarkIcon = (ImageView) findViewById(R.id.bookmark_icon);
        mHighlightIcon = (ImageView) findViewById(R.id.highlight_icon);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(pagerOnPageChangeListener);

        mTocItemTab = (FrameLayout) findViewById(R.id.toc_item_tab);
        mTocItemTab.setOnClickListener(tabOnClikListener);
        mBookmarkTab = (FrameLayout) findViewById(R.id.bookmark_tab);
        mBookmarkTab.setOnClickListener(tabOnClikListener);
        mHighlightTab = (FrameLayout) findViewById(R.id.highlight_tab);
        mHighlightTab.setOnClickListener(tabOnClikListener);

        tocItemList = getIntent().getParcelableArrayListExtra("tocItemList");
        bookmarkList = getIntent().getParcelableArrayListExtra("bookmarkList");
        highlightList = getIntent().getParcelableArrayListExtra("highlightList");

        setTabSelected(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        actionBar = null;

        mTocItemIcon = null;
        mBookmarkIcon = null;
        mHighlightIcon = null;

        mTocItemTab = null;
        mBookmarkTab = null;
        mHighlightTab = null;

        mSectionsPagerAdapter = null;
        mViewPager = null;

        tocItemList = null;
        bookmarkList = null;
        highlightList = null;

        Runtime.getRuntime().gc();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ViewPager.SimpleOnPageChangeListener pagerOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            setTabSelected(position);
        }
    };

    View.OnClickListener tabOnClikListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.toc_item_tab) {
                setTabSelected(0);
            } else if (v.getId() == R.id.bookmark_tab) {
                setTabSelected(1);
            } else if (v.getId() == R.id.highlight_tab) {
                setTabSelected(2);
            }
        }
    };

    private void setTabSelected(int position) {
        mViewPager.setCurrentItem(position);
        actionBar.setTitle(mSectionsPagerAdapter.getPageTitle(position));

        switch (position) {
            case 0:
                mTocItemTab.setBackgroundResource(R.color.blue_theme);
                mBookmarkTab.setBackgroundResource(R.color.color_white);
                mHighlightTab.setBackgroundResource(R.color.color_white);

                mTocItemIcon.setImageResource(R.drawable.list_mock_on);
                mBookmarkIcon.setImageResource(R.drawable.list_mark_blue);
                mHighlightIcon.setImageResource(R.drawable.list_highlights_blue);
                break;
            case 1:
                mTocItemTab.setBackgroundResource(R.color.color_white);
                mBookmarkTab.setBackgroundResource(R.color.blue_theme);
                mHighlightTab.setBackgroundResource(R.color.color_white);

                mTocItemIcon.setImageResource(R.drawable.list_mock_blue);
                mBookmarkIcon.setImageResource(R.drawable.list_mark_on);
                mHighlightIcon.setImageResource(R.drawable.list_highlights_blue);
                break;
            case 2:
                mTocItemTab.setBackgroundResource(R.color.color_white);
                mBookmarkTab.setBackgroundResource(R.color.color_white);
                mHighlightTab.setBackgroundResource(R.color.blue_theme);

                mTocItemIcon.setImageResource(R.drawable.list_mock_blue);
                mBookmarkIcon.setImageResource(R.drawable.list_mark_blue);
                mHighlightIcon.setImageResource(R.drawable.list_highlights_on);
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.toc);
                case 1:
                    return getString(R.string.bookmark);
                case 2:
                    return getString(R.string.highlight);
            }
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TocItemFragment.newInstance(tocItemList);
                case 1:
                    return BookmarkFragment.newInstance(bookmarkList);
                case 2:
                    return HighlightFragment.newInstance(highlightList);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
