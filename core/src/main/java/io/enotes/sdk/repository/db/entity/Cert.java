package io.enotes.sdk.repository.db.entity;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1GeneralizedTime;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERIA5String;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERPrintableString;
import org.spongycastle.asn1.DERTaggedObject;
import org.spongycastle.asn1.DLSequence;

import java.math.BigInteger;


public class Cert {
    private byte[] tbsCertificate;
    private int certVersion;
    private String vendorName;
    private String vendorId;
    private String batch;
    private long productionDate;
    private long issuerDate;
    private String serialNumber;
    private String blockChain;
    private String tokenProtocol;
    private String tokenAddress;
    private BigInteger coinDeno;
    private int netWork;
    private String publicKey;//compressed
    private BigInteger r;
    private BigInteger s;

    public byte[] getTbsCertificate() {
        return tbsCertificate;
    }

    public void setTbsCertificate(byte[] tbsCertificate) {
        this.tbsCertificate = tbsCertificate;
    }

    public int getCertVersion() {
        return certVersion;
    }

    public void setCertVersion(int certVersion) {
        this.certVersion = certVersion;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
        if (vendorName != null) {
            //for call contacts
            vendorId = ByteUtil.toHexString(HashUtil.sha3((vendorName.toUpperCase().getBytes())));
        }
    }

    public String getVendorId() {
        if (vendorId == null && vendorName != null) {
            //for call contacts
            vendorId = ByteUtil.toHexString(HashUtil.sha3((vendorName.toUpperCase().getBytes())));
        }
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public long getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(long productionDate) {
        this.productionDate = productionDate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBlockChain() {
        return blockChain;
    }

    public void setBlockChain(String blockChain) {
        this.blockChain = blockChain;
    }

    public String getTokenProtocol() {
        return tokenProtocol;
    }

    public void setTokenProtocol(String tokenProtocol) {
        this.tokenProtocol = tokenProtocol;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public BigInteger getCoinDeno() {
        return coinDeno;
    }

    public void setCoinDeno(BigInteger coinDeno) {
        this.coinDeno = coinDeno;
    }

    public int getNetWork() {
        return netWork;
    }

    public void setNetWork(int netWork) {
        this.netWork = netWork;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public BigInteger getR() {
        return r;
    }

    public void setR(BigInteger r) {
        this.r = r;
    }

    public BigInteger getS() {
        return s;
    }

    public void setS(BigInteger s) {
        this.s = s;
    }

    public long getIssuerDate() {
        return issuerDate;
    }

    public void setIssuerDate(long issuerDate) {
        this.issuerDate = issuerDate;
    }

    @Override
    public String toString() {
        return "Cert{" +
                "certVersion=" + certVersion +
                ", vendorName='" + vendorName + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", batch=" + batch +
                ", productionDate=" + productionDate +
                ", issuerDate=" + issuerDate +
                ", serialNumber='" + serialNumber + '\'' +
                ", blockChain=" + blockChain +
                ", tokenProtocol='" + tokenProtocol + '\'' +
                ", tokenAddress='" + tokenAddress + '\'' +
                ", coinDeno=" + coinDeno +
                ", network=" + netWork + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", r='" + r + '\'' +
                ", s='" + s + '\'' +
                '}';
    }

    public static Cert fromHex(@NonNull String hex) throws IllegalArgumentException {
        Cert cert = new Cert();
        try {
            ASN1InputStream decoder = new ASN1InputStream(ByteUtil.hexStringToBytes(hex));
            DLSequence seq = (DLSequence) decoder.readObject();
            if (seq == null)
                throw new IllegalArgumentException("Reached past end of ASN.1 stream.");
            if (seq.size() == 2) {
                ASN1Sequence tbsCertificateSequence = (ASN1Sequence) seq.getObjectAt(0);
                ASN1Sequence signatureValueSequence = (ASN1Sequence) seq.getObjectAt(1);
                cert.setTbsCertificate(tbsCertificateSequence.getEncoded());
                if (tbsCertificateSequence.size() == 6) {
                    ASN1Integer version = (ASN1Integer) tbsCertificateSequence.getObjectAt(0);
                    DERPrintableString issuer = (DERPrintableString) tbsCertificateSequence.getObjectAt(1);
                    ASN1GeneralizedTime issuerTime = (ASN1GeneralizedTime) tbsCertificateSequence.getObjectAt(2);
                    ASN1Sequence subjectSequence = (ASN1Sequence) tbsCertificateSequence.getObjectAt(3);
                    DEROctetString publicKeyInfo = (DEROctetString) tbsCertificateSequence.getObjectAt(4);
                    ASN1Sequence manufactureInfoSequence = (ASN1Sequence) tbsCertificateSequence.getObjectAt(5);

                    cert.setCertVersion(version.getValue().intValue());
                    cert.setVendorName(issuer.getString());
                    cert.setIssuerDate(issuerTime.getDate().getTime());
                    cert.setPublicKey(ByteUtil.toHexString(publicKeyInfo.getOctets()));
                    if (subjectSequence.size() >= 3) {
                        cert.setCoinDeno(((ASN1Integer) subjectSequence.getObjectAt(0)).getValue());
                        cert.setBlockChain(ByteUtil.toHexString(((DEROctetString) subjectSequence.getObjectAt(1)).getOctets()));
                        cert.setNetWork(((ASN1Integer) subjectSequence.getObjectAt(2)).getValue().intValue());
                        if (subjectSequence.size() == 4) {
                            if (subjectSequence.getObjectAt(3) instanceof DEROctetString) {
                                cert.setTokenProtocol("00000001");
                                cert.setTokenAddress(ByteUtil.toHexString(((DEROctetString) subjectSequence.getObjectAt(3)).getOctets()));
                                if (!TextUtils.isEmpty(cert.getTokenAddress())) {
                                    cert.setTokenAddress("0x" + cert.getTokenAddress());
                                }
                            } else {
                                DLSequence token = (DLSequence) subjectSequence.getObjectAt(3);
                                cert.setTokenProtocol(ByteUtil.toHexString(((DEROctetString) token.getObjectAt(0)).getOctets()));
                                DERTaggedObject taggedObject = (DERTaggedObject) token.getObjectAt(1);
                                if (!cert.getTokenProtocol().equals("00000003")) {
                                    DEROctetString objectParser = (DEROctetString) taggedObject.getObjectParser(taggedObject.getTagNo(), true);
                                    if (cert.getTokenProtocol().equals("00000000")) {//omni layer
                                        cert.setTokenAddress(new BigInteger(ByteUtil.toHexString(objectParser.getOctets()), 16).intValue() + "");
                                    } else {//erc20 erc721
                                        cert.setTokenAddress(ByteUtil.toHexString(objectParser.getOctets()));
                                        if (!TextUtils.isEmpty(cert.getTokenAddress())) {
                                            cert.setTokenAddress("0x" + cert.getTokenAddress());
                                        }
                                    }
                                } else {
                                    cert.setTokenAddress(((DERIA5String) taggedObject.getObjectParser(taggedObject.getTagNo(), true)).getString());
                                }
                            }

                        }
                    } else {
                        throw new IllegalArgumentException("Cert : get subject fail");
                    }

                    if (manufactureInfoSequence.size() >= 2) {
                        cert.setSerialNumber(((DERIA5String) manufactureInfoSequence.getObjectAt(0)).getString());
                        cert.setBatch(((DERIA5String) manufactureInfoSequence.getObjectAt(1)).getString());
                        if (manufactureInfoSequence.size() == 3)
                            cert.setProductionDate(((ASN1GeneralizedTime) manufactureInfoSequence.getObjectAt(2)).getDate().getTime());
                    } else {
                        throw new IllegalArgumentException("Cert : get manufacturer fail");
                    }
                } else {
                    throw new IllegalArgumentException("Cert : get tbsCertificate fail");
                }
                if (signatureValueSequence.size() == 2) {
                    cert.setR(((ASN1Integer) signatureValueSequence.getObjectAt(0)).getValue());
                    cert.setS(((ASN1Integer) signatureValueSequence.getObjectAt(1)).getValue());
                } else {
                    throw new IllegalArgumentException("Cert : get signatureValue fail");
                }


            } else {
                throw new IllegalArgumentException("Cert : get certificate fail");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cert : parse certificate fail", e);
        }

        return cert;
    }
}
