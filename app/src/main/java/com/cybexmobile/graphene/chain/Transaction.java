package com.cybexmobile.graphene.chain;

import com.cybexmobile.common.UnsignedShort;
import com.cybexmobile.crypto.Ripemd160Object;
import com.cybexmobile.crypto.Sha256Object;
import com.cybexmobile.fc.io.BitUtil;
import com.cybexmobile.fc.io.RawType;
import com.google.common.primitives.UnsignedInteger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Transaction {
    public class required_authorities {
        public List<ObjectId<AccountObject>> active;
        public List<ObjectId<AccountObject>> owner;
        public List<Authority> other;
    }

    /**
     * Least significant 16 bits from the reference block number. If @ref relative_expiration is zero, this field
     * must be zero as well.
     */
    public transient UnsignedShort unsign_ref_block_num    = UnsignedShort.ZERO;

    long ref_block_num;
    /**
     * The first non-block-number 32-bits of the reference block ID. Recall that block IDs have 32 bits of block
     * number followed by the actual block hash, so this field should be set using the second 32 bits in the
     * @ref block_id_type
     */
    public transient UnsignedInteger unsign_ref_block_prefix = UnsignedInteger.ZERO;

    long ref_block_prefix;
    /**
     * This field specifies the absolute expiration for this transaction.
     */
    public Date expiration;
    public transient List<Operations.operation_type> operationTypes;
    public List<Object> operations;
    public Set<Types.void_t> extensions;

    public Ripemd160Object id() {
        return null;
    }

    public void set_reference_block(Ripemd160Object reference_block) {
        unsign_ref_block_num = new UnsignedShort((short) BitUtil.endian_reverse_u32(reference_block.hash[0]));
        ref_block_num = unsign_ref_block_num.longValue();
        //ref_block_prefix = new UnsignedInteger(reference_block.hash[1]);
        unsign_ref_block_prefix = UnsignedInteger.fromIntBits(reference_block.hash[1]);
        ref_block_prefix = unsign_ref_block_prefix.longValue();
    }

    public void set_expiration(Date expiration_time) {
        expiration = expiration_time;
    }

    public required_authorities get_required_authorities() {
        required_authorities requiredAuthorities = new required_authorities();
        requiredAuthorities.active = new ArrayList<>();
        requiredAuthorities.owner = new ArrayList<>();
        requiredAuthorities.other = new ArrayList<>();

        for (Operations.operation_type operationType : operationTypes) {
            Operations.base_operation baseOperation = (Operations.base_operation) operationType.operationContent;
            requiredAuthorities.active.addAll(baseOperation.get_required_active_authorities());
            requiredAuthorities.owner.addAll(baseOperation.get_required_owner_authorities());
            requiredAuthorities.other.addAll(baseOperation.get_required_authorities());
        }

        return requiredAuthorities;
    }

    public Sha256Object sig_digest(Sha256Object chain_id) {
        // // TODO: 07/09/2017 这里还未处理
        Sha256Object.encoder enc = new Sha256Object.encoder();

        enc.write(chain_id.hash, 0, chain_id.hash.length);
        RawType rawTypeObject = new RawType();
        enc.write(rawTypeObject.get_byte_array(unsign_ref_block_num.shortValue()));
        enc.write(rawTypeObject.get_byte_array(unsign_ref_block_prefix.intValue()));
        enc.write(rawTypeObject.get_byte_array(expiration));

        //enc.write(rawTypeObject.get_byte_array(operations.size()));
        rawTypeObject.pack(enc, UnsignedInteger.fromIntBits(operationTypes.size()));
        for (Operations.operation_type operationType : operationTypes) {
            //enc.write(rawTypeObject.get_byte_array(operationType.nOperationType));
            rawTypeObject.pack(enc, UnsignedInteger.fromIntBits(operationType.nOperationType));
            Operations.base_operation baseOperation = (Operations.base_operation) operationType.operationContent;
            baseOperation.write_to_encoder(enc);
        }
        //enc.write(rawTypeObject.get_byte_array(extensions.size()));
        rawTypeObject.pack(enc, UnsignedInteger.fromIntBits(extensions.size()));



        return enc.result();
    }
}
