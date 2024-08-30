/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Debug;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

import static cloud.tavitian.dedrmtools.CharMaps.charMap1;
import static cloud.tavitian.dedrmtools.HashUtils.sha1;
import static cloud.tavitian.dedrmtools.Util.formatByteArray;
import static cloud.tavitian.dedrmtools.Util.hexStringToByteArray;
import static cloud.tavitian.dedrmtools.kindlekeys.KindleKeyUtils.encode;
import static cloud.tavitian.dedrmtools.kindlekeys.KindleKeyUtils.encodeHash;

public class KindleDatabase<V> extends LinkedHashMap<String, V> {
    private static final String kindleAccountTokensKey = "kindle.account.tokens";
    private static final String dsnKey = "DSN";
    private static final String mazamaRandomNumberKey = "MazamaRandomNumber";
    private static final String serialNumberKey = "SerialNumber";
    private static final String idStringKey = "IDString";
    private static final String usernameHashKey = "UsernameHash";
    private static final String userNameKey = "UserName";

    private static final String kindleCookieItemKey = "kindle.cookie.item";
    private static final String eulaVersionAcceptedKey = "eulaVersionAccepted";
    private static final String loginDateKey = "login_date";
    private static final String kindleTokenItemKey = "kindle.token.item";
    private static final String loginKey = "login";
    private static final String kindleKeyItemKey = "kindle.key.item";
    private static final String kindleNameInfoKey = "kindle.name.info";
    private static final String kindleDeviceInfoKey = "kindle.device.info";
    private static final String maxDateKey = "max_date";
    private static final String sigVerifKey = "SIGVERIF";
    private static final String buildVersionKey = "build_version";
    private static final String kindleDirectedIDInfoKey = "kindle.directedid.info";
    private static final String kindleAccountTypeInfoKey = "kindle.accounttype.info";
    private static final String flashcardsPluginDataEncryptionKeyKey = "krx.flashcardsplugin.data.encryption_key";
    private static final String notebookExportPluginDataEncryptionKeyKey = "krx.notebookexportplugin.data.encryption_key";
    private static final String proxyHttpPasswordKey = "proxy.http.password";
    private static final String proxyHttpUsernameKey = "proxy.http.username";

    public static byte[][] keyBytesList = new byte[][]{
            kindleAccountTokensKey.getBytes(StandardCharsets.US_ASCII),
            kindleCookieItemKey.getBytes(StandardCharsets.US_ASCII),
            eulaVersionAcceptedKey.getBytes(StandardCharsets.US_ASCII),
            loginDateKey.getBytes(StandardCharsets.US_ASCII),
            kindleTokenItemKey.getBytes(StandardCharsets.US_ASCII),
            loginKey.getBytes(StandardCharsets.US_ASCII),
            kindleKeyItemKey.getBytes(StandardCharsets.US_ASCII),
            kindleNameInfoKey.getBytes(StandardCharsets.US_ASCII),
            kindleDeviceInfoKey.getBytes(StandardCharsets.US_ASCII),
            mazamaRandomNumberKey.getBytes(StandardCharsets.US_ASCII),
            maxDateKey.getBytes(StandardCharsets.US_ASCII),
            sigVerifKey.getBytes(StandardCharsets.US_ASCII),
            buildVersionKey.getBytes(StandardCharsets.US_ASCII),
            serialNumberKey.getBytes(StandardCharsets.US_ASCII),
            usernameHashKey.getBytes(StandardCharsets.US_ASCII),
            kindleDirectedIDInfoKey.getBytes(StandardCharsets.US_ASCII),
            dsnKey.getBytes(StandardCharsets.US_ASCII),
            kindleAccountTypeInfoKey.getBytes(StandardCharsets.US_ASCII),
            flashcardsPluginDataEncryptionKeyKey.getBytes(StandardCharsets.US_ASCII),
            notebookExportPluginDataEncryptionKeyKey.getBytes(StandardCharsets.US_ASCII),
            proxyHttpPasswordKey.getBytes(StandardCharsets.US_ASCII),
            proxyHttpUsernameKey.getBytes(StandardCharsets.US_ASCII)
    };

    public V getKindleAccountToken() {
        return get(kindleAccountTokensKey);
    }

    public V getDSN() {
        return get(dsnKey);
    }

    public V getMazamaRandomNumber() {
        return get(mazamaRandomNumberKey);
    }

    public V getSerialNumber() {
        return get(serialNumberKey);
    }

    public V getIDString() {
        return get(idStringKey);
    }

    public V getUsernameHash() {
        return get(usernameHashKey);
    }

    public V getUserName() {
        return get(userNameKey);
    }

    public V getKindleAccountTokenOrDefault(V defaultValue) {
        return getOrDefault(kindleAccountTokensKey, defaultValue);
    }

    public V getDSNOrDefault(V defaultValue) {
        return getOrDefault(dsnKey, defaultValue);
    }

    public V getMazamaRandomNumberOrDefault(V defaultValue) {
        return getOrDefault(mazamaRandomNumberKey, defaultValue);
    }

    public V getSerialNumberOrDefault(V defaultValue) {
        return getOrDefault(serialNumberKey, defaultValue);
    }

    public V getIDStringOrDefault(V defaultValue) {
        return getOrDefault(idStringKey, defaultValue);
    }

    public V getUsernameHashOrDefault(V defaultValue) {
        return getOrDefault(usernameHashKey, defaultValue);
    }

    public V getUserNameOrDefault(V defaultValue) {
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
        Debug.println(String.format("Got Kindle Account Token: %s", getKindleAccountToken()));

        V token = getKindleAccountToken();

        if (token instanceof String string) return hexStringToByteArray(string);
        else if (token instanceof byte[] bytes) return bytes;

        return null;
    }

    public byte[] getDSNBytes() {
        Debug.println(String.format("Got DSN: %s", getDSN()));

        V dsn = getDSN();

        if (dsn instanceof String string) return hexStringToByteArray(string);
        else if (dsn instanceof byte[] bytes) return bytes;

        return null;
    }

    public byte[] getMazamaRandomNumberBytes() {
        Debug.println(String.format("Got MazamaRandomNumber: %s", getMazamaRandomNumber()));

        V mazamaRandomNumber = getMazamaRandomNumber();

        if (mazamaRandomNumber instanceof String string) return hexStringToByteArray(string);
        else if (mazamaRandomNumber instanceof byte[] bytes) return bytes;

        return null;
    }

    public byte[] getSerialNumberBytes() {
        Debug.println(String.format("Got SerialNumber: %s", getSerialNumber()));

        V serialNumber = getSerialNumber();

        if (serialNumber instanceof String string) return hexStringToByteArray(string);
        else if (serialNumber instanceof byte[] bytes) return bytes;

        return null;
    }

    public byte[] getIDStringBytes() {
        Debug.println(String.format("Got IDString: %s", getIDString()));

        V idString = getIDString();

        if (idString instanceof String string) return hexStringToByteArray(string);
        else if (idString instanceof byte[] bytes) return bytes;

        return null;
    }

    public byte[] getUsernameHashBytes() {
        Debug.println(String.format("Got UsernameHash: %s", getUsernameHash()));

        V usernameHash = getUsernameHash();

        if (usernameHash instanceof String string) return hexStringToByteArray(string);
        else if (usernameHash instanceof byte[] bytes) return bytes;

        return null;
    }

    public byte[] getUserNameBytes() {
        Debug.println(String.format("Got UserName: %s", getUserName()));

        V userName = getUserName();

        if (userName instanceof String string) return hexStringToByteArray(string);
        else if (userName instanceof byte[] bytes) return bytes;

        return null;
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

    /**
     * @return <code>kindle.account.tokens</code> as a byte array, or an empty byte array if the token is not present.
     */
    public byte[] genKindleAccountToken() {
        return getKindleAccountTokenBytesOrDefault(new byte[0]);
    }

    /**
     * @return <code>SerialNumber</code> as a byte array, or <code>IDString</code> as a byte array if <code>SerialNumber</code> is not present.
     */
    public byte[] genIdString() {
        return getSerialNumberBytesOrDefault(getIDStringBytes());
    }

    /**
     * @return The encoded MD5 hash of {@link #genIdString()} as a byte array.
     * @throws NoSuchAlgorithmException If the MD5 algorithm is not available.
     */
    public byte[] genEncodedIdString() throws NoSuchAlgorithmException {
        return encodeHash(genIdString(), charMap1);
    }

    /**
     * @return <code>UsernameHash</code> as a byte array, or the encoded MD5 hash of <code>UserName</code> as a byte array if <code>UsernameHash</code> is not present.
     * @throws NoSuchAlgorithmException If the MD5 algorithm is not available.
     */
    public byte[] genEncodedUsername() throws NoSuchAlgorithmException {
        return getUsernameHashBytesOrDefault(encodeHash(getUserNameBytes(), charMap1));
    }

    /**
     * @return <code>DSN</code> as a byte array, or the result of {@link #genAltDSN()} as a byte array if <code>DSN</code> is not present.
     * @throws NoSuchAlgorithmException if the SHA-1 algorithm is not available.
     */
    public byte[] genDSN() throws NoSuchAlgorithmException {
        byte[] derivedDSN = getDSNBytesOrDefault(genAltDSN());
        Debug.println(String.format("Derived DSN: %s", formatByteArray(derivedDSN)));
        return derivedDSN;
    }

    /**
     * @return The encoded SHA-1 hash of the concatenation of <code>MazamaRandomNumber</code>, {@link #genEncodedIdString()}, and {@link #genEncodedUsername()}, as a byte array.
     * @throws NoSuchAlgorithmException If the SHA-1 algorithm is not available.
     */
    private byte[] genAltDSN() throws NoSuchAlgorithmException {
        return encode(sha1(getMazamaRandomNumberBytes(), genEncodedIdString(), genEncodedUsername()), charMap1);
    }
}
