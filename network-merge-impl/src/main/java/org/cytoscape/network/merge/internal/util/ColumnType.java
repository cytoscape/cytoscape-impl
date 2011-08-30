
package org.cytoscape.network.merge.internal.util;

import java.util.EnumSet;
import org.cytoscape.model.CyColumn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jj
 */
public enum ColumnType {
    STRING(String.class, new CastService<String>() {
                @Override
                public String cast(Object from) {
                    return from.toString();
                }
            }),
    INTEGER(Integer.class, new CastService<Integer>() {
                @Override
                public Integer cast(Object from) {
                    return Integer.valueOf(from.toString());
                }
            }),
    LONG(Long.class, new CastService<Long>() {
                @Override
                public Long cast(Object from) {
                    return Long.valueOf(from.toString());
                }
            }),
    DOUBLE(Double.class, new CastService<Double>() {
                @Override
                public Double cast(Object from) {
                    return Double.valueOf(from.toString());
                }
            }),
    BOOLEAN(Boolean.class, new CastService<Boolean>() {
                @Override
                public Boolean cast(Object from) {
                    return Boolean.valueOf(from.toString());
                }
            }),
    LIST_STRING(String.class, null, true),
    LIST_INTEGER(Integer.class, null, true),
    LIST_LONG(Long.class, null, true),
    LIST_DOUBLE(Double.class, null, true),
    LIST_BOOLEAN(Boolean.class, null, true)
    ;
    
    private Class<?> type;
    private boolean isList;
    private CastService<?> castServ;
    
    private static final Map<Class<?>, ColumnType> plainTypes;
    private static final Map<Class<?>, ColumnType> listTypes;
    static {
        plainTypes = new HashMap<Class<?>, ColumnType>();
        listTypes = new HashMap<Class<?>, ColumnType>();
        for (ColumnType ct : ColumnType.values()) {
            if (ct.isList) {
                listTypes.put(ct.type, ct);
            } else {
                plainTypes.put(ct.type, ct);
            }
        }
    }
    
    ColumnType(Class<?> type, CastService castServ) {
        this(type, castServ, false);
    }
    
    ColumnType(Class<?> type, CastService castServ, boolean isList) {
        this.type = type;
        this.isList = isList;
        if (castServ==null)
            this.castServ = new CastService() {
                @Override
                public Object cast(Object from) {
                    throw new UnsupportedOperationException();
                }
            };
    }

    public boolean isList() {
        return isList;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        if (isList)
            return "List<"+type.getSimpleName()+">";
        else
            return type.getSimpleName();
    }
    
    public static ColumnType getType(CyColumn col) {
        Class<?> type = col.getClass();
        if (List.class.isAssignableFrom(type)) {
            return listTypes.get(col.getListElementType());
        } else {
            return plainTypes.get(type);
        }
    }
    
    public static boolean isConvertable(ColumnType from, ColumnType to) {
        if (from == to)
            return true;
        
        if (to == STRING)
            return true;
        
        if (from == INTEGER && (to == DOUBLE || to ==LONG))
            return true;
        
        if (from == LONG && to == DOUBLE)
            return true;
        
        if (to.isList && isConvertable(from.toPlain(), to.toPlain()))
            return true;
        
        return false;
    }
    
    public ColumnType toPlain() {
        return plainTypes.get(type);
    }
    
    public ColumnType toList() {
        return listTypes.get(type);
    }
    
    public Object castService(Object from) {
        return castServ.cast(from);
    }
    
    public static ColumnType getResonableCompatibleConvertionType(Set<ColumnType> types) {
        Iterator<ColumnType> it = types.iterator();
        ColumnType curr = it.next();
        boolean li = curr.isList;
        ColumnType ret = curr.toPlain();
        while (it.hasNext()) {
            curr = it.next();
            ColumnType plain = curr.toPlain();
            if (!isConvertable(plain, ret)) {
                ret = isConvertable(ret, plain) ? plain : STRING;
            }
            if (!li) {
                li = curr.isList;
            }
        }
        
        return li ? ret.toList() : ret;
    }
    
    public static Set<ColumnType> getConvertibleTypes(ColumnType fromType) {
        Set<ColumnType> types = EnumSet.noneOf(ColumnType.class);
        for (ColumnType type : ColumnType.values()) {
            if (isConvertable(fromType, type)) {
                types.add(type);
            }
        }
        return types;
    }

    interface CastService <T> {
        T cast(Object from);
    }
}
