/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.RefModel;

import java.lang.reflect.Field;

/**
 *
 * @author Lemmin
 */
public class RefModel extends Ref {

    public static <T extends RefModel> T compile(Class<T> rootCls) throws InstantiationException, IllegalAccessException {
        RefModel newInstance = rootCls.newInstance();
        newInstance.local = "";
        newInstance.relative = "";
        newInstance.compileInner(null);
        return (T) newInstance;
    }

    private void compileInner(Ref parent) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        String substring = "";
        if (parent != null && !parent.relative.isEmpty()) {
            substring = parent.relative + ".";
        }
        Class cls = this.getClass();
        Field[] fields = cls.getFields();
        for (Field f : fields) {
            Class<?> type = f.getType();
            if (Ref.class.isAssignableFrom(type)) {
                Ref ref = (Ref) type.newInstance();
                f.set(this, ref);
                ref.local = f.getName();
                ref.relative = substring + ref.local;
                if (RefModel.class.isAssignableFrom(type)) {
                    RefModel model = (RefModel) ref;
                    model.compileInner(model);
                    f.set(this, model);
                }
            }
        }
    }

}
