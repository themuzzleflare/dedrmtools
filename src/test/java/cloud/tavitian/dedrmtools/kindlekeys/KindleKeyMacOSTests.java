/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

@DisplayName("KindleKeyMacOS Tests")
final class KindleKeyMacOSTests {
    @Test
    @DisplayName("Get Mac Addresses Munged Test")
    void getMacAddressesMunged() {
        Set<byte[]> macAddressesMunged = KindleKeyMacOS.getMacAddressesMunged();
        System.out.printf("macAddressesMunged: %s%n", macAddressesMunged);
    }

    @Test
    @DisplayName("Get Volume Serial Numbers Test")
    void getVolumeSerialNumbers() {
        Set<byte[]> volumeSerialNumbers = KindleKeyMacOS.getVolumeSerialNumbers();
        System.out.printf("volumeSerialNumbers: %s%n", volumeSerialNumbers);
    }

    @Test
    @DisplayName("Get Disk Partition Names Test")
    void getDiskPartitionNames() {
        Set<byte[]> diskPartitionNames = KindleKeyMacOS.getDiskPartitionNames();
        System.out.printf("diskPartitionNames: %s%n", diskPartitionNames);
    }

    @Test
    @DisplayName("Get Disk Partition UUIDs Test")
    void getDiskPartitionUUIDs() {
        Set<byte[]> diskPartitionUUIDs = KindleKeyMacOS.getDiskPartitionUUIDs();
        System.out.printf("diskPartitionUUIDs: %s%n", diskPartitionUUIDs);
    }

    @Test
    @DisplayName("Get ID Strings Test")
    void getIdStrings() {
        Set<byte[]> idStrings = KindleKeyMacOS.getIdStrings();
        System.out.printf("idStrings: %s%n", idStrings);
    }

    @Test
    @DisplayName("Get Username Test")
    void getUsername() {
        byte[] username = new KindleKeyMacOS().getUsername();
        System.out.printf("username: %s%n", Util.formatByteArray(username));
    }
}
