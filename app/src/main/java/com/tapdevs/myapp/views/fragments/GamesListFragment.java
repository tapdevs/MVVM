package com.tapdevs.myapp.views.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tapdevs.myapp.MyApp;
import com.tapdevs.myapp.R;
import com.tapdevs.myapp.data.DataManager;
import com.tapdevs.myapp.models.GameData;
import com.tapdevs.myapp.models.GameObject;
import com.tapdevs.myapp.utils.DialogFactory;
import com.tapdevs.myapp.utils.GridAutofitLayoutManager;
import com.tapdevs.myapp.views.activitys.MainActivity;
import com.tapdevs.myapp.views.adapters.GamesAdapter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static com.tapdevs.myapp.R.layout.fragment_games_list;

/**
 * Created by  Jan Shair on 15/02/2017.
 */

public class GamesListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {


    private MainActivity context;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;


    private CompositeDisposable mCompositeDisposable;

    private GamesAdapter mAdapter;

    private List<GameObject> gameDatas;
    @BindView(R.id.swipe_container)
    SwipeRefreshLayout mSwipeRefreshLayout;

    

    @BindView(R.id.layout_offline)
    LinearLayout mOfflineContainer;

    @BindView(R.id.progress_indicator)
    ProgressBar mProgressBar;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_error)
    TextView errorView;


    @Inject DataManager mDataManager;
    private String currency;


    public String getCurrency(){
        return currency;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=(MainActivity)getActivity();
        mCompositeDisposable = new CompositeDisposable();
        gameDatas = new ArrayList<>();
        mAdapter = new GamesAdapter(this, gameDatas);
    }



    @Override
    public View getBindingView(LayoutInflater inflater, int fragmentLayout, ViewGroup container, boolean b) {
        return null;
    }

    @Override
    public void initialize() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        setupToolbar();
        setupRecyclerView();
        loadData();
    }

    @Override
    protected int getFragmentLayout() {
        return fragment_games_list;
    }

    @Override
    protected void injectDependencies() {
        MyApp.get(getActivity()).getNetComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    @Override
    public void onRefresh() {
        mCompositeDisposable.clear();
        if (mAdapter != null) mAdapter.setItems(new ArrayList<GameObject>());

        loadData();
    }

    @OnClick(R.id.button_try_again)
    public void onTryAgainClick() {
        loadData();
    }

    private void setupToolbar() {
        context.setSupportActionBar(mToolbar);
        ActionBar actionBar = context.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }


    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new GridAutofitLayoutManager(this.getContext(),300));
        mRecyclerView.setHasFixedSize(true);
        mAdapter.setItems(gameDatas);
        mRecyclerView.setAdapter(mAdapter);
    }


    public void onNext(GameData value) {
        showHideOfflineLayout(false);
        hideLoadingViews();
        handleResponse(value);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onError(Throwable e) {
        String errorMessage=null;
        if(e instanceof UnknownHostException){
            errorMessage=getString(R.string.noInternet);
        }
        showError("Error :"+errorMessage);
        hideLoadingViews();
        Timber.e("There was a problem loading gameDatas " + e);
        e.printStackTrace();
        DialogFactory.createSimpleOkErrorDialog(
                context,
                "There was a problem loading gameDatas \n " + errorMessage
        ).show();
    }

    public void onComplete() {

        hideLoadingViews();


    }
    private void loadData() {

            showHideOfflineLayout(false);
            mCompositeDisposable.add(mDataManager.getUserList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(mDataManager.getScheduler())
                    .subscribe(this::onNext, this::onError,this::onComplete));

    }

    private void handleResponse(GameData androidList) {
        hideLoadingViews();
        currency=androidList.getCurrency();
        gameDatas = androidList.getData();
        mAdapter = new GamesAdapter(this, gameDatas);
        mRecyclerView.setAdapter(mAdapter);
    }




    private void hideLoadingViews() {
        mProgressBar.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void showHideOfflineLayout(boolean isOffline) {
        mOfflineContainer.setVisibility(isOffline ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(isOffline ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(isOffline ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        showHideOfflineLayout(true);
        errorView.setText(message);
    }


    public void browseThisUser(GameObject gameData) {
        PlayerInfoFragment playerInfoFragment =new PlayerInfoFragment();
        context.addFragment(playerInfoFragment);
    }
}
