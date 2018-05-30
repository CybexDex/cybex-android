package com.cybexmobile.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cybexmobile.activity.LockAssetsActivity;
import com.cybexmobile.activity.SettingActivity;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.activity.LoginActivity;
import com.cybexmobile.activity.OpenOrdersActivity;
import com.cybexmobile.activity.PortfolioActivity;
import com.cybexmobile.adapter.PortfolioRecyclerViewAdapter;
import com.cybexmobile.crypto.Sha256Object;
import com.cybexmobile.dialog.RegisterDialog;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.decimal4j.util.DoubleRounder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {

    private OnAccountFragmentInteractionListener mListener;
    private RecyclerView mPortfolioRecyclerView;
    private PortfolioRecyclerViewAdapter mPortfolioRecyclerViewAdapter;
    private TextView mLoginTextView, mMembershipTextView, mViewAllTextView, mSayHelloTextView, mTotalAccountTextView;
    private WebView mAvatarWebView;
    private ImageView mAvatarImageView, mBalanceInfoImageView;
    private LinearLayout mBeforeLoginLayout, mAfterLoginLayout, mOpenOrderLayout, mOpenLockAssetsLayout;
    private RelativeLayout mPortfolioTitleLayout;
    private RecyclerView.LayoutManager mPortfolioRecycerViewManager;
    private SharedPreferences mSharedPreference;
    private List<AccountBalanceObject> mAccountObjectBalance = new ArrayList<>();
    private List<LimitOrderObject> mLimitOrderObjectList = new ArrayList<>();
    private KProgressHUD mProcessHud;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        initViews(view);
        setClickListener();
        mProcessHud = KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please Wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        mProcessHud.show();
        setViews();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mProcessHud.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        switch (string) {
            case "logout":
                setViews();
                break;
        }

    }

    private void initViews(View view) {
        mBeforeLoginLayout = view.findViewById(R.id.account_no_log_in);
        mAfterLoginLayout = view.findViewById(R.id.account_logged_in);
        mLoginTextView = view.findViewById(R.id.account_log_in_text);
        mSayHelloTextView = view.findViewById(R.id.account_say_hello_text_view);
        mMembershipTextView = view.findViewById(R.id.account_membership_text);
        mPortfolioRecyclerView = view.findViewById(R.id.account_my_asset_recycler_view);
        mViewAllTextView = view.findViewById(R.id.account_view_all);
        mAvatarImageView = view.findViewById(R.id.account_avatar);
        mAvatarWebView = view.findViewById(R.id.account_avatar_webview);
        mTotalAccountTextView = view.findViewById(R.id.account_balance);
        mOpenOrderLayout = view.findViewById(R.id.account_open_order_item_background);
        mOpenLockAssetsLayout = view.findViewById(R.id.account_lockup_item_background);
        mPortfolioTitleLayout = view.findViewById(R.id.portfolio_title_layout);
        mBalanceInfoImageView = view.findViewById(R.id.balance_info_question_marker);
    }

    private void setViews() {
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean ifLoggedIn = mSharedPreference.getBoolean("isLoggedIn", false);
        List<String> nameList = new ArrayList<>();
        String name = mSharedPreference.getString("name", "");
        nameList.add(name);
        if (ifLoggedIn) {
            processLogIn(name, nameList);
        } else {
            mAvatarImageView.setVisibility(View.VISIBLE);
            mAvatarWebView.setVisibility(View.GONE);
            mBeforeLoginLayout.setVisibility(View.VISIBLE);
            mAfterLoginLayout.setVisibility(View.GONE);
        }
    }

    private void processLogIn(String name, List<String> nameList) {
        mAfterLoginLayout.setVisibility(View.VISIBLE);
        mBeforeLoginLayout.setVisibility(View.GONE);
        mAvatarWebView.setVisibility(View.VISIBLE);
        mAvatarImageView.setVisibility(View.GONE);
        loadWebView(mAvatarWebView, 56, name);
        setSayHelloView(name);
        setMemberShipViews(nameList);
        setRecyclerViews(nameList);
        setTotalBalance(nameList);
    }

    private void setMemberShipViews(List<String> nameList) {
        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.membership_item_background);
        drawable.mutate().setAlpha(50);
        mMembershipTextView.setBackground(drawable);
        String registerYear = getMembershipExpirationDate(nameList).substring(0, 4);
        if (Integer.parseInt(registerYear) < 1970) {
            mMembershipTextView.setText(getActivity().getResources().getString(R.string.account_membership_lifetime));
        } else {
            mMembershipTextView.setText(getActivity().getResources().getString(R.string.account_membership_basic));
        }
    }

    private void setRecyclerViews(List<String> nameList) {
        mPortfolioRecycerViewManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mPortfolioRecyclerView.setLayoutManager(mPortfolioRecycerViewManager);
        mAccountObjectBalance.clear();
        mAccountObjectBalance.addAll(getAccountBalance(nameList));
        if (mAccountObjectBalance.size() == 0) {
            mPortfolioTitleLayout.setVisibility(View.GONE);
        } else {
            mPortfolioTitleLayout.setVisibility(View.VISIBLE);
        }
        mPortfolioRecyclerViewAdapter = new PortfolioRecyclerViewAdapter(mAccountObjectBalance);
        mPortfolioRecyclerView.setAdapter(mPortfolioRecyclerViewAdapter);
    }

    private void setSayHelloView(String name) {
        mSayHelloTextView.setText(String.format("%s %s", getActivity().getResources().getString(R.string.account_hello), name));
    }

    private void setTotalBalance(List<String> nameList) {
        mLimitOrderObjectList.clear();
        mLimitOrderObjectList.addAll(getLimitOrder(nameList));
        double mTotal = 0;
        for (AccountBalanceObject balance_object : mAccountObjectBalance) {
            AssetObject mAssetObject = null;
            double priceCyb;
            try {
                mAssetObject = BitsharesWalletWraper.getInstance().get_objects(balance_object.asset_type.toString());
                if (!mAssetObject.id.toString().equals("1.3.0")) {
                    priceCyb = BitsharesWalletWraper.getInstance().get_ticker("1.3.0", mAssetObject.id.toString()).latest;
                } else {
                    priceCyb = 1;
                }
                double price = balance_object.balance / Math.pow(10, mAssetObject.precision);
                mTotal += (price * priceCyb);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }
        for (LimitOrderObject LimitOrderObject : mLimitOrderObjectList) {
            double price = LimitOrderObject.for_sale / Math.pow(10, 5);
            mTotal += price;
        }
        if (mTotal == 0) {
            mTotalAccountTextView.setText("0.00000≈¥0.00");
        } else {
            mTotalAccountTextView.setText(String.valueOf(DoubleRounder.round(mTotal,5)));
        }

    }


    private void setClickListener() {
        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        mViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PortfolioActivity.class);
                startActivity(intent);
            }
        });
        mOpenOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OpenOrdersActivity.class);
                startActivity(intent);
            }
        });
        mOpenLockAssetsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), LockAssetsActivity.class);
                startActivity(intent);
            }
        });

        mBalanceInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterDialog.showDialog(getActivity(), null);
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAccountFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<String> nameList = new ArrayList<>();
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("LogIn", false)) {
                    nameList.add(data.getStringExtra("name"));
                    processLogIn(data.getStringExtra("name"), nameList);
                }
            }
        }
    }

    private List<FullAccountObject> getFullAccount(List<String> names, boolean subscribe) {
        if (BitsharesWalletWraper.getInstance().getMyFullAccountInstance().size() != 0) {
            if (!names.get(0).equals(BitsharesWalletWraper.getInstance().getMyFullAccountInstance().get(0).account.name)) {
                BitsharesWalletWraper.getInstance().getMyFullAccountInstance().clear();
            }
        }
        if (BitsharesWalletWraper.getInstance().getMyFullAccountInstance().size() == 0 ||
                !BitsharesWalletWraper.getInstance().getMyFullAccountInstance().get(0).account.name.equals(names.get(0))) {
            try {
                return BitsharesWalletWraper.getInstance().get_full_accounts(names, subscribe);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }
        return BitsharesWalletWraper.getInstance().getMyFullAccountInstance();
    }

    private List<AccountBalanceObject> getAccountBalance(List<String> names) {
        List<FullAccountObject> FullAccountObjects;
        FullAccountObjects = getFullAccount(names, true);
        if (FullAccountObjects.size() != 0) {
            FullAccountObject fullAccountObject = FullAccountObjects.get(0);
            return fullAccountObject.balances;
        }
        return new ArrayList<AccountBalanceObject>();
    }

    private List<LimitOrderObject> getLimitOrder(List<String> names) {
        List<FullAccountObject> FullAccountObjects;
        FullAccountObjects = getFullAccount(names, true);
        if (FullAccountObjects.size() != 0) {
            FullAccountObject fullAccountObject = FullAccountObjects.get(0);
            return fullAccountObject.limit_orders;
        }
        return new ArrayList<LimitOrderObject>();
    }


    private String getMembershipExpirationDate(List<String> names) {
        List<FullAccountObject> full_account_objects;
        full_account_objects = getFullAccount(names, true);
        if (full_account_objects.size() != 0) {
            FullAccountObject fullAccountObject = full_account_objects.get(0);
            return fullAccountObject.account.membership_expiration_date;
        }
        return null;
    }

    private void loadWebView(WebView webView, int size, String encryptText) {
        Sha256Object.encoder encoder = new Sha256Object.encoder();
        encoder.write(encryptText.getBytes());
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encoder.result().toString() + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);

        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAccountFragmentInteractionListener) {
            mListener = (OnAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setting, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAccountFragmentInteractionListener {
        // TODO: Update argument type and name
        void onAccountFragmentInteraction(Uri uri);
    }
}
