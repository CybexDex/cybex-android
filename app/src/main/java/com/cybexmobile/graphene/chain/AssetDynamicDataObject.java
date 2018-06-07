package com.cybexmobile.graphene.chain;


import java.io.Serializable;

public class AssetDynamicDataObject implements Serializable {

    //static const uint8_t space_id = implementation_ids;
    //static const uint8_t type_id  = impl_asset_dynamic_data_type;

    /// The number of shares currently in existence
    long current_supply;
    long confidential_supply; ///< total Asset held in confidential balances
    long accumulated_fees; ///< fees accumulate to be paid out over time
    long fee_pool;         ///< in core Asset
}
