package bolts;

import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class AggregateException extends Exception {
    private static final long serialVersionUID = 1;
    private Throwable[] causes;

    public AggregateException(String str, Throwable[] thArr) {
        super(str, (thArr == null || thArr.length <= 0) ? null : thArr[0]);
        this.causes = (thArr == null || thArr.length <= 0) ? null : thArr;
    }

    @Deprecated
    public AggregateException(List<Exception> list) {
        this("There were multiple errors.", (Throwable[]) list.toArray(new Exception[list.size()]));
    }

    @Deprecated
    public List<Exception> getErrors() {
        ArrayList arrayList = new ArrayList();
        Throwable[] thArr = this.causes;
        if (thArr != null) {
            for (Throwable th : thArr) {
                if (th instanceof Exception) {
                    arrayList.add((Exception) th);
                } else {
                    arrayList.add(new Exception(th));
                }
            }
        }
        return arrayList;
    }

    public Throwable[] getCauses() {
        return this.causes;
    }
}
