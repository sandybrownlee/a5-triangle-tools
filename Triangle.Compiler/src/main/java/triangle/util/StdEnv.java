package triangle.util;

import triangle.repr.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static triangle.repr.Type.*;

public final class StdEnv {

    public static final Map<String, Type> STD_TERMS = new HashMap<>();

    //@formatter:off
    public static final  Map<String, Type> STD_TYPES =
            Map.of(
                    "Integer", INT_TYPE,
                    "Char", CHAR_TYPE,
                    "Boolean", BOOL_TYPE
            );

    // binary/unary operations known to be pure
    public static final  Set<String> PURE_OPS = Set.of(
            "\\/",
            "/\\",
            "<=",
            ">=",
            ">",
            "<",
            "\\",
            "-",
            "+",
            "*",
            "/",
            "//",
            "|"
    );
    //@formatter:on

    private static final Type RELATION            = new Type.PrimType.FuncType(List.of(BOOL_TYPE, BOOL_TYPE), BOOL_TYPE);
    private static final Type BINARY_INT_RELATION = new Type.PrimType.FuncType(List.of(INT_TYPE, INT_TYPE), BOOL_TYPE);
    private static final Type BINARY_INT_FUNC     = new Type.PrimType.FuncType(List.of(INT_TYPE, INT_TYPE), INT_TYPE);//@formatter:off

    //@formatter:off
    static {
        STD_TERMS.putAll(
                Map.of(
                        "\\/", RELATION,
                        "/\\", RELATION,
                        "<=",  BINARY_INT_RELATION,
                        ">=",  BINARY_INT_RELATION,
                        ">",   BINARY_INT_RELATION,
                        "<",   BINARY_INT_RELATION,
                        "\\",  new PrimType.FuncType(List.of(BOOL_TYPE), BOOL_TYPE)
                ));

        STD_TERMS.putAll(
                Map.of(
                        "-",  BINARY_INT_FUNC,
                        "+",  BINARY_INT_FUNC,
                        "*",  BINARY_INT_FUNC,
                        "/",  BINARY_INT_FUNC,
                        "//", BINARY_INT_FUNC,
                        "|",  new PrimType.FuncType(List.of(INT_TYPE), INT_TYPE),
                        "++", new PrimType.FuncType(List.of(INT_TYPE), INT_TYPE),
                        "**", new PrimType.FuncType(List.of(INT_TYPE), INT_TYPE)
                ));

        // these are set to VOID so that we (hopefully) fail fast in case something tries to access their types
        STD_TERMS.putAll(
                Map.of(
                        "=", VOID_TYPE,
                        "\\=", VOID_TYPE
                ));

        STD_TERMS.putAll(
                Map.of(
                        "get",      new PrimType.FuncType(List.of(new RefOf(CHAR_TYPE)), VOID_TYPE),
                        "getint",   new PrimType.FuncType(List.of(new RefOf(INT_TYPE)), VOID_TYPE),
                        "geteol",   new PrimType.FuncType(List.of(), VOID_TYPE),
                        "puteol",   new PrimType.FuncType(List.of(), VOID_TYPE),
                        "put",      new PrimType.FuncType(List.of(CHAR_TYPE), VOID_TYPE),
                        "putint",   new PrimType.FuncType(List.of(INT_TYPE), VOID_TYPE),
                        "chr",      new PrimType.FuncType(List.of(INT_TYPE), CHAR_TYPE),
                        "eol",      new PrimType.FuncType(List.of(), BOOL_TYPE),
                        "ord",      new PrimType.FuncType(List.of(CHAR_TYPE), INT_TYPE)
                ));
    }
    //@formatter:on

    private StdEnv() {
        throw new IllegalStateException("Utility class");
    }

}
