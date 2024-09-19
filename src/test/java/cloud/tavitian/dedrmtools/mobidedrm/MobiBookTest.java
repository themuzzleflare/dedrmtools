/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

import cloud.tavitian.dedrmtools.PIDMetaInfo;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class MobiBookTest {
    @Test
    void getPidMetaInfo() throws Exception {
        // from test resources folder
        String book1 = ClassLoader.getSystemResource("einkbookfiles/1.azw3").getPath();
        String book2 = ClassLoader.getSystemResource("einkbookfiles/2.azw3").getPath();
        String book3 = ClassLoader.getSystemResource("einkbookfiles/3.azw3").getPath();
        String book4 = ClassLoader.getSystemResource("einkbookfiles/4.azw3").getPath();
        String book5 = ClassLoader.getSystemResource("einkbookfiles/5.azw3").getPath();

        MobiBook mobiBook1 = new MobiBook(book1);
        MobiBook mobiBook2 = new MobiBook(book2);
        MobiBook mobiBook3 = new MobiBook(book3);
        MobiBook mobiBook4 = new MobiBook(book4);
        MobiBook mobiBook5 = new MobiBook(book5);

        PIDMetaInfo pidMetaInfo1 = mobiBook1.getPidMetaInfo();
        PIDMetaInfo pidMetaInfo2 = mobiBook2.getPidMetaInfo();
        PIDMetaInfo pidMetaInfo3 = mobiBook3.getPidMetaInfo();
        PIDMetaInfo pidMetaInfo4 = mobiBook4.getPidMetaInfo();
        PIDMetaInfo pidMetaInfo5 = mobiBook5.getPidMetaInfo();

        byte[] token1 = {97, 116, 118, 58, 107, 105, 110, 58, 50, 58, 87, 98, 86, 80, 116, 54, 109, 69, 69, 68, 109, 69, 55, 115, 80, 98, 117, 98, 107, 83, 87, 48, 105, 112, 107, 82, 82, 52, 47, 72, 66, 114, 85, 68, 82, 121, 75, 115, 75, 47, 112, 76, 114, 76, 112, 86, 79, 118, 108, 55, 65, 108, 72, 90, 122, 110, 85, 83, 43, 81, 71, 43, 117, 121, 69, 68, 76, 48, 47, 82, 111, 117, 100, 106, 107, 81, 86, 72, 66, 119, 75, 120, 98, 71, 114, 78, 78, 66, 51, 100, 101, 122, 116, 78, 110, 52, 74, 83, 86, 110, 52, 106, 66, 99, 90, 98, 70, 101, 51, 83, 66, 54, 102, 69, 103, 88, 76, 116, 117, 69, 105, 114, 98, 50, 79, 80, 74, 77, 56, 57, 105, 75, 113, 66, 67, 107, 85, 72, 99, 73, 69, 77, 69, 115, 115, 108, 105, 106, 81, 79, 79, 115, 90, 87, 55, 98, 83, 102, 77, 65, 47, 118, 47, 117, 72, 109, 80, 88, 110, 89, 52, 61, 58, 86, 83, 57, 77, 101, 97, 82, 43, 74, 49, 83, 67, 48, 88, 107, 79, 87, 82, 53, 69, 113, 47, 105, 104, 121, 117, 119, 61, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        byte[] token2 = {97, 116, 118, 58, 107, 105, 110, 58, 50, 58, 104, 107, 109, 110, 68, 80, 121, 117, 70, 81, 51, 54, 119, 97, 109, 51, 54, 65, 72, 115, 107, 76, 102, 89, 115, 117, 50, 50, 72, 112, 43, 110, 106, 80, 116, 50, 75, 117, 68, 87, 106, 104, 72, 76, 112, 86, 79, 118, 108, 55, 65, 108, 72, 90, 122, 110, 85, 83, 43, 81, 71, 43, 117, 121, 69, 68, 76, 48, 47, 82, 111, 117, 100, 106, 107, 81, 86, 72, 66, 119, 75, 120, 98, 71, 114, 78, 78, 66, 51, 100, 101, 122, 116, 78, 110, 52, 74, 83, 86, 110, 52, 106, 66, 99, 90, 98, 70, 101, 51, 83, 66, 54, 102, 69, 103, 88, 76, 116, 117, 69, 105, 114, 98, 50, 79, 80, 74, 77, 56, 57, 105, 75, 113, 66, 67, 107, 85, 72, 99, 73, 69, 77, 69, 115, 115, 108, 105, 106, 81, 79, 79, 115, 90, 87, 55, 98, 83, 102, 77, 65, 47, 118, 47, 117, 72, 109, 80, 88, 110, 89, 52, 61, 58, 80, 107, 99, 71, 109, 116, 78, 72, 47, 57, 83, 68, 112, 70, 113, 117, 121, 71, 113, 102, 108, 54, 76, 77, 54, 97, 73, 61, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        byte[] token3 = {97, 116, 118, 58, 107, 105, 110, 58, 50, 58, 105, 73, 88, 101, 66, 54, 77, 113, 88, 122, 80, 77, 90, 118, 90, 57, 48, 83, 108, 113, 121, 83, 57, 51, 83, 100, 77, 86, 113, 113, 76, 88, 100, 102, 81, 75, 98, 66, 120, 70, 105, 108, 84, 76, 112, 86, 79, 118, 108, 55, 65, 108, 72, 90, 122, 110, 85, 83, 43, 81, 71, 43, 117, 121, 69, 68, 76, 48, 47, 82, 111, 117, 100, 106, 107, 81, 86, 72, 66, 119, 75, 120, 98, 71, 114, 78, 78, 66, 51, 100, 101, 122, 116, 78, 110, 52, 74, 83, 86, 110, 52, 106, 66, 99, 90, 98, 70, 101, 51, 83, 66, 54, 102, 69, 103, 88, 76, 116, 117, 69, 105, 114, 98, 50, 79, 80, 74, 77, 56, 57, 105, 75, 113, 66, 67, 107, 85, 72, 99, 73, 69, 77, 69, 115, 115, 108, 105, 106, 81, 79, 79, 115, 90, 87, 55, 98, 83, 102, 77, 65, 47, 118, 47, 117, 72, 109, 80, 88, 110, 89, 52, 61, 58, 116, 79, 105, 87, 116, 55, 122, 116, 77, 108, 89, 99, 43, 104, 85, 97, 116, 43, 119, 66, 85, 76, 114, 49, 49, 76, 89, 61, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        byte[] token4 = {97, 116, 118, 58, 107, 105, 110, 58, 50, 58, 117, 72, 78, 79, 76, 88, 76, 81, 115, 52, 74, 115, 121, 108, 103, 84, 116, 106, 77, 97, 102, 74, 105, 100, 98, 56, 67, 49, 70, 54, 84, 70, 75, 51, 82, 73, 110, 101, 56, 111, 113, 80, 114, 76, 112, 86, 79, 118, 108, 55, 65, 108, 72, 90, 122, 110, 85, 83, 43, 81, 71, 43, 117, 121, 69, 68, 76, 48, 47, 82, 111, 117, 100, 106, 107, 81, 86, 72, 66, 119, 75, 120, 98, 71, 114, 78, 78, 66, 51, 100, 101, 122, 116, 78, 110, 52, 74, 83, 86, 110, 52, 106, 66, 99, 90, 98, 70, 101, 51, 83, 66, 54, 102, 69, 103, 88, 76, 116, 117, 69, 105, 114, 98, 50, 79, 80, 74, 77, 56, 57, 105, 75, 113, 66, 67, 107, 85, 72, 99, 73, 69, 77, 69, 115, 115, 108, 105, 106, 81, 79, 79, 115, 90, 87, 55, 98, 83, 102, 77, 65, 47, 118, 47, 117, 72, 109, 80, 88, 110, 89, 52, 61, 58, 68, 88, 48, 101, 57, 102, 77, 54, 117, 83, 73, 83, 71, 67, 118, 118, 86, 54, 56, 104, 103, 109, 108, 67, 88, 67, 119, 61, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        byte[] token5 = {97, 116, 118, 58, 107, 105, 110, 58, 50, 58, 77, 111, 97, 103, 43, 101, 56, 70, 84, 80, 81, 70, 121, 51, 115, 68, 118, 69, 57, 74, 84, 50, 52, 47, 68, 48, 55, 90, 101, 53, 103, 120, 110, 82, 118, 82, 109, 87, 84, 52, 69, 101, 47, 76, 112, 86, 79, 118, 108, 55, 65, 108, 72, 90, 122, 110, 85, 83, 43, 81, 71, 43, 117, 121, 69, 68, 76, 48, 47, 82, 111, 117, 100, 106, 107, 81, 86, 72, 66, 119, 75, 120, 98, 71, 114, 78, 78, 66, 51, 100, 101, 122, 116, 78, 110, 52, 74, 83, 86, 110, 52, 106, 66, 99, 90, 98, 70, 101, 51, 83, 66, 54, 102, 69, 103, 88, 76, 116, 117, 69, 105, 114, 98, 50, 79, 80, 74, 77, 56, 57, 105, 75, 113, 66, 67, 107, 85, 72, 99, 73, 69, 77, 69, 115, 115, 108, 105, 106, 81, 79, 79, 115, 90, 87, 55, 98, 83, 102, 77, 65, 47, 118, 47, 117, 72, 109, 80, 88, 110, 89, 52, 61, 58, 98, 84, 83, 79, 70, 101, 67, 101, 53, 112, 115, 109, 66, 47, 48, 71, 80, 68, 71, 84, 118, 97, 115, 122, 116, 89, 115, 61, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        assertArrayEquals(token1, pidMetaInfo1.token());
        assertArrayEquals(token2, pidMetaInfo2.token());
        assertArrayEquals(token3, pidMetaInfo3.token());
        assertArrayEquals(token4, pidMetaInfo4.token());
        assertArrayEquals(token5, pidMetaInfo5.token());
    }

    @Test
    void processBook() throws Exception {
        // read non-DRM books from test resources folder
        byte[] noDrmBook1 = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("einkbookfilesnodrm/1.azw3")).readAllBytes();
        byte[] noDrmBook2 = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("einkbookfilesnodrm/2.azw3")).readAllBytes();
        byte[] noDrmBook3 = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("einkbookfilesnodrm/3.azw3")).readAllBytes();
        byte[] noDrmBook4 = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("einkbookfilesnodrm/4.azw3")).readAllBytes();
        byte[] noDrmBook5 = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("einkbookfilesnodrm/5.azw3")).readAllBytes();

        // retrieve the paths for the test books
        String book1 = ClassLoader.getSystemResource("einkbookfiles/1.azw3").getPath();
        String book2 = ClassLoader.getSystemResource("einkbookfiles/2.azw3").getPath();
        String book3 = ClassLoader.getSystemResource("einkbookfiles/3.azw3").getPath();
        String book4 = ClassLoader.getSystemResource("einkbookfiles/4.azw3").getPath();
        String book5 = ClassLoader.getSystemResource("einkbookfiles/5.azw3").getPath();

        // instantiate classes
        MobiBook mobiBook1 = new MobiBook(book1);
        MobiBook mobiBook2 = new MobiBook(book2);
        MobiBook mobiBook3 = new MobiBook(book3);
        MobiBook mobiBook4 = new MobiBook(book4);
        MobiBook mobiBook5 = new MobiBook(book5);

        // process the books
        mobiBook1.processBook("vCNIml/cF7");
        mobiBook2.processBook("JBJfi+WmJC");
        mobiBook3.processBook("5m9pZCYOGG");
        mobiBook4.processBook("bEQyy4RzR3");
        mobiBook5.processBook("EGnqh3QSPS");

        // assert the decrypted book data
        assertArrayEquals(noDrmBook1, mobiBook1.mobiData);
        assertArrayEquals(noDrmBook2, mobiBook2.mobiData);
        assertArrayEquals(noDrmBook3, mobiBook3.mobiData);
        assertArrayEquals(noDrmBook4, mobiBook4.mobiData);
        assertArrayEquals(noDrmBook5, mobiBook5.mobiData);
    }
}
