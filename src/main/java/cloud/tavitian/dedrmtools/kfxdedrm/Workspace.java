/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cloud.tavitian.dedrmtools.Util.toIntegerList;

final class Workspace {
    private List<Integer> work;

    public Workspace(List<Integer> initialList) {
        work = initialList;
    }

    public Workspace(int[] initialList) {
        this(toIntegerList(initialList));
    }

    public void shuffle(int[] shufList) {
        shuffle(toIntegerList(shufList));
    }

    public void shuffle(@NotNull List<Integer> shufList) {
        List<Integer> rt = new ArrayList<>();

        for (Integer integer : shufList) rt.add(work.get(integer));

        work = rt;
    }

    public void sbox(List<Integer> table, List<Integer> matrix, int[] skpList) {
        sbox(table, matrix, toIntegerList(skpList));
    }

    public void sbox(List<Integer> table, List<Integer> matrix) {
        sbox(table, matrix, Collections.emptyList());
    }

    public void sbox(List<Integer> table, List<Integer> matrix, List<Integer> skpList) {
        if (skpList == null) skpList = Collections.emptyList();

        int offset = 0;

        List<Integer> nwork = new ArrayList<>(work);

        int wo = 0;
        int toff = 0;

        while (offset < 0x6000) {
            int uv5 = table.get(toff + nwork.get(wo));
            int uv1 = table.get(toff + nwork.get(wo + 1) + 0x100);
            int uv2 = table.get(toff + nwork.get(wo + 2) + 0x200);
            int uv3 = table.get(toff + nwork.get(wo + 3) + 0x300);
            int moff = 0;

            int nib1 = 0;
            int nib2 = 0;
            int nib3 = 0;
            int nib4 = 0;

            if (skpList.contains(0)) moff += 0x400;
            else {
                nib1 = matrix.get(moff + offset + ((uv1 >> 0x1c) & 0xf) | ((uv5 >> 0x18) & 0xf0));
                moff += 0x100;
                nib2 = matrix.get(moff + offset + ((uv3 >> 0x1c) & 0xf) | ((uv2 >> 0x18) & 0xf0));
                moff += 0x100;
                nib3 = matrix.get(moff + offset + ((uv1 >> 0x18) & 0xf) | ((uv5 >> 0x14) & 0xf0));
                moff += 0x100;
                nib4 = matrix.get(moff + offset + ((uv3 >> 0x18) & 0xf) | ((uv2 >> 0x14) & 0xf0));
                moff += 0x100;
            }

            int rnib1 = matrix.get(moff + offset + nib1 * 0x10 + nib2);
            moff += 0x100;
            int rnib2 = matrix.get(moff + offset + nib3 * 0x10 + nib4);
            moff += 0x100;
            nwork.set(wo, rnib1 * 0x10 + rnib2);

            if (skpList.contains(1)) moff += 0x400;
            else {
                nib1 = matrix.get(moff + offset + ((uv1 >> 0x14) & 0xf) | ((uv5 >> 0x10) & 0xf0));
                moff += 0x100;
                nib2 = matrix.get(moff + offset + ((uv3 >> 0x14) & 0xf) | ((uv2 >> 0x10) & 0xf0));
                moff += 0x100;
                nib3 = matrix.get(moff + offset + ((uv1 >> 0x10) & 0xf) | ((uv5 >> 0xc) & 0xf0));
                moff += 0x100;
                nib4 = matrix.get(moff + offset + ((uv3 >> 0x10) & 0xf) | ((uv2 >> 0xc) & 0xf0));
                moff += 0x100;
            }

            rnib1 = matrix.get(moff + offset + nib1 * 0x10 + nib2);
            moff += 0x100;
            rnib2 = matrix.get(moff + offset + nib3 * 0x10 + nib4);
            moff += 0x100;
            nwork.set(wo + 1, rnib1 * 0x10 + rnib2);

            if (skpList.contains(2)) moff += 0x400;
            else {
                nib1 = matrix.get(moff + offset + ((uv1 >> 0xc) & 0xf) | ((uv5 >> 0x8) & 0xf0));
                moff += 0x100;
                nib2 = matrix.get(moff + offset + ((uv3 >> 0xc) & 0xf) | ((uv2 >> 0x8) & 0xf0));
                moff += 0x100;
                nib3 = matrix.get(moff + offset + ((uv1 >> 0x8) & 0xf) | ((uv5 >> 0x4) & 0xf0));
                moff += 0x100;
                nib4 = matrix.get(moff + offset + ((uv3 >> 0x8) & 0xf) | ((uv2 >> 0x4) & 0xf0));
                moff += 0x100;
            }

            rnib1 = matrix.get(moff + offset + nib1 * 0x10 + nib2);
            moff += 0x100;
            rnib2 = matrix.get(moff + offset + nib3 * 0x10 + nib4);
            moff += 0x100;
            nwork.set(wo + 2, rnib1 * 0x10 + rnib2);

            if (skpList.contains(3)) moff += 0x400;
            else {
                nib1 = matrix.get(moff + offset + ((uv1 >> 0x4) & 0xf) | (uv5 & 0xf0));
                moff += 0x100;
                nib2 = matrix.get(moff + offset + ((uv3 >> 0x4) & 0xf) | (uv2 & 0xf0));
                moff += 0x100;
                nib3 = matrix.get(moff + offset + (uv1 & 0xf) | ((uv5 << 4) & 0xf0));
                moff += 0x100;
                nib4 = matrix.get(moff + offset + (uv3 & 0xf) | ((uv2 << 4) & 0xf0));
                moff += 0x100;
            }

            rnib1 = matrix.get(moff + offset + nib1 * 0x10 + nib2);
            moff += 0x100;
            rnib2 = matrix.get(moff + offset + nib3 * 0x10 + nib4);
            //noinspection UnusedAssignment
            moff += 0x100;
            nwork.set(wo + 3, rnib1 * 0x10 + rnib2);

            offset += 0x1800;
            wo += 4;
            toff += 0x400;
        }

        work = nwork;
    }

    public void exlookup(List<Integer> ltable) {
        int lookoffs = 0;

        for (int a = 0; a < work.size(); a++) {
            work.set(a, ltable.get(work.get(a) + lookoffs));
            lookoffs += 0x100;
        }
    }

    public @NotNull List<Integer> mask(byte @NotNull [] chunk) {
        List<Integer> out = new ArrayList<>();

        for (int a = 0; a < chunk.length; a++) {
            work.set(a, work.get(a) ^ chunk[a]);
            out.add(work.get(a));
        }

        return out;
    }
}
