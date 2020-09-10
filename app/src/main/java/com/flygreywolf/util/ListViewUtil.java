package com.flygreywolf.util;

import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;

public class ListViewUtil {

    /**
     * 判断listView是否在底部
     *
     * @param listView
     * @return
     */
    public static boolean isListViewReachBottomEdge(final AbsListView listView) {
        boolean result = false;

        if (listView.getLastVisiblePosition() == -1) { // 第一条数据
            return true;
        }
        if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
            final View bottomChildView = listView.getChildAt(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
            result = (listView.getHeight() >= bottomChildView.getBottom());
        }
        return result;
    }

    public static void listViewSlideToBottom(final AbsListView listView, final Adapter adapter) {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(adapter.getCount() - 1);
            }
        });
    }
}
