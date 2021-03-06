package act.metric;

import org.osgl.$;

public class MetricInfo {

    public static final String HTTP_HANDLER = "act:http";
    public static final String CREATE_CONTEXT = "act:http:create-ctx";
    public static final String CLASS_LOADING = "act:classload";
    public static final String JOB_HANDLER = "act:job";
    public static final String CLI_HANDLER = "act:cli";
    public static final String MAILER = "act:mail";
    public static final String EVENT_HANDLER = "act:event";
    public static final String ROUTING = "act:routing";
    public static final String PATH_SEPARATOR = Metric.PATH_SEPARATOR;

    private String name;
    private long count;
    private Long ns;

    MetricInfo(String name, long count) {
        this.name = name;
        this.count = count;
    }

    MetricInfo(String name, long ns, long count) {
        this.name = name;
        this.ns = ns;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public long getNs() {
        return ns;
    }

    public long getMs() {
        return ns / 1000L / 1000L;
    }

    public long getSeconds() {
        return ns / 1000L / 1000L / 1000L;
    }

    public long getCount() {
        return count;
    }

    public String getCountAsStr() {
        return CountScale.format(count);
    }

    public long getMsAvg() {
        return getMs() / count;
    }

    public String getAccumulated() {
        return DurationScale.format(ns);
    }

    public String getAvg() {
        return DurationScale.format(ns / count);
    }

    @Override
    public int hashCode() {
        return $.hc(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MetricInfo) {
            MetricInfo that = (MetricInfo) obj;
            return $.eq(name, that.name);
        }
        return false;
    }

    public enum Comparator {
        ;
        public static $.Comparator<MetricInfo> COUNTER = new $.Comparator<MetricInfo>() {
            @Override
            public int compare(MetricInfo m1, MetricInfo m2) {
                long l = m2.count - m1.count;
                if (l < 0) {
                    return -1;
                } else if (l == 0) {
                    return m2.name.compareTo(m1.name);
                }
                return 1;
            }
        };

        public static $.Comparator<MetricInfo> TIMER = new $.Comparator<MetricInfo>() {
            @Override
            public int compare(MetricInfo m1, MetricInfo m2) {
                long l;
                if (null == m1.ns) {
                    l = m2.count - m1.count;
                } else {
                    l = m2.getMsAvg() - m1.getMsAvg();
                }
                if (l < 0) {
                    return -1;
                } else if (l == 0) {
                    return m2.name.compareTo(m1.name);
                } else {
                    return 1;
                }
            }
        };
    }
}
