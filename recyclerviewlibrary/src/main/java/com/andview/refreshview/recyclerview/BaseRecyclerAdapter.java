package com.andview.refreshview.recyclerview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.andview.refreshview.R;
import com.andview.refreshview.callback.IFooterCallBack;
import com.andview.refreshview.swipe.OnSwipeMenuItemClickListener;
import com.andview.refreshview.swipe.SwipeMenu;
import com.andview.refreshview.swipe.SwipeMenuCreator;
import com.andview.refreshview.swipe.SwipeMenuLayout;
import com.andview.refreshview.swipe.SwipeMenuRecyclerView;
import com.andview.refreshview.swipe.SwipeMenuView;
import com.andview.refreshview.utils.LogUtils;
import com.andview.refreshview.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract adapter which can be extended for Recyclerview
 */
public abstract class BaseRecyclerAdapter<T,VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {


    protected View customLoadMoreView = null;
    protected View customHeaderView = null;
    private boolean isFooterEnable = true;
    protected List<T> datas = new ArrayList<T>();

    public List<T> getDatas() {
        if (datas==null)
            datas = new ArrayList<T>();
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    private void showFooter(View footerview, boolean show) {
        if (isFooterEnable && footerview != null && footerview instanceof IFooterCallBack) {
            IFooterCallBack footerCallBack = (IFooterCallBack) footerview;
            if (show) {
                if (!footerCallBack.isShowing()) {
                    footerCallBack.show(show);
                }
            } else {
                if (getAdapterItemCount() == 0 && footerCallBack.isShowing()) {
                    footerCallBack.show(false);
                } else if (getAdapterItemCount() != 0 && !footerCallBack.isShowing()) {
                    footerCallBack.show(true);
                }
            }
        }
    }
    private boolean removeFooter = false;

    public void addFooterView() {
        LogUtils.d("test addFooterView");
        if (removeFooter) {
            notifyItemInserted(getItemCount());
            removeFooter = false;
            showFooter(customLoadMoreView, true);
        }
    }

    public boolean isFooterShowing() {
        return !removeFooter;
    }

    public void removeFooterView() {
        LogUtils.d("test removeFooterView");
        if (!removeFooter) {
            notifyItemRemoved(getItemCount() - 1);
            removeFooter = true;
        }
    }

    public abstract VH getViewHolder(View view, int viewType);

    /**
     * @param parent
     * @param viewType
     * @param isItem   如果是true，才需要做处理 ,但是这个值总是true
     */
//    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem);

    /**
     * 替代onBindViewHolder方法，实现这个方法就行了
     *
     * @param holder
     * @param position
     */
    public abstract void onBindViewHolder(VH holder, int position, boolean isItem);

    public abstract View onCreateContentView(ViewGroup parent, int viewType);

    public abstract VH onCompatCreateViewHolder(View realContentView, int viewType);

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {

        showFooter(customLoadMoreView, false);
        if (viewType == VIEW_TYPES.FOOTER) {
            Utils.removeViewFromParent(customLoadMoreView);
            VH viewHolder = getViewHolder(customLoadMoreView,viewType);
            return viewHolder;
        } else if (viewType == VIEW_TYPES.HEADER) {
            Utils.removeViewFromParent(customHeaderView);
            VH viewHolder = getViewHolder(customHeaderView,viewType);
            return viewHolder;
        }

        View contentView = onCreateContentView(parent, viewType);//返回当前的布局情况
        if (mSwipeMenuCreator != null) {
            SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.yanzhenjie_item_default, parent, false);

            SwipeMenu swipeLeftMenu = new SwipeMenu(swipeMenuLayout, viewType);
            SwipeMenu swipeRightMenu = new SwipeMenu(swipeMenuLayout, viewType);

            mSwipeMenuCreator.onCreateMenu(swipeLeftMenu, swipeRightMenu, viewType);

            int leftMenuCount = swipeLeftMenu.getMenuItems().size();
            if (leftMenuCount > 0) {
                SwipeMenuView swipeLeftMenuView = (SwipeMenuView) swipeMenuLayout.findViewById(R.id.swipe_left);
                swipeLeftMenuView.setOrientation(swipeLeftMenu.getOrientation());
                swipeLeftMenuView.bindMenu(swipeLeftMenu, SwipeMenuRecyclerView.LEFT_DIRECTION);
                swipeLeftMenuView.bindMenuItemClickListener(mSwipeMenuItemClickListener, swipeMenuLayout);
            }

            int rightMenuCount = swipeRightMenu.getMenuItems().size();
            if (rightMenuCount > 0) {
                SwipeMenuView swipeRightMenuView = (SwipeMenuView) swipeMenuLayout.findViewById(R.id.swipe_right);
                swipeRightMenuView.setOrientation(swipeRightMenu.getOrientation());
                swipeRightMenuView.bindMenu(swipeRightMenu, SwipeMenuRecyclerView.RIGHT_DIRECTION);
                swipeRightMenuView.bindMenuItemClickListener(mSwipeMenuItemClickListener, swipeMenuLayout);
            }

            if (leftMenuCount > 0 || rightMenuCount > 0) {
                ViewGroup viewGroup = (ViewGroup) swipeMenuLayout.findViewById(R.id.swipe_content);
                viewGroup.addView(contentView);
                contentView = swipeMenuLayout;

            }
        }

        return onCompatCreateViewHolder(contentView, viewType);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        View itemView = holder.itemView;
        if (itemView instanceof SwipeMenuLayout) {
            SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) itemView;
            int childCount = swipeMenuLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = swipeMenuLayout.getChildAt(i);
                if (childView instanceof SwipeMenuView) {
                    ((SwipeMenuView) childView).bindAdapterViewHolder(holder);
                }
            }
        }

        int start = getStart();
        if (!isHeader(position) && !isFooter(position)) {
            onBindViewHolder(holder, position - start, true);
        }
    }

    @Override
    public void onViewAttachedToWindow(VH holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(isFooter(position));
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
//        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
//        if (manager instanceof GridLayoutManager) {
//            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
//            gridManager.setSpanSizeLookup(new XSpanSizeLookup(this, ((GridLayoutManager) manager).getSpanCount()));
//        }
    }

    /**
     * Using a custom LoadMoreView
     *
     * @param footerView the inflated view
     */
    public void setCustomLoadMoreView(View footerView) {
        Utils.removeViewFromParent(customLoadMoreView);
        if (footerView instanceof IFooterCallBack) {
            customLoadMoreView = footerView;
            showFooter(customLoadMoreView, false);
            notifyDataSetChanged();
        } else {
            throw new RuntimeException("footerView must be implementes IFooterCallBack!");
        }
    }

    public void setHeaderView(View headerView, RecyclerView recyclerView) {
        if (recyclerView == null) return;
        Utils.removeViewFromParent(customLoadMoreView);
        customHeaderView = headerView;
        notifyDataSetChanged();
    }

    public View setHeaderView(@LayoutRes int id, RecyclerView recyclerView) {

        if (recyclerView == null) return null;
        Context context = recyclerView.getContext();
        String resourceTypeName = context.getResources().getResourceTypeName(id);
        if (!resourceTypeName.contains("layout")) {
            throw new RuntimeException(context.getResources().getResourceName(id) + " is a illegal layoutid , please check your layout id first !");
        }
        FrameLayout headerview = new FrameLayout(recyclerView.getContext());
        customHeaderView = LayoutInflater.from(context).inflate(id, headerview, false);
        notifyDataSetChanged();
        return customHeaderView;
    }

    public boolean isFooter(int position) {
        int start = getStart();
        return customLoadMoreView != null && position >= getAdapterItemCount() + start;
    }

    public boolean isHeader(int position) {
        return getStart() > 0 && position == 0;
    }

    public View getCustomLoadMoreView() {
        return customLoadMoreView;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isHeader(position)) {
            return VIEW_TYPES.HEADER;
        } else if (isFooter(position)) {
            return VIEW_TYPES.FOOTER;
        } else {
            position = getStart() > 0 ? position - 1 : position;
            return getAdapterItemViewType(position);
        }
    }

    /**
     * 实现此方法来设置viewType
     *
     * @param position
     * @return viewType
     */
    public int getAdapterItemViewType(int position) {
        return VIEW_TYPES.NORMAL;
    }

    public int getStart() {
        return customHeaderView == null ? 0 : 1;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public final int getItemCount() {
        int count = getAdapterItemCount();
        count += getStart();
        if (customLoadMoreView != null && !removeFooter) {
            count++;
        }
        return count;
    }

    /**
     * Returns the number of items in the adapter bound to the parent
     * RecyclerView.
     *
     * @return The number of items in the bound adapter
     */
    public abstract int getAdapterItemCount();

    /**
     * Swap the item of list
     *
     * @param list data list
     * @param from position from
     * @param to   position to
     */
    public void swapPositions(List<?> list, int from, int to) {
        Collections.swap(list, from, to);
    }

    public void insideEnableFooter(boolean enable) {
        isFooterEnable = enable;
    }

    /**
     * Insert a item to the list of the adapter
     *
     * @param list     data list
     * @param object   object T
     * @param position position
     * @param <T>      in T
     */
    public <T> void insert(List<T> list, T object, int position) {
        list.add(position, object);
        notifyItemInserted(position + getStart());
    }

    /**
     * Remove a item of the list of the adapter
     *
     * @param list     data list
     * @param position position
     */
    public void remove(List<?> list, int position) {
        if (list.size() > 0) {
            notifyItemRemoved(position + getStart());
        }
    }

    /**
     * Clear the list of the adapter
     *
     * @param list data list
     */
    public void clear(List<?> list) {
        int start = getStart();
        int size = list.size() + start;
        list.clear();
        notifyItemRangeRemoved(start, size);
    }

    protected class VIEW_TYPES {
        public static final int FOOTER = -1;
        public static final int HEADER = -3;
        public static final int NORMAL = -4;
    }


    /**
     * 侧滑删除数据结构
     * */
    /**
     * Swipe menu creator。
     */
    private SwipeMenuCreator mSwipeMenuCreator;

    /**
     * Swipe menu click listener。
     */
    private OnSwipeMenuItemClickListener mSwipeMenuItemClickListener;

    /**
     * Set to create menu listener.
     *
     * @param swipeMenuCreator listener.
     */
    public void setSwipeMenuCreator(SwipeMenuCreator swipeMenuCreator) {
        this.mSwipeMenuCreator = swipeMenuCreator;
    }

    /**
     * Set to click menu listener.
     *
     * @param swipeMenuItemClickListener listener.
     */
    public void setSwipeMenuItemClickListener(OnSwipeMenuItemClickListener swipeMenuItemClickListener) {
        this.mSwipeMenuItemClickListener = swipeMenuItemClickListener;
    }


    /**
     * 自定义接口
     * */
    public RecyclerViewClick recyclerViewItemListener;

    public interface RecyclerViewClick{
        void onItemClick(int position);
    }

    public void setOnRecyclerItemListener(RecyclerViewClick recyclerViewItemListener){
        this.recyclerViewItemListener=recyclerViewItemListener;
    }

    /**
     * 原始数据
     * */
//    protected View customLoadMoreView = null;
//    protected View customHeaderView = null;
//    private boolean isFooterEnable = true;
//    protected List<T> datas = new ArrayList<T>();
//
//    public List<T> getDatas() {
//        if (datas==null)
//            datas = new ArrayList<T>();
//        return datas;
//    }
//
//    public void setDatas(List<T> datas) {
//        this.datas = datas;
//    }
//
//    @Override
//    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
//        showFooter(customLoadMoreView, false);
//        if (viewType == VIEW_TYPES.FOOTER) {
//            Utils.removeViewFromParent(customLoadMoreView);
//            VH viewHolder = getViewHolder(customLoadMoreView);
//            return viewHolder;
//        } else if (viewType == VIEW_TYPES.HEADER) {
//            Utils.removeViewFromParent(customHeaderView);
//            VH viewHolder = getViewHolder(customHeaderView);
//            return viewHolder;
//        }
//        return onCreateViewHolder(parent, viewType, true);
//    }
//
//    private void showFooter(View footerview, boolean show) {
//        if (isFooterEnable && footerview != null && footerview instanceof IFooterCallBack) {
//            IFooterCallBack footerCallBack = (IFooterCallBack) footerview;
//            if (show) {
//                if (!footerCallBack.isShowing()) {
//                    footerCallBack.show(show);
//                }
//            } else {
//                if (getAdapterItemCount() == 0 && footerCallBack.isShowing()) {
//                    footerCallBack.show(false);
//                } else if (getAdapterItemCount() != 0 && !footerCallBack.isShowing()) {
//                    footerCallBack.show(true);
//                }
//            }
//        }
//    }
//    private boolean removeFooter = false;
//
//    public void addFooterView() {
//        LogUtils.d("test addFooterView");
//        if (removeFooter) {
//            notifyItemInserted(getItemCount());
//            removeFooter = false;
//            showFooter(customLoadMoreView, true);
//        }
//    }
//
//    public boolean isFooterShowing() {
//        return !removeFooter;
//    }
//
//    public void removeFooterView() {
//        LogUtils.d("test removeFooterView");
//        if (!removeFooter) {
//            notifyItemRemoved(getItemCount() - 1);
//            removeFooter = true;
//        }
//    }
//
//    public abstract VH getViewHolder(View view);
//
//    /**
//     * @param parent
//     * @param viewType
//     * @param isItem   如果是true，才需要做处理 ,但是这个值总是true
//     */
//    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem);
//
//    /**
//     * 替代onBindViewHolder方法，实现这个方法就行了
//     *
//     * @param holder
//     * @param position
//     */
//    public abstract void onBindViewHolder(VH holder, int position, boolean isItem);
//
//    @Override
//    public final void onBindViewHolder(VH holder, int position) {
//        int start = getStart();
//        if (!isHeader(position) && !isFooter(position)) {
//            onBindViewHolder(holder, position - start, true);
//        }
//    }
//
//    @Override
//    public void onViewAttachedToWindow(VH holder) {
//        super.onViewAttachedToWindow(holder);
//        int position = holder.getLayoutPosition();
//        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
//        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
//            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
//            p.setFullSpan(isFooter(position));
//        }
//    }
//
//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
////        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
////        if (manager instanceof GridLayoutManager) {
////            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
////            gridManager.setSpanSizeLookup(new XSpanSizeLookup(this, ((GridLayoutManager) manager).getSpanCount()));
////        }
//    }
//
//    /**
//     * Using a custom LoadMoreView
//     *
//     * @param footerView the inflated view
//     */
//    public void setCustomLoadMoreView(View footerView) {
//        Utils.removeViewFromParent(customLoadMoreView);
//        if (footerView instanceof IFooterCallBack) {
//            customLoadMoreView = footerView;
//            showFooter(customLoadMoreView, false);
//            notifyDataSetChanged();
//        } else {
//            throw new RuntimeException("footerView must be implementes IFooterCallBack!");
//        }
//    }
//
//    public void setHeaderView(View headerView, RecyclerView recyclerView) {
//        if (recyclerView == null) return;
//        Utils.removeViewFromParent(customLoadMoreView);
//        customHeaderView = headerView;
//        notifyDataSetChanged();
//    }
//
//    public View setHeaderView(@LayoutRes int id, RecyclerView recyclerView) {
//
//        if (recyclerView == null) return null;
//        Context context = recyclerView.getContext();
//        String resourceTypeName = context.getResources().getResourceTypeName(id);
//        if (!resourceTypeName.contains("layout")) {
//            throw new RuntimeException(context.getResources().getResourceName(id) + " is a illegal layoutid , please check your layout id first !");
//        }
//        FrameLayout headerview = new FrameLayout(recyclerView.getContext());
//        customHeaderView = LayoutInflater.from(context).inflate(id, headerview, false);
//        notifyDataSetChanged();
//        return customHeaderView;
//    }
//
//    public boolean isFooter(int position) {
//        int start = getStart();
//        return customLoadMoreView != null && position >= getAdapterItemCount() + start;
//    }
//
//    public boolean isHeader(int position) {
//        return getStart() > 0 && position == 0;
//    }
//
//    public View getCustomLoadMoreView() {
//        return customLoadMoreView;
//    }
//
//    @Override
//    public final int getItemViewType(int position) {
//        if (isHeader(position)) {
//            return VIEW_TYPES.HEADER;
//        } else if (isFooter(position)) {
//            return VIEW_TYPES.FOOTER;
//        } else {
//            position = getStart() > 0 ? position - 1 : position;
//            return getAdapterItemViewType(position);
//        }
//    }
//
//    /**
//     * 实现此方法来设置viewType
//     *
//     * @param position
//     * @return viewType
//     */
//    public int getAdapterItemViewType(int position) {
//        return VIEW_TYPES.NORMAL;
//    }
//
//    public int getStart() {
//        return customHeaderView == null ? 0 : 1;
//    }
//
//    /**
//     * Returns the total number of items in the data set hold by the adapter.
//     *
//     * @return The total number of items in this adapter.
//     */
//    @Override
//    public final int getItemCount() {
//        int count = getAdapterItemCount();
//        count += getStart();
//        if (customLoadMoreView != null && !removeFooter) {
//            count++;
//        }
//        return count;
//    }
//
//    /**
//     * Returns the number of items in the adapter bound to the parent
//     * RecyclerView.
//     *
//     * @return The number of items in the bound adapter
//     */
//    public abstract int getAdapterItemCount();
//
//    /**
//     * Swap the item of list
//     *
//     * @param list data list
//     * @param from position from
//     * @param to   position to
//     */
//    public void swapPositions(List<?> list, int from, int to) {
//        Collections.swap(list, from, to);
//    }
//
//    public void insideEnableFooter(boolean enable) {
//        isFooterEnable = enable;
//    }
//
//    /**
//     * Insert a item to the list of the adapter
//     *
//     * @param list     data list
//     * @param object   object T
//     * @param position position
//     * @param <T>      in T
//     */
//    public <T> void insert(List<T> list, T object, int position) {
//        list.add(position, object);
//        notifyItemInserted(position + getStart());
//    }
//
//    /**
//     * Remove a item of the list of the adapter
//     *
//     * @param list     data list
//     * @param position position
//     */
//    public void remove(List<?> list, int position) {
//        if (list.size() > 0) {
//            notifyItemRemoved(position + getStart());
//        }
//    }
//
//    /**
//     * Clear the list of the adapter
//     *
//     * @param list data list
//     */
//    public void clear(List<?> list) {
//        int start = getStart();
//        int size = list.size() + start;
//        list.clear();
//        notifyItemRangeRemoved(start, size);
//    }
//
//    protected class VIEW_TYPES {
//        public static final int FOOTER = -1;
//        public static final int HEADER = -3;
//        public static final int NORMAL = -4;
//    }
}