package reobf.proghatches.gt.cover.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

//spotless:off
public class SimpleParser {

    static enum OPType {
        FUNC,
        BIOP,
        TRIOP,
        BRACKET,
        SIOP

    }

    static enum Operation {

        ADD(OPType.BIOP, "ADD",
            simplebi((a, b) -> a + b, (a, b) -> (new Rational(a.num * b.den + a.den * b.num, a.den * b.den)))),
        SUB(OPType.BIOP, "SUB",
            simplebi((a, b) -> a - b, (a, b) -> (new Rational(a.num * b.den - a.den * b.num, a.den * b.den)))),
        MUL(OPType.BIOP, "MUL", simplebi((a, b) -> a * b, (a, b) -> (new Rational(a.num * b.num, a.den * b.den)))),
        DIV(OPType.BIOP, "DIV", simplebi((a, b) -> a / b, (a, b) -> {
            if (b.num == 0) throw new ArithmeticException();
            return (new Rational(a.num * b.den, a.den * b.num));
        })),
        MOD(OPType.BIOP, "MOD", simplebi((a, b) -> a % b, (a, b) -> {
            if (b.num == 0) throw new ArithmeticException();
            Rational aa = (Rational) a.clone();
            Rational bb = (Rational) b.clone();
            bb.num = Math.abs(bb.num);
            bb.den = Math.abs(bb.den);
            int neg = (aa.num > 0) == (aa.den > 0) ? 1 : -1;
            aa.num = Math.abs(aa.num);
            aa.den = Math.abs(aa.den);
            int result = (aa.num * bb.den) % (aa.den * bb.num);

            return new Rational(neg * result, bb.den * aa.den);
        })),
        SEL(OPType.TRIOP, "TERNARY", (e, c) -> {
            Number aa = (Number) resolve(e, c, 0);
            Object bb = resolve(e, c, 1);
            Object cc = resolve(e, c, 2);
            return Math.round((aa).doubleValue()) != 0 ? (bb) : (cc);
        }
        // ,(RALogic)null//(e, c) ->{return
        // (((((Rational)resolve(e,c,0)).intValue())!=0)?(resolve(e,c,1)):(resolve(e,c,2)));}

        ),
        BRC(OPType.BRACKET, "", (e, c) -> {
            if (e.sub.size() > 1) {
                throw new RuntimeException("tuple unsupported");
            }
            return (resolve(e, c, 0));
        }),
        BRC_(OPType.BRACKET, "", (e, c) -> {
            if (e.sub.size() > 1) {
                throw new RuntimeException("tuple unsupported");
            }
            return (resolve(e, c, 0));
        }),
        VAR(OPType.SIOP, "VAR", (Expression e, Context c) -> {
            String key = e.sub.stream()
                .map(s -> s.s)
                .collect(StringBuilder::new, StringBuilder::append, (a, b) -> {})
                .toString();
            if (key.startsWith("\"") && key.endsWith("\"") && key.length() >= 2) {
                String s = key.substring(1, key.length() - 1);
                return s;
            }
            Object o = c.get(key);
            if (o instanceof Integer) {
                return new Rational((Integer) o, 1);
            }
            return o;
        }),
        CST(OPType.SIOP, "CONST", (Expression e, Context c) -> {
            String key = e.sub.stream()
                .map(s -> s.s)
                .collect(StringBuilder::new, StringBuilder::append, (a, b) -> {})
                .toString();

            try {
                int i = Integer.valueOf(key);
                return new Rational(i, 1);
            } catch (Exception aa) {}
            try {
                String[] i = key.split("\\.");
                int a = Integer.valueOf(i[0]);
                int b = Integer.valueOf(i[1]);
                int length = i[1].length();
                int pw = (int) Math.pow(10, length);
                return new Rational(a * pw + b, pw);
            } catch (Exception aa) {}

            return Double.valueOf(key);
        }),
        FUN(OPType.SIOP, "FUNCTION"),
        CMM(OPType.BRACKET, ""),
        @SuppressWarnings("unchecked")
        CALL(OPType.FUNC, "CALL", (Expression e, Context c) -> {
            if (e.sub.size() != 2) {
                throw new RuntimeException("malformed call");
            }
            // if(e.sub.get(0).ex.op!=Operation.CMM){throw new RuntimeException("missing target");}
            String key = e.sub.get(0).ex.sub.stream()
                .map(s -> s.s)
                .collect(StringBuilder::new, StringBuilder::append, (a, b) -> {})
                .toString();
            ArrayList<Object> args = new ArrayList<>();
            // System.err.println(e.sub.get(1).ex.op);
            if (e.sub.get(1).ex.op == Operation.BRC || e.sub.get(1).ex.op == Operation.BRC_) {} else {
                throw new RuntimeException("malformed call");
            }
            if ((e.sub.get(1).ex.sub.isEmpty() == false) && e.sub.get(1).ex.sub.get(0).ex.sub.size() > 1) {
                throw new RuntimeException("malformed call");
            }

            if (e.sub.get(1).ex.sub.isEmpty()) {} else {
                Expression ex = e.sub.get(1).ex.sub.get(0).ex;
                if (ex.op == Operation.CMM) {

                    ex.sub.get(0).ex.sub.forEach(s -> { args.add(s.ex.evaluate(c)); });

                } else {
                    args.add(ex.evaluate(c));;

                }
            }

            Object v = c.get(key);
            if (v == null) throw new RuntimeException("no such function");

            // System.out.println(key);System.out.println(args);
            try {
                return ((Number) ((Function<List<Object>, Object>) v).apply(args));
            } catch (ClassCastException ee) {
                throw new RuntimeException("not callable: " + v);
            } catch (Exception ee) {
                throw new RuntimeException("failed to call", ee);
            }
            /**/
            // return 0;
        }),
        EQ(OPType.BIOP, "EQUALS", simplebi((a, b) -> a == b ? 1 : 0)),
        NEQ(OPType.BIOP, "N-EQUALS", simplebi((a, b) -> a != b ? 1 : 0)),
        GE(OPType.BIOP, "GREATER-EQUALS", simplebi((a, b) -> a >= b ? 1 : 0)),
        LE(OPType.BIOP, "LESS-EQUALS", simplebi((a, b) -> a <= b ? 1 : 0)),
        L(OPType.BIOP, "LESS", simplebi((a, b) -> a < b ? 1 : 0)),
        G(OPType.BIOP, "GREATER", simplebi((a, b) -> a > b ? 1 : 0));

        static Handler simplebi(BILogic v) {
            return (e, c) -> v
                .evaluate(((Number) resolve(e, c, 0)).doubleValue(), ((Number) resolve(e, c, 1)).doubleValue());
        }

        static Handler simplebi(BILogic v, RALogic r) {

            return (e, c) -> {

                Number a = (Number) resolve(e, c, 0);
                Number b = (Number) resolve(e, c, 1);
                if (a instanceof Rational && b instanceof Rational) return r.evaluate(((Rational) a), ((Rational) b));
                return v.evaluate((a).doubleValue(), (b).doubleValue());
            };
        }

        static public Object resolve(Expression e, Context c, int index) {

            return e.sub.get(index).ex.evaluate(c);
        }

        OPType t;
        String display;
        Handler handler;

        @FunctionalInterface
        interface Handler {

            public Object evaluate(Expression e, Context c);
        }

        @FunctionalInterface
        interface BILogic {

            public Object evaluate(double e, double c);
        }

        @FunctionalInterface
        interface RALogic {

            public Object evaluate(Rational e, Rational c);
        }

        public Object evaluate(Expression e, Context c) {
            if (handler != null) return handler.evaluate(e, c);
            throw new RuntimeException("no behaviour defined: " + this.toString());
        }

        Operation(OPType t, String display) {
            this.t = t;
            this.display = display;
        }

        Operation(OPType t, String display, Handler h) {
            this.t = t;
            this.display = display;
            this.handler = h;
        }
        /*
         * @Override
         * public String toString() {
         * return display;
         * }
         */

    }

    static class Snippet {

        public String toString() {
            if (s != 0) return String.valueOf(s);// String.format("'%s'", String.valueOf(s));
            return ex.toString();

        };

        Expression ex;
        char s = 0;

        char getc() {

            return s;
        }

        public byte type;

        public boolean isEx() {
            return type == 0;
        }

        public boolean isChar() {
            return type == 1;
        }

        Snippet(Expression ex) {
            this.ex = ex;
            type = 0;
        }

        Snippet(char s) {
            this.s = s;
            type = 1;
        }

    }

    public static class Expression {

        @Override
        public String toString() {
            return String.valueOf(this.op) + (complete ? "" : "*") + sub.toString();
        }

        public Object evaluate(Context c) {

            return this.op.evaluate(this, c);
        }

        boolean complete;

        public String checkCompleteOpt() {
            if (complete) {
                return "true";
            }
            for (Snippet s : sub) {
                if (s.isChar()) return "false";
                if (s.isEx()) {
                    String opt = s.ex.checkCompleteOpt();
                    if (opt == "false") {
                        return "false";
                    } ;
                    if (opt == "changed") {
                        return "changed";
                    }
                }

            }
            // more check

            complete = true;
            return "changed";
        }

        public boolean checkComplete() {
            if (complete) {
                return true;
            }
            for (Snippet s : sub) {
                if (s.isChar()) return false;
                if (s.isEx()) {
                    if (s.ex.checkComplete() == false) {
                        return false;
                    } ;

                }

            }
            // more check

            complete = true;
            return true;
        }

        Operation op = Operation.BRC_;
        // String raw;
        ArrayList<Snippet> sub = new ArrayList<>();

        Expression(ArrayList<Snippet> sub) {
            this.sub = sub;
        }

        public Expression(String s) {

            // raw=s;
            for (char c : s.toCharArray()) {
                sub.add(new Snippet(c));
            }
        }

        public boolean parse() {
            if (complete) return false;
            if (pairBracket()) return true;
            if (tryParseAsLiteral()) return true;
            if (comma()) return true;
            if (biop()) return true;

            for (Snippet s : sub) {
                if (s.isEx()) {
                    if (s.ex.parse()) {
                        return true;
                    }

                }
            }
            if (agressiveParseAsLiteral()) return true;
            for (Snippet s : sub) {
                if (s.isChar()) throw new RuntimeException("unrecognized char");

            }
            if (function()) return true;
            return false;
        }

        public boolean comma() {
            ArrayList<Snippet> tmp = new ArrayList<Snippet>();
            ArrayList<ArrayList<Snippet>> s = new ArrayList<>();
            boolean dirty = false;
            for (int i = 0; i < sub.size(); i++) {
                char c = sub.get(i)
                    .getc();
                if (c == ',') {
                    dirty = true;
                    s.add(tmp);
                    tmp = new ArrayList<Snippet>();
                } else {
                    tmp.add(sub.get(i));
                }

            }
            if (tmp.isEmpty() == false) s.add(tmp);
            if (dirty) {
                sub.clear();
                ArrayList<Snippet> o = (s).stream()
                    .map(Expression::new)
                    .map(Snippet::new)
                    .collect(ArrayList<Snippet>::new, List::add, (a, b) -> { throw new RuntimeException(); });
                Expression oo = new Expression(o);
                sub.add(new Snippet(oo));

                // s.forEach(sub::addAll);

                // op==Operation.BRC
                // oo.op=Operation.CMM;
                nest(this, op == Operation.BRC, false, Operation.CMM);
            }
            return dirty;
        }

        public boolean function() {
            boolean bracket = op == Operation.BRC || op == Operation.BRC_;
            if (!bracket) return false;

            if (sub.size() < 2) return false;
            Expression ex = sub.get(0).ex;
            Expression ex2 = sub.get(1).ex;
            /*
             * if(ex.op==Operation.VAR||ex.op==Operation.FUN){
             * ex.op=Operation.FUN;
             * op=Operation.CALL;
             * }
             * if(ex.op==Operation.VAR||ex.op==Operation.BRC){
             * //ex.op=Operation.FUN;
             * op=Operation.CALL;
             * }
             */
            if ((ex.op == Operation.VAR || ex.op == Operation.FUN) && ex2.op == Operation.BRC) {
                ex.op = Operation.FUN;
                op = Operation.CALL;
            }

            return false;
        }

        public boolean biop() {
            boolean bracket = op == Operation.BRC_;
            int m2 = -1;
            int m1 = -1;
            for (int i = sub.size() - 1; i >= 0; i--) {
                char c = sub.get(i)
                    .getc();

                if (c == ':' && (m2 == -1)) m2 = i;
                if (c == '?' && (m2 != -1)) {
                    m1 = i;
                    Snippet condition = new Snippet(new Expression(new ArrayList<>(sub.subList(0, m1))));
                    Snippet left = new Snippet(new Expression(new ArrayList<>(sub.subList(m1 + 1, m2))));
                    Snippet right = new Snippet(new Expression(new ArrayList<>(sub.subList(m2 + 1, sub.size()))));
                    sub.clear();
                    sub.add(condition);
                    sub.add(left);
                    sub.add(right);

                    nest(this, !bracket, false, Operation.SEL);
                    return true;
                }

            }

            for (int i = sub.size() - 1; i >= 0; i--) {

                char c = sub.get(i)
                    .getc();
                pass: if (c == '+' || c == '-') {
                    if (i == 0 || i == sub.size() - 1) {
                        if (c == '-') {
                            sub.add(0, new Snippet('0'));
                            break pass;
                        }
                        throw new RuntimeException("operand missing");
                    }
                    Snippet left = new Snippet(new Expression(new ArrayList<>(sub.subList(0, i))));
                    Snippet right = new Snippet(new Expression(new ArrayList<>(sub.subList(i + 1, sub.size()))));
                    sub.clear();
                    sub.add(left);
                    sub.add(right);
                    switch (c) {
                        case '+':
                            nest(this, !bracket, false, Operation.ADD);
                            break;
                        case '-':
                            nest(this, !bracket, false, Operation.SUB);
                    }
                    return true;
                }

            }

            for (int i = sub.size() - 1; i >= 0; i--) {
                char c = sub.get(i)
                    .getc();
                if (c == '*' || c == '/' || c == '%') {
                    if (i == 0 || i == sub.size() - 1) {
                        throw new RuntimeException("operand missing");
                    }
                    Snippet left = new Snippet(new Expression(new ArrayList<>(sub.subList(0, i))));
                    Snippet right = new Snippet(new Expression(new ArrayList<>(sub.subList(i + 1, sub.size()))));
                    sub.clear();
                    sub.add(left);
                    sub.add(right);
                    switch (c) {
                        case '*':
                            nest(this, !bracket, false, Operation.MUL);
                            break;
                        case '/':
                            nest(this, !bracket, false, Operation.DIV);
                            break;
                        case '%':
                            nest(this, !bracket, false, Operation.MOD);
                    }
                    return true;
                }

            }
            char prev = 0;
            for (int i = sub.size() - 1; i >= 0; i--) {
                char c = sub.get(i)
                    .getc();
                if ((c == '=' || c == '<' || c == '>' || c == '!') && prev == '=') {
                    if (i == 0 || i >= sub.size() - 2) {
                        throw new RuntimeException("operand missing");
                    }
                    Snippet left = new Snippet(new Expression(new ArrayList<>(sub.subList(0, i))));
                    Snippet right = new Snippet(new Expression(new ArrayList<>(sub.subList(i + 2, sub.size()))));
                    sub.clear();
                    sub.add(left);
                    sub.add(right);

                    switch (c) {
                        case '=':
                            nest(this, !bracket, false, Operation.EQ);
                            break;
                        case '<':
                            nest(this, !bracket, false, Operation.LE);
                            break;
                        case '>':
                            nest(this, !bracket, false, Operation.GE);
                            break;
                        case '!':
                            nest(this, !bracket, false, Operation.NEQ);
                            break;

                    }
                    return true;
                }

                if (c == '<' || c == '>') {
                    if (i == 0 || i >= sub.size() - 1) {
                        throw new RuntimeException("operand missing");
                    }
                    Snippet left = new Snippet(new Expression(new ArrayList<>(sub.subList(0, i))));
                    Snippet right = new Snippet(new Expression(new ArrayList<>(sub.subList(i + 1, sub.size()))));
                    sub.clear();
                    sub.add(left);
                    sub.add(right);
                    switch (c) {

                        case '<':
                            nest(this, !bracket, false, Operation.L);
                            break;
                        case '>':
                            nest(this, !bracket, false, Operation.G);
                            break;

                    }

                    return true;
                }

                prev = c;

            }

            return false;
        }

        public boolean pairBracket() {
            int firstleft = -1;
            int numleft = 0;
            for (int i = 0; i < sub.size(); i++) {
                if (sub.get(i).ex != null) {
                    if (sub.get(i).ex.pairBracket()) {
                        return true;
                    } ;
                }
                Snippet s = sub.get(i);
                if (s.getc() == '(') {
                    numleft++;
                    if (firstleft == -1) {
                        firstleft = i;
                    }
                }
                if (s.getc() == ')') {
                    if (numleft == 0) {
                        throw new RuntimeException("unclosed bracket ')'");
                    }
                    if (numleft == 1) {
                        // found
                        sub.remove(firstleft);// (
                        ArrayList<Snippet> sb = new ArrayList<Snippet>();
                        for (int j = 0; j < i - firstleft - 1; j++) {
                            sb.add(sub.remove(firstleft));
                        }
                        sub.remove(firstleft);// )
                        Expression ex = new Expression(sb);
                        ex.op = Operation.BRC;
                        sub.add(firstleft, new Snippet(ex));

                        return true;
                    }
                    numleft--;
                }
            }
            if (numleft > 0) {
                throw new RuntimeException("unclosed bracket '('");
            }

            return false;
        }

        public boolean agressiveParseAsLiteral() {
            int start = -1;
            ArrayList<Snippet> sb = new ArrayList<>();
            for (int i = 0; i < sub.size(); i++) {
                if (sub.get(i)
                    .isChar() == false && start != -1) {
                    int end = i;
                    for (int j = 0; j < end - start; j++) sub.remove(start);
                    Expression ex = new Expression(sb);

                    ex.op = Operation.FUN;
                    ex.complete = true;
                    sub.add(start, new Snippet(ex));

                    return true;
                }
                if (sub.get(i)
                    .isChar()) {
                    if (start == -1) start = i;
                    sb.add(new Snippet(sub.get(i).s));
                }

            }

            return false;
        }

        public boolean tryParseAsLiteral() {
            if (op == Operation.BRC || op == Operation.BRC_) {} else {
                return false;
            }
            // if(sub.stream().filter(s->!s.isChar()).findAny().isPresent()){return false;};
            StringBuilder sb = sub.stream()
                .collect(StringBuilder::new, StringBuilder::append, (a, b) -> { throw new RuntimeException("how?"); });

            try {
                Integer.parseInt(sb.toString());

                nest(this, op == Operation.BRC, true, Operation.CST);
                /*
                 * Expression a=new Expression(new ArrayList<>(sub));sub.clear();
                 * a.op=Operation.CST;
                 * a.complete=true;
                 * sub.add(new Snippet(a));
                 */
                return true;
            } catch (Exception e) {}
            try {
                Float.parseFloat(sb.toString());
                nest(this, op == Operation.BRC, true, Operation.CST);
                /*
                 * Expression a=new Expression(new ArrayList<>(sub));sub.clear();
                 * a.op=Operation.CST;
                 * a.complete=true;
                 * sub.add(new Snippet(a));
                 */
                return true;
            } catch (Exception e) {}

            if (val.matcher(new seq(sub))
                .find()) {

                nest(this, op == Operation.BRC, true, Operation.VAR);
                /*
                 * Expression a=new Expression(new ArrayList<>(sub));sub.clear();
                 * a.op=Operation.VAR;
                 * a.complete=true;
                 * sub.add(new Snippet(a));
                 */

                return true;
            }

            return false;
        }
    }

    static class seq implements CharSequence {

        seq(List<Snippet> s) {
            this.s = s;
        }

        List<Snippet> s;

        @Override
        public char charAt(int index) {

            return s.get(index).s;
        }

        @Override
        public int length() {

            return s.size();
        }

        @Override
        public CharSequence subSequence(int arg0, int arg1) {
            throw new UnsupportedOperationException();
        }

    }

    public static Pattern val = Pattern.compile("^([_a-zA-Z0-9]|\\\"|)+$");// ("^[_a-zA-Z0-9]+$");

    public static void main(String[] args) {

        Expression e = new Expression("add(a,2)");
        while (e.parse()) {
            System.out.println(e);
        } ;
        System.out.println(e);
        e.checkComplete();
        System.out.println(e);
        System.out.println(
            e.evaluate(
                new Context().add("a", 12)
                    .add("add", list -> ((Number) list.get(0)).doubleValue() + ((Number) list.get(1)).doubleValue())));
        // System.out.println(e.checkComplete());System.out.println(e);
    }

    public static void nest(Expression e, boolean nest, boolean complete, Operation nt) {

        if (!nest) {
            e.op = nt;
            e.complete = complete;
            return;
        }
        Expression a = new Expression(new ArrayList<>(e.sub));
        e.sub.clear();
        a.op = nt;
        a.complete = complete;
        e.sub.add(new Snippet(a));

    }

    public static class Rational extends Number {

        private static final long serialVersionUID = 1645615648645315645L;
        public int num;
        public int den;

        public Rational(int num, int den) {
            this.num = num;
            this.den = den;
        }

        @Override
        public Object clone() {

            return new Rational(num, den);
        }

        @Override
        public double doubleValue() {

            return Double.valueOf(num) / den;
        }

        @Override
        public float floatValue() {

            return Float.valueOf(num) / den;
        }

        @Override
        public int intValue() {
            if (den == 0) throw new ArithmeticException();
            return Math.round(floatValue());
        }

        @Override
        public long longValue() {

            return intValue();
        }
    }

    static public class Context {

        public Context() {
            // TODO Auto-generated constructor stub
        }

        public Context(Context c) {
            this.parent = c;
        }

        private HashMap<String, Object> varibles = new HashMap<>();
        private Context parent;

        public Object get(String s) {
            Object tmp;
            if (parent != null && (tmp = parent.get(s)) != null) return tmp;
            return varibles.get(s);
        }

        public Context add(String k, Function<List<Object>, Object> v) {

            varibles.put(k, v);
            return this;
        }

        public Context add(String k, Number v) {

            varibles.put(k, v);
            return this;
        }
    }

  

}
