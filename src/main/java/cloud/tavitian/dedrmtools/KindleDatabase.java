/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

import static cloud.tavitian.dedrmtools.Util.*;

public final class KindleDatabase extends LinkedHashMap<String, String> {
    private static final byte[] charMap1 = "n5Pr6St7Uv8Wx9YzAb0Cd1Ef2Gh3Jk4M".getBytes(StandardCharsets.US_ASCII);

    private static final String kindleAccountTokensKey = "kindle.account.tokens";
    private static final String dsnKey = "DSN";
    private static final String mazamaRandomNumberKey = "MazamaRandomNumber";
    private static final String serialNumberKey = "SerialNumber";
    private static final String idStringKey = "IDString";
    private static final String usernameHashKey = "UsernameHash";
    private static final String userNameKey = "UserName";

    private static byte[] encode(byte[] data) {
        return KGenPidsUtils.encode(data, charMap1);
    }

    // Hash the bytes in data and then encode the digest with the characters in map
    private static byte[] encodeHash(byte[] data) throws NoSuchAlgorithmException {
        return encode(md5(data));
    }

    public String getKindleAccountToken() {
        return get(kindleAccountTokensKey);
    }

    public String getDSN() {
        return get(dsnKey);
    }

    public String getMazamaRandomNumber() {
        return get(mazamaRandomNumberKey);
    }

    public String getSerialNumber() {
        return get(serialNumberKey);
    }

    public String getIDString() {
        return get(idStringKey);
    }

    public String getUsernameHash() {
        return get(usernameHashKey);
    }

    public String getUserName() {
        return get(userNameKey);
    }

    public String getKindleAccountTokenOrDefault(String defaultValue) {
        return getOrDefault(kindleAccountTokensKey, defaultValue);
    }

    public String getDSNOrDefault(String defaultValue) {
        return getOrDefault(dsnKey, defaultValue);
    }

    public String getMazamaRandomNumberOrDefault(String defaultValue) {
        return getOrDefault(mazamaRandomNumberKey, defaultValue);
    }

    public String getSerialNumberOrDefault(String defaultValue) {
        return getOrDefault(serialNumberKey, defaultValue);
    }

    public String getIDStringOrDefault(String defaultValue) {
        return getOrDefault(idStringKey, defaultValue);
    }

    public String getUsernameHashOrDefault(String defaultValue) {
        return getOrDefault(usernameHashKey, defaultValue);
    }

    public String getUserNameOrDefault(String defaultValue) {
        return getOrDefault(userNameKey, defaultValue);
    }

    public boolean containsKindleAccountToken() {
        return containsKey(kindleAccountTokensKey);
    }

    public boolean containsDSN() {
        return containsKey(dsnKey);
    }

    public boolean containsMazamaRandomNumber() {
        return containsKey(mazamaRandomNumberKey);
    }

    public boolean containsSerialNumber() {
        return containsKey(serialNumberKey);
    }

    public boolean containsIDString() {
        return containsKey(idStringKey);
    }

    public boolean containsUsernameHash() {
        return containsKey(usernameHashKey);
    }

    public boolean containsUserName() {
        return containsKey(userNameKey);
    }

    public byte[] getKindleAccountTokenBytes() {
        Debug.log(String.format("Got Kindle Account Token: %s", getKindleAccountToken()));
        return hexStringToByteArray(getKindleAccountToken());
    }

    public byte[] getDSNBytes() {
        Debug.log(String.format("Got DSN: %s", getDSN()));
        return hexStringToByteArray(getDSN());
    }

    public byte[] getMazamaRandomNumberBytes() {
        Debug.log(String.format("Got MazamaRandomNumber: %s", getMazamaRandomNumber()));
        return hexStringToByteArray(getMazamaRandomNumber());
    }

    public byte[] getSerialNumberBytes() {
        Debug.log(String.format("Got SerialNumber: %s", getSerialNumber()));
        return hexStringToByteArray(getSerialNumber());
    }

    public byte[] getIDStringBytes() {
        Debug.log(String.format("Got IDString: %s", getIDString()));
        return hexStringToByteArray(getIDString());
    }

    public byte[] getUsernameHashBytes() {
        Debug.log(String.format("Got UsernameHash: %s", getUsernameHash()));
        return hexStringToByteArray(getUsernameHash());
    }

    public byte[] getUserNameBytes() {
        Debug.log(String.format("Got UserName: %s", getUserName()));
        return hexStringToByteArray(getUserName());
    }

    public byte[] getKindleAccountTokenBytesOrDefault(byte[] defaultValue) {
        return containsKindleAccountToken() ? getKindleAccountTokenBytes() : defaultValue;
    }

    public byte[] getDSNBytesOrDefault(byte[] defaultValue) {
        return containsDSN() ? getDSNBytes() : defaultValue;
    }

    public byte[] getMazamaRandomNumberBytesOrDefault(byte[] defaultValue) {
        return containsMazamaRandomNumber() ? getMazamaRandomNumberBytes() : defaultValue;
    }

    public byte[] getSerialNumberBytesOrDefault(byte[] defaultValue) {
        return containsSerialNumber() ? getSerialNumberBytes() : defaultValue;
    }

    public byte[] getIDStringBytesOrDefault(byte[] defaultValue) {
        return containsIDString() ? getIDStringBytes() : defaultValue;
    }

    public byte[] getUsernameHashBytesOrDefault(byte[] defaultValue) {
        return containsUsernameHash() ? getUsernameHashBytes() : defaultValue;
    }

    public byte[] getUserNameBytesOrDefault(byte[] defaultValue) {
        return containsUserName() ? getUserNameBytes() : defaultValue;
    }

    public byte[] genKindleAccountToken() {
        return getKindleAccountTokenBytesOrDefault(new byte[0]);
    }

    public byte[] genIdString() {
        return getSerialNumberBytesOrDefault(getIDStringBytes());
    }

    public byte[] genEncodedIdString() throws NoSuchAlgorithmException {
        return encodeHash(genIdString());
    }

    public byte[] genEncodedUsername() throws NoSuchAlgorithmException {
        return getUsernameHashBytesOrDefault(encodeHash(getUserNameBytes()));
    }

    public byte[] genDSN() throws NoSuchAlgorithmException {
        return getDSNBytesOrDefault(genAltDSN());
    }

    public byte[] genAltDSN() throws NoSuchAlgorithmException {
        return encode(sha1(getMazamaRandomNumberBytes(), genEncodedIdString(), genEncodedUsername()));
    }
}
