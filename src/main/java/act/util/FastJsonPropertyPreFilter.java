package act.util;

import act.data.util.StringOrPattern;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import org.osgl.util.C;
import org.osgl.util.FastStr;
import org.osgl.util.S;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Extend the function of {@link com.alibaba.fastjson.serializer.SimplePropertyPreFilter}
 * so it can properly handle the property filters defined with hierarchies, e.g. "foo.bar.name"
 */
public class FastJsonPropertyPreFilter implements PropertyPreFilter {

    /**
     * The properties separator pattern {@code [,;:]+}
     */
    public static final Pattern PROPERTY_SEPARATOR = Pattern.compile("[,;:]+");

    private final Set<String> includes = new HashSet<String>();
    private final Set<String> excludes = new HashSet<String>();
    private final List<String> fullPaths = C.newList();

    public FastJsonPropertyPreFilter(String... properties) {
        super();
        addIncludes(properties);
    }

    public void setFullPaths(List<String> ls) {
        fullPaths.clear();
        fullPaths.addAll(ls);
    }

    /**
     * Add name/path of the properties to be exported
     * <p>
     * It supports adding multiple properties in one string separated by
     * the {@link #PROPERTY_SEPARATOR}
     * </p>
     * <p>
     * It can add a multiple level path separated by "{@code .}" or "{@code /}
     * e.g. "{@code foo/bar}" or "{@code foo.bar}"
     * </p>
     *
     * @param properties the properties
     */
    public void addIncludes(String... properties) {
        addTo(includes, properties);
    }

    /**
     * Add name/path of the properties to be exported
     * <p>
     * It supports adding multiple properties in one string separated by
     * the {@link #PROPERTY_SEPARATOR}
     * </p>
     * <p>
     * It can add a multiple level path separated by "{@code .}" or "{@code /}
     * e.g. "{@code foo/bar}" or "{@code foo.bar}"
     * </p>
     *
     * @param properties the properties
     */
    public void addIncludes(Collection<String> properties) {
        String[] sa = new String[properties.size()];
        addIncludes(properties.toArray(sa));
    }

    /**
     * Add name/path of the properties to be banned
     * <p>
     * It supports adding multiple properties in one string separated by
     * the {@link #PROPERTY_SEPARATOR}
     * </p>
     * <p>
     * It can add a multiple level path separated by "{@code .}" or "{@code /}
     * e.g. "{@code foo/bar}" or "{@code foo.bar}"
     * </p>
     *
     * @param properties the properties
     */
    public void addExcludes(String... properties) {
        addTo(excludes, properties);
    }

    /**
     * Add name/path of the properties to be banned
     * <p>
     * It supports adding multiple properties in one string separated by
     * the {@link #PROPERTY_SEPARATOR}
     * </p>
     * <p>
     * It can add a multiple level path separated by "{@code .}" or "{@code /}
     * e.g. "{@code foo/bar}" or "{@code foo.bar}"
     * </p>
     *
     * @param properties the properties
     */
    public void addExcludes(Set<String> properties) {
        String[] sa = new String[properties.size()];
        addTo(excludes, properties.toArray(sa));
    }

    private void addTo(Set<String> set, String... properties) {
        for (String s : properties) {
            if (S.blank(s)) {
                continue;
            }
            String[] sa = PROPERTY_SEPARATOR.split(s);
            for (String s0 : sa) {
                addOneTo(set, s0);
            }
        }
    }

    private void addOneTo(Set<String> set, String path) {
        // use path's canonical form
        if (path.contains("/")) {
            path = path.replace('/', '.');
        }
        set.add(path);
    }

    @Override
    public boolean apply(JSONSerializer serializer, Object source, String name) {
        if (source == null) {
            return true;
        }

        // if context path is "$.bar.zee" or "$[0].bar.zee" and name is "foo"
        // then path should be "bar.zee.foo"
        String path;
        FastStr fs = FastStr.of(serializer.getContext().toString()).append('.').append(name);
        path = fs.substring(fs.indexOf('.') + 1); // skip the first "."

        return !matches(excludes, path, true) && (includes.isEmpty() || matches(includes, path, false));
    }

    private static final Pattern SQUARE_BLOCK = Pattern.compile("\\[.*\\]");
    private boolean matches(Set<String> paths, String path, boolean exclude) {
        if (path.contains("[")) {
            path = SQUARE_BLOCK.matcher(path).replaceAll("");
        }
        if (paths.contains(path)) {
            return true;
        }
        if (hasPattern(paths)) {
            return patternMatches(paths, path, exclude);
        }
        if (exclude) {
            return false;
        }
        path = path + ".";
        for (String s : paths) {
            if (s.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPattern(Collection<String> paths) {
        return S.join("", paths).contains("*");
    }

    private static Map<Collection<String>, List<StringOrPattern>> spCache = C.newMap();

    private boolean patternMatches(Set<String> paths, String path, boolean exclude) {
        List<StringOrPattern> spList = spList(paths);
        for (StringOrPattern sp : spList) {
            if (sp.matches(path)) {
                return true;
            } else if (!exclude && sp.isPattern()) {
                // check if it is the case that path is still at the upper level
                Pattern p = Pattern.compile(path + "(\\.)?" + sp.s());
                for (String fp : fullPaths) {
                    if (p.matcher(fp).matches()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<StringOrPattern> spList(Collection<String> strings) {
        List<StringOrPattern> ret = spCache.get(strings);
        if (null == ret) {
            ret = C.newList();
            for (String s : strings) {
                ret.add(new StringOrPattern(s));
            }
            spCache.put(strings, ret);
        }
        return ret;
    }
}
