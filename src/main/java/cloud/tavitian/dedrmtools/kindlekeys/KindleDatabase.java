/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Debug;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

import static cloud.tavitian.dedrmtools.CharMaps.charMap1;
import static cloud.tavitian.dedrmtools.HashUtils.sha1;
import static cloud.tavitian.dedrmtools.Util.formatByteArray;
import static cloud.tavitian.dedrmtools.Util.hexStringToByteArray;
import static cloud.tavitian.dedrmtools.kindlekeys.KindleKeyUtils.encode;
import static cloud.tavitian.dedrmtools.kindlekeys.KindleKeyUtils.encodeHash;

public class KindleDatabase extends LinkedHashMap<String, String> {
    private static final Gson GSON = new Gson();

    private static final String KINDLE_ACCOUNT_TOKENS_KEY = "kindle.account.tokens";
    private static final String DSN_KEY = "DSN";
    private static final String MAZAMA_RANDOM_NUMBER_KEY = "MazamaRandomNumber";
    private static final String SERIAL_NUMBER_KEY = "SerialNumber";
    private static final String ID_STRING_KEY = "IDString";
    private static final String USERNAME_HASH_KEY = "UsernameHash";
    private static final String USER_NAME_KEY = "UserName";

    private static final String KINDLE_COOKIE_ITEM_KEY = "kindle.cookie.item";
    private static final String EULA_VERSION_ACCEPTED_KEY = "eulaVersionAccepted";
    private static final String LOGIN_DATE_KEY = "login_date";
    private static final String KINDLE_TOKEN_ITEM_KEY = "kindle.token.item";
    private static final String LOGIN_KEY = "login";
    private static final String KINDLE_KEY_ITEM_KEY = "kindle.key.item";
    private static final String KINDLE_NAME_INFO_KEY = "kindle.name.info";
    private static final String KINDLE_DEVICE_INFO_KEY = "kindle.device.info";
    private static final String MAX_DATE_KEY = "max_date";
    private static final String SIG_VERIF_KEY = "SIGVERIF";
    private static final String BUILD_VERSION_KEY = "build_version";
    private static final String KINDLE_DIRECTED_ID_INFO_KEY = "kindle.directedid.info";
    private static final String KINDLE_ACCOUNT_TYPE_INFO_KEY = "kindle.accounttype.info";
    private static final String FLASHCARDS_PLUGIN_DATA_ENCRYPTION_KEY_KEY = "krx.flashcardsplugin.data.encryption_key";
    private static final String NOTEBOOK_EXPORT_PLUGIN_DATA_ENCRYPTION_KEY_KEY = "krx.notebookexportplugin.data.encryption_key";
    private static final String PROXY_HTTP_PASSWORD_KEY = "proxy.http.password";
    private static final String PROXY_HTTP_USERNAME_KEY = "proxy.http.username";

    public static byte[][] keyBytesList = new byte[][]{
            KINDLE_ACCOUNT_TOKENS_KEY.getBytes(StandardCharsets.US_ASCII),
            KINDLE_COOKIE_ITEM_KEY.getBytes(StandardCharsets.US_ASCII),
            EULA_VERSION_ACCEPTED_KEY.getBytes(StandardCharsets.US_ASCII),
            LOGIN_DATE_KEY.getBytes(StandardCharsets.US_ASCII),
            KINDLE_TOKEN_ITEM_KEY.getBytes(StandardCharsets.US_ASCII),
            LOGIN_KEY.getBytes(StandardCharsets.US_ASCII),
            KINDLE_KEY_ITEM_KEY.getBytes(StandardCharsets.US_ASCII),
            KINDLE_NAME_INFO_KEY.getBytes(StandardCharsets.US_ASCII),
            KINDLE_DEVICE_INFO_KEY.getBytes(StandardCharsets.US_ASCII),
            MAZAMA_RANDOM_NUMBER_KEY.getBytes(StandardCharsets.US_ASCII),
            MAX_DATE_KEY.getBytes(StandardCharsets.US_ASCII),
            SIG_VERIF_KEY.getBytes(StandardCharsets.US_ASCII),
            BUILD_VERSION_KEY.getBytes(StandardCharsets.US_ASCII),
            SERIAL_NUMBER_KEY.getBytes(StandardCharsets.US_ASCII),
            USERNAME_HASH_KEY.getBytes(StandardCharsets.US_ASCII),
            KINDLE_DIRECTED_ID_INFO_KEY.getBytes(StandardCharsets.US_ASCII),
            DSN_KEY.getBytes(StandardCharsets.US_ASCII),
            KINDLE_ACCOUNT_TYPE_INFO_KEY.getBytes(StandardCharsets.US_ASCII),
            FLASHCARDS_PLUGIN_DATA_ENCRYPTION_KEY_KEY.getBytes(StandardCharsets.US_ASCII),
            NOTEBOOK_EXPORT_PLUGIN_DATA_ENCRYPTION_KEY_KEY.getBytes(StandardCharsets.US_ASCII),
            PROXY_HTTP_PASSWORD_KEY.getBytes(StandardCharsets.US_ASCII),
            PROXY_HTTP_USERNAME_KEY.getBytes(StandardCharsets.US_ASCII)
    };

    public KindleDatabase() {
        super();
    }

    @SuppressWarnings("unused")
    public KindleDatabase(File file) throws IOException {
        this(file.getAbsolutePath());
    }

    public KindleDatabase(String filename) throws IOException {
        super();
        putAll(loadFromFile(filename));
    }

    @SuppressWarnings("unused")
    public static KindleDatabase loadFromFile(File file) throws IOException {
        return loadFromFile(file.getAbsolutePath());
    }

    public static KindleDatabase loadFromFile(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        KindleDatabase result = GSON.fromJson(fileReader, KindleDatabase.class);
        fileReader.close();
        return result;
    }

    public void writeToFile(File file) throws IOException {
        writeToFile(file.getAbsolutePath());
    }

    public void writeToFile(String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        GSON.toJson(this, fileWriter);
        fileWriter.close();
    }

    public String getKindleAccountToken() {
        return get(KINDLE_ACCOUNT_TOKENS_KEY);
    }

    public String getDSN() {
        return get(DSN_KEY);
    }

    public String getMazamaRandomNumber() {
        return get(MAZAMA_RANDOM_NUMBER_KEY);
    }

    public String getSerialNumber() {
        return get(SERIAL_NUMBER_KEY);
    }

    public String getIDString() {
        return get(ID_STRING_KEY);
    }

    public String getUsernameHash() {
        return get(USERNAME_HASH_KEY);
    }

    public String getUserName() {
        return get(USER_NAME_KEY);
    }

    @SuppressWarnings("unused")
    public String getKindleAccountTokenOrDefault(String defaultValue) {
        return getOrDefault(KINDLE_ACCOUNT_TOKENS_KEY, defaultValue);
    }

    @SuppressWarnings("unused")
    public String getDSNOrDefault(String defaultValue) {
        return getOrDefault(DSN_KEY, defaultValue);
    }

    @SuppressWarnings("unused")
    public String getMazamaRandomNumberOrDefault(String defaultValue) {
        return getOrDefault(MAZAMA_RANDOM_NUMBER_KEY, defaultValue);
    }

    @SuppressWarnings("unused")
    public String getSerialNumberOrDefault(String defaultValue) {
        return getOrDefault(SERIAL_NUMBER_KEY, defaultValue);
    }

    @SuppressWarnings("unused")
    public String getIDStringOrDefault(String defaultValue) {
        return getOrDefault(ID_STRING_KEY, defaultValue);
    }

    @SuppressWarnings("unused")
    public String getUsernameHashOrDefault(String defaultValue) {
        return getOrDefault(USERNAME_HASH_KEY, defaultValue);
    }

    @SuppressWarnings("unused")
    public String getUserNameOrDefault(String defaultValue) {
        return getOrDefault(USER_NAME_KEY, defaultValue);
    }

    public boolean containsKindleAccountToken() {
        return containsKey(KINDLE_ACCOUNT_TOKENS_KEY);
    }

    public boolean containsDSN() {
        return containsKey(DSN_KEY);
    }

    public boolean containsMazamaRandomNumber() {
        return containsKey(MAZAMA_RANDOM_NUMBER_KEY);
    }

    public boolean containsSerialNumber() {
        return containsKey(SERIAL_NUMBER_KEY);
    }

    public boolean containsIDString() {
        return containsKey(ID_STRING_KEY);
    }

    public boolean containsUsernameHash() {
        return containsKey(USERNAME_HASH_KEY);
    }

    public boolean containsUserName() {
        return containsKey(USER_NAME_KEY);
    }

    public byte[] getKindleAccountTokenBytes() {
        Debug.printf("Got Kindle Account Token: %s%n", getKindleAccountToken());
        return hexStringToByteArray(getKindleAccountToken());
    }

    public byte[] getDSNBytes() {
        Debug.printf("Got DSN: %s%n", getDSN());
        return hexStringToByteArray(getDSN());
    }

    public byte[] getMazamaRandomNumberBytes() {
        Debug.printf("Got MazamaRandomNumber: %s%n", getMazamaRandomNumber());
        return hexStringToByteArray(getMazamaRandomNumber());
    }

    public byte[] getSerialNumberBytes() {
        Debug.printf("Got SerialNumber: %s%n", getSerialNumber());
        return hexStringToByteArray(getSerialNumber());
    }

    public byte[] getIDStringBytes() {
        Debug.printf("Got IDString: %s%n", getIDString());
        return hexStringToByteArray(getIDString());
    }

    public byte[] getUsernameHashBytes() {
        Debug.printf("Got UsernameHash: %s%n", getUsernameHash());
        return hexStringToByteArray(getUsernameHash());
    }

    public byte[] getUserNameBytes() {
        Debug.printf("Got UserName: %s%n", getUserName());
        return hexStringToByteArray(getUserName());
    }

    public byte[] getKindleAccountTokenBytesOrDefault(byte[] defaultValue) {
        return containsKindleAccountToken() ? getKindleAccountTokenBytes() : defaultValue;
    }

    public byte[] getDSNBytesOrDefault(byte[] defaultValue) {
        return containsDSN() ? getDSNBytes() : defaultValue;
    }

    @SuppressWarnings("unused")
    public byte[] getMazamaRandomNumberBytesOrDefault(byte[] defaultValue) {
        return containsMazamaRandomNumber() ? getMazamaRandomNumberBytes() : defaultValue;
    }

    public byte[] getSerialNumberBytesOrDefault(byte[] defaultValue) {
        return containsSerialNumber() ? getSerialNumberBytes() : defaultValue;
    }

    @SuppressWarnings("unused")
    public byte[] getIDStringBytesOrDefault(byte[] defaultValue) {
        return containsIDString() ? getIDStringBytes() : defaultValue;
    }

    public byte[] getUsernameHashBytesOrDefault(byte[] defaultValue) {
        return containsUsernameHash() ? getUsernameHashBytes() : defaultValue;
    }

    @SuppressWarnings("unused")
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
        Debug.printf("Derived DSN: %s%n", formatByteArray(derivedDSN));
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
