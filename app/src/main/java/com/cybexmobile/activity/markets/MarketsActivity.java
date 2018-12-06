package com.cybexmobile.activity.markets;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.BucketObject;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.market.HistoryPrice;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.utils.MyUtils;
import com.cybex.provider.utils.PriceUtil;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybexmobile.R;
import com.cybexmobile.activity.chat.ChatActivity;
import com.cybexmobile.adapter.OrderHistoryFragmentPageAdapter;
import com.cybexmobile.data.DataParse;
import com.cybexmobile.data.KLineBean;
import com.cybexmobile.fragment.MarketTradeHistoryFragment;
import com.cybexmobile.fragment.OrderHistoryListFragment;
import com.cybexmobile.fragment.dummy.DummyContent;
import com.cybexmobile.mychart.CoupleChartGestureListener;
import com.cybexmobile.mychart.MyBottomMarkerView;
import com.cybexmobile.mychart.MyCombinedChart;
import com.cybexmobile.mychart.MyHMarkerView;
import com.cybexmobile.mychart.MyLeftMarkerView;
import com.cybexmobile.utils.VolFormatter;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.ACTION_BUY;
import static com.cybex.basemodule.constant.Constant.ACTION_SELL;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ACTION;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CHANNEL;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_FROM;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_WATCHLIST;

public class MarketsActivity extends BaseActivity implements OrderHistoryListFragment.OnListFragmentInteractionListener{

    private static final long MARKET_STAT_INTERVAL_MILLIS_5_MIN = TimeUnit.MINUTES.toSeconds(5);
    private static final long MARKET_STAT_INTERVAL_MILLIS_1_HOUR = TimeUnit.HOURS.toSeconds(1);
    private static final long MARKET_STAT_INTERVAL_MILLIS_1_DAY = TimeUnit.DAYS.toSeconds(1);
    public static final int RESULT_CODE_BACK = 1;
    public static final int MAXBUCKETCOUNT = 200;

    private Unbinder mUnbinder;
    protected XAxis xAxisKline, xAxisVolume, xAxisCharts;
    protected YAxis axisLeftKline, axisLeftVolume, axisLeftCharts;
    protected YAxis axisRightKline, axisRightVolume, axisRightCharts;
    private OrderHistoryFragmentPageAdapter mOrderHistoryFragmentPageAdapter;
    protected List<HistoryPrice> mHistoryPriceList = new ArrayList<>();
    protected WatchlistData mWatchListData;
    private long mDuration = MARKET_STAT_INTERVAL_MILLIS_1_DAY;

    private DataParse mData;
    private CandleData mCandleData;
    int mBasePrecision;

    private ArrayList<KLineBean> kLineDatas;

    private String mFromWhere;

    private Disposable mDisposable;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.market_page_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.market_page_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.header_kline_chart)
    LinearLayout mHeaderKlineChart;
    @BindView(R.id.header_kline_boll)
    LinearLayout mHeaderBOLLChart;
    @BindView(R.id.k_line_header_ema_layout)
    LinearLayout mHeaderEMAChart;
    @BindView(R.id.index_header_layout)
    LinearLayout mIndexHeaderLayout;
    @BindView(R.id.market_page_layout_footer)
    LinearLayout mLayoutFooter;
    @BindView(R.id.markets_layout_base_header)
    LinearLayout mLayoutBaseHeader;
    @BindView(R.id.market_page_scroll_view)
    NestedScrollView mScrollerView;
    @BindView(R.id.kline_chart_k)
    MyCombinedChart mChartKline;
    @BindView(R.id.kline_chart_volume)
    MyCombinedChart mChartVolume;
    @BindView(R.id.kline_chart_chart)
    MyCombinedChart mChartCharts;
    @BindView(R.id.market_page_btn_buy)
    Button mBtnBuy;
    @BindView(R.id.market_page_btn_sell)
    Button mBtnSell;
    @BindView(R.id.market_page_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.market_page_k_line_duration_spinner)
    MaterialSpinner mDurationSpinner;
    @BindView(R.id.market_page_k_line_ma_spinner)
    MaterialSpinner mMaSpinner;
    @BindView(R.id.market_page_current_money)
    TextView mCurrentPriceView;
    @BindView(R.id.market_page_high_price)
    TextView mHighPriceView;
    @BindView(R.id.market_page_low_price)
    TextView mLowPriceView;
    @BindView(R.id.market_page_volume_base)
    TextView mVolumeBaseView;
    @BindView(R.id.market_page_volume_quote)
    TextView mVolumeQuoteView;
    @BindView(R.id.market_page_exchange_variation)
    TextView mChangeRateView;
    @BindView(R.id.view_kline_tv_ma5)
    TextView mTvKMa5;
    @BindView(R.id.view_kline_tv_ma10)
    TextView mTvKMa10;
    @BindView(R.id.view_kline_tv_ma20)
    TextView mTvKMa20;
    @BindView(R.id.view_boll_tv_1)
    TextView mBOLLTv1;
    @BindView(R.id.view_boll_tv_2)
    TextView mBOLLTv2;
    @BindView(R.id.view_boll_tv_3)
    TextView mBOLLTv3;
    @BindView(R.id.view_ema_tv_5)
    TextView mEMA5Tv;
    @BindView(R.id.view_ema_tv_10)
    TextView mEMA10Tv;
    @BindView(R.id.index_high_tv)
    TextView mTvHighIndex;
    @BindView(R.id.index_low_tv)
    TextView mTvLowIndex;
    @BindView(R.id.index_open_tv)
    TextView mTvOpenIndex;
    @BindView(R.id.index_close_tv)
    TextView mTvCloseIndex;
    @BindView(R.id.tv_index_change_ratio)
    TextView mTvChangeRatioIndex;
    @BindView(R.id.tv_index_change_price)
    TextView mTvChangePriceIndex;
    @BindView(R.id.tv_index_vol)
    TextView mTvVol;
    @BindView(R.id.tv_comment_count)
    TextView mTvCommentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markets);
        mUnbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        setSupportActionBar(mToolbar);
        mWatchListData = (WatchlistData) getIntent().getSerializableExtra(INTENT_PARAM_WATCHLIST);
        mBasePrecision = mWatchListData.getBasePrecision();
        mFromWhere = getIntent().getStringExtra(INTENT_PARAM_FROM);
        initViews();
        mOrderHistoryFragmentPageAdapter = new OrderHistoryFragmentPageAdapter(getSupportFragmentManager());
        mOrderHistoryFragmentPageAdapter.addFragment(OrderHistoryListFragment.newInstance(mWatchListData));
        mOrderHistoryFragmentPageAdapter.addFragment(MarketTradeHistoryFragment.newInstance(mWatchListData));
        mViewPager.setAdapter(mOrderHistoryFragmentPageAdapter);
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mDurationSpinner.setItems(Constant.DURATION_SPINNER);
        mMaSpinner.setItems(Constant.MA_INDEX_SPINNER);
        setSpinnerOnSelectItemListener();
        addContentToView(mWatchListData);
        initChartKline();
        initChartVolume();
        initChartChart();
        setChartListener();
        mProgressBar.setVisibility(View.VISIBLE);
        loadMarketHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLastMsgID();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
        mUnbinder.unbind();
    }

    public void setSpinnerOnSelectItemListener() {
        mDurationSpinner.setSelectedIndex(2);
        mDurationSpinner.setDrawableLevelValue(5000);
        mDurationSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                view.setTextColor(getResources().getColor(R.color.btn_orange_end));
                view.setArrowColor(getResources().getColor(R.color.btn_orange_end));
                mIndexHeaderLayout.setVisibility(View.GONE);
                mLayoutBaseHeader.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mChartKline.setVisibility(View.INVISIBLE);
                mChartVolume.setVisibility(View.INVISIBLE);
                mDuration = getDuration(item);
                loadMarketHistory();
            }
        });
        mMaSpinner.setDrawableLevelValue(5000);
        mMaSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                view.setTextColor(getResources().getColor(R.color.btn_orange_end));
                view.setArrowColor(getResources().getColor(R.color.btn_orange_end));
                changeIndexLine(item);
            }
        });
    }

    private long getDuration(String item) {
        switch (item) {
            case Constant.DURATION5M:
                return MARKET_STAT_INTERVAL_MILLIS_5_MIN;
            case Constant.DURATION1H:
                return MARKET_STAT_INTERVAL_MILLIS_1_HOUR;
            case Constant.DURATION1D:
                return MARKET_STAT_INTERVAL_MILLIS_1_DAY;
            default:
                return MARKET_STAT_INTERVAL_MILLIS_1_DAY;

        }
    }

    private void setDefaultAverageAlgorithm() {
        mData.initKLineMA(kLineDatas);
        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setMaLine(5, mData.getXVals(), mData.getMa5DataL()));
        sets.add(setMaLine(10, mData.getXVals(), mData.getMa10DataL()));
        sets.add(setMaLine(20, mData.getXVals(), mData.getMa20DataL()));
        LineData lineData = new LineData(mData.getXVals(), sets);
        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(lineData);
        combinedData.setData(mCandleData);
        mChartKline.setData(combinedData);
        mChartKline.notifyDataSetChanged();
        mChartKline.invalidate();
        mChartCharts.setVisibility(View.GONE);
        mHeaderKlineChart.setVisibility(View.VISIBLE);
        mHeaderBOLLChart.setVisibility(View.GONE);
        mHeaderEMAChart.setVisibility(View.GONE);
        updateText(mData.getMa20DataL().size() - 1);
    }

    private void changeIndexLine(String item) {
        switch (item) {
            case Constant.INDEXMA:
                mData.initKLineMA(kLineDatas);
                ArrayList<ILineDataSet> sets = new ArrayList<>();
                sets.add(setMaLine(5, mData.getXVals(), mData.getMa5DataL()));
                sets.add(setMaLine(10, mData.getXVals(), mData.getMa10DataL()));
                sets.add(setMaLine(20, mData.getXVals(), mData.getMa20DataL()));
                LineData lineData = new LineData(mData.getXVals(), sets);
                CombinedData combinedData = new CombinedData(mData.getXVals());
                combinedData.setData(lineData);
                combinedData.setData(mCandleData);
                mChartKline.setData(combinedData);
                mChartKline.notifyDataSetChanged();
                mChartKline.invalidate();
                mChartCharts.setVisibility(View.GONE);
                mHeaderKlineChart.setVisibility(View.VISIBLE);
                mHeaderBOLLChart.setVisibility(View.GONE);
                mHeaderEMAChart.setVisibility(View.GONE);
                updateText(mData.getMa20DataL().size() - 1);
                break;
            case Constant.INDEXEMA:
                mData.initEXPMA(kLineDatas);
                ArrayList<ILineDataSet> setEMA = new ArrayList<>();
                setEMA.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData5()));
                setEMA.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData10()));
                setEMA.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData20()));
                setEMA.add(setKDJMaLine(3, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData60()));
                LineData lineDataEma = new LineData(mData.getXVals(), setEMA);

                CombinedData combinedDataEMa = new CombinedData(mData.getXVals());
                combinedDataEMa.setData(lineDataEma);
                combinedDataEMa.setData(mCandleData);
                mChartKline.setData(combinedDataEMa);
                mChartKline.notifyDataSetChanged();
                mChartKline.invalidate();
                mChartCharts.setVisibility(View.GONE);
                mHeaderKlineChart.setVisibility(View.GONE);
                mHeaderBOLLChart.setVisibility(View.GONE);
                mHeaderEMAChart.setVisibility(View.VISIBLE);
                updateEMA(mData.getExpmaData5().size() - 1);
                break;
            case Constant.INDEXBOLL:
                mData.initBOLL(kLineDatas);
                ArrayList<ILineDataSet> setsBoll = new ArrayList<>();
                setsBoll.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataUP()));
                setsBoll.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataMB()));
                setsBoll.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataDN()));
                LineData lineDataBoll = new LineData(mData.getXVals(), setsBoll);
                CombinedData combinedDataBoll = new CombinedData(mData.getXVals());
                combinedDataBoll.setData(lineDataBoll);
                combinedDataBoll.setData(mCandleData);
                mChartKline.setData(combinedDataBoll);
                mChartKline.notifyDataSetChanged();
                mChartKline.invalidate();
                mChartCharts.setVisibility(View.GONE);
                mHeaderKlineChart.setVisibility(View.GONE);
                mHeaderBOLLChart.setVisibility(View.VISIBLE);
                mHeaderEMAChart.setVisibility(View.GONE);
                updateBOLL(mData.getBollDataUP().size() - 1);
                break;
            case Constant.INDEXMACD:
                setMACDByChart(mChartCharts);
                mChartCharts.setVisibility(View.VISIBLE);
                mChartCharts.notifyDataSetChanged();
                mChartCharts.invalidate();
                mHeaderKlineChart.setVisibility(View.GONE);
                mHeaderBOLLChart.setVisibility(View.GONE);
                mHeaderEMAChart.setVisibility(View.GONE);
                break;


        }
    }

    @OnClick(R.id.tv_comment_now)
    public void onCommentNowClick(View view){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(INTENT_PARAM_CHANNEL, String.format("%s/%s",
                AssetUtil.parseSymbol(mWatchListData.getQuoteSymbol()),
                AssetUtil.parseSymbol(mWatchListData.getBaseSymbol())));
        startActivity(intent);
    }

    private void loadLastMsgID() {
        mDisposable = RetrofitFactory.getInstance().apiChat().getLastMsgID(
                String.format("%s/%s", AssetUtil.parseSymbol(mWatchListData.getQuoteSymbol()),
                AssetUtil.parseSymbol(mWatchListData.getBaseSymbol())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mTvCommentCount.setText(integer.intValue() + "");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

    }

    private void loadMarketHistory() {
        if (mWatchListData == null) {
            return;
        }
        Date startDate = new Date(System.currentTimeMillis() - mDuration * MAXBUCKETCOUNT * 1000);
        Date endDate = new Date(System.currentTimeMillis());
        try {
            BitsharesWalletWraper.getInstance().get_market_history(mWatchListData.getBaseAsset().id,
                    mWatchListData.getQuoteAsset().id, (int) mDuration, startDate, endDate, mMarketHistoryCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private WebSocketClient.MessageCallback<WebSocketClient.Reply<List<BucketObject>>> mMarketHistoryCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<BucketObject>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<BucketObject>> reply) {
            List<BucketObject> bucketObjects = reply.result;
            if (bucketObjects == null || bucketObjects.size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
                return;
            }
            mHistoryPriceList.clear();
            for (int i = 0; i < bucketObjects.size(); i++) {
                BucketObject bucket = bucketObjects.get(i);
                mHistoryPriceList.add(PriceUtil.priceFromBucket(mWatchListData.getBaseAsset(), mWatchListData.getQuoteAsset(), bucket));
            }
            /**
             * fix bug
             * 添加无成交量的烛状图
             */
            HistoryPrice historyPricePre = mHistoryPriceList.get(0);
            long size = (System.currentTimeMillis() - historyPricePre.date.getTime()) / (mDuration * 1000);
            Log.e("size", String.valueOf(size));
            HistoryPrice historyPriceNew = null;
            for(int i = 1; i <= size; i++){
                if (i >= mHistoryPriceList.size()) {
                    historyPriceNew = new HistoryPrice();
                    historyPriceNew.date = new Date(historyPricePre.date.getTime() + mDuration * 1000);
                    historyPriceNew.baseVolume = 0;
                    historyPriceNew.quoteVolume = 0;
                    historyPriceNew.open = historyPricePre.close;
                    historyPriceNew.high = historyPricePre.close;
                    historyPriceNew.low = historyPricePre.close;
                    historyPriceNew.close = historyPricePre.close;
                    mHistoryPriceList.add(i, historyPriceNew);
                    historyPricePre = historyPriceNew;
                } else {
                    HistoryPrice historyPriceCurr = mHistoryPriceList.get(i);
                    if (historyPriceCurr.date.getTime() != historyPricePre.date.getTime() + mDuration * 1000) {
                        historyPriceNew = new HistoryPrice();
                        historyPriceNew.date = new Date(historyPricePre.date.getTime() + mDuration * 1000);
                        historyPriceNew.baseVolume = 0;
                        historyPriceNew.quoteVolume = 0;
                        historyPriceNew.open = historyPricePre.close;
                        historyPriceNew.high = historyPricePre.close;
                        historyPriceNew.low = historyPricePre.close;
                        historyPriceNew.close = historyPricePre.close;
                        mHistoryPriceList.add(i, historyPriceNew);
                        historyPricePre = historyPriceNew;
                    } else {
                        historyPricePre = historyPriceCurr;
                    }
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mProgressBar != null){
                        mProgressBar.setVisibility(View.GONE);
                        if(mDuration == MARKET_STAT_INTERVAL_MILLIS_1_DAY){
                            HistoryPrice lastHistoryPrice = mHistoryPriceList.get(mHistoryPriceList.size() - 1);
                            mHighPriceView.setText(lastHistoryPrice.high == 0.f ? "-" :
                                    String.format("High: %s", AssetUtil.formatNumberRounding(lastHistoryPrice.high, mWatchListData.getBasePrecision())));
                            mLowPriceView.setText(lastHistoryPrice.low == 0.f ? "-" :
                                    String.format("Low: %s", AssetUtil.formatNumberRounding(lastHistoryPrice.low, mWatchListData.getBasePrecision())));
                        }
                    }
                }
            });
            initChartData(mHistoryPriceList, mDuration);
            EventBus.getDefault().post(new Event.UpdateKLineChar());
        }

        @Override
        public void onFailure() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateKLineChar(Event.UpdateKLineChar event) {
        Matrix matrix = new Matrix();
        mChartKline.getViewPortHandler().refresh(matrix, mChartKline, true);
        mChartVolume.getViewPortHandler().refresh(matrix, mChartVolume, true);
        mChartCharts.getViewPortHandler().refresh(matrix, mChartCharts, true);
        setKlineByChart(mChartKline);
        setVolumeByChart(mChartVolume);
        setMACDByChart(mChartCharts);
        mChartKline.moveViewToX(kLineDatas.size() - 1);
        mChartVolume.moveViewToX(kLineDatas.size() - 1);
        mChartCharts.moveViewToX(kLineDatas.size() - 1);
        mChartKline.setVisibility(View.VISIBLE);
        mChartVolume.setVisibility(View.VISIBLE);

        mChartKline.setAutoScaleMinMaxEnabled(true);
        mChartVolume.setAutoScaleMinMaxEnabled(true);
        mChartCharts.setAutoScaleMinMaxEnabled(true);

        mChartKline.setVisibleXRangeMinimum(30);
        mChartVolume.setVisibleXRangeMinimum(30);
        mChartCharts.setVisibleXRangeMinimum(30);

        mChartKline.notifyDataSetChanged();
        mChartVolume.notifyDataSetChanged();
        mChartCharts.notifyDataSetChanged();

        mChartKline.invalidate();
        mChartVolume.invalidate();
        mChartCharts.invalidate();
        setDefaultAverageAlgorithm();
    }

    private double getLowFromPriceList(List<HistoryPrice> historyPriceList) {
        double min = historyPriceList.get(0).low;
        for (HistoryPrice historyPrice : historyPriceList) {
            min = Math.min(historyPrice.low, min);
        }
        return min;
    }

    private void initViews() {
        mLayoutFooter.setVisibility(mFromWhere == null ? View.VISIBLE : View.GONE);
        setViewListener();
    }

    private void setViewListener(){
        mBtnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(INTENT_PARAM_ACTION, ACTION_BUY);
                intent.putExtra(INTENT_PARAM_WATCHLIST, mWatchListData);
                setResult(RESULT_CODE_BACK, intent);
                finish();
            }
        });
        mBtnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(INTENT_PARAM_ACTION, ACTION_SELL);
                intent.putExtra(INTENT_PARAM_WATCHLIST, mWatchListData);
                setResult(RESULT_CODE_BACK, intent);
                finish();
            }
        });
    }

    private void addContentToView(WatchlistData watchListData) {
        if (mWatchListData == null) {
            return;
        }
        String trimmedBase = AssetUtil.parseSymbol(watchListData.getBaseSymbol());
        String trimmedQuote = AssetUtil.parseSymbol(watchListData.getQuoteSymbol());
        mTvTitle.setText(String.format("%s/%s", trimmedQuote, trimmedBase));
        watchListData.getBasePrecision();
        mCurrentPriceView.setText(watchListData.getCurrentPrice() == 0.f ? "-" :
                AssetUtil.formatNumberRounding(watchListData.getCurrentPrice(), watchListData.getBasePrecision()));
        mVolumeBaseView.setText(watchListData.getBaseVol() == 0.f ? "-" : String.format("%1$s: %2$s", trimmedBase, AssetUtil.formatAmountToKMB(watchListData.getBaseVol(), 2)));
        double volQuote = 0.f;
        if (watchListData.getCurrentPrice() != 0.f) {
            volQuote = watchListData.getBaseVol() / watchListData.getCurrentPrice();
        }
        mVolumeQuoteView.setText(volQuote == 0.f ? "-" : String.format("%1$s: %2$s", trimmedQuote,
                AssetUtil.formatAmountToKMB(watchListData.getQuoteVol(), 2)));
        double change = watchListData.getChange();
        if (change > 0.f) {
            mChangeRateView.setText(String.format(Locale.US, "+%.2f%%", change));
            mChangeRateView.setTextColor(getResources().getColor(R.color.increasing_color));
            mChangeRateView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_drop_up_24px, 0, 0, 0);
        } else if (change < 0.f) {
            mChangeRateView.setTextColor(getResources().getColor(R.color.decreasing_color));
            mChangeRateView.setText(String.format(Locale.US, "%.2f%%", change));
            mChangeRateView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_drop_down_24px, 0, 0, 0);
        } else {
            mChangeRateView.setText(volQuote == 0.f ? "--" : "0.00%");
            mChangeRateView.setTextColor(getResources().getColor(R.color.no_change_color));
        }
    }

    private void initChartKline() {
        mChartKline.setScaleEnabled(true);//启用图表缩放事件
        mChartKline.setDrawBorders(true);//是否绘制边线
        mChartKline.setBorderWidth(1);//边线宽度，单位dp
        mChartKline.setDragEnabled(true);//启用图表拖拽事件
        mChartKline.setScaleYEnabled(false);//启用Y轴上的缩放
        mChartKline.setBorderColor(Color.TRANSPARENT);//边线颜色
        mChartKline.setDescription("");//右下角对图表的描述信息
        mChartKline.setMinOffset(0f);
        mChartKline.setExtraOffsets(0f, 0f, 0f, 3f);

        Legend lineChartLegend = mChartKline.getLegend();
        lineChartLegend.setEnabled(false);//是否绘制 Legend 图例
        lineChartLegend.setForm(Legend.LegendForm.CIRCLE);

        //bar x y轴
        xAxisKline = mChartKline.getXAxis();
        xAxisKline.setDrawLabels(true); //是否显示X坐标轴上的刻度，默认是true
        xAxisKline.setDrawGridLines(true);//是否显示X坐标轴上的刻度竖线，默认是true
        xAxisKline.setDrawAxisLine(false); //是否绘制坐标轴的线，即含有坐标的那条线，默认是true
        xAxisKline.enableGridDashedLine(10f, 10f, 0f);//虚线表示X轴上的刻度竖线(float lineLength, float spaceLength, float phase)三个参数，1.线长，2.虚线间距，3.虚线开始坐标
        xAxisKline.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));//设置字的颜色
        xAxisKline.setPosition(XAxis.XAxisPosition.TOP_INSIDE);//设置值显示在什么位置
        xAxisKline.setAvoidFirstLastClipping(true);//设置首尾的值是否自动调整，避免被遮挡

        axisLeftKline = mChartKline.getAxisLeft();
        axisLeftKline.setDrawGridLines(false);
        axisLeftKline.setDrawAxisLine(false);
        axisLeftKline.setDrawZeroLine(false);
        axisLeftKline.setDrawLabels(true);
        axisLeftKline.enableGridDashedLine(10f, 10f, 0f);
        axisLeftKline.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
//        axisLeftKline.setGridColor(getResources().getColor(R.color.minute_grayLine));
        axisLeftKline.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeftKline.setLabelCount(4, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布
        axisLeftKline.setSpaceTop(10);//距离顶部留白

        axisRightKline = mChartKline.getAxisRight();
        axisRightKline.setDrawLabels(false);
        axisRightKline.setDrawGridLines(false);
        axisRightKline.setDrawAxisLine(false);
        axisRightKline.setLabelCount(4, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布

        mChartKline.setDragDecelerationEnabled(true);
        mChartKline.setDragDecelerationFrictionCoef(0.2f);

        mChartKline.animateXY(2000, 2000);
    }

    private void initChartVolume() {
        mChartVolume.setScaleEnabled(true);
        mChartVolume.setDrawBorders(true);  //边框是否显示
        mChartVolume.setBorderWidth(1);//边框的宽度，float类型，dp单位
        mChartVolume.setBorderColor(Color.TRANSPARENT);//边框颜色
        mChartVolume.setDescription(""); //图表默认右下方的描述，参数是String对象
        mChartVolume.setDragEnabled(true);// 是否可以拖拽
        mChartVolume.setScaleYEnabled(false); //是否可以缩放 仅y轴
        mChartVolume.setMinOffset(3f);
        mChartVolume.setExtraOffsets(0f, 0f, 0f, 5f);

        Legend combinedchartLegend = mChartVolume.getLegend(); // 设置比例图标示，就是那个一组y的value的
        combinedchartLegend.setEnabled(false);//是否绘制比例图

        //bar x y轴
        xAxisVolume = mChartVolume.getXAxis();
        xAxisVolume.setEnabled(false);
//        xAxisVolume.setDrawLabels(false); //是否显示X坐标轴上的刻度，默认是true
//        xAxisVolume.setDrawGridLines(false);//是否显示X坐标轴上的刻度竖线，默认是true
//        xAxisVolume.setDrawAxisLine(false); //是否绘制坐标轴的线，即含有坐标的那条线，默认是true
//        xAxisVolume.enableGridDashedLine(10f, 10f, 0f);//虚线表示X轴上的刻度竖线(float lineLength, float spaceLength, float phase)三个参数，1.线长，2.虚线间距，3.虚线开始坐标
//        xAxisVolume.setTextColor(getResources().getColor(R.color.text_color_common));//设置字的颜色
//        xAxisVolume.setPosition(XAxis.XAxisPosition.BOTTOM);//设置值显示在什么位置
//        xAxisVolume.setAvoidFirstLastClipping(true);//设置首尾的值是否自动调整，避免被遮挡

        axisLeftVolume = mChartVolume.getAxisLeft();
        axisLeftVolume.setAxisMinValue(0);//设置Y轴坐标最小为多少
//        axisLeftVolume.setShowOnlyMinMax(true);//设置Y轴坐标最小为多少
        axisLeftVolume.setDrawGridLines(false);
        axisLeftVolume.setDrawAxisLine(false);
//        axisLeftVolume.setShowOnlyMinMax(true);
        axisLeftVolume.setDrawLabels(true);
        axisLeftVolume.enableGridDashedLine(10f, 10f, 0f);
        axisLeftVolume.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
//        axisLeftVolume.setGridColor(getResources().getColor(R.color.minute_grayLine));
        axisLeftVolume.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeftVolume.setLabelCount(1, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布
        axisLeftVolume.setSpaceTop(0);//距离顶部留白
//        axisLeftVolume.setSpaceBottom(0);//距离顶部留白

        axisRightVolume = mChartVolume.getAxisRight();
        axisRightVolume.setDrawLabels(false);
        axisRightVolume.setDrawGridLines(false);
        axisRightVolume.setDrawAxisLine(false);

        mChartVolume.setDragDecelerationEnabled(true);
        mChartVolume.setDragDecelerationFrictionCoef(0.2f);

        mChartVolume.animateXY(2000, 2000);
    }

    private void initChartChart() {

        mChartCharts.setScaleEnabled(true);//启用图表缩放事件
        mChartCharts.setDrawBorders(true);//是否绘制边线
        mChartCharts.setBorderWidth(1);//边线宽度，单位dp
        mChartCharts.setDragEnabled(true);//启用图表拖拽事件
        mChartCharts.setScaleYEnabled(false);//启用Y轴上的缩放
        mChartCharts.setBorderColor(Color.TRANSPARENT);//边线颜色
        mChartCharts.setDescription("");//右下角对图表的描述信息
        mChartCharts.setMinOffset(0f);
        mChartCharts.setExtraOffsets(0f, 0f, 0f, 3f);

        Legend lineChartLegend = mChartCharts.getLegend();
        lineChartLegend.setEnabled(false);//是否绘制 Legend 图例

        //bar x y轴
        xAxisCharts = mChartCharts.getXAxis();
        xAxisCharts.setEnabled(false);

        axisLeftCharts = mChartCharts.getAxisLeft();
        axisLeftCharts.setDrawGridLines(true);
        axisLeftCharts.setDrawAxisLine(false);
        axisLeftCharts.setDrawLabels(true);
        axisLeftCharts.enableGridDashedLine(10f, 10f, 0f);
        axisLeftCharts.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
        axisLeftCharts.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeftCharts.setLabelCount(1, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布


        axisRightCharts = mChartCharts.getAxisRight();
        axisRightCharts.setDrawLabels(false);
        axisRightCharts.setDrawGridLines(false);
        axisRightCharts.setDrawAxisLine(false);

        mChartCharts.setDragDecelerationEnabled(true);
        mChartCharts.setDragDecelerationFrictionCoef(0.2f);

        mChartCharts.animateXY(2000, 2000);

    }

    private void setChartListener() {
        // 将K线控的滑动事件传递给交易量控件
        mChartKline.setOnChartGestureListener(new CoupleChartGestureListener(mChartKline, new Chart[]{mChartVolume, mChartCharts}));
        // 将交易量控件的滑动事件传递给K线控件
        mChartVolume.setOnChartGestureListener(new CoupleChartGestureListener(mChartVolume, new Chart[]{mChartKline, mChartCharts}));

        mChartCharts.setOnChartGestureListener(new CoupleChartGestureListener(mChartCharts, new Chart[]{mChartKline, mChartVolume}));

        mChartKline.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                mIndexHeaderLayout.setVisibility(View.VISIBLE);
                mLayoutBaseHeader.setVisibility(View.INVISIBLE);
                Highlight highlight = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY = h.getTouchY() - mChartKline.getHeight();
                Highlight h1 = mChartVolume.getHighlightByTouchPoint(h.getXIndex(), touchY);
                highlight.setTouchY(touchY);
                if (null == h1) {
                    highlight.setTouchYValue(0);
                } else {
                    highlight.setTouchYValue(h1.getTouchYValue());
                }
                mChartVolume.highlightValues(new Highlight[]{highlight});

                Highlight highlight2 = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY2 = h.getTouchY() - mChartKline.getHeight() - mChartVolume.getHeight();
                Highlight h2 = mChartCharts.getHighlightByTouchPoint(h.getXIndex(), touchY2);
                highlight2.setTouchY(touchY2);
                if (null == h2) {
                    highlight2.setTouchYValue(0);
                } else {
                    highlight2.setTouchYValue(h2.getTouchYValue());
                }
                mChartCharts.highlightValues(new Highlight[]{highlight2});

                updateText(e.getXIndex());
                updateBOLL(e.getXIndex());
            }

            @Override
            public void onNothingSelected() {
                mChartVolume.highlightValue(null);
                mChartCharts.highlightValue(null);
                mIndexHeaderLayout.setVisibility(View.GONE);
                mHeaderKlineChart.setVisibility(View.GONE);
                mLayoutBaseHeader.setVisibility(View.VISIBLE);
            }
        });

        mChartKline.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //允许ScrollView截断点击事件，ScrollView可滑动
                    mScrollerView.requestDisallowInterceptTouchEvent(false);
                } else {
                    //不允许ScrollView截断点击事件，点击事件由子View处理
                    mScrollerView.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        mChartVolume.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Highlight highlight = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY = h.getTouchY() + mChartKline.getHeight();
                Highlight h1 = mChartKline.getHighlightByTouchPoint(h.getXIndex(), touchY);
                highlight.setTouchY(touchY);
                if (null == h1) {
                    highlight.setTouchYValue(0);
                } else {
                    highlight.setTouchYValue(h1.getTouchYValue());
                }
                mChartKline.highlightValues(new Highlight[]{highlight});

                Highlight highlight2 = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY2 = h.getTouchY() - mChartVolume.getHeight();
                Highlight h2 = mChartCharts.getHighlightByTouchPoint(h.getXIndex(), touchY2);
                highlight2.setTouchY(touchY2);
                if (null == h2) {
                    highlight2.setTouchYValue(0);
                } else {
                    highlight2.setTouchYValue(h2.getTouchYValue());
                }
                mChartCharts.highlightValues(new Highlight[]{highlight2});

                updateText(e.getXIndex());
                updateBOLL(e.getXIndex());
            }

            @Override
            public void onNothingSelected() {
                mChartKline.highlightValue(null);
                mChartCharts.highlightValue(null);
            }
        });

        mChartCharts.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Highlight highlight = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY = h.getTouchY() + mChartVolume.getHeight();
                Highlight h1 = mChartVolume.getHighlightByTouchPoint(h.getXIndex(), touchY);
                highlight.setTouchY(touchY);
                if (null == h1) {
                    highlight.setTouchYValue(0);
                } else {
                    highlight.setTouchYValue(h1.getTouchYValue());
                }
                mChartVolume.highlightValues(new Highlight[]{highlight});

                Highlight highlight2 = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY2 = h.getTouchY() + mChartVolume.getHeight() + mChartKline.getHeight();
                Highlight h2 = mChartKline.getHighlightByTouchPoint(h.getXIndex(), touchY2);
                highlight2.setTouchY(touchY2);
                if (null == h2) {
                    highlight2.setTouchYValue(0);
                } else {
                    highlight2.setTouchYValue(h2.getTouchYValue());
                }
                mChartKline.highlightValues(new Highlight[]{highlight2});

                updateText(e.getXIndex());
                updateBOLL(e.getXIndex());
            }

            @Override
            public void onNothingSelected() {
                mChartKline.highlightValue(null);
                mChartVolume.highlightValue(null);
            }
        });

    }

    private void initChartData(List<HistoryPrice> historyPriceList, long duration) {
        mData = new DataParse();
        mData.parseKlineHistoryData(historyPriceList, duration);
        kLineDatas = mData.getKLineDatas();
        mData.initLineDatas(kLineDatas);

        setMarkerViewButtom(mData, mChartKline);
        setMarkerView(mData, mChartVolume);
        setMarkerView(mData, mChartCharts);
    }

    private void setMarkerViewButtom(DataParse mData, MyCombinedChart combinedChart) {
        MyLeftMarkerView leftMarkerView = new MyLeftMarkerView(MarketsActivity.this, R.layout.my_marker_view);
        MyHMarkerView hMarkerView = new MyHMarkerView(MarketsActivity.this, R.layout.mymarkerview_line);
        MyBottomMarkerView bottomMarkerView = new MyBottomMarkerView(MarketsActivity.this, R.layout.my_marker_view);
        combinedChart.setMarker(leftMarkerView, bottomMarkerView, hMarkerView, mData);
    }
    private void setMarkerView(DataParse mData, MyCombinedChart combinedChart) {
        MyLeftMarkerView leftMarkerView = new MyLeftMarkerView(MarketsActivity.this, R.layout.my_marker_view);
        MyHMarkerView hMarkerView = new MyHMarkerView(MarketsActivity.this, R.layout.mymarkerview_line);
        combinedChart.setMarker(leftMarkerView, hMarkerView, mData);
    }

    private void setKlineByChart(MyCombinedChart combinedChart) {
        CandleDataSet set = new CandleDataSet(mData.getCandleEntries(), "");
        set.setDrawHorizontalHighlightIndicator(true);
        set.setHighlightEnabled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowWidth(1f);
        set.setValueTextSize(10f);
        set.setDecreasingColor(getResources().getColor(R.color.decreasing_color));//设置开盘价高于收盘价的颜色
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(getResources().getColor(R.color.increasing_color));//设置开盘价地狱收盘价的颜色
        set.setIncreasingPaintStyle(Paint.Style.FILL);
        set.setNeutralColor(getResources().getColor(R.color.increasing_color));//设置开盘价等于收盘价的颜色
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(.5f);
        set.setHighLightColor(getResources().getColor(R.color.no_change_color));
        set.setDrawValues(true);
        set.setValueTextColor(getResources().getColor(R.color.font_color_white_dark));
        CandleData candleData = new CandleData(mData.getXVals(), set);
        mCandleData = candleData;

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(candleData);
        combinedChart.setData(combinedData);

        setHandler(combinedChart);
    }

    private void setVolumeByChart(MyCombinedChart combinedChart) {

        String unit = MyUtils.getVolUnit(mData.getVolmax());
        String k = "k";
        String m = "m";
        int u = 1;
        if (k.equals(unit)) {
            u = 3;
        } else if (m.equals(unit)) {
            u = 6;
        }
        combinedChart.getAxisLeft().setValueFormatter(new VolFormatter((int) Math.pow(10, u)));
//        combinedChart.getAxisLeft().setAxisMaxValue(mData.getVolmax());
        Log.e("@@@", mData.getVolmax() + "da");

        BarDataSet set = new BarDataSet(mData.getBarEntries(), "成交量");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(false);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.no_change_color));
        set.setDrawValues(false);

        List<Integer> list = new ArrayList<>();
        list.add(getResources().getColor(R.color.fade_background_green));
        list.add(getResources().getColor(R.color.fade_background_red));
        set.setColors(list);
        BarData barData = new BarData(mData.getXVals(), set);

        mData.initVlumeMA(kLineDatas);
//        ArrayList<ILineDataSet> sets = new ArrayList<>();
//
//        /******此处修复如果显示的点的个数达不到MA均线的位置所有的点都从0开始计算最小值的问题******************************/
//        sets.add(setMaLine(5, mData.getXVals(), mData.getMa5DataV()));
//        sets.add(setMaLine(10, mData.getXVals(), mData.getMa10DataV()));
//        sets.add(setMaLine(20, mData.getXVals(), mData.getMa20DataV()));
//        sets.add(setMaLine(30, mData.getXVals(), mData.getMa30DataV()));

//        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
//        combinedData.setData(lineData);
        combinedChart.setData(combinedData);

        setHandler(combinedChart);

    }

    private void setMACDByChart(MyCombinedChart combinedChart) {
        mData.initMACD(kLineDatas);

        BarDataSet set = new BarDataSet(mData.getMacdData(), "BarDataSet");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(true);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.font_color_white_dark));
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<Integer> list = new ArrayList<>();
        list.add(getResources().getColor(R.color.increasing_color));
        list.add(getResources().getColor(R.color.decreasing_color));
        set.setColors(list);

        BarData barData = new BarData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setMACDMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getDeaData()));
        sets.add(setMACDMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getDifData()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);
        setHandler(combinedChart);
    }

    private void setKDJByChart(MyCombinedChart combinedChart) {
        mData.initKDJ(kLineDatas);

        BarDataSet set = new BarDataSet(mData.getBarDatasKDJ(), "BarDataSet");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(true);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.kline_x_axis_text_color));
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.TRANSPARENT);

        BarData barData = new BarData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getkData()));
        sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getdData()));
        sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getjData()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);

//        if (isRefresh)
//            setHandler(combinedChart);
    }

    private void setRSIByChart(MyCombinedChart combinedChart) {
        mData.initRSI(kLineDatas);

        BarDataSet set = new BarDataSet(mData.getBarDatasRSI(), "BarDataSet");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(true);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.font_color_white_dark));
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.TRANSPARENT);

        BarData barData = new BarData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getRsiData6()));
        sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getRsiData12()));
        sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getRsiData24()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);

        setHandler(combinedChart);
    }

    private void setBOLLByChart(MyCombinedChart combinedChart) {
        mData.initBOLL(kLineDatas);

//        BarDataSet set = new BarDataSet(mData.getBarDatasBOLL(), "Sinus Function");
//        set.setBarSpacePercent(20); //bar空隙
//        set.setHighlightEnabled(true);
//        set.setHighLightAlpha(255);
//        set.setHighLightColor(getResources().getColor(R.color.marker_line_bg));
//        set.setDrawValues(false);
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set.setColor(getResources().getColor(R.color.transparent));
//
//        BarData barData = new BarData(mData.getXVals(), set);

        int size = kLineDatas.size();   //点的个数
        CandleDataSet set = new CandleDataSet(mData.getCandleEntries(), "");
        set.setDrawHorizontalHighlightIndicator(false);
        set.setHighlightEnabled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowWidth(1f);
        set.setValueTextSize(10f);
        set.setDecreasingColor(getResources().getColor(R.color.decreasing_color));
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(getResources().getColor(R.color.increasing_color));
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setNeutralColor(getResources().getColor(R.color.decreasing_color));
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(1f);
        set.setHighLightColor(getResources().getColor(R.color.font_color_white_dark));
        set.setDrawValues(true);
        set.setValueTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
        set.setShowCandleBar(false);
        CandleData candleData = new CandleData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataUP()));
        sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataMB()));
        sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataDN()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(candleData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);
    }

    @NonNull
    private LineDataSet setMaLine(int ma, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + ma);
        if (ma == 5) {
            lineDataSetMa.setHighlightEnabled(true);
            lineDataSetMa.setDrawHorizontalHighlightIndicator(false);
            lineDataSetMa.setHighLightColor(getResources().getColor(R.color.increasing_color));
        } else {/*此处必须得写*/
            lineDataSetMa.setHighlightEnabled(false);
        }
        lineDataSetMa.setDrawValues(false);
        if (ma == 5) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_5_color));
        } else if (ma == 10) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_10_color));
        } else if (ma == 20) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        } else {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        }
        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);

        lineDataSetMa.setHighlightEnabled(false);
        return lineDataSetMa;
    }

    @NonNull
    private LineDataSet setKDJMaLine(int type, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + type);
        lineDataSetMa.setHighlightEnabled(false);
        lineDataSetMa.setDrawValues(false);

        //DEA
        if (type == 0) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_5_color));
        } else if (type == 1) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_10_color));
        } else if (type == 2) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        } else {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        }

        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);

        return lineDataSetMa;
    }


    private void setHandler(MyCombinedChart combinedChart) {
        final ViewPortHandler viewPortHandlerBar = combinedChart.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(culcMaxscale(mData.getXVals().size()));
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        if (combinedChart.getScaleX() < 3) {
            final float xscale = 3;
            touchmatrix.postScale(xscale, 1f);
        }
    }

    @NonNull
    private LineDataSet setMACDMaLine(int type, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + type);
        lineDataSetMa.setHighlightEnabled(false);
        lineDataSetMa.setDrawValues(false);

        //DEA
        if (type == 0) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_5_color));
        } else {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_10_color));
        }

        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);

        return lineDataSetMa;
    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return max;
    }

    private void updateText(int index) {
        if (index >= 0 && index < kLineDatas.size()) {
            KLineBean klData = kLineDatas.get(index);
            if(index == 0){
                mTvChangeRatioIndex.setText(getResources().getString(R.string.text_empty));
                mTvChangePriceIndex.setText(getResources().getString(R.string.text_empty));
                mTvChangeRatioIndex.setTextColor(getResources().getColor(R.color.font_color_white_dark));
                mTvChangePriceIndex.setTextColor(getResources().getColor(R.color.font_color_white_dark));
            } else {
                /**
                 * 涨跌幅计算规则
                 * 当前K线的close - 上一个K线的close / 上一个K线的close
                 */
                KLineBean klDataPre = kLineDatas.get(index - 1);
                double changePrice = klData.close - klDataPre.close;
                double changeRatio = (changePrice / klDataPre.close) *100;
                if (changeRatio > 0) {
                    mTvChangeRatioIndex.setTextColor(getResources().getColor(R.color.increasing_color));
                    mTvChangePriceIndex.setTextColor(getResources().getColor(R.color.increasing_color));
                    mTvChangeRatioIndex.setText(String.format(Locale.US, "+%s%%", AssetUtil.formatNumberRounding(changeRatio, 2)));
                    mTvChangePriceIndex.setText(String.format(Locale.US, "+%s", AssetUtil.formatNumberRounding(changePrice, mBasePrecision)));
                } else {
                    mTvChangeRatioIndex.setTextColor(getResources().getColor(R.color.decreasing_color));
                    mTvChangePriceIndex.setTextColor(getResources().getColor(R.color.decreasing_color));
                    mTvChangeRatioIndex.setText(String.format(Locale.US, "%s%%", AssetUtil.formatNumberRounding(changeRatio, 2)));
                    mTvChangePriceIndex.setText(AssetUtil.formatNumberRounding(changePrice, mBasePrecision));
                }
            }
            mTvOpenIndex.setText(AssetUtil.formatNumberRounding(Double.parseDouble(String.valueOf(klData.open)), mBasePrecision));
            mTvCloseIndex.setText(AssetUtil.formatNumberRounding(Double.parseDouble(String.valueOf(klData.close)), mBasePrecision));
            mTvHighIndex.setText(AssetUtil.formatNumberRounding(Double.parseDouble(String.valueOf(klData.high)), mBasePrecision));
            mTvLowIndex.setText(AssetUtil.formatNumberRounding(Double.parseDouble(String.valueOf(klData.low)), mBasePrecision));
            mTvVol.setText(String.format(Locale.US, "%s %s", AssetUtil.formatAmountToKMB(klData.baseVol, 2), AssetUtil.parseSymbol(mWatchListData.getBaseSymbol())));
        }
        int newIndex = index;
        if (null != mData.getMa5DataL() && mData.getMa5DataL().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getMa5DataL().size())
                mTvKMa5.setText(Float.isNaN(mData.getMa5DataL().get(newIndex).getVal()) ? "" : AssetUtil.formatNumberRounding(mData.getMa5DataL().get(newIndex).getVal(), mWatchListData.getBasePrecision()));
        }
        if (null != mData.getMa10DataL() && mData.getMa10DataL().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getMa10DataL().size())
                mTvKMa10.setText(Float.isNaN(mData.getMa10DataL().get(newIndex).getVal()) ? "" : AssetUtil.formatNumberRounding(mData.getMa10DataL().get(newIndex).getVal(), mWatchListData.getBasePrecision()));
        }
        if (null != mData.getMa20DataL() && mData.getMa20DataL().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getMa20DataL().size())
                mTvKMa20.setText(Float.isNaN(mData.getMa20DataL().get(newIndex).getVal()) ? "" : AssetUtil.formatNumberRounding(mData.getMa20DataL().get(newIndex).getVal(), mWatchListData.getBasePrecision()));
        }
    }

    private void updateBOLL(int index) {
        int newIndex = index;
        if (null != mData.getBollDataDN() && mData.getBollDataDN().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getBollDataDN().size())
                mBOLLTv1.setText(Float.isNaN(mData.getBollDataDN().get(newIndex).getVal()) ? "" : AssetUtil.formatNumberRounding(mData.getBollDataDN().get(newIndex).getVal(), mWatchListData.getBasePrecision()));
        }

        if (null != mData.getBollDataMB() && mData.getBollDataMB().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getBollDataMB().size())
                mBOLLTv2.setText(Float.isNaN(mData.getBollDataMB().get(newIndex).getVal()) ? "" :AssetUtil.formatNumberRounding(mData.getBollDataMB().get(newIndex).getVal(), mWatchListData.getBasePrecision()));
        }
        if (null != mData.getBollDataUP() && mData.getBollDataUP().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getBollDataUP().size())
                mBOLLTv3.setText(Float.isNaN(mData.getBollDataUP().get(newIndex).getVal()) ? "" :AssetUtil.formatNumberRounding(mData.getBollDataUP().get(newIndex).getVal(), mWatchListData.getBasePrecision()));
        }
    }

    private void updateEMA(int index) {
        if (null != mData.getExpmaData5() && mData.getExpmaData5().size() > 0) {
            if (index >= 0 && index < mData.getExpmaData5().size())
                mEMA5Tv.setText(MyUtils.getDecimalFormatVol(mData.getExpmaData5().get(index).getVal()));
        }

        if (null != mData.getExpmaData10() && mData.getExpmaData10().size() > 0) {
            if (index >= 0 && index < mData.getExpmaData10().size())
                mEMA10Tv.setText(MyUtils.getDecimalFormatVol(mData.getExpmaData10().get(index).getVal()));
        }
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
