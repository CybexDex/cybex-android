package com.cybex.basemodule.event;

import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybex.provider.graphene.chain.AccountHistoryObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.BlockHeader;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectStatus;
import com.cybex.provider.market.HistoryPrice;
import com.cybex.provider.graphene.chain.MarketTrade;
import com.cybex.provider.market.Order;
import com.cybex.provider.market.OrderBook;
import com.cybex.provider.market.WatchlistData;

import java.util.List;

public class Event {

    public static class IsOnBackground {
        public boolean isOnBackground;

        public IsOnBackground(boolean isOnBackground) {
            this.isOnBackground = isOnBackground;
        }

        public boolean isOnBackground() {
            return isOnBackground;
        }

        public void setOnBackground(boolean onBackground) {
            isOnBackground = onBackground;
        }
    }

    //更新币价格
    public static class UpdateRmbPrice {
        private List<AssetRmbPrice> data;

        public UpdateRmbPrice(List<AssetRmbPrice> data) {
            this.data = data;
        }

        public List<AssetRmbPrice> getData() {
            return data;
        }

        public void setData(List<AssetRmbPrice> data) {
            this.data = data;
        }
    }

    public static class UpdateFullAccount {
        private FullAccountObject fullAccount;

        public UpdateFullAccount(FullAccountObject fullAccount) {
            this.fullAccount = fullAccount;
        }

        public FullAccountObject getFullAccount() {
            return fullAccount;
        }
    }

    //更新所有行情数据
    public static class UpdateWatchlists {
        private String baseAssetId;

        private List<WatchlistData> data;

        public UpdateWatchlists(String baseAssetId, List<WatchlistData> data) {
            this.baseAssetId = baseAssetId;
            this.data = data;
        }

        public List<WatchlistData> getData() {
            return data;
        }

        public void setData(List<WatchlistData> data) {
            this.data = data;
        }

        public String getBaseAssetId() {
            return baseAssetId;
        }

        public void setBaseAssetId(String baseAssetId) {
            this.baseAssetId = baseAssetId;
        }
    }

    /**
     * 更新热门交易对
     */
    public static class UpdateHotWatchlists {
        private List<WatchlistData> allWatchlists;
        private List<WatchlistData> hotWatchlists;

        public UpdateHotWatchlists(List<WatchlistData> hotWatchlists, List<WatchlistData> allWatchlists) {
            this.allWatchlists = allWatchlists;
            this.hotWatchlists = hotWatchlists;
        }

        public List<WatchlistData> getAllWatchlists() {
            return allWatchlists;
        }

        public List<WatchlistData> getHotWatchlists() {
            return hotWatchlists;
        }

    }

    //更新单条行情数据
    public static class UpdateWatchlist {
        private WatchlistData data;

        public UpdateWatchlist(WatchlistData data) {
            this.data = data;
        }

        public WatchlistData getData() {
            return data;
        }

        public void setData(WatchlistData data) {
            this.data = data;
        }
    }

    //
    public static class UpdateOrderBook {
        private OrderBook data;

        public UpdateOrderBook(OrderBook data) {
            this.data = data;
        }

        public OrderBook getData() {
            return data;
        }

        public void setData(OrderBook data) {
            this.data = data;
        }
    }

    public static class UpdateMarketTrade {
        private List<MarketTrade> data;

        public UpdateMarketTrade(List<MarketTrade> data) {
            this.data = data;
        }

        public List<MarketTrade> getData() {
            return data;
        }

        public void setData(List<MarketTrade> data) {
            this.data = data;
        }
    }

    //更新K线图
    public static class UpdateKLineChar {
        private List<HistoryPrice> data;

        public UpdateKLineChar() {

        }

        public UpdateKLineChar(List<HistoryPrice> data) {
            this.data = data;
        }

        public List<HistoryPrice> getData() {
            return data;
        }

        public void setData(List<HistoryPrice> data) {
            this.data = data;
        }
    }

    //加载单个AssertAbject
    public static class LoadAsset {
        private AssetObject data;

        public LoadAsset(AssetObject data) {
            this.data = data;
        }

        public AssetObject getData() {
            return data;
        }

        public void setData(AssetObject data) {
            this.data = data;
        }
    }

    //加载多个AssertAbject
    public static class LoadAssets {
        private List<AssetObject> data;

        public LoadAssets(List<AssetObject> data) {
            this.data = data;
        }

        public List<AssetObject> getData() {
            return data;
        }

        public void setData(List<AssetObject> data) {
            this.data = data;
        }
    }

    //登出
    public static class LoginOut {

    }

    //登录
    public static class LoginIn {
        private String name;

        public LoginIn(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    //http请求超时
    public static class HttpTimeOut {

    }

    //线程调度
    public static class ThreadScheduler<T> {
        private T data;

        public ThreadScheduler(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    public static class NetWorkStateChanged {
        private int state;

        public NetWorkStateChanged(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }

    public static class ConfigChanged {
        private String configName;

        public ConfigChanged(String configName) {
            this.configName = configName;
        }

        public String getConfigName() {
            return configName;
        }

        public void setConfigName(String configName) {
            this.configName = configName;
        }
    }

    public static class UpdateBuySellOrders {
        private List<Order> buyOrders;
        private List<Order> sellOrders;

        public UpdateBuySellOrders(List<Order> buyOrders, List<Order> sellOrders) {
            this.buyOrders = buyOrders;
            this.sellOrders = sellOrders;
        }

        public List<Order> getBuyOrders() {
            return buyOrders;
        }

        public List<Order> getSellOrders() {
            return sellOrders;
        }
    }

    /**
     * 通过K线图界面快捷买入卖出
     */
    public static class MarketIntentToExchange {
        private String action;
        private WatchlistData watchlist;

        public MarketIntentToExchange(String action, WatchlistData watchlist) {
            this.action = action;
            this.watchlist = watchlist;
        }

        public String getAction() {
            return action;
        }

        public WatchlistData getWatchlist() {
            return watchlist;
        }
    }

    /**
     * 交易界面委单被点击 委单价格设置到EditText
     */
    public static class LimitOrderClick {
        private double price;
        private double quoteAmount;

        public LimitOrderClick(double price) {
            this.price = price;
        }

        public LimitOrderClick(double price, double quoteAmount) {
            this.price = price;
            this.quoteAmount = quoteAmount;
        }

        public double getPrice() {
            return price;
        }

        public double getQuoteAmount() {
            return quoteAmount;
        }
    }

    public static class LoadAccountHistory {
        private List<AccountHistoryObject> accountHistoryObjects;

        public LoadAccountHistory(List<AccountHistoryObject> accountHistoryObjects) {
            this.accountHistoryObjects = accountHistoryObjects;
        }

        public List<AccountHistoryObject> getAccountHistoryObjects() {
            return accountHistoryObjects;
        }
    }

    public static class LoadBlock {
        private int callId;
        private BlockHeader blockHeader;

        public LoadBlock(int callId, BlockHeader blockHeader) {
            this.callId = callId;
            this.blockHeader = blockHeader;
        }

        public int getCallId() {
            return callId;
        }

        public BlockHeader getBlockHeader() {
            return blockHeader;
        }
    }

    /**
     * 加载交易手续费
     */
    public static class LoadRequiredFee {
        private FeeAmountObject fee;

        public LoadRequiredFee(FeeAmountObject fee) {
            this.fee = fee;
        }

        public FeeAmountObject getFee() {
            return fee;
        }
    }

    /**
     * 加载转账手续费
     */
    public static class LoadTransferFee{
        private FeeAmountObject fee;
        private boolean isToTransfer;

        public LoadTransferFee(FeeAmountObject fee, boolean isToTransfer) {
            this.fee = fee;
            this.isToTransfer = isToTransfer;
        }

        public FeeAmountObject getFee() {
            return fee;
        }

        public boolean isToTransfer() {
            return isToTransfer;
        }
    }

    /**
     * 加载交易撤单手续费
     */
    public static class LoadRequiredCancelFee {
        private FeeAmountObject fee;

        public LoadRequiredCancelFee(FeeAmountObject fee) {
            this.fee = fee;
        }

        public FeeAmountObject getFee() {
            return fee;
        }
    }

    /**
     * 创建委单
     */
    public static class LimitOrderCreate {
        private boolean isSuccess;

        public LimitOrderCreate(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

    }

    /**
     * 撤销委单
     */
    public static class LimitOrderCancel {
        private boolean isSuccess;

        public LimitOrderCancel(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

    }

    /**
     * 转账
     */
    public static class Transfer{
        private boolean isSuccess;

        public Transfer(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }

    /**
     * 申领
     */
    public static class BalanceClaim {
        private boolean isSuccess;

        public BalanceClaim(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }
    /**
     * 提现
     */
    public static class Withdraw {
        private boolean isSuccess;

        public Withdraw(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }
    /**
     * 行情数据加载完通知交易界面刷新 防止行情界面数据没加载出来进入交易界面交易界面数据为空
     */
    public static class InitExchangeWatchlist {
        private WatchlistData watchlist;

        public InitExchangeWatchlist(WatchlistData watchlist) {
            this.watchlist = watchlist;
        }

        public WatchlistData getWatchlist() {
            return watchlist;
        }
    }

    /**
     * 加载账户信息
     */
    public static class LoadAccountObject {
        private AccountObject accountObject;

        public LoadAccountObject(AccountObject accountObject) {
            this.accountObject = accountObject;
        }

        public AccountObject getAccountObject() {
            return accountObject;
        }
    }

    public static class VerifyAddress {
        private boolean isValid;

        public VerifyAddress(boolean isValid) {
            this.isValid = isValid;
        }

        public boolean isValid() {
            return isValid;
        }
    }

    /**
     * 网络加载模式改变
     */
    public static class LoadModeChanged{
        private int mode;

        public LoadModeChanged(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
    }

    /**
     * ETO项目刷新
     */
    public static class OnRefreshEtoProject {
        private EtoProject etoProject;
        public OnRefreshEtoProject(EtoProject etoProject) {
            this.etoProject = etoProject;
        }

        public EtoProject getEtoProject() {
            return etoProject;
        }
    }

    /**
     * 充提页面隐藏零余额勾选
     */

    public static class onHideZeroBalanceAssetCheckBox {
        private boolean checked;
        public onHideZeroBalanceAssetCheckBox(boolean checked) {
            this.checked = checked;
        }

        public boolean isChecked() {
            return checked;
        }
    }

    /**
     * 充提页面搜索
     */
    public static class onSearchBalanceAsset {
        private String query;
        public onSearchBalanceAsset(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }
    }
}

