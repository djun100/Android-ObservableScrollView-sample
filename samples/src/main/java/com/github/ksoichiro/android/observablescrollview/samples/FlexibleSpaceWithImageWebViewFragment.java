/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ksoichiro.android.observablescrollview.samples;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;

/**copied to fix java and xml*/
public class FlexibleSpaceWithImageWebViewFragment extends FlexibleSpaceWithImageBaseFragment<ObservableScrollView> {
    private View mWebViewContainer;
    WebView mWebView;
//    TextView mTv;
    private int mFlexibleSpaceHeight;
    ObservableScrollView scrollView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flexiblespacewithimagewebview, container, false);
        //webview
        mWebViewContainer = view.findViewById(R.id.webViewContainer);
        mWebView = (WebView) view.findViewById(R.id.webView);
        mWebView.loadUrl("file:///android_asset/lipsum.html");

        mFlexibleSpaceHeight = ((FlexibleSpaceWithImageWithViewPagerTabActivity)getActivity()).mFlexibleSpaceHeight;

        adjustTopMargin(mWebViewContainer,mFlexibleSpaceHeight);
        scrollView = (ObservableScrollView) view.findViewById(R.id.scroll);
        // TouchInterceptionViewGroup should be a parent view other than ViewPager.
        // This is a workaround for the issue #117:
        // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
        scrollView.setTouchInterceptionViewGroup((ViewGroup) view.findViewById(R.id.fragment_root));

        // Scroll to the specified offset after layout
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SCROLL_Y)) {
            final int scrollY = args.getInt(ARG_SCROLL_Y, 0);
//            ScrollUtils.addOnGlobalLayoutListener(mWebViewContainer, new Runnable() {
            ScrollUtils.addOnGlobalLayoutListener(scrollView, new Runnable() {
                @Override
                public void run() {

                    Log.e("","addOnGlobalLayoutListener scrollY:"+scrollY);
//                    scrollView.scrollTo(0, scrollY);
                    scrollView.scrollTo(0, (int)ScrollUtils.getFloat(scrollY,0,mFlexibleSpaceHeight));

                }
            });
            updateFlexibleSpace(scrollY, view);
        } else {
            updateFlexibleSpace(0, view);
        }

        scrollView.setScrollViewCallbacks(this);

/*        ScrollUtils.addOnGlobalLayoutListener(mWebViewContainer, new Runnable() {
            @Override
            public void run() {
//                updateFlexibleSpaceText(observableWebView.getCurrentScrollY());
            }
        });*/

        return view;
    }
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (getView() == null) {
            return;
        }
        updateFlexibleSpace(scrollY, getView());
    }
    @Override
    protected void updateFlexibleSpace(int scrollY) {
        com.cy.app.Log.e("webviewfragment updateFlexibleSpace(int scrollY) :"+scrollY);
        // Sometimes scrollable.getCurrentScrollY() and the real scrollY has different values.
        // As a workaround, we should call scrollVerticallyTo() to make sure that they match.
//        Scrollable s = getScrollable();
//        s.scrollVerticallyTo(scrollY);

        // If scrollable.getCurrentScrollY() and the real scrollY has the same values,
        // calling scrollVerticallyTo() won't invoke scroll (or onScrollChanged()), so we call it here.
        // Calling this twice is not a problem as long as updateFlexibleSpace(int, View) has idempotence.
        updateFlexibleSpace(scrollY, getView());
    }

    /**父类的onScrollChanged调用,只做父布局的flex处理*/
    @Override
    protected void updateFlexibleSpace(int scrollY, View view) {
        if (scrollY==mFlexibleSpaceHeight){
            int y= ((FlexibleSpaceWithImageWithViewPagerTabActivity)getActivity()).mPagerAdapter.getScrollY();
            scrollView.scrollTo(0, (int)ScrollUtils.getFloat(y,0,mFlexibleSpaceHeight));
            return;
        }
        Log.e("","webviewfragment updateFlexibleSpace 2param scrollY:"+scrollY);

        // Also pass this event to parent Activity
        FlexibleSpaceWithImageWithViewPagerTabActivity parentActivity =
                (FlexibleSpaceWithImageWithViewPagerTabActivity) getActivity();
        if (parentActivity != null) {
            parentActivity.onScrollChanged(scrollY, scrollView);
        }
    }

    /**设置FrameLayout中webview的margintop*/
    private void adjustTopMargin(View view, int topMargin) {
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();

        if (layoutParams.topMargin == topMargin) {
            return;
        }
        layoutParams.gravity= Gravity.TOP;
        layoutParams.topMargin = topMargin;

        view.setLayoutParams(layoutParams);
    }
}
