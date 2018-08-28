package com.cybex.provider.graphene.chain;


import java.io.Serializable;
import java.util.List;

public class AssetOptions implements Serializable {

    /// The maximum supply of this Asset which may exist at any given time. This can be as large as
    /// GRAPHENE_MAX_SHARE_SUPPLY
    long max_supply = Config.GRAPHENE_MAX_SHARE_SUPPLY;
    /// When this Asset is traded on the markets, this percentage of the total traded will be exacted and paid
    /// to the issuer. This is a fixed point value, representing hundredths of a percent, i.e. a value of 100
    /// in this field means a 1% fee is charged on market trades of this Asset.
    int market_fee_percent = 0;
    /// Market fees calculated as @ref market_fee_percent of the traded volume are capped to this value
    long max_market_fee = Config.GRAPHENE_MAX_SHARE_SUPPLY;

    /// The flags which the issuer has permission to update. See @ref asset_issuer_permission_flags
    //int issuer_permissions = UIA_ASSET_ISSUER_PERMISSION_MASK;
    /// The currently active flags on this permission. See @ref asset_issuer_permission_flags
    int flags = 0;

    /// When a non-core Asset is used to pay a fee, the blockchain must convert that Asset to core Asset in
    /// order to accept the fee. If this Asset's fee pool is funded, the chain will automatically deposite fees
    /// in this Asset to its accumulated fees, and withdraw from the fee pool the same amount as converted at
    /// the core exchange rate.
    public Price core_exchange_rate;

    /// A set of accounts which maintain whitelists to consult for this Asset. If whitelist_authorities
    /// is non-empty, then only accounts in whitelist_authorities are allowed to hold, use, or transfer the Asset.
    List<ObjectId<AccountObject>> whitelist_authorities;
    /// A set of accounts which maintain blacklists to consult for this Asset. If flags & white_list is set,
    /// an account may only send, receive, trade, etc. in this Asset if none of these accounts appears in
    /// its AccountObject::blacklisting_accounts field. If the account is blacklisted, it may not transact in
    /// this Asset even if it is also whitelisted.
    List<ObjectId<AccountObject>> blacklist_authorities;

    /** defines the assets that this Asset may be traded against in the market */
    List<ObjectId<AssetObject>>   whitelist_markets;
    /** defines the assets that this Asset may not be traded against in the market, must not overlap whitelist */
    List<ObjectId<AssetObject>>   blacklist_markets;

    /**
     * data that describes the meaning/purpose of this Asset, fee will be charged proportional to
     * size of description.
     */
    String description;
    //extensions_type extensions;

    /// Perform internal consistency checks.
    /// @throws fc::exception if any check fails
}
