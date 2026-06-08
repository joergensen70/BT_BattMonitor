package com.journeyapps.barcodescanner;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class Decoder implements ResultPointCallback {
    private List<ResultPoint> possibleResultPoints = new ArrayList();
    private Reader reader;

    public Decoder(Reader reader) {
        this.reader = reader;
    }

    protected Reader getReader() {
        return this.reader;
    }

    public Result decode(LuminanceSource luminanceSource) {
        return decode(toBitmap(luminanceSource));
    }

    protected BinaryBitmap toBitmap(LuminanceSource luminanceSource) {
        return new BinaryBitmap(new HybridBinarizer(luminanceSource));
    }

    protected Result decode(BinaryBitmap binaryBitmap) {
        this.possibleResultPoints.clear();
        try {
            Reader reader = this.reader;
            if (reader instanceof MultiFormatReader) {
                Result resultDecodeWithState = ((MultiFormatReader) reader).decodeWithState(binaryBitmap);
                this.reader.reset();
                return resultDecodeWithState;
            }
            Result resultDecode = reader.decode(binaryBitmap);
            this.reader.reset();
            return resultDecode;
        } catch (Exception unused) {
            this.reader.reset();
            return null;
        } catch (Throwable th) {
            this.reader.reset();
            throw th;
        }
    }

    public List<ResultPoint> getPossibleResultPoints() {
        return new ArrayList(this.possibleResultPoints);
    }

    @Override // com.google.zxing.ResultPointCallback
    public void foundPossibleResultPoint(ResultPoint resultPoint) {
        this.possibleResultPoints.add(resultPoint);
    }
}
