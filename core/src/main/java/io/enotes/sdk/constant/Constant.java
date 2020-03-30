package io.enotes.sdk.constant;

public class Constant {
    public static class BlockChain {
        public static final String BITCOIN = "80000000";
        public static final String ETHEREUM = "8000003c";
        public static final String BITCOIN_CASH = "80000091";
        public static final String RIPPLE = "80000090";
        public static final String CYBEX = "00000001";
    }

    public static class Network {
        public static final int BTC_MAINNET = 0;
        public static final int BTC_TESTNET = 1;
        public static final int ETH_MAINNET = 1;
        public static final int ETH_ROPSTEN = 3;
        public static final int ETH_RINKEBY = 4;
        public static final int ETH_KOVAN = 42;
    }

    public static class ContractAddress {
        public static final String ABI_ADDRESS = "0x9A21e2c918026D9420DdDb2357C8205216AdD269";
        public static final String ABI_KOVAN_ADDRESS = "0x5C036d8490127ED26E3A142024082eaEE482BbA2";
    }

    public static class APDU {
        public static final int CERT_VERSION = 2;
        public static final String APDU_VERSION = "1.2.0";
    }

    public static class CardType {
        public static final String BTC = "BTC";
        public static final String ETH = "ETH";
        public static final String GUSD = "GUSD";
        public static final String USDT = "USDT";
        public static final String BCH = "BCH";
        public static final String XRP = "XRP";
        public static final String OTHER_ERC20 = "Other_erc20";
        public static final String OTHERS = "Others";
    }
}
